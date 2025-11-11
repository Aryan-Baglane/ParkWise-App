package com.example.parkwise.network


import com.example.parkwise.model.ParkingArea
import com.example.parkwise.model.ParkingSlot
import retrofit2.Response
import retrofit2.http.*

data class BookingResponse(val bookingId: String, val qr_url: String, val status: String)
data class BookRequest(val userId: Int, val slotId: Int, val durationHours: Double)
data class ConfirmRequest(val orderId: String, val paymentProvider: String, val providerPaymentId: String)

interface ApiService {
    @GET("parking/nearby")
    suspend fun getNearbyParking(@Query("lat") lat: Double, @Query("lon") lon: Double): Response<List<ParkingArea>>

    @GET("parking/{areaId}/slots")
    suspend fun getParkingSlots(@Path("areaId") areaId: Int): Response<List<ParkingSlot>>

    @POST("parking/{areaId}/predictPrice")
    suspend fun predictPrice(@Path("areaId") areaId: Int, @Body body: Map<String, Any>): Response<Map<String, Any>>

    @POST("booking/book")
    suspend fun bookSlot(@Body req: BookRequest): Response<BookingResponse>

    @POST("booking/confirm")
    suspend fun confirmBooking(@Body req: ConfirmRequest): Response<BookingResponse>

    // History & profile
    @GET("user/{userId}/bookings")
    suspend fun getBookingHistory(@Path("userId") userId: Int): Response<List<Map<String, Any>>>

    @GET("user/{userId}/profile")
    suspend fun getProfile(@Path("userId") userId: Int): Response<Map<String, Any>>
}
