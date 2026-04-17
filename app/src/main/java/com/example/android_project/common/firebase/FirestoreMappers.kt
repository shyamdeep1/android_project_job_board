package com.example.android_project.common.firebase

import com.example.android_project.common.model.AppUser
import com.example.android_project.common.model.Application
import com.example.android_project.common.model.Job
import com.example.android_project.common.model.UserRole
import com.google.firebase.firestore.DocumentSnapshot

object FirestoreMappers {

    fun mapUser(doc: DocumentSnapshot): AppUser {
        val roleName = doc.getString("role") ?: UserRole.JOB_SEEKER.name
        val role = runCatching { UserRole.valueOf(roleName) }.getOrDefault(UserRole.JOB_SEEKER)
        return AppUser(
            id = doc.id,
            name = doc.getString("name").orEmpty(),
            email = doc.getString("email").orEmpty(),
            role = role,
            isActive = doc.getBoolean("isActive") ?: true
        )
    }

    fun mapJob(doc: DocumentSnapshot): Job {
        return Job(
            id = doc.id,
            title = doc.getString("title").orEmpty(),
            companyName = doc.getString("companyName").orEmpty(),
            location = doc.getString("location").orEmpty(),
            salaryRange = doc.getString("salaryRange").orEmpty(),
            status = doc.getString("status").orEmpty(),
            type = doc.getString("type").orEmpty(),
            description = doc.getString("description").orEmpty(),
            createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
        )
    }

    fun mapApplication(doc: DocumentSnapshot): Application {
        val createdAt = doc.getLong("createdAt") ?: 0L
        val providedTimeLabel = doc.getString("timeLabel").orEmpty()
        val fallbackTimeLabel = if (createdAt > 0L) {
            val secondsAgo = ((System.currentTimeMillis() - createdAt).coerceAtLeast(0L) / 1000L)
            when {
                secondsAgo < 60L -> "Just now"
                secondsAgo < 3600L -> "${secondsAgo / 60L}m ago"
                secondsAgo < 86400L -> "${secondsAgo / 3600L}h ago"
                else -> "${secondsAgo / 86400L}d ago"
            }
        } else ""
        return Application(
            id = doc.id,
            applicantName = doc.getString("applicantName").orEmpty(),
            appliedRole = doc.getString("appliedRole").orEmpty(),
            status = doc.getString("status").orEmpty(),
            timeLabel = providedTimeLabel.ifBlank { fallbackTimeLabel },
            applicantUid = doc.getString("applicantUid"),
            applicantEmail = doc.getString("applicantEmail"),
            applicantPhone = doc.getString("applicantPhone"),
            jobId = doc.getString("jobId"),
            createdAt = createdAt,
            resumeUrl = doc.getString("resumeUrl")
        )
    }
}
