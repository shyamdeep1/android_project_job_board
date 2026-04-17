package com.example.android_project.auth.data

import com.example.android_project.common.model.UserRole

class FirebaseAuthRepository : AuthRepository {
    override suspend fun signIn(username: String, password: String): Result<UserRole> {
        return try {
            if (username.trim().isEmpty() || password.trim().isEmpty()) {
                return Result.failure(IllegalArgumentException("Username and password cannot be empty"))
            }
            
            val role = when {
                username.contains("admin", ignoreCase = true) -> UserRole.ADMIN
                username.contains("company", ignoreCase = true) -> UserRole.COMPANY
                else -> UserRole.JOB_SEEKER
            }
            
            if (password.length < 4) {
                return Result.failure(IllegalArgumentException("Invalid credentials"))
            }
            
            Result.success(role)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
