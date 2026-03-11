package com.streamiq.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

// All color definitions are in Color.kt
// This file contains only the theme setup

private val DarkColorScheme = darkColorScheme(
    primary = Accent, onPrimary = Background, secondary = AccentDim,
    background = Background, surface = Surface, surfaceVariant = SurfaceVariant,
    onBackground = TextPrimary, onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary, outline = CardBorder, error = Red
)

private val LightColorScheme = lightColorScheme(
    primary = Accent, onPrimary = Color.White, secondary = AccentDim,
    background = LightBackground, surface = LightSurface, surfaceVariant = LightSurfaceVariant,
    onBackground = LightTextPrimary, onSurface = LightTextPrimary,
    onSurfaceVariant = LightTextSecondary, outline = LightCardBorder, error = Red
)

@Composable
fun StreamIQTheme(isDark: Boolean = true, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalIsDarkTheme provides isDark) {
        MaterialTheme(
            colorScheme = if (isDark) DarkColorScheme else LightColorScheme,
            content = content
        )
    }
}
