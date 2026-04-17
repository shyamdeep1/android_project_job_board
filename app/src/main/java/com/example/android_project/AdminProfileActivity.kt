package com.example.android_project

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.android_project.common.navigation.RoleNavHostActivity
import com.example.android_project.common.session.SessionManager
import com.example.android_project.databinding.ActivityAdminProfileBinding

class AdminProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminProfileBinding
    private val prefs by lazy { getSharedPreferences("admin_profile", MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }
        loadProfileData()

        binding.btnSaveAdminProfile.setOnClickListener {
            val name = binding.etAdminName.text.toString().trim()
            val email = binding.etAdminEmail.text.toString().trim()
            val phone = binding.etAdminPhone.text.toString().trim()
            val password = binding.etAdminPassword.text.toString().trim()
            val settings = binding.etAdminSettings.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty() || settings.isEmpty()) {
                Toast.makeText(this, "Please complete all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            saveProfileData(name, email, phone, settings)
            Toast.makeText(this, "Admin profile updated", Toast.LENGTH_SHORT).show()
        }
        
        binding.btnLogout.setOnClickListener {
            showLogoutConfirmation()
        }
    }
    
    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                logout()
            }
            .setNegativeButton("No", null)
            .show()
    }
    
    private fun logout() {
        SessionManager(this).clearSession()
        startActivity(Intent(this, RoleNavHostActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }

    private fun loadProfileData() {
        val name = prefs.getString("name", null)
        val email = prefs.getString("email", null)
        val phone = prefs.getString("phone", null)
        val settings = prefs.getString("settings", null)

        if (name != null) binding.etAdminName.setText(name)
        if (email != null) binding.etAdminEmail.setText(email)
        if (phone != null) binding.etAdminPhone.setText(phone)
        if (settings != null) binding.etAdminSettings.setText(settings)
    }

    private fun saveProfileData(
        name: String,
        email: String,
        phone: String,
        settings: String
    ) {
        prefs.edit()
            .putString("name", name)
            .putString("email", email)
            .putString("phone", phone)
            .putString("settings", settings)
            .apply()
    }
}
