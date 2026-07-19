package com.hashfactory.game.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp

// Terminal CRT palette — the game is dark-only by design.
val TerminalGreen = Color(0xFF33FF66)
val TerminalGreenDim = Color(0xFF1E9944)
val TerminalAmber = Color(0xFFFFB000)
val TerminalRed = Color(0xFFFF4444)
val CrtBackground = Color(0xFF0A0E0A)
val CrtSurface = Color(0xFF111611)
val CrtSurfaceBright = Color(0xFF1A241A)
val CrtText = Color(0xFFB8E6C4)

private val ColorScheme = darkColorScheme(
    primary = TerminalGreen,
    onPrimary = Color(0xFF00220A),
    secondary = TerminalGreenDim,
    onSecondary = CrtText,
    background = CrtBackground,
    onBackground = CrtText,
    surface = CrtSurface,
    onSurface = CrtText,
    surfaceVariant = CrtSurfaceBright,
    onSurfaceVariant = CrtText,
    error = TerminalRed,
    outline = TerminalGreenDim,
)

private val Mono = FontFamily.Monospace

private val TerminalTypography = Typography(
    displayMedium = TextStyle(fontFamily = Mono, fontSize = 36.sp),
    headlineSmall = TextStyle(fontFamily = Mono, fontSize = 22.sp),
    titleMedium = TextStyle(fontFamily = Mono, fontSize = 16.sp),
    bodyLarge = TextStyle(fontFamily = Mono, fontSize = 15.sp),
    bodyMedium = TextStyle(fontFamily = Mono, fontSize = 13.sp),
    bodySmall = TextStyle(fontFamily = Mono, fontSize = 11.sp),
    labelLarge = TextStyle(fontFamily = Mono, fontSize = 14.sp),
    labelMedium = TextStyle(fontFamily = Mono, fontSize = 12.sp),
)

// The CRT look is intentionally identical in light and dark system themes.
@Composable
fun HashFactoryTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ColorScheme,
        typography = TerminalTypography,
        content = content,
    )
}
