package com.example.android_project

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
import com.example.android_project.common.model.Application
import com.example.android_project.databinding.ActivityJobsAppliedBinding
import com.example.android_project.jobseeker.presentation.JobSeekerDashboardViewModel

class JobsAppliedActivity : AppCompatActivity() {

    private lateinit var binding: ActivityJobsAppliedBinding
    private lateinit var viewModel: JobSeekerDashboardViewModel
    private val applications: MutableList<Application> = mutableListOf()
    private lateinit var applicationsAdapter: ApplicationsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJobsAppliedBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this).get(JobSeekerDashboardViewModel::class.java)

        binding.btnBack.setOnClickListener { finish() }

        binding.rvApplications.layoutManager = LinearLayoutManager(this)
        applicationsAdapter = ApplicationsAdapter(applications)
        binding.rvApplications.adapter = applicationsAdapter

        observeViewModel()
        viewModel.fetchMyApplications()
    }

    private fun withdrawApplication(application: Application) {
        viewModel.withdrawApplication(application.id) { success ->
            if (success) {
                Toast.makeText(this, "Application withdrawn", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchMyApplications()
    }

    private fun observeViewModel() {
        viewModel.myApplications.observe(this) { items ->
            applications.clear()
            applications.addAll(items)
            applicationsAdapter.notifyDataSetChanged()

            binding.tvCount.text = getString(R.string.jobs_applied_count, items.size)
            binding.tvEmpty.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    inner class ApplicationsAdapter(private val items: List<Application>) :
        RecyclerView.Adapter<ApplicationsAdapter.VH>() {

        inner class VH(view: View) : RecyclerView.ViewHolder(view) {
            val tvApplicantName: TextView = view.findViewById(R.id.tvApplicantName)
            val tvAppliedRole: TextView = view.findViewById(R.id.tvAppliedRole)
            val tvStatus: TextView = view.findViewById(R.id.tvStatus)
            val tvTime: TextView = view.findViewById(R.id.tvTime)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_job_applicant, parent, false)
            return VH(view)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val item = items[position]
            holder.tvApplicantName.text = item.applicantName
            holder.tvAppliedRole.text = item.appliedRole
            holder.tvStatus.text = item.status
            holder.tvTime.text = item.timeLabel
            holder.tvStatus.setTextColor(
                when {
                    item.status.equals("Applied", ignoreCase = true) -> getColor(R.color.figmaPrimaryBtn)
                    item.status.equals("Interview", ignoreCase = true) -> getColor(R.color.figmaPrimaryBtn)
                    item.status.equals("Selected", ignoreCase = true) -> getColor(R.color.colorPrimary)
                    item.status.equals("Withdrawn", ignoreCase = true) -> getColor(R.color.statusBadgeGrey)
                    else -> getColor(R.color.statusBadgeGrey)
                }
            )
            holder.itemView.setOnLongClickListener {
                withdrawApplication(item)
                true
            }
        }

        override fun getItemCount(): Int = items.size
    }
}

