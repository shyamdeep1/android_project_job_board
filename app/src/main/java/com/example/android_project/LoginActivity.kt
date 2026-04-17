package com.example.android_project

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.example.android_project.common.analytics.AppAnalyticsLogger
import com.example.android_project.common.model.UserRole
import com.example.android_project.common.navigation.RoleNavHostActivity
import com.example.android_project.common.session.CompanyProfile
import com.example.android_project.common.session.SessionManager
import com.example.android_project.common.session.UserProfileStore
import com.example.android_project.common.ui.disableSuggestions
import com.example.android_project.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private var isPasswordVisible = false
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val profileStore by lazy { UserProfileStore(this) }
    private lateinit var googleClient: GoogleSignInClient
    private val googleLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        handleGoogleResult(result.data)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        AppAnalyticsLogger.logScreen(this, "CompanyLogin")

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            v.updatePadding(top = statusBar.top)
            insets
        }

        googleClient = GoogleSignIn.getClient(
            this,
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        )

        binding.tvHeaderTitle.text = getString(R.string.company)

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
            loginCompany()
        }

        binding.tvResetHere.setOnClickListener {
            Toast.makeText(this, "Reset password", Toast.LENGTH_SHORT).show()
        }

        binding.btnGoogle.setOnClickListener {
            startGoogleLogin()
        }

        binding.btnFacebook.setOnClickListener {
            loginGuest()
        }

        binding.btnCreateAccount.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun loginCompany() {
        val email = binding.etUsername.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            AppAnalyticsLogger.trackDropOff(this, "company_login", "empty_credentials")
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            return
        }

        if (email.equals("admin", ignoreCase = true) && password == "admin123") {
            SessionManager(this).saveSession(UserRole.ADMIN)
            startActivity(Intent(this, AdminDashboardActivity::class.java))
            finish()
            return
        }

        setLoading(true)

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = task.result?.user?.uid
                    if (uid != null) {
                        fetchAndCacheCompany(uid) {
                            AppAnalyticsLogger.logFlowStep(this, "company_login", "success")
                            SessionManager(this).saveSession(UserRole.COMPANY)
                            startActivity(Intent(this, RoleNavHostActivity::class.java).apply {
                                putExtra(RoleNavHostActivity.EXTRA_ROLE, UserRole.COMPANY.name)
                            })
                            finish()
                        }
                    } else {
                        Toast.makeText(this, "Login failed: missing user id", Toast.LENGTH_LONG).show()
                        setLoading(false)
                    }
                } else {
                    val message = when (task.exception) {
                        is FirebaseAuthInvalidUserException, is FirebaseAuthInvalidCredentialsException -> "Invalid email or password"
                        else -> task.exception?.localizedMessage ?: "Login failed"
                    }
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                    setLoading(false)
                }
            }
    }

    private fun setLoading(loading: Boolean) {
        binding.btnLogin.isEnabled = !loading
        binding.btnCreateAccount.isEnabled = !loading
        if (loading) showLoading() else hideLoading()
    }

    private fun handleGoogleResult(data: Intent?) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = task.getResult(ApiException::class.java) ?: return
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            setLoading(true)
            auth.signInWithCredential(credential)
                .addOnCompleteListener { authTask ->
                    if (authTask.isSuccessful) {
                        val uid = authTask.result?.user?.uid ?: return@addOnCompleteListener
                        val isNew = authTask.result?.additionalUserInfo?.isNewUser == true
                        if (isNew) {
                            val profile = hashMapOf(
                                "uid" to uid,
                                "name" to (account.displayName ?: ""),
                                "email" to (account.email ?: ""),
                                "phone" to "",
                                "website" to "",
                                "about" to "",
                                "photoUrl" to (account.photoUrl?.toString() ?: ""),
                                "role" to "COMPANY",
                                "createdAt" to System.currentTimeMillis()
                            )
                            firestore.collection("companies").document(uid).set(profile)
                        }

                        fetchAndCacheCompany(uid, fallback = CompanyProfile(
                            uid = uid,
                            name = account.displayName ?: "",
                            email = account.email ?: "",
                            photoUrl = account.photoUrl?.toString() ?: ""
                        )) {
                            SessionManager(this).saveSession(UserRole.COMPANY)
                            startActivity(Intent(this, RoleNavHostActivity::class.java).apply {
                                putExtra(RoleNavHostActivity.EXTRA_ROLE, UserRole.COMPANY.name)
                            })
                            finish()
                        }
                    } else {
                        Toast.makeText(
                            this,
                            authTask.exception?.localizedMessage ?: "Google sign-in failed",
                            Toast.LENGTH_LONG
                        ).show()
                        setLoading(false)
                    }
                }
        } catch (e: ApiException) {
            Toast.makeText(this, "Google sign-in canceled", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startGoogleLogin() {
        // Clear any previously selected Google account so chooser is shown every time.
        googleClient.signOut().addOnCompleteListener {
            googleLauncher.launch(googleClient.signInIntent)
        }
    }

    private fun loginGuest() {
        setLoading(true)
        auth.signInAnonymously()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    SessionManager(this).saveSession(UserRole.GUEST)
                    startActivity(Intent(this, RoleNavHostActivity::class.java).apply {
                        putExtra(RoleNavHostActivity.EXTRA_ROLE, UserRole.GUEST.name)
                    })
                    finish()
                } else {
                    Toast.makeText(
                        this,
                        task.exception?.localizedMessage ?: "Guest sign-in failed",
                        Toast.LENGTH_LONG
                    ).show()
                    setLoading(false)
                }
            }
    }


    private fun fetchAndCacheCompany(
        uid: String,
        fallback: CompanyProfile? = null,
        onComplete: () -> Unit
    ) {
        firestore.collection("companies").document(uid).get()
            .addOnSuccessListener { snapshot ->
                val name = snapshot.getString("name") ?: fallback?.name ?: ""
                val email = snapshot.getString("email") ?: fallback?.email ?: ""
                val phone = snapshot.getString("phone") ?: ""
                val website = snapshot.getString("website") ?: ""
                val about = snapshot.getString("about") ?: ""
                val photoUrl = snapshot.getString("photoUrl") ?: fallback?.photoUrl ?: ""
                profileStore.saveCompany(
                    CompanyProfile(
                        uid = uid,
                        name = name,
                        email = email,
                        phone = phone,
                        website = website,
                        about = about,
                        photoUrl = photoUrl
                    )
                )
            }
            .addOnFailureListener {
                fallback?.let { profileStore.saveCompany(it) }
            }
            .addOnCompleteListener {
                setLoading(false)
                onComplete()
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

    override fun onDestroy() {
        hideLoading()
        super.onDestroy()
    }
}
