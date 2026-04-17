package com.example.android_project

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android_project.common.model.Job
import com.example.android_project.company.presentation.CompanyDashboardViewModel
import com.example.android_project.databinding.ActivityManageJobsBinding

class ManageJobsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityManageJobsBinding
    private lateinit var adapter: JobsAdapter
    private lateinit var viewModel: CompanyDashboardViewModel
    private val jobs: MutableList<Job> = mutableListOf()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageJobsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[CompanyDashboardViewModel::class.java]

        adapter = JobsAdapter(jobs)
        binding.rvJobs.layoutManager = LinearLayoutManager(this)
        binding.rvJobs.adapter = adapter

        binding.btnBack.setOnClickListener { finish() }
        binding.btnGoReports.setOnClickListener {
            startActivity(Intent(this, ReportsModerationActivity::class.java))
        }

        observeViewModel()
        viewModel.loadCompanyJobs()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadCompanyJobs()
    }

    private fun observeViewModel() {
        viewModel.companyJobs.observe(this) { list ->
            jobs.clear()
            jobs.addAll(list)
            adapter.notifyDataSetChanged()
        }

        viewModel.error.observe(this) { error ->
            error?.let { Toast.makeText(this, it, Toast.LENGTH_SHORT).show() }
        }
    }

    inner class JobsAdapter(private val items: MutableList<Job>) :
        RecyclerView.Adapter<JobsAdapter.VH>() {

        inner class VH(view: View) : RecyclerView.ViewHolder(view) {
            val tvJobTitle: TextView = view.findViewById(R.id.tvJobTitle)
            val tvCompany: TextView = view.findViewById(R.id.tvCompany)
            val tvStatus: TextView = view.findViewById(R.id.tvStatus)
            val btnApprove: TextView = view.findViewById(R.id.btnApprove)
            val btnReject: TextView = view.findViewById(R.id.btnReject)
            val btnRemove: TextView = view.findViewById(R.id.btnRemove)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_manage_job, parent, false)
            return VH(view)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            try {
                if (position < 0 || position >= items.size) return

                val item = items[position]
                holder.tvJobTitle.text = item.title
                holder.tvCompany.text = "${item.companyName} · ${item.location}"
                holder.tvStatus.text = item.status
                holder.tvStatus.setTextColor(
                    if (item.status.equals("Approved", ignoreCase = true) || item.status.equals("Active", ignoreCase = true)) getColor(R.color.figmaPrimaryBtn)
                    else getColor(R.color.statusBadgeGrey)
                )

                holder.btnApprove.setOnClickListener {
                    if (position < items.size) {
                        val updated = item.copy(status = "Approved")
                        items[position] = updated
                        notifyItemChanged(position)
                        viewModel.updateJob(updated)
                        Toast.makeText(this@ManageJobsActivity, "${item.title} approved", Toast.LENGTH_SHORT).show()
                    }
                }
                holder.btnReject.setOnClickListener {
                    if (position < items.size) {
                        val updated = item.copy(status = "Rejected")
                        items[position] = updated
                        notifyItemChanged(position)
                        viewModel.updateJob(updated)
                        Toast.makeText(this@ManageJobsActivity, "${item.title} rejected", Toast.LENGTH_SHORT).show()
                    }
                }
                holder.btnRemove.setOnClickListener {
                    if (position < items.size) {
                        val removed = items.removeAt(position)
                        notifyItemRemoved(position)
                        viewModel.deleteJob(removed.id)
                        Toast.makeText(this@ManageJobsActivity, "${removed.title} removed", Toast.LENGTH_SHORT).show()
                    }
                }

                holder.itemView.setOnClickListener {
                    val intent = Intent(this@ManageJobsActivity, PostJobActivity::class.java)
                    intent.putExtra(PostJobActivity.EXTRA_JOB, item)
                    startActivity(intent)
                }
            } catch (e: Exception) {
                Toast.makeText(this@ManageJobsActivity, "Error loading job", Toast.LENGTH_SHORT).show()
            }
        }

        override fun getItemCount(): Int = items.size
    }
}
