package com.example.android_project.jobseeker.data

import com.example.android_project.common.model.Job
import com.example.android_project.common.model.Application

interface JobSeekerRepository {
    suspend fun fetchRecommendedJobs(): List<Job>
    suspend fun fetchMyApplications(): List<Application>
    suspend fun submitJobApplication(jobId: String, name: String, email: String, phone: String): Result<String>
    suspend fun submitJobApplicationWithResume(jobId: String, name: String, email: String, phone: String, resumeUrl: String): Result<String>
    suspend fun withdrawApplication(applicationId: String): Result<Unit>
}
