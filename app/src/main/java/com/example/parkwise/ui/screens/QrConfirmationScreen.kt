package com.example.parkwise.ui.screens



import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import coil3.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrConfirmationScreen(bookingId: String, onBack: () -> Unit) {
    Scaffold(topBar = {
        TopAppBar(title = { Text("Booking Confirmed") }, navigationIcon = {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
        })
    }) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize(), horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
            Spacer(Modifier.height(24.dp))
            // AsyncImage to show QR (backend-provided URL)
            AsyncImage(model = "https://api.yourdomain.com/booking/$bookingId/qr", contentDescription = "QR Code", modifier = Modifier.size(220.dp))
            Spacer(Modifier.height(16.dp))
            Text("Show this QR code at entry", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(12.dp))
            Button(onClick = { /* share logic */ }) { Text("Share") }
        }
    }
}
