package com.example.parkwise.components



import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun UserCard(name: String, vehicle: String) {
    Card(modifier = Modifier.padding(16.dp).fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)) {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(name, style = MaterialTheme.typography.titleMedium)
                Text(vehicle, style = MaterialTheme.typography.bodyMedium)
            }
            Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                Text("EV Friendly", style = MaterialTheme.typography.bodySmall)
                Text("CO₂ saved 12kg", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
