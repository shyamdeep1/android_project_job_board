package com.example.android_project

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.android_project.databinding.ActivityCompanyDetailBinding

class CompanyDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCompanyDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCompanyDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val companyName = intent.getStringExtra(EXTRA_COMPANY_NAME) ?: "Highspeed Studios"
        val phone       = intent.getStringExtra(EXTRA_PHONE)        ?: "+51 632 445 556"
        val email       = intent.getStringExtra(EXTRA_EMAIL)        ?: "highspeedst@mail.com"

        binding.tvCompanyName.text = companyName
        binding.tvPhone.text       = phone
        binding.tvEmail.text       = email

        binding.btnBack.setOnClickListener { finish() }

        binding.btnOptions.setOnClickListener {
            Toast.makeText(this, "Options", Toast.LENGTH_SHORT).show()
        }

        binding.btnAvailableJobs.setOnClickListener {
            Toast.makeText(this, "Showing available jobs at $companyName", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        const val EXTRA_COMPANY_NAME = "extra_company_name"
        const val EXTRA_PHONE        = "extra_phone"
        const val EXTRA_EMAIL        = "extra_email"
    }
}
