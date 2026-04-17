package com.example.android_project

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android_project.common.data.mock.MockDataProvider
import com.example.android_project.common.model.Job
import com.example.android_project.common.navigation.RoleNavHostActivity
import com.example.android_project.common.session.SessionManager
import com.example.android_project.databinding.ActivitySavedJobsBinding

class SavedJobsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySavedJobsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySavedJobsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rvSavedJobs.layoutManager = LinearLayoutManager(this)
        binding.rvSavedJobs.adapter = SavedJobsAdapter(MockDataProvider.jobsForModeration())

        binding.btnBack.setOnClickListener { finish() }
        binding.btnLogout.setOnClickListener { showLogoutConfirmation() }
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                SessionManager(this).clearSession()
                startActivity(Intent(this, RoleNavHostActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
                finish()
            }
            .setNegativeButton("No", null)
            .show()
    }

    inner class SavedJobsAdapter(private val jobs: List<Job>) :
        RecyclerView.Adapter<SavedJobsAdapter.VH>() {

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
            val item = jobs[position]
            holder.tvTitle.text = item.title
            holder.tvSubtitle.text = "${item.companyName} · ${item.location}"
        }

        override fun getItemCount(): Int = jobs.size
    }
}
