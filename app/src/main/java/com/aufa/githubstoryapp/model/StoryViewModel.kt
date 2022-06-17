package com.aufa.githubstoryapp.model

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.aufa.githubstoryapp.api.ApiConfig
import com.aufa.githubstoryapp.data.EndpointResponse
import com.aufa.githubstoryapp.data.Stories
import com.aufa.githubstoryapp.repository.StoryRepository
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class StoryViewModel(token: String, private val storyRepository: StoryRepository): ViewModel() {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _responseMessage = MutableLiveData<String>()
    val responseMessage: LiveData<String> = _responseMessage

    private val _uploadStatus = MutableLiveData<Boolean>()
    val uploadStatus: LiveData<Boolean> = _uploadStatus

    private val bearer: String = "Bearer $token"

    private val _storyDataMap = MutableLiveData<List<Stories>>()
    val storiesDataMap: LiveData<List<Stories>> = _storyDataMap

    init {
        getStoriesData()
        getStoriesMapData()
    }

    fun getStoriesData(): LiveData<PagingData<Stories>> =
        storyRepository.getDataStories(bearer).cachedIn(viewModelScope)

    private fun getStoriesMapData() {
        viewModelScope.launch {
            _storyDataMap.postValue(storyRepository.getDataMapStories(bearer).listStories)
        }
    }

    fun uploadImage(imageMultipart: MultipartBody.Part, description: RequestBody) {
        val service = ApiConfig.getApiService().uploadImage(bearer, imageMultipart, description)
        service.enqueue(object : Callback<EndpointResponse> {
            override fun onResponse(
                call: Call<EndpointResponse>,
                response: Response<EndpointResponse>
            ) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        _uploadStatus.value = true
                        _responseMessage.value = responseBody.message
                    }
                } else {
                    Log.e(TAG, "onFailure: ${response.message()}")
                    _uploadStatus.value = false
                    _responseMessage.value = response.message()
                }
            }
            override fun onFailure(call: Call<EndpointResponse>, t: Throwable) {
                Log.e(TAG, "onFailure: ${t.message}")
                _uploadStatus.value = false
                _responseMessage.value = t.message
            }
        })
    }

    fun uploadImageWithLocation(imageMultipart: MultipartBody.Part, description: RequestBody, lat: RequestBody, lon: RequestBody) {
        val service = ApiConfig.getApiService().uploadImageWithLocation(bearer, imageMultipart, description, lat, lon)
        service.enqueue(object : Callback<EndpointResponse> {
            override fun onResponse(
                call: Call<EndpointResponse>,
                response: Response<EndpointResponse>
            ) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        _uploadStatus.value = true
                        _responseMessage.value = responseBody.message
                    }
                } else {
                    Log.e(TAG, "onFailure: ${response.message()}")
                    _uploadStatus.value = false
                    _responseMessage.value = response.message()
                }
            }
            override fun onFailure(call: Call<EndpointResponse>, t: Throwable) {
                Log.e(TAG, "onFailure: ${t.message}")
                _uploadStatus.value = false
                _responseMessage.value = t.message
            }
        })
    }

    companion object {
        private const val TAG = "StoryViewModel"
    }
}