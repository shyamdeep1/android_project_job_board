package com.example.android_project.common.model

enum class ApplicationStatus {
    APPLIED,
    REVIEWED,
    INTERVIEW,
    SELECTED,
    REJECTED,
    WITHDRAWN;

    fun nextReviewStep(): ApplicationStatus {
        return when (this) {
            APPLIED -> REVIEWED
            REVIEWED -> INTERVIEW
            INTERVIEW -> SELECTED
            SELECTED -> SELECTED
            REJECTED -> REJECTED
            WITHDRAWN -> WITHDRAWN
        }
    }

    fun label(): String {
        return when (this) {
            APPLIED -> "Applied"
            REVIEWED -> "Reviewed"
            INTERVIEW -> "Interview"
            SELECTED -> "Selected"
            REJECTED -> "Rejected"
            WITHDRAWN -> "Withdrawn"
        }
    }

    fun isTerminal(): Boolean = this == SELECTED || this == REJECTED || this == WITHDRAWN

    companion object {
        fun fromLabel(label: String): ApplicationStatus {
            return when {
                label.equals("New", true) -> APPLIED
                label.equals("Hired", true) -> SELECTED
                else -> entries.firstOrNull { it.label().equals(label, true) } ?: APPLIED
            }
        }
    }
}
