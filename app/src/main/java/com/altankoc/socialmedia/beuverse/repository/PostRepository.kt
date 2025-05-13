package com.altankoc.socialmedia.beuverse.repository

import android.net.Uri
import android.util.Log
import com.altankoc.socialmedia.beuverse.model.Comment // Comment modelini import et
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.altankoc.socialmedia.beuverse.model.Post
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.lang.Exception

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
            val imageUrl = imageUri?.let { uploadPostImage(post.postId, it) } ?: ""
            val updatedPost = post.copy(imageUrl = imageUrl)
            postsCollection.document(post.postId).set(updatedPost).await()
            true
        } catch (e: Exception) {
            Log.e("PostRepository", "Error adding post with image: ${e.message}", e)
            false
        }
    }


    suspend fun addPost(post: Post): Boolean {
        return try {
            postsCollection.document(post.postId).set(post).await()
            true
        } catch (e: Exception) {
            Log.e("PostRepository", "Error adding post: ${e.message}", e)
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
            Log.e("PostRepository", "Error getting all posts: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun toggleLikePost(postId: String, userId: String): Boolean {
        val postRef = postsCollection.document(postId)
        return try {
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(postRef)
                // val currentLikes = snapshot.getLong("likeCount")?.toInt() ?: 0 // Bu satıra gerek yok
                val likedByList = snapshot.get("likedBy") as? List<String> ?: emptyList()

                if (likedByList.contains(userId)) {
                    transaction.update(postRef, "likeCount", FieldValue.increment(-1))
                    transaction.update(postRef, "likedBy", FieldValue.arrayRemove(userId))
                } else {
                    transaction.update(postRef, "likeCount", FieldValue.increment(1))
                    transaction.update(postRef, "likedBy", FieldValue.arrayUnion(userId))
                }
                null // Transaction başarılı olursa null döndürülür
            }.await()
            true
        } catch (e: Exception) {
            Log.e("PostRepository", "Error toggling like: ${e.message}", e)
            false
        }
    }

    // YENİ FONKSİYON: Gönderiye yorum ekleme
    suspend fun addCommentToPost(postId: String, comment: Comment): Boolean {
        return try {
            val postRef = postsCollection.document(postId)
            val commentRef = postRef.collection("comments").document() // Yorum için yeni ID oluştur

            firestore.runTransaction { transaction ->
                // 1. Yorumu comments alt koleksiyonuna ekle
                // Yorum ID'sini comment nesnesine ata (eğer Comment modelinde commentId varsa ve boşsa)
                val finalComment = if (comment.commentId.isEmpty()) {
                    comment.copy(commentId = commentRef.id, postId = postId)
                } else {
                    comment.copy(postId = postId) // postId'nin doğru olduğundan emin ol
                }
                transaction.set(commentRef, finalComment)

                // 2. Ana gönderideki commentCount'ı artır
                transaction.update(postRef, "commentCount", FieldValue.increment(1))
                null // Transaction başarılı
            }.await()
            true
        } catch (e: Exception) {
            Log.e("PostRepository", "Error adding comment: ${e.message}", e)
            false
        }
    }

    // YENİ FONKSİYON: Bir gönderinin yorumlarını çekme
    suspend fun getCommentsForPost(postId: String): List<Comment> {
        return try {
            val snapshot = postsCollection.document(postId)
                .collection("comments")
                .orderBy("timestamp", Query.Direction.ASCENDING) // Yorumları eskiden yeniye sırala
                .get()
                .await()
            snapshot.toObjects(Comment::class.java)
        } catch (e: Exception) {
            Log.e("PostRepository", "Error fetching comments: ${e.message}", e)
            emptyList()
        }
    }
}
