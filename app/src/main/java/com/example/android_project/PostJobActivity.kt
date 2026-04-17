package com.example.android_project

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.android_project.common.model.Job
import com.example.android_project.company.presentation.CompanyDashboardViewModel
import com.example.android_project.databinding.ActivityPostJobBinding

class PostJobActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPostJobBinding
    private lateinit var viewModel: CompanyDashboardViewModel
    private var editingJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostJobBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        viewModel = ViewModelProvider(this).get(CompanyDashboardViewModel::class.java)

        binding.btnBack.setOnClickListener { finish() }

        editingJob = intent.getSerializableExtra(EXTRA_JOB) as? Job
        editingJob?.let { job ->
            binding.etJobTitle.setText(job.title)
            binding.etLocation.setText(job.location)
            binding.etSalary.setText(job.salaryRange)
            binding.etType.setText(job.type)
            binding.etDescription.setText(job.description)
            binding.btnPublishJob.text = "Update Job"
        }

        binding.btnSaveDraft.setOnClickListener {
            val title = binding.etJobTitle.text.toString().trim()
            if (title.isNotEmpty()) {
                Toast.makeText(this, "Draft saved: $title", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Please enter a job title to save", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnPublishJob.setOnClickListener {
            val title = binding.etJobTitle.text.toString().trim()
            val location = binding.etLocation.text.toString().trim()
            val salary = binding.etSalary.text.toString().trim()
            val type = binding.etType.text.toString().trim()
            val description = binding.etDescription.text.toString().trim()

            if (title.isEmpty() || location.isEmpty() || salary.isEmpty() || type.isEmpty() || description.isEmpty()) {
                Toast.makeText(this, "Please complete all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            val jobToSave = editingJob?.copy(
                title = title,
                location = location,
                salaryRange = salary,
                type = type,
                description = description
            )

            if (jobToSave != null) {
                viewModel.updateJob(jobToSave)
            } else {
                viewModel.postJob(title, location, salary, type, description)
            }
        }
        
        observeViewModel()
    }
    
    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { isLoading ->
            binding.btnPublishJob.isEnabled = !isLoading
            binding.btnPublishJob.text = if (isLoading) "Publishing..." else "Publish Job"
            if (isLoading) showLoading() else hideLoading()
        }
        
        viewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }
        
        viewModel.jobPosted.observe(this) { posted ->
            if (posted) {
                Toast.makeText(this, "Job saved successfully!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    companion object {
        const val EXTRA_JOB = "extra_job"
    }

    override fun onDestroy() {
        hideLoading()
        super.onDestroy()
    }
}
