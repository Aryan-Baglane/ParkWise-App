package com.example.parkwise.model

import androidx.compose.runtime.Immutable

@Immutable
data class ParkingArea(
    val id: Int,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val totalSlots: Int,
    val occupiedSlots: Int,
    val availableSlots: Int,
    val evSlots: Int,
    val occupiedEvSlots: Int,
    val pricePerHour: Double,
    val rating: Double,
    val hours: String,
    val amenities: List<String>,
    val hasEV: Boolean = evSlots > 0
)
