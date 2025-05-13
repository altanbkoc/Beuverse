package com.altankoc.socialmedia.beuverse.model // Kendi paket adınızla değiştirin

// Post veri modeli
data class Post(
    val postId: String = "",
    val userId: String = "",
    val userUsername: String = "",
    val userNickname: String = "",
    val userProfileImage: String = "",
    val tag: String = "",
    val explanation: String = "",
    val imageUrl: String = "",
    val timestamp: Long = 0L,
    val likeCount: Int = 0,
    val likedBy: List<String> = emptyList(),
    // YENİ EKLENEN ALAN
    val commentCount: Int = 0 // Gönderinin toplam yorum sayısı
)
