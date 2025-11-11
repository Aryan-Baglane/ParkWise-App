package com.example.parkwise.model

import kotlinx.serialization.Serializable

@Serializable
data class BookingSummary(
    val bookingId: String,
    val parkingAreaId: Int,
    val parkingName: String,
    val slotNumber: String,
    val amount: Double,
    val status: String, // "Completed" | "Upcoming" | "Cancelled"
    val startTime: String = "",
    val endTime: String = "",
    val date: String = "",
    val duration: String = "",
    val bookingTimestamp: String = "",
    val dynamicPricing: String = "",
    val paymentMethod: String = "",
    val rating: Float? = null,
    val ecoScore: Float? = null,
    val isFavorite: Boolean = false,

)
