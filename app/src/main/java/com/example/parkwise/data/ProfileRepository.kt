package com.example.parkwise.data.repository

import android.net.Uri
import android.util.Log
import com.example.parkwise.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ProfileRepository(
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private val usersRef = database.getReference("users")
    private val defaultPhotoUrl = "https://example.com/default_profile.png"

    /**
     * Creates initial user profile in RTDB.
     * Ensures that Google Sign-In photo or default photo is stored.
     */
    suspend fun createInitialUserProfile(user: FirebaseUser, googlePhotoUrl: String? = null) {
        val uid = user.uid
        val userProfileRef = usersRef.child(uid)

        try {
            val snapshot = userProfileRef.get().await()

            if (!snapshot.exists()) {
                // Force refresh FirebaseUser
                user.reload().await()

                val photoUrl = googlePhotoUrl ?: user.photoUrl?.toString() ?: defaultPhotoUrl
                val initialCompletion = UserProfile().profileCompletionPercentage

                val profileMap: Map<String, Any?> = mapOf(
                    "userId" to uid,
                    "email" to user.email,
                    "name" to (user.displayName ?: user.email?.substringBefore("@")),
                    "profileImageUrl" to photoUrl,
                    "status" to "Active",
                    "language" to "English",
                    "walletBalance" to 0.0,
                    "identityVerificationPercentage" to initialCompletion,
                    "phone" to null,
                    "vehicle" to null,
                    "prefersEv" to null,
                    "carName" to null,
                    "carType" to null,
                    "fuelType" to null,
                    "carNumberPlate" to null,
                    "dateOfBirth" to null,
                    "gender" to null,
                    // 🟢 NEW: Initialize generatedCarImageBase64 as null
                    "generatedCarImageBase64" to null
                )

                userProfileRef.updateChildren(profileMap).await()
                Log.d("ProfileRepository", "Profile created for $uid with photo $photoUrl")
            }
        } catch (e: Exception) {
            Log.e("ProfileRepository", "Error creating profile for $uid: ${e.message}")
        }
    }

    fun getUserProfileFlow(uid: String): Flow<UserProfile> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val profile = snapshot.getValue(UserProfile::class.java)
                val liveProfile = profile?.copy(
                    identityVerificationPercentage = profile.profileCompletionPercentage,
                    profileImageUrl = profile.profileImageUrl ?: defaultPhotoUrl
                ) ?: UserProfile(userId = uid, name = "Profile Missing", profileImageUrl = defaultPhotoUrl)
                trySend(liveProfile)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        usersRef.child(uid).addValueEventListener(listener)
        awaitClose { usersRef.child(uid).removeEventListener(listener) }
    }

    suspend fun updateUserProfile(profile: UserProfile): Result<Unit> {
        val uid = profile.userId ?: return Result.failure(IllegalStateException("User ID missing"))
        val photoUrl = profile.profileImageUrl ?: defaultPhotoUrl

        // Update Firebase Auth display name and photo
        val authResult = updateAuthProfile(profile.name ?: "", photoUrl)
        if (authResult.isFailure) return authResult

        val updates = mapOf(
            "name" to profile.name,
            "email" to profile.email,
            "phone" to profile.phone,
            "walletBalance" to profile.walletBalance,
            "prefersEv" to profile.prefersEv,
            "profileImageUrl" to photoUrl,
            "status" to profile.status,
            "language" to profile.language,
            "carName" to profile.carName,
            "carType" to profile.carType,
            "fuelType" to profile.fuelType,
            "carNumberPlate" to profile.carNumberPlate,
            "identityVerificationPercentage" to profile.getCompletionToSave(),
            "dateOfBirth" to profile.dateOfBirth,
            "gender" to profile.gender,
            // 🟢 NEW: Include the generated image Base64 string in the update map
            "generatedCarImageBase64" to profile.generatedCarImageBase64
        )

        return try {
            usersRef.child(uid).updateChildren(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun updateAuthProfile(name: String, imageUrl: String): Result<Unit> {
        val user = auth.currentUser ?: return Result.failure(IllegalStateException("User not logged in"))

        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(name)
            .setPhotoUri(Uri.parse(imageUrl))
            .build()

        return try {
            user.updateProfile(profileUpdates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
