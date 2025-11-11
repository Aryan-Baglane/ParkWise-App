package com.example.parkwise.util

// Define this class in a utility file or related package


import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.google.android.gms.auth.api.signin.GoogleSignIn


import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.GoogleAuthProvider

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

// Custom contract to handle Google Sign-In intent result
class GoogleSignInAuthContract : ActivityResultContract<Unit, AuthCredential?>() {

    override fun createIntent(context: Context, input: Unit): Intent {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            // Replace with your actual Web Client ID for Firebase Auth
            .requestIdToken("359471696021-csku4ofsq8o12cdq1d5qk84681u2jmd6.apps.googleusercontent.com")
            .requestEmail()
            .requestProfile()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(context, gso)
        return googleSignInClient.signInIntent
    }

    override fun parseResult(resultCode: Int, intent: Intent?): AuthCredential? {
        if (resultCode == Activity.RESULT_OK) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(intent)
            try {
                val account = task.getResult(ApiException::class.java)
                return GoogleAuthProvider.getCredential(account.idToken, null)
            } catch (e: ApiException) {
                // Log the error
                return null
            }
        }
        return null
    }
}