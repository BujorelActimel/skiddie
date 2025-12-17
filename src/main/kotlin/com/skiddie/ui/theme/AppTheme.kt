package com.skiddie.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val GruvboxDarkColorScheme = darkColorScheme(
    primary = Color(0xFF83a598),        // blue
    secondary = Color(0xFF8ec07c),      // aqua
    tertiary = Color(0xFFd3869b),       // purple
    background = Color(0xFF282828),     // bg0
    surface = Color(0xFF3c3836),        // bg1
    surfaceVariant = Color(0xFF504945), // bg2
    onPrimary = Color(0xFF282828),
    onSecondary = Color(0xFF282828),
    onTertiary = Color(0xFF282828),
    onBackground = Color(0xFFebdbb2),   // fg1
    onSurface = Color(0xFFebdbb2),      // fg1
    onSurfaceVariant = Color(0xFFd5c4a1), // fg2
    error = Color(0xFFfb4934)           // red
)

private val AppTypography = Typography(
    titleLarge = TextStyle(
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold
    ),

    titleMedium = TextStyle(
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold
    ),

    bodyLarge = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontSize = 18.sp,
        fontWeight = FontWeight.Medium
    ),

    bodyMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal
    )
)

@Composable
fun SkiddieTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = GruvboxDarkColorScheme,
        typography = AppTypography,
        content = content
    )
}
