package com.altankoc.socialmedia.beuverse.view.user // Kendi paket adınızla değiştirin

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels // activityViewModels için
import androidx.lifecycle.ViewModelProvider // ViewModelProvider için (eğer activityViewModels kullanılmayacaksa)
import androidx.recyclerview.widget.LinearLayoutManager
import com.altankoc.socialmedia.R
import com.altankoc.socialmedia.beuverse.adapter.CommentAdapter
import com.altankoc.socialmedia.beuverse.model.Comment
import com.altankoc.socialmedia.beuverse.model.User // User modelini import et
import com.altankoc.socialmedia.beuverse.repository.PostRepository
import com.altankoc.socialmedia.beuverse.repository.UserRepository
import com.altankoc.socialmedia.beuverse.viewmodel.PostViewModel
import com.altankoc.socialmedia.beuverse.viewmodel.PostViewModelFactory
import com.altankoc.socialmedia.beuverse.viewmodel.UserViewModel // UserViewModel'i import et
import com.altankoc.socialmedia.beuverse.viewmodel.UserViewModelFactory // UserViewModelFactory'yi import et
import com.altankoc.socialmedia.databinding.FragmentCommentsDialogBinding // fragment_comments_dialog.xml için ViewBinding
import com.google.firebase.auth.FirebaseAuth
import java.util.Date

class CommentsDialogFragment : DialogFragment() {

    private var _binding: FragmentCommentsDialogBinding? = null
    private val binding get() = _binding!!

    private lateinit var commentAdapter: CommentAdapter
    private var postId: String? = null

    // PostViewModel'i Activity scope'unda veya parent Fragment scope'unda kullanmak daha iyi olabilir.
    // Eğer HomeFragment'tan açılıyorsa, parentFragment?.viewModels veya activityViewModels kullanılabilir.
    // Şimdilik activityViewModels kullanalım, bu UserActivity scope'unda bir ViewModel örneği paylaşır.
    private val postViewModel: PostViewModel by activityViewModels {
        PostViewModelFactory(PostRepository())
    }
    // Yorum yapan kullanıcının bilgilerini almak için UserViewModel
    private val userViewModel: UserViewModel by activityViewModels {
        UserViewModelFactory(UserRepository())
    }

    private var currentUserData: User? = null // Yorum yapan kullanıcının bilgilerini tutmak için

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            postId = it.getString(ARG_POST_ID)
        }
        // Kullanıcı bilgilerini önceden çek
        FirebaseAuth.getInstance().currentUser?.uid?.let { uid ->
            userViewModel.getUserProfile(uid) { user ->
                currentUserData = user
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCommentsDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()

        postId?.let {
            postViewModel.fetchCommentsForPost(it)
        } ?: run {
            Toast.makeText(context, "Gönderi ID'si bulunamadı.", Toast.LENGTH_SHORT).show()
            dismiss()
        }

        binding.buttonSendComment.setOnClickListener {
            sendComment()
        }
    }

    override fun onStart() {
        super.onStart()
        // Dialog boyutunu ayarla
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        // İsteğe bağlı: Dialog'un alt kısmında görünmesini sağlamak için
        // dialog?.window?.setGravity(Gravity.BOTTOM)
    }


    private fun setupRecyclerView() {
        commentAdapter = CommentAdapter()
        binding.recyclerViewComments.apply {
            adapter = commentAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun observeViewModel() {
        postViewModel.comments.observe(viewLifecycleOwner) { comments ->
            if (comments.isEmpty()) {
                binding.textViewNoComments.visibility = View.VISIBLE
                binding.recyclerViewComments.visibility = View.GONE
            } else {
                binding.textViewNoComments.visibility = View.GONE
                binding.recyclerViewComments.visibility = View.VISIBLE
                commentAdapter.submitList(comments)
            }
        }

        postViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBarComments.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        postViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                // Hata mesajı gösterildikten sonra ViewModel'de temizlenebilir
            }
        }

        postViewModel.commentAddedStatus.observe(viewLifecycleOwner) { status ->
            status?.let { success ->
                if (success) {
                    binding.editTextComment.text?.clear()
                    // İsteğe bağlı: "Yorum eklendi" mesajı
                    // Toast.makeText(context, "Yorumunuz eklendi.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Yorum eklenirken bir hata oluştu.", Toast.LENGTH_SHORT).show()
                }
                postViewModel.clearCommentAddedStatus() // Durumu sıfırla
            }
        }
    }

    private fun sendComment() {
        val commentText = binding.editTextComment.text.toString().trim()
        val currentPostId = postId
        val firebaseUser = FirebaseAuth.getInstance().currentUser

        if (commentText.isEmpty()) {
            binding.textInputLayoutComment.error = "Yorum boş olamaz."
            return
        } else {
            binding.textInputLayoutComment.error = null // Hatayı temizle
        }

        if (currentPostId == null) {
            Toast.makeText(context, "Gönderi ID'si bulunamadı.", Toast.LENGTH_SHORT).show()
            return
        }

        if (firebaseUser == null) {
            Toast.makeText(context, "Yorum yapmak için giriş yapmalısınız.", Toast.LENGTH_SHORT).show()
            // Burada giriş ekranına yönlendirme yapılabilir
            return
        }

        // currentUserData'nın yüklenmiş olmasını bekle veya kontrol et
        if (currentUserData == null) {
            // Kullanıcı verileri henüz yüklenmemişse, tekrar çekmeyi deneyebilir veya bir uyarı verebilirsiniz.
            // Şimdilik basit bir uyarı:
            Toast.makeText(context, "Kullanıcı bilgileri yükleniyor, lütfen tekrar deneyin.", Toast.LENGTH_SHORT).show()
            // Veya kullanıcı bilgilerini burada tekrar çek:
            userViewModel.getUserProfile(firebaseUser.uid) { user ->
                if (user != null) {
                    currentUserData = user
                    // Tekrar göndermeyi dene (recursive çağrıya dikkat, bir flag ile kontrol edilebilir)
                    if (binding.editTextComment.text.toString().trim().isNotEmpty()){ // Tekrar kontrol et
                        createAndSendComment(currentPostId, user, commentText)
                    }
                } else {
                    Toast.makeText(context, "Kullanıcı bilgileri alınamadı.", Toast.LENGTH_SHORT).show()
                }
            }
            return
        }
        createAndSendComment(currentPostId, currentUserData!!, commentText)
    }

    private fun createAndSendComment(postId: String, commenterInfo: User, text: String){
        val newComment = Comment(
            // commentId Firestore tarafından otomatik atanacak veya Repository'de atanabilir
            postId = postId,
            userId = commenterInfo.uid,
            userUsername = commenterInfo.username, // Veya nickname, hangisini göstermek isterseniz
            userProfileImage = commenterInfo.profileImage,
            text = text,
            timestamp = Date() // Firestore @ServerTimestamp bunu ezecek
        )
        postViewModel.addCommentToPost(postId, newComment)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        binding.recyclerViewComments.adapter = null // RecyclerView adaptörünü temizle
        _binding = null
    }

    companion object {
        private const val ARG_POST_ID = "post_id"

        @JvmStatic
        fun newInstance(postId: String) =
            CommentsDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_POST_ID, postId)
                }
            }
    }
}
