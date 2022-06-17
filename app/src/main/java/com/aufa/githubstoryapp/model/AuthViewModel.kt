package com.aufa.githubstoryapp.model

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.aufa.githubstoryapp.api.ApiConfig
import com.aufa.githubstoryapp.data.EndpointResponse
import com.aufa.githubstoryapp.data.Login
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AuthViewModel : ViewModel() {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _status = MutableLiveData<Boolean>()
    val status: LiveData<Boolean> = _status

    private val _responseMessage = MutableLiveData<String>()
    val responseMessage: LiveData<String> = _responseMessage

    private val _userLogin = MutableLiveData<String>()
    val userLogin: LiveData<String> = _userLogin

    fun login(email: String, password: String) {
        _isLoading.value = true
        val client = ApiConfig.getApiService().login(email, password)
        client.enqueue(object : Callback<Login> {
            override fun onResponse(
                call: Call<Login>,
                response: Response<Login>
            ) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    _userLogin.value = responseBody?.loginResult?.token
                    if (responseBody != null) {
                        _status.value = true
                    }
                } else {
                    _status.value = false
                    val errorMessage = response.errorBody()?.string()?.let { JSONObject(it) }
                    Log.e(TAG, "onFailure: ${errorMessage?.getString("message")}")
                    _responseMessage.value = errorMessage?.getString("message")
                }
            }

            override fun onFailure(call: Call<Login>, t: Throwable) {
                _isLoading.value = false
                Log.e(TAG, "onFailure: ${t.message}")
                _responseMessage.value = t.message
                _status.value = false
            }
        })
    }

    fun register(name: String,email: String, password: String) {
        _isLoading.value = true
        val client = ApiConfig.getApiService().register(name, email, password)
        client.enqueue(object : Callback<EndpointResponse> {
            override fun onResponse(
                call: Call<EndpointResponse>,
                response: Response<EndpointResponse>
            ) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        _status.value = true
                    }
                } else {
                    _status.value = false
                    val errorMessage = response.errorBody()?.string()?.let { JSONObject(it) }
                    Log.e(TAG, "onFailure: ${errorMessage?.getString("message")}")
                    _responseMessage.value = errorMessage?.getString("message")
                }
            }

            override fun onFailure(call: Call<EndpointResponse>, t: Throwable) {
                _isLoading.value = false
                Log.e(TAG, "onFailure: ${t.message}")
                _responseMessage.value = t.message
                _status.value = false
            }
        })
    }

    companion object {
        private const val TAG = "AuthViewModel"
    }
}