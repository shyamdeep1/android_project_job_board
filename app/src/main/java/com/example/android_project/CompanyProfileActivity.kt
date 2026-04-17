package com.example.android_project

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.android_project.common.navigation.RoleNavHostActivity
import com.example.android_project.common.session.CompanyProfile
import com.example.android_project.common.session.SessionManager
import com.example.android_project.common.session.UserProfileStore
import com.example.android_project.common.ui.loadProfilePhoto
import com.example.android_project.databinding.ActivityCompanyProfileBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage

class CompanyProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCompanyProfileBinding
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val storage by lazy { FirebaseStorage.getInstance() }
    private val profileStore by lazy { UserProfileStore(this) }
    private var currentPhotoUrl: String = ""
    private val imagePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { uploadLogo(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCompanyProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }
        loadProfileData()

        binding.btnSaveProfile.setOnClickListener {
            saveProfile()
        }

        binding.ivCompanyLogo.setOnClickListener { imagePicker.launch("image/*") }

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
        profileStore.clearCompany()
        auth.signOut()
        GoogleSignIn.getClient(
            this,
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
        ).signOut()
        startActivity(Intent(this, RoleNavHostActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }

    private fun loadProfileData() {
        val cached = profileStore.getCompany()
        currentPhotoUrl = cached.photoUrl
        applyProfile(cached)
        binding.ivCompanyLogo.loadProfilePhoto(currentPhotoUrl, R.drawable.ic_company)

        val uid = auth.currentUser?.uid ?: return
        firestore.collection("companies").document(uid).get()
            .addOnSuccessListener { snapshot ->
                val profile = CompanyProfile(
                    uid = uid,
                    name = snapshot.getString("name") ?: cached.name,
                    email = snapshot.getString("email") ?: cached.email,
                    phone = snapshot.getString("phone") ?: cached.phone,
                    website = snapshot.getString("website") ?: cached.website,
                    about = snapshot.getString("about") ?: cached.about,
                    photoUrl = snapshot.getString("photoUrl") ?: cached.photoUrl
                )
                currentPhotoUrl = profile.photoUrl
                applyProfile(profile)
                binding.ivCompanyLogo.loadProfilePhoto(currentPhotoUrl, R.drawable.ic_company)
                profileStore.saveCompany(profile)
            }
    }

    private fun applyProfile(profile: CompanyProfile) {
        if (profile.name.isNotBlank()) binding.etCompanyName.setText(profile.name)
        if (profile.email.isNotBlank()) binding.etCompanyEmail.setText(profile.email)
        if (profile.phone.isNotBlank()) binding.etCompanyPhone.setText(profile.phone)
        if (profile.website.isNotBlank()) binding.etCompanyWebsite.setText(profile.website)
        if (profile.about.isNotBlank()) binding.etCompanyAbout.setText(profile.about)
    }

    private fun uploadLogo(uri: Uri) {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        showLoading()
        val ref = storage.reference.child("profile_photos/companies/$uid.jpg")
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
                binding.ivCompanyLogo.loadProfilePhoto(url, R.drawable.ic_company)
                firestore.collection("companies").document(uid)
                    .set(mapOf("photoUrl" to url), SetOptions.merge())
                profileStore.saveCompany(profileStore.getCompany().copy(uid = uid, photoUrl = url))
                Toast.makeText(this, "Company logo updated", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, e.localizedMessage ?: "Logo upload failed", Toast.LENGTH_LONG).show()
            }
            .addOnCompleteListener { hideLoading() }
    }

    private fun saveProfile() {
        val companyName = binding.etCompanyName.text.toString().trim()
        val email = binding.etCompanyEmail.text.toString().trim()
        val phone = binding.etCompanyPhone.text.toString().trim()
        val website = binding.etCompanyWebsite.text.toString().trim()
        val about = binding.etCompanyAbout.text.toString().trim()

        if (companyName.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Please complete name and email", Toast.LENGTH_SHORT).show()
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show()
            return
        }

        val uid = auth.currentUser?.uid
        if (uid == null) {
            Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val profile = CompanyProfile(
            uid = uid,
            name = companyName,
            email = email,
            phone = phone,
            website = website,
            about = about,
            photoUrl = currentPhotoUrl
        )

        firestore.collection("companies").document(uid)
            .set(
                mapOf(
                    "uid" to uid,
                    "name" to companyName,
                    "email" to email,
                    "phone" to phone,
                    "website" to website,
                    "about" to about,
                    "photoUrl" to currentPhotoUrl,
                    "role" to "COMPANY"
                ),
                SetOptions.merge()
            )
            .addOnSuccessListener {
                profileStore.saveCompany(profile)
                Toast.makeText(this, "Company profile updated", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, e.localizedMessage ?: "Failed to save profile", Toast.LENGTH_LONG).show()
            }
    }
}
