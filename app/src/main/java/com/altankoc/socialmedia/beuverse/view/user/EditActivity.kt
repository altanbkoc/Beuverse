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
import com.google.firebase.auth.FirebaseAuth

class EditActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditBinding

    private lateinit var userViewModel: UserViewModel


    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
    private var selectedImageUri: Uri? = null

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

        val userRepository = UserRepository()
        val factory = UserViewModelFactory(userRepository)
        userViewModel = ViewModelProvider(this, factory).get(UserViewModel::class.java)
        val currentUser = FirebaseAuth.getInstance().currentUser



        if(currentUser != null){
            userViewModel.getUserProfile(currentUser.uid) { user ->
                if (user != null) {
                  binding.profilEditTextNickname.setText(user.nickname)
                    binding.profilEditTextAdSoyad.setText(user.username)
                    binding.profilEditTextMail.setText(user.email)
                    binding.profilEditTextAboutMe.setText(user.aboutMe)
                    binding.profilEditTextDepartment.setText(user.department)


                    val profileImageUrl = user.profileImage
                    if (profileImageUrl.isNotEmpty()) {
                        Glide.with(this)
                            .load(profileImageUrl)
                            .placeholder(R.drawable.default_pp)
                            .error(R.drawable.default_pp)
                            .into(binding.imageViewProfile)
                    } else {
                        binding.imageViewProfile.setImageResource(R.drawable.default_pp)
                    }

                } else {
                }
            }
        }



        binding.profilCloseMageButton.setOnClickListener {
            val intent = Intent(this@EditActivity, UserActivity::class.java)
            startActivity(intent)
            finish()
        }


        registerLaunchers()
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
            val newNickname = binding.profilEditTextNickname.text.toString().trim()
            val newUsername = binding.profilEditTextAdSoyad.text.toString().trim()
            val newDepartment = binding.profilEditTextDepartment.text.toString().trim()
            val newAboutMe = binding.profilEditTextAboutMe.text.toString().trim()

            val profileImage = selectedImageUri?.toString()
            if (currentUser != null){

                userViewModel.updateUserProfile(
                    currentUser.uid,
                    newUsername,
                    newNickname,
                    newDepartment,
                    newAboutMe,
                    profileImage
                ){
                    val intent = Intent(this@EditActivity, UserActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }




    }


    private fun registerLaunchers() {
        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    ImageUtils.openGallery(galleryLauncher)
                } else {
                    ImageUtils.showToast(this, "Galeri erişim izni reddedildi.")
                }
            }

        galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
            if (result.resultCode == RESULT_OK){
                val intentData = result.data
                if (intentData != null){
                    selectedImageUri = intentData.data
                    selectedImageUri?.let { uri ->
                        Glide.with(this)
                            .load(uri)
                            .into(binding.imageViewProfile)
                    }
                }
            }
        }
    }


}