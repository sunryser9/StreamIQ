package com.streamiq.utils

import androidx.compose.ui.graphics.Color
import com.streamiq.data.StreamType
import com.streamiq.ui.theme.*

// ── Currency support ─────────────────────────────────────────────────────────
enum class AppCurrency(
    val code: String,
    val symbol: String,
    val flag: String,
    val displayName: String
) {
    USD("USD", "$",  "🇺🇸", "US Dollar"),
    INR("INR", "₹",  "🇮🇳", "Indian Rupee"),
    EUR("EUR", "€",  "🇪🇺", "Euro"),
    GBP("GBP", "£",  "🇬🇧", "British Pound"),
    AED("AED", "د.إ","🇦🇪", "UAE Dirham"),
    SGD("SGD", "S$", "🇸🇬", "Singapore Dollar"),
    AUD("AUD", "A$", "🇦🇺", "Australian Dollar"),
    CAD("CAD", "C$", "🇨🇦", "Canadian Dollar")
}

var selectedCurrency: AppCurrency = AppCurrency.USD

fun formatMoney(amount: Double, currency: AppCurrency = selectedCurrency): String {
    val sym = currency.symbol
    return when {
        amount >= 1_000_000 -> "$sym${String.format("%.1f", amount / 1_000_000)}M"
        amount >= 1_000     -> "$sym${String.format("%.1f", amount / 1_000)}K"
        else                -> "$sym${String.format("%.0f", amount)}"
    }
}

fun formatMoneyFull(amount: Double, currency: AppCurrency = selectedCurrency): String {
    val sym = currency.symbol
    return "$sym${String.format("%,.0f", amount)}"
}

fun formatMoneyInt(amount: Double, currency: AppCurrency = selectedCurrency): String {
    val sym = currency.symbol
    return when {
        amount >= 1_000_000 -> "$sym${String.format("%.1f", amount / 1_000_000)}M"
        amount >= 1_000     -> "$sym${String.format("%.1f", amount / 1_000)}K"
        else                -> "$sym${amount.toInt()}"
    }
}

fun streamColor(type: StreamType): Color = when (type) {
    StreamType.YOUTUBE   -> YoutubeColor
    StreamType.WEBSITE   -> WebsiteColor
    StreamType.CRYPTO    -> CryptoColor
    StreamType.DOMAIN    -> DomainColor
    StreamType.FREELANCE -> FreelanceColor
    StreamType.OTHER     -> OtherColor
}

// ── Stream Score engine ──────────────────────────────────────────────────────
// Scores each stream 0–100 based on consistency, growth, reliability
// This is the unicorn feature — no competitor does this
fun calculateStreamScore(
    monthAmount: Double,
    lastMonthAmount: Double,
    streak: Int,
    allTimeAmount: Double,
    entriesThisMonth: Int
): Int {
    // Consistency (0–40): how many days did this stream earn this month
    val daysInMonth = 30
    val consistencyScore = ((entriesThisMonth.toDouble() / daysInMonth) * 40).toInt().coerceIn(0, 40)

    // Growth (0–40): month over month trend
    val growthScore = when {
        lastMonthAmount <= 0 && monthAmount > 0 -> 30  // new stream earning
        lastMonthAmount <= 0 -> 10
        else -> {
            val pct = ((monthAmount - lastMonthAmount) / lastMonthAmount) * 100
            when {
                pct >= 50  -> 40
                pct >= 20  -> 35
                pct >= 5   -> 28
                pct >= 0   -> 22
                pct >= -10 -> 15
                pct >= -30 -> 8
                else       -> 2
            }
        }
    }

    // Reliability (0–20): streak bonus
    val reliabilityScore = when {
        streak >= 30 -> 20
        streak >= 14 -> 16
        streak >= 7  -> 12
        streak >= 3  -> 8
        streak >= 1  -> 5
        else         -> 0
    }

    return (consistencyScore + growthScore + reliabilityScore).coerceIn(0, 100)
}

fun scoreLabel(score: Int): String = when {
    score >= 80 -> "🔥 Thriving"
    score >= 60 -> "📈 Growing"
    score >= 40 -> "😐 Stable"
    score >= 20 -> "⚠️ Weak"
    else        -> "💀 Dead"
}

fun scoreColor(score: Int): Color = when {
    score >= 80 -> Color(0xFF00E676)
    score >= 60 -> Color(0xFF69F0AE)
    score >= 40 -> Color(0xFFFFD700)
    score >= 20 -> Color(0xFFFF9800)
    else        -> Color(0xFFFF5252)
}



// ── Forecast engine ──────────────────────────────────────────────────────────
fun forecastMonthEnd(
    currentMonthTotal: Double,
    dayOfMonth: Int,
    daysInMonth: Int = 30
): Double {
    if (dayOfMonth <= 0) return 0.0
    val dailyAvg = currentMonthTotal / dayOfMonth
    return dailyAvg * daysInMonth
}

fun forecastLabel(forecast: Double, goal: Double): String = when {
    goal <= 0              -> "On track"
    forecast >= goal * 1.2 -> "🚀 Crushing it"
    forecast >= goal       -> "✅ On track for goal"
    forecast >= goal * 0.8 -> "⚡ Close — push harder"
    else                   -> "⚠️ Behind goal"
}
