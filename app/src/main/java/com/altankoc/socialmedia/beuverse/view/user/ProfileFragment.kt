package com.altankoc.socialmedia.beuverse.view.user

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.altankoc.socialmedia.R
import com.altankoc.socialmedia.beuverse.repository.UserRepository
import com.altankoc.socialmedia.beuverse.view.login.LoginActivity
import com.altankoc.socialmedia.beuverse.viewmodel.UserViewModel
import com.altankoc.socialmedia.beuverse.viewmodel.UserViewModelFactory
import com.altankoc.socialmedia.databinding.FragmentProfileBinding
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth


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
                    binding.textViewName.text = user.username
                    binding.textViewNickname.text = "@"+user.nickname
                    binding.textViewBolum.text = user.department
                    binding.textViewAbout.text = user.aboutMe
                    binding.loadingOverlay.visibility = View.GONE

                    // Profil resmi varsa, Glide ile yükle
                    val profileImageUrl = user.profileImage
                    if (profileImageUrl.isNotEmpty()) {
                        Glide.with(this)
                            .load(profileImageUrl)
                            .placeholder(R.drawable.default_pp)
                            .error(R.drawable.default_pp)
                            .into(binding.imageViewProfile) // imageViewProfile, profil resmini gösterecek olan ImageView
                    } else {
                        binding.imageViewProfile.setImageResource(R.drawable.default_pp) // Varsayılan profil resmi
                    }

                } else {
                    Toast.makeText(requireContext(), "Kullanıcı bilgileri alınamadı", Toast.LENGTH_SHORT).show()
                }
            }
        }





        binding.floatingActionButton.setOnClickListener {
            val popup = PopupMenu(requireContext(), binding.floatingActionButton)
            popup.menuInflater.inflate(R.menu.profile_menu, popup.menu)

            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_edit_profile -> {
                        // Profili düzenleye git
                        val intent = Intent(requireActivity(), EditActivity::class.java)
                        startActivity(intent)
                        true
                    }
                    R.id.action_logout -> {
                        // Oturumu kapat
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Çıkış")
                            .setIcon(R.drawable.baseline_power_settings_new_24)
                            .setMessage("Çıkış yapmak istediğinize emin misiniz?")
                            .setPositiveButton("Evet") { _, _ ->
                            binding.loadingOverlay.visibility = View.VISIBLE
                                binding.root.postDelayed({
                                    userViewModel.logoutUser()
                                    val intent = Intent(requireContext(), LoginActivity::class.java)
                                    startActivity(intent)
                                    requireActivity().finish()
                                },1500)
                            }
                            .setNegativeButton("Hayır", null)
                            .show()

                        true
                    }
                    else -> false
                }
            }

            popup.show()
        }



    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}