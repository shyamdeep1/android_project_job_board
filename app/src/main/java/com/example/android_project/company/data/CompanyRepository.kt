package com.example.android_project.company.data

import com.example.android_project.common.model.Application
import com.example.android_project.common.model.Job

interface CompanyRepository {
    suspend fun fetchRecentApplications(): List<Application>
    suspend fun fetchNewApplicants(): List<Application>
    suspend fun fetchApplicationsForJob(jobId: String): List<Application>
    suspend fun updateApplicationStatus(applicationId: String, status: String): Result<Unit>
    suspend fun postJob(
        title: String,
        location: String,
        salary: String,
        type: String,
        description: String
    ): Result<String>

    suspend fun getCompanyJobs(): List<Job>

    suspend fun getJob(jobId: String): Job?

    suspend fun updateJob(job: Job): Result<Unit>

    suspend fun deleteJob(jobId: String): Result<Unit>
}
