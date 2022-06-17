package com.aufa.githubstoryapp.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.aufa.githubstoryapp.DetailActivity
import com.aufa.githubstoryapp.data.Stories
import com.aufa.githubstoryapp.databinding.GithubStoryRowBinding
import com.bumptech.glide.Glide

class ListStoryAdapter(private val optionsCompat: ActivityOptionsCompat
) :
    PagingDataAdapter<Stories, ListStoryAdapter.ListViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ListViewHolder {
        val binding =
            GithubStoryRowBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return ListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val data = getItem(position)
        if (data != null) {
            holder.bind(data, optionsCompat)
        }
    }

    class ListViewHolder(private var binding: GithubStoryRowBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: Stories, optionsCompat: ActivityOptionsCompat) {
            Glide.with(itemView.context)
                .load(data.photoUrl)
                .into(binding.tvPhotoStory)
            binding.tvName.text = data.name

            itemView.setOnClickListener {
                val intentToDetail = Intent(itemView.context, DetailActivity::class.java)
                intentToDetail.putExtra(NAME, data.name)
                intentToDetail.putExtra(PHOTO, data.photoUrl)
                intentToDetail.putExtra(DESC, data.description)

                itemView.context.startActivity(intentToDetail, optionsCompat.toBundle())
            }
        }
    }

    companion object {
        private const val NAME = "NAME"
        private const val PHOTO = "PHOTO"
        private const val DESC = "DESC"

        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Stories>() {
            override fun areItemsTheSame(oldItem: Stories, newItem: Stories): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: Stories, newItem: Stories): Boolean {
                return oldItem.id == newItem.id
            }
        }
    }
}