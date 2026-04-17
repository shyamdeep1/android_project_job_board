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
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.android_project.common.model.Application
import com.example.android_project.common.session.UserProfileStore
import com.example.android_project.company.presentation.CompanyDashboardViewModel
import com.example.android_project.databinding.ActivityCompanyDashboardBinding
import com.example.android_project.common.ui.loadProfilePhoto
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CompanyDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCompanyDashboardBinding
    private lateinit var viewModel: CompanyDashboardViewModel
    private val profileStore by lazy { UserProfileStore(this) }
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private lateinit var recentApplicationsAdapter: RecentApplicationsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCompanyDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        viewModel = ViewModelProvider(this).get(CompanyDashboardViewModel::class.java)

        binding.etSearchCompany.setOnClickListener {
            startActivity(Intent(this, GlobalSearchActivity::class.java))
        }
        binding.etSearchCompany.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) startActivity(Intent(this, GlobalSearchActivity::class.java))
        }

        binding.cardPostJob.setOnClickListener {
            startActivity(Intent(this, PostJobActivity::class.java))
        }
        binding.cardManageJobs.setOnClickListener {
            startActivity(Intent(this, ManageJobsActivity::class.java))
        }

        binding.cardActiveJobs.setOnClickListener {
            startActivity(Intent(this, ActiveJobsActivity::class.java))
        }
        binding.cardNewApplicants.setOnClickListener {
            startActivity(Intent(this, NewApplicantsActivity::class.java))
        }

        binding.rvRecentApplications.layoutManager = LinearLayoutManager(this)
        recentApplicationsAdapter = RecentApplicationsAdapter { application ->
            val intent = Intent(this, ApplicantDetailActivity::class.java).apply {
                putExtra(ApplicantDetailActivity.EXTRA_APPLICATION_ID, application.id)
                putExtra(ApplicantDetailActivity.EXTRA_APPLICANT_NAME, application.applicantName)
                putExtra(ApplicantDetailActivity.EXTRA_APPLICANT_EMAIL, application.applicantEmail)
                putExtra(ApplicantDetailActivity.EXTRA_APPLICANT_PHONE, application.applicantPhone)
                putExtra(ApplicantDetailActivity.EXTRA_APPLICANT_UID, application.applicantUid)
                putExtra(ApplicantDetailActivity.EXTRA_APPLIED_ROLE, application.appliedRole)
                putExtra(ApplicantDetailActivity.EXTRA_STATUS, application.status)
                putExtra(ApplicantDetailActivity.EXTRA_RESUME_URL, application.resumeUrl)
            }
            startActivity(intent)
        }
        binding.rvRecentApplications.adapter = recentApplicationsAdapter

        binding.tvViewAllApplications.setOnClickListener {
            openApplicants("All Applications", null)
        }

        binding.ivCompanyLogo.setOnClickListener { openCompanyDetail() }

        binding.navHome.setOnClickListener { }
        binding.navNotification.setOnClickListener {
            startActivity(Intent(this, CompanyNotificationsActivity::class.java))
        }
        binding.navMessages.setOnClickListener {
            startActivity(Intent(this, MessagesActivity::class.java))
        }
        binding.navAccount.setOnClickListener {
            startActivity(Intent(this, CompanyProfileActivity::class.java))
        }
        
        observeViewModel()
        viewModel.fetchRecentApplications()
        viewModel.fetchNewApplicants()
        viewModel.loadCompanyJobs()

        loadCompanyName()
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchRecentApplications()
        viewModel.fetchNewApplicants()
        viewModel.loadCompanyJobs()
    }

    private fun loadCompanyName() {
        val cached = profileStore.getCompany()
        if (cached.name.isNotBlank()) {
            binding.tvCompanyName.text = cached.name
        }
        binding.ivCompanyLogo.loadProfilePhoto(cached.photoUrl, R.drawable.ic_company)

        val uid = auth.currentUser?.uid ?: return
        firestore.collection("companies").document(uid).get()
            .addOnSuccessListener { snapshot ->
                val name = snapshot.getString("name") ?: return@addOnSuccessListener
                binding.tvCompanyName.text = name
                val email = snapshot.getString("email") ?: cached.email
                val phone = snapshot.getString("phone") ?: cached.phone
                val website = snapshot.getString("website") ?: cached.website
                val about = snapshot.getString("about") ?: cached.about
                val photoUrl = snapshot.getString("photoUrl") ?: cached.photoUrl
                binding.ivCompanyLogo.loadProfilePhoto(photoUrl, R.drawable.ic_company)
                profileStore.saveCompany(
                    cached.copy(
                        uid = uid,
                        name = name,
                        email = email,
                        phone = phone,
                        website = website,
                        about = about,
                        photoUrl = photoUrl
                    )
                )
            }
    }
    
    private fun observeViewModel() {
        viewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }
        
        viewModel.applications.observe(this) { applications ->
            binding.tvApplicantsCount.text = applications.size.toString()
            val topThree = applications.take(3)
            recentApplicationsAdapter.submitList(topThree)
            binding.tvRecentApplicationsEmpty.visibility = if (topThree.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.companyJobs.observe(this) { jobs ->
            val activeCount = jobs.count { it.status.equals("Active", ignoreCase = true) }
            binding.tvActiveJobsCount.text = activeCount.toString()
        }

        viewModel.newApplicants.observe(this) { newApplicants ->
            binding.tvApplicantsCount.text = newApplicants.size.toString()
        }
    }

    private fun extractJobTitle(application: Application): String {
        val prefix = "Applied for "
        return application.appliedRole.removePrefix(prefix).ifBlank { application.appliedRole }
    }

    private fun openCompanyDetail() {
        val intent = Intent(this, CompanyDetailActivity::class.java).apply {
            putExtra(CompanyDetailActivity.EXTRA_COMPANY_NAME, "Highspeed Studios")
            putExtra(CompanyDetailActivity.EXTRA_PHONE, "+51 632 445 556")
            putExtra(CompanyDetailActivity.EXTRA_EMAIL, "highspeedst@mail.com")
        }
        startActivity(intent)
    }

    private fun openApplicants(jobTitle: String, jobId: String?) {
        val intent = Intent(this, JobApplicantsActivity::class.java).apply {
            putExtra(JobApplicantsActivity.EXTRA_JOB_TITLE, jobTitle)
            if (!jobId.isNullOrBlank()) putExtra(JobApplicantsActivity.EXTRA_JOB_ID, jobId)
        }
        startActivity(intent)
    }

    private class RecentApplicationsAdapter(
        private val onClick: (Application) -> Unit
    ) : ListAdapter<Application, RecentApplicationsAdapter.VH>(Diff) {

        object Diff : DiffUtil.ItemCallback<Application>() {
            override fun areItemsTheSame(oldItem: Application, newItem: Application): Boolean = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Application, newItem: Application): Boolean = oldItem == newItem
        }

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
            val item = getItem(position)
            holder.tvApplicantName.text = item.applicantName
            holder.tvAppliedRole.text = item.appliedRole
            holder.tvStatus.text = item.status
            holder.tvTime.text = item.timeLabel
            holder.tvStatus.setTextColor(
                when {
                    item.status.equals("Applied", ignoreCase = true) -> holder.itemView.context.getColor(R.color.figmaPrimaryBtn)
                    item.status.equals("Reviewed", ignoreCase = true) -> holder.itemView.context.getColor(R.color.colorPrimary)
                    item.status.equals("Interview", ignoreCase = true) -> holder.itemView.context.getColor(R.color.figmaPrimaryBtn)
                    item.status.equals("Selected", ignoreCase = true) -> holder.itemView.context.getColor(R.color.colorPrimary)
                    else -> holder.itemView.context.getColor(R.color.statusBadgeGrey)
                }
            )
            holder.itemView.setOnClickListener { onClick(item) }
        }
    }
}
