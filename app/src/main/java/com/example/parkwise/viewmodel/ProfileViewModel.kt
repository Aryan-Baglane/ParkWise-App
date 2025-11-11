package com.example.parkwise.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.parkwise.data.ParkingRepository
import com.example.parkwise.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(private val repo: ParkingRepository = ParkingRepository()) : ViewModel() {
    private val _profile = MutableStateFlow(UserProfile("1", "Loading...", "—", "—", "0.0"))
    val profile: StateFlow<UserProfile> = _profile

    init { loadProfile() }

    private fun loadProfile(userId: Int = 1) {
        viewModelScope.launch {
            repo.getProfile(userId)?.let { _profile.value = it }
        }
    }

    fun setPrefersEv(prefers: Boolean) {
        val p = _profile.value.copy(prefersEv = prefers)
        _profile.value = p
        // Optionally send to backend
    }
}
