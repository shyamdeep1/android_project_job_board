package com.example.android_project.common.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.android_project.common.model.Job

@Entity(tableName = "jobs")
data class JobEntity(
    @PrimaryKey val id: String,
    val title: String,
    val companyName: String,
    val location: String,
    val salaryRange: String,
    val status: String,
    val type: String,
    val description: String,
    val createdAt: Long
)

fun JobEntity.toModel(): Job = Job(
    id = id,
    title = title,
    companyName = companyName,
    location = location,
    salaryRange = salaryRange,
    status = status,
    type = type,
    description = description,
    createdAt = createdAt
)

fun Job.toEntity(): JobEntity = JobEntity(
    id = id,
    title = title,
    companyName = companyName,
    location = location,
    salaryRange = salaryRange,
    status = status,
    type = type,
    description = description,
    createdAt = createdAt
)
