package com.example.parkwise.model

import com.google.firebase.database.Exclude // Must be the correct import for RTDB

data class UserProfile(
    val userId: String? = null,
    val name: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val vehicle: String? = null,
    val walletBalance: Double = 0.0,
    val prefersEv: Boolean? = null,
    val profileImageUrl: String? = null,
    val status: String = "Guest",
    val identityVerificationPercentage: Int = 0,
    val language: String = "English",
    val carName: String? = null,
    val carType: String? = null,
    val fuelType: String? = null,
    val carNumberPlate: String? = null,
    val dateOfBirth: String? = null, // 🟢 Field for Date of Birth
    val gender: String? = null,      // 🟢 Field for Gender
    // 🟢 NEW: Field to store the Base64 string of the generated car image
    val generatedCarImageBase64: String? = null
) {
    // REQUIRED no-argument constructor for RTDB Deserialization
    @Suppress("unused")
    constructor() : this(name = "Loading...")

    // --- Profile Completion Logic ---

    // Simplified list of properties used to calculate completion
    private val completionFields: List<Any?>
        get() = listOf(
            name,
            email,
            phone,
            carName,
            carType,
            fuelType,
            carNumberPlate,
            profileImageUrl,
            dateOfBirth,
            gender
            // NOTE: Generated image is not considered a required completion field
        )

    /**
     * Calculates the percentage of required profile fields that have been completed.
     * This property is excluded from Firebase saving.
     */
    val profileCompletionPercentage: Int
        @Exclude // 🟢 Exclude this getter from being saved to RTDB
        get() {
            val completedCount = completionFields.count { value ->
                when (value) {
                    is String -> value.isNotBlank()
                    is Boolean -> true // If a boolean is required, it counts as complete once initialized
                    null -> false
                    else -> true
                }
            }

            val totalFields = completionFields.size
            if (totalFields == 0) return 0

            return ((completedCount.toFloat() / totalFields.toFloat()) * 100).toInt()
        }

    /**
     * Helper function for the repository to get the completion percentage to save to DB.
     * This function is needed because RTDB does not automatically save @get:Exclude properties.
     */
    fun getCompletionToSave(): Int = profileCompletionPercentage
}
