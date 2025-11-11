package com.example.parkwise.data



import androidx.compose.ui.graphics.Color

data class CreditCard(
    val lastFour: String,
    val expiry: String,
    val name: String,
    val type: String,
    val gradientStartColor: Color,
    val gradientEndColor: Color,
    val isPrimary: Boolean = false
)