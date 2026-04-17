package com.example.android_project.common.navigation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.example.android_project.R
import com.example.android_project.auth.presentation.OnboardingRoleActivity
import com.example.android_project.common.model.UserRole
import com.example.android_project.common.session.SessionManager

class RoleNavHostActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_role_nav_host)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navHostContainer) as NavHostFragment
        val navController = navHostFragment.navController

        val roleFromIntent = intent.getStringExtra(EXTRA_ROLE)
        val sessionManager = SessionManager(this)
        val role = roleFromIntent?.let { runCatching { UserRole.valueOf(it) }.getOrNull() }
            ?: sessionManager.getRole()

        if (role != null && role != UserRole.GUEST && !sessionManager.isOnboardingDone(role)) {
            startActivity(android.content.Intent(this, OnboardingRoleActivity::class.java).apply {
                putExtra(OnboardingRoleActivity.EXTRA_ROLE, role.name)
            })
            finish()
            return
        }

        val graphRes = when (role) {
            UserRole.JOB_SEEKER -> R.navigation.nav_jobseeker_role
            UserRole.COMPANY -> R.navigation.nav_company_role
            UserRole.ADMIN -> R.navigation.nav_admin_role
            UserRole.GUEST -> R.navigation.nav_jobseeker_role
            null -> R.navigation.nav_auth_role
        }
        navController.setGraph(graphRes, intent.extras)
    }

    companion object {
        const val EXTRA_ROLE = "extra_role"
    }
}
