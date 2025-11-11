package com.example.parkwise.ui.screens

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.parkwise.viewmodel.BookingViewModel
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun BookingScreen(
    slotId: Int,
    viewModel: BookingViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onBookingSuccess: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var startTime by remember { mutableStateOf("Now") }
    var endTime by remember { mutableStateOf("1 hr") }
    var price by remember { mutableStateOf(0.0) }
    var checking by remember { mutableStateOf(false) }

    Scaffold(topBar = { TopAppBar(title = { Text("Booking") }) }) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Text("Slot: #$slotId", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))

            // Time pickers
            OutlinedButton(onClick = {
                val now = Calendar.getInstance()
                TimePickerDialog(context, { _, hour, minute ->
                    startTime = "%02d:%02d".format(hour, minute)
                }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true).show()
            }) { Text("Select start: $startTime") }

            Spacer(Modifier.height(8.dp))

            OutlinedButton(onClick = {
                val now = Calendar.getInstance()
                TimePickerDialog(context, { _, hour, minute ->
                    endTime = "%02d:%02d".format(hour, minute)
                }, now.get(Calendar.HOUR_OF_DAY) + 1, now.get(Calendar.MINUTE), true).show()
            }) { Text("Select end: $endTime") }

            Spacer(Modifier.height(12.dp))

            // Price preview
            Button(onClick = {
                checking = true
                scope.launch {
                    price = viewModel.predictPrice(slotId, 1.0) ?: 0.0
                    checking = false
                }
            }) {
                Text(if (checking) "Checking..." else "Get Price")
            }

            if (price > 0.0) {
                Text("Estimated Price: ₹${"%.2f".format(price)}", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(12.dp))
                Button(onClick = {
                    viewModel.bookSlot(userId = 1, slotId = slotId, durationHours = 1.0) { bookingRes ->
                        bookingRes?.let { onBookingSuccess(it.bookingId) }
                    }
                }) {
                    Text("Confirm & Pay")
                }
            }
        }
    }
}
