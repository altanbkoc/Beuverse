package com.altankoc.socialmedia.beuverse.view.login

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.altankoc.socialmedia.beuverse.repository.UserRepository
import com.altankoc.socialmedia.beuverse.util.Departments
import com.altankoc.socialmedia.beuverse.viewmodel.UserViewModel
import com.altankoc.socialmedia.beuverse.viewmodel.UserViewModelFactory
import com.altankoc.socialmedia.databinding.FragmentRegisterBinding


class RegisterFragment : Fragment() {
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private lateinit var userViewModel: UserViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userRepository = UserRepository()
        val factory = UserViewModelFactory(userRepository)
        userViewModel = ViewModelProvider(this,factory).get(UserViewModel::class.java)




        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, Departments.departments)
        binding.autoCompleteBolum.setAdapter(adapter)


        binding.buttonKayit.setOnClickListener {
            val email = binding.editTextEmail.text.toString().trim()
            val password = binding.editTextPw.text.toString()
            val username = binding.editTextUsername.text.toString().trim()
            val nickname = binding.editTextNickname.text.toString().trim()
            val department = binding.autoCompleteBolum.text.toString().trim()
            val passwordConfirm = binding.editTextPwConfirm.text.toString().trim()

            if (email.isEmpty() || password.isEmpty() || passwordConfirm.isEmpty() || username.isEmpty() || nickname.isEmpty() || department.isEmpty()) {
                Toast.makeText(requireContext(), "Lütfen tüm alanları doldurun!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!email.endsWith("@mf.karaelmas.edu.tr")) {
                Toast.makeText(requireContext(), "Sadece okul e-postası ile kayıt olabilirsiniz!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 8) {
                Toast.makeText(requireContext(), "Şifre en az 8 karakter olmalıdır!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != passwordConfirm) {
                Toast.makeText(requireContext(), "Şifreler eşleşmiyor!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            userViewModel.registerUser(email, password, username, nickname, department) { result ->
                if (result == "Kayıt başarılı!") {
                    binding.loadingOverlay.visibility = View.VISIBLE
                    binding.root.postDelayed({
                        val intent = Intent(requireActivity(), VerificationActivity::class.java)
                        startActivity(intent)
                        requireActivity().finish()
                    }, 1000)
                } else {
                    Toast.makeText(requireContext(), result, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}