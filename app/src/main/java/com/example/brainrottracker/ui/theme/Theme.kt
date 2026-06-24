package com.example.brainrottracker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

val LocalThemeIsDark = compositionLocalOf { true }

object AppTheme {
    val isDark: Boolean
        @Composable
        get() = LocalThemeIsDark.current
}

@Composable
fun BrainrotTrackerTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary            = PrimaryPurple,
            onPrimary          = Color.White,
            primaryContainer   = Color(0xFF6D28D9),
            onPrimaryContainer = RawForegroundDark,
            secondary          = RawSecondaryDark,
            onSecondary        = RawVioletDark,
            secondaryContainer = RawMutedDark,
            onSecondaryContainer = RawForegroundDark,
            tertiary           = AccentPurple,
            onTertiary         = Color.White,
            background         = RawBackgroundDark,
            onBackground       = RawForegroundDark,
            surface            = RawCardDark,
            onSurface          = RawForegroundDark,
            surfaceVariant     = RawMutedDark,
            onSurfaceVariant   = RawMutedFgDark,
            outline            = RawBorderStrDark,
            error              = Color(0xFFF87171),
            onError            = Color.White,
        )
    } else {
        lightColorScheme(
            primary            = PrimaryPurple,
            onPrimary          = Color.White,
            primaryContainer   = Color(0xFFDDD2FF),
            onPrimaryContainer = RawForegroundLight,
            secondary          = RawSecondaryLight,
            onSecondary        = RawVioletLight,
            secondaryContainer = RawMutedLight,
            onSecondaryContainer = RawForegroundLight,
            tertiary           = AccentPurple,
            onTertiary         = Color.White,
            background         = RawBackgroundLight,
            onBackground       = RawForegroundLight,
            surface            = RawCardLight,
            onSurface          = RawForegroundLight,
            surfaceVariant     = RawMutedLight,
            onSurfaceVariant   = RawMutedFgLight,
            outline            = RawBorderStrLight,
            error              = Color(0xFFDC2626),
            onError            = Color.White,
        )
    }

    CompositionLocalProvider(LocalThemeIsDark provides darkTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography  = Typography,
            content     = content
        )
    }
}
