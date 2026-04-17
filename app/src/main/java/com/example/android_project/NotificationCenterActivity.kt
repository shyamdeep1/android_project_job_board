package com.example.android_project

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android_project.common.firebase.FirebaseNotificationRepository
import com.example.android_project.common.model.AppNotification
import com.example.android_project.common.model.UserRole
import com.example.android_project.databinding.ActivityNotificationCenterBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class NotificationCenterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationCenterBinding
    private val repository by lazy { FirebaseNotificationRepository() }
    private val items = mutableListOf<AppNotification>()
    private val adapter by lazy { CenterAdapter(items) { openNotification(it) } }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationCenterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rvNotifications.layoutManager = LinearLayoutManager(this)
        binding.rvNotifications.adapter = adapter

        binding.btnBack.setOnClickListener { finish() }
        loadNotifications()
    }

    private fun loadNotifications() {
        lifecycleScope.launch {
            val notifications = repository.fetchForRole(UserRole.JOB_SEEKER)
            val oldSize = items.size
            items.clear()
            if (oldSize > 0) {
                adapter.notifyItemRangeRemoved(0, oldSize)
            }
            if (notifications.isNotEmpty()) {
                items.addAll(notifications)
                adapter.notifyItemRangeInserted(0, notifications.size)
            }
        }
    }

    private fun openNotification(item: AppNotification) {
        startActivity(Intent(this, NotificationDetailActivity::class.java).apply {
            putExtra(NotificationDetailExtras.EXTRA_ID, item.id)
            putExtra(NotificationDetailExtras.EXTRA_TITLE, item.title)
            putExtra(NotificationDetailExtras.EXTRA_MESSAGE, item.message)
            putExtra(NotificationDetailExtras.EXTRA_TIME, item.timeLabel())
            putExtra(NotificationDetailExtras.EXTRA_SENDER_NAME, item.senderName)
            putExtra(NotificationDetailExtras.EXTRA_AUDIENCE, item.audience.displayName)
        })
    }

    inner class CenterAdapter(
        private val notifications: List<AppNotification>,
        private val onClick: (AppNotification) -> Unit
    ) :
        RecyclerView.Adapter<CenterAdapter.VH>() {

        inner class VH(view: View) : RecyclerView.ViewHolder(view) {
            val tvTitle: TextView = view.findViewById(R.id.tvTitle)
            val tvSubtitle: TextView = view.findViewById(R.id.tvSubtitle)
            val tvTime: TextView = view.findViewById(R.id.tvTime)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_company_notification, parent, false)
            return VH(view)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val item = notifications[position]
            holder.tvTitle.text = item.title
            holder.tvSubtitle.text = item.message
            holder.tvTime.text = item.timeLabel()
            holder.itemView.setOnClickListener { onClick(item) }
        }

        override fun getItemCount(): Int = notifications.size
    }
}
