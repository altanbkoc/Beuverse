package com.altankoc.socialmedia.beuverse.view.user

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.altankoc.socialmedia.R
import com.altankoc.socialmedia.beuverse.repository.UserRepository
import com.altankoc.socialmedia.beuverse.util.ImageUtils
import com.altankoc.socialmedia.beuverse.viewmodel.UserViewModel
import com.altankoc.socialmedia.beuverse.viewmodel.UserViewModelFactory
import com.altankoc.socialmedia.databinding.ActivityEditBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.firebase.auth.FirebaseAuth

class EditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditBinding
    private lateinit var userViewModel: UserViewModel
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>

    private var selectedImageUri: Uri? = null
    private var currentProfileImageUrl: String = ""
    private val currentUser = FirebaseAuth.getInstance().currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityEditBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViewModel()
        setupClickListeners()
        registerLaunchers()
        loadUserData()
    }

    private fun initViewModel() {
        val factory = UserViewModelFactory(UserRepository())
        userViewModel = ViewModelProvider(this, factory)[UserViewModel::class.java]
    }

    private fun loadUserData() {
        currentUser?.let { user ->
            userViewModel.getUserProfile(user.uid) { profile ->
                profile?.let {
                    binding.apply {
                        profilEditTextNickname.setText(it.nickname)
                        profilEditTextAdSoyad.setText(it.username)
                        profilEditTextMail.setText(it.email)
                        profilEditTextAboutMe.setText(it.aboutMe)
                        profilEditTextDepartment.setText(it.department)

                        currentProfileImageUrl = it.profileImage
                        loadProfileImage(it.profileImage)
                    }
                }
            }
        }
    }

    private fun loadProfileImage(imageUrl: String) {
        Glide.with(this)
            .load(imageUrl.ifEmpty { R.drawable.default_pp })
            .transition(DrawableTransitionOptions.withCrossFade())
            .circleCrop()
            .error(R.drawable.default_pp)
            .into(binding.imageViewProfile)
    }

    private fun setupClickListeners() {
        binding.profilCloseMageButton.setOnClickListener {
            finish()
        }

        binding.imageViewProfile.setOnClickListener {
            ImageUtils.checkAndRequestPermission(
                activity = this,
                view = it,
                permissionLauncher = permissionLauncher
            ) {
                ImageUtils.openGallery(galleryLauncher)
            }
        }

        binding.profilKaydetSaveButton.setOnClickListener {
            updateProfile()
        }
    }

    private fun updateProfile() {
        currentUser?.let { user ->
//            binding.progressBar.visibility = View.VISIBLE

            val updatedUser = mapOf(
                "username" to binding.profilEditTextAdSoyad.text.toString().trim(),
                "nickname" to binding.profilEditTextNickname.text.toString().trim(),
                "department" to binding.profilEditTextDepartment.text.toString().trim(),
                "aboutMe" to binding.profilEditTextAboutMe.text.toString().trim()
            )

            when {
                selectedImageUri != null -> {
                    // Yeni resimle güncelle
                    userViewModel.updateProfileWithImage(
                        userId = user.uid,
                        username = updatedUser["username"]!!,
                        nickname = updatedUser["nickname"]!!,
                        department = updatedUser["department"]!!,
                        aboutMe = updatedUser["aboutMe"]!!,
                        oldImageUrl = currentProfileImageUrl,
                        newImageUri = selectedImageUri!!
                    ) { result ->
                        handleUpdateResult(result)
                    }
                }
                currentProfileImageUrl.isNotEmpty() -> {
                    // Resmi değiştirmeden güncelle
                    userViewModel.updateUserProfile(
                        uid = user.uid,
                        username = updatedUser["username"]!!,
                        nickname = updatedUser["nickname"]!!,
                        department = updatedUser["department"]!!,
                        aboutMe = updatedUser["aboutMe"]!!,
                        profileImage = currentProfileImageUrl
                    ) { result ->
                        handleUpdateResult(result)
                    }
                }
                else -> {
                    // Hiç resim yokken güncelle
                    userViewModel.updateUserProfile(
                        uid = user.uid,
                        username = updatedUser["username"]!!,
                        nickname = updatedUser["nickname"]!!,
                        department = updatedUser["department"]!!,
                        aboutMe = updatedUser["aboutMe"]!!,
                        profileImage = ""
                    ) { result ->
                        handleUpdateResult(result)
                    }
                }
            }
        }
    }

    private fun handleUpdateResult(result: String) {
//        binding.progressBar.visibility = View.GONE
        Toast.makeText(this, result, Toast.LENGTH_SHORT).show()

        if (result.contains("başarılı", ignoreCase = true)) {
            setResult(RESULT_OK)
            finish()
        }
    }

    private fun registerLaunchers() {
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) ImageUtils.openGallery(galleryLauncher)
            else Toast.makeText(this, "Galeri izni gerekiyor", Toast.LENGTH_SHORT).show()
        }

        galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.data?.let { uri ->
                    selectedImageUri = uri
                    Glide.with(this)
                        .load(uri)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .circleCrop()
                        .into(binding.imageViewProfile)
                }
            }
        }
    }
}