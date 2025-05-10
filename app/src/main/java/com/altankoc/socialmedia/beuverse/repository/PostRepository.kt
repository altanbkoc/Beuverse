package com.altankoc.socialmedia.beuverse.repository

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.altankoc.socialmedia.beuverse.model.Post
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class PostRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val postsCollection = firestore.collection("posts")
    private val storage = FirebaseStorage.getInstance()


    suspend fun uploadPostImage(postId: String, imageUri: Uri): String {
        val storageRef = storage.getReference("posts/$postId")
        storageRef.putFile(imageUri).await()
        return storageRef.downloadUrl.await().toString()
    }

    suspend fun addPostWithImage(post: Post, imageUri: Uri?): Boolean {
        return try {
            // Resmi yükle (varsa)
            val imageUrl = imageUri?.let { uploadPostImage(post.postId, it) } ?: ""

            // Postu güncelle
            val updatedPost = post.copy(imageUrl = imageUrl)
            postsCollection.document(post.postId).set(updatedPost).await()
            true
        } catch (e: Exception) {
            false
        }
    }


    suspend fun addPost(post: Post): Boolean {
        return try {
            postsCollection.document(post.postId).set(post).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getAllPosts(): List<Post> {
        return try {
            val snapshot = postsCollection
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            snapshot.toObjects(Post::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
}