package com.example.brainrottracker.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ─── Dark-mode fixed colours ──────────────────────────────────────────────────
val PrimaryPurple  = Color(0xFF8B5CF6)
val AccentPurple   = Color(0xFFA855F7)
val OrangeAccent   = Color(0xFFF97316)
val OrangeLight    = Color(0xFFFB923C)
val FuchsiaAccent  = Color(0xFFE879F9)

// ─── Adaptive colours (dark ↔ light) ──────────────────────────────────────────

val BackgroundDark: Color
    @Composable get() = if (AppTheme.isDark) Color(0xFF0C0A16) else Color(0xFFF5F3FF)

val ForegroundLight: Color
    @Composable get() = if (AppTheme.isDark) Color(0xFFEDE8FF) else Color(0xFF1E1B4B)

val CardDark: Color
    @Composable get() = if (AppTheme.isDark) Color(0xFF13102A) else Color(0xFFFFFFFF)

val LightPurple: Color
    @Composable get() = if (AppTheme.isDark) Color(0xFFC084FC) else Color(0xFF7C3AED)

val MutedForeground: Color
    @Composable get() = if (AppTheme.isDark) Color(0xFF7C6FA0) else Color(0xFF7B6CA4)

val MutedDark: Color
    @Composable get() = if (AppTheme.isDark) Color(0xFF1A1535) else Color(0xFFEDE9FE)

val SecondaryDark: Color
    @Composable get() = if (AppTheme.isDark) Color(0xFF231D4A) else Color(0xFFF3E8FF)

val BorderPurple: Color
    @Composable get() = if (AppTheme.isDark) Color(0x268B5CF6) else Color(0x288B5CF6)

val BorderPurpleStrong: Color
    @Composable get() = if (AppTheme.isDark) Color(0x4D8B5CF6) else Color(0x448B5CF6)

val NavInactive: Color
    @Composable get() = if (AppTheme.isDark) Color(0xFF4A3F6B) else Color(0xFF9E92C4)

val NavActive: Color
    @Composable get() = if (AppTheme.isDark) Color(0xFFA78BFA) else Color(0xFF6D28D9)

val SuccessGreen: Color
    @Composable get() = if (AppTheme.isDark) Color(0xFF86EFAC) else Color(0xFF16A34A)

val ErrorRed: Color
    @Composable get() = if (AppTheme.isDark) Color(0xFFF87171) else Color(0xFFDC2626)

val ErrorRedDark: Color
    @Composable get() = if (AppTheme.isDark) Color(0xFFDC2626) else Color(0xFF991B1B)

val VioletLight: Color
    @Composable get() = if (AppTheme.isDark) Color(0xFFC4B5FD) else Color(0xFF5B21B6)

val Purple950: Color
    @Composable get() = if (AppTheme.isDark) Color(0xFF2D1B69) else Color(0xFFE5E0FF)

// Raw (non-composable) constants needed in Theme.kt before composition starts:
val RawBackgroundDark  = Color(0xFF0C0A16)
val RawBackgroundLight = Color(0xFFF5F3FF)
val RawCardDark        = Color(0xFF13102A)
val RawCardLight       = Color(0xFFFFFFFF)
val RawForegroundDark  = Color(0xFFEDE8FF)
val RawForegroundLight = Color(0xFF1E1B4B)
val RawMutedFgDark     = Color(0xFF7C6FA0)
val RawMutedFgLight    = Color(0xFF7B6CA4)
val RawMutedDark       = Color(0xFF1A1535)
val RawMutedLight      = Color(0xFFEDE9FE)
val RawSecondaryDark   = Color(0xFF231D4A)
val RawSecondaryLight  = Color(0xFFF3E8FF)
val RawBorderStrDark   = Color(0x4D8B5CF6)
val RawBorderStrLight  = Color(0x448B5CF6)
val RawVioletDark      = Color(0xFFC4B5FD)
val RawVioletLight     = Color(0xFF5B21B6)
