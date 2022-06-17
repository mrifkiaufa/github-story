package com.aufa.githubstoryapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.aufa.githubstoryapp.preference.SessionManager

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        supportActionBar?.hide()

        val milliSecond = 2000

        val pref = SessionManager(this)

        Handler(Looper.getMainLooper()).postDelayed({
            if(pref.fetchAuthToken()!!.isEmpty()){
                val intentToLogin = Intent(this@SplashScreenActivity, LoginActivity::class.java)
                startActivity(intentToLogin)
            }
            else{
                val intent = Intent(this@SplashScreenActivity, StoryListActivity::class.java)
                startActivity(intent)
            }
            finish()
        }, milliSecond.toLong())
    }
}