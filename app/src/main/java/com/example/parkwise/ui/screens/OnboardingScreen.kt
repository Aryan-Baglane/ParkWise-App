package com.example.parkwise.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(onAuthStart: () -> Unit) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Welcome to ParkWise", style = MaterialTheme.typography.headlineLarge)
                Text("Find the perfect parking spot with ease. Our app helps you locate, book, and pay for parking instantly. Learn more about EV charging and dynamic pricing.")
            }

            Button(
                onClick = onAuthStart,
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("Get Started")
            }
        }
    }
}