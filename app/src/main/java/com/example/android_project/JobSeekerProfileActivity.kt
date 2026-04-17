package com.example.android_project

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.android_project.common.session.JobSeekerProfile
import com.example.android_project.common.session.SessionManager
import com.example.android_project.common.session.UserProfileStore
import com.example.android_project.common.ui.loadProfilePhoto
import com.example.android_project.databinding.ActivityJobSeekerProfileBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage

class JobSeekerProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityJobSeekerProfileBinding
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val storage by lazy { FirebaseStorage.getInstance() }
    private val profileStore by lazy { UserProfileStore(this) }
    private var currentPhotoUrl: String = ""
    private val imagePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { uploadProfilePhoto(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJobSeekerProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }
        binding.btnSaveProfile.setOnClickListener { saveProfile() }
        binding.ivProfilePhoto.setOnClickListener { imagePicker.launch("image/*") }

        binding.btnLogout.setOnClickListener {
            SessionManager(this).clearSession()
            profileStore.clearJobSeeker()
            auth.signOut()
            GoogleSignIn.getClient(
                this,
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
            ).signOut()
            startActivity(Intent(this, WelcomeActivity::class.java))
            finishAffinity()
        }

        loadProfile()
    }

    private fun loadProfile() {
        val cached = profileStore.getJobSeeker()
        currentPhotoUrl = cached.photoUrl
        applyToFields(cached)
        binding.ivProfilePhoto.loadProfilePhoto(currentPhotoUrl, R.drawable.ic_jobseeker)

        val uid = auth.currentUser?.uid ?: return
        firestore.collection("job_seekers").document(uid).get()
            .addOnSuccessListener { snapshot ->
                val profile = JobSeekerProfile(
                    uid = uid,
                    name = snapshot.getString("name") ?: cached.name,
                    email = snapshot.getString("email") ?: cached.email,
                    title = snapshot.getString("title") ?: cached.title,
                    location = snapshot.getString("location") ?: cached.location,
                    about = snapshot.getString("about") ?: cached.about,
                    photoUrl = snapshot.getString("photoUrl") ?: cached.photoUrl
                )
                currentPhotoUrl = profile.photoUrl
                applyToFields(profile)
                binding.ivProfilePhoto.loadProfilePhoto(currentPhotoUrl, R.drawable.ic_jobseeker)
                profileStore.saveJobSeeker(profile)
            }
    }

    private fun uploadProfilePhoto(uri: Uri) {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        showLoading()
        val ref = storage.reference.child("profile_photos/job_seekers/$uid.jpg")
        ref.putFile(uri)
            .continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let { throw it }
                }
                ref.downloadUrl
            }
            .addOnSuccessListener { downloadUri ->
                val url = downloadUri.toString()
                currentPhotoUrl = url
                binding.ivProfilePhoto.loadProfilePhoto(url, R.drawable.ic_jobseeker)
                firestore.collection("job_seekers").document(uid)
                    .set(mapOf("photoUrl" to url), SetOptions.merge())
                profileStore.saveJobSeeker(profileStore.getJobSeeker().copy(uid = uid, photoUrl = url))
                Toast.makeText(this, "Profile photo updated", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, e.localizedMessage ?: "Photo upload failed", Toast.LENGTH_LONG).show()
            }
            .addOnCompleteListener { hideLoading() }
    }

    private fun applyToFields(profile: JobSeekerProfile) {
        if (profile.name.isNotBlank()) binding.etFullName.setText(profile.name)
        if (profile.email.isNotBlank()) binding.etEmail.setText(profile.email)
        if (profile.title.isNotBlank()) binding.etJobTitle.setText(profile.title)
        if (profile.location.isNotBlank()) binding.etLocation.setText(profile.location)
        if (profile.about.isNotBlank()) binding.etAbout.setText(profile.about)
    }

    private fun saveProfile() {
        val name = binding.etFullName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val title = binding.etJobTitle.text.toString().trim()
        val location = binding.etLocation.text.toString().trim()
        val about = binding.etAbout.text.toString().trim()

        if (name.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Please enter your name and email", Toast.LENGTH_SHORT).show()
            return
        }

        val uid = auth.currentUser?.uid
        if (uid == null) {
            Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val profile = JobSeekerProfile(
            uid = uid,
            name = name,
            email = email,
            title = title,
            location = location,
            about = about,
            photoUrl = currentPhotoUrl
        )

        firestore.collection("job_seekers").document(uid)
            .set(
                mapOf(
                    "uid" to uid,
                    "name" to name,
                    "email" to email,
                    "title" to title,
                    "location" to location,
                    "about" to about,
                    "photoUrl" to currentPhotoUrl,
                    "role" to "JOB_SEEKER"
                ),
                SetOptions.merge()
            )
            .addOnSuccessListener {
                profileStore.saveJobSeeker(profile)
                Toast.makeText(this, "Profile saved", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, e.localizedMessage ?: "Failed to save profile", Toast.LENGTH_LONG).show()
            }
    }
}
