package com.altankoc.socialmedia.beuverse.view.login

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.altankoc.socialmedia.beuverse.repository.UserRepository
import com.altankoc.socialmedia.beuverse.view.user.UserActivity
import com.altankoc.socialmedia.beuverse.viewmodel.UserViewModel
import com.altankoc.socialmedia.beuverse.viewmodel.UserViewModelFactory
import com.altankoc.socialmedia.databinding.FragmentLoginBinding
import com.google.firebase.auth.FirebaseAuth


class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var userViewModel: UserViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null && currentUser.isEmailVerified) {
            val intent = Intent(requireContext(), UserActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }

        val userRepository = UserRepository()
        val factory = UserViewModelFactory(userRepository)
        userViewModel = ViewModelProvider(this,factory).get(UserViewModel::class.java)

        binding.buttonGiris.setOnClickListener {
            val email = binding.editTextGirisMail.text.toString()
            val password = binding.editTextGirisPw.text.toString()

            userViewModel.loginUser(email, password) { result ->
                if (result == "Giriş başarılı!") {
                    binding.loadingOverlay.visibility = View.VISIBLE
                    binding.root.postDelayed({
                        startActivity(Intent(requireContext(), UserActivity::class.java))
                        requireActivity().finish()
                    }, 1000)
                } else {
                    Toast.makeText(requireContext(), result, Toast.LENGTH_LONG).show()
                }
            }
        }

        binding.textViewOrLoginWith.setOnClickListener {
            val action = LoginFragmentDirections.actionLoginFragmentToRegisterFragment()
            Navigation.findNavController(requireView()).navigate(action)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}