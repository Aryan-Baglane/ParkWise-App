package com.example.parkwise.viewmodel



import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.parkwise.data.ParkingRepository
import com.example.parkwise.model.BookingSummary
import com.example.parkwise.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HistoryViewModel(private val repo: ParkingRepository = ParkingRepository()) : ViewModel() {
    private val _bookings = MutableStateFlow<List<BookingSummary>>(emptyList())
    val bookings: StateFlow<List<BookingSummary>> = _bookings
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    fun load(userId: Int = 1) {
        viewModelScope.launch {
            _loading.value = true
            _bookings.value = repo.getHistory(userId)
            _loading.value = false
        }
    }
}

