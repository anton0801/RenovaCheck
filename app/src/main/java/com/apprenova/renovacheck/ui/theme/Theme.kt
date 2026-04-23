package com.apprenova.renovacheck.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ── Brand Colors ──────────────────────────────────────────────────────────────
val RenovaPrimary       = Color(0xFF1B4D3E)   // Deep forest green
val RenovaSecondary     = Color(0xFF2E7D5B)   // Medium green
val RenovaAccent        = Color(0xFF5CB88A)   // Mint accent
val RenovaGold          = Color(0xFFF5A623)   // Warm gold (pass/warning)
val RenovaRed           = Color(0xFFD93025)   // Alert red (fail/issue)
val RenovaSuccess       = Color(0xFF34A853)   // Success green
val RenovaSurface       = Color(0xFFF7FAF8)   // Off-white surface
val RenovaBackground    = Color(0xFFF0F5F2)   // Soft green-tinted bg
val RenovaCard          = Color(0xFFFFFFFF)
val RenovaTextPrimary   = Color(0xFF0D1F17)
val RenovaTextSecondary = Color(0xFF5A7066)
val RenovaDivider       = Color(0xFFDDE8E3)
val RenovaGradientStart = Color(0xFF1B4D3E)
val RenovaGradientEnd   = Color(0xFF2E7D5B)

// Dark palette
val RenovaDarkPrimary       = Color(0xFF5CB88A)
val RenovaDarkSecondary     = Color(0xFF3D9E72)
val RenovaDarkBackground    = Color(0xFF0D1F17)
val RenovaDarkSurface       = Color(0xFF162B20)
val RenovaDarkCard          = Color(0xFF1E3828)
val RenovaDarkTextPrimary   = Color(0xFFE8F5EE)
val RenovaDarkTextSecondary = Color(0xFF7FB89A)

// Severity Colors
val SeverityCritical = Color(0xFFD93025)
val SeverityHigh     = Color(0xFFFF6D00)
val SeverityMedium   = Color(0xFFF5A623)
val SeverityLow      = Color(0xFF34A853)

// ── Typography ────────────────────────────────────────────────────────────────
val RenovaTypography = Typography(
    displayLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.5).sp
    ),
    displayMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.25).sp
    ),
    displaySmall = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp
    ),
    headlineLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    headlineSmall = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.5.sp
    )
)

// ── Light Color Scheme ────────────────────────────────────────────────────────
private val LightColorScheme = lightColorScheme(
    primary           = RenovaPrimary,
    onPrimary         = Color.White,
    primaryContainer  = Color(0xFFB8E8D0),
    onPrimaryContainer= RenovaPrimary,
    secondary         = RenovaSecondary,
    onSecondary       = Color.White,
    secondaryContainer= Color(0xFFCCEEDE),
    onSecondaryContainer = RenovaSecondary,
    tertiary          = RenovaGold,
    onTertiary        = Color.White,
    background        = RenovaBackground,
    onBackground      = RenovaTextPrimary,
    surface           = RenovaCard,
    onSurface         = RenovaTextPrimary,
    surfaceVariant    = Color(0xFFE8F2EC),
    onSurfaceVariant  = RenovaTextSecondary,
    outline           = RenovaDivider,
    error             = RenovaRed,
    onError           = Color.White,
)

private val DarkColorScheme = darkColorScheme(
    primary           = RenovaDarkPrimary,
    onPrimary         = RenovaDarkBackground,
    primaryContainer  = Color(0xFF1E3828),
    onPrimaryContainer= RenovaDarkPrimary,
    secondary         = RenovaDarkSecondary,
    onSecondary       = RenovaDarkBackground,
    background        = RenovaDarkBackground,
    onBackground      = RenovaDarkTextPrimary,
    surface           = RenovaDarkCard,
    onSurface         = RenovaDarkTextPrimary,
    surfaceVariant    = Color(0xFF1E3828),
    onSurfaceVariant  = RenovaDarkTextSecondary,
    outline           = Color(0xFF2E4A38),
    error             = Color(0xFFFF6B6B),
    onError           = Color.White,
)

@Composable
fun RenovaCheckTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = RenovaTypography,
        content     = content
    )
}
