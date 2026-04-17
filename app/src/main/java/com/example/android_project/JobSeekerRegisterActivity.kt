package com.example.android_project

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.android_project.common.model.UserRole
import com.example.android_project.common.navigation.RoleNavHostActivity
import com.example.android_project.common.session.JobSeekerProfile
import com.example.android_project.common.session.SessionManager
import com.example.android_project.common.session.UserProfileStore
import com.example.android_project.common.ui.disableSuggestions
import com.example.android_project.databinding.ActivityRegisterJobseekerBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class JobSeekerRegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterJobseekerBinding
    private var isPasswordVisible = false
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private lateinit var googleClient: GoogleSignInClient
    private val profileStore by lazy { UserProfileStore(this) }
    private val googleLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        handleGoogleResult(result.data)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterJobseekerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupInputHighlights(binding.etName, binding.etEmail, binding.etPassword)
        binding.etName.disableSuggestions()
        binding.etEmail.disableSuggestions()
        binding.etPassword.disableSuggestions()

        googleClient = GoogleSignIn.getClient(
            this,
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        )

        binding.btnBack.setOnClickListener { finish() }

        binding.ivTogglePassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            binding.etPassword.transformationMethod =
                if (isPasswordVisible) HideReturnsTransformationMethod.getInstance()
                else PasswordTransformationMethod.getInstance()
            binding.etPassword.setSelection(binding.etPassword.text.length)
        }

        binding.btnSignUp.setOnClickListener {
            registerJobSeeker()
        }

        binding.btnGoogle.setOnClickListener {
            startGoogleRegistration()
        }

        binding.btnFacebook.setOnClickListener {
            loginGuest()
        }

        binding.btnGoToLogin.setOnClickListener {
            startActivity(Intent(this, JobSeekerLoginActivity::class.java))
            finish()
        }
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
                        val isNewUser = authTask.result?.additionalUserInfo?.isNewUser == true
                        if (isNewUser) {
                            val profile = hashMapOf(
                                "uid" to uid,
                                "name" to (account.displayName ?: ""),
                                "email" to (account.email ?: ""),
                                "title" to "",
                                "location" to "",
                                "about" to "",
                                "photoUrl" to (account.photoUrl?.toString() ?: ""),
                                "role" to "JOB_SEEKER",
                                "createdAt" to System.currentTimeMillis()
                            )
                            firestore.collection("job_seekers").document(uid).set(profile)
                            profileStore.saveJobSeeker(
                                JobSeekerProfile(
                                    uid = uid,
                                    name = account.displayName ?: "",
                                    email = account.email ?: "",
                                    photoUrl = account.photoUrl?.toString() ?: ""
                                )
                            )
                            SessionManager(this).saveSession(UserRole.JOB_SEEKER)
                            startActivity(Intent(this, RoleNavHostActivity::class.java).apply {
                                putExtra(RoleNavHostActivity.EXTRA_ROLE, UserRole.JOB_SEEKER.name)
                            })
                            finish()
                        } else {
                            auth.signOut()
                            googleClient.signOut()
                            Toast.makeText(
                                this,
                                "Account already registered. Please use login.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            this,
                            authTask.exception?.localizedMessage ?: "Google sign-in failed",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    setLoading(false)
                }
        } catch (e: ApiException) {
            setLoading(false)
            Toast.makeText(this, "Google sign-in canceled", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startGoogleRegistration() {
        setLoading(true)
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
                }
                setLoading(false)
            }
    }


    private fun registerJobSeeker() {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        when {
            name.isEmpty() -> {
                Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show()
                return
            }
            email.isEmpty() -> {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
                return
            }
            password.length < 6 -> {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return
            }
        }

        setLoading(true)

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = task.result?.user?.uid ?: return@addOnCompleteListener
                    val profile = hashMapOf(
                        "uid" to uid,
                        "name" to name,
                        "email" to email,
                        "title" to "",
                        "location" to "",
                        "about" to "",
                        "photoUrl" to "",
                        "role" to "JOB_SEEKER",
                        "createdAt" to System.currentTimeMillis()
                    )
                    firestore.collection("job_seekers").document(uid)
                        .set(profile)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Account created", Toast.LENGTH_SHORT).show()
                            profileStore.saveJobSeeker(
                                JobSeekerProfile(
                                    uid = uid,
                                    name = name,
                                    email = email
                                )
                            )
                            startActivity(Intent(this, JobSeekerLoginActivity::class.java))
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Saved auth but failed to store profile: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                        }
                        .addOnCompleteListener {
                            setLoading(false)
                        }
                } else {
                    setLoading(false)
                    Toast.makeText(this, task.exception?.localizedMessage ?: "Registration failed", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun setLoading(loading: Boolean) {
        binding.btnSignUp.isEnabled = !loading
        binding.btnGoToLogin.isEnabled = !loading
        if (loading) showLoading() else hideLoading()
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
