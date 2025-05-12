package com.altankoc.socialmedia.beuverse.view.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.altankoc.socialmedia.R
import com.altankoc.socialmedia.databinding.FragmentHomeBinding
import com.altankoc.socialmedia.beuverse.adapter.PostAdapter
import com.altankoc.socialmedia.beuverse.model.Post
import com.altankoc.socialmedia.beuverse.viewmodel.PostViewModel
import com.altankoc.socialmedia.beuverse.viewmodel.PostViewModelFactory
import com.altankoc.socialmedia.beuverse.repository.PostRepository

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var postAdapter: PostAdapter

    private val postViewModel: PostViewModel by viewModels {
        PostViewModelFactory(PostRepository())
    }

    private var allPosts: List<Post> = emptyList()

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
    }

    private fun setupRecyclerView() {
        postAdapter = PostAdapter()
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
    }

    private fun loadInitialData() {
        postViewModel.fetchPosts()
    }

    private fun filterPostsByTag(tag: String) {
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
