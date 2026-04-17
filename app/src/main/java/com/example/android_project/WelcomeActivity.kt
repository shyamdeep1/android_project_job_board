package com.example.android_project

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.android_project.common.analytics.AppAnalyticsLogger
import com.example.android_project.databinding.ActivityWelcomeBinding

class WelcomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWelcomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        AppAnalyticsLogger.logScreen(this, "Welcome")

        binding.cardCompany.setOnClickListener {
            AppAnalyticsLogger.logFlowStep(this, "role_select", "company")
            startActivity(Intent(this, LoginActivity::class.java))
        }

        binding.cardJobSeekers.setOnClickListener {
            AppAnalyticsLogger.logFlowStep(this, "role_select", "job_seeker")
            startActivity(Intent(this, JobSeekerLoginActivity::class.java))
        }

    }
}
