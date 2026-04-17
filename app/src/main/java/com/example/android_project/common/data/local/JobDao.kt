package com.example.android_project.common.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface JobDao {
    @Query("SELECT * FROM jobs ORDER BY createdAt DESC")
    suspend fun getAll(): List<JobEntity>

    @Query("SELECT * FROM jobs WHERE status != 'Rejected' ORDER BY createdAt DESC")
    suspend fun getVisibleForSeekers(): List<JobEntity>

    @Query("SELECT * FROM jobs WHERE id = :jobId LIMIT 1")
    suspend fun getById(jobId: String): JobEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(job: JobEntity)

    @Update
    suspend fun update(job: JobEntity)

    @Query("DELETE FROM jobs WHERE id = :jobId")
    suspend fun delete(jobId: String)
}
