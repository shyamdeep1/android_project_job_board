package com.example.android_project

import android.app.Application

class JobBoardApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(UiEffectsLifecycleCallbacks())
    }
}

