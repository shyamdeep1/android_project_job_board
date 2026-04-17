package com.example.android_project.common.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.android_project.common.model.Application

@Entity(
    tableName = "applications",
    indices = [Index("jobId"), Index("applicantUid"), Index("applicantEmail")]
)
data class ApplicationEntity(
    @PrimaryKey val id: String,
    val applicantName: String,
    val appliedRole: String,
    val status: String,
    val timeLabel: String,
    val applicantUid: String?,
    val applicantEmail: String?,
    val jobId: String?,
    val createdAt: Long,
    val resumeUrl: String? = null
)

fun ApplicationEntity.toModel(): Application = Application(
    id = id,
    applicantName = applicantName,
    appliedRole = appliedRole,
    status = status,
    timeLabel = timeLabel,
    applicantUid = applicantUid,
    applicantEmail = applicantEmail,
    jobId = jobId,
    createdAt = createdAt,
    resumeUrl = resumeUrl
)

fun Application.toEntity(): ApplicationEntity = ApplicationEntity(
    id = id,
    applicantName = applicantName,
    appliedRole = appliedRole,
    status = status,
    timeLabel = timeLabel,
    applicantUid = applicantUid,
    applicantEmail = applicantEmail,
    jobId = jobId,
    createdAt = createdAt,
    resumeUrl = resumeUrl
)
