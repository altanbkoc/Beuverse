package com.altankoc.socialmedia.beuverse.view.user

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.altankoc.socialmedia.R
import com.altankoc.socialmedia.databinding.ActivityUserBinding

class UserActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityUserBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, 0)
            insets
        }


        val navController = supportFragmentManager
            .findFragmentById(R.id.fragmentContainerView)
            ?.findNavController()?:throw  IllegalStateException("Hata")

        binding.bottomNavBar.setupWithNavController(navController)


        binding.bottomNavBar.setOnItemReselectedListener { }

    }
}