package com.example.android_project

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.android_project.common.model.Application
import com.example.android_project.company.presentation.CompanyDashboardViewModel
import com.example.android_project.databinding.ActivityJobApplicantsBinding

class JobApplicantsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityJobApplicantsBinding
    private lateinit var viewModel: CompanyDashboardViewModel
    private lateinit var applicantAdapter: ApplicantAdapter
    private var allApplicants: List<Application> = emptyList()
    private var currentJobTitle: String = "All Applications"
    private var currentJobId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJobApplicantsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this).get(CompanyDashboardViewModel::class.java)

        currentJobTitle = intent.getStringExtra(EXTRA_JOB_TITLE) ?: "All Applications"
        currentJobId = intent.getStringExtra(EXTRA_JOB_ID)
        binding.tvTitle.text = if (currentJobTitle == "All Applications") "All Applicants" else "Job Applicants"

        binding.rvApplicants.layoutManager = LinearLayoutManager(this)
        applicantAdapter = ApplicantAdapter { item ->
            startActivity(Intent(this, ApplicantDetailActivity::class.java).apply {
                putExtra(ApplicantDetailActivity.EXTRA_APPLICATION_ID, item.id)
                putExtra(ApplicantDetailActivity.EXTRA_APPLICANT_NAME, item.applicantName)
                putExtra(ApplicantDetailActivity.EXTRA_APPLICANT_EMAIL, item.applicantEmail)
                putExtra(ApplicantDetailActivity.EXTRA_APPLICANT_PHONE, item.applicantPhone)
                putExtra(ApplicantDetailActivity.EXTRA_APPLICANT_UID, item.applicantUid)
                putExtra(ApplicantDetailActivity.EXTRA_APPLIED_ROLE, item.appliedRole)
                putExtra(ApplicantDetailActivity.EXTRA_STATUS, item.status)
                putExtra(ApplicantDetailActivity.EXTRA_RESUME_URL, item.resumeUrl)
            })
        }
        binding.rvApplicants.adapter = applicantAdapter

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                applySearch(s?.toString().orEmpty())
            }
        })

        observeViewModel()
        loadApplicants()

        binding.btnBack.setOnClickListener { finish() }
        binding.navHome.setOnClickListener { finish() }
        binding.navNotifications.setOnClickListener {
            startActivity(Intent(this, CompanyNotificationsActivity::class.java))
        }
        binding.navMessages.setOnClickListener {
            startActivity(Intent(this, MessagesActivity::class.java))
        }
        binding.navAccount.setOnClickListener {
            startActivity(Intent(this, CompanyProfileActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        loadApplicants()
    }

    private fun loadApplicants() {
        val jobId = currentJobId
        if (!jobId.isNullOrBlank()) {
            viewModel.fetchApplicationsForJob(jobId)
        } else {
            viewModel.fetchRecentApplications()
        }
    }

    private fun observeViewModel() {
        viewModel.jobApplicants.observe(this) { applicants ->
            if (!currentJobId.isNullOrBlank()) {
                allApplicants = applicants
                renderCount(applicants.size)
                applySearch(binding.etSearch.text?.toString().orEmpty())
            }
        }

        viewModel.applications.observe(this) { applicants ->
            if (currentJobId.isNullOrBlank()) {
                allApplicants = applicants
                renderCount(applicants.size)
                applySearch(binding.etSearch.text?.toString().orEmpty())
            }
        }
    }

    private fun applySearch(query: String) {
        val normalized = query.trim()
        val filtered = if (normalized.isBlank()) {
            allApplicants
        } else {
            allApplicants.filter {
                it.applicantName.contains(normalized, ignoreCase = true) ||
                    it.appliedRole.contains(normalized, ignoreCase = true)
            }
        }
        applicantAdapter.submitList(filtered)
    }

    private fun renderCount(count: Int) {
        binding.tvApplicantsCount.text = if (currentJobTitle == "All Applications") {
            "$count applicants"
        } else {
            "$count applicants for $currentJobTitle"
        }
    }

    class ApplicantAdapter(
        private val onClick: (Application) -> Unit
    ) : ListAdapter<Application, ApplicantAdapter.VH>(Diff) {

        object Diff : DiffUtil.ItemCallback<Application>() {
            override fun areItemsTheSame(oldItem: Application, newItem: Application): Boolean = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Application, newItem: Application): Boolean = oldItem == newItem
        }

        class VH(view: View) : RecyclerView.ViewHolder(view) {
            val tvApplicantName: TextView = view.findViewById(R.id.tvApplicantName)
            val tvAppliedRole: TextView = view.findViewById(R.id.tvAppliedRole)
            val tvTime: TextView = view.findViewById(R.id.tvTime)
            val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_job_applicant, parent, false)
            return VH(v)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val item = getItem(position)
            holder.tvApplicantName.text = item.applicantName
            holder.tvAppliedRole.text = item.appliedRole
            holder.tvTime.text = item.timeLabel
            holder.tvStatus.text = item.status
            holder.tvStatus.setTextColor(
                when (item.status) {
                    "Applied", "Interview" -> holder.itemView.context.getColor(R.color.figmaPrimaryBtn)
                    "Reviewed", "Selected" -> holder.itemView.context.getColor(R.color.colorPrimary)
                    else -> holder.itemView.context.getColor(R.color.statusBadgeGrey)
                }
            )
            holder.itemView.setOnClickListener { onClick(item) }
        }
    }

    companion object {
        const val EXTRA_JOB_TITLE = "extra_job_title"
        const val EXTRA_JOB_ID = "extra_job_id"
    }
}
