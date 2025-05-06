package com.altankoc.socialmedia.beuverse.view.user

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.altankoc.socialmedia.R
import com.altankoc.socialmedia.beuverse.repository.UserRepository
import com.altankoc.socialmedia.beuverse.view.login.LoginActivity
import com.altankoc.socialmedia.beuverse.viewmodel.UserViewModel
import com.altankoc.socialmedia.beuverse.viewmodel.UserViewModelFactory
import com.altankoc.socialmedia.databinding.FragmentProfileBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var userViewModel: UserViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val userRepository = UserRepository()
        val factory = UserViewModelFactory(userRepository)
        userViewModel = ViewModelProvider(this, factory).get(UserViewModel::class.java)
        val currentUser = FirebaseAuth.getInstance().currentUser


        if (currentUser != null) {
            userViewModel.getUserProfile(currentUser.uid) { user ->
                if (user != null) {
                    binding.textmail.text = user.email
                    binding.textAd.text = user.username
                    binding.textNickname.text = user.nickname
                    binding.textBolum.text = user.department
                } else {
                    Toast.makeText(requireContext(), "Kullanıcı bilgileri alınamadı", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.buttonCikisYap.setOnClickListener {
            userViewModel.logoutUser()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }





    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}