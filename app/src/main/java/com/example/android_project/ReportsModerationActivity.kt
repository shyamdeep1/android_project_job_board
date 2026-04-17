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
import com.example.android_project.databinding.ActivityReportsModerationBinding

data class ReportItem(
    val job: String,
    val reason: String,
    val reporter: String
)

class ReportsModerationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReportsModerationBinding
    private val reports = mutableListOf(
        ReportItem("Android Developer at Highspeed Studios", "Fake posting / spam content", "Henry Kanwil"),
        ReportItem("UI/UX Designer at Darkseer Studios", "Misleading salary information", "Claudia Surrr"),
        ReportItem("Data Engineer at Lunar Djaja", "Duplicate job posting", "David Mckanzie")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportsModerationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rvReports.layoutManager = LinearLayoutManager(this)
        binding.rvReports.adapter = ReportsAdapter(reports)

        binding.btnBack.setOnClickListener { finish() }
        binding.btnGoVerification.setOnClickListener {
            startActivity(Intent(this, VerificationRequestsActivity::class.java))
        }
    }

    inner class ReportsAdapter(private val items: MutableList<ReportItem>) :
        RecyclerView.Adapter<ReportsAdapter.VH>() {

        inner class VH(view: View) : RecyclerView.ViewHolder(view) {
            val tvReportedJob: TextView = view.findViewById(R.id.tvReportedJob)
            val tvReason: TextView = view.findViewById(R.id.tvReason)
            val tvReporter: TextView = view.findViewById(R.id.tvReporter)
            val btnDismiss: TextView = view.findViewById(R.id.btnDismiss)
            val btnTakeDown: TextView = view.findViewById(R.id.btnTakeDown)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_report_moderation, parent, false)
            return VH(view)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val item = items[position]
            holder.tvReportedJob.text = item.job
            holder.tvReason.text = "Reason: ${item.reason}"
            holder.tvReporter.text = "Reported by: ${item.reporter}"

            holder.btnDismiss.setOnClickListener {
                val removed = items.removeAt(position)
                notifyItemRemoved(position)
                Toast.makeText(this@ReportsModerationActivity, "Dismissed report for ${removed.job}", Toast.LENGTH_SHORT).show()
            }
            holder.btnTakeDown.setOnClickListener {
                val removed = items.removeAt(position)
                notifyItemRemoved(position)
                Toast.makeText(this@ReportsModerationActivity, "Job taken down: ${removed.job}", Toast.LENGTH_SHORT).show()
            }
        }

        override fun getItemCount(): Int = items.size
    }
}
