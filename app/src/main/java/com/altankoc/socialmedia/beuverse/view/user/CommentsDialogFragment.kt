package com.altankoc.socialmedia.beuverse.view.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.altankoc.socialmedia.beuverse.adapter.CommentAdapter
import com.altankoc.socialmedia.beuverse.model.Comment
import com.altankoc.socialmedia.beuverse.model.User
import com.altankoc.socialmedia.beuverse.repository.PostRepository
import com.altankoc.socialmedia.beuverse.repository.UserRepository
import com.altankoc.socialmedia.beuverse.viewmodel.PostViewModel
import com.altankoc.socialmedia.beuverse.viewmodel.PostViewModelFactory
import com.altankoc.socialmedia.beuverse.viewmodel.UserViewModel
import com.altankoc.socialmedia.beuverse.viewmodel.UserViewModelFactory
import com.altankoc.socialmedia.databinding.FragmentCommentsDialogBinding
import com.google.firebase.auth.FirebaseAuth
import java.util.Date

class CommentsDialogFragment : DialogFragment() {

    private var _binding: FragmentCommentsDialogBinding? = null
    private val binding get() = _binding!!

    private lateinit var commentAdapter: CommentAdapter
    private var postId: String? = null


    private val postViewModel: PostViewModel by activityViewModels {
        PostViewModelFactory(PostRepository())
    }
    private val userViewModel: UserViewModel by activityViewModels {
        UserViewModelFactory(UserRepository())
    }

    private var currentUserData: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            postId = it.getString(ARG_POST_ID)
        }
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
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

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
            }
        }

        postViewModel.commentAddedStatus.observe(viewLifecycleOwner) { status ->
            status?.let { success ->
                if (success) {
                    binding.editTextComment.text?.clear()

                } else {
                    Toast.makeText(context, "Yorum eklenirken bir hata oluştu.", Toast.LENGTH_SHORT).show()
                }
                postViewModel.clearCommentAddedStatus()
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
            binding.textInputLayoutComment.error = null
        }

        if (currentPostId == null) {
            Toast.makeText(context, "Gönderi ID'si bulunamadı.", Toast.LENGTH_SHORT).show()
            return
        }

        if (firebaseUser == null) {
            Toast.makeText(context, "Yorum yapmak için giriş yapmalısınız.", Toast.LENGTH_SHORT).show()
            return
        }

        if (currentUserData == null) {
            Toast.makeText(context, "Kullanıcı bilgileri yükleniyor, lütfen tekrar deneyin.", Toast.LENGTH_SHORT).show()
            userViewModel.getUserProfile(firebaseUser.uid) { user ->
                if (user != null) {
                    currentUserData = user
                    if (binding.editTextComment.text.toString().trim().isNotEmpty()){
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
            postId = postId,
            userId = commenterInfo.uid,
            userUsername = commenterInfo.username,
            userProfileImage = commenterInfo.profileImage,
            text = text,
            timestamp = Date()
        )
        postViewModel.addCommentToPost(postId, newComment)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        binding.recyclerViewComments.adapter = null
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
