package com.altankoc.socialmedia.beuverse.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.altankoc.socialmedia.beuverse.model.User
import com.altankoc.socialmedia.beuverse.repository.UserRepository
import kotlinx.coroutines.launch

class UserViewModel(private val userRepository: UserRepository) : ViewModel() {

    fun registerUser(
        email: String,
        password: String,
        username: String,
        nickname: String,
        department: String,
        callback: (String) -> Unit
    ) {
        viewModelScope.launch {
            val result = userRepository.registerUser(email, password, username, nickname, department)

            callback(result)
        }
    }


    fun loginUser(
        email: String,
        password: String,
        callback: (String) -> Unit
    ) {
        viewModelScope.launch {
            val result = userRepository.loginUser(email, password)
            callback(result)
        }
    }


    fun getUserProfile(uid: String, callback: (User?) -> Unit) {
        viewModelScope.launch {
            val user = userRepository.getUserProfile(uid)
            callback(user)
        }
    }

    // Kullanıcıyı güncelleme fonksiyonu
    fun updateUserProfile(
        uid: String,
        username: String,
        nickname: String,
        department: String,
        aboutMe: String,
        profileImage: String?,
        callback: (String) -> Unit
    ) {
        viewModelScope.launch {
            val result = userRepository.updateUserProfile(uid, username, nickname, department, aboutMe, profileImage)
            callback(result)
        }
    }

    fun logoutUser() {
        userRepository.logoutUser()
    }

}
