package com.altankoc.socialmedia.beuverse.view.user

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
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
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    private val postViewModel: PostViewModel by viewModels()
    private var selectedImageUri: Uri? = null
    private var isPostInProgress = false

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) openGallery() else
            ImageUtils.showToast(requireContext(), "Depolama izni verilmedi!")
    }

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                Glide.with(this)
                    .load(uri)
                    .into(binding.imageViewPaylas)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val repo = PostRepository()
        val factory = PostViewModelFactory(repo)
        val postViewModel: PostViewModel = ViewModelProvider(this, factory)[PostViewModel::class.java]

        binding.imageViewPaylas.setOnClickListener {
            if (!isPostInProgress) {
                ImageUtils.checkAndRequestPermission(
                    activity = requireActivity(),
                    view = it,
                    permissionLauncher = permissionLauncher,
                    onPermissionGranted = ::openGallery
                )
            }
        }

        binding.buttonPaylas.setOnClickListener {
            if (!isPostInProgress) {
                validateAndSharePost()
            }
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        galleryLauncher.launch(intent)
    }

    private fun validateAndSharePost() {
        val explanation = binding.editTextPaylas.text.toString().trim()
        val selectedTagId = binding.radioGroup.checkedRadioButtonId.takeIf { it != -1 }
            ?: run {
                ImageUtils.showToast(requireContext(), "Lütfen bir etiket seçin!")
                return
            }

        if (explanation.isEmpty() && selectedImageUri == null) {
            ImageUtils.showToast(requireContext(), "En az bir içerik (yazı veya resim) ekleyin!")
            return
        }

        val currentUser = auth.currentUser ?: run {
            ImageUtils.showToast(requireContext(), "Kullanıcı girişi yapılmamış!")
            return
        }

        startPostingProcess(currentUser.uid, explanation, selectedTagId)
    }

    private fun startPostingProcess(userId: String, explanation: String, selectedTagId: Int) {
        isPostInProgress = true
        showLoading()

        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                val post = createPost(document, selectedTagId, explanation)

                selectedImageUri?.let { uri ->
                    uploadImageAndPost(post, uri)
                } ?: run {
                    postViewModel.addPost(post, null)
                    completePostingProcess()
                }
            }
            .addOnFailureListener { e ->
                handlePostingFailure("Kullanıcı bilgileri alınamadı: ${e.message}")
            }
    }

    private fun uploadImageAndPost(post: Post, imageUri: Uri) {
        val storageRef = storage.getReference("posts/${post.postId}")

        storageRef.putFile(imageUri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    val updatedPost = post.copy(imageUrl = uri.toString())
                    postViewModel.addPost(updatedPost, null)
                    completePostingProcess()
                }
                    .addOnFailureListener { e ->
                        handlePostingFailure("Resim URL'si alınamadı: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                handlePostingFailure("Resim yüklenemedi: ${e.message}")
            }
    }

    private fun createPost(
        userDocument: com.google.firebase.firestore.DocumentSnapshot,
        selectedTagId: Int,
        explanation: String
    ): Post {
        val selectedTag = binding.radioGroup.findViewById<RadioButton>(selectedTagId).text.toString()

        return Post(
            postId = UUID.randomUUID().toString(),
            userId = auth.currentUser?.uid ?: "",
            userUsername = userDocument.getString("username") ?: "",
            userNickname = userDocument.getString("nickname") ?: "",
            userProfileImage = userDocument.getString("profileImage") ?: "",
            tag = selectedTag,
            explanation = explanation,
            timestamp = System.currentTimeMillis(),
            imageUrl = ""
        )
    }

    private fun showLoading() {
        binding.loadingOverlay.visibility = View.VISIBLE
        binding.buttonPaylas.isEnabled = false
        binding.imageViewPaylas.isEnabled = false
    }

    private fun hideLoading() {
        binding.loadingOverlay.visibility = View.GONE
        binding.buttonPaylas.isEnabled = true
        binding.imageViewPaylas.isEnabled = true
    }

    private fun resetForm() {
        binding.editTextPaylas.setText("")
        binding.radioGroup.clearCheck()
        binding.imageViewPaylas.setImageResource(R.drawable.baseline_image_search_24)
        selectedImageUri = null
    }

    private fun completePostingProcess() {
        resetForm()
        hideLoading()
        isPostInProgress = false
        ImageUtils.showToast(requireContext(), "Paylaşım başarıyla oluşturuldu!")
    }

    private fun handlePostingFailure(errorMessage: String) {
        hideLoading()
        isPostInProgress = false
        ImageUtils.showToast(requireContext(), errorMessage)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}