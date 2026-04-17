package com.example.android_project.jobseeker.presentation

import android.app.Application as AndroidApplication
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.android_project.common.model.Application as JobApplication
import com.example.android_project.common.model.ApplicationStatus
import com.example.android_project.common.model.Job
import com.example.android_project.jobseeker.data.JobSeekerRepository
import com.example.android_project.jobseeker.data.FirebaseJobSeekerRepository
import kotlinx.coroutines.launch

class JobSeekerDashboardViewModel(application: AndroidApplication) : AndroidViewModel(application) {

    private val repository: JobSeekerRepository = FirebaseJobSeekerRepository(application)
    
    private val _jobs = MutableLiveData<List<Job>>()
    val jobs: LiveData<List<Job>> = _jobs

    private val _myApplications = MutableLiveData<List<JobApplication>>(emptyList())
    val myApplications: LiveData<List<JobApplication>> = _myApplications

    private val _interviewApplications = MutableLiveData<List<JobApplication>>(emptyList())
    val interviewApplications: LiveData<List<JobApplication>> = _interviewApplications

    private val _jobsAppliedCount = MutableLiveData(0)
    val jobsAppliedCount: LiveData<Int> = _jobsAppliedCount

    private val _interviewsCount = MutableLiveData(0)
    val interviewsCount: LiveData<Int> = _interviewsCount
    
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error
    
    private val _applicationSubmitted = MutableLiveData(false)
    val applicationSubmitted: LiveData<Boolean> = _applicationSubmitted
    
    fun fetchRecommendedJobs() {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                val jobList = repository.fetchRecommendedJobs()
                _jobs.value = jobList
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load jobs"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun submitJobApplication(jobId: String, name: String, email: String, phone: String) {
        _isLoading.value = true
        _error.value = null
        _applicationSubmitted.value = false
        viewModelScope.launch {
            try {
                val result = repository.submitJobApplication(jobId, name, email, phone)
                result.onSuccess {
                    _applicationSubmitted.value = true
                    fetchMyApplications()
                }
                result.onFailure { exception ->
                    _error.value = exception.message ?: "Failed to submit application"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to submit application"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun submitJobApplicationWithResume(jobId: String, name: String, email: String, phone: String, resumeUrl: String) {
        _isLoading.value = true
        _error.value = null
        _applicationSubmitted.value = false
        viewModelScope.launch {
            try {
                val result = repository.submitJobApplicationWithResume(jobId, name, email, phone, resumeUrl)
                result.onSuccess {
                    _applicationSubmitted.value = true
                    fetchMyApplications()
                }
                result.onFailure { exception ->
                    _error.value = exception.message ?: "Failed to submit application"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to submit application"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchMyApplications() {
        _error.value = null
        viewModelScope.launch {
            try {
                val applications = repository.fetchMyApplications()
                _myApplications.value = applications

                val interviews = applications.filter {
                    it.status.equals(ApplicationStatus.INTERVIEW.label(), ignoreCase = true)
                }
                _interviewApplications.value = interviews

                _jobsAppliedCount.value = applications.count { !it.status.equals(ApplicationStatus.WITHDRAWN.label(), ignoreCase = true) }
                _interviewsCount.value = interviews.size
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load your applications"
            }
        }
    }

    fun withdrawApplication(applicationId: String, onComplete: (Boolean) -> Unit = {}) {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                val success = repository.withdrawApplication(applicationId).isSuccess
                if (success) {
                    fetchMyApplications()
                } else {
                    _error.value = "Failed to withdraw application"
                }
                onComplete(success)
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to withdraw application"
                onComplete(false)
            } finally {
                _isLoading.value = false
            }
        }
    }
}
