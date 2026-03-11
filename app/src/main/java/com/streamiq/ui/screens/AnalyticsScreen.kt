package com.streamiq.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.streamiq.data.StreamSummary
import com.streamiq.ui.theme.*
import com.streamiq.ui.viewmodel.StreamIQViewModel
import com.streamiq.utils.formatMoney
import com.streamiq.utils.streamColor
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(viewModel: StreamIQViewModel, onBack: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    val totalMonth = uiState.totalMonth
    val totalAllTime = uiState.summaries.sumOf { it.allTimeAmount }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("Analytics", color = TextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = TextSecondary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            // Month summary
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                BigStatCard("THIS MONTH", formatMoney(totalMonth), Accent, Modifier.weight(1f))
                BigStatCard("ALL TIME", formatMoney(totalAllTime), Gold, Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Stream breakdown
            if (uiState.summaries.isNotEmpty()) {
                Text("STREAM BREAKDOWN", fontSize = 11.sp, fontWeight = FontWeight.Bold,
                    color = TextMuted, letterSpacing = 2.sp)
                Spacer(modifier = Modifier.height(12.dp))

                // Bar chart
                uiState.summaries.forEach { summary ->
                    StreamBarRow(summary = summary, maxAmount = uiState.summaries.maxOf { it.monthAmount })
                    Spacer(modifier = Modifier.height(10.dp))
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Kill/Keep analysis
                Text("KILL OR KEEP? ⚡", fontSize = 11.sp, fontWeight = FontWeight.Bold,
                    color = TextMuted, letterSpacing = 2.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Based on monthly earnings", color = TextMuted, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(12.dp))

                val avgMonthAmount = if (uiState.summaries.isNotEmpty())
                    uiState.summaries.sumOf { it.monthAmount } / uiState.summaries.size else 0.0

                uiState.summaries.forEach { summary ->
                    KillKeepCard(summary = summary, avgAmount = avgMonthAmount)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Streak leaderboard
                Text("STREAK LEADERS 🔥", fontSize = 11.sp, fontWeight = FontWeight.Bold,
                    color = TextMuted, letterSpacing = 2.sp)
                Spacer(modifier = Modifier.height(12.dp))

                uiState.summaries.sortedByDescending { it.streak }.forEach { summary ->
                    if (summary.streak > 0) {
                        StreakRow(summary)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            } else {
                Text("No data yet. Start logging daily!", color = TextSecondary, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun BigStatCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceVariant)
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(label, fontSize = 10.sp, color = TextMuted, letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(value, fontSize = 24.sp, fontWeight = FontWeight.Black, color = color)
    }
}

@Composable
fun StreamBarRow(summary: StreamSummary, maxAmount: Double) {
    val color = streamColor(summary.stream.type)
    val fraction = if (maxAmount > 0) (summary.monthAmount / maxAmount).toFloat() else 0f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Card)
            .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(summary.stream.type.emoji, fontSize = 16.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(summary.stream.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            }
            Text(formatMoney(summary.monthAmount), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(CardBorder)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(3.dp))
                    .background(color)
            )
        }
    }
}

@Composable
fun KillKeepCard(summary: StreamSummary, avgAmount: Double) {
    val keep = summary.monthAmount >= avgAmount || summary.streak >= 7
    val color = if (keep) Green else Red
    val verdict = if (keep) "✅ KEEP" else "⚠️ REVIEW"
    val reason = when {
        summary.monthAmount == 0.0 -> "No earnings logged this month"
        summary.monthAmount < avgAmount * 0.5 -> "Earning below 50% of your average"
        summary.streak >= 7 -> "Strong ${summary.streak}-day streak"
        keep -> "Above average earnings"
        else -> "Below average — consider optimizing"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.08f))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(summary.stream.type.emoji, fontSize = 20.sp)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(summary.stream.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Text(reason, fontSize = 11.sp, color = TextSecondary)
        }
        Text(verdict, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
fun StreakRow(summary: StreamSummary) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Card)
            .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(summary.stream.type.emoji, fontSize = 20.sp)
        Spacer(modifier = Modifier.width(12.dp))
        Text(summary.stream.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
            color = TextPrimary, modifier = Modifier.weight(1f))
        Text("🔥 ${summary.streak} days", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Gold)
    }
}
