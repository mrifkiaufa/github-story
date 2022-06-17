package com.aufa.githubstoryapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.aufa.githubstoryapp.databinding.ActivityDetailBinding
import com.bumptech.glide.Glide

class DetailActivity : AppCompatActivity() {
    private lateinit var activityDetailBinding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityDetailBinding = ActivityDetailBinding.inflate(layoutInflater)
        supportActionBar?.hide()
        setContentView(activityDetailBinding.root)

        val name = intent.extras?.get("NAME").toString()
        val photoUrl = intent.extras?.get("PHOTO").toString()
        val desc = intent.extras?.get("DESC").toString()

        activityDetailBinding.backButton.setOnClickListener {
            finish()
        }

        activityDetailBinding.apply {
            tvNameDetail.text = name
            tvDescription.text = desc
        }

        Glide.with(this)
            .load(photoUrl)
            .into(activityDetailBinding.tvImageDetail)
    }
}