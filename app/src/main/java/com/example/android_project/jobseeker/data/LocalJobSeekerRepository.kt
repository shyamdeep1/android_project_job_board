package com.example.android_project.jobseeker.data

import android.content.Context
import com.example.android_project.common.data.local.JobBoardDatabase
import com.example.android_project.common.data.local.toEntity
import com.example.android_project.common.data.local.toModel
import com.example.android_project.common.data.mock.MockDataProvider
import com.example.android_project.common.model.Application
import com.example.android_project.common.model.ApplicationStatus
import com.example.android_project.common.model.Job
import com.example.android_project.common.session.UserProfileStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class LocalJobSeekerRepository private constructor(context: Context) : JobSeekerRepository {

    private val jobDao = JobBoardDatabase.getInstance(context).jobDao()
    private val applicationDao = JobBoardDatabase.getInstance(context).applicationDao()
    private val profileStore = UserProfileStore(context)

    override suspend fun fetchRecommendedJobs(): List<Job> = withContext(Dispatchers.IO) {
        val stored = jobDao.getVisibleForSeekers().map { it.toModel() }
        if (stored.isNotEmpty()) return@withContext stored

        val seeded = MockDataProvider.jobsForModeration()
        seeded.forEach { jobDao.upsert(it.toEntity()) }
        return@withContext seeded
    }

    override suspend fun submitJobApplication(jobId: String, name: String, email: String, phone: String): Result<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            val profile = profileStore.getJobSeeker()
            val applicationId = UUID.randomUUID().toString()
            val createdAt = System.currentTimeMillis()
            val title = jobDao.getById(jobId)?.title ?: jobId
            val application = Application(
                id = applicationId,
                applicantName = name,
                appliedRole = "Applied for $title",
                status = ApplicationStatus.APPLIED.label(),
                timeLabel = "Just now",
                applicantUid = profile.uid,
                applicantEmail = email,
                jobId = jobId,
                createdAt = createdAt
            )
            applicationDao.upsert(application.toEntity())
            Result.success(applicationId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun submitJobApplicationWithResume(jobId: String, name: String, email: String, phone: String, resumeUrl: String): Result<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            val profile = profileStore.getJobSeeker()
            val applicationId = UUID.randomUUID().toString()
            val createdAt = System.currentTimeMillis()
            val title = jobDao.getById(jobId)?.title ?: jobId
            val application = Application(
                id = applicationId,
                applicantName = name,
                appliedRole = "Applied for $title",
                status = ApplicationStatus.APPLIED.label(),
                timeLabel = "Just now",
                applicantUid = profile.uid,
                applicantEmail = email,
                applicantPhone = phone,
                jobId = jobId,
                createdAt = createdAt,
                resumeUrl = resumeUrl
            )
            applicationDao.upsert(application.toEntity())
            Result.success(applicationId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun fetchMyApplications(): List<Application> = withContext(Dispatchers.IO) {
        val profile = profileStore.getJobSeeker()
        val fromRoom = when {
            !profile.uid.isNullOrBlank() -> applicationDao.getByApplicantUid(profile.uid).map { it.toModel() }
            profile.email.isNotBlank() -> applicationDao.getByApplicantEmail(profile.email).map { it.toModel() }
            else -> emptyList()
        }
        if (fromRoom.isNotEmpty()) return@withContext fromRoom

        MockDataProvider.applications().filter { application ->
            profile.name.isBlank() || application.applicantName.equals(profile.name, ignoreCase = true)
        }
    }

    override suspend fun withdrawApplication(applicationId: String): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            applicationDao.updateStatus(applicationId, ApplicationStatus.WITHDRAWN.label())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: LocalJobSeekerRepository? = null

        fun getInstance(context: Context): LocalJobSeekerRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: LocalJobSeekerRepository(context).also { INSTANCE = it }
            }
        }
    }
}
