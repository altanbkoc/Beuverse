package com.altankoc.socialmedia.beuverse.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.altankoc.socialmedia.beuverse.model.Comment // Comment modelini import et
import com.altankoc.socialmedia.beuverse.model.Post
import com.altankoc.socialmedia.beuverse.repository.PostRepository
import kotlinx.coroutines.launch

class PostViewModel(private val repo: PostRepository) : ViewModel() {

    // Mevcut LiveData'lar
    private val _posts = MutableLiveData<List<Post>>()
    val posts: LiveData<List<Post>> = _posts

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage

    // YENİ EKLENEN LiveData: Seçili gönderinin yorumlarını tutmak için
    private val _comments = MutableLiveData<List<Comment>>()
    val comments: LiveData<List<Comment>> = _comments

    // YENİ EKLENEN LiveData: Yorum ekleme işleminin durumunu bildirmek için (opsiyonel)
    private val _commentAddedStatus = MutableLiveData<Boolean?>()
    val commentAddedStatus: LiveData<Boolean?> = _commentAddedStatus


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
                if (success) {
                    fetchPosts() // Yeni gönderi eklendikten sonra listeyi yenile
                    _errorMessage.value = null
                } else {
                    _errorMessage.value = "Paylaşım oluşturulamadı."
                }
            } catch (e: Exception) {
                _errorMessage.value = "Paylaşım hatası: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleLikePost(postId: String, userId: String) {
        // _isLoading.value = true // Anlık yükleme göstergesi için açılabilir
        viewModelScope.launch {
            try {
                val success = repo.toggleLikePost(postId, userId)
                if (success) {
                    fetchPosts() // Beğeni sonrası ana gönderi listesini yenile
                    _errorMessage.value = null
                } else {
                    _errorMessage.value = "Beğeni işlemi başarısız oldu."
                }
            } catch (e: Exception) {
                _errorMessage.value = "Beğeni hatası: ${e.message}"
            } finally {
                // _isLoading.value = false // Eğer üstte açıldıysa
            }
        }
    }

    // YENİ FONKSİYON: Bir gönderinin yorumlarını çekme
    fun fetchCommentsForPost(postId: String) {
        _isLoading.value = true // Yorumlar yüklenirken genel yükleme durumu
        viewModelScope.launch {
            try {
                _comments.value = repo.getCommentsForPost(postId)
                _errorMessage.value = null
            } catch (e: Exception) {
                _comments.value = emptyList() // Hata durumunda boş liste ata
                _errorMessage.value = "Yorumlar yüklenemedi: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // YENİ FONKSİYON: Gönderiye yorum ekleme
    fun addCommentToPost(postId: String, comment: Comment) {
        // _isLoading.value = true // Yorum eklenirken anlık yükleme durumu için
        _commentAddedStatus.value = null // Önceki durumu sıfırla
        viewModelScope.launch {
            try {
                val success = repo.addCommentToPost(postId, comment)
                if (success) {
                    // Yorum başarıyla eklendi, şimdi yorum listesini yenileyebiliriz
                    fetchCommentsForPost(postId) // Yorumları yeniden çekerek listeyi güncelle
                    // Ana gönderi listesindeki commentCount'ı da güncellemek için fetchPosts() çağrılabilir
                    // veya sadece ilgili post'u güncelleyip _posts LiveData'sını tetikleyebiliriz.
                    // Şimdilik fetchPosts() daha basit bir yaklaşım.
                    fetchPosts()
                    _errorMessage.value = null
                    _commentAddedStatus.value = true
                } else {
                    _errorMessage.value = "Yorum eklenemedi."
                    _commentAddedStatus.value = false
                }
            } catch (e: Exception) {
                _errorMessage.value = "Yorum ekleme hatası: ${e.message}"
                _commentAddedStatus.value = false
            } finally {
                // _isLoading.value = false // Eğer üstte açıldıysa
            }
        }
    }

    // Yorum eklendi durumunu sıfırlamak için (opsiyonel)
    fun clearCommentAddedStatus() {
        _commentAddedStatus.value = null
    }
}
