package com.aufa.githubstoryapp.data

import com.google.gson.annotations.SerializedName

data class StoriesResponse(
    @field:SerializedName("listStory")
    val listStories: List<Stories>
)

data class Stories(
    @field:SerializedName("name")
    val name: String,

    @field:SerializedName("photoUrl")
    val photoUrl: String,

    @field:SerializedName("description")
    val description: String,

    @field:SerializedName("id")
    val id: String,

    @field:SerializedName("lat")
    val lat: Float,

    @field:SerializedName("lon")
    val lon: Float
)
