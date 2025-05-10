package com.altankoc.socialmedia.beuverse.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.altankoc.socialmedia.beuverse.model.Post
import com.altankoc.socialmedia.beuverse.repository.PostRepository
import kotlinx.coroutines.launch

class PostViewModel(private val repo: PostRepository) : ViewModel() {

    private val _posts = MutableLiveData<List<Post>>()
    val posts: LiveData<List<Post>> = _posts

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage

    fun fetchPosts() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                _posts.value = repo.getAllPosts()
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Gönderiler yüklenemedi: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addPost(post: Post, imageUri: Uri?) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val success = if (imageUri != null) {
                    repo.addPostWithImage(post, imageUri)
                } else {
                    repo.addPost(post)
                }
                if (success) fetchPosts()
            } catch (e: Exception) {
                _errorMessage.value = "Paylaşım hatası: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}