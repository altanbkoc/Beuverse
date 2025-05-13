package com.altankoc.socialmedia.beuverse.model // Kendi paket adınızla değiştirin

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

// Yorum veri modeli
data class Comment(
    val commentId: String = "", // Yorumun benzersiz ID'si
    val postId: String = "",    // Yorumun ait olduğu gönderinin ID'si
    val userId: String = "",    // Yorumu yapan kullanıcının UID'si
    val userUsername: String = "", // Yorumu yapan kullanıcının adı/kullanıcı adı
    val userProfileImage: String = "", // Yorumu yapan kullanıcının profil resmi URL'si
    val text: String = "",      // Yorum metni
    @ServerTimestamp // Firestore sunucu zaman damgasını kullanır
    val timestamp: Date? = null // Yorumun oluşturulma zamanı
)
