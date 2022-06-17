package com.aufa.githubstoryapp

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.paging.PagingData
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aufa.githubstoryapp.adapter.ListStoryAdapter
import com.aufa.githubstoryapp.adapter.LoadingStateAdapter
import com.aufa.githubstoryapp.data.Stories
import com.aufa.githubstoryapp.databinding.ActivityStoryListBinding
import com.aufa.githubstoryapp.model.StoryViewModel
import com.aufa.githubstoryapp.model.StoryViewModelFactory
import com.aufa.githubstoryapp.preference.SessionManager

class StoryListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStoryListBinding
    private lateinit var userStories: RecyclerView

    private lateinit var pref: SessionManager
    private lateinit var adapter: ListStoryAdapter

    private lateinit var storyViewModel: StoryViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStoryListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        pref = SessionManager(this)

        val storyViewModelFactory = StoryViewModelFactory(pref.fetchAuthToken().toString())
        storyViewModel = ViewModelProvider(this, storyViewModelFactory)[StoryViewModel::class.java]

        storyViewModel.getStoriesData().observe(this) { storyData ->
            setStoryData(storyData)
        }

        storyViewModel.isLoading.observe(this) {
            showLoading(it)
        }

        storyViewModel.responseMessage.observe(this) { message ->
            Toast.makeText(this@StoryListActivity, message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRestart() {
        super.onRestart()

        storyViewModel.getStoriesData().observe(this) { storyData ->
            setStoryData(storyData)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.option_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.add_menu -> {
                val i = Intent(this, AddStoryActivity::class.java)
                startActivity(
                    i,
                    ActivityOptionsCompat.makeSceneTransitionAnimation(this as Activity).toBundle()
                )
                return true
            }
            R.id.map_menu -> {
                val i = Intent(this, MapActivity::class.java)
                startActivity(i)
                return true
            }
            R.id.logout_menu -> {
                pref = SessionManager(this)
                pref.clearToken()
                val i = Intent(this, LoginActivity::class.java)
                startActivity(i)
                Toast.makeText(this@StoryListActivity, R.string.logout_success, Toast.LENGTH_SHORT)
                    .show()
                finish()
                return true
            }
            else -> return true
        }
    }

    private fun setStoryData(storiesData: PagingData<Stories>) {
        userStories = binding.userStories
        userStories.setHasFixedSize(true)

        if (applicationContext.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            userStories.layoutManager = GridLayoutManager(this, 2)
        } else {
            userStories.layoutManager = LinearLayoutManager(this)
        }

        val optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(
            this, binding.userStories, "detail"
        )

        adapter = ListStoryAdapter(optionsCompat)

        adapter.submitData(lifecycle, storiesData)

        userStories.adapter = adapter.withLoadStateFooter(
            footer = LoadingStateAdapter {
                adapter.retry()
            }
        )
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}