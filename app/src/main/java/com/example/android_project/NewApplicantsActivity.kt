package com.example.android_project

import android.content.Intent
import android.os.Bundle
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
import com.example.android_project.databinding.ActivityNewApplicantsBinding

class NewApplicantsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewApplicantsBinding
    private lateinit var viewModel: CompanyDashboardViewModel
    private lateinit var applicantsAdapter: ApplicantsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewApplicantsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this).get(CompanyDashboardViewModel::class.java)

        binding.btnBack.setOnClickListener { finish() }

        binding.rvApplicants.layoutManager = LinearLayoutManager(this)
        applicantsAdapter = ApplicantsAdapter { item ->
            startActivity(Intent(this, ApplicantDetailActivity::class.java).apply {
                putExtra(ApplicantDetailActivity.EXTRA_APPLICATION_ID, item.id)
                putExtra(ApplicantDetailActivity.EXTRA_APPLICANT_NAME, item.applicantName)
                putExtra(ApplicantDetailActivity.EXTRA_APPLICANT_EMAIL, item.applicantEmail)
                putExtra(ApplicantDetailActivity.EXTRA_APPLICANT_PHONE, item.applicantPhone)
                putExtra(ApplicantDetailActivity.EXTRA_APPLICANT_UID, item.applicantUid)
                putExtra(ApplicantDetailActivity.EXTRA_APPLIED_ROLE, item.appliedRole)
                putExtra(ApplicantDetailActivity.EXTRA_STATUS, item.status)
                putExtra("extra_resume_url", item.resumeUrl)
            })
        }
        binding.rvApplicants.adapter = applicantsAdapter

        observeViewModel()
        viewModel.fetchNewApplicants()
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchNewApplicants()
    }

    private fun observeViewModel() {
        viewModel.newApplicants.observe(this) { applicants ->
            applicantsAdapter.submitList(applicants)
            binding.tvCount.text = "${applicants.size} applicants"
            binding.tvEmpty.visibility = if (applicants.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private class ApplicantsAdapter(
        private val onClick: (Application) -> Unit
    ) : ListAdapter<Application, ApplicantsAdapter.VH>(Diff) {

        object Diff : DiffUtil.ItemCallback<Application>() {
            override fun areItemsTheSame(oldItem: Application, newItem: Application): Boolean = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Application, newItem: Application): Boolean = oldItem == newItem
        }

        class VH(view: View) : RecyclerView.ViewHolder(view) {
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
            val item = getItem(position)
            holder.tvApplicantName.text = item.applicantName
            holder.tvAppliedRole.text = item.appliedRole
            holder.tvStatus.text = item.status
            holder.tvTime.text = item.timeLabel
            holder.tvStatus.setTextColor(
                when {
                    item.status.equals("Interview", ignoreCase = true) -> holder.itemView.context.getColor(R.color.figmaPrimaryBtn)
                    item.status.equals("Applied", ignoreCase = true) -> holder.itemView.context.getColor(R.color.figmaPrimaryBtn)
                    else -> holder.itemView.context.getColor(R.color.statusBadgeGrey)
                }
            )
            holder.itemView.setOnClickListener { onClick(item) }
        }
    }
}

