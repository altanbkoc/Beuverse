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

    fun logoutUser() {
        userRepository.logoutUser()
    }

}
