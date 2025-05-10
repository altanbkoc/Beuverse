package com.altankoc.socialmedia.beuverse.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.altankoc.socialmedia.R
import com.altankoc.socialmedia.beuverse.model.Post
import com.altankoc.socialmedia.databinding.RecyclerRowBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions

class PostAdapter : ListAdapter<Post, PostAdapter.PostHolder>(PostDiffCallback()) {

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
            // Kullanıcı bilgileri
            tvPostUsername.text = post.userUsername
            tvPostNickname.text = "@${post.userNickname}"

            if(post.tag == "Soru"){
                tvPostTag.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.strong_blue))            }
            else{

            }
            tvPostTag.text = post.tag

            tvPostDescription.text = post.explanation

            // Profil resmi (Storage URL'si)
            Glide.with(root.context)
                .load(post.userProfileImage.ifEmpty { R.drawable.default_pp })
                .transition(DrawableTransitionOptions.withCrossFade())
                .circleCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.default_pp)
                .error(R.drawable.default_pp)
                .into(imageViewProfile)

            // Post resmi (Storage URL'si)
            if (post.imageUrl.isNotEmpty()) {
                imageViewPost.visibility = View.VISIBLE
                Glide.with(root.context)
                    .load(post.imageUrl)
                    .transition(DrawableTransitionOptions.withCrossFade(300))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .override(1000, 1000) // Optimize edilmiş boyut
                    .into(imageViewPost)
            } else {
                imageViewPost.visibility = View.GONE
            }
        }
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