package com.example.android_project.admin.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android_project.admin.data.AdminRepository
import com.example.android_project.admin.data.FirebaseAdminRepository
import com.example.android_project.common.model.AppUser
import com.example.android_project.common.model.Job
import kotlinx.coroutines.launch

class AdminDashboardViewModel(
    private val repository: AdminRepository = FirebaseAdminRepository()
) : ViewModel() {
    
    private val _users = MutableLiveData<List<AppUser>>()
    val users: LiveData<List<AppUser>> = _users
    
    private val _jobs = MutableLiveData<List<Job>>()
    val jobs: LiveData<List<Job>> = _jobs
    
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error
    
    fun fetchUsers() {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                val userList = repository.fetchUsers()
                _users.value = userList
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load users"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun fetchJobsForModeration() {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                val jobList = repository.fetchJobsForModeration()
                _jobs.value = jobList
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load jobs"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
