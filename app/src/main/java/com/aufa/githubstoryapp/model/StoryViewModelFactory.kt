package com.aufa.githubstoryapp.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.aufa.githubstoryapp.di.Injection

@Suppress("UNCHECKED_CAST")
class StoryViewModelFactory(private var token: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StoryViewModel::class.java)) {
            return StoryViewModel(token, Injection.provideRepository()) as T
        }

        throw IllegalArgumentException("Class StoryViewModel Not Found!")
    }
}