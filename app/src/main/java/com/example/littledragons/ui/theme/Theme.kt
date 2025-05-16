package com.example.littledragons.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = AppPalette.Green,
    primaryContainer = AppPalette.GreenVariant2,
    background = AppPalette.White,
    onBackground = AppPalette.TextPrimary,
    surface = AppPalette.White,
    onSurface = AppPalette.TextPrimary,
    surfaceContainer = AppPalette.White,
    surfaceContainerHigh = AppPalette.GreenVariant4,
    surfaceContainerHighest = AppPalette.GreenVariant4,
    surfaceContainerLow = AppPalette.White,
    surfaceContainerLowest = AppPalette.White,
    onSurfaceVariant = AppPalette.TextSecondary,
    secondary = AppPalette.GreenVariant3,
    secondaryContainer = AppPalette.GreenVariant4,
    tertiary = AppPalette.BlueVariant1,
    onTertiary = AppPalette.TextPrimary,
    tertiaryContainer = AppPalette.BlueVariant2,
    outline = AppPalette.Outline,
    surfaceTint = AppPalette.Green
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}