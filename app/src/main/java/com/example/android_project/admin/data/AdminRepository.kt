package com.example.android_project.admin.data

import com.example.android_project.common.model.AppUser
import com.example.android_project.common.model.Job

interface AdminRepository {
    suspend fun fetchUsers(): List<AppUser>
    suspend fun fetchJobsForModeration(): List<Job>
}
