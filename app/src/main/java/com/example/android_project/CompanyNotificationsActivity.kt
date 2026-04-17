package com.example.android_project

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android_project.common.firebase.FirebaseNotificationRepository
import com.example.android_project.common.model.AppNotification
import com.example.android_project.common.model.NotificationAudience
import com.example.android_project.common.model.UserRole
import com.example.android_project.common.session.UserProfileStore
import com.example.android_project.common.ui.disableSuggestions
import com.example.android_project.databinding.ActivityCompanyNotificationsBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class CompanyNotificationsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCompanyNotificationsBinding
    private val repository by lazy { FirebaseNotificationRepository() }
    private val items = mutableListOf<AppNotification>()
    private val adapter by lazy { NotificationAdapter(items, ::openNotification, ::showItemActions) }
    private val companyName by lazy {
        UserProfileStore(this).getCompany().name.ifBlank { "Company" }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCompanyNotificationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rvNotifications.layoutManager = LinearLayoutManager(this)
        binding.rvNotifications.adapter = adapter

        binding.btnBack.setOnClickListener { finish() }
        binding.btnCreateNotification.setOnClickListener { showComposeDialog() }
        binding.navHome.setOnClickListener {
            startActivity(Intent(this, CompanyDashboardActivity::class.java))
            finish()
        }
        binding.navNotifications.setOnClickListener { }
        binding.navMessages.setOnClickListener {
            startActivity(Intent(this, MessagesActivity::class.java))
        }
        binding.navAccount.setOnClickListener {
            startActivity(Intent(this, CompanyProfileActivity::class.java))
        }

        loadNotifications()
    }

    private fun loadNotifications() {
        lifecycleScope.launch {
            val notifications = repository.fetchForRole(UserRole.COMPANY)
            runOnUiThread {
                items.clear()
                items.addAll(notifications)
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun openNotification(notification: AppNotification) {
        startActivity(Intent(this, NotificationDetailActivity::class.java).apply {
            putExtra(NotificationDetailExtras.EXTRA_ID, notification.id)
            putExtra(NotificationDetailExtras.EXTRA_TITLE, notification.title)
            putExtra(NotificationDetailExtras.EXTRA_MESSAGE, notification.message)
            putExtra(NotificationDetailExtras.EXTRA_TIME, notification.timeLabel())
            putExtra(NotificationDetailExtras.EXTRA_SENDER_NAME, notification.senderName)
            putExtra(NotificationDetailExtras.EXTRA_AUDIENCE, notification.audience.displayName)
        })
    }

    private fun showItemActions(notification: AppNotification) {
        val options = if (notification.senderRole == UserRole.COMPANY) {
            arrayOf("View", "Edit", "Delete")
        } else {
            arrayOf("View")
        }

        AlertDialog.Builder(this)
            .setTitle(notification.title)
            .setItems(options) { _, which ->
                when (options[which]) {
                    "View" -> openNotification(notification)
                    "Edit" -> showComposeDialog(notification)
                    "Delete" -> confirmDelete(notification)
                }
            }
            .show()
    }

    private fun showComposeDialog(existing: AppNotification? = null) {
        val titleField = EditText(this).apply {
            hint = "Notification title"
            setText(existing?.title.orEmpty())
            disableSuggestions()
        }
        val messageField = EditText(this).apply {
            hint = "Notification message"
            setText(existing?.message.orEmpty())
            minLines = 4
            maxLines = 6
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE
            disableSuggestions()
        }
        val targetField = EditText(this).apply {
            hint = "Target audience"
            setText(existing?.audience?.displayName ?: NotificationAudience.JOB_SEEKER.displayName)
            disableSuggestions()
        }

        val container = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            val padding = (16 * resources.displayMetrics.density).toInt()
            setPadding(padding, padding, padding, 0)
            addView(titleField)
            addView(messageField)
            addView(targetField)
        }

        AlertDialog.Builder(this)
            .setTitle(if (existing == null) "New Notification" else "Edit Notification")
            .setView(container)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Save") { _, _ ->
                val title = titleField.text.toString().trim()
                val message = messageField.text.toString().trim()
                val audience = NotificationAudience.fromInput(targetField.text.toString())

                if (title.isBlank() || message.isBlank()) {
                    Toast.makeText(this, "Please fill title and message", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val now = System.currentTimeMillis()
                val notification = AppNotification(
                    id = existing?.id.orEmpty(),
                    title = title,
                    message = message,
                    audience = audience,
                    senderRole = UserRole.COMPANY,
                    senderName = companyName,
                    createdAt = existing?.createdAt ?: now,
                    updatedAt = now
                )

                saveNotification(notification, existing == null)
            }
            .show()
    }

    private fun saveNotification(notification: AppNotification, isCreate: Boolean) {
        lifecycleScope.launch {
            val result = if (isCreate) repository.create(notification) else repository.update(notification)
            runOnUiThread {
                result.fold(
                    onSuccess = {
                        Toast.makeText(
                            this@CompanyNotificationsActivity,
                            if (isCreate) "Notification created" else "Notification updated",
                            Toast.LENGTH_SHORT
                        ).show()
                        loadNotifications()
                    },
                    onFailure = { error ->
                        Toast.makeText(
                            this@CompanyNotificationsActivity,
                            error.localizedMessage ?: "Failed to save notification",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                )
            }
        }
    }

    private fun confirmDelete(notification: AppNotification) {
        AlertDialog.Builder(this)
            .setTitle("Delete notification?")
            .setMessage(notification.title)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    val result = repository.delete(notification.id)
                    runOnUiThread {
                        result.fold(
                            onSuccess = {
                                Toast.makeText(this@CompanyNotificationsActivity, "Notification deleted", Toast.LENGTH_SHORT).show()
                                loadNotifications()
                            },
                            onFailure = { error ->
                                Toast.makeText(
                                    this@CompanyNotificationsActivity,
                                    error.localizedMessage ?: "Failed to delete notification",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        )
                    }
                }
            }
            .show()
    }

    inner class NotificationAdapter(
        private val notifications: List<AppNotification>,
        private val onClick: (AppNotification) -> Unit,
        private val onLongClick: (AppNotification) -> Unit
    ) : RecyclerView.Adapter<NotificationAdapter.VH>() {

        inner class VH(view: View) : RecyclerView.ViewHolder(view) {
            val tvTitle: TextView = view.findViewById(R.id.tvTitle)
            val tvSubtitle: TextView = view.findViewById(R.id.tvSubtitle)
            val tvTime: TextView = view.findViewById(R.id.tvTime)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_company_notification, parent, false)
            return VH(v)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val item = notifications[position]
            holder.tvTitle.text = item.title
            holder.tvSubtitle.text = item.message
            holder.tvTime.text = item.timeLabel()
            holder.itemView.setOnClickListener { onClick(item) }
            holder.itemView.setOnLongClickListener {
                onLongClick(item)
                true
            }
        }

        override fun getItemCount(): Int = notifications.size
    }
}
