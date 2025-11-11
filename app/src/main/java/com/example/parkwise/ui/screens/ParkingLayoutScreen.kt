package com.example.parkwise.ui.screens



import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.parkwise.model.ParkingSlot
import com.example.parkwise.viewmodel.LayoutViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParkingLayoutScreen(
    parkingId: Int,
    viewModel: LayoutViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onBookSlot: (Int) -> Unit,
    onBack: () -> Unit
) {
    LaunchedEffect(parkingId) { viewModel.loadSlots(parkingId) }
    val slots by viewModel.slots.collectAsState()
    val loading by viewModel.loading.collectAsState()
    var selectedSlot by remember { mutableStateOf<ParkingSlot?>(null) }

    Scaffold(topBar = {
        TopAppBar(title = { Text("Parking Layout") }, navigationIcon = {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
        })
    }) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            // 3D placeholder area
            Box(modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
                .padding(12.dp),
            ) {
                // TODO: Replace with SceneView/Filament 3D rendering and map slots -> node colors
                Card(modifier = Modifier.fillMaxSize(), elevation = CardDefaults.cardElevation(8.dp)) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                        Text("3D Layout (placeholder) — integrate SceneView/GLB here")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text("Slots", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 16.dp))

            LazyVerticalGrid(columns = GridCells.Fixed(4), modifier = Modifier.padding(12.dp).fillMaxHeight(), contentPadding = PaddingValues(bottom = 80.dp)) {
                items(slots, key = { it.id }) { slot ->
                    val color = when {
                        slot.status == com.example.parkwise.model.SlotStatus.AVAILABLE && slot.hasCharger -> MaterialTheme.colorScheme.primary
                        slot.status == com.example.parkwise.model.SlotStatus.AVAILABLE -> MaterialTheme.colorScheme.secondaryContainer
                        slot.status == com.example.parkwise.model.SlotStatus.OCCUPIED -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                    Card(modifier = Modifier
                        .padding(8.dp)
                        .size(72.dp)
                        .clickable(enabled = slot.status == com.example.parkwise.model.SlotStatus.AVAILABLE) {
                            selectedSlot = slot
                        },
                        colors = CardDefaults.cardColors(containerColor = color)
                    ) {
                        Box(contentAlignment = androidx.compose.ui.Alignment.Center) {
                            Text(slot.number, color = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                }
            }
        }

        // selection bottom sheet
        AnimatedVisibility(visible = selectedSlot != null) {
            selectedSlot?.let { s ->
                Card(modifier = Modifier.fillMaxWidth().padding(12.dp), elevation = CardDefaults.cardElevation(12.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Slot ${s.number}", style = MaterialTheme.typography.titleMedium)
                        Text(if (s.hasCharger) "EV Charger: ${s.chargerType} • ${s.chargingSpeedKw} kW" else "No EV charger")
                        Spacer(modifier = Modifier.height(8.dp))
                        Row {
                            Button(onClick = { onBookSlot(s.id) }) { Text("Book & Pay") }
                            Spacer(Modifier.width(8.dp))
                            TextButton(onClick = { selectedSlot = null }) { Text("Cancel") }
                        }
                    }
                }
            }
        }
    }
}
