package com.example.parkwise.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.parkwise.R
import androidx.compose.ui.res.painterResource // Required for using R.drawable icons

// ---------- SHARED CONSTANTS (Inferred from previous code) ----------
private val FontFamilyDefault = FontFamily.SansSerif
private val TitleFontSize = 20.sp


// ---------- DATA STRUCTURE FOR NOTIFICATIONS ----------

data class NotificationItem(
    val id: Int,
    val title: String,
    val message: String,
    val type: NotificationType,
    val timestamp: String,
    val isRead: Boolean = false
)

enum class NotificationType(val iconResourceOrVector: Any, val color: Color) {
    // Note: We use 'Any' to hold both R.drawable (Int) and ImageVector
    PARKING(R.drawable.local_parking, Color(0xFF4CAF50)), // Green
    ALERT(Icons.Default.Info, Color(0xFFFF9800)),         // Orange/Amber
    BILLING(R.drawable.credit_card_icon, Color(0xFF2196F3))   // Blue - Assuming a credit card icon resource
}

// Placeholder Data for the Notifications Screen
val dummyNotifications = listOf(
    NotificationItem(
        1,
        "Parking Session Ended",
        "Your session at Siliwangi Plaza has ended. Amount charged: \$5.00.",
        NotificationType.PARKING,
        "2 hours ago"
    ),
    NotificationItem(
        2,
        "Low Wallet Balance",
        "Your wallet balance is below \$10. Consider topping up to avoid parking interruptions.",
        NotificationType.BILLING,
        "3 hours ago"
    ),
    NotificationItem(
        3,
        "New Parking Zone Alert",
        "A new EV parking zone has been activated near Cikole.",
        NotificationType.ALERT,
        "1 day ago",
        isRead = true
    ),
    NotificationItem(
        4,
        "System Maintenance",
        "Scheduled system maintenance tonight at 2 AM. Services may be briefly unavailable.",
        NotificationType.ALERT,
        "2 days ago"
    )
)

// --- Helper Data Classes (Kept for environment consistency) ---




// ---------- NOTIFICATION SCREEN COMPOSABLE ----------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    onBack: () -> Unit // Function to handle navigation back
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Notifications & Alerts",
                        fontWeight = FontWeight.Bold,
                        fontSize = TitleFontSize
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go Back"
                        )
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(dummyNotifications) { notification ->
                NotificationCard(notification = notification)
            }
            item {
                Spacer(modifier = Modifier.height(32.dp))
                // Simple footer/status
                Text(
                    "Showing ${dummyNotifications.size} recent notifications.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

// ---------- SUPPORTING COMPOSABLES ----------

@Composable
fun NotificationCard(notification: NotificationItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Handle notification tap/view */ },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(notification.type.color.copy(alpha = 0.1f))
                    .border(1.dp, notification.type.color, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                // Determine whether the icon is a Vector or a Resource ID
                when (val icon = notification.type.iconResourceOrVector) {
                    is ImageVector -> Icon(
                        icon,
                        contentDescription = notification.type.name,
                        tint = notification.type.color,
                        modifier = Modifier.size(24.dp)
                    )
                    is Int -> Icon(
                        painterResource(icon),
                        contentDescription = notification.type.name,
                        tint = notification.type.color,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Text content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    notification.title,
                    fontWeight = if (notification.isRead) FontWeight.SemiBold else FontWeight.Bold,
                    fontSize = SubTitleFontSize,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    notification.message,
                    fontSize = BodyFontSize,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }

            // Timestamp and Unread dot
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    notification.timestamp,
                    fontSize = 11.sp,
                    color = Color.Gray
                )
                if (!notification.isRead) {
                    Spacer(Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color.Red)
                    )
                }
            }
        }
    }
}


// Keeping these composables for completeness if they were part of the original file context
