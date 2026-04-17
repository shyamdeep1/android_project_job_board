package com.example.android_project

import android.annotation.SuppressLint
import android.app.Activity
import android.os.SystemClock
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isVisible
import com.google.android.material.button.MaterialButton

object UiEffects {

    private const val AUTO_LOADING_DURATION_MS = 450L
    private const val OVERLAY_TAG = "loading_overlay_tag"
    private const val PRESS_APPLIED_MARKER = "press_animation_applied"
    private const val PRESS_TAG_KEY = 0x7f0aff11

    fun showLoading(activity: Activity) {
        val overlay = ensureLoadingOverlay(activity) ?: return
        if (overlay.isVisible) return

        overlay.alpha = 0f
        overlay.isVisible = true
        overlay.animate()
            .alpha(1f)
            .setDuration(140L)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
    }

    fun showAutoLoading(activity: Activity) {
        val overlay = ensureLoadingOverlay(activity) ?: return
        val shownAt = SystemClock.uptimeMillis()
        showLoading(activity)

        overlay.postDelayed({
            val elapsed = SystemClock.uptimeMillis() - shownAt
            if (elapsed >= AUTO_LOADING_DURATION_MS) {
                hideLoading(activity)
            } else {
                overlay.postDelayed({ hideLoading(activity) }, AUTO_LOADING_DURATION_MS - elapsed)
            }
        }, AUTO_LOADING_DURATION_MS)
    }

    fun hideLoading(activity: Activity) {
        val content = activity.findViewById<ViewGroup>(android.R.id.content) ?: return
        val overlay = content.findViewWithTag<View>(OVERLAY_TAG) ?: return
        if (!overlay.isVisible) return

        overlay.animate()
            .alpha(0f)
            .setDuration(180L)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction { overlay.isVisible = false }
            .start()
    }

    fun installButtonPressAnimation(activity: Activity) {
        val content = activity.findViewById<ViewGroup>(android.R.id.content) ?: return
        applyButtonAnimationRecursively(content)
    }

    private fun ensureLoadingOverlay(activity: Activity): View? {
        val content = activity.findViewById<ViewGroup>(android.R.id.content)
            ?: return null

        val existing = content.findViewWithTag<View>(OVERLAY_TAG)
        if (existing != null) return existing

        val overlay = FrameLayout(activity).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(0x66000000)
            isClickable = true
            isFocusable = true
            tag = OVERLAY_TAG
            addView(
                ProgressBar(activity),
                FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = android.view.Gravity.CENTER
                }
            )
        }
        overlay.isVisible = false
        content.addView(overlay)
        return overlay
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun applyButtonAnimationRecursively(view: View) {
        if (isSupportedPressTarget(view) && view.getTag(PRESS_TAG_KEY) != PRESS_APPLIED_MARKER) {
            view.setOnTouchListener { v, event ->
                when (event.action) {
                    android.view.MotionEvent.ACTION_DOWN -> v.startAnimation(scale(1f, 0.96f, 90))
                    android.view.MotionEvent.ACTION_UP -> {
                        v.startAnimation(scale(0.96f, 1f, 120))
                        v.performClick()
                    }
                    android.view.MotionEvent.ACTION_CANCEL -> v.startAnimation(scale(0.96f, 1f, 120))
                }
                true
            }
            view.setTag(PRESS_TAG_KEY, PRESS_APPLIED_MARKER)
        }

        if (view is ViewGroup) {
            for (index in 0 until view.childCount) {
                applyButtonAnimationRecursively(view.getChildAt(index))
            }
        }
    }

    private fun isSupportedPressTarget(view: View): Boolean {
        if (!view.isClickable) return false

        return view is MaterialButton ||
            view is Button ||
            view is ImageButton ||
            view is TextView ||
            view is ImageView ||
            view.javaClass.simpleName.contains("CardView", ignoreCase = true)
    }

    private fun scale(from: Float, to: Float, duration: Long): Animation {
        return ScaleAnimation(
            from,
            to,
            from,
            to,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        ).apply {
            this.duration = duration
            fillAfter = true
            interpolator = AccelerateDecelerateInterpolator()
        }
    }
}


