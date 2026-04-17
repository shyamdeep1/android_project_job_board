package com.example.android_project.common.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ApplicationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(application: ApplicationEntity)

    @Query("SELECT * FROM applications WHERE applicantUid = :uid ORDER BY createdAt DESC")
    suspend fun getByApplicantUid(uid: String): List<ApplicationEntity>

    @Query("SELECT * FROM applications WHERE applicantEmail = :email ORDER BY createdAt DESC")
    suspend fun getByApplicantEmail(email: String): List<ApplicationEntity>

    @Query("SELECT * FROM applications WHERE jobId IN (SELECT id FROM jobs WHERE companyName = :companyName) ORDER BY createdAt DESC")
    suspend fun getByCompanyName(companyName: String): List<ApplicationEntity>

    @Query("SELECT * FROM applications WHERE jobId = :jobId ORDER BY createdAt DESC")
    suspend fun getByJobId(jobId: String): List<ApplicationEntity>

    @Query("SELECT * FROM applications WHERE jobId IN (SELECT id FROM jobs WHERE companyName = :companyName) AND status = :status ORDER BY createdAt DESC")
    suspend fun getByCompanyNameAndStatus(companyName: String, status: String): List<ApplicationEntity>

    @Query("SELECT * FROM applications WHERE id = :applicationId LIMIT 1")
    suspend fun getById(applicationId: String): ApplicationEntity?

    @Query("UPDATE applications SET status = :status WHERE id = :applicationId")
    suspend fun updateStatus(applicationId: String, status: String)
}

