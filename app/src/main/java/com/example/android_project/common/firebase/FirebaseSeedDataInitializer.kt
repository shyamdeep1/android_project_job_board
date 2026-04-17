package com.example.android_project.common.firebase

import android.content.Context
import androidx.core.content.edit
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseSeedDataInitializer {

    private const val PREFS_NAME = "db_seed_prefs"
    private const val KEY_SEEDED = "seed_v1_completed"

    fun seedIfNeeded(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (prefs.getBoolean(KEY_SEEDED, false)) return

        val db = FirebaseFirestore.getInstance()
        val batch = db.batch()

        val userDocs = listOf(
            "u1" to mapOf(
                "name" to "Henry Kanwil",
                "email" to "henry@mail.com",
                "role" to "JOB_SEEKER",
                "isActive" to true
            ),
            "u2" to mapOf(
                "name" to "Claudia Surrr",
                "email" to "claudia@mail.com",
                "role" to "JOB_SEEKER",
                "isActive" to true
            ),
            "c1" to mapOf(
                "name" to "Highspeed Studios",
                "email" to "highspeedst@mail.com",
                "role" to "COMPANY",
                "isActive" to true
            ),
            "c3" to mapOf(
                "name" to "Lunar Djaja Corp.",
                "email" to "lunar@mail.com",
                "role" to "COMPANY",
                "isActive" to true
            ),
            "a1" to mapOf(
                "name" to "Main Admin",
                "email" to "admin@jobboard.com",
                "role" to "ADMIN",
                "isActive" to true
            )
        )

        userDocs.forEach { (id, data) ->
            batch.set(db.collection("users").document(id), data)
        }

        val companyDocs = listOf(
            "c1" to mapOf(
                "name" to "Highspeed Studios",
                "ownerUserId" to "c1",
                "location" to "Jakarta",
                "verified" to true
            ),
            "c2" to mapOf(
                "name" to "Lunar Djaja Corp.",
                "ownerUserId" to "c3",
                "location" to "Bandung",
                "verified" to false
            )
        )

        companyDocs.forEach { (id, data) ->
            batch.set(db.collection("companies").document(id), data)
        }

        val adminDocs = listOf(
            "a1" to mapOf(
                "name" to "Main Admin",
                "permissions" to listOf("MANAGE_USERS", "MODERATE_JOBS", "VERIFY_COMPANIES")
            )
        )

        adminDocs.forEach { (id, data) ->
            batch.set(db.collection("admins").document(id), data)
        }

        val jobDocs = listOf(
            "j1" to mapOf(
                "title" to "Senior Software Engineer",
                "companyId" to "c1",
                "companyName" to "Highspeed Studios",
                "location" to "Jakarta",
                "salaryRange" to "$500 - $1,000",
                "status" to "Approved",
                "createdBy" to "c1",
                "createdAt" to FieldValue.serverTimestamp()
            ),
            "j2" to mapOf(
                "title" to "Android Developer",
                "companyId" to "c2",
                "companyName" to "Lunar Djaja Corp.",
                "location" to "Bandung",
                "salaryRange" to "$700 - $1,200",
                "status" to "Pending",
                "createdBy" to "c3",
                "createdAt" to FieldValue.serverTimestamp()
            ),
            "j3" to mapOf(
                "title" to "UI/UX Designer",
                "companyId" to "c1",
                "companyName" to "Highspeed Studios",
                "location" to "Medan",
                "salaryRange" to "$600 - $1,100",
                "status" to "Approved",
                "createdBy" to "c1",
                "createdAt" to FieldValue.serverTimestamp()
            )
        )

        jobDocs.forEach { (id, data) ->
            batch.set(db.collection("jobs").document(id), data)
        }

        val applicationDocs = listOf(
            "a_job_1" to mapOf(
                "applicantName" to "Henry Kanwil",
                "appliedRole" to "Applied for Senior Software Engineer",
                "status" to "Applied",
                "timeLabel" to "2m ago",
                "jobId" to "j1",
                "companyId" to "c1"
            ),
            "a_job_2" to mapOf(
                "applicantName" to "Claudia Surrr",
                "appliedRole" to "Applied for Android Developer",
                "status" to "Reviewed",
                "timeLabel" to "15m ago",
                "jobId" to "j2",
                "companyId" to "c2"
            )
        )

        applicationDocs.forEach { (id, data) ->
            batch.set(db.collection("applications").document(id), data)
        }

        batch.commit()
            .addOnSuccessListener {
                prefs.edit { putBoolean(KEY_SEEDED, true) }
            }
            .addOnFailureListener { error ->
                FirebaseCrashlytics.getInstance().recordException(error)
            }
    }
}
