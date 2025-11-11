package com.example.parkwise.viewmodel



import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.parkwise.data.ParkingRepository
import com.example.parkwise.model.ParkingSlot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LayoutViewModel(private val repo: ParkingRepository = ParkingRepository()) : ViewModel() {

    private val _slots = MutableStateFlow<List<ParkingSlot>>(emptyList())
    val slots: StateFlow<List<ParkingSlot>> = _slots

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    fun loadSlots(areaId: Int) {
        viewModelScope.launch {
            _loading.value = true
            try {
                _slots.value = repo.getSlots(areaId)
            } catch (e: Exception) {
                _slots.value = emptyList()
            } finally {
                _loading.value = false
            }
        }
    }

    /** Update slot locally (used by WebSocket to reflect remote updates) */
    fun updateSlot(slotId: Int, newStatus: com.example.parkwise.model.SlotStatus) {
        _slots.value = _slots.value.map {
            if (it.id == slotId) it.copy(status = newStatus) else it
        }
    }
}
