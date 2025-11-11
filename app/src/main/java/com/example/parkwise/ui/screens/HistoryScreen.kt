package com.example.parkwise.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import com.example.parkwise.R
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.parkwise.model.BookingSummary


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onRebook: (Int) -> Unit = {},
    onBookingClick: (BookingSummary) -> Unit = {}
) {
    val bookings = remember {
        mutableStateListOf(
            BookingSummary(
                bookingId = "B001",
                parkingAreaId = 1,
                parkingName = "City Center Parking",
                slotNumber = "A12",
                amount = 200.0,
                status = "Completed",
                startTime = "10:00 AM",
                endTime = "12:00 PM",
                date = "2025-10-20",
                duration = "2h",
                bookingTimestamp = "2025-10-19 09:00",
                dynamicPricing = "None",
                paymentMethod = "UPI",
                rating = 4.5f,
                ecoScore = 2.0f
            ),
            BookingSummary(
                bookingId = "B002",
                parkingAreaId = 2,
                parkingName = "Mall Parking Lot",
                slotNumber = "B7",
                amount = 150.0,
                status = "Cancelled",
                startTime = "02:00 PM",
                endTime = "04:00 PM",
                date = "2025-10-18",
                duration = "2h",
                bookingTimestamp = "2025-10-17 11:00",
                dynamicPricing = "Yes",
                paymentMethod = "Card",
                rating = null,
                ecoScore = null
            )
        )
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("My Bookings") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxSize()
        ) {
            if (bookings.isEmpty()) {
                Text(
                    "No bookings yet",
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                bookings.forEach { booking ->
                    BookingCard(
                        booking = booking,
                        onRebook = { onRebook(booking.parkingAreaId) },
                        onBookingClick = { onBookingClick(booking) }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun BookingCard(
    booking: BookingSummary,
    onRebook: () -> Unit,
    onBookingClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(24.dp), clip = true)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painterResource(R.drawable.local_parking),
                    contentDescription = "Parking Icon",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = booking.parkingName,
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp)
                    )
                    Text("Slot: ${booking.slotNumber}", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                }
                Spacer(modifier = Modifier.weight(1f))
                Column(horizontalAlignment = Alignment.End) {
                    // Status and Price
                    val statusColor = when (booking.status.lowercase()) {
                        "completed" -> Color(0xFF4CAF50)
                        "cancelled" -> Color(0xFFF44336)
                        else -> Color.Gray
                    }
                    Text(booking.status, color = statusColor, fontSize = 13.sp)
                    Text(
                        "₹${booking.amount}",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = "Rating", tint = Color(0xFFFFD700), modifier = Modifier.size(18.dp))
                    Text(text = booking.rating?.toString() ?: "-", fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(booking.date, fontSize = 13.sp, color = Color.Gray)
                }
                Row {
                    Button(
                        onClick = onRebook,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2061F9))
                    ) { Text("Rebook") }
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(onClick = onBookingClick) {
                        Text("View Details")
                    }
                }
            }
        }
    }
}
