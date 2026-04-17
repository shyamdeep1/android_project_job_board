package com.example.android_project

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.android_project.databinding.ActivityNotificationDetailBinding

object NotificationDetailExtras {
    const val EXTRA_ID = "extra_id"
    const val EXTRA_TITLE = "extra_title"
    const val EXTRA_MESSAGE = "extra_message"
    const val EXTRA_SUBTITLE = "extra_subtitle"
    const val EXTRA_TIME = "extra_time"
    const val EXTRA_SENDER_NAME = "extra_sender_name"
    const val EXTRA_AUDIENCE = "extra_audience"
}

class NotificationDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val notificationId = intent.getStringExtra(NotificationDetailExtras.EXTRA_ID).orEmpty()
        val title = intent.getStringExtra(NotificationDetailExtras.EXTRA_TITLE) ?: "Notification"
        val message = intent.getStringExtra(NotificationDetailExtras.EXTRA_MESSAGE)
            ?: intent.getStringExtra(NotificationDetailExtras.EXTRA_SUBTITLE)
            ?: ""
        val time = intent.getStringExtra(NotificationDetailExtras.EXTRA_TIME) ?: ""
        val senderName = intent.getStringExtra(NotificationDetailExtras.EXTRA_SENDER_NAME).orEmpty()
        val audience = intent.getStringExtra(NotificationDetailExtras.EXTRA_AUDIENCE).orEmpty()

        binding.tvTitle.text = title
        binding.tvSubtitle.text = buildString {
            append(message)
            if (senderName.isNotBlank() || audience.isNotBlank()) {
                if (isNotBlank()) append("\n\n")
                append(listOf(senderName.takeIf { it.isNotBlank() }, audience.takeIf { it.isNotBlank() }).joinToString(" • "))
            }
            if (notificationId.isNotBlank()) {
                if (isNotBlank()) append("\n\n")
                append("ID: ").append(notificationId)
            }
        }
        binding.tvTime.text = time

        binding.btnBack.setOnClickListener { finish() }
    }

}

