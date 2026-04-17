package com.example.android_project.common.ui

import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.bumptech.glide.Glide

fun ImageView.loadProfilePhoto(url: String?, @DrawableRes fallbackRes: Int) {
    val safeUrl = url?.trim().orEmpty()
    if (safeUrl.isEmpty()) {
        setImageResource(fallbackRes)
        return
    }

    Glide.with(this)
        .load(safeUrl)
        .placeholder(fallbackRes)
        .error(fallbackRes)
        .centerCrop()
        .into(this)
}
