package com.example.parkwise.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.parkwise.model.ParkingArea
import com.mapbox.geojson.Point
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class MapViewModel : ViewModel() {

    private val _parkingAreas = MutableStateFlow(FULL_PARKING_DATA)
    val parkingAreas: StateFlow<List<ParkingArea>> = _parkingAreas.asStateFlow()

    private val _userLocation = MutableStateFlow<Point?>(null)
    val userLocation: StateFlow<Point?> = _userLocation.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _roadDistances = MutableStateFlow<Map<Int, Double>>(emptyMap())
    val roadDistances: StateFlow<Map<Int, Double>> = _roadDistances.asStateFlow()

    private val _roadDurations = MutableStateFlow<Map<Int, Double>>(emptyMap())
    val roadDurations: StateFlow<Map<Int, Double>> = _roadDurations.asStateFlow()

    private val client = OkHttpClient()

    fun updateUserLocation(point: Point) {
        _userLocation.value = point
        fetchRoadDistancesAndTimes(point, _parkingAreas.value)
    }
    private val _routeCoordinates = MutableStateFlow<List<Point>>(emptyList())
    val routeCoordinates: StateFlow<List<Point>> = _routeCoordinates.asStateFlow()



    fun fetchRoutePoints(userPt: Point, dest: Point) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val url = "https://api.mapbox.com/directions/v5/mapbox/driving/" +
                        "${userPt.longitude()},${userPt.latitude()};${dest.longitude()},${dest.latitude()}" +
                        "?geometries=geojson&access_token=pk.eyJ1IjoiYXJ5YW5iYWdsYW5lIiwiYSI6ImNtaDgyemF3NTBicmYyanFzeDM2aWc2anIifQ.GH4eeYtzOShfYGAY-iAWnA"

                val response = client.newCall(Request.Builder().url(url).build()).execute()
                val json = JSONObject(response.body?.string() ?: "")
                val coordinates = json.optJSONArray("routes")
                    ?.optJSONObject(0)
                    ?.optJSONObject("geometry")
                    ?.optJSONArray("coordinates")

                val points = mutableListOf<Point>()
                for (i in 0 until (coordinates?.length() ?: 0)) {
                    val coord = coordinates!!.getJSONArray(i)
                    val lon = coord.getDouble(0)
                    val lat = coord.getDouble(1)
                    points.add(Point.fromLngLat(lon, lat))
                }

                _routeCoordinates.emit(points)
            } catch (e: Exception) {
                Log.e("MapViewModel", "Failed to fetch route points: ${e.message}")
            }
        }
    }

    fun fetchRoadDistancesAndTimes(userPt: Point, parkingAreas: List<ParkingArea>) {
        viewModelScope.launch(Dispatchers.IO) {
            _loading.emit(true)

            val distances = mutableMapOf<Int, Double>()
            val durations = mutableMapOf<Int, Double>()

            for (area in parkingAreas) {
                try {
                    val url = "https://api.mapbox.com/directions/v5/mapbox/driving/" +
                            "${userPt.longitude()},${userPt.latitude()};${area.longitude},${area.latitude}" +
                            "?overview=false&access_token=pk.eyJ1IjoiYXJ5YW5iYWdsYW5lIiwiYSI6ImNtaDgyemF3NTBicmYyanFzeDM2aWc2anIifQ.GH4eeYtzOShfYGAY-iAWnA"

                    val response = client.newCall(Request.Builder().url(url).build()).execute()
                    val json = JSONObject(response.body?.string() ?: "")
                    val route = json.optJSONArray("routes")?.optJSONObject(0)
                    val distance = route?.optDouble("distance", Double.MAX_VALUE) ?: Double.MAX_VALUE
                    val duration = route?.optDouble("duration", Double.MAX_VALUE) ?: Double.MAX_VALUE

                    distances[area.id] = distance
                    durations[area.id] = duration

                } catch (e: Exception) {
                    Log.e("MapViewModel", "Mapbox directions failed: ${e.message}")
                    distances[area.id] = Double.MAX_VALUE
                    durations[area.id] = Double.MAX_VALUE
                }
            }

            _roadDistances.emit(distances)
            _roadDurations.emit(durations)
            _loading.emit(false)
        }
    }
}

private val FULL_PARKING_DATA = listOf(
    ParkingArea(
        1,
        "Select City Walk",
        "Saket, New Delhi",
        28.5244,
        77.2066,
        300,
        120,
        180,
        30,
        12,
        60.0,
        4.6,
        "24/7",
        listOf("Covered", "CCTV", "Valet")
    ), ParkingArea(
        2,
        "DLF Mall Parking",
        "Vasant Kunj, Delhi",
        28.5177,
        77.1583,
        250,
        80,
        170,
        25,
        10,
        55.0,
        4.4,
        "10:00-23:00",
        listOf("Covered", "Security", "Washroom")
    ), ParkingArea(
        3,
        "Pacific Mall Parking",
        "Subhash Nagar, Delhi",
        28.6398,
        77.1050,
        400,
        200,
        200,
        40,
        20,
        50.0,
        4.5,
        "24/7",
        listOf("Open Air", "CCTV", "Security")
    ), ParkingArea(
        4,
        "India Gate Parking",
        "Rajpath, New Delhi",
        28.6129,
        77.2295,
        150,
        10,
        140,
        15,
        2,
        40.0,
        4.2,
        "06:00-22:00",
        listOf("Open Air", "Paid Entry")
    ), ParkingArea(
        5,
        "Nehru Place Parking",
        "Nehru Place, Delhi",
        28.5494,
        77.2501,
        500,
        300,
        200,
        50,
        25,
        35.0,
        4.0,
        "24/7",
        listOf("Multi-level", "CCTV", "Security")
    ), ParkingArea(
        6,
        "Connaught Place Parking",
        "CP, New Delhi",
        28.6315,
        77.2167,
        200,
        50,
        150,
        0,
        8,
        70.0,
        4.7,
        "24/7",
        listOf("Underground", "CCTV", "Security", "Valet")
    ),


    ParkingArea(
        8,
        "Rohini Sector 8 Parking",
        "Rohini Sector 8, Delhi",
        28.7297,
        77.0954,
        200,
        80,
        120,
        0,
        0,
        35.0,
        4.1,
        "24/7",
        listOf("Open Air", "Security"),
        hasEV = false
    ), ParkingArea(
        9,
        "Rohini Sector 9 Parking",
        "Rohini Sector 9, Delhi",
        28.7362,
        77.0950,
        180,
        70,
        110,
        8,
        4,
        42.0,
        4.2,
        "24/7",
        listOf("Covered", "Valet"),
        hasEV = true
    ), ParkingArea(
        10,
        "Rohini Sector 10 Parking",
        "Rohini Sector 10, Delhi",
        28.7441,
        77.1021,
        220,
        90,
        130,
        0,
        0,
        30.0,
        3.9,
        "24/7",
        listOf("CCTV", "Security"),
        hasEV = false
    ), ParkingArea(
        11,
        "Rohini Sector 11 Parking",
        "Rohini Sector 11, Delhi",
        28.7434,
        77.1228,
        160,
        55,
        105,
        9,
        3,
        38.0,
        4.1,
        "24/7",
        listOf("Open Air"),
        hasEV = true
    ), ParkingArea(
        12,
        "Rohini Sector 12 Parking",
        "Rohini Sector 12, Delhi",
        28.7314,
        77.1360,
        210,
        85,
        125,
        0,
        0,
        40.0,
        4.0,
        "24/7",
        listOf("Covered", "CCTV"),
        hasEV = false
    ), ParkingArea(
        13,
        "Rohini Sector 13 Parking",
        "Rohini Sector 13, Delhi",
        28.7207,
        77.1428,
        190,
        60,
        130,
        11,
        5,
        44.0,
        4.3,
        "24/7",
        listOf("Security", "Valet"),
        hasEV = true
    ), ParkingArea(
        14,
        "Rohini Sector 14 Parking",
        "Rohini Sector 14, Delhi",
        28.7138,
        77.1302,
        170,
        65,
        105,
        0,
        0,
        36.0,
        3.8,
        "24/7",
        listOf("Open Air", "CCTV"),
        hasEV = false
    ), ParkingArea(
        15,
        "Rohini Sadar Bazar Parking",
        "Sadar Bazar, Rohini, Delhi",
        28.7060,
        77.1379,
        200,
        80,
        120,
        12,
        6,
        46.0,
        4.2,
        "10:00-22:00",
        listOf("Open Air", "Security"),
        hasEV = true
    ), ParkingArea(
        16,
        "Rohini Market Parking",
        "Rohini Market, Delhi",
        28.7054,
        77.1274,
        165,
        55,
        110,
        0,
        0,
        34.0,
        4.0,
        "10:00-22:00",
        listOf("Covered"),
        hasEV = false
    ), ParkingArea(
        17,
        "Shahbad Dairy Parking",
        "Shahbad Dairy, Rohini, Delhi",
        28.7947,
        77.0232,
        220,
        80,
        140,
        13,
        7,
        45.0,
        4.4,
        "24/7",
        listOf("CCTV", "Valet"),
        hasEV = true
    ), ParkingArea(
        18,
        "Rohini Sector 2 Parking",
        "Rohini Sector 2, Delhi",
        28.7123,
        77.1078,
        150,
        50,
        100,
        0,
        0,
        33.0,
        3.9,
        "24/7",
        listOf("Open Air"),
        hasEV = false
    ), ParkingArea(
        19,
        "Rohini Sector 3 Parking",
        "Rohini Sector 3, Delhi",
        28.7167,
        77.0954,
        185,
        70,
        115,
        10,
        4,
        41.0,
        4.2,
        "24/7",
        listOf("Security"),
        hasEV = true
    ), ParkingArea(
        20,
        "Rohini Sector 4 Parking",
        "Rohini Sector 4, Delhi",
        28.7223,
        77.0855,
        160,
        55,
        105,
        0,
        0,
        37.0,
        4.0,
        "24/7",
        listOf("CCTV", "Valet"),
        hasEV = false
    ), ParkingArea(
        21,
        "Rohini Sector 5 Parking",
        "Rohini Sector 5, Delhi",
        28.7350,
        77.0920,
        170,
        60,
        110,
        9,
        5,
        39.0,
        4.1,
        "24/7",
        listOf("Covered"),
        hasEV = true
    ), ParkingArea(
        22,
        "Rohini Sector 6 Parking",
        "Rohini Sector 6, Delhi",
        28.7405,
        77.0980,
        210,
        90,
        120,
        0,
        0,
        35.0,
        3.8,
        "24/7",
        listOf("Open Air", "Security"),
        hasEV = false
    ), ParkingArea(
        23,
        "Rohini Sector 15 Parking",
        "Rohini Sector 15, Delhi",
        28.7501,
        77.1150,
        190,
        70,
        120,
        11,
        6,
        42.0,
        4.3,
        "24/7",
        listOf("CCTV"),
        hasEV = true
    ), ParkingArea(
        24,
        "Rohini Sector 16 Parking",
        "Rohini Sector 16, Delhi",
        28.7536,
        77.1210,
        180,
        65,
        115,
        0,
        0,
        33.0,
        3.9,
        "24/7",
        listOf("Covered"),
        hasEV = false
    ), ParkingArea(
        25,
        "Rohini Sector 17 Parking",
        "Rohini Sector 17, Delhi",
        28.7580,
        77.1320,
        220,
        85,
        135,
        14,
        7,
        48.0,
        4.5,
        "24/7",
        listOf("Valet"),
        hasEV = true
    ), ParkingArea(
        26,
        "Rohini Sector 18 Parking",
        "Rohini Sector 18, Delhi",
        28.7620,
        77.1400,
        160,
        50,
        110,
        0,
        0,
        38.0,
        4.0,
        "24/7",
        listOf("Open Air"),
        hasEV = false
    )
)

