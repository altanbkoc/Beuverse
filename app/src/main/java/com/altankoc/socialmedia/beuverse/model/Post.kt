package com.altankoc.socialmedia.beuverse.model

data class Post(
    val postId: String = "",
    val userId: String = "",
    val userUsername: String = "",
    val userNickname: String = "",
    val userProfileImage: String = "",
    val tag: String = "",
    val explanation: String = "",
    val imageUrl: String = "",
    val timestamp: Long = 0L
)
