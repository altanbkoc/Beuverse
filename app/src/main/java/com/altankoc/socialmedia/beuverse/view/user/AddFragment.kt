package com.altankoc.socialmedia.beuverse.view.user

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import com.altankoc.socialmedia.R
import com.altankoc.socialmedia.beuverse.model.Post
import com.altankoc.socialmedia.beuverse.repository.PostRepository
import com.altankoc.socialmedia.beuverse.util.ImageUtils
import com.altankoc.socialmedia.beuverse.viewmodel.PostViewModel
import com.altankoc.socialmedia.beuverse.viewmodel.PostViewModelFactory
import com.altankoc.socialmedia.databinding.FragmentAddBinding
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class AddFragment : Fragment() {
    private var _binding: FragmentAddBinding? = null
    private val binding get() = _binding!!

    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
    private var selectedImageUri: Uri? = null

    private lateinit var postViewModel: PostViewModel
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        val factory = PostViewModelFactory(PostRepository())
        postViewModel = ViewModelProvider(this, factory)[PostViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        registerLaunchers()

        binding.imageViewPaylas.setOnClickListener {
            ImageUtils.checkAndRequestPermission(
                activity = requireActivity(),
                view = it,
                permissionLauncher = permissionLauncher
            ) {
                ImageUtils.openGallery(galleryLauncher)
            }
        }

        binding.buttonPaylas.setOnClickListener {
                sharePost()
        }

        observeViewModel()
    }

    private fun showLoading() {
        binding.loadingOverlay.visibility = View.VISIBLE
        binding.loadingOverlay.isClickable = true // Arka plana tıklanmasını engeller
    }

    private fun hideLoading() {
        if (_binding != null) { // Null kontrolü ekleyin
            binding.loadingOverlay.visibility = View.GONE
        }
    }

    private fun sharePost() {
        val explanation = binding.editTextPaylas.text.toString().trim()
        val selectedTagId = binding.radioGroup.checkedRadioButtonId

        if (selectedTagId == -1) {
            ImageUtils.showToast(requireContext(), "Lütfen bir etiket seçin!")
            return
        }

        if (explanation.isEmpty() && selectedImageUri == null) {
            ImageUtils.showToast(requireContext(), "En az bir içerik (yazı veya resim) ekleyin!")
            return
        }

        val selectedTag = binding.radioGroup.findViewById<RadioButton>(selectedTagId).text.toString()
        val currentUser = firebaseAuth.currentUser ?: run {
            ImageUtils.showToast(requireContext(), "Kullanıcı girişi yapılmamış!")
            return
        }

        showLoading()


        FirebaseFirestore.getInstance().collection("users").document(currentUser.uid).get()
            .addOnSuccessListener { document ->
                val post = Post(
                    postId = UUID.randomUUID().toString(),
                    userId = currentUser.uid,
                    userUsername = document.getString("username") ?: "",
                    userNickname = document.getString("nickname") ?: "",
                    userProfileImage = document.getString("profileImage") ?: "",
                    tag = selectedTag,
                    explanation = explanation,
                    timestamp = System.currentTimeMillis(),
                    imageUrl = ""
                )

                if (selectedImageUri != null) {
                    uploadImageAndPost(post)
                } else {
                    postViewModel.addPost(post)
                }
            }
    }

    private fun uploadImageAndPost(post: Post) {
        val storageRef = FirebaseStorage.getInstance().reference
            .child("posts_images/${UUID.randomUUID()}")

        storageRef.putFile(selectedImageUri!!)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    val updatedPost = post.copy(imageUrl = uri.toString())
                    postViewModel.addPost(updatedPost)
                }
            }
            .addOnFailureListener {
                ImageUtils.showToast(requireContext(), "Resim yüklenemedi!")
            }
    }

    private fun observeViewModel() {
        postViewModel.postStatus.observe(viewLifecycleOwner) { isSuccess ->
            hideLoading()

            if (isSuccess) {
                binding.editTextPaylas.text?.clear()
                binding.radioGroup.clearCheck()
                binding.imageViewPaylas.setImageResource(R.drawable.baseline_image_search_24)
                selectedImageUri = null
            }
        }
    }


    private fun registerLaunchers() {
        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    ImageUtils.openGallery(galleryLauncher)
                } else {
                    ImageUtils.showToast(requireContext(), "Galeri erişim izni reddedildi!")
                }
            }

        galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val intentData = result.data
                if (intentData != null) {
                    selectedImageUri = intentData.data
                    selectedImageUri?.let { uri ->
                        Glide.with(this)
                            .load(uri)
                            .into(binding.imageViewPaylas)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        hideLoading()

    }
}
