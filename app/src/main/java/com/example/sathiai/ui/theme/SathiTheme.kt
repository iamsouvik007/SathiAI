package com.example.sathiai.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ─── Premium AI Colour Palette ────────────────────────────────────────────────
val DeepBlack     = Color(0xFF020408)
val MidnightBlue  = Color(0xFF0A0C14)
val SurfaceGlass  = Color(0x99121420)
val AccentPrimary = Color(0xFF7B61FF) // Futuristic Violet
val AccentSecond  = Color(0xFF4E9FFF) // Electric Blue
val AccentGlow    = Color(0x337B61FF)
val TextPrimary   = Color(0xFFF0F2FF)
val TextSecondary = Color(0xFF8E92B0)
val TextMuted     = Color(0xFF4A5070)

private val SathiColorScheme = darkColorScheme(
    primary = AccentPrimary,
    onPrimary = Color.White,
    secondary = AccentSecond,
    background = DeepBlack,
    surface = MidnightBlue,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceGlass,
    onSurfaceVariant = TextSecondary
)

// ─── Typography ───────────────────────────────────────────────────────────────
val SathiTypography = Typography(
    headlineLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        letterSpacing = (-0.5).sp,
        color = TextPrimary
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        letterSpacing = 0.sp,
        color = TextPrimary
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        letterSpacing = 0.sp,
        color = TextPrimary
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
        color = TextPrimary
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp,
        color = TextSecondary
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
        color = TextMuted
    )
)

@Composable
fun SathiTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SathiColorScheme,
        typography = SathiTypography,
        content = content
    )
}