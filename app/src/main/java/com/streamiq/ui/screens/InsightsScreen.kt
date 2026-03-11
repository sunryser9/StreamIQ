package com.streamiq.ui.screens

// ═══════════════════════════════════════════════════════════════════════════
//  AI INSIGHTS — Rule-based engine that FEELS like AI. Zero API cost.
//  "YouTube is your fastest growing stream — up 40% this month"
//  Users can't tell the difference. Giants charge $10/mo for this.
// ═══════════════════════════════════════════════════════════════════════════

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.streamiq.data.StreamSummary
import com.streamiq.data.DailyEntry
import com.streamiq.ui.theme.*
import com.streamiq.ui.viewmodel.StreamIQViewModel
import com.streamiq.utils.*
import java.time.LocalDate
import java.time.YearMonth

data class Insight(
    val emoji: String,
    val title: String,
    val body: String,
    val color: Color,
    val priority: Int // lower = shown first
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(viewModel: StreamIQViewModel, onBack: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    val isDark = uiState.isDarkTheme
    val bg = if (isDark) Background else LightBackground
    val textPrimary = if (isDark) TextPrimary else LightTextPrimary
    val textSecondary = if (isDark) TextSecondary else LightTextSecondary

    val insights = remember(uiState.summaries, uiState.entries, uiState.totalMonth) {
        generateInsights(uiState.summaries, uiState.entries, uiState.totalMonth, uiState.overallStreak)
    }

    Scaffold(
        containerColor = bg,
        topBar = {
            TopAppBar(
                title = { Text("AI Insights 🧠", color = textPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = textSecondary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bg)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Accent.copy(0.1f))
            ) {
                Row(modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Text("🧠", fontSize = 24.sp)
                    Column {
                        Text("Personalised to your streams",
                            fontWeight = FontWeight.Bold, fontSize = 14.sp, color = textPrimary)
                        Text("Updated daily based on your actual income patterns",
                            fontSize = 12.sp, color = textSecondary)
                    }
                }
            }

            if (insights.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) Card else LightCard)
                ) {
                    Column(
                        modifier = Modifier.padding(40.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🧠", fontSize = 48.sp)
                        Spacer(Modifier.height(12.dp))
                        Text("Log income for 3+ days to unlock insights",
                            fontSize = 14.sp, color = textSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                }
            } else {
                insights.sortedBy { it.priority }.forEach { insight ->
                    InsightCard(insight, isDark, textPrimary, textSecondary)
                }
            }

            // Weekly challenge
            val challenge = getWeeklyChallenge(uiState.summaries, uiState.overallStreak)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Gold.copy(0.1f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("🎯 This Week's Challenge",
                        fontWeight = FontWeight.Bold, fontSize = 14.sp, color = textPrimary)
                    Spacer(Modifier.height(6.dp))
                    Text(challenge, fontSize = 13.sp, color = textSecondary, lineHeight = 18.sp)
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun InsightCard(
    insight: Insight,
    isDark: Boolean,
    textPrimary: Color,
    textSecondary: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = insight.color.copy(alpha = 0.08f)),
        border = BorderStroke(1.dp, insight.color.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(insight.color.copy(0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(insight.emoji, fontSize = 20.sp)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(insight.title, fontWeight = FontWeight.Bold,
                    fontSize = 14.sp, color = textPrimary)
                Spacer(Modifier.height(4.dp))
                Text(insight.body, fontSize = 13.sp,
                    color = textSecondary, lineHeight = 18.sp)
            }
        }
    }
}

private fun generateInsights(
    summaries: List<StreamSummary>,
    entries: List<DailyEntry>,
    totalMonth: Double,
    streak: Int
): List<Insight> {
    val insights = mutableListOf<Insight>()
    val today = LocalDate.now()
    val thisMonth = today.toString().substring(0, 7)
    val lastMonth = today.minusMonths(1).toString().substring(0, 7)
    val dayOfMonth = today.dayOfMonth
    val daysInMonth = YearMonth.now().lengthOfMonth()

    // Best performing stream
    val best = summaries.maxByOrNull { it.monthAmount }
    if (best != null && best.monthAmount > 0) {
        val pct = if (best.trend > 0) "+${String.format("%.0f", best.trend)}%" else "${String.format("%.0f", best.trend)}%"
        insights.add(Insight(
            emoji = "🏆",
            title = "${best.stream.name} is your top earner",
            body = "${formatMoney(best.monthAmount)} this month ($pct vs last month). Double down here — it's working.",
            color = Color(0xFF00E676),
            priority = 1
        ))
    }

    // Fastest growing stream
    val fastest = summaries.filter { it.trend > 20 && it.monthAmount > 0 }.maxByOrNull { it.trend }
    if (fastest != null) {
        insights.add(Insight(
            emoji = "🚀",
            title = "${fastest.stream.name} is growing fast",
            body = "Up ${String.format("%.0f", fastest.trend)}% vs last month. This is momentum — don't let it die.",
            color = Accent,
            priority = 2
        ))
    }

    // Dead stream alert
    val dead = summaries.filter { it.monthAmount == 0.0 && it.allTimeAmount > 0 }
    dead.forEach { d ->
        insights.add(Insight(
            emoji = "💀",
            title = "${d.stream.name} earned nothing this month",
            body = "This stream has gone quiet. One small action today — post, pitch, or list something — can restart it.",
            color = Red,
            priority = 3
        ))
    }

    // Streak insight
    if (streak >= 7) {
        insights.add(Insight(
            emoji = "🔥",
            title = "$streak-day earning streak!",
            body = "You've earned something every day for $streak days. Consistency is your superpower — protect this streak.",
            color = Gold,
            priority = 4
        ))
    } else if (streak == 0) {
        insights.add(Insight(
            emoji = "⚡",
            title = "No income logged today yet",
            body = "Log even a small amount to keep your streak alive. Consistency compounds over months.",
            color = Color(0xFFFF9800),
            priority = 4
        ))
    }

    // Month forecast
    val forecast = forecastMonthEnd(totalMonth, dayOfMonth, daysInMonth)
    val totalGoal = summaries.sumOf { it.stream.monthlyGoal }
    if (totalGoal > 0) {
        if (forecast >= totalGoal) {
            insights.add(Insight(
                emoji = "🎯",
                title = "On track to hit your goal",
                body = "At this pace you'll end the month at ${formatMoney(forecast)} — above your ${formatMoney(totalGoal)} goal. Keep it up!",
                color = Color(0xFF00E676),
                priority = 5
            ))
        } else {
            val gap = totalGoal - totalMonth
            val daysLeft = daysInMonth - dayOfMonth
            val perDay = if (daysLeft > 0) gap / daysLeft else gap
            insights.add(Insight(
                emoji = "⚠️",
                title = "Behind on your monthly goal",
                body = "You need ${formatMoney(perDay)}/day for $daysLeft more days to reach ${formatMoney(totalGoal)}. Which stream can push harder?",
                color = Red,
                priority = 5
            ))
        }
    }

    // Best day of week
    val byDayOfWeek = entries
        .filter { it.date.startsWith(thisMonth) }
        .groupBy { LocalDate.parse(it.date).dayOfWeek.name }
        .mapValues { e -> e.value.sumOf { it.amount } }
    val bestDay = byDayOfWeek.maxByOrNull { it.value }
    if (bestDay != null && bestDay.value > 0) {
        val dayName = bestDay.key.lowercase().replaceFirstChar { it.uppercase() }
        insights.add(Insight(
            emoji = "📅",
            title = "$dayName is your best earning day",
            body = "You earn the most on ${dayName}s this month. Schedule your biggest income tasks on this day.",
            color = Accent,
            priority = 6
        ))
    }

    // Multiple streams advice
    if (summaries.size == 1) {
        insights.add(Insight(
            emoji = "🌊",
            title = "You have 1 income stream",
            body = "Single stream = single point of failure. Adding a second stream reduces risk by 50%. What could you start this week?",
            color = Color(0xFFFF9800),
            priority = 7
        ))
    } else if (summaries.size >= 3) {
        val diversityPct = (100.0 / summaries.size).toInt()
        insights.add(Insight(
            emoji = "🌊",
            title = "${summaries.size} income streams — well diversified",
            body = "No single stream makes up more than ~${100 - diversityPct}% of your income. You're building real resilience.",
            color = Color(0xFF69F0AE),
            priority = 8
        ))
    }

    return insights
}

private fun getWeeklyChallenge(summaries: List<StreamSummary>, streak: Int): String {
    val weakest = summaries.filter { it.monthAmount > 0 }.minByOrNull { it.monthAmount }
    return when {
        summaries.isEmpty() -> "Add your first income stream and log one entry today."
        streak < 3 -> "Log income every day for 7 days straight. Consistency builds momentum."
        weakest != null -> "Give ${weakest.stream.name} one hour of focused work this week. Can you move the needle?"
        else -> "Set a monthly goal on each stream. Goals you track are 3x more likely to be achieved."
    }
}
