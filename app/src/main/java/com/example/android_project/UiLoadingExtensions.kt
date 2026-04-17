package com.example.android_project

import android.app.Activity

fun Activity.showLoading() {
    UiEffects.showLoading(this)
}

fun Activity.hideLoading() {
    UiEffects.hideLoading(this)
}

