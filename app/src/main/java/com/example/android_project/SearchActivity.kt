package com.example.android_project

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.android_project.databinding.ActivitySearchBinding

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.btnClearSearch.visibility =
                    if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
                val count = if (s.isNullOrEmpty()) 0 else 45
                binding.tvResultCount.text = "$count Job founded"
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.btnClearSearch.setOnClickListener {
            binding.etSearch.setText("")
            binding.btnClearSearch.visibility = View.GONE
        }

        binding.btnFilter.setOnClickListener {
            Toast.makeText(this, "Filter options", Toast.LENGTH_SHORT).show()
        }

        binding.chipFulltime.setOnClickListener {
            binding.chipFulltime.visibility = View.GONE
        }
        binding.chipLondon.setOnClickListener {
            binding.chipLondon.visibility = View.GONE
        }
        binding.chipRemote.setOnClickListener {
            binding.chipRemote.visibility = View.GONE
        }
        binding.chipHourly.setOnClickListener {
            binding.chipHourly.visibility = View.GONE
        }

        binding.searchJobItem1.setOnClickListener {
            openJobDetail("Senior Software Engineer", "amazon", "$500 - $1,000/monthly", "Seattle, Washington, United States")
        }
        binding.searchJobItem2.setOnClickListener {
            openJobDetail("Database Engineer", "google", "$500 - $1,000/monthly", "London, United Kingdom")
        }
        binding.searchJobItem3.setOnClickListener {
            openJobDetail("Junior Software Engineer", "Highspeed Studios", "$500 - $1,000/monthly", "Jakarta, Indonesia")
        }
    }

    private fun openJobDetail(title: String, company: String, salary: String, location: String) {
        val intent = Intent(this, JobDetailActivity::class.java).apply {
            putExtra(JobDetailActivity.EXTRA_JOB_TITLE,    title)
            putExtra(JobDetailActivity.EXTRA_COMPANY_NAME, company)
            putExtra(JobDetailActivity.EXTRA_SALARY,       salary)
            putExtra(JobDetailActivity.EXTRA_LOCATION,     location)
        }
        startActivity(intent)
    }
}
