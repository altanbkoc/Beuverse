package com.altankoc.socialmedia.beuverse.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.altankoc.socialmedia.beuverse.model.User
import com.altankoc.socialmedia.beuverse.repository.UserRepository
import kotlinx.coroutines.launch

class UserViewModel(private val userRepository: UserRepository) : ViewModel() {



    fun updateProfileWithImage(
        userId: String,
        username: String,
        nickname: String,
        department: String,
        aboutMe: String,
        oldImageUrl: String,
        newImageUri: Uri,
        callback: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // 1. Eski resmi sil
                userRepository.deleteOldProfileImage(oldImageUrl)

                // 2. Yeni resmi yükle
                val newUrl = userRepository.uploadProfileImage(userId, newImageUri)

                // 3. Profili güncelle
                val result = userRepository.updateUserProfile(
                    uid = userId,
                    username = username,
                    nickname = nickname,
                    department = department,
                    aboutMe = aboutMe,
                    profileImage = newUrl
                )
                callback(result)
            } catch (e: Exception) {
                callback(e.message ?: "Güncelleme başarısız")
            }
        }
    }


    fun uploadProfileImage(userId: String, imageUri: Uri, callback: (String?) -> Unit) {
        viewModelScope.launch {
            try {
                val imageUrl = userRepository.uploadProfileImage(userId, imageUri)
                callback(imageUrl)
            } catch (e: Exception) {
                callback(null)
            }
        }
    }

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
