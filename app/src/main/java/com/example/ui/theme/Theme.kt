package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Light Color Scheme: Rich Agriculture Greens, Loam Earth Tones & Golden Wheat
private val LightColorScheme = lightColorScheme(
    primary = AgriGreenPrimary,
    onPrimary = AgriOnPrimary,
    primaryContainer = AgriGreenPrimaryContainer,
    onPrimaryContainer = AgriOnPrimaryContainer,

    secondary = AgriEarthSecondary,
    onSecondary = AgriOnSecondary,
    secondaryContainer = AgriEarthSecondaryContainer,
    onSecondaryContainer = AgriOnSecondaryContainer,

    tertiary = AgriHarvestTertiary,
    onTertiary = AgriOnTertiary,
    tertiaryContainer = AgriHarvestTertiaryContainer,
    onTertiaryContainer = AgriOnTertiaryContainer,

    background = AgriBackgroundLight,
    onBackground = AgriOnBackgroundLight,

    surface = AgriSurfaceLight,
    onSurface = AgriOnSurfaceLight,
    surfaceVariant = AgriSurfaceVariant,
    onSurfaceVariant = AgriOnSurfaceVariant,

    outline = AgriOutline,
    outlineVariant = AgriOutlineVariant,

    error = AgriError,
    onError = AgriOnError,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
)

// Dark Color Scheme: Luminous Chlorophyll, Terracotta Sand & Fertile Deep Soil
private val DarkColorScheme = darkColorScheme(
    primary = AgriGreenPrimaryDarkTheme,
    onPrimary = AgriOnPrimaryDarkTheme,
    primaryContainer = AgriGreenPrimaryContainerDarkTheme,
    onPrimaryContainer = AgriOnPrimaryContainerDarkTheme,

    secondary = AgriEarthSecondaryDarkTheme,
    onSecondary = AgriOnSecondaryDarkTheme,
    secondaryContainer = AgriEarthSecondaryContainerDarkTheme,
    onSecondaryContainer = AgriOnSecondaryContainerDarkTheme,

    tertiary = AgriHarvestTertiaryDarkTheme,
    onTertiary = AgriOnTertiaryDarkTheme,
    tertiaryContainer = AgriHarvestTertiaryContainerDarkTheme,
    onTertiaryContainer = AgriOnTertiaryContainerDarkTheme,

    background = AgriBackgroundDark,
    onBackground = AgriOnBackgroundDark,

    surface = AgriSurfaceDark,
    onSurface = AgriOnSurfaceDark,
    surfaceVariant = AgriSurfaceVariantDark,
    onSurfaceVariant = AgriOnSurfaceVariantDark,

    outline = AgriOutlineDark,
    outlineVariant = Color(0xFF424A41),

    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
)

@Composable
fun CropHubTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
