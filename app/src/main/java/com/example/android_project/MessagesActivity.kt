package com.example.android_project

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android_project.common.model.UserRole
import com.example.android_project.common.session.SessionManager
import com.example.android_project.databinding.ActivityMessagesBinding

data class MessageItem(
    val name: String,
    val preview: String,
    val time: String,
    val status: String
)

class MessagesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMessagesBinding

    private val sampleMessages = listOf(
        MessageItem("Gustauv Semalam",    "Roger that sir, thankyou",          "2m ago",  "Read"),
        MessageItem("David Mckanzie",     "Lorem ipsum dolor sit amet, consect...", "2m ago", "Read"),
        MessageItem("Claudia Surrr",      "OK. Lorem ipsum dolor sect...",     "2m ago",  "Pending"),
        MessageItem("Cindy Sinambela",    "OK. Lorem ipsum dolor sect...",     "2m ago",  "Pending"),
        MessageItem("Rose Melati",        "Lorem ipsum dolor",                 "2m ago",  "Unread"),
        MessageItem("Olivia James",       "OK. Lorem ipsum dolor sect...",     "2m ago",  "Unread"),
        MessageItem("Daphne Putri",       "OK. Lorem ipsum dolor sect...",     "2m ago",  "Unread")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessagesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val currentRole = SessionManager(this).getRole()

        binding.btnBack.setOnClickListener { finish() }

        binding.rvMessages.layoutManager = LinearLayoutManager(this)
        binding.rvMessages.adapter = MessageAdapter(sampleMessages) { item ->
            val intent = Intent(this, MessagesDetailActivity::class.java).apply {
                putExtra(MessagesDetailActivity.EXTRA_CONTACT_NAME, item.name)
            }
            startActivity(intent)
        }

        binding.fabNewChat.setOnClickListener {
            Toast.makeText(this, "Start a new chat", Toast.LENGTH_SHORT).show()
        }

        binding.navHome.setOnClickListener {
            when (currentRole) {
                UserRole.COMPANY -> startActivity(Intent(this, CompanyDashboardActivity::class.java))
                else -> startActivity(Intent(this, DashboardActivity::class.java))
            }
            finish()
        }
        binding.navNotifications.setOnClickListener {
            when (currentRole) {
                UserRole.COMPANY -> startActivity(Intent(this, CompanyNotificationsActivity::class.java))
                else -> startActivity(Intent(this, NotificationCenterActivity::class.java))
            }
        }
        binding.navAccount.setOnClickListener {
            when (currentRole) {
                UserRole.COMPANY -> startActivity(Intent(this, CompanyProfileActivity::class.java))
                else -> startActivity(Intent(this, JobSeekerProfileActivity::class.java))
            }
        }
    }

    inner class MessageAdapter(
        private val items: List<MessageItem>,
        private val onClick: (MessageItem) -> Unit
    ) : RecyclerView.Adapter<MessageAdapter.VH>() {

        inner class VH(view: View) : RecyclerView.ViewHolder(view) {
            val tvName: TextView    = view.findViewById(R.id.tvName)
            val tvPreview: TextView = view.findViewById(R.id.tvPreview)
            val tvTime: TextView    = view.findViewById(R.id.tvTime)
            val tvStatus: TextView  = view.findViewById(R.id.tvStatus)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_message_card, parent, false)
            return VH(v)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val item = items[position]
            holder.tvName.text    = item.name
            holder.tvPreview.text = item.preview
            holder.tvTime.text    = item.time
            holder.tvStatus.text  = item.status

            val statusColor = when (item.status) {
                "Read"    -> getColor(R.color.figmaPrimaryBtn)
                "Pending" -> getColor(R.color.statusBadgeGrey)
                else      -> getColor(R.color.statusBadgeGrey)
            }
            holder.tvStatus.setTextColor(statusColor)

            holder.itemView.setOnClickListener { onClick(item) }
        }

        override fun getItemCount() = items.size
    }
}
