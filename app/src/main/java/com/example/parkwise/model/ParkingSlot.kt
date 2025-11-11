package com.example.parkwise.model



data class ParkingSlot(
    val id: Int,
    val number: String,
    val name: String = "Slot $id",
    val hasCharger: Boolean = false,
    val chargerType: String? = null,
    val chargingSpeedKw: Double? = null,
    val status: SlotStatus = SlotStatus.AVAILABLE
)

enum class SlotStatus {
    AVAILABLE,
    OCCUPIED,
    RESERVED
}
