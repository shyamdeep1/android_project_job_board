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
import com.example.android_project.databinding.ActivityInterviewsBinding
import com.example.android_project.jobseeker.presentation.JobSeekerDashboardViewModel

class InterviewsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInterviewsBinding
    private lateinit var viewModel: JobSeekerDashboardViewModel
    private val interviews: MutableList<Application> = mutableListOf()
    private lateinit var interviewsAdapter: InterviewsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInterviewsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this).get(JobSeekerDashboardViewModel::class.java)

        binding.btnBack.setOnClickListener { finish() }

        binding.rvApplications.layoutManager = LinearLayoutManager(this)
        interviewsAdapter = InterviewsAdapter(interviews)
        binding.rvApplications.adapter = interviewsAdapter

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
        viewModel.interviewApplications.observe(this) { items ->
            interviews.clear()
            interviews.addAll(items)
            interviewsAdapter.notifyDataSetChanged()

            binding.tvCount.text = getString(R.string.interview_schedules_count, items.size)
            binding.tvEmpty.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    inner class InterviewsAdapter(private val items: List<Application>) :
        RecyclerView.Adapter<InterviewsAdapter.VH>() {

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
            holder.tvStatus.setTextColor(getColor(R.color.figmaPrimaryBtn))
            holder.itemView.setOnLongClickListener {
                withdrawApplication(item)
                true
            }
        }

        override fun getItemCount(): Int = items.size
    }
}

