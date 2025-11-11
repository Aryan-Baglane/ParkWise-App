package com.example.parkwise.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.parkwise.data.repository.AuthRepository
import com.example.parkwise.data.repository.ProfileRepository
import com.example.parkwise.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val email: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {

    private val authRepository = AuthRepository()
    private val profileRepository = ProfileRepository()
    private val firebaseAuth = FirebaseAuth.getInstance()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _isUserAuthenticated = MutableStateFlow(false)
    val isUserAuthenticated: StateFlow<Boolean> = _isUserAuthenticated.asStateFlow()

    private val defaultPhotoUrl = "https://example.com/default_profile.png" // Default profile photo

    private val _userProfile = MutableStateFlow(
        UserProfile(name = "Loading...", profileImageUrl = defaultPhotoUrl)
    )
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    init {
        firebaseAuth.addAuthStateListener { auth ->
            val user = auth.currentUser
            val isAuthenticated = user != null
            _isUserAuthenticated.value = isAuthenticated

            if (isAuthenticated && user != null) {
                // Ensure RTDB profile exists and start collecting profile
                viewModelScope.launch {
                    profileRepository.createInitialUserProfile(user)
                    startProfileDataCollection(user.uid)
                }
            } else {
                _userProfile.value = UserProfile(name = "Guest", profileImageUrl = defaultPhotoUrl)
            }
        }
    }

    private fun startProfileDataCollection(uid: String) {
        profileRepository.getUserProfileFlow(uid)
            .onEach { profile ->
                val updatedProfile = profile.copy(
                    profileImageUrl = profile.profileImageUrl ?: defaultPhotoUrl
                )
                _userProfile.value = updatedProfile
            }
            .launchIn(viewModelScope)
    }

    // ---------------- Authentication ----------------

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            authRepository.signIn(email, password)
                .onSuccess { user ->
                    _authState.value = AuthState.Success(user.email ?: "Unknown Email")
                    user.uid.let { startProfileDataCollection(it) } // Ensure profile is collected
                }
                .onFailure {
                    _authState.value = AuthState.Error(it.message ?: "Unknown error")
                }
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.signInWithGoogle(idToken)
            result.onSuccess { user ->
                _authState.value = AuthState.Success(user.email ?: "Unknown Email")

                // Use Google photo if available
                val googlePhotoUrl = user.photoUrl?.toString()

                viewModelScope.launch {
                    profileRepository.createInitialUserProfile(user, googlePhotoUrl)
                    startProfileDataCollection(user.uid)
                }
            }
            result.onFailure {
                _authState.value = AuthState.Error(it.message ?: "Unknown error")
            }
        }
    }


    fun logout() {
        viewModelScope.launch {
            try {
                FirebaseAuth.getInstance().signOut()
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Failed to log out")
            }
        }
    }

    // ---------------- Profile ----------------

    fun updateUserProfile(updatedProfile: UserProfile) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val profileToSave = updatedProfile.copy(
                profileImageUrl = updatedProfile.profileImageUrl ?: defaultPhotoUrl
            )
            profileRepository.updateUserProfile(profileToSave)
                .onSuccess {
                    _authState.value = AuthState.Idle
                }
                .onFailure {
                    _authState.value = AuthState.Error("Profile update failed: ${it.message}")
                    Log.e("AuthViewModel", "Profile update failed: ${it.message}", it)
                }
        }
    }

    fun refreshAuthStatus() {
        _isUserAuthenticated.value = authRepository.getCurrentUser() != null
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}
