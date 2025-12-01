package edu.uta.campussports.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

// Mock authentication for testing UI without Firebase
class MockAuthViewModel : ViewModel() {
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState
    
    private val _currentUser = MutableStateFlow<MockUser?>(null)
    val currentUser: StateFlow<MockUser?> = _currentUser
    
    fun signIn(email: String, password: String) {
        if (!isValidUTAEmail(email)) {
            _authState.value = AuthState.Error("Please use your UTA student email (@mavs.uta.edu)")
            return
        }
        
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                delay(1500) // Simulate network call
                
                // Mock successful login
                _currentUser.value = MockUser(email = email, uid = "mock-uid-123")
                _authState.value = AuthState.Authenticated
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Sign in failed")
            }
        }
    }
    
    fun signUp(email: String, password: String, confirmPassword: String) {
        if (!isValidUTAEmail(email)) {
            _authState.value = AuthState.Error("Please use your UTA student email (@mavs.uta.edu)")
            return
        }
        
        if (password != confirmPassword) {
            _authState.value = AuthState.Error("Passwords do not match")
            return
        }
        
        if (password.length < 6) {
            _authState.value = AuthState.Error("Password must be at least 6 characters")
            return
        }
        
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                delay(2000) // Simulate network call
                
                // Mock successful signup
                _currentUser.value = MockUser(email = email, uid = "mock-uid-456")
                _authState.value = AuthState.Authenticated
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Sign up failed")
            }
        }
    }
    
    fun signOut() {
        _currentUser.value = null
        _authState.value = AuthState.Unauthenticated
    }
    
    fun resetPassword(email: String) {
        if (!isValidUTAEmail(email)) {
            _authState.value = AuthState.Error("Please use your UTA student email (@mavs.uta.edu)")
            return
        }
        
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                delay(1000) // Simulate network call
                _authState.value = AuthState.Success("Password reset email sent to $email")
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Password reset failed")
            }
        }
    }
    
    private fun isValidUTAEmail(email: String): Boolean {
        return email.lowercase().endsWith("@mavs.uta.edu")
    }
    
    fun clearError() {
        if (_authState.value is AuthState.Error || _authState.value is AuthState.Success) {
            _authState.value = if (_currentUser.value != null) {
                AuthState.Authenticated
            } else {
                AuthState.Unauthenticated
            }
        }
    }
}

// Mock user class for testing
data class MockUser(
    val email: String,
    val uid: String
)

sealed class AuthState {
    object Loading : AuthState()
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    data class Error(val message: String) : AuthState()
    data class Success(val message: String) : AuthState()
}