package com.example.android_project.company.data

import android.content.Context
import com.example.android_project.common.data.local.JobBoardDatabase
import com.example.android_project.common.data.local.toEntity
import com.example.android_project.common.data.local.toModel
import com.example.android_project.common.data.mock.MockDataProvider
import com.example.android_project.common.model.Application
import com.example.android_project.common.model.Job
import com.example.android_project.common.session.UserProfileStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class LocalCompanyRepository private constructor(context: Context) : CompanyRepository {

    private val jobDao = JobBoardDatabase.getInstance(context).jobDao()
    private val applicationDao = JobBoardDatabase.getInstance(context).applicationDao()
    private val profileStore = UserProfileStore(context)

    override suspend fun fetchRecentApplications(): List<Application> {
        val companyName = profileStore.getCompany().name.ifBlank { "Highspeed Studios" }
        val fromRoom = applicationDao.getByCompanyName(companyName).map { it.toModel() }
        return if (fromRoom.isNotEmpty()) fromRoom else MockDataProvider.applications()
    }

    override suspend fun fetchNewApplicants(): List<Application> {
        val companyName = profileStore.getCompany().name.ifBlank { "Highspeed Studios" }
        return applicationDao.getByCompanyName(companyName)
            .map { it.toModel() }
            .filter { it.status.equals("Applied", ignoreCase = true) || it.status.equals("New", ignoreCase = true) }
    }

    override suspend fun fetchApplicationsForJob(jobId: String): List<Application> {
        return applicationDao.getByJobId(jobId).map { it.toModel() }
    }

    override suspend fun updateApplicationStatus(applicationId: String, status: String): Result<Unit> {
        return try {
            applicationDao.updateStatus(applicationId, status)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun postJob(
        title: String,
        location: String,
        salary: String,
        type: String,
        description: String
    ): Result<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            val jobId = UUID.randomUUID().toString()
            val job = Job(
                id = jobId,
                title = title,
                companyName = "Highspeed Studios",
                location = location,
                salaryRange = salary,
                status = "Active",
                type = type,
                description = description
            )
            jobDao.upsert(job.toEntity())
            Result.success(jobId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCompanyJobs(): List<Job> = withContext(Dispatchers.IO) {
        val cached = jobDao.getAll().map { it.toModel() }
        if (cached.isNotEmpty()) return@withContext cached

        val seeded = MockDataProvider.jobsForModeration()
        seeded.forEach { jobDao.upsert(it.toEntity()) }
        return@withContext seeded
    }

    override suspend fun getJob(jobId: String): Job? = withContext(Dispatchers.IO) {
        jobDao.getById(jobId)?.toModel()
    }

    override suspend fun updateJob(job: Job): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            jobDao.upsert(job.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteJob(jobId: String): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            jobDao.delete(jobId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: LocalCompanyRepository? = null

        fun getInstance(context: Context): LocalCompanyRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: LocalCompanyRepository(context).also { INSTANCE = it }
            }
        }
    }
}
