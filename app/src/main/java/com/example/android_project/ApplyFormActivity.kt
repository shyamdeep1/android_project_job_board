package com.example.android_project

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.android_project.databinding.ActivityApplyFormBinding
import com.example.android_project.jobseeker.presentation.JobSeekerDashboardViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage

class ApplyFormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityApplyFormBinding
    private lateinit var viewModel: JobSeekerDashboardViewModel
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val storage by lazy { FirebaseStorage.getInstance() }
    
    private var selectedResumeUri: Uri? = null
    private var uploadedResumeUrl: String? = null
    
    private val resumePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { 
            selectedResumeUri = it
            binding.tvResumeStatus.text = "Resume selected: ${getFileName(it)}"
            uploadResume(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityApplyFormBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        viewModel = ViewModelProvider(this).get(JobSeekerDashboardViewModel::class.java)

        val jobTitle    = intent.getStringExtra(EXTRA_JOB_TITLE)    ?: "Senior Software Engineer"
        val jobId       = intent.getStringExtra(EXTRA_JOB_ID)       ?: "job_${System.currentTimeMillis()}"
        val companyName = intent.getStringExtra(EXTRA_COMPANY_NAME) ?: "Highspeed Studios"

        binding.tvJobTitle.text    = jobTitle
        binding.tvCompanyName.text = companyName

        binding.btnBack.setOnClickListener { finish() }

        binding.btnUploadResume.setOnClickListener {
            resumePicker.launch("application/pdf")
        }

        binding.btnApplyJob.setOnClickListener {
            val name  = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val phone = binding.etPhone.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            if (!phone.matches(Regex("[0-9+\\-() ]{7,}"))) {
                Toast.makeText(this, "Please enter a valid phone number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            if (uploadedResumeUrl == null) {
                Toast.makeText(this, "Please upload a resume", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            viewModel.submitJobApplicationWithResume(jobId, name, email, phone, uploadedResumeUrl!!)
        }
        
        observeViewModel()
    }
    
    private fun uploadResume(uri: Uri) {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        showLoading()
        binding.btnUploadResume.isEnabled = false
        val fileName = "resumes/${uid}/${System.currentTimeMillis()}.pdf"
        val ref = storage.reference.child(fileName)

        ref.putFile(uri)
            .continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let { throw it }
                }
                ref.downloadUrl
            }
            .addOnSuccessListener { downloadUri ->
                uploadedResumeUrl = downloadUri.toString()
                binding.tvResumeStatus.text = "✓ Resume uploaded successfully"
                binding.btnUploadResume.isEnabled = true
                Toast.makeText(this, "Resume uploaded", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                uploadedResumeUrl = null
                binding.tvResumeStatus.text = "Resume upload failed"
                binding.btnUploadResume.isEnabled = true
                Toast.makeText(this, e.localizedMessage ?: "Resume upload failed", Toast.LENGTH_LONG).show()
            }
            .addOnCompleteListener { hideLoading() }
    }
    
    private fun getFileName(uri: Uri): String {
        return uri.lastPathSegment ?: "resume.pdf"
    }
    
    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { isLoading ->
            binding.btnApplyJob.isEnabled = !isLoading
            binding.btnApplyJob.text = if (isLoading) "Submitting..." else "Apply Now"
            if (isLoading) showLoading() else hideLoading()
        }
        
        viewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }
        
        viewModel.applicationSubmitted.observe(this) { submitted ->
            if (submitted) {
                Toast.makeText(this, "Application submitted successfully!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    override fun onDestroy() {
        hideLoading()
        super.onDestroy()
    }

    companion object {
        const val EXTRA_JOB_TITLE    = "extra_job_title"
        const val EXTRA_JOB_ID       = "extra_job_id"
        const val EXTRA_COMPANY_NAME = "extra_company_name"
    }
}
