package com.example.android_project.common.data.mock

import com.example.android_project.common.model.AppUser
import com.example.android_project.common.model.Application
import com.example.android_project.common.model.Job
import com.example.android_project.common.model.UserRole

object MockDataProvider {

    private val userData = mutableListOf(
        AppUser("u1", "Henry Kanwil", "henry@mail.com", UserRole.JOB_SEEKER, true),
        AppUser("u2", "Highspeed Studios", "highspeedst@mail.com", UserRole.COMPANY, true),
        AppUser("u3", "Claudia Surrr", "claudia@mail.com", UserRole.JOB_SEEKER, true),
        AppUser("u4", "Lunar Djaja Corp.", "lunar@mail.com", UserRole.COMPANY, false)
    )

    private val jobData = mutableListOf(
        Job("j1", "Senior Software Engineer", "Highspeed Studios", "Jakarta", "$500 - $1,000", "Pending"),
        Job("j2", "Android Developer", "Lunar Djaja Corp.", "Bandung", "$700 - $1,200", "Pending"),
        Job("j3", "UI/UX Designer", "Darkseer Studios", "Medan", "$600 - $1,100", "Approved")
    )

    private val applicationData = mutableListOf(
        Application("a1", "Henry Kanwil", "Applied for Senior Software Engineer", "Applied", "2m ago"),
        Application("a2", "Claudia Surrr", "Applied for Senior Software Engineer", "Reviewed", "15m ago"),
        Application("a3", "David Mckanzie", "Applied for Senior Software Engineer", "Interview", "1h ago")
    )

    fun users(): List<AppUser> = userData.toList()

    fun jobsForModeration(): MutableList<Job> = jobData.toMutableList()

    fun applications(): List<Application> = applicationData.toList()

    fun addJob(job: Job) {
        jobData.add(0, job)
    }

    fun addApplication(application: Application) {
        applicationData.add(0, application)
    }
}
