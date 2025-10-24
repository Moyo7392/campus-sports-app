package edu.uta.campussports.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import edu.uta.campussports.data.UserProfile

class FirebaseAuthViewModel : ViewModel() {
    
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    
    private val _authState = MutableStateFlow<AuthState>(
        if (auth.currentUser != null) AuthState.Authenticated else AuthState.Unauthenticated
    )
    val authState: StateFlow<AuthState> = _authState
    
    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser
    
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile
    
    fun signIn(email: String, password: String) {
        if (!isValidUTAEmail(email)) {
            _authState.value = AuthState.Error("Please use your UTA student email (@mavs.uta.edu)")
            return
        }
        
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            _currentUser.value = user
                            if (user != null) {
                                loadUserProfile(user.uid)
                            }
                            _authState.value = AuthState.Authenticated
                        } else {
                            val errorMessage = task.exception?.message ?: "Sign in failed"
                            _authState.value = AuthState.Error(errorMessage)
                        }
                    }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Sign in failed")
            }
        }
    }
    
    fun signUp(
        email: String, 
        password: String, 
        confirmPassword: String, 
        fullName: String,
        major: String,
        year: String,
        favoriteSports: List<String>,
        skillLevel: String,
        bio: String
    ) {
        if (!isValidUTAEmail(email)) {
            _authState.value = AuthState.Error("Please use your UTA student email (@mavs.uta.edu)")
            return
        }
        
        if (fullName.trim().isEmpty()) {
            _authState.value = AuthState.Error("Please enter your full name")
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
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            if (user != null) {
                                // Save user profile to Firestore
                                val userProfile = UserProfile(
                                    uid = user.uid,
                                    fullName = fullName.trim(),
                                    email = email,
                                    major = major,
                                    year = year,
                                    favoritesSports = favoriteSports,
                                    skillLevel = skillLevel,
                                    bio = bio.trim(),
                                    createdAt = com.google.firebase.Timestamp.now()
                                )
                                
                                firestore.collection("users")
                                    .document(user.uid)
                                    .set(userProfile)
                                    .addOnSuccessListener {
                                        println("✅ User profile saved successfully for UID: ${user.uid}")
                                        _currentUser.value = user
                                        loadUserProfile(user.uid)
                                        _authState.value = AuthState.Authenticated
                                    }
                                    .addOnFailureListener { exception ->
                                        println("❌ Failed to save profile: ${exception.message}")
                                        println("❌ Error code: ${exception::class.simpleName}")
                                        println("❌ User UID: ${user.uid}")
                                        println("❌ Full error: ${exception.stackTraceToString()}")

                                        // Provide helpful error message based on error type
                                        val errorMsg = when {
                                            exception.message?.contains("PERMISSION_DENIED", ignoreCase = true) == true ->
                                                "Permission denied. Please check Firestore security rules are set up correctly."
                                            exception.message?.contains("UNAUTHENTICATED", ignoreCase = true) == true ->
                                                "Authentication error. Please try signing in again."
                                            else -> "Failed to create profile: ${exception.message}"
                                        }
                                        _authState.value = AuthState.Error(errorMsg)
                                    }
                            }
                        } else {
                            val errorMessage = task.exception?.message ?: "Sign up failed"
                            _authState.value = AuthState.Error(errorMessage)
                        }
                    }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Sign up failed")
            }
        }
    }
    
    fun signOut() {
        auth.signOut()
        _currentUser.value = null
        _userProfile.value = null
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
                auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            _authState.value = AuthState.Success("Password reset email sent to $email")
                        } else {
                            val errorMessage = task.exception?.message ?: "Password reset failed"
                            _authState.value = AuthState.Error(errorMessage)
                        }
                    }
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
    
    private fun loadUserProfile(uid: String) {
        firestore.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val profile = document.toObject(UserProfile::class.java)
                    if (profile != null) {
                        _userProfile.value = profile.copy(uid = uid)
                    }
                }
            }
            .addOnFailureListener {
                // Handle error silently for now
            }
    }
    
    init {
        // Load user profile on initialization if user is already signed in
        auth.currentUser?.let { user ->
            loadUserProfile(user.uid)
        }
    }
}


