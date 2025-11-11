package com.example.carimagegenerator

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.IOException
import com.example.parkwise.data.repository.ProfileRepository
import com.example.parkwise.model.UserProfile

// --- 1. Data Classes for API Request/Response (Using Gson) ---
data class Part(val text: String)
data class Content(val parts: List<Part>)
data class GenerationConfig(val responseModalities: List<String>)
data class GeminiRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig
)
data class InlineData(val mimeType: String, val data: String)
data class ImagePart(val inlineData: InlineData)
data class CandidateContent(val parts: List<ImagePart>)
data class Candidate(val content: CandidateContent)
data class GeminiResponse(val candidates: List<Candidate>)

// --- 2. API Service Layer ---
// NOTE: Replace with your actual API Key. In a real app, this should be secured.
const val GEMINI_API_KEY = "AIzaSyAKLxPRU0tJadcj10s9Au3wBAWTJGzipY4" // <-- Your API KEY
const val API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-image-preview:generateContent?key=$GEMINI_API_KEY"
const val MODEL_NAME = "gemini-2.5-flash-image"
val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

class CarImageGeneratorApi {
    private val client = OkHttpClient()
    private val gson = Gson()
    suspend fun generateImage(prompt: String): String? {
        Log.d("GeminiAPI", "Preparing API request with prompt: $prompt")

        val requestBody = GeminiRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(responseModalities = listOf("TEXT", "IMAGE"))
        )
        val jsonBody = gson.toJson(requestBody)

        val request = Request.Builder()
            .url(API_URL)
            .post(jsonBody.toRequestBody(JSON_MEDIA_TYPE))
            .build()

        return try {
            val response = client.newCall(request).execute()
            Log.d("GeminiAPI", "API call executed. Response code: ${response.code}")

            if (!response.isSuccessful) {
                Log.e("GeminiAPI", "API call failed: ${response.code} - ${response.message}. Response body: ${response.body?.string()}")
                return null
            }
            val responseBody = response.body?.string()
            if (responseBody.isNullOrEmpty()) {
                Log.e("GeminiAPI", "API returned empty response body.")
                return null
            }

            val geminiResponse = gson.fromJson(responseBody, GeminiResponse::class.java)

            // Safely extract the first inline image data
            val imageData = geminiResponse.candidates
                .firstOrNull()
                ?.content
                ?.parts
                ?.firstOrNull { it.inlineData != null && it.inlineData.data.isNotEmpty() }
                ?.inlineData
                ?.data

            if (imageData != null) {
                Log.d("GeminiAPI", "Image data successfully extracted from response. Size: ${imageData.length} bytes.")
            } else {
                Log.e("GeminiAPI", "Could not find image data in API response candidates.")
            }
            return imageData
        } catch (e: IOException) {
            Log.e("GeminiAPI", "Network error during image generation", e)
            null
        } catch (e: Exception) {
            Log.e("GeminiAPI", "Error parsing API response: ${e.message}", e)
            null
        }
    }
}

// --- 3. ViewModel for State Management and Business Logic (UPDATED) ---

// Defining a structure to hold car details for the UI/Generator (simulating extraction from UserProfile)
data class CarDetailsState(
    val maker: String = "Loading...",
    val model: String = "Loading...",
    val year: String = "Loading..."
)

class MainViewModel(
    // NOTE: This ViewModel assumes a User ID is available upon creation.
    private val userId: String = "placeholder_user_id", // Replace with real UID from Auth
    private val profileRepository: ProfileRepository = ProfileRepository()
) : ViewModel() {

    private val api = CarImageGeneratorApi()

    // State to hold the full, latest UserProfile object for updates
    private val _currentProfile = MutableStateFlow(UserProfile(generatedCarImageBase64 = null))

    // State to hold the fetched car details
    private val _carDetails = MutableStateFlow(CarDetailsState())
    val carDetails: StateFlow<CarDetailsState> = _carDetails

    // State to hold the generated image bitmap
    private val _generatedImageBitmap = MutableStateFlow<Bitmap?>(null)
    val generatedImageBitmap: StateFlow<Bitmap?> = _generatedImageBitmap

    // State for loading indicator
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // State for error messages
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        Log.d("MainViewModel", "ViewModel initialized. Starting profile fetch for UID: $userId")
        fetchCarDetailsFromDB()
    }

    /**
     * Subscribes to the UserProfile flow to fetch the user's car details.
     */
    private fun fetchCarDetailsFromDB() {
        viewModelScope.launch {
            try {
                profileRepository.getUserProfileFlow(userId).collect { userProfile ->
                    Log.d("MainViewModel", "Profile update received. Car Name: ${userProfile.carName}, Image Status: ${if (userProfile.generatedCarImageBase64 != null) "SAVED" else "MISSING"}")

                    _currentProfile.value = userProfile // Store the latest profile

                    val (maker, model, year) = parseCarName(userProfile.carName)

                    _carDetails.update {
                        it.copy(maker = maker, model = model, year = year)
                    }
                    Log.d("MainViewModel", "Car Details parsed: Maker=$maker, Model=$model, Year=$year")

                    // Also check if an image is already saved in the profile and load it
                    if (userProfile.generatedCarImageBase64 != null) {
                        val existingBitmap = base64ToBitmap(userProfile.generatedCarImageBase64)
                        _generatedImageBitmap.value = existingBitmap
                        if (existingBitmap != null) {
                            _errorMessage.value = "Image loaded from your profile!"
                            Log.d("MainViewModel", "Existing image successfully decoded and loaded.")
                        } else {
                            Log.e("MainViewModel", "Existing Base64 failed to decode.")
                        }
                    } else {
                        _generatedImageBitmap.value = null
                    }
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "DB Error during profile flow: ${e.message}", e)
                _errorMessage.value = "DB Error: Could not fetch profile or access car data. Showing default car."
                _carDetails.update {
                    it.copy(maker = "Hyundai", model = "Creta", year = "2025")
                }
            }
        }
    }

    /**
     * Attempts to parse a combined car name string into Maker, Model, and Year.
     */
    private fun parseCarName(carName: String?): Triple<String, String, String> {
        val defaultMaker = "Hyundai"
        val defaultModel = "Creta"
        val defaultYear = "2025"

        val parts = carName?.split(" ")?.filter { it.isNotBlank() } ?: emptyList()

        return when (parts.size) {
            4 -> Triple("${parts[0]} ${parts[1]}", parts[2], parts[3])
            3 -> Triple(parts[0], parts[1], parts[2])
            else -> {
                Log.w("MainViewModel", "Car name format invalid, using defaults.")
                Triple(defaultMaker, defaultModel, defaultYear)
            }
        }
    }

    /**
     * Triggers image generation using the car details loaded from the database state.
     */
    fun generateCarImage() {
        val details = _carDetails.value
        Log.d("MainViewModel", "Attempting image generation for: ${details.maker} ${details.model} ${details.year}")

        if (GEMINI_API_KEY.isEmpty()) {
            _errorMessage.value = "Error: Please set your GEMINI_API_KEY in MainActivity.kt."
            Log.e("MainViewModel", "API Key is missing.")
            return
        }
        if (details.maker.isBlank() || details.model.isBlank() || details.year.isBlank() || details.maker == "Loading...") {
            _errorMessage.value = "Car details are still loading or missing. Please wait or update your profile."
            Log.w("MainViewModel", "Car details missing: $details")
            return
        }

        val prompt = "A photorealistic, highly detailed CGI render of a ${details.year} ${details.maker} ${details.model} car. " +
                "Captured from a three-quarter front view (showing the side and front). " +
                "The car is the sole subject, perfectly isolated on a **pure, seamless white background** (infinity cyclorama). " +
                "Studio lighting, professional automotive photography quality, 8K, centered."

        Log.d("MainViewModel", "Full Prompt: $prompt")
        _isLoading.value = true
        _errorMessage.value = null
        _generatedImageBitmap.value = null

        viewModelScope.launch(Dispatchers.IO) {
            val base64Image = api.generateImage(prompt)

            launch(Dispatchers.Main) {
                _isLoading.value = false
                if (base64Image != null) {
                    val bitmap = base64ToBitmap(base64Image)
                    if (bitmap != null) {
                        _generatedImageBitmap.value = bitmap
                        Log.d("MainViewModel", "Image successfully generated and decoded to Bitmap.")

                        val base64ToSave = bitmapToBase64(bitmap)
                        val currentProfile = _currentProfile.value

                        val updatedProfile = currentProfile
                            .copy(generatedCarImageBase64 = base64ToSave)

                        viewModelScope.launch(Dispatchers.IO) {
                            Log.d("MainViewModel", "Saving new image Base64 to profile.")
                            val result = profileRepository.updateUserProfile(updatedProfile)
                            if (result.isSuccess) {
                                Log.d("MainViewModel", "Generated image successfully saved to profile.")
                                _errorMessage.value = "Image generated and saved to your profile!"
                            } else {
                                Log.e("MainViewModel", "Failed to save image to profile: ${result.exceptionOrNull()}")
                                _errorMessage.value = "Image generated, but failed to save to profile."
                            }
                        }

                    } else {
                        _errorMessage.value = "Failed to decode the image data."
                        Log.e("MainViewModel", "Failed to convert API Base64 response to Bitmap.")
                    }
                } else {
                    _errorMessage.value = "Image generation failed. Check logs for details."
                    Log.e("MainViewModel", "API returned null or failed image data.")
                }
            }
        }
    }

    /**
     * Utility function to convert a Base64 string (from API) to a Bitmap object.
     */
    private fun base64ToBitmap(base64String: String): Bitmap? {
        return try {
            val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: IllegalArgumentException) {
            Log.e("Base64Decoder", "Invalid Base64 string during decode: ${e.message}")
            null
        }
    }

    /**
     * Utility function to convert a Bitmap to a Base64 string for saving to RTDB.
     */
    private fun bitmapToBase64(bitmap: Bitmap): String? {
        return try {
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
        } catch (e: Exception) {
            Log.e("Base64Encoder", "Error encoding bitmap to Base64: ${e.message}", e)
            null
        }
    }
}