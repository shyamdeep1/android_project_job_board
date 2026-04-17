package com.example.android_project.jobseeker.data

import android.content.Context
import com.example.android_project.common.data.local.JobBoardDatabase
import com.example.android_project.common.data.local.toEntity
import com.example.android_project.common.data.local.toModel
import com.example.android_project.common.data.mock.MockDataProvider
import com.example.android_project.common.firebase.FirestoreMappers
import com.example.android_project.common.firebase.awaitResult
import com.example.android_project.common.model.ApplicationStatus
import com.example.android_project.common.model.Application
import com.example.android_project.common.model.Job
import com.example.android_project.common.session.UserProfileStore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class FirebaseJobSeekerRepository(
    context: Context,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : JobSeekerRepository {

    private val jobDao = JobBoardDatabase.getInstance(context).jobDao()
    private val applicationDao = JobBoardDatabase.getInstance(context).applicationDao()
    private val auth = FirebaseAuth.getInstance()
    private val profileStore = UserProfileStore(context)
    
    override suspend fun fetchRecommendedJobs(): List<Job> = withContext(Dispatchers.IO) {
        runCatching {
            val snapshot = firestore.collection("jobs")
                .whereEqualTo("status", "Active")
                .orderBy("createdAt")
                .get()
                .awaitResult()
            val jobs = snapshot.documents.map(FirestoreMappers::mapJob)
            jobs.forEach { jobDao.upsert(it.toEntity()) }
            jobs
        }.getOrElse {
            jobDao.getVisibleForSeekers()
                .map { it.toModel() }
                .ifEmpty { MockDataProvider.jobsForModeration() }
        }
    }
    
    override suspend fun submitJobApplication(
        jobId: String,
        name: String,
        email: String,
        phone: String
    ): Result<String> {
        return try {
            val applicationId = UUID.randomUUID().toString()
            val createdAt = System.currentTimeMillis()
            val uid = auth.currentUser?.uid ?: profileStore.getJobSeeker().uid
            val appliedRole = resolveAppliedRole(jobId)
            val application = Application(
                id = applicationId,
                applicantName = name,
                appliedRole = appliedRole,
                status = ApplicationStatus.APPLIED.label(),
                timeLabel = "Just now",
                applicantUid = uid,
                applicantEmail = email,
                jobId = jobId,
                createdAt = createdAt
            )
            MockDataProvider.addApplication(application)
            applicationDao.upsert(application.toEntity())

            val applicationData = mapOf(
                "id" to applicationId,
                "jobId" to jobId,
                "applicantName" to name,
                "applicantEmail" to email,
                "applicantPhone" to phone,
                "applicantUid" to uid,
                "appliedRole" to appliedRole,
                "timeLabel" to "Just now",
                "status" to ApplicationStatus.APPLIED.label(),
                "createdAt" to createdAt
            )
            runCatching {
                firestore.collection("applications").document(applicationId).set(applicationData).awaitResult()
            }
            Result.success(applicationId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun submitJobApplicationWithResume(
        jobId: String,
        name: String,
        email: String,
        phone: String,
        resumeUrl: String
    ): Result<String> {
        return try {
            val applicationId = UUID.randomUUID().toString()
            val createdAt = System.currentTimeMillis()
            val uid = auth.currentUser?.uid ?: profileStore.getJobSeeker().uid
            val appliedRole = resolveAppliedRole(jobId)
            val application = Application(
                id = applicationId,
                applicantName = name,
                appliedRole = appliedRole,
                status = ApplicationStatus.APPLIED.label(),
                timeLabel = "Just now",
                applicantUid = uid,
                applicantEmail = email,
                applicantPhone = phone,
                jobId = jobId,
                createdAt = createdAt,
                resumeUrl = resumeUrl
            )
            MockDataProvider.addApplication(application)
            applicationDao.upsert(application.toEntity())

            val applicationData = mapOf(
                "id" to applicationId,
                "jobId" to jobId,
                "applicantName" to name,
                "applicantEmail" to email,
                "applicantPhone" to phone,
                "applicantUid" to uid,
                "appliedRole" to appliedRole,
                "timeLabel" to "Just now",
                "status" to ApplicationStatus.APPLIED.label(),
                "createdAt" to createdAt,
                "resumeUrl" to resumeUrl
            )
            runCatching {
                firestore.collection("applications").document(applicationId).set(applicationData).awaitResult()
            }
            Result.success(applicationId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun withdrawApplication(applicationId: String): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            applicationDao.updateStatus(applicationId, ApplicationStatus.WITHDRAWN.label())
            runCatching {
                firestore.collection("applications")
                    .document(applicationId)
                    .update("status", ApplicationStatus.WITHDRAWN.label())
                    .awaitResult()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun fetchMyApplications(): List<Application> = withContext(Dispatchers.IO) {
        val currentUid = auth.currentUser?.uid ?: profileStore.getJobSeeker().uid
        val currentEmail = auth.currentUser?.email ?: profileStore.getJobSeeker().email
        val safeEmail = currentEmail?.takeIf { it.isNotBlank() }

        val fromRoom = when {
            !currentUid.isNullOrBlank() -> applicationDao.getByApplicantUid(currentUid).map { it.toModel() }
            !safeEmail.isNullOrBlank() -> applicationDao.getByApplicantEmail(safeEmail).map { it.toModel() }
            else -> emptyList()
        }
        if (fromRoom.isNotEmpty()) return@withContext fromRoom

        runCatching {
            val byUid = if (!currentUid.isNullOrBlank()) {
                firestore.collection("applications")
                    .whereEqualTo("applicantUid", currentUid)
                    .get()
                    .awaitResult()
                    .documents
                    .map(FirestoreMappers::mapApplication)
            } else {
                emptyList()
            }

            val fromFirestore = if (byUid.isNotEmpty()) {
                byUid
            } else if (safeEmail != null) {
                firestore.collection("applications")
                    .whereEqualTo("applicantEmail", safeEmail)
                    .get()
                    .awaitResult()
                    .documents
                    .map(FirestoreMappers::mapApplication)
            } else {
                emptyList()
            }

            val sorted = fromFirestore.sortedByDescending { it.createdAt }
            sorted.forEach { applicationDao.upsert(it.toEntity()) }
            sorted
        }.getOrElse {
            val profile = profileStore.getJobSeeker()
            MockDataProvider.applications()
                .filter { application ->
                    when {
                        !application.applicantUid.isNullOrBlank() && !profile.uid.isNullOrBlank() -> {
                            application.applicantUid == profile.uid
                        }
                        !application.applicantEmail.isNullOrBlank() && profile.email.isNotBlank() -> {
                            application.applicantEmail.equals(profile.email, ignoreCase = true)
                        }
                        profile.name.isNotBlank() -> {
                            application.applicantName.equals(profile.name, ignoreCase = true)
                        }
                        else -> true
                    }
                }
                .sortedByDescending { it.createdAt }
        }
    }

    private suspend fun resolveAppliedRole(jobId: String): String {
        val localTitle = jobDao.getById(jobId)?.title
        if (!localTitle.isNullOrBlank()) return "Applied for $localTitle"

        return runCatching {
            val doc = firestore.collection("jobs").document(jobId).get().awaitResult()
            val title = doc.getString("title").orEmpty()
            if (title.isNotBlank()) "Applied for $title" else "Applied for $jobId"
        }.getOrDefault("Applied for $jobId")
    }
}
