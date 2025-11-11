package com.example.parkwise.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import com.google.android.gms.location.*
import com.mapbox.geojson.Point
import kotlinx.coroutines.tasks.await
import android.location.Geocoder
import android.os.Build
import java.io.IOException
import java.util.Locale


@Composable
fun RequestLocationPermission(onGrantedChange: (Boolean) -> Unit) {
    var permissionGranted by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        permissionGranted = granted
        onGrantedChange(granted)
    }

    LaunchedEffect(Unit) {
        launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    if (!permissionGranted) {
        Text("Location permission required for showing nearby parking.")
    }
}

@SuppressLint("MissingPermission")
suspend fun fetchLastKnownLocation(context: Context): Point? {
    val client = LocationServices.getFusedLocationProviderClient(context)
    val loc = client.lastLocation.await()
    return loc?.let { Point.fromLngLat(it.longitude, it.latitude) }
}

@SuppressLint("MissingPermission")
fun startLocationUpdates(context: Context, onLocationUpdate: (Point) -> Unit): FusedLocationProviderClient {
    val client = LocationServices.getFusedLocationProviderClient(context)
    val req = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L).build()
    val callback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let { loc ->
                onLocationUpdate(Point.fromLngLat(loc.longitude, loc.latitude))
            }
        }
    }
    client.requestLocationUpdates(req, callback, null)
    return client
}

// In package com.example.parkwise.util
// ... existing imports ...

// ... existing functions ...

/**
 * Converts latitude and longitude to a human-readable address (city and province/country).
 * This uses the Android Geocoder and should be called off the main thread.
 */
@Suppress("DEPRECATION")
suspend fun reverseGeocode(context: Context, point: Point): String? {
    // Geocoder needs to be run off the main thread, which we are in via a coroutine.
    return try {
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // New Geocoding API for Tiramisu and above
            geocoder.getFromLocation(point.latitude(), point.longitude(), 1)
        } else {
            // Deprecated API for older versions
            geocoder.getFromLocation(point.latitude(), point.longitude(), 1)
        }

        addresses?.firstOrNull()?.let { address ->
            val city = address.locality ?: address.subAdminArea
            val area = address.adminArea ?: address.countryName

            if (city != null && area != null) {
                "$city, $area"
            } else if (area != null) {
                area
            } else {
                // Fallback to coordinates if address is incomplete
                null
            }
        }
    } catch (e: IOException) {
        // Handle network or I/O errors (e.g., no internet connection)
        e.printStackTrace()
        null
    }
}
