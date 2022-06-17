package com.aufa.githubstoryapp.repository

import androidx.lifecycle.LiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.aufa.githubstoryapp.api.ApiService
import com.aufa.githubstoryapp.data.Stories
import com.aufa.githubstoryapp.data.StoriesResponse
import com.aufa.githubstoryapp.data.StoryPagingSource

class StoryRepository(private val apiService: ApiService) {
    fun getDataStories(token: String): LiveData<PagingData<Stories>> {
        return Pager(
            config = PagingConfig(
                pageSize = 5
            ),
            pagingSourceFactory = {
                StoryPagingSource(apiService, token, 0)
            }
        ).liveData
    }

    suspend fun getDataMapStories(token: String): StoriesResponse =
        apiService.getAllStories(token, 1, 30, 1)
}