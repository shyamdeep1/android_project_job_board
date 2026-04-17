package com.example.android_project

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.android_project.common.analytics.AppAnalyticsLogger
import com.example.android_project.common.firebase.FirebaseSeedDataInitializer
import com.example.android_project.common.navigation.RoleNavHostActivity
import com.example.android_project.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        AppAnalyticsLogger.logScreen(this, "Splash")
        FirebaseSeedDataInitializer.seedIfNeeded(this)

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, RoleNavHostActivity::class.java))
            finish()
        }, 2500)
    }
}
