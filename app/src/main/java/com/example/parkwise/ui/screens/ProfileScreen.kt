package com.example.parkwise.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.parkwise.R
import com.example.parkwise.model.UserProfile
import com.example.parkwise.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel = viewModel(),
    onNavigateToEditProfile: () -> Unit = {},
    onNavigateToPaymentMethods: () -> Unit = {},
    onNavigateToMyDetails: () -> Unit = {},
    onNavigateToVehicles: () -> Unit = {},
    onNavigateToPromoCodes: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToHelp: () -> Unit = {},
    onNavigateToFeedback: () -> Unit = {},
    onLogout: () -> Unit,
    onBack: () -> Unit
) {
    val profile by authViewModel.userProfile.collectAsState()
    val isAuthenticated by authViewModel.isUserAuthenticated.collectAsState()

    var showLogoutDialog by remember { mutableStateOf(false) }
    var shouldNavigateOut by remember { mutableStateOf(false) }

    // Logic to handle transient sign-out status (kept for completeness)
    LaunchedEffect(isAuthenticated) {
        if (!isAuthenticated) {
            delay(200)
            if (!isAuthenticated) {
                shouldNavigateOut = true
            }
        }
    }

    LaunchedEffect(shouldNavigateOut) {
        if (shouldNavigateOut) {
            onLogout()
        }
    }

    // Determine loading state: True if profile is placeholder AND user is authenticated
    val isLoading = profile.name == "Loading..." && FirebaseAuth.getInstance().currentUser != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Account") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        // 🟢 FIX: Conditionally render loading indicator or main content
        if (isLoading) {
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Profile Header Card
                ProfileHeaderCard(
                    profile = profile,
                    onEditClick = onNavigateToEditProfile
                )

                Spacer(modifier = Modifier.height(16.dp))

                // My Details
                MenuItemCard(
                    icon = Icons.Default.Person,
                    title = "My Details",
                    onClick = onNavigateToMyDetails
                )

                Spacer(modifier = Modifier.height(12.dp))

                // My Payment Methods
                MenuItemCard(
                    // NOTE: Assumes R.drawable.credit_card_icon exists
                    painter = painterResource(R.drawable.credit_card_icon),
                    title = "My Payment Methods",
                    onClick = onNavigateToPaymentMethods
                )

                Spacer(modifier = Modifier.height(12.dp))

                // My Vehicles
                MenuItemCard(
                    // NOTE: Assumes R.drawable.electric_car exists
                    painter = painterResource(R.drawable.electric_car),
                    title = "My Vehicles",
                    onClick = onNavigateToVehicles
                )

                Spacer(modifier = Modifier.height(12.dp))

                // My Promo Codes
                MenuItemCard(
                    // NOTE: Assumes R.drawable.discount exists
                    painter = painterResource(R.drawable.discount),
                    title = "My Promo Codes",
                    onClick = onNavigateToPromoCodes
                )

                Spacer(modifier = Modifier.height(12.dp))

                // My Notifications
                MenuItemCard(
                    icon = Icons.Default.Notifications,
                    title = "My Notifications",
                    onClick = onNavigateToNotifications
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Help and Support
                MenuItemCard(
                    // NOTE: Assumes R.drawable.headset exists
                    painter = painterResource(R.drawable.headset),
                    title = "Help and Support",
                    onClick = onNavigateToHelp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // App Feedback
                MenuItemCard(
                    // NOTE: Assumes R.drawable.feedback exists
                    painter = painterResource(R.drawable.feedback),
                    title = "App feedback",
                    onClick = onNavigateToFeedback
                )

                Spacer(modifier = Modifier.height(24.dp))

                LogoutButton(onShowDialog = { showLogoutDialog = true })
            }
        }
    }

    // Logout Dialog remains outside the Scaffold
    if (showLogoutDialog) {
        LogoutConfirmationDialog(
            onConfirmLogout = onLogout,
            onDismiss = { showLogoutDialog = false }
        )
    }
}

// --- Helper Composable Functions ---

@Composable
fun ProfileHeaderCard(profile: UserProfile, onEditClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEditClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile Image
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF8B9DFF)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = profile.profileImageUrl?.takeIf { it.isNotBlank() },
                    contentDescription = "Profile Picture",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(70.dp).clip(CircleShape),
                    placeholder = painterResource(R.drawable.profile_placeholder),
                    error = painterResource(R.drawable.profile_placeholder)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Profile Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = profile.name ?: "Guest User",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "@${profile.email?.substringBefore("@") ?: "user"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Verified Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFE8F5E9)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Verified",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Verified",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Arrow
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Edit Profile",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun MenuItemCard(
    painter: Painter? = null,
    icon: ImageVector? = null,
    title: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Background
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE8EAFF)),
                contentAlignment = Alignment.Center
            ) {
                when {
                    painter != null -> Icon(
                        painter = painter,
                        contentDescription = title,
                        tint = Color(0xFF5B6FE3),
                        modifier = Modifier.size(24.dp)
                    )
                    icon != null -> Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = Color(0xFF5B6FE3),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Navigate",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun LogoutButton(onShowDialog: () -> Unit) {
    Button(
        onClick = onShowDialog,
        modifier = Modifier
            .fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Red.copy(0.1f),
            contentColor = Color.Red
        )
    ){
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Logout",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            )
        }
    }
}

@Composable
fun LogoutConfirmationDialog(
    onConfirmLogout: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Confirm Logout", style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Text("Are you sure you want to log out of your account?")
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirmLogout()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("Yes, Logout")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        }
    )
}

// Dummy classes and placeholders for compilation (replace with your actual code)
data class UserProfile(
    val name: String? = "Loading...",
    val email: String? = null,
    val profileImageUrl: String? = null
)



