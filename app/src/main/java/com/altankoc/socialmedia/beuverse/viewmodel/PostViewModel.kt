package com.altankoc.socialmedia.beuverse.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.altankoc.socialmedia.beuverse.model.Post
import com.altankoc.socialmedia.beuverse.repository.PostRepository
import kotlinx.coroutines.launch

class PostViewModel(private val postRepository: PostRepository) : ViewModel() {
    private val _posts = MutableLiveData<List<Post>>()
    val posts: LiveData<List<Post>> get() = _posts

    private val _postStatus = MutableLiveData<Boolean>()
    val postStatus: LiveData<Boolean> get() = _postStatus

    fun addPost(post: Post) {  // Sadece Post objesi alıyor
        viewModelScope.launch {
            val isSuccess = postRepository.addPost(post)
            _postStatus.value = isSuccess
        }
    }

    fun fetchPosts() {
        viewModelScope.launch {
            _posts.value = postRepository.getAllPosts()
        }
    }
}