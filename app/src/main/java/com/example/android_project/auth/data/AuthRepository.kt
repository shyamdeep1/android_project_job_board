package com.example.android_project.auth.data

import com.example.android_project.common.model.UserRole

interface AuthRepository {
    suspend fun signIn(username: String, password: String): Result<UserRole>
}
