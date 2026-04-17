package com.example.android_project.company.presentation

import android.app.Application as AndroidApp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.android_project.common.model.Application
import com.example.android_project.common.model.Job
import com.example.android_project.company.data.CompanyRepository
import com.example.android_project.company.data.FirebaseCompanyRepository
import com.example.android_project.common.session.UserProfileStore
import kotlinx.coroutines.launch

class CompanyDashboardViewModel(application: AndroidApp) : AndroidViewModel(application) {

    private val repository: CompanyRepository

    init {
        val profileStore = UserProfileStore(application)
        val companyName = profileStore.getCompany().name.ifBlank { "Highspeed Studios" }
        repository = FirebaseCompanyRepository(context = application, companyName = companyName)
    }
    
    private val _applications = MutableLiveData<List<Application>>()
    val applications: LiveData<List<Application>> = _applications

    private val _newApplicants = MutableLiveData<List<Application>>()
    val newApplicants: LiveData<List<Application>> = _newApplicants

    private val _jobApplicants = MutableLiveData<List<Application>>()
    val jobApplicants: LiveData<List<Application>> = _jobApplicants
    
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error
    
    private val _jobPosted = MutableLiveData(false)
    val jobPosted: LiveData<Boolean> = _jobPosted

    private val _companyJobs = MutableLiveData<List<Job>>()
    val companyJobs: LiveData<List<Job>> = _companyJobs
    
    fun fetchRecentApplications() {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                val appList = repository.fetchRecentApplications()
                _applications.value = appList
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load applications"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchNewApplicants() {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                _newApplicants.value = repository.fetchNewApplicants()
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load new applicants"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchApplicationsForJob(jobId: String) {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                _jobApplicants.value = repository.fetchApplicationsForJob(jobId)
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load job applicants"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateApplicationStatus(applicationId: String, status: String, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val success = repository.updateApplicationStatus(applicationId, status).isSuccess
            if (!success) {
                _error.value = "Failed to update application status"
            }
            fetchRecentApplications()
            fetchNewApplicants()
            onComplete(success)
        }
    }

    fun loadCompanyJobs() {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                _companyJobs.value = repository.getCompanyJobs()
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load jobs"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun postJob(
        title: String,
        location: String,
        salary: String,
        type: String,
        description: String
    ) {
        _isLoading.value = true
        _error.value = null
        _jobPosted.value = false
        viewModelScope.launch {
            try {
                val result = repository.postJob(title, location, salary, type, description)
                result.onSuccess {
                    _jobPosted.value = true
                    loadCompanyJobs()
                }
                result.onFailure { exception ->
                    _error.value = exception.message ?: "Failed to post job"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to post job"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateJob(job: Job) {
        _isLoading.value = true
        _error.value = null
        _jobPosted.value = false
        viewModelScope.launch {
            try {
                val result = repository.updateJob(job)
                result.onSuccess {
                    _jobPosted.value = true
                    loadCompanyJobs()
                }
                result.onFailure { exception ->
                    _error.value = exception.message ?: "Failed to update job"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to update job"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteJob(jobId: String) {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                val result = repository.deleteJob(jobId)
                result.onSuccess {
                    loadCompanyJobs()
                }
                result.onFailure { exception ->
                    _error.value = exception.message ?: "Failed to delete job"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to delete job"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
