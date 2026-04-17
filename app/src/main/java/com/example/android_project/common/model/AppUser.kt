package com.example.android_project.common.model

data class AppUser(
    val id: String,
    val name: String,
    val email: String,
    val role: UserRole,
    val isActive: Boolean = true
)
