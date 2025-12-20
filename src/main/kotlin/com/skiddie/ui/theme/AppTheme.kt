package com.skiddie.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
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

@Immutable
data class TerminalColors(
    val stdout: Color,
    val stderr: Color,
    val stdin: Color,
    val system: Color,

    val ansiBlack: Color,
    val ansiRed: Color,
    val ansiGreen: Color,
    val ansiYellow: Color,
    val ansiBlue: Color,
    val ansiMagenta: Color,
    val ansiCyan: Color,
    val ansiWhite: Color,

    val ansiBrightBlack: Color,
    val ansiBrightRed: Color,
    val ansiBrightGreen: Color,
    val ansiBrightYellow: Color,
    val ansiBrightBlue: Color,
    val ansiBrightMagenta: Color,
    val ansiBrightCyan: Color,
    val ansiBrightWhite: Color,
)

private val GruvboxDarkTerminalColors = TerminalColors(
    stdout = Color(0xFFebdbb2),     // fg1 - default foreground
    stderr = Color(0xFFfb4934),     // red - errors
    stdin = Color(0xFFebdbb2),      // fg1 - will be dimmed with alpha
    system = Color(0xFF83a598),     // blue - system messages

    ansiBlack = Color(0xFF282828),
    ansiRed = Color(0xFFfb4934),
    ansiGreen = Color(0xFFb8bb26),
    ansiYellow = Color(0xFFfabd2f),
    ansiBlue = Color(0xFF83a598),
    ansiMagenta = Color(0xFFd3869b),
    ansiCyan = Color(0xFF8ec07c),
    ansiWhite = Color(0xFFebdbb2),

    ansiBrightBlack = Color(0xFF928374),
    ansiBrightRed = Color(0xFFfb4934),
    ansiBrightGreen = Color(0xFFb8bb26),
    ansiBrightYellow = Color(0xFFfabd2f),
    ansiBrightBlue = Color(0xFF83a598),
    ansiBrightMagenta = Color(0xFFd3869b),
    ansiBrightCyan = Color(0xFF8ec07c),
    ansiBrightWhite = Color(0xFFfbf1c7),
)

val LocalTerminalColors = staticCompositionLocalOf { GruvboxDarkTerminalColors }

val MaterialTheme.terminalColors: TerminalColors
    @Composable
    get() = LocalTerminalColors.current

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
    CompositionLocalProvider(LocalTerminalColors provides GruvboxDarkTerminalColors) {
        MaterialTheme(
            colorScheme = GruvboxDarkColorScheme,
            typography = AppTypography,
            content = content
        )
    }
}
