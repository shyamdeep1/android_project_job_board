package com.example.android_project.common.session

import android.content.Context
import com.example.android_project.common.model.UserRole

class SessionManager(context: Context) {

    private val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun saveSession(role: UserRole) {
        prefs.edit()
            .putBoolean(KEY_LOGGED_IN, true)
            .putString(KEY_ROLE, role.name)
            .apply()
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_LOGGED_IN, false)

    fun getRole(): UserRole? {
        val role = prefs.getString(KEY_ROLE, null) ?: return null
        return runCatching { UserRole.valueOf(role) }.getOrNull()
    }

    fun isOnboardingDone(role: UserRole): Boolean {
        return prefs.getBoolean("${KEY_ONBOARDING_DONE}_${role.name}", false)
    }

    fun setOnboardingDone(role: UserRole) {
        prefs.edit().putBoolean("${KEY_ONBOARDING_DONE}_${role.name}", true).apply()
    }

    companion object {
        private const val PREF_NAME = "job_portal_session"
        private const val KEY_LOGGED_IN = "is_logged_in"
        private const val KEY_ROLE = "user_role"
        private const val KEY_ONBOARDING_DONE = "onboarding_done"
    }
}
