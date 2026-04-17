package com.example.android_project

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android_project.common.model.Job
import com.example.android_project.company.presentation.CompanyDashboardViewModel
import com.example.android_project.databinding.ActivityActiveJobsBinding

class ActiveJobsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityActiveJobsBinding
    private lateinit var viewModel: CompanyDashboardViewModel
    private val jobs: MutableList<Job> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityActiveJobsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[CompanyDashboardViewModel::class.java]

        binding.btnBack.setOnClickListener { finish() }

        binding.rvJobs.layoutManager = LinearLayoutManager(this)
        binding.rvJobs.adapter = ActiveJobsAdapter(jobs)

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
            jobs.addAll(list.filter { !it.status.equals("Rejected", ignoreCase = true) })
            binding.tvCount.text = "${jobs.size} active jobs"
            binding.tvEmpty.visibility = if (jobs.isEmpty()) View.VISIBLE else View.GONE
            binding.rvJobs.adapter?.notifyDataSetChanged()
        }
    }

    private inner class ActiveJobsAdapter(private val items: List<Job>) :
        RecyclerView.Adapter<ActiveJobsAdapter.VH>() {

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
            val item = items[position]
            holder.tvJobTitle.text = item.title
            holder.tvCompany.text = "${item.companyName} · ${item.location}"
            holder.tvStatus.text = item.status
            holder.tvStatus.setTextColor(
                if (item.status.equals("Approved", ignoreCase = true)) getColor(R.color.figmaPrimaryBtn)
                else getColor(R.color.statusBadgeGrey)
            )

            holder.btnApprove.visibility = View.GONE
            holder.btnReject.visibility = View.GONE
            holder.btnRemove.visibility = View.GONE
        }

        override fun getItemCount(): Int = items.size
    }
}
