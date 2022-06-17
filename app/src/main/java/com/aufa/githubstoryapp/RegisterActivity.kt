package com.aufa.githubstoryapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.aufa.githubstoryapp.databinding.ActivityRegisterBinding
import com.aufa.githubstoryapp.model.AuthViewModel

class RegisterActivity : AppCompatActivity() {
    private val authViewModel: AuthViewModel by viewModels()
    private lateinit var activityRegisterBinding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityRegisterBinding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(activityRegisterBinding.root)

        supportActionBar?.hide()

        authViewModel.isLoading.observe(this) {
            showLoading(it)
        }

        activityRegisterBinding.buttonToLogin.setOnClickListener {
            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        activityRegisterBinding.registerButton.setOnClickListener {
            val name = activityRegisterBinding.nameRegister.text.toString()
            val email = activityRegisterBinding.emailRegister.text.toString()
            val password = activityRegisterBinding.passwordRegister.text.toString()

            authViewModel.register(name,email, password)
            authViewModel.status.observe(this) { status ->
                if (status) {
                    Toast.makeText(this@RegisterActivity, R.string.register_success, Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    authViewModel.responseMessage.observe(this) { message ->
                        Toast.makeText(this@RegisterActivity, message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        activityRegisterBinding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}