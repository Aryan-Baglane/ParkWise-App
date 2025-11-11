package com.example.parkwise.nav

import android.annotation.SuppressLint
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.parkwise.model.BookingSummary
import com.example.parkwise.ui.screens.*
import com.example.parkwise.viewmodel.AuthViewModel
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@SuppressLint("UnusedContentLambdaTargetStateParameter")
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppNavGraph(
    authViewModel: AuthViewModel = viewModel()
) {
    val navController = rememberNavController()
    val isAuthenticated by authViewModel.isUserAuthenticated.collectAsState(initial = false)
    val startDestination = "splash_or_check"

    AnimatedContent(targetState = navController.currentBackStackEntry?.destination?.route) {
        NavHost(navController = navController, startDestination = startDestination) {

            // --- Splash / Auth Check ---
            composable("splash_or_check") {
                LaunchedEffect(isAuthenticated) {
                    if (isAuthenticated) {
                        navController.navigate("home") { popUpTo("splash_or_check") { inclusive = true } }
                    } else {
                        navController.navigate("onboarding") { popUpTo("splash_or_check") { inclusive = true } }
                    }
                }
                Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primary))
            }

            // --- Onboarding / Auth ---
            composable("onboarding") {
                OnboardingScreen(onAuthStart = { navController.navigate("auth") })
            }

            composable("auth") {
                AuthScreen(
                    onBack = { navController.popBackStack() },
                    onSignInSuccess = { navController.navigate("home") { popUpTo("onboarding") { inclusive = true } } },
                    viewModel = authViewModel
                )
            }

            // --- Home ---
            composable("home") {
                HomeScreen(
                    onOpenMap = { navController.navigate("map") },
                    onOpenProfile = { navController.navigate("profile") },
                    onOpenHistory = { navController.navigate("history") },
                    onOpenWallet = { navController.navigate("payment_method") },
                    // UPDATED: Link the notification bell to the notifications route
                    onOpenNotifications = { navController.navigate("notifications") }
                )
            }

            // --- Map / Layout / Booking ---
            composable("map") {
                MapSearchScreen(onParkingSelected = { parkingId ->
                    navController.navigate("layout/$parkingId")
                })
            }

            composable(
                "layout/{parkingId}",
                arguments = listOf(navArgument("parkingId") { type = NavType.IntType })
            ) { backStack ->
                val id = backStack.arguments?.getInt("parkingId") ?: -1
                ParkingLayoutScreen(
                    parkingId = id,
                    onBookSlot = { slotId -> navController.navigate("booking/$slotId") },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                "booking/{slotId}",
                arguments = listOf(navArgument("slotId") { type = NavType.IntType })
            ) { backStack ->
                val slotId = backStack.arguments?.getInt("slotId") ?: -1
                BookingScreen(slotId = slotId, onBookingSuccess = { bookingId ->
                    navController.navigate("qr/$bookingId") {
                        popUpTo("home") { inclusive = false }
                    }
                })
            }

            composable(
                "qr/{bookingId}",
                arguments = listOf(navArgument("bookingId") { type = NavType.StringType })
            ) { backStack ->
                val bookingId = backStack.arguments?.getString("bookingId") ?: ""
                QrConfirmationScreen(bookingId = bookingId, onBack = { navController.popBackStack() })
            }

            // --- History / Booking Details ---
            composable("history") {
                HistoryScreen(
                    onRebook = { parkingId -> navController.navigate("layout/$parkingId") },
                    onBookingClick = { booking ->
                        val json = URLEncoder.encode(Json.encodeToString(booking), StandardCharsets.UTF_8.name())
                        navController.navigate("booking_details/$json")
                    }
                )
            }

            composable(
                "booking_details/{bookingJson}",
                arguments = listOf(navArgument("bookingJson") { type = NavType.StringType })
            ) { backStackEntry ->
                val bookingJson = backStackEntry.arguments?.getString("bookingJson") ?: ""
                val booking = Json.decodeFromString<BookingSummary>(
                    URLDecoder.decode(bookingJson, StandardCharsets.UTF_8.name())
                )
                BookingDetailsScreen(
                    booking = booking,
                    onRebook = { parkingId -> navController.navigate("layout/$parkingId") },
                    onNavigate = { parkingId -> navController.navigate("layout/$parkingId") }
                )
            }

            // --- Profile & Settings ---
            composable("profile") {
                ProfileScreen(
                    authViewModel = authViewModel,
                    onNavigateToEditProfile = { navController.navigate("edit_profile") },
                    onNavigateToPaymentMethods = { navController.navigate("payment_method") },
                    onNavigateToMyDetails = { navController.navigate("my_details") },
                    // 🟢 FIX: Correctly implement navigation to the 'vehicles' route
                    onNavigateToVehicles = { navController.navigate("vehicles") },
                    onNavigateToPromoCodes = { navController.navigate("promo_codes") },
                    onNavigateToNotifications = { navController.navigate("notifications") },
                    onNavigateToHelp = { navController.navigate("help") },
                    onNavigateToFeedback = { navController.navigate("feedback") },
                    onLogout = {
                        authViewModel.logout()
                        navController.navigate("splash_or_check") {
                            popUpTo(navController.graph.id) { inclusive = true }
                        }
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            // --- Edit Profile Screen ---
            composable("edit_profile") {
                EditProfileScreen(
                    authViewModel = authViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // --- Payment Methods ---
            composable("payment_method") {
                PaymentMethodScreen(onBack = { navController.popBackStack() })
            }

            // --- My Details (Profile Details) ---
            composable("my_details") {
                // Reusing EditProfileScreen for profile details
                EditProfileScreen(
                    authViewModel = authViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // 🚗 Vehicle Details Screen (Replaced Placeholder) ---
            composable("vehicles") {
                // 🟢 FIX: Use VehicleDetailsScreen here
                VehicleDetailsScreen(
                    authViewModel = authViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            // --- Promo Codes Screen ---
            composable("promo_codes") {
                // Placeholder screen for now
                PlaceholderScreen(
                    title = "My Promo Codes",
                    onBack = { navController.popBackStack() }
                )
            }

            // --- Notifications Screen ---
            composable("notifications") {
                // UPDATED: Use the fully implemented NotificationScreen
                NotificationScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            // --- Help and Support Screen ---
            composable("help") {
                // Placeholder screen for now
                PlaceholderScreen(
                    title = "Help and Support",
                    onBack = { navController.popBackStack() }
                )
            }

            // --- App Feedback Screen ---
            composable("feedback") {
                // Placeholder screen for now
                PlaceholderScreen(
                    title = "App Feedback",
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

// Placeholder screen for unimplemented sections
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceholderScreen(
    title: String,
    onBack: () -> Unit
) {
    androidx.compose.material3.Scaffold(
        topBar = {
            androidx.compose.material3.TopAppBar(
                title = { androidx.compose.material3.Text(title) },
                navigationIcon = {
                    androidx.compose.material3.IconButton(onClick = onBack) {
                        androidx.compose.material3.Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            androidx.compose.material3.Text(
                text = "$title\n\nComing Soon...",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}