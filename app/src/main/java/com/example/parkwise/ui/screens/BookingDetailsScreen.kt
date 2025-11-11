package com.example.parkwise.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.parkwise.model.BookingSummary
import kotlinx.coroutines.delay

@Composable
fun BookingDetailsScreen(
    booking: BookingSummary,
    onRebook: (Int) -> Unit,
    onNavigate: (Int) -> Unit
) {
    // Determine the state based on booking status
    val isSuccessful = booking.status.uppercase() == "COMPLETED" || booking.status.uppercase() == "ACTIVE"
    val isCancelled = booking.status.uppercase() == "CANCELED"

    // Set colors and text based on status
    val primaryColor = if (isSuccessful) Color(0xFF66BB6A) else if (isCancelled) Color(0xFFF44336) else Color(
        0xFFEF0303
    )
    val statusText = if (isSuccessful) "Booking Successful!" else if (isCancelled) "Booking Canceled" else "Booking Details"

    // Outer container: Add top padding to account for the camera cutout/status bar
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .verticalScroll(rememberScrollState())
            // Added 30.dp of padding to push content down from the top edge
            .padding(top = 60.dp, start = 20.dp, end = 20.dp, bottom = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- 1. Main Content Card ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .padding(vertical = 30.dp, horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- Status Icon (Checkmark or X) ---
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(primaryColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isSuccessful) Icons.Default.Check else Icons.Default.Close,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- Status Text & Price ---
            Text(
                text = statusText,
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.Black
            )

            if (isSuccessful) {
                Text(
                    text = "$${booking.amount}",
                    style = MaterialTheme.typography.headlineMedium.copy(color = primaryColor, fontWeight = FontWeight.SemiBold)
                )
            } else if (isCancelled) {
                Text(
                    text = "No Charge Applied",
                    style = MaterialTheme.typography.headlineMedium.copy(color = Color.Gray, fontWeight = FontWeight.SemiBold)
                )
            }


            Spacer(modifier = Modifier.height(30.dp))
            Divider(color = Color(0xFFE0E0E0), thickness = 1.dp, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(20.dp))

            // --- Parking Details (A-09 Badge, Location) ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = booking.slotNumber.ifEmpty { "A-09" },
                    color = Color.White,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF424242))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .width(IntrinsicSize.Min),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = booking.parkingName.ifEmpty { "Toserba Yogya Car Parking" },
                        style = MaterialTheme.typography.titleLarge
                    )

                }
            }

            Spacer(modifier = Modifier.height(30.dp))
            Divider(color = Color(0xFFE0E0E0), thickness = 1.dp, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(20.dp))

            // --- Booking Time and Date Details ---
            val detailsList = listOf(
                "Booking Date" to booking.date.ifEmpty { "28 July 2023" },
                "No. Parking" to "#${booking.parkingAreaId}",
                "Arrive After" to booking.startTime.ifEmpty { "10:00 AM" },
                "Exit Before" to booking.endTime.ifEmpty { "11:00 AM" },
                "Duration" to booking.duration.ifEmpty { "1 Hours" }
            )

            detailsList.forEach { (label, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = label, style = MaterialTheme.typography.bodyLarge, color = Color.DarkGray)
                    Text(text = value, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
                }
            }

            // --- Conditionally show Barcode/Error Message ---
            if (isSuccessful) {
                Spacer(modifier = Modifier.height(40.dp))

                // Barcode Placeholder
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Canvas(modifier = Modifier.fillMaxWidth(0.95f).height(80.dp)) {
                        // Barcode drawing logic (unchanged)
                        val barColor = Color.DarkGray
                        val totalBars = 80
                        val barWidth = size.width / totalBars
                        for (i in 0 until totalBars) {
                            val x = i * barWidth
                            val heightFactor = 0.7f + (i % 7) * 0.05f
                            val height = size.height * heightFactor
                            val thickness = barWidth * 0.7f
                            drawRect(
                                color = barColor,
                                topLeft = Offset(x, (size.height - height) / 2),
                                size = androidx.compose.ui.geometry.Size(thickness, height)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Show Your Ticket Barcode", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            } else {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "This ticket is no longer valid for entry.",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = Color.Red
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))



    }
}