package com.aufa.githubstoryapp.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.aufa.githubstoryapp.api.ApiService

class StoryPagingSource(private val apiService: ApiService, private val token: String, private val location: Int) : PagingSource<Int, Stories>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Stories> {
        return try {
            val position = params.key ?: INITIAL_PAGE_INDEX
            val responseData = apiService.getAllStories(token, position, params.loadSize, location)

            LoadResult.Page(
                data = responseData.listStories,
                prevKey = if (position == INITIAL_PAGE_INDEX) null else position - 1,
                nextKey = if (responseData.listStories.isNullOrEmpty()) null else position + 1
            )
        } catch (exception: Exception) {
            return LoadResult.Error(exception)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Stories>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    private companion object {
        const val INITIAL_PAGE_INDEX = 1
    }
}