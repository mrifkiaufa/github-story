package com.aufa.githubstoryapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.aufa.githubstoryapp.databinding.ActivityLoginBinding
import com.aufa.githubstoryapp.model.AuthViewModel
import com.aufa.githubstoryapp.preference.SessionManager

class LoginActivity : AppCompatActivity() {
    private val authViewModel: AuthViewModel by viewModels()
    private lateinit var activityLoginBinding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityLoginBinding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(activityLoginBinding.root)

        supportActionBar?.hide()

        authViewModel.isLoading.observe(this) {
            showLoading(it)
        }

        activityLoginBinding.buttonToRegister.setOnClickListener {
            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }

        activityLoginBinding.loginButton.setOnClickListener {
            val email = activityLoginBinding.emailLogin.text.toString()
            val password = activityLoginBinding.passwordLogin.text.toString()

            authViewModel.login(email, password)

            authViewModel.userLogin.observe(this) { token ->
                val pref = SessionManager(this)
                pref.saveAuthToken(token)
            }

            authViewModel.status.observe(this) { status ->
                if (status) {
                    Toast.makeText(this@LoginActivity, R.string.login_success, Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@LoginActivity, StoryListActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    authViewModel.responseMessage.observe(this) { message ->
                        Toast.makeText(this@LoginActivity, message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        activityLoginBinding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}