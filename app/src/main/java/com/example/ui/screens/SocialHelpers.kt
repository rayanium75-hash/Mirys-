package com.example.ui.screens

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

fun getAvatarText(name: String): String {
    return if (name.length >= 2) {
        name.substring(0, 2).uppercase()
    } else if (name.isNotEmpty()) {
        name.uppercase()
    } else {
        "?"
    }
}

fun getTierColor(tier: String?): Color {
    return when (tier) {
        "Elite" -> Color(0xFFD32F2F)
        "Viral" -> Color(0xFFF57C00)
        "Engageant" -> Color(0xFF388E3C)
        "Standard" -> Color(0xFF1976D2)
        else -> Color(0xFF616161)
    }
}

fun getTierEmojiAndText(tier: String?): String {
    return when (tier) {
        "Elite" -> "🔥 Élite"
        "Viral" -> "🚀 Viral"
        "Engageant" -> "✨ Engageant"
        "Standard" -> "📊 Standard"
        else -> "⚪ Basique"
    }
}

fun getBadgeBorderBrush(badgeName: String): Brush {
    return when (badgeName) {
        "Créateur 👑" -> Brush.sweepGradient(listOf(Color(0xFFFFD700), Color(0xFFFFA500), Color(0xFFFFD700)))
        "Légende 🏆" -> Brush.linearGradient(listOf(Color(0xFFE91E63), Color(0xFF9C27B0)))
        "Aura Pro ✨" -> Brush.linearGradient(listOf(Color(0xFF00BCD4), Color(0xFF2196F3)))
        else -> Brush.linearGradient(listOf(Color.Gray, Color.LightGray))
    }
}
