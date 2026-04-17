package com.example.android_project.common.firebase

import com.example.android_project.common.model.AppNotification
import com.example.android_project.common.model.NotificationAudience
import com.example.android_project.common.model.UserRole
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class FirebaseNotificationRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    suspend fun fetchForRole(role: UserRole): List<AppNotification> = withContext(Dispatchers.IO) {
        runCatching {
            firestore.collection(COLLECTION)
                .get()
                .awaitResult()
                .documents
                .mapNotNull(::mapNotification)
                .filter { it.isVisibleTo(role) }
                .sortedByDescending { it.createdAt }
        }.getOrElse { emptyList() }
    }

    suspend fun create(notification: AppNotification): Result<String> = withContext(Dispatchers.IO) {
        try {
            val id = notification.id.ifBlank { UUID.randomUUID().toString() }
            val data = notification.copy(id = id, updatedAt = notification.createdAt).toMap()
            firestore.collection(COLLECTION).document(id).set(data).awaitResult()
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun update(notification: AppNotification): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val data = notification.copy(updatedAt = System.currentTimeMillis()).toMap()
            firestore.collection(COLLECTION).document(notification.id).set(data).awaitResult()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun delete(notificationId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            firestore.collection(COLLECTION).document(notificationId).delete().awaitResult()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun mapNotification(doc: DocumentSnapshot): AppNotification? {
        val title = doc.getString("title").orEmpty().trim()
        val message = doc.getString("message")?.trim().orEmpty()
            .ifBlank { doc.getString("subtitle")?.trim().orEmpty() }
        if (title.isBlank() && message.isBlank()) return null

        val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
        val senderRole = runCatching {
            UserRole.valueOf(doc.getString("senderRole").orEmpty())
        }.getOrDefault(UserRole.COMPANY)
        val audience = NotificationAudience.fromInput(
            doc.getString("audience") ?: doc.getString("target")
        )

        return AppNotification(
            id = doc.id,
            title = title.ifBlank { message },
            message = message.ifBlank { title },
            audience = audience,
            senderRole = senderRole,
            senderName = doc.getString("senderName").orEmpty().ifBlank {
                when (senderRole) {
                    UserRole.ADMIN -> "Admin"
                    UserRole.COMPANY -> "Company"
                    UserRole.JOB_SEEKER -> "Job Seeker"
                    UserRole.GUEST -> "Guest"
                }
            },
            createdAt = createdAt,
            updatedAt = doc.getLong("updatedAt") ?: createdAt
        )
    }

    private fun AppNotification.toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "title" to title,
            "message" to message,
            "subtitle" to message,
            "audience" to audience.name,
            "target" to audience.displayName,
            "senderRole" to senderRole.name,
            "senderName" to senderName,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt
        )
    }

    companion object {
        private const val COLLECTION = "notifications"
    }
}

