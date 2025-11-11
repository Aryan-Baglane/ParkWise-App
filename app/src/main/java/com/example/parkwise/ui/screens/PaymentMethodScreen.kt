package com.example.parkwise.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.parkwise.R
import com.example.parkwise.data.CreditCard // Import the new data class
import androidx.compose.ui.geometry.Offset

// Aspect Ratio for Credit Card (85.6mm / 53.98mm ≈ 1.586)
private const val CR80_ASPECT_RATIO = 1.586f

data class PaymentOption(
    val title: String,
    val iconId: Int,
    val isAvailable: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentMethodScreen(onBack: () -> Unit) {
    var selectedOption by remember { mutableStateOf("Credit Card") }

    // --- Card Data ---
    val cards = listOf(
        CreditCard(
            lastFour = "9012",
            expiry = "12/26",
            name = "PRINCE KUMAR",
            type = "VISA",
            gradientStartColor = Color(0xFF141F32), // Dark Blue/Black
            gradientEndColor = Color(0xFF43587A),
            isPrimary = true
        ),
        CreditCard(
            lastFour = "5501",
            expiry = "08/25",
            name = "P. KUMAR",
            type = "MasterCard",
            gradientStartColor = Color(0xFFC70039), // Red
            gradientEndColor = Color(0xFFFF5733),
            isPrimary = false
        ),
        CreditCard(
            lastFour = "0033",
            expiry = "01/27",
            name = "PRINCE K.",
            type = "AMEX",
            gradientStartColor = Color(0xFF0D6EFD), // Blue
            gradientEndColor = Color(0xFF00C0FF),
            isPrimary = false
        )
    )
    // --- End Card Data ---


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Payment method") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            Button(
                onClick = { /* Handle Add action */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                Text("Add", style = MaterialTheme.typography.titleMedium)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // 1. Horizontal Scrollable Card List
            LazyRow(
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(cards) { card ->
                    CreditCardComposable(card = card)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2. 'Add new card' label
            Text(
                text = "Add new card",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Payment Option List
            PaymentOptionList(selectedOption = selectedOption, onOptionSelected = { selectedOption = it })
        }
    }
}

// =================================================================
// === NEW: Reusable Credit Card Composable with Ratio, Gradient, and Shadow ===
// =================================================================
@Composable
fun CreditCardComposable(card: CreditCard) {
    Card(
        modifier = Modifier
            .width(320.dp) // Fixed width for visibility
            .aspectRatio(CR80_ASPECT_RATIO) // Enforce the 1.586:1 credit card ratio
            .clip(RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent), // Use Transparent to show the gradient background
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp) // High elevation for prominent shadow
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(card.gradientStartColor, card.gradientEndColor),
                        // --- FIX: Use Offset constants instead of Alignment ---
                        start = Offset.Zero, // Corresponds to TopStart (0, 0)
                        end = Offset.Infinite  // Corresponds to BottomEnd (Max X, Max Y)
                        // -----------------------------------------------------
                    )
                )
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Row: Card Type (VISA) and Indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        card.type,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    // Primary Indicator (if needed)
                    if (card.isPrimary) {
                        Text(
                            "PRIMARY",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Yellow,
                            modifier = Modifier.background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(4.dp)).padding(horizontal = 4.dp)
                        )
                    }
                }

                // Card Number (Hidden part shown as dots, last four visible)
                Text(
                    text = "•••• •••• •••• ${card.lastFour}",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                // Bottom Row: Name and Expiry
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(card.name, style = MaterialTheme.typography.bodyMedium, color = Color.White)
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Expires", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                        Text(card.expiry, style = MaterialTheme.typography.bodyMedium, color = Color.White)
                    }
                }
            }
        }
    }
}
// =================================================================


// --- Helper Composable 2: Payment Option List (unchanged) ---
@Composable
fun PaymentOptionList(selectedOption: String, onOptionSelected: (String) -> Unit) {
    val options = remember {
        listOf(
            PaymentOption("Credit Card", R.drawable.credit_card_icon, true),
            PaymentOption("Paypal", R.drawable.paypal_icon, true),
            PaymentOption("Apple Pay", R.drawable.apple_pay_icon, true),
            PaymentOption("Google Pay", R.drawable.google_pay_icon, true)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        options.forEach { option ->
            PaymentItem(
                title = option.title,
                icon = painterResource(id = option.iconId),
                isAvailable = option.isAvailable,
                isSelected = option.title == selectedOption,
                onClick = { onOptionSelected(option.title) }
            )
            Divider(color = MaterialTheme.colorScheme.surfaceContainerHigh, thickness = 1.dp, modifier = Modifier.padding(horizontal = 8.dp))
        }
    }
}

// --- Helper Composable 3: Payment Item (unchanged) ---
@Composable
fun PaymentItem(
    title: String,
    icon: Painter,
    isAvailable: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick, enabled = isAvailable)
            .padding(horizontal = 8.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = icon,
                contentDescription = title,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isAvailable) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }

        // Right side icon (using R.drawable.circle as requested)
        Icon(
            painter = painterResource(id = R.drawable.circle),
            contentDescription = null,
            tint = if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
            modifier = Modifier.size(20.dp)
        )
    }
}

// --- Preview (Optional) ---
@Preview(showBackground = true)
@Composable
fun PreviewPaymentMethodScreen() {
    MaterialTheme {
        PaymentMethodScreen(onBack = {})
    }
}