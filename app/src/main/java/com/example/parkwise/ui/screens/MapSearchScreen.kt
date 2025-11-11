package com.example.parkwise.ui.screens

import android.graphics.BitmapFactory
import android.util.Log
import android.view.ContextThemeWrapper
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.parkwise.R
import com.example.parkwise.model.ParkingArea
import com.example.parkwise.util.*
import com.example.parkwise.viewmodel.MapViewModel
import com.google.gson.JsonParser
import com.mapbox.geojson.Point
import com.mapbox.maps.*
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.*
import kotlin.math.max

@Composable
fun MapSearchScreen(
    viewModel: MapViewModel = viewModel(),
    onParkingSelected: (Int) -> Unit
) {
    val context = LocalContext.current
    val userLocation by viewModel.userLocation.collectAsState()
    val areas by viewModel.parkingAreas.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val roadDistances by viewModel.roadDistances.collectAsState()
    val roadDurations by viewModel.roadDurations.collectAsState()

    var mapView by remember { mutableStateOf<MapView?>(null) }
    var markerManager by remember { mutableStateOf<PointAnnotationManager?>(null) }
    var permissionGranted by remember { mutableStateOf(false) }

    // State for current map zoom to be used for marker scaling
    val DEFAULT_ZOOM = 14.5
    var currentZoom by remember { mutableStateOf(DEFAULT_ZOOM) }

    RequestLocationPermission { granted -> permissionGranted = granted }

    LaunchedEffect(permissionGranted) {
        if (permissionGranted) {
            fetchLastKnownLocation(context)?.let { point ->
                viewModel.updateUserLocation(point)
            }
            startLocationUpdates(context) { point ->
                viewModel.updateUserLocation(point)
            }
        }
    }

    // State to ensure we only fly to the user location once on initialization
    val mapInitializedToUserLocation = remember { mutableStateOf(false) }

    LaunchedEffect(userLocation, mapView) {
        // Only fly to user location if we have a location, the map is ready, and we haven't done it yet
        if (mapView != null && userLocation != null && !mapInitializedToUserLocation.value) {
            mapView?.mapboxMap?.flyTo(
                CameraOptions.Builder().center(userLocation!!).zoom(currentZoom).build()
            )
            mapInitializedToUserLocation.value = true
        }
    }

    // Effect to update markers when data changes (areas, user location)
    LaunchedEffect(areas, userLocation, markerManager, currentZoom) {
        markerManager?.let { manager ->
            updateMapMarkers(
                manager = manager,
                areas = areas,
                onParkingSelected = onParkingSelected,
                zoom = currentZoom,
                userLoc = userLocation
            )
        }
    }

    // FIX: Define the state setter capture using rememberUpdatedState
    val updatedSetZoom = rememberUpdatedState(newValue = { zoom: Double -> currentZoom = zoom })

    // FIX: Use DisposableEffect to manage the subscription lifecycle with the correct Mapbox API
    DisposableEffect(mapView, markerManager) {
        val mapboxMap = mapView?.mapboxMap
        var cancelable: com.mapbox.common.Cancelable? = null

        cancelable = mapboxMap?.subscribeCameraChanged { cameraChangedEvent ->
            updatedSetZoom.value(cameraChangedEvent.cameraState.zoom)
        }

        onDispose {
            cancelable?.cancel()
        }
    }

    val recommended = remember(areas, roadDistances) {
        if (roadDistances.isNotEmpty()) {
            areas.sortedBy { roadDistances[it.id] ?: Double.MAX_VALUE }.take(3)
        } else emptyList()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 🌍 Dimmed Map Background
        AndroidView(
            factory = { ctx ->
                val themed = ContextThemeWrapper(ctx, androidx.appcompat.R.style.Theme_AppCompat)

                // Set initial camera options
                val initialCameraOptions = CameraOptions.Builder()
                    // Default to a common location (e.g., San Francisco) or use a known fallback
                    .center(userLocation ?: Point.fromLngLat(-122.4194, 37.7749))
                    .zoom(DEFAULT_ZOOM)
                    .build()

                // FIX: Use the simplest MapView(context) constructor
                // and rely on the LaunchedEffect for the initial camera move.
                MapView(context = themed).apply {
                    mapView = this

                    // ❌ REMOVED: this.initialize(MapInitOptions(cameraOptions = initialCameraOptions))
                    // This is handled by the LaunchedEffect to prevent crashing.

                    mapboxMap.loadStyleUri(Style.MAPBOX_STREETS) { style ->
                        try {
                            style.addImage(
                                "marker-blue",
                                BitmapFactory.decodeResource(ctx.resources, R.drawable.marker_red_icon)
                            )
                            style.addImage(
                                "marker-user",
                                BitmapFactory.decodeResource(ctx.resources, R.drawable.marker_blue_icon)
                            )
                        } catch (e: Exception) {
                            Log.e("MapSearchScreen", "Marker load failed: ${e.message}")
                        }
                        markerManager = annotations.createPointAnnotationManager()
                    }
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = 0.9f } // Slight dim effect
        )

        // 🩶 Top gradient blur overlay (for forged UI look)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.background.copy(alpha = 0.95f),
                            MaterialTheme.colorScheme.background.copy(alpha = 0.3f),
                            Color.Transparent
                        )
                    )
                )
                .blur(18.dp)
        )

        // 🩶 Bottom gradient overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Transparent,
                            MaterialTheme.colorScheme.background.copy(alpha = 0.3f),
                            MaterialTheme.colorScheme.background.copy(alpha = 0.95f)
                        )
                    )
                )
                .blur(22.dp)
        )

        // 🔍 Floating Top Search Bar + Filter Chips
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 36.dp)
                .align(Alignment.TopCenter)
        ) {
            TopSearchAndFilterChips()
        }

        // 🔘 Loading Indicator
        if (loading)
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

        // 🧭 Bottom Recommendation Cards
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Recommended Nearby Parking",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(8.dp))
            if (recommended.isEmpty()) {
                Text("Searching for best nearby parking...")
            } else {
                recommended.forEach { area ->
                    val dist = roadDistances[area.id]
                    val dur = roadDurations[area.id]
                    ParkingCard(
                        area = area,
                        distance = dist,
                        duration = dur,
                        onSelect = { onParkingSelected(area.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun TopSearchAndFilterChips() {
    var searchQuery by remember { mutableStateOf("") }
    var selectedChip by remember { mutableStateOf("Car") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // 🔹 Search Bar
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search parking or location...") },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            trailingIcon = {
                Icon(
                    painter = painterResource(R.drawable.filter),
                    contentDescription = "Filter",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(26.dp)
                )
            },
            shape = RoundedCornerShape(16.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(6.dp, RoundedCornerShape(16.dp))
        )

        Spacer(Modifier.height(12.dp))

        val chips = listOf(
            Triple("Car", R.drawable.car, Color(0xFF2196F3)),
            Triple("Scooter", R.drawable.scooter, Color(0xFF4CAF50)),
            Triple("EV", R.drawable.electric_car, Color(0xFFFFC107)),
            Triple("Truck", R.drawable.truck, Color(0xFFF44336))
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(chips) { (label, iconRes, color) ->
                val isSelected = selectedChip == label
                val bgColor = if (isSelected) color.copy(alpha = 0.15f)
                else MaterialTheme.colorScheme.surface
                val borderColor = if (isSelected) color else Color.Gray.copy(alpha = 0.3f)

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = bgColor,
                    border = BorderStroke(1.dp, borderColor),
                    modifier = Modifier
                        .height(42.dp)
                        .clickable { selectedChip = label }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(iconRes),
                            contentDescription = label,
                            tint = color,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = label,
                            color = color,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ParkingCard(
    area: ParkingArea,
    distance: Double?,
    duration: Double?,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        onClick = onSelect
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(area.name, style = MaterialTheme.typography.titleMedium)
                    Text(
                        area.address,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "₹${"%.2f".format(area.pricePerHour)}/hr",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoChip(R.drawable.ic_distance, distance?.let { formatDistance(it) } ?: "—")
                InfoChip(R.drawable.time, duration?.let { formatDuration(it) } ?: "—")
                InfoChip(R.drawable.local_parking, "${area.availableSlots} slots")
                InfoChip(R.drawable.ic_star, "${area.rating}⭐")
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = onSelect,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Choose Place")
            }
        }
    }
}

@Composable
fun InfoChip(icon: Int, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = null,
            modifier = Modifier
                .size(16.dp)
                .padding(end = 4.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
    }
}

fun formatDistance(m: Double): String =
    if (m >= 1000) "${"%.1f".format(m / 1000)} km" else "${m.toInt()} m"

fun formatDuration(seconds: Double): String {
    val minutes = (seconds / 60).toInt()
    val hours = minutes / 60
    return when {
        hours > 0 -> "${hours}h ${minutes % 60}m"
        else -> "${minutes} min"
    }
}

// 💡 NEW CONSTANT: Multiplier to aggressively shrink the icon size, fixing the over-scaling issue.
private const val ICON_SIZE_MULTIPLIER = 0.2 // Adjust this value (0.1 to 0.5 is common)

/**
 * UPDATED: Now takes the current map zoom to calculate a reasonable, scaled icon size.
 * Uses ICON_SIZE_MULTIPLIER to shrink the base size of the marker.
 */
fun updateMapMarkers(
    manager: PointAnnotationManager?,
    areas: List<ParkingArea>,
    onParkingSelected: (Int) -> Unit,
    zoom: Double,
    userLoc: Point?
) {
    manager ?: return
    manager.deleteAll()

    // Base zoom level for icon normalization.
    val baseZoom = 14.5

    // Scale factor: Scales the icon size between 0.6 (min) and 1.3 (max)
    val scaleFactor: Double = (zoom / baseZoom).coerceIn(0.6, 1.3)

    // Base size is multiplied by the new ICON_SIZE_MULTIPLIER
    val parkingIconBaseSize = 1.0 * ICON_SIZE_MULTIPLIER
    val userIconBaseSize = 1.3 * ICON_SIZE_MULTIPLIER // User icon slightly larger

    val parkingSize = parkingIconBaseSize * scaleFactor
    val userSize = userIconBaseSize * scaleFactor

    areas.forEach { area ->
        val point = Point.fromLngLat(area.longitude, area.latitude)
        manager.create(
            PointAnnotationOptions()
                .withPoint(point)
                .withIconImage("marker-blue")
                .withIconSize(parkingSize) // Scaled size
                .withData(JsonParser.parseString("{\"parking_id\":${area.id}}"))
        )
    }

    userLoc?.let {
        manager.create(
            PointAnnotationOptions()
                .withPoint(it)
                .withIconImage("marker-user")
                .withIconSize(userSize) // Scaled size
        )
    }

    manager.addClickListener { ann ->
        ann.getData()?.asJsonObject?.get("parking_id")?.asInt?.let(onParkingSelected)
        true
    }
}
