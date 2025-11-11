package com.example.parkwise.ui.screens


import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.parkwise.R
import com.example.parkwise.viewmodel.AuthViewModel
import com.example.parkwise.model.UserProfile
import com.example.parkwise.util.RequestLocationPermission
import com.example.parkwise.util.fetchLastKnownLocation
import com.example.parkwise.util.startLocationUpdates
import com.example.parkwise.util.reverseGeocode
import com.google.android.gms.location.FusedLocationProviderClient
import com.mapbox.geojson.Point
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// New necessary imports for image decoding
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
// End of Placeholder definitions

// ---------- FONTS ----------
private val FontFamilyDefault = FontFamily.SansSerif
private val TitleFontSize = 20.sp
val SubTitleFontSize = 14.sp
val BodyFontSize = 13.sp

// ---------- DATA ----------
data class ParkingRecommendation(
    val name: String,
    val distance: String,
    val price: String,
    val availability: Int,
    val isEV: Boolean
)

data class NavItem(
    val label: String,
    val imageVector: ImageVector? = null,
    val resourceId: Int? = null
)

data class VehicleType(
    val label: String,
    val resourceId: Int
)

// ---------- UTILITY FUNCTION FOR BASE64 DECODING (Needed to render saved image) ----------

/**
 * Utility function to convert a Base64 string to a Bitmap object.
 */
private fun base64ToBitmap(base64String: String): Bitmap? {
    return try {
        val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    } catch (e: IllegalArgumentException) {
        // Log error if necessary
        null
    }
}


// ---------- HOME SCREEN ----------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    authViewModel: AuthViewModel = viewModel(),
    onOpenMap: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenWallet: () -> Unit,
    onOpenNotifications: () -> Unit // NEW: Function to navigate to the notifications screen
) {
    // LOCATION LOGIC STATES AND SETUP
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isPermissionGranted by remember { mutableStateOf(false) }
    var userLocation by remember { mutableStateOf<Point?>(null) }
    var locationDisplayName by remember { mutableStateOf("Fetching location...") }
    var locationClient by remember { mutableStateOf<FusedLocationProviderClient?>(null) }
    var locationUpdateJob by remember { mutableStateOf<Job?>(null) }

    // Effect to update the display name whenever userLocation changes
    LaunchedEffect(userLocation) {
        val currentLoc = userLocation
        if (currentLoc != null) {
            val geocodedName = reverseGeocode(context, currentLoc)
            locationDisplayName = geocodedName ?: "Lat: ${String.format("%.4f", currentLoc.latitude())}, Lon: ${String.format("%.4f", currentLoc.longitude())}"
        }
    }

    // State for location string display (now mostly reflects the geocoded name)
    val locationText by remember {
        derivedStateOf {
            when {
                !isPermissionGranted -> "Location required"
                locationDisplayName.startsWith("Lat:") -> locationDisplayName
                else -> locationDisplayName
            }
        }
    }

    // Function to start or stop location updates
    val toggleLocationUpdates = remember<(Boolean) -> Unit> {
        { start ->
            if (start && isPermissionGranted) {
                locationUpdateJob?.cancel()
                locationClient?.removeLocationUpdates {}

                locationUpdateJob = coroutineScope.launch {
                    val client = startLocationUpdates(context) { newLocation ->
                        userLocation = newLocation
                    }
                    locationClient = client
                }
            } else {
                locationUpdateJob?.cancel()
                locationClient?.removeLocationUpdates {}
                locationClient = null
                locationUpdateJob = null
            }
        }
    }

    // 1. Request Location Permission at launch
    RequestLocationPermission { granted ->
        isPermissionGranted = granted
        if (granted) {
            coroutineScope.launch {
                userLocation = fetchLastKnownLocation(context)
                toggleLocationUpdates(true)
            }
        } else {
            toggleLocationUpdates(false)
        }
    }

    // 2. Cleanup: Stop location updates when the composable leaves the composition
    DisposableEffect(key1 = locationClient) {
        onDispose {
            locationClient?.removeLocationUpdates {}
        }
    }
    // END LOCATION LOGIC

    // COLLECT USER PROFILE
    val userProfile by authViewModel.userProfile.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var currentRoute by remember { mutableStateOf("Home") }
    val isLightTheme = !isSystemInDarkTheme()
    val mainBackgroundColor = if (isLightTheme) Color.White else Color.Black

    // Data for the 'Nearby Park' section
    val nearbyParks = listOf(
        ParkingRecommendation("Toserba Yogya Parking", "4 minutes", "4.20/hr", 28, false),
        ParkingRecommendation("Jayanti Sukabumi Parking", "7 minutes", "3.50/hr", 15, true),
        ParkingRecommendation("Siliwangi Plaza", "12 minutes", "5.00/hr", 45, false)
    )

    // Data for vehicle types (Park Easy & Safety)
    val vehicleTypes = listOf(
        VehicleType("Car", R.drawable.car),
        VehicleType("Bike", R.drawable.scooter),
        VehicleType("Truck", R.drawable.truck),
        VehicleType("Scooter", R.drawable.scooter)
    )

    // State for the selected vehicle type
    var selectedVehicle by remember { mutableStateOf(vehicleTypes.first()) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = if (isLightTheme) Color.White else Color.Black,
                modifier = Modifier.shadow(1.dp)
            ) {
                val navItems = listOf(
                    NavItem(label = "Home", imageVector = Icons.Default.Home),
                    NavItem(label = "Map", resourceId = R.drawable.map),
                    NavItem(label = "History", resourceId = R.drawable.circle),
                    NavItem(label = "Payments", resourceId = R.drawable.credit_card_icon),
                    NavItem(label = "Profile", imageVector = Icons.Default.Person)
                )

                navItems.forEach { item ->
                    val isSelected = currentRoute == item.label
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            currentRoute = item.label
                            when (item.label) {
                                "Map" -> onOpenMap()
                                "History" -> onOpenHistory()
                                "Payments" -> onOpenWallet()
                                "Profile" -> onOpenProfile()
                                else -> {}
                            }
                        },
                        icon = {
                            val tint = if (isSelected) {
                                Color(0xFF4CAF50)
                            } else {
                                (if (isLightTheme) Color.Gray else Color.LightGray).copy(alpha = 0.7f)
                            }
                            when {
                                item.imageVector != null -> Icon(
                                    item.imageVector,
                                    contentDescription = item.label,
                                    tint = tint,
                                    modifier = Modifier.size(28.dp)
                                )
                                item.resourceId != null -> Icon(
                                    painterResource(item.resourceId),
                                    contentDescription = item.label,
                                    tint = tint,
                                    modifier = Modifier.size(28.dp)
                                )
                                else -> {}
                            }
                        },
                        label = {
                            Text(
                                item.label,
                                fontFamily = FontFamilyDefault,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelected) Color(0xFF4CAF50) else (if (isLightTheme) Color.Black else Color.White).copy(alpha = 0.7f)
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF4CAF50),
                            selectedTextColor = Color(0xFF4CAF50),
                            unselectedIconColor = (if (isLightTheme) Color.Black else Color.White).copy(alpha = 0.7f),
                            unselectedTextColor = (if (isLightTheme) Color.Black else Color.White).copy(alpha = 0.7f),
                            indicatorColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                        )
                    )
                }
            }
        },
        containerColor = mainBackgroundColor
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .background(mainBackgroundColor)
                .fillMaxSize()
        ) {
            item {
                // 1. TOP HEADER & PROFILE SECTION
                TopHeaderAndBalanceCard(
                    location = locationText,
                    userName = userProfile.name ?: "User",
                    carName = userProfile.carName ?: "No Car",
                    fuelType = userProfile.fuelType ?: "N/A",
                    isLightTheme = isLightTheme,
                    carImageBase64 = userProfile.generatedCarImageBase64, // PASS NEW IMAGE DATA
                    onNotificationClick = onOpenNotifications // PASS THE CLICK HANDLER
                )
            }

            item {
                // 2. PARK EASY & SAFETY SECTION
                ParkEasyAndSafetySection(
                    vehicleTypes = vehicleTypes,
                    selectedVehicle = selectedVehicle,
                    onVehicleSelect = { selectedVehicle = it },
                    searchQuery = searchQuery,
                    onQueryChange = { searchQuery = it }
                )
                Spacer(Modifier.height(16.dp))
            }

            item {
                // 3. NEARBY PARK SECTION
                NearbyParkSection(
                    nearbyParks = nearbyParks,
                    isLightTheme = isLightTheme
                )
                Spacer(Modifier.height(20.dp))
            }

            // 4. OTHER SERVICES SECTION (QuickActionsAnimatedRow)
            item {
                QuickActionsAnimatedRow(MaterialTheme.colorScheme, isLightTheme)
                Spacer(Modifier.height(16.dp))
            }

            // REMOVED: NotificationsAnimatedCard section has been removed as requested.
            // Spacer(Modifier.height(24.dp)) // If you need space at the bottom, add it here.
        }
    }
}

// ----------------------------------------------------------------------------------
// ---------- MODIFIED COMPOSABLES FOR IMAGE UI MATCHING (TopHeaderAndBalanceCard) ----------
// ----------------------------------------------------------------------------------

/**
 * Composables that creates the large, green, gradient header with location and profile info.
 * MODIFIED: Notification bell is now clickable, and the Car Image is displayed.
 */
@Composable
fun TopHeaderAndBalanceCard(
    location: String,
    userName: String,
    carName: String,
    fuelType: String,
    isLightTheme: Boolean,
    carImageBase64: String?, // NEW: Accepts the Base64 image string
    onNotificationClick: () -> Unit // Click handler for the bell icon
) {
    val topGradientStart = Color(0xFF45D5FA)
    val topGradientEnd = Color(0xFF83E3E7)
    val totalHeaderHeight = 250.dp

    // Local state to hold the decoded image and its loading status
    var decodedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isImageLoading by remember { mutableStateOf(false) }

    // LaunchedEffect to decode the Base64 image when the string changes
    LaunchedEffect(carImageBase64) {
        if (carImageBase64 != null) {
            isImageLoading = true
            decodedBitmap = base64ToBitmap(carImageBase64)
            isImageLoading = false
        } else {
            decodedBitmap = null
            isImageLoading = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(totalHeaderHeight)
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(topGradientStart, topGradientEnd)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Location and Notification Icon (Top Row)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Location
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        location,
                        color = Color.White,
                        fontSize = TitleFontSize,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                // Notification Icon (Red dot) - NOW CLICKABLE
                BadgedBox(
                    badge = {
                        // Placeholder: set to false when no unread notifications
                        val hasUnread = true
                        if (hasUnread) {
                            Badge(
                                containerColor = Color.Red,
                                modifier = Modifier.size(8.dp).offset(x = (-4).dp, y = 4.dp)
                            )
                        }
                    },
                    modifier = Modifier
                        .clickable(onClick = onNotificationClick) // Add click handler
                        .align(Alignment.Top)
                        .padding(8.dp) // Add padding for a better click target
                ) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            Spacer(Modifier.height(40.dp)) // Space above Profile Info

            // Profile Info Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(Modifier.weight(1f)) {
                    // User Name
                    Text(
                        userName,
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(Modifier.height(4.dp))
                    // Car Name and Fuel Type
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Car Icon
                        Icon(
                            painterResource(R.drawable.car),
                            contentDescription = "Car",
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        // Car Name
                        Text(
                            carName,
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = SubTitleFontSize,
                            fontWeight = FontWeight.SemiBold
                        )
                        // Separator
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "|",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = SubTitleFontSize
                        )
                        Spacer(Modifier.width(8.dp))
                        // Fuel Type
                        Text(
                            fuelType,
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = SubTitleFontSize,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // CAR IMAGE CARD
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .size(90.dp)
                        .align(Alignment.Bottom)
                        .offset(y = (-4).dp)
                        .shadow(4.dp, RoundedCornerShape(16.dp))
                        .clickable { /* Handle click, e.g., open image viewer */ }
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            isImageLoading -> {
                                // Show loading indicator while decoding
                                CircularProgressIndicator(color = Color(0xFF4CAF50), modifier = Modifier.size(32.dp))
                            }
                            decodedBitmap != null -> {
                                // Show the decoded generated car image
                                Image(
                                    bitmap = decodedBitmap!!.asImageBitmap(),
                                    contentDescription = "User's Generated Car Image",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(16.dp)),
                                    contentScale = ContentScale.Crop // Use Crop to fill the square
                                )
                            }
                            else -> {
                                // Placeholder when no image is available
                                Icon(
                                    Icons.Default.Build,
                                    contentDescription = "No Car Image",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------------
// ---------- UNMODIFIED/SUPPORTING COMPOSABLES (Kept for completeness) ----------
// ----------------------------------------------------------------------------------

/**
 * Composables that creates the 'Park Easy & Safety' card with vehicle selection and search bar.
 */
@Composable
fun ParkEasyAndSafetySection(
    vehicleTypes: List<VehicleType>,
    selectedVehicle: VehicleType,
    onVehicleSelect: (VehicleType) -> Unit,
    searchQuery: String,
    onQueryChange: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .offset(y = (-50).dp)
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Park Wise",
                fontWeight = FontWeight.Bold,
                fontSize = TitleFontSize,
                color = Color.Black
            )
            Spacer(Modifier.height(16.dp))

            // Vehicle Type Selector Row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(vehicleTypes) { vehicle ->
                    VehicleTypeItem(
                        vehicle = vehicle,
                        isSelected = vehicle == selectedVehicle,
                        onClick = { onVehicleSelect(vehicle) }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onQueryChange,
                placeholder = { Text("Find Parking Space...", fontSize = BodyFontSize, color = Color.Gray) },
                trailingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.LightGray,
                    unfocusedBorderColor = Color.LightGray,
                    cursorColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Single item for vehicle type selection.
 */
@Composable
fun VehicleTypeItem(
    vehicle: VehicleType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp)
    ) {
        val primaryColor = Color(0xFF4CAF50)
        val backgroundColor = if (isSelected) primaryColor.copy(alpha = 0.1f) else Color.LightGray.copy(alpha = 0.3f)
        val iconTint = if (isSelected) primaryColor else Color.Gray

        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(backgroundColor)
                .border(
                    width = 2.dp,
                    color = if (isSelected) primaryColor else Color.Transparent,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painterResource(vehicle.resourceId),
                contentDescription = vehicle.label,
                tint = iconTint,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            vehicle.label,
            fontSize = SubTitleFontSize,
            color = if (isSelected) Color.Black else Color.Gray,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

/**
 * Composables that implements the 'Nearby Park' list, matching the card style.
 */
@Composable
fun NearbyParkSection(
    nearbyParks: List<ParkingRecommendation>,
    isLightTheme: Boolean
) {
    val textColor = if (isLightTheme) Color.Black else Color.Black
    val subTextColor = if (isLightTheme) Color.Gray else Color.LightGray

    Column(
        modifier = Modifier
            .offset(y = (-50).dp)
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Nearby Park",
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.Bold,
                fontSize = TitleFontSize,
                fontFamily = FontFamilyDefault,
                color = textColor
            )
            Text(
                "See More",
                color = Color(0xFF4CAF50),
                fontWeight = FontWeight.SemiBold,
                fontSize = SubTitleFontSize,
                modifier = Modifier.clickable { /* Handle See More Click */ }
            )
        }

        Spacer(Modifier.height(12.dp))

        // List of nearby parking spots
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            nearbyParks.forEachIndexed { index, rec ->
                NearbyParkItem(
                    recommendation = rec,
                    isFirst = index == 0,
                    isLightTheme = isLightTheme
                )
            }
        }
    }
}

/**
 * Single item card for a nearby parking recommendation.
 */
@Composable
fun NearbyParkItem(
    recommendation: ParkingRecommendation,
    isFirst: Boolean,
    isLightTheme: Boolean
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Placeholder for the Image/Map Thumbnail
            Box(
                modifier = Modifier
                    .size(width = 100.dp, height = 90.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.LightGray)
            ) {
                // Placeholder content for image
                Icon(
                    Icons.Default.Place,
                    contentDescription = "Map Thumbnail Placeholder",
                    modifier = Modifier.align(Alignment.Center).size(36.dp),
                    tint = Color.Gray
                )
            }

            Column(Modifier.weight(1f)) {
                // Parking Tags/Chips (Car, Bike, Bus, Truck, Scooter)
                // Using hardcoded tags to match the image
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    val tags = listOf("Car", "Bike", "Bus")
                    tags.forEach { tag ->
                        Text(
                            tag,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.LightGray.copy(alpha = 0.5f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    recommendation.name,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = TitleFontSize,
                    color = Color.Black
                )
                Text(
                    "Jl. R. E. Martadinata, Cikole, Suka...",
                    fontSize = BodyFontSize,
                    color = Color.Gray
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Price
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painterResource(R.drawable.payment),
                            contentDescription = "Price",
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            recommendation.price,
                            fontWeight = FontWeight.Bold,
                            fontSize = BodyFontSize,
                            color = Color.Black
                        )
                    }

                    // Distance/Time
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painterResource(R.drawable.time),
                            contentDescription = "Time",
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            recommendation.distance,
                            fontSize = BodyFontSize,
                            color = Color.Gray
                        )
                    }

                    // Availability
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painterResource(R.drawable.local_parking),
                            contentDescription = "Available Spots",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "${recommendation.availability} Available",
                            fontSize = BodyFontSize,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4CAF50)
                        )
                    }
                }
            }
        }
    }
}


// Placeholder for removed/modified components to avoid compile errors, with minimal content.

@Composable
fun HexProfileCard(
    name: String, vehicle: String, ecoScore: Int, fueltype: String, carNumberPlate: String,
    profileImageUrl: String? = null, colorScheme: ColorScheme, isLightTheme: Boolean
) {
    Spacer(Modifier.height(0.dp))
}

@Composable
fun SearchBarSection(searchQuery: String, onQueryChange: (String) -> Unit) {
    Spacer(Modifier.height(0.dp))
}

@Composable
fun ParkingRecommendationsGridItems(
    recommendations: List<ParkingRecommendation>,
    colorScheme: ColorScheme,
    isLightTheme: Boolean
) {
    Spacer(Modifier.height(0.dp))
}


// Helper class for four values (since Kotlin doesn't have Quadruple)
data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
fun QuickActionsAnimatedRow(colorScheme: ColorScheme, isLightTheme: Boolean) {
    Text(
        "Other Services",
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        fontWeight = FontWeight.Bold,
        fontSize = TitleFontSize,
        color = if (isLightTheme) Color.Black else Color.White
    )

    // Define actions with gradient colors
    val actions = listOf(
        Quadruple("Find Nearest", R.drawable.map, Color(0xFF141F32), Color(0xFF43587A)),
        Quadruple("EV Zones", R.drawable.electric_car, Color(0xFFC70039), Color(0xFFFF5733)),
        Quadruple("Book by QR", R.drawable.qrcode, Color(0xFF0D6EFD), Color(0xFF00C0FF)),
        Quadruple("Offers", R.drawable.ticket, Color(0xFFFF9800), Color(0xFFFFC107))
    )

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(actions) { (label, iconRes, startColor, endColor) ->
            val scale = remember { Animatable(1f) }
            LaunchedEffect(Unit) {
            }

            Box(
                modifier = Modifier
                    .height(52.dp)
                    .graphicsLayer(scaleX = scale.value, scaleY = scale.value)
                    .shadow(8.dp, RoundedCornerShape(14.dp))
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(startColor, endColor)
                        )
                    )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .clickable { /* handle click */ }
                ) {
                    Icon(
                        painter = painterResource(iconRes),
                        contentDescription = label,
                        modifier = Modifier.size(24.dp),
                        tint = Color.White
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(label, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color.White)
                }
            }
        }
    }
    Spacer(Modifier.height(16.dp))
}

// REMOVED: NotificationsAnimatedCard has been removed as requested.
