package com.example.android_project.common.analytics

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics

object AppAnalyticsLogger {

    fun logScreen(context: Context, screenName: String) {
        FirebaseAnalytics.getInstance(context).logEvent(
            FirebaseAnalytics.Event.SCREEN_VIEW,
            Bundle().apply { putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName) }
        )
    }

    fun logFlowStep(context: Context, flowName: String, step: String) {
        FirebaseAnalytics.getInstance(context).logEvent(
            "flow_step",
            Bundle().apply {
                putString("flow_name", flowName)
                putString("step_name", step)
            }
        )
    }

    fun trackDropOff(context: Context, flowName: String, reason: String) {
        FirebaseAnalytics.getInstance(context).logEvent(
            "flow_drop_off",
            Bundle().apply {
                putString("flow_name", flowName)
                putString("drop_reason", reason)
            }
        )
        FirebaseCrashlytics.getInstance().log("DropOff[$flowName]: $reason")
    }
}
