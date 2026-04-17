package com.example.android_project

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.android_project.admin.presentation.AdminDashboardViewModel
import com.example.android_project.databinding.ActivityAdminDashboardBinding

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminDashboardBinding
    private lateinit var viewModel: AdminDashboardViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        viewModel = ViewModelProvider(this).get(AdminDashboardViewModel::class.java)

        binding.cardManageUsers.setOnClickListener {
            startActivity(Intent(this, ManageUsersActivity::class.java))
        }
        binding.cardModerateJobs.setOnClickListener {
            startActivity(Intent(this, ManageJobsActivity::class.java))
        }

        binding.navHome.setOnClickListener { }
        binding.navNotification.setOnClickListener {
            startActivity(Intent(this, BroadcastNotificationActivity::class.java))
        }
        binding.navMessages.setOnClickListener {
            startActivity(Intent(this, ManageUsersActivity::class.java))
        }
        binding.navAccount.setOnClickListener {
            startActivity(Intent(this, AdminProfileActivity::class.java))
        }
        
        observeViewModel()
        viewModel.fetchUsers()
        viewModel.fetchJobsForModeration()
    }
    
    private fun observeViewModel() {
        viewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }
        
        viewModel.users.observe(this) { users ->
            binding.tvUsersCount.text = users.size.toString()
        }
        
        viewModel.jobs.observe(this) { jobs ->
            binding.tvJobsCount.text = jobs.size.toString()
        }
    }
}
