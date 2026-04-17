package com.example.android_project.admin.data

import com.example.android_project.common.data.mock.MockDataProvider
import com.example.android_project.common.firebase.FirestoreMappers
import com.example.android_project.common.firebase.awaitResult
import com.example.android_project.common.model.AppUser
import com.example.android_project.common.model.Job
import com.google.firebase.firestore.FirebaseFirestore

class FirebaseAdminRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : AdminRepository {
    override suspend fun fetchUsers(): List<AppUser> {
        return runCatching {
            val snapshot = firestore.collection("users").get().awaitResult()
            snapshot.documents.map(FirestoreMappers::mapUser)
        }.getOrElse {
            MockDataProvider.users()
        }
    }

    override suspend fun fetchJobsForModeration(): List<Job> {
        return runCatching {
            val snapshot = firestore.collection("jobs").get().awaitResult()
            snapshot.documents.map(FirestoreMappers::mapJob)
        }.getOrElse {
            MockDataProvider.jobsForModeration()
        }
    }
}
