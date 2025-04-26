package com.altankoc.socialmedia.beuverse.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.altankoc.socialmedia.beuverse.model.Post
import com.altankoc.socialmedia.databinding.RecyclerRowBinding

class PostAdapter(val postList: List<Post>): RecyclerView.Adapter<PostAdapter.PostHolder>() {

    inner class PostHolder(val binding: RecyclerRowBinding): RecyclerView.ViewHolder(binding.root)


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PostHolder {
        val binding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)

        return PostHolder(binding)
    }



    override fun getItemCount(): Int {
        return postList.size
    }


    override fun onBindViewHolder(
        holder: PostHolder,
        position: Int
    ) {
        TODO("Not yet implemented")
    }




}