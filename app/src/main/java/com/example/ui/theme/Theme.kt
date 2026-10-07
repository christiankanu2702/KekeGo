package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val TransitDarkColorScheme = darkColorScheme(
    primary = SafetyAmber,
    onPrimary = AsphaltDark,
    primaryContainer = SafetyAmberDark,
    onPrimaryContainer = HighContrastWhite,
    secondary = EmeraldGreen,
    onSecondary = AsphaltDark,
    secondaryContainer = EmeraldGreenDark,
    onSecondaryContainer = HighContrastWhite,
    background = AsphaltBlack,
    onBackground = HighContrastWhite,
    surface = AsphaltDark,
    onSurface = HighContrastWhite,
    surfaceVariant = AsphaltSurface,
    onSurfaceVariant = MutedSilver,
    error = ErrorRed,
    onError = HighContrastWhite,
    outline = AsphaltDivider
)

// In daylight mode, maintain high contrast amber and clean transit contrast
private val TransitLightColorScheme = lightColorScheme(
    primary = SafetyAmberDark,
    onPrimary = HighContrastWhite,
    primaryContainer = SafetyAmberLight,
    onPrimaryContainer = AsphaltDark,
    secondary = EmeraldGreenDark,
    onSecondary = HighContrastWhite,
    background = HighContrastWhite,
    onBackground = AsphaltDark,
    surface = Color(0xFFF5F5F5),
    onSurface = AsphaltDark,
    surfaceVariant = Color(0xFFE0E0E0),
    onSurfaceVariant = Color(0xFF424242),
    error = ErrorRed,
    onError = HighContrastWhite,
    outline = Color(0xFFBDBDBD)
)

@Composable
fun KekeGoTheme(
    darkTheme: Boolean = true, // Default to high-contrast dark transit theme for outdoor visibility
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) TransitDarkColorScheme else TransitLightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
