package com.example.android_project.auth.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android_project.auth.data.AuthRepository
import com.example.android_project.auth.data.FirebaseAuthRepository
import com.example.android_project.common.model.UserRole
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: AuthRepository = FirebaseAuthRepository()
) : ViewModel() {
    
    private val _role = MutableLiveData<UserRole?>(null)
    val role: LiveData<UserRole?> = _role
    
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error
    
    fun signIn(username: String, password: String) {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                val result = repository.signIn(username, password)
                result.onSuccess { userRole ->
                    _role.value = userRole
                }
                result.onFailure { exception ->
                    _error.value = exception.message ?: "Sign in failed"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Sign in failed"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
