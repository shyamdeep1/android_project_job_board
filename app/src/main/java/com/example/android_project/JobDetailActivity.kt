package com.example.android_project

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.android_project.databinding.ActivityJobDetailBinding

class JobDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityJobDetailBinding
    private var isBookmarked = false
    
    companion object {
        const val EXTRA_JOB_ID       = "extra_job_id"
        const val EXTRA_JOB_TITLE    = "extra_job_title"
        const val EXTRA_COMPANY_NAME = "extra_company_name"
        const val EXTRA_SALARY       = "extra_salary"
        const val EXTRA_LOCATION     = "extra_location"
        const val SAVED_BOOKMARK_STATE = "saved_bookmark_state"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJobDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState != null) {
            isBookmarked = savedInstanceState.getBoolean(SAVED_BOOKMARK_STATE, false)
        }

        val jobTitle    = intent.getStringExtra(EXTRA_JOB_TITLE)    ?: "Senior Software Engineer"
        val jobId       = intent.getStringExtra(EXTRA_JOB_ID)       ?: "job_${System.currentTimeMillis()}"
        val companyName = intent.getStringExtra(EXTRA_COMPANY_NAME) ?: "Highspeed Studios"
        val salary      = intent.getStringExtra(EXTRA_SALARY)       ?: "$500 - $1,000/monthly"
        val location    = intent.getStringExtra(EXTRA_LOCATION)     ?: "Medan, Indonesia"

        binding.tvJobTitle.text    = jobTitle
        binding.tvCompanyName.text = companyName
        binding.tvSalary.text      = salary
        binding.tvLocation.text    = location

        binding.btnBack.setOnClickListener { finish() }

        binding.btnBookmark.setOnClickListener { toggleBookmark() }

        binding.btnBookmarkBottom.setOnClickListener { toggleBookmark() }

        binding.btnApplyJob.setOnClickListener {
            val intent = Intent(this, ApplyFormActivity::class.java).apply {
                putExtra(ApplyFormActivity.EXTRA_JOB_TITLE, jobTitle)
                putExtra(ApplyFormActivity.EXTRA_JOB_ID, jobId)
                putExtra(ApplyFormActivity.EXTRA_COMPANY_NAME, companyName)
            }
            startActivity(intent)
        }
        
        updateBookmarkIcon(showToast = false)
    }
    
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(SAVED_BOOKMARK_STATE, isBookmarked)
    }

    private fun toggleBookmark() {
        isBookmarked = !isBookmarked
        updateBookmarkIcon()
    }
    
    private fun updateBookmarkIcon(showToast: Boolean = true) {
        val icon = if (isBookmarked) R.drawable.ic_bookmark_filled else R.drawable.ic_bookmark_outline
        binding.btnBookmark.setImageResource(icon)
        binding.btnBookmarkBottom.setImageResource(icon)
        if (showToast) {
            val msg = if (isBookmarked) "Job saved!" else "Job removed from saved"
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }
    }
}
