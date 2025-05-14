package com.altankoc.socialmedia.beuverse.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Comment(
    val commentId: String = "",
    val postId: String = "",
    val userId: String = "",
    val userUsername: String = "",
    val userProfileImage: String = "",
    val text: String = "",
    @ServerTimestamp
    val timestamp: Date? = null
)
