package com.example.android_project.common.model

import java.io.Serializable

data class Application(
    val id: String,
    val applicantName: String,
    val appliedRole: String,
    val status: String,
    val timeLabel: String,
    val applicantUid: String? = null,
    val applicantEmail: String? = null,
    val applicantPhone: String? = null,
    val jobId: String? = null,
    val createdAt: Long = 0L,
    val resumeUrl: String? = null
) : Serializable
