package com.altankoc.socialmedia.beuverse.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.altankoc.socialmedia.R
import com.altankoc.socialmedia.beuverse.model.Post
import com.altankoc.socialmedia.beuverse.view.user.ViewUserActivity
import com.altankoc.socialmedia.databinding.RecyclerRowBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PostAdapter(
    private val onLikeClicked: (postId: String) -> Unit,
    private val onCommentClicked: (postId: String) -> Unit
) : ListAdapter<Post, PostAdapter.PostHolder>(PostDiffCallback()) {

    private val currentUserId: String? = FirebaseAuth.getInstance().currentUser?.uid

    inner class PostHolder(val binding: RecyclerRowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostHolder {
        val binding = RecyclerRowBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PostHolder(binding)
    }

    override fun onBindViewHolder(holder: PostHolder, position: Int) {
        val post = getItem(position)

        with(holder.binding) {
            tvPostUsername.text = post.userUsername
            tvPostNickname.text = "@${post.userNickname}"

            when(post.tag) {
                "Soru" -> tvPostTag.background = ContextCompat.getDrawable(holder.itemView.context, R.drawable.gradient_soru)
                "Ticaret" -> tvPostTag.background = ContextCompat.getDrawable(holder.itemView.context, R.drawable.gradient_ticaret)
                "Şikayet" -> tvPostTag.background = ContextCompat.getDrawable(holder.itemView.context, R.drawable.gradient_sikayet)
                "Sosyal" -> tvPostTag.background = ContextCompat.getDrawable(holder.itemView.context, R.drawable.gradient_sosyal)
                else -> tvPostTag.background = ContextCompat.getDrawable(holder.itemView.context, R.color.light_gray_blue)
            }
            tvPostTag.text = "#${post.tag}"
            tvPostDate.text = getTimeAgo(post.timestamp)

            val text = post.explanation.trim()
            if(text.isEmpty()){
                tvPostDescription.visibility = View.GONE
            }else{
                tvPostDescription.text = post.explanation
                tvPostDescription.visibility = View.VISIBLE
            }

            Glide.with(root.context)
                .load(post.userProfileImage.ifEmpty { R.drawable.default_pp })
                .transition(DrawableTransitionOptions.withCrossFade())
                .circleCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.default_pp)
                .error(R.drawable.default_pp)
                .into(imageViewProfile)

            if (post.imageUrl.isNotEmpty()) {
                imageViewPost.visibility = View.VISIBLE
                Glide.with(root.context)
                    .load(post.imageUrl)
                    .transition(DrawableTransitionOptions.withCrossFade(300))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(imageViewPost)
            } else {
                imageViewPost.visibility = View.GONE
            }

            val userProfileClickListener = View.OnClickListener {
                viewOtherUserProfile(holder.itemView.context, post.userId)
            }
            tvPostUsername.setOnClickListener(userProfileClickListener)
            tvPostNickname.setOnClickListener(userProfileClickListener)
            cardViewProfilePic.setOnClickListener(userProfileClickListener)

            textViewLikeCount.text = post.likeCount.toString()
            if (currentUserId != null && post.likedBy.contains(currentUserId)) {
                buttonLike.setImageResource(R.drawable.heart)
            } else {
                buttonLike.setImageResource(R.drawable.like)
            }
            buttonLike.setOnClickListener {
                if (currentUserId != null) {
                    onLikeClicked(post.postId)
                } else {
                    Toast.makeText(holder.itemView.context, "Beğenmek için lütfen giriş yapın!", Toast.LENGTH_SHORT).show()
                }
            }

            textViewCommentCount.text = post.commentCount.toString()

            buttonComment.setOnClickListener {
                onCommentClicked(post.postId)
            }
        }
    }

    private fun getTimeAgo(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        return when {
            seconds < 60 -> "$seconds saniye önce"
            minutes < 60 -> "$minutes dakika önce"
            hours < 24 -> "$hours saat önce"
            days < 7 -> "$days gün önce"
            else -> {
                val format = SimpleDateFormat("dd MMM yy", Locale("tr"))
                format.format(Date(timestamp))
            }
        }
    }

    private fun viewOtherUserProfile(context: Context, userId: String){
        val intent = Intent(context, ViewUserActivity::class.java)
        intent.putExtra("userid", userId)
        context.startActivity(intent)
    }

    class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem.postId == newItem.postId
        }

        override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem == newItem
        }
    }
}
