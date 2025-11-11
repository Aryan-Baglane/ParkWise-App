package com.example.parkwise.data

import com.example.parkwise.model.*
import com.example.parkwise.network.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ParkingRepository(private val api: ApiService = RetrofitClient.apiService) {

    /** Nearby parking areas */
    suspend fun getNearby(lat: Double, lon: Double): List<ParkingArea> = withContext(Dispatchers.IO) {
        val res = api.getNearbyParking(lat, lon)
        if (res.isSuccessful) res.body() ?: emptyList() else emptyList()
    }

    /** Slots for a specific parking area */
    suspend fun getSlots(areaId: Int): List<ParkingSlot> = withContext(Dispatchers.IO) {
        val res = api.getParkingSlots(areaId)
        if (res.isSuccessful) res.body() ?: emptyList() else emptyList()
    }

    /** Predict parking price for a duration */
    suspend fun predictPrice(areaId: Int, durationHours: Double): Double? = withContext(Dispatchers.IO) {
        val res = api.predictPrice(areaId, mapOf("duration_hours" to durationHours))
        if (res.isSuccessful) {
            val body = res.body()
            (body?.get("predicted_price") as? Number)?.toDouble()
        } else null
    }

    /** Book a slot; returns bookingId + QR URL */
    suspend fun bookSlot(userId: Int, slotId: Int, durationHours: Double): BookingResponse? = withContext(Dispatchers.IO) {
        val res = api.bookSlot(BookRequest(userId, slotId, durationHours))
        if (res.isSuccessful) res.body() else null
    }

    /** Confirm booking after payment */
    suspend fun confirmBooking(orderId: String, provider: String, providerPaymentId: String): BookingResponse? = withContext(Dispatchers.IO) {
        val res = api.confirmBooking(ConfirmRequest(orderId, provider, providerPaymentId))
        if (res.isSuccessful) res.body() else null
    }

    /** Get booking history for a user */
    suspend fun getHistory(userId: Int): List<BookingSummary> = withContext(Dispatchers.IO) {
        val res = api.getBookingHistory(userId)
        if (!res.isSuccessful) return@withContext emptyList()
        val list = res.body() ?: return@withContext emptyList()
        list.map {
            BookingSummary(
                bookingId = it["bookingId"].toString(),
                parkingAreaId = (it["areaId"] as Number).toInt(),
                parkingName = it["parkingName"].toString(),
                slotNumber = it["slotNumber"].toString(),
                amount = (it["amount"] as Number).toDouble(),
                status = it["status"].toString(),
                startTime = it["startTime"].toString(),
                endTime = it["endTime"].toString()
            )
        }
    }

    /** Get user profile */
    suspend fun getProfile(userId: Int): UserProfile? = withContext(Dispatchers.IO) {
        val res = api.getProfile(userId)
        if (!res.isSuccessful) return@withContext null
        val body = res.body() ?: return@withContext null
        UserProfile(
            userId = userId.toString(),
            name = body["name"].toString(),
            email = body["email"].toString(),
            vehicle = body["vehicle"].toString(),
            walletBalance = (body["walletBalance"] as? Number)?.toDouble() ?: 0.0,
            prefersEv = (body["prefersEv"] as? Boolean) ?: false
        )
    }
}
