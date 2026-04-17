package com.example.android_project.common.session

import android.content.Context

data class JobSeekerProfile(
    val uid: String? = null,
    val name: String = "",
    val email: String = "",
    val title: String = "",
    val location: String = "",
    val about: String = "",
    val photoUrl: String = ""
)

data class CompanyProfile(
    val uid: String? = null,
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val website: String = "",
    val about: String = "",
    val photoUrl: String = ""
)

class UserProfileStore(context: Context) {

    private val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun saveJobSeeker(profile: JobSeekerProfile) {
        prefs.edit()
            .putString(KEY_UID, profile.uid)
            .putString(KEY_NAME, profile.name)
            .putString(KEY_EMAIL, profile.email)
            .putString(KEY_TITLE, profile.title)
            .putString(KEY_LOCATION, profile.location)
            .putString(KEY_ABOUT, profile.about)
            .putString(KEY_PHOTO_URL, profile.photoUrl)
            .apply()
    }

    fun getJobSeeker(): JobSeekerProfile {
        return JobSeekerProfile(
            uid = prefs.getString(KEY_UID, null),
            name = prefs.getString(KEY_NAME, "") ?: "",
            email = prefs.getString(KEY_EMAIL, "") ?: "",
            title = prefs.getString(KEY_TITLE, "") ?: "",
            location = prefs.getString(KEY_LOCATION, "") ?: "",
            about = prefs.getString(KEY_ABOUT, "") ?: "",
            photoUrl = prefs.getString(KEY_PHOTO_URL, "") ?: ""
        )
    }

    fun clearJobSeeker() {
        prefs.edit()
            .remove(KEY_UID)
            .remove(KEY_NAME)
            .remove(KEY_EMAIL)
            .remove(KEY_TITLE)
            .remove(KEY_LOCATION)
            .remove(KEY_ABOUT)
            .remove(KEY_PHOTO_URL)
            .apply()
    }

    fun saveCompany(profile: CompanyProfile) {
        prefs.edit()
            .putString(KEY_COMPANY_UID, profile.uid)
            .putString(KEY_COMPANY_NAME, profile.name)
            .putString(KEY_COMPANY_EMAIL, profile.email)
            .putString(KEY_COMPANY_PHONE, profile.phone)
            .putString(KEY_COMPANY_WEBSITE, profile.website)
            .putString(KEY_COMPANY_ABOUT, profile.about)
            .putString(KEY_COMPANY_PHOTO_URL, profile.photoUrl)
            .apply()
    }

    fun getCompany(): CompanyProfile {
        return CompanyProfile(
            uid = prefs.getString(KEY_COMPANY_UID, null),
            name = prefs.getString(KEY_COMPANY_NAME, "") ?: "",
            email = prefs.getString(KEY_COMPANY_EMAIL, "") ?: "",
            phone = prefs.getString(KEY_COMPANY_PHONE, "") ?: "",
            website = prefs.getString(KEY_COMPANY_WEBSITE, "") ?: "",
            about = prefs.getString(KEY_COMPANY_ABOUT, "") ?: "",
            photoUrl = prefs.getString(KEY_COMPANY_PHOTO_URL, "") ?: ""
        )
    }

    fun clearCompany() {
        prefs.edit()
            .remove(KEY_COMPANY_UID)
            .remove(KEY_COMPANY_NAME)
            .remove(KEY_COMPANY_EMAIL)
            .remove(KEY_COMPANY_PHONE)
            .remove(KEY_COMPANY_WEBSITE)
            .remove(KEY_COMPANY_ABOUT)
            .remove(KEY_COMPANY_PHOTO_URL)
            .apply()
    }

    companion object {
        private const val PREF_NAME = "user_profile_store"
        private const val KEY_UID = "jobseeker_uid"
        private const val KEY_NAME = "jobseeker_name"
        private const val KEY_EMAIL = "jobseeker_email"
        private const val KEY_TITLE = "jobseeker_title"
        private const val KEY_LOCATION = "jobseeker_location"
        private const val KEY_ABOUT = "jobseeker_about"
        private const val KEY_PHOTO_URL = "jobseeker_photo_url"

        private const val KEY_COMPANY_UID = "company_uid"
        private const val KEY_COMPANY_NAME = "company_name"
        private const val KEY_COMPANY_EMAIL = "company_email"
        private const val KEY_COMPANY_PHONE = "company_phone"
        private const val KEY_COMPANY_WEBSITE = "company_website"
        private const val KEY_COMPANY_ABOUT = "company_about"
        private const val KEY_COMPANY_PHOTO_URL = "company_photo_url"
    }
}
