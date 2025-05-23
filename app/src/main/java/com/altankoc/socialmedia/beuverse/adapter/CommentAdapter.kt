package com.altankoc.socialmedia.beuverse.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.altankoc.socialmedia.R
import com.altankoc.socialmedia.beuverse.model.Comment
import com.altankoc.socialmedia.databinding.ItemCommentBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class CommentAdapter : ListAdapter<Comment, CommentAdapter.CommentViewHolder>(CommentDiffCallback()) {

    inner class CommentViewHolder(private val binding: ItemCommentBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(comment: Comment) {
            binding.textViewCommenterUsername.text = comment.userUsername
            binding.textViewCommentText.text = comment.text

            Glide.with(binding.imageViewCommenterProfile.context)
                .load(comment.userProfileImage.ifEmpty { R.drawable.default_pp })
                .circleCrop()
                .placeholder(R.drawable.default_pp)
                .error(R.drawable.default_pp)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(binding.imageViewCommenterProfile)

            if (comment.timestamp != null) {
                binding.textViewCommentTimestamp.text = formatTimestamp(comment.timestamp)
            } else {
                binding.textViewCommentTimestamp.text = "Şimdi"
            }
        }

        private fun formatTimestamp(date: Date): String {
            val now = System.currentTimeMillis()
            val diff = now - date.time

            val seconds = TimeUnit.MILLISECONDS.toSeconds(diff)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
            val hours = TimeUnit.MILLISECONDS.toHours(diff)
            val days = TimeUnit.MILLISECONDS.toDays(diff)

            return when {
                seconds < 60 -> "$seconds saniye önce"
                minutes < 60 -> "$minutes dakika önce"
                hours < 24 -> "$hours saat önce"
                days < 7 -> "$days gün önce"
                else -> {
                    val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("tr"))
                    sdf.format(date)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = ItemCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CommentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class CommentDiffCallback : DiffUtil.ItemCallback<Comment>() {
        override fun areItemsTheSame(oldItem: Comment, newItem: Comment): Boolean {
            return oldItem.commentId == newItem.commentId
        }

        override fun areContentsTheSame(oldItem: Comment, newItem: Comment): Boolean {
            return oldItem == newItem
        }
    }
}
