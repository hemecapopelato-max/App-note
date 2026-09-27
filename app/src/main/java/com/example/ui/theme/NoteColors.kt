package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

data class NoteColorOption(
    val key: String,
    val name: String,
    val lightColor: Color,
    val darkColor: Color
)

object NoteColors {
    val options = listOf(
        NoteColorOption("DEFAULT", "Default", NoteColorDefault, NoteColorDefault),
        NoteColorOption("AMBER", "Amber", NoteColorAmber, NoteColorDarkAmber),
        NoteColorOption("MINT", "Mint", NoteColorMint, NoteColorDarkMint),
        NoteColorOption("SKY", "Sky", NoteColorSky, NoteColorDarkSky),
        NoteColorOption("ROSE", "Rose", NoteColorRose, NoteColorDarkRose),
        NoteColorOption("VIOLET", "Violet", NoteColorViolet, NoteColorDarkViolet),
        NoteColorOption("PEACH", "Peach", NoteColorPeach, NoteColorDarkPeach),
        NoteColorOption("EMERALD", "Emerald", NoteColorEmerald, NoteColorDarkEmerald)
    )

    @Composable
    fun getBackgroundColor(key: String, isDark: Boolean): Color {
        val option = options.find { it.key == key } ?: options.first()
        return if (option.key == "DEFAULT") {
            MaterialTheme.colorScheme.surfaceVariant
        } else {
            if (isDark) option.darkColor else option.lightColor
        }
    }

    fun getSwatchColor(key: String): Color {
        val option = options.find { it.key == key } ?: options.first()
        return when (option.key) {
            "DEFAULT" -> Color(0xFF9E9E9E)
            "AMBER" -> Color(0xFFF59E0B)
            "MINT" -> Color(0xFF10B981)
            "SKY" -> Color(0xFF0284C7)
            "ROSE" -> Color(0xFFF43F5E)
            "VIOLET" -> Color(0xFF8B5CF6)
            "PEACH" -> Color(0xFFF97316)
            "EMERALD" -> Color(0xFF059669)
            else -> Color(0xFF9E9E9E)
        }
    }
}
