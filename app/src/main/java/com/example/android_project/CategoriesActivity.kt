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
import com.example.android_project.databinding.ActivityCategoriesBinding
import com.example.android_project.jobseeker.presentation.JobSeekerDashboardViewModel

class CategoriesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCategoriesBinding
    private lateinit var viewModel: JobSeekerDashboardViewModel
    private val allJobs = mutableListOf<Job>()
    private lateinit var adapter: CategoryJobsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoriesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(JobSeekerDashboardViewModel::class.java)

        val categoryKey = intent.getStringExtra(EXTRA_CATEGORY_KEY) ?: "all"
        val categoryTitle = intent.getStringExtra(EXTRA_CATEGORY_TITLE) ?: "Category Jobs"
        val filteredJobs = filterJobsByCategory(categoryKey)

        binding.tvCategoryTitle.text = categoryTitle
        binding.btnBack.setOnClickListener { finish() }

        binding.rvCategoryJobs.layoutManager = LinearLayoutManager(this)
        adapter = CategoryJobsAdapter(filteredJobs)
        binding.rvCategoryJobs.adapter = adapter
        binding.tvEmpty.visibility = if (filteredJobs.isEmpty()) View.VISIBLE else View.GONE

        observeViewModel(categoryKey)
        viewModel.fetchRecommendedJobs()
    }

    private fun filterJobsByCategory(categoryKey: String): List<Job> {
        if (categoryKey == "all") {
            return allJobs
        }

        return allJobs.filter { job ->
            val title = job.title.lowercase()
            when (categoryKey) {
                "trending" -> true
                "design" -> title.contains("design") || title.contains("ux") || title.contains("ui")
                "ui_ux" -> title.contains("ux") || title.contains("ui") || title.contains("designer")
                "language" -> title.contains("translator") || title.contains("content") || title.contains("writer")
                "engineering" -> title.contains("engineer") || title.contains("developer") || title.contains("software")
                "data" -> title.contains("data") || title.contains("database") || title.contains("analyst")
                "mechanical" -> title.contains("mechanical") || title.contains("technician")
                else -> true
            }
        }
    }

    private fun observeViewModel(categoryKey: String) {
        viewModel.jobs.observe(this) { list ->
            allJobs.clear()
            allJobs.addAll(list)
            val filtered = filterJobsByCategory(categoryKey)
            adapter.data = filtered
            adapter.notifyDataSetChanged()
            binding.tvEmpty.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(this) { error ->
            if (error != null) {
                binding.tvEmpty.text = error
                binding.tvEmpty.visibility = View.VISIBLE
            }
        }
    }

    inner class CategoryJobsAdapter(var data: List<Job>) :
        RecyclerView.Adapter<CategoryJobsAdapter.VH>() {

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
            holder.tvSubtitle.text = "${item.companyName} · ${item.location} · ${item.salaryRange}"
        }

        override fun getItemCount(): Int = data.size
    }

    companion object {
        const val EXTRA_CATEGORY_KEY = "category_key"
        const val EXTRA_CATEGORY_TITLE = "category_title"
    }
}
