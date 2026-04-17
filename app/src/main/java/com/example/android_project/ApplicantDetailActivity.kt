package com.example.android_project

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.android_project.common.model.ApplicationStatus
import com.example.android_project.company.presentation.CompanyDashboardViewModel
import com.example.android_project.databinding.ActivityApplicantDetailBinding
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ApplicantDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityApplicantDetailBinding
    private lateinit var viewModel: CompanyDashboardViewModel
    private lateinit var applicantName: String
    private var applicationId: String? = null
    private var status: ApplicationStatus = ApplicationStatus.APPLIED
    private var applicantEmail: String = ""
    private var applicantPhone: String = ""
    private var applicantUid: String? = null
    private var resumeUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityApplicantDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this).get(CompanyDashboardViewModel::class.java)

        applicationId = intent.getStringExtra(EXTRA_APPLICATION_ID)
        applicantName = intent.getStringExtra(EXTRA_APPLICANT_NAME) ?: "Applicant"
        val appliedRole = intent.getStringExtra(EXTRA_APPLIED_ROLE) ?: "Applied"
        val incomingStatus = intent.getStringExtra(EXTRA_STATUS) ?: ApplicationStatus.APPLIED.label()
        applicantEmail = intent.getStringExtra(EXTRA_APPLICANT_EMAIL) ?: ""
        applicantPhone = intent.getStringExtra(EXTRA_APPLICANT_PHONE) ?: ""
        applicantUid = intent.getStringExtra(EXTRA_APPLICANT_UID)
        resumeUrl = intent.getStringExtra(EXTRA_RESUME_URL)?.trim().orEmpty().ifBlank { null }
        
        status = ApplicationStatus.fromLabel(incomingStatus)

        binding.tvApplicantName.text = applicantName
        binding.tvApplicantEmail.text = applicantEmail.ifBlank { "Email not available" }
        binding.tvApplicantPhone.text = applicantPhone.ifBlank { "Phone not available" }
        
        if (!applicantUid.isNullOrBlank()) {
            fetchApplicantDetailsFromFirebase(applicantUid!!)
        }
        
        renderStatus(appliedRole)

        binding.btnBack.setOnClickListener { finish() }

        binding.btnViewResume.isEnabled = !resumeUrl.isNullOrBlank()
        binding.btnViewResume.alpha = if (resumeUrl.isNullOrBlank()) 0.5f else 1f
        binding.btnViewResume.setOnClickListener {
            openResume()
        }

        binding.btnReject.setOnClickListener {
            updateStatus(appliedRole, ApplicationStatus.REJECTED)
        }

        binding.btnShortlist.setOnClickListener {
            val nextStatus = status.nextReviewStep()
            if (nextStatus == status || status.isTerminal()) {
                Toast.makeText(this, "No next review step available", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            updateStatus(appliedRole, nextStatus)
        }
    }

    private fun openResume() {
        val url = resumeUrl
        if (url.isNullOrBlank()) {
            Toast.makeText(this, "Resume not available", Toast.LENGTH_SHORT).show()
            return
        }

        val viewIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        try {
            startActivity(viewIntent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(this, "No app found to open resume", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun fetchApplicantDetailsFromFirebase(uid: String) {
        GlobalScope.launch {
            try {
                val db = FirebaseFirestore.getInstance()
                db.collection("job_seekers").document(uid).get()
                    .addOnSuccessListener { doc ->
                        if (doc.exists()) {
                            val name = doc.getString("name") ?: applicantName
                            val email = doc.getString("email") ?: applicantEmail
                            val phone = doc.getString("phone") ?: applicantPhone
                            
                            applicantName = name.ifBlank { applicantName }
                            applicantEmail = email.ifBlank { applicantEmail }
                            applicantPhone = phone.ifBlank { applicantPhone }
                            
                            binding.tvApplicantName.text = applicantName
                            binding.tvApplicantEmail.text = applicantEmail.ifBlank { "Email not available" }
                            binding.tvApplicantPhone.text = applicantPhone.ifBlank { "Phone not available" }
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this@ApplicantDetailActivity, "Failed to load applicant details", Toast.LENGTH_SHORT).show()
                    }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun updateStatus(appliedRole: String, nextStatus: ApplicationStatus) {
        val id = applicationId
        if (id.isNullOrBlank()) {
            Toast.makeText(this, "Application id missing", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.updateApplicationStatus(id, nextStatus.label()) { success ->
            if (!success) return@updateApplicationStatus

            status = nextStatus
            renderStatus(appliedRole)
            Toast.makeText(this, "$applicantName moved to ${status.label()}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun renderStatus(appliedRole: String) {
        binding.tvAppliedRole.text = "$appliedRole • ${status.label()}"
        binding.btnShortlist.text = when (status) {
            ApplicationStatus.APPLIED -> "MARK REVIEWED"
            ApplicationStatus.REVIEWED -> "MARK INTERVIEW"
            ApplicationStatus.INTERVIEW -> "MARK SELECTED"
            ApplicationStatus.SELECTED -> "SELECTED"
            ApplicationStatus.REJECTED -> "REJECTED"
            ApplicationStatus.WITHDRAWN -> "WITHDRAWN"
        }
        binding.btnShortlist.isEnabled = status == ApplicationStatus.APPLIED || status == ApplicationStatus.REVIEWED || status == ApplicationStatus.INTERVIEW
        binding.btnReject.isEnabled = !status.isTerminal()
    }

    companion object {
        const val EXTRA_APPLICATION_ID = "extra_application_id"
        const val EXTRA_APPLICANT_NAME = "extra_applicant_name"
        const val EXTRA_APPLICANT_EMAIL = "extra_applicant_email"
        const val EXTRA_APPLICANT_PHONE = "extra_applicant_phone"
        const val EXTRA_APPLICANT_UID = "extra_applicant_uid"
        const val EXTRA_APPLIED_ROLE = "extra_applied_role"
        const val EXTRA_STATUS = "extra_status"
        const val EXTRA_RESUME_URL = "extra_resume_url"
    }
}
