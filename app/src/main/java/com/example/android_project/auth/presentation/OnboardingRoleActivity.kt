package com.example.android_project.auth.presentation

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.android_project.common.model.UserRole
import com.example.android_project.common.navigation.RoleNavHostActivity
import com.example.android_project.common.session.SessionManager
import com.example.android_project.databinding.ActivityOnboardingRoleBinding

class OnboardingRoleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingRoleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingRoleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val roleName = intent.getStringExtra(EXTRA_ROLE) ?: UserRole.JOB_SEEKER.name
        val role = runCatching { UserRole.valueOf(roleName) }.getOrDefault(UserRole.JOB_SEEKER)

        val (title, description) = when (role) {
            UserRole.JOB_SEEKER, UserRole.GUEST -> "Job Seeker Onboarding" to "Browse jobs, save favorites, and track your application progress."
            UserRole.COMPANY -> "Company Onboarding" to "Post jobs, review applicants, and manage hiring steps from your dashboard."
            UserRole.ADMIN -> "Admin Onboarding" to "Moderate content, verify companies, and broadcast platform-wide notices."
        }
        binding.tvTitle.text = title
        binding.tvDescription.text = description

        binding.btnContinue.setOnClickListener {
            SessionManager(this).setOnboardingDone(role)
            startActivity(Intent(this, RoleNavHostActivity::class.java).apply {
                putExtra(RoleNavHostActivity.EXTRA_ROLE, role.name)
            })
            finish()
        }
    }

    companion object {
        const val EXTRA_ROLE = "extra_role"
    }
}
