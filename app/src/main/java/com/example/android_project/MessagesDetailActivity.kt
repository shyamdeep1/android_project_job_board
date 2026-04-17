package com.example.android_project

import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.android_project.databinding.ActivityMessagesDetailBinding
import kotlinx.parcelize.Parcelize

@Parcelize
data class ChatMessage(
    val text: String,
    val isOutgoing: Boolean,
    val timestamp: String
) : Parcelable

class MessagesDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMessagesDetailBinding
    private val chatMessages = mutableListOf<ChatMessage>()
    
    companion object {
        const val EXTRA_CONTACT_NAME = "extra_contact_name"
        const val SAVED_MESSAGES = "saved_messages"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessagesDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val contactName = intent.getStringExtra(EXTRA_CONTACT_NAME) ?: "Claudia Surr"
        binding.tvContactName.text = contactName

        if (savedInstanceState != null) {
            @Suppress("UNCHECKED_CAST")
            val messages = savedInstanceState.getParcelableArrayList<ChatMessage>(SAVED_MESSAGES)
            if (messages != null) {
                chatMessages.addAll(messages)
                reloadChatUI()
            }
        }

        binding.btnBack.setOnClickListener { finish() }

        binding.btnOptions.setOnClickListener {
            Toast.makeText(this, "Options", Toast.LENGTH_SHORT).show()
        }

        binding.chatScrollView.post {
            binding.chatScrollView.fullScroll(View.FOCUS_DOWN)
        }

        binding.btnSend.setOnClickListener { sendMessage() }

        binding.navHome.setOnClickListener { finish() }
        binding.navNotifications.setOnClickListener {
            Toast.makeText(this, "Notifications", Toast.LENGTH_SHORT).show()
        }
        binding.navAccount.setOnClickListener {
            Toast.makeText(this, "Account", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList(SAVED_MESSAGES, ArrayList(chatMessages))
    }

    private fun sendMessage() {
        val text = binding.etMessage.text.toString().trim()
        if (text.isEmpty()) return

        val message = ChatMessage(text, true, "Just now")
        chatMessages.add(message)
        
        addMessageBubble(message)
        binding.etMessage.text?.clear()

        binding.chatScrollView.post {
            binding.chatScrollView.fullScroll(View.FOCUS_DOWN)
        }
    }
    
    private fun reloadChatUI() {
        binding.chatContainer.removeAllViews()
        for (message in chatMessages) {
            addMessageBubble(message)
        }
    }
    
    private fun addMessageBubble(message: ChatMessage) {
        try {
            val sentLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = if (message.isOutgoing) android.view.Gravity.END else android.view.Gravity.START
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).also { it.bottomMargin = 16.dp }
            }

            val bubble = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                background = getDrawable(
                    if (message.isOutgoing) R.drawable.bg_chat_sent else R.drawable.bg_chat_received
                )
                setPadding(18.dp, 14.dp, 18.dp, 14.dp)
            }

            val msgTv = TextView(this).apply {
                this.text = message.text
                setTextColor(getColor(if (message.isOutgoing) R.color.white else R.color.black))
                textSize = 16f
            }
            bubble.addView(msgTv)

            val timeTv = TextView(this).apply {
                this.text = message.timestamp
                setTextColor(getColor(R.color.chatTimestamp))
                textSize = 12f
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).also { it.topMargin = 4.dp }
            }

            sentLayout.addView(bubble)
            sentLayout.addView(timeTv)
            binding.chatContainer.addView(sentLayout)
        } catch (e: Exception) {
            Toast.makeText(this, "Error displaying message", Toast.LENGTH_SHORT).show()
        }
    }

    private val Int.dp: Int get() = (this * resources.displayMetrics.density).toInt()
}
