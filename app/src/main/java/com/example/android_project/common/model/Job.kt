package com.example.android_project.common.model

import java.io.Serializable

data class Job(
    val id: String,
    val title: String,
    val companyName: String,
    val location: String,
    val salaryRange: String,
    val status: String,
    val type: String = "",
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis()
) : Serializable
