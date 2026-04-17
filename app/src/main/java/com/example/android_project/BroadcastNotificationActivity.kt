package com.example.android_project

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.android_project.common.firebase.FirebaseNotificationRepository
import com.example.android_project.common.model.AppNotification
import com.example.android_project.common.model.NotificationAudience
import com.example.android_project.common.model.UserRole
import com.example.android_project.common.ui.disableSuggestions
import com.example.android_project.databinding.ActivityBroadcastNotificationBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class BroadcastNotificationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBroadcastNotificationBinding
    private val repository by lazy { FirebaseNotificationRepository() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBroadcastNotificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }
        binding.etTitle.disableSuggestions()
        binding.etMessage.disableSuggestions()
        binding.etTarget.disableSuggestions()

        binding.btnSendBroadcast.setOnClickListener {
            sendBroadcast()
        }
    }

    private fun sendBroadcast() {
        val title = binding.etTitle.text.toString().trim()
        val message = binding.etMessage.text.toString().trim()
        val target = NotificationAudience.fromInput(binding.etTarget.text.toString())

        if (title.isBlank() || message.isBlank()) {
            Toast.makeText(this, "Please fill title and message", Toast.LENGTH_SHORT).show()
            return
        }

        val notification = AppNotification(
            id = "",
            title = title,
            message = message,
            audience = target,
            senderRole = UserRole.ADMIN,
            senderName = "Admin",
            createdAt = System.currentTimeMillis()
        )

        binding.btnSendBroadcast.isEnabled = false
        lifecycleScope.launch {
            val result = repository.create(notification)
            runOnUiThread {
                binding.btnSendBroadcast.isEnabled = true
                result.fold(
                    onSuccess = {
                        Toast.makeText(this@BroadcastNotificationActivity, "Notification sent to ${target.displayName}", Toast.LENGTH_SHORT).show()
                        binding.etTitle.text?.clear()
                        binding.etMessage.text?.clear()
                        binding.etTarget.setText(target.displayName)
                    },
                    onFailure = { error ->
                        Toast.makeText(this@BroadcastNotificationActivity, error.localizedMessage ?: "Failed to send notification", Toast.LENGTH_LONG).show()
                    }
                )
            }
        }
    }
}
