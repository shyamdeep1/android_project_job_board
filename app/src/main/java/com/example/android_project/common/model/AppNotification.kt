package com.example.android_project.common.model

import java.io.Serializable

fun NotificationAudience.matches(role: UserRole): Boolean = when (this) {
    NotificationAudience.ALL -> true
    NotificationAudience.JOB_SEEKER -> role == UserRole.JOB_SEEKER
    NotificationAudience.COMPANY -> role == UserRole.COMPANY
}

enum class NotificationAudience(val displayName: String) {
    ALL("All"),
    JOB_SEEKER("Job Seekers"),
    COMPANY("Companies");

    companion object {
        fun fromInput(raw: String?): NotificationAudience {
            val normalized = raw.orEmpty().trim().lowercase()
            return when {
                normalized.isBlank() || normalized == "all" -> ALL
                normalized.contains("job") || normalized.contains("seeker") -> JOB_SEEKER
                normalized.contains("company") -> COMPANY
                else -> ALL
            }
        }
    }
}

data class AppNotification(
    val id: String,
    val title: String,
    val message: String,
    val audience: NotificationAudience,
    val senderRole: UserRole,
    val senderName: String,
    val createdAt: Long,
    val updatedAt: Long = createdAt
) : Serializable {

    fun isVisibleTo(role: UserRole): Boolean {
        return audience.matches(role) || senderRole == role
    }

    fun timeLabel(now: Long = System.currentTimeMillis()): String {
        val ageMillis = (now - createdAt).coerceAtLeast(0L)
        val minutes = ageMillis / 60_000L
        val hours = ageMillis / 3_600_000L
        val days = ageMillis / 86_400_000L

        return when {
            ageMillis < 60_000L -> "Just now"
            minutes < 60L -> "${minutes}m ago"
            hours < 24L -> "${hours}h ago"
            else -> "${days}d ago"
        }
    }
}

