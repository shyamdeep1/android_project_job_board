package com.example.android_project

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.example.android_project.common.analytics.AppAnalyticsLogger
import com.example.android_project.common.model.UserRole
import com.example.android_project.common.navigation.RoleNavHostActivity
import com.example.android_project.common.session.SessionManager
import com.example.android_project.common.ui.disableSuggestions
import com.example.android_project.databinding.ActivityAdminLoginBinding

class AdminLoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminLoginBinding
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        AppAnalyticsLogger.logScreen(this, "AdminLogin")

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            v.updatePadding(top = statusBar.top)
            insets
        }

        setupInputHighlights(binding.etUsername, binding.etPassword)
        binding.etUsername.disableSuggestions()
        binding.etPassword.disableSuggestions()

        binding.btnBack.setOnClickListener { finish() }

        binding.ivTogglePassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            binding.etPassword.transformationMethod =
                if (isPasswordVisible) HideReturnsTransformationMethod.getInstance()
                else PasswordTransformationMethod.getInstance()
            binding.etPassword.setSelection(binding.etPassword.text.length)
        }

        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            if (username.isEmpty() || password.isEmpty()) {
                AppAnalyticsLogger.trackDropOff(this, "admin_login", "empty_credentials")
                Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show()
            } else {
                AppAnalyticsLogger.logFlowStep(this, "admin_login", "success")
                SessionManager(this).saveSession(UserRole.ADMIN)
                startActivity(Intent(this, RoleNavHostActivity::class.java).apply {
                    putExtra(RoleNavHostActivity.EXTRA_ROLE, UserRole.ADMIN.name)
                })
                finish()
            }
        }
    }

    private fun setupInputHighlights(vararg fields: EditText) {
        val activeRes = R.drawable.bg_input_active
        val normalRes = R.drawable.bg_input_normal
        fun applyHighlight(focused: EditText?) {
            fields.forEach { it.setBackgroundResource(if (it == focused) activeRes else normalRes) }
        }
        fields.forEach { field ->
            field.setOnFocusChangeListener { v, hasFocus ->
                applyHighlight(if (hasFocus) v as EditText else fields.firstOrNull { it.isFocused })
            }
            field.setOnClickListener { field.requestFocus() }
        }
        applyHighlight(fields.firstOrNull { it.isFocused } ?: fields.firstOrNull())
    }
}
