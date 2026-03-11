package com.streamiq.ui.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

// ── Dark theme ──────────────────────────────────────────────────────────────
val Background     = Color(0xFF080C14)
val Surface        = Color(0xFF0F1623)
val SurfaceVariant = Color(0xFF161E2E)
val Card           = Color(0xFF1A2235)
val CardBorder     = Color(0xFF243048)
val TextPrimary    = Color(0xFFFFFFFF)
val TextSecondary  = Color(0xFFB0BEC5)
val TextMuted      = Color(0xFF78909C)

// ── Light theme ─────────────────────────────────────────────────────────────
val LightBackground     = Color(0xFFF0F4FF)
val LightSurface        = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFE8EDF8)
val LightCard           = Color(0xFFFFFFFF)
val LightCardBorder     = Color(0xFFD0D8F0)
val LightTextPrimary    = Color(0xFF050A18)
val LightTextSecondary  = Color(0xFF2D3A55)
val LightTextMuted      = Color(0xFF5A6478)

// ── Accent ───────────────────────────────────────────────────────────────────
val Accent     = Color(0xFF00C8E8)
val AccentDim  = Color(0xFF0097A7)
val AccentGlow = Color(0x2000E5FF)

// ── Status ───────────────────────────────────────────────────────────────────
val Green    = Color(0xFF00E676)
val GreenDim = Color(0xFF1B5E20)
val Red      = Color(0xFFFF5252)
val RedDim   = Color(0xFF7F0000)
val Gold     = Color(0xFFFFD700)
val GoldDim  = Color(0xFF7B6200)

// ── Stream type colors ────────────────────────────────────────────────────────
val YoutubeColor   = Color(0xFFFF4444)
val WebsiteColor   = Color(0xFF4FC3F7)
val CryptoColor    = Color(0xFFF7931A)
val DomainColor    = Color(0xFFCE93D8)
val FreelanceColor = Color(0xFF69F0AE)
val OtherColor     = Color(0xFF90A4AE)

val LocalIsDarkTheme = compositionLocalOf { true }
