package com.altankoc.socialmedia.beuverse.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.altankoc.socialmedia.beuverse.model.User

class UserRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()


    suspend fun registerUser(email: String,
                             password: String,
                             username: String,
                             nickname: String,
                             department: String)
    : String {
        return try {

            val result = auth.createUserWithEmailAndPassword(email, password).await()


            val user = User(
                uid = result.user?.uid ?: "",
                email = email,
                username = username,
                nickname = nickname,
                department = department,
                aboutMe = "",
                profileImage = ""
            )


            firestore.collection("users").document(result.user?.uid!!).set(user).await()

            "Kayıt başarılı!"
        } catch (e: Exception) {
            e.message ?: "Kayıt sırasında hata oluştu!"
        }
    }



    suspend fun loginUser(email: String, password: String): String {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            "Giriş başarılı!"
        } catch (e: Exception) {
            e.message ?: "Giriş sırasında hata oluştu!"
        }
    }


    suspend fun getUserProfile(uid: String): User? {
        return try {
            val document = firestore.collection("users").document(uid).get().await()
            if (document.exists()) {
                document.toObject(User::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    // Kullanıcı bilgilerini güncelleme fonksiyonu
    suspend fun updateUserProfile(
        uid: String,
        username: String,
        nickname: String,
        department: String,
        aboutMe: String,
        profileImage: String?
    ): String {
        return try {
            val user = User(
                uid = uid,
                email = "", // E-posta güncellenmez, mevcut kullanıcıdan alınabilir
                username = username,
                nickname = nickname,
                department = department,
                aboutMe = aboutMe,
                profileImage = profileImage ?: ""
            )

            // Kullanıcı verilerini güncelle
            firestore.collection("users").document(uid).update(
                "username", user.username,
                "nickname", user.nickname,
                "department", user.department,
                "aboutMe", user.aboutMe,
                "profileImage", user.profileImage
            ).await()

            "Profil güncelleme başarılı!"
        } catch (e: Exception) {
            e.message ?: "Profil güncellenirken hata oluştu!"
        }
    }


    fun logoutUser() {
        auth.signOut()
    }

}
