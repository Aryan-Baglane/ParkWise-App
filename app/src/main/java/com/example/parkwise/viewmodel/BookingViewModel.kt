package com.example.parkwise.viewmodel



import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.parkwise.data.ParkingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BookingViewModel(private val repo: ParkingRepository = ParkingRepository()) : ViewModel() {

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    /** Returns predicted price or null */
    suspend fun predictPrice(slotId: Int, durationHours: Double): Double? {
        // here we assume slotId -> areaId mapping exists or backend endpoint accepts slot id.
        return repo.predictPrice(areaId = 0, durationHours = durationHours) // replace areaId mapping
    }

    fun bookSlot(userId: Int, slotId: Int, durationHours: Double, onResult: (result: com.example.parkwise.network.BookingResponse?) -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val res = repo.bookSlot(userId, slotId, durationHours)
                onResult(res)
            } catch (e: Exception) {
                onResult(null)
            } finally {
                _loading.value = false
            }
        }
    }

    fun confirmBooking(orderId: String, provider: String, providerPaymentId: String, onResult: (com.example.parkwise.network.BookingResponse?) -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val res = repo.confirmBooking(orderId, provider, providerPaymentId)
                onResult(res)
            } catch (e: Exception) {
                onResult(null)
            } finally {
                _loading.value = false
            }
        }
    }
}
