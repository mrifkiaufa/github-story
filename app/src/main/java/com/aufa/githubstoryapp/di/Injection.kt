package com.aufa.githubstoryapp.di

import com.aufa.githubstoryapp.api.ApiConfig
import com.aufa.githubstoryapp.repository.StoryRepository

object Injection {
    fun provideRepository(): StoryRepository {
        val apiService = ApiConfig.getApiService()
        return StoryRepository(apiService)
    }
}