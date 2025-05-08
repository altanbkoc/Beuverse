package com.altankoc.socialmedia.beuverse.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.altankoc.socialmedia.beuverse.model.Post
import kotlinx.coroutines.tasks.await

class PostRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val postsCollection = firestore.collection("posts")

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