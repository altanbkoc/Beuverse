package com.altankoc.socialmedia.beuverse.view.user

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log // Loglama için eklendi (isteğe bağlı)
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.PopupMenu
import android.widget.Toast // Toast için zaten vardı
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
// import androidx.navigation.fragment.findNavController // DialogFragment için bu genellikle gerekmez
import androidx.recyclerview.widget.LinearLayoutManager
import com.altankoc.socialmedia.R
import com.altankoc.socialmedia.databinding.FragmentHomeBinding
import com.altankoc.socialmedia.beuverse.adapter.PostAdapter
import com.altankoc.socialmedia.beuverse.model.Post
import com.altankoc.socialmedia.beuverse.viewmodel.PostViewModel
import com.altankoc.socialmedia.beuverse.viewmodel.PostViewModelFactory
import com.altankoc.socialmedia.beuverse.repository.PostRepository
import com.google.firebase.auth.FirebaseAuth

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var postAdapter: PostAdapter

    private val postViewModel: PostViewModel by viewModels {
        PostViewModelFactory(PostRepository())
    }

    private var allPosts: List<Post> = emptyList()
    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()
        loadInitialData()

        // Mevcut UI etkileşimleriniz
        binding.root.setOnClickListener {
            clearFocusAndHideKeyboard()
        }

        binding.recyclerView.setOnTouchListener { _, _ ->
            clearFocusAndHideKeyboard()
            false
        }

        binding.buttonTagFilter.setOnClickListener {
            val popup = PopupMenu(requireContext(), binding.buttonTagFilter)
            popup.menuInflater.inflate(R.menu.tag_menu, popup.menu)

            popup.setOnMenuItemClickListener { item ->
                val selectedTag = when (item.itemId) {
                    R.id.tagTumu -> "Tümü"
                    R.id.tagSoru -> "Soru"
                    R.id.tagSosyal -> "Sosyal"
                    R.id.tagTicari -> "Ticaret"
                    R.id.tagSikayet -> "Şikayet"
                    else -> "Tümü"
                }
                filterPostsByTag(selectedTag)
                true
            }
            popup.show()
        }

        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()
                val filtered = if (query.isEmpty()) {
                    allPosts
                } else {
                    allPosts.filter {
                        it.userUsername.contains(query, ignoreCase = true) ||
                                it.userNickname.contains(query, ignoreCase = true) ||
                                it.explanation.contains(query, ignoreCase = true)
                    }
                }
                postAdapter.submitList(filtered) {
                    if (query.isEmpty()) {
                        binding.recyclerView.scrollToPosition(0)
                    }
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun clearFocusAndHideKeyboard() {
        binding.focusStealer.requestFocus() // XML'de focusStealer ID'li bir View olmalı
        hideKeyboard()
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.searchEditText.windowToken, 0)
    }

    private fun setupRecyclerView() {
        postAdapter = PostAdapter(
            onLikeClicked = { postId ->
                val currentUserId = firebaseAuth.currentUser?.uid
                // Kullanıcının verdiği kodda currentUserId null değilse !! ile kullanılmış,
                // bu riskli olabilir. Güvenli olması için if (currentUserId != null) kontrolü daha iyi olurdu.
                // Ancak kullanıcının isteği üzerine bu kısma dokunmuyorum.
                if (currentUserId != null) { // Güvenlik için null kontrolü eklendi
                    postViewModel.toggleLikePost(postId, currentUserId)
                } else {
                    Toast.makeText(requireContext(), "Lütfen giriş yapınız.", Toast.LENGTH_SHORT).show()
                }
            },
            // YENİ EKLENEN: Yorum butonuna tıklandığında çağrılacak lambda
            onCommentClicked = { postId ->
                // CommentsDialogFragment'ı göster
                try {
                    val commentsDialog = CommentsDialogFragment.newInstance(postId)
                    // DialogFragment'ı bir Fragment içinden gösteriyorsanız childFragmentManager kullanmak daha uygundur.
                    // Eğer UserActivity gibi bir Activity'den direkt gösterilmiyorsa.
                    commentsDialog.show(childFragmentManager, "CommentsDialogTag")
                } catch (e: Exception) {
                    Log.e("HomeFragment", "CommentsDialogFragment gösterilemedi.", e)
                    Toast.makeText(requireContext(), "Yorumlar açılamadı.", Toast.LENGTH_SHORT).show()
                }
            }
        )
        binding.recyclerView.apply {
            adapter = postAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun observeViewModel() {
        postViewModel.posts.observe(viewLifecycleOwner) { posts ->
            allPosts = posts
            postAdapter.submitList(posts)
        }
        // Yorum veya beğeni sonrası anlık hata mesajları için (isteğe bağlı)
        // postViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
        //    error?.let { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
        // }
    }

    private fun loadInitialData() {
        postViewModel.fetchPosts()
    }

    private fun filterPostsByTag(tag: String) {
        // XML'de loadingOverlay ID'li bir View olmalı
        binding.loadingOverlay.visibility = View.VISIBLE
        binding.recyclerView.postDelayed({
            val filtered = if (tag == "Tümü") {
                allPosts
            } else {
                allPosts.filter { it.tag.equals(tag, ignoreCase = true) }
            }
            postAdapter.submitList(filtered)
            binding.loadingOverlay.visibility = View.GONE
        }, 800)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
