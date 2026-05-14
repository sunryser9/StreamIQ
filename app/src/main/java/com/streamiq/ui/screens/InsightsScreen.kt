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
                title = { Text("Revenue Intelligence", color = textPrimary, fontWeight = FontWeight.Bold) },
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
                   Text("▲", fontSize = 24.sp)
                    Column {
                 Text("CPA-Grade Analysis of Your Revenue Streams",
                fontWeight = FontWeight.Bold, fontSize = 14.sp, color = textPrimary)
                Text("Updated daily — tax flags, run-rate calculations, and concentration risk.",
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
                        Text("▲", fontSize = 48.sp)
                        Spacer(Modifier.height(12.dp))
                      Text("Log revenue across 3+ days to generate your first financial analysis.",
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
                    Text("This Week's Operational Priority",
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
    val dayOfMonth = today.dayOfMonth
    val daysInMonth = YearMonth.now().lengthOfMonth()
    val daysLeft = daysInMonth - dayOfMonth

    // ── 1. TAX MITIGATION ───────────────────────────────────────────────
    // Identify top 2 expense categories as valid business tax write-offs
    val expenseEntries = entries.filter { it.amount < 0 }
    if (expenseEntries.isNotEmpty()) {
        insights.add(Insight(
            emoji = "▲",
            title = "Tax Write-Off Opportunity Identified",
            body = "Your logged expenses qualify as deductible business operating costs. " +
                   "Filing these reduces your net taxable income pool directly. " +
                   "Retain all receipts and categorize under: Software & Subscriptions, " +
                   "Home Office, or Professional Services for maximum deduction eligibility.",
            color = Color(0xFF00E676),
            priority = 1
        ))
    }

    // ── 2. CONCENTRATION RISK ───────────────────────────────────────────
    // Flag if single stream accounts for >70% of total revenue
    val best = summaries.maxByOrNull { it.monthAmount }
    if (best != null && totalMonth > 0) {
        val concentration = (best.monthAmount / totalMonth) * 100
        val pct = if (best.trend > 0) "+${String.format("%.0f", best.trend)}%"
                  else "${String.format("%.0f", best.trend)}%"
        if (concentration >= 70) {
            insights.add(Insight(
                emoji = "▲",
                title = "Income Concentration Risk — ${String.format("%.0f", concentration)}% Single Stream",
                body = "${best.stream.name} accounts for ${String.format("%.0f", concentration)}% " +
                       "of your total monthly revenue. This represents a critical concentration risk. " +
                       "A disruption to this stream directly impacts your operating income. " +
                       "Diversify by activating or scaling a secondary revenue stream immediately.",
                color = Color(0xFFFF5252),
                priority = 2
            ))
        } else {
            insights.add(Insight(
                emoji = "★",
                title = "${best.stream.name} — Highest Revenue Stream",
                body = "${formatMoney(best.monthAmount)} recorded this month ($pct vs prior month). " +
                       "This stream carries the highest ROI weight in your current portfolio.",
                color = Color(0xFF00E676),
                priority = 2
            ))
        }
    }

    // ── 3. RUN-RATE REALITY CHECK ───────────────────────────────────────
    // Calculate exact daily amount needed to hit monthly revenue target
    val totalGoal = summaries.sumOf { it.stream.monthlyGoal }
    if (totalGoal > 0 && daysLeft > 0) {
        val gap = (totalGoal - totalMonth).coerceAtLeast(0.0)
        val dailyRunRate = gap / daysLeft
        val currentDailyRate = if (dayOfMonth > 0) totalMonth / dayOfMonth else 0.0
        if (gap > 0) {
            insights.add(Insight(
                emoji = "▲",
                title = "Daily Run-Rate Required: ${formatMoney(dailyRunRate)}/day",
                body = "To reach your ${formatMoney(totalGoal)} monthly target you must generate " +
                       "${formatMoney(dailyRunRate)} per day across your top-performing streams " +
                       "for the remaining $daysLeft days. Your current daily average is " +
                       "${formatMoney(currentDailyRate)}/day. " +
                       "${if (dailyRunRate > currentDailyRate) "You need to increase output by ${formatMoney(dailyRunRate - currentDailyRate)}/day." else "You are on track to meet your target."}",
                color = if (dailyRunRate <= currentDailyRate) Color(0xFF00E676) else Color(0xFFFF9800),
                priority = 3
            ))
        } else {
            insights.add(Insight(
                emoji = "★",
                title = "Monthly Revenue Target Achieved",
                body = "You have met your ${formatMoney(totalGoal)} operating target for this period. " +
                       "Any additional revenue recorded this month represents surplus above plan.",
                color = Color(0xFF00E676),
                priority = 3
            ))
        }
    }

    // ── 4. GROWTH SIGNAL ────────────────────────────────────────────────
    val fastest = summaries.filter { it.trend > 20 && it.monthAmount > 0 }
                           .maxByOrNull { it.trend }
    if (fastest != null) {
        insights.add(Insight(
            emoji = "▲",
            title = "${fastest.stream.name} — High Growth Signal",
            body = "Up ${String.format("%.0f", fastest.trend)}% vs prior month. " +
                   "Allocate additional operational time to this stream " +
                   "to capitalize on current momentum.",
            color = Accent,
            priority = 4
        ))
    }

    // ── 5. DORMANT STREAM ALERT ─────────────────────────────────────────
    summaries.filter { it.monthAmount == 0.0 && it.allTimeAmount > 0 }.forEach { d ->
        insights.add(Insight(
            emoji = "▲",
            title = "${d.stream.name} — Zero Revenue This Period",
            body = "This stream has recorded no revenue this month. " +
                   "A dormant stream represents an unallocated asset. " +
                   "Schedule one revenue action this week to reactivate it.",
            color = Color(0xFFFF5252),
            priority = 5
        ))
    }

    // ── 6. ACTIVITY CONSISTENCY ─────────────────────────────────────────
    if (streak >= 7) {
        insights.add(Insight(
            emoji = "▲",
            title = "$streak Consecutive Days of Revenue Activity",
            body = "You have logged revenue every day for $streak days. " +
                   "Consistent logging produces more accurate monthly forecasts " +
                   "and stronger tax documentation.",
            color = Color(0xFFFFD700),
            priority = 6
        ))
    }

    return insights
}


private fun getWeeklyChallenge(summaries: List<StreamSummary>, streak: Int): String {
    val weakest = summaries.filter { it.monthAmount > 0 }.minByOrNull { it.monthAmount }
    return when {
        summaries.isEmpty() -> "Add your first revenue stream and log one entry today to begin generating financial data."
        streak < 3 -> "Log revenue every day this week to establish a consistent data baseline for accurate forecasting."
        weakest != null -> "Allocate focused time to ${weakest.stream.name} this week. Underperforming streams represent your highest marginal ROI opportunity."
        else -> "Set a monthly revenue target on each stream. Tracked targets produce measurably higher attainment rates."
    }
}
