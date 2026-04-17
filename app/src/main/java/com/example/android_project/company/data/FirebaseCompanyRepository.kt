package com.example.android_project.company.data

import android.content.Context
import com.example.android_project.common.data.local.JobBoardDatabase
import com.example.android_project.common.data.local.toEntity
import com.example.android_project.common.data.local.toModel
import com.example.android_project.common.data.mock.MockDataProvider
import com.example.android_project.common.firebase.FirestoreMappers
import com.example.android_project.common.firebase.awaitResult
import com.example.android_project.common.model.Application
import com.example.android_project.common.model.Job
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class FirebaseCompanyRepository(
    context: Context,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val companyName: String = "Highspeed Studios"
) : CompanyRepository {

    private val jobDao = JobBoardDatabase.getInstance(context).jobDao()
    private val applicationDao = JobBoardDatabase.getInstance(context).applicationDao()
    
    override suspend fun fetchRecentApplications(): List<Application> {
        return withContext(Dispatchers.IO) {
            val fromRoom = applicationDao.getByCompanyName(companyName)
                .map { it.toModel() }
            if (fromRoom.isNotEmpty()) return@withContext fromRoom

            syncCompanyApplicationsFromRemote().ifEmpty {
                MockDataProvider.applications().sortedByDescending { it.createdAt }
            }
        }
    }

    override suspend fun fetchNewApplicants(): List<Application> = withContext(Dispatchers.IO) {
        val fromRoom = applicationDao.getByCompanyName(companyName)
            .map { it.toModel() }
            .filter { it.status.equals("Applied", ignoreCase = true) || it.status.equals("New", ignoreCase = true) }
        if (fromRoom.isNotEmpty()) return@withContext fromRoom

        syncCompanyApplicationsFromRemote().filter { it.status.equals("Applied", ignoreCase = true) || it.status.equals("New", ignoreCase = true) }
    }

    override suspend fun fetchApplicationsForJob(jobId: String): List<Application> = withContext(Dispatchers.IO) {
        val fromRoom = applicationDao.getByJobId(jobId).map { it.toModel() }
        if (fromRoom.isNotEmpty()) return@withContext fromRoom

        syncCompanyApplicationsFromRemote().filter { it.jobId == jobId }
    }

    override suspend fun updateApplicationStatus(applicationId: String, status: String): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            val existing = applicationDao.getById(applicationId)
            if (existing != null) {
                applicationDao.upsert(existing.copy(status = status))
            }

            runCatching {
                firestore.collection("applications")
                    .document(applicationId)
                    .update("status", status)
                    .awaitResult()
            }
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
    ): Result<String> {
        return try {
            val jobId = UUID.randomUUID().toString()
            val jobData = Job(
                id = jobId,
                title = title,
                companyName = companyName,
                location = location,
                salaryRange = salary,
                status = "Active",
                type = type,
                description = description,
                createdAt = System.currentTimeMillis()
            )
            MockDataProvider.addJob(jobData)
            jobDao.upsert(jobData.toEntity())

            val jobMap = mapOf(
                "id" to jobId,
                "title" to title,
                "companyName" to companyName,
                "location" to location,
                "salaryRange" to salary,
                "type" to type,
                "description" to description,
                "status" to "Active",
                "createdAt" to jobData.createdAt
            )
            runCatching {
                firestore.collection("jobs").document(jobId).set(jobMap).awaitResult()
            }
            Result.success(jobId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCompanyJobs(): List<Job> {
        return runCatching {
            val snapshot = firestore.collection("jobs").get().awaitResult()
            val jobs = snapshot.documents.map(FirestoreMappers::mapJob)
            jobs.forEach { jobDao.upsert(it.toEntity()) }
            jobs
        }.getOrElse {
            val cached = jobDao.getAll().map { it.toModel() }
            if (cached.isNotEmpty()) cached else MockDataProvider.jobsForModeration()
        }
    }

    override suspend fun getJob(jobId: String): Job? {
        return runCatching {
            val doc = firestore.collection("jobs").document(jobId).get().awaitResult()
            if (doc.exists()) FirestoreMappers.mapJob(doc) else null
        }.getOrElse {
            MockDataProvider.jobsForModeration().firstOrNull { it.id == jobId }
        }
    }

    override suspend fun updateJob(job: Job): Result<Unit> {
        return try {
            val data = mapOf(
                "title" to job.title,
                "companyName" to job.companyName,
                "location" to job.location,
                "salaryRange" to job.salaryRange,
                "type" to job.type,
                "description" to job.description,
                "status" to job.status,
                "createdAt" to job.createdAt
            )
            runCatching {
                firestore.collection("jobs").document(job.id).set(data).awaitResult()
            }
            MockDataProvider.addJob(job)
            jobDao.upsert(job.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteJob(jobId: String): Result<Unit> {
        return try {
            runCatching {
                firestore.collection("jobs").document(jobId).delete().awaitResult()
            }
            jobDao.delete(jobId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun syncCompanyApplicationsFromRemote(): List<Application> {
        return runCatching {
            val jobs = firestore.collection("jobs")
                .whereEqualTo("companyName", companyName)
                .get()
                .awaitResult()
                .documents
                .map(FirestoreMappers::mapJob)
            jobs.forEach { jobDao.upsert(it.toEntity()) }
            val jobIds = jobs.map { it.id }.toSet()

            val applications = firestore.collection("applications")
                .get()
                .awaitResult()
                .documents
                .map(FirestoreMappers::mapApplication)
                .filter { app -> !app.jobId.isNullOrBlank() && jobIds.contains(app.jobId) }
                .sortedByDescending { it.createdAt }

            val enrichedApplications = applications.map { app ->
                enrichApplicationWithApplicantInfo(app)
            }

            enrichedApplications.forEach { applicationDao.upsert(it.toEntity()) }
            enrichedApplications
        }.getOrDefault(emptyList())
    }

    private suspend fun enrichApplicationWithApplicantInfo(application: Application): Application {
        val uid = application.applicantUid
        if (uid.isNullOrBlank()) return application

        return runCatching {
            val docSnapshot = firestore.collection("job_seekers")
                .document(uid)
                .get()
                .awaitResult()

            if (docSnapshot.exists()) {
                val name = docSnapshot.getString("name") ?: application.applicantName
                val email = docSnapshot.getString("email") ?: application.applicantEmail
                application.copy(
                    applicantName = name.ifBlank { application.applicantName },
                    applicantEmail = email?.ifBlank { application.applicantEmail }
                        ?: application.applicantEmail
                )
            } else {
                application
            }
        }.getOrDefault(application)
    }
}
