package com.example.android_project

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android_project.common.session.UserProfileStore
import com.example.android_project.databinding.ActivityDashboardBinding
import com.example.android_project.jobseeker.presentation.JobSeekerDashboardViewModel
import com.example.android_project.common.ui.loadProfilePhoto
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.android_project.common.model.Job

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var viewModel: JobSeekerDashboardViewModel
    private lateinit var jobsAdapter: JobsAdapter
    private val jobs: MutableList<Job> = mutableListOf()
    private val profileStore by lazy { UserProfileStore(this) }
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val firestore by lazy { FirebaseFirestore.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        viewModel = ViewModelProvider(this).get(JobSeekerDashboardViewModel::class.java)

        binding.rvJobs.layoutManager = LinearLayoutManager(this)
        jobsAdapter = JobsAdapter(jobs)
        binding.rvJobs.adapter = jobsAdapter

        binding.etSearch.setOnClickListener {
            startActivity(Intent(this, GlobalSearchActivity::class.java))
        }
        binding.etSearch.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) startActivity(Intent(this, GlobalSearchActivity::class.java))
        }

        binding.navHome.setOnClickListener { }
        binding.navNotification.setOnClickListener {
            startActivity(Intent(this, NotificationCenterActivity::class.java))
        }
        binding.navMessages.setOnClickListener {
            startActivity(Intent(this, MessagesActivity::class.java))
        }
        binding.navAccount.setOnClickListener {
            startActivity(Intent(this, JobSeekerProfileActivity::class.java))
        }

        binding.cardJobsApplied.setOnClickListener {
            startActivity(Intent(this, JobsAppliedActivity::class.java))
        }
        binding.cardInterviews.setOnClickListener {
            startActivity(Intent(this, InterviewsActivity::class.java))
        }

        binding.categoryFlash.setOnClickListener { openCategories("trending", "Trending Jobs") }
        binding.categoryDesign.setOnClickListener { openCategories("design", "Design Jobs") }
        binding.categoryUiUx.setOnClickListener { openCategories("ui_ux", "UI/UX Jobs") }
        binding.categoryTranslate.setOnClickListener { openCategories("language", "Language Jobs") }
        binding.categoryCode.setOnClickListener { openCategories("engineering", "Engineering Jobs") }
        binding.categoryDatabase.setOnClickListener { openCategories("data", "Data Jobs") }
        binding.categoryMechanical.setOnClickListener { openCategories("mechanical", "Mechanical Jobs") }
        binding.categoryMore.setOnClickListener { openCategories("all", "All Category Jobs") }

        observeViewModel()
        
        viewModel.fetchRecommendedJobs()
        viewModel.fetchMyApplications()

        loadUserName()
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchMyApplications()
    }

    private fun loadUserName() {
        val cached = profileStore.getJobSeeker()
        val name = cached.name.ifBlank { null }
        if (name != null) {
            binding.tvUserName.text = name
        }
        binding.ivProfile.loadProfilePhoto(cached.photoUrl, R.drawable.ic_jobseeker)

        val uid = auth.currentUser?.uid ?: return
        firestore.collection("job_seekers").document(uid).get()
            .addOnSuccessListener { snapshot ->
                val fetchedName = snapshot.getString("name") ?: return@addOnSuccessListener
                binding.tvUserName.text = fetchedName
                val email = snapshot.getString("email") ?: cached.email
                val title = snapshot.getString("title") ?: cached.title
                val location = snapshot.getString("location") ?: cached.location
                val about = snapshot.getString("about") ?: cached.about
                val photoUrl = snapshot.getString("photoUrl") ?: cached.photoUrl
                binding.ivProfile.loadProfilePhoto(photoUrl, R.drawable.ic_jobseeker)
                profileStore.saveJobSeeker(
                    cached.copy(
                        uid = uid,
                        name = fetchedName,
                        email = email,
                        title = title,
                        location = location,
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
        
        viewModel.jobs.observe(this) { jobs ->
            this.jobs.clear()
            this.jobs.addAll(jobs)
            jobsAdapter.notifyDataSetChanged()
            binding.tvJobsEmpty.visibility = if (jobs.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.jobsAppliedCount.observe(this) { count ->
            binding.tvJobsCount.text = count.toString()
        }

        viewModel.interviewsCount.observe(this) { count ->
            binding.tvInterviewsCount.text = count.toString()
        }
    }

    private fun openJobDetail(jobId: String, title: String, company: String, salary: String, location: String) {
        val intent = Intent(this, JobDetailActivity::class.java).apply {
            putExtra(ApplyFormActivity.EXTRA_JOB_ID,       jobId)
            putExtra(JobDetailActivity.EXTRA_JOB_TITLE,    title)
            putExtra(JobDetailActivity.EXTRA_COMPANY_NAME, company)
            putExtra(JobDetailActivity.EXTRA_SALARY,       salary)
            putExtra(JobDetailActivity.EXTRA_LOCATION,     location)
        }
        startActivity(intent)
    }

    private fun openCategories(categoryKey: String, categoryTitle: String) {
        val intent = Intent().setClassName(this, "com.example.android_project.CategoriesActivity")
        intent.putExtra("category_key", categoryKey)
        intent.putExtra("category_title", categoryTitle)
        startActivity(intent)
    }

    inner class JobsAdapter(private val data: List<Job>) :
        RecyclerView.Adapter<JobsAdapter.VH>() {

        inner class VH(view: View) : RecyclerView.ViewHolder(view) {
            val tvTitle: TextView = view.findViewById(R.id.tvTitle)
            val tvSubtitle: TextView = view.findViewById(R.id.tvSubtitle)
            val tvSalary: TextView = view.findViewById(R.id.tvSalary)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val view = layoutInflater.inflate(R.layout.item_job_simple, parent, false)
            return VH(view)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val job = data[position]
            holder.tvTitle.text = job.title
            holder.tvSubtitle.text = getString(R.string.global_search_company_location, job.companyName, job.location)
            holder.tvSalary.text = job.salaryRange
            holder.itemView.setOnClickListener {
                openJobDetail(job.id, job.title, job.companyName, job.salaryRange, job.location)
            }
        }

        override fun getItemCount(): Int = data.size
    }
}
