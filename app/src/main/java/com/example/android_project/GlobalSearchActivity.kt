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
import com.example.android_project.databinding.ActivityGlobalSearchBinding
import com.example.android_project.jobseeker.presentation.JobSeekerDashboardViewModel

class GlobalSearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGlobalSearchBinding
    private lateinit var viewModel: JobSeekerDashboardViewModel
    private lateinit var emptyView: TextView
    private val items = mutableListOf<Job>()
    private lateinit var adapter: SearchAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGlobalSearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        emptyView = findViewById(R.id.tvEmpty)

        viewModel = ViewModelProvider(this).get(JobSeekerDashboardViewModel::class.java)

        binding.rvSearchResults.layoutManager = LinearLayoutManager(this)
        adapter = SearchAdapter(items)
        binding.rvSearchResults.adapter = adapter

        binding.btnBack.setOnClickListener { finish() }

        observeViewModel()
        viewModel.fetchRecommendedJobs()
    }

    private fun observeViewModel() {
        viewModel.jobs.observe(this) { list ->
            items.clear()
            items.addAll(list)
            adapter.notifyDataSetChanged()
            emptyView.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(this) { error ->
            if (error != null) {
                emptyView.text = error
                emptyView.visibility = View.VISIBLE
            }
        }
    }

    inner class SearchAdapter(private val data: List<Job>) :
        RecyclerView.Adapter<SearchAdapter.VH>() {

        inner class VH(view: View) : RecyclerView.ViewHolder(view) {
            val tvTitle: TextView = view.findViewById(R.id.tvTitle)
            val tvSubtitle: TextView = view.findViewById(R.id.tvSubtitle)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_global_search_result, parent, false)
            return VH(view)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val item = data[position]
            holder.tvTitle.text = item.title
            holder.tvSubtitle.text = getString(
                R.string.global_search_company_location,
                item.companyName,
                item.location
            )
        }

        override fun getItemCount(): Int = data.size
    }
}
