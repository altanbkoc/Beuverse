package com.altankoc.socialmedia.beuverse.view.user

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.altankoc.socialmedia.R
import com.altankoc.socialmedia.databinding.ActivityViewUserBinding
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore

class ViewUserActivity : AppCompatActivity() {
    private lateinit var binding: ActivityViewUserBinding
    private lateinit var firestore: FirebaseFirestore



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityViewUserBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, 0)
            insets
        }




        val userid = intent.getStringExtra("userid")


        firestore = FirebaseFirestore.getInstance()

        firestore.collection("users").document(userid.toString())
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()){
                    val username = document.getString("username")
                    val nickname = document.getString("nickname")
                    val department = document.getString("department")
                    val aboutMe = document.getString("aboutMe")
                    val profileImage = document.getString("profileImage")

                    binding.textViewName.text = username
                    binding.textViewNickname.text = nickname
                    binding.textViewBolum.text = department
                    binding.textViewAbout.text = aboutMe

                    Glide.with(this)
                        .load(profileImage)
                        .placeholder(R.drawable.default_pp)
                        .error(R.drawable.default_pp)
                        .circleCrop()
                        .into(binding.imageViewProfile)
                }
            }
            .addOnFailureListener {
                // Hata logla veya kullanıcıya bildir
            }



        binding.ibBackHome.setOnClickListener {
            val intent = Intent(this@ViewUserActivity, UserActivity::class.java)
            startActivity(intent)
            finish()
        }
    }



}