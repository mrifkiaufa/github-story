package com.aufa.githubstoryapp.data

import com.google.gson.annotations.SerializedName

data class EndpointResponse(
    @field:SerializedName("error")
    val error: Boolean,

    @field:SerializedName("message")
    val message: String
)
