package com.streamiq.ui.screens

// ═══════════════════════════════════════════════════════════════════════════
//  FORECAST SCREEN — "At this pace, you'll earn ₹47,000 this month"
//  Nobody shows this per-stream. YNAB only does expense forecasting.
//  This is why users open StreamIQ every morning.
// ═══════════════════════════════════════════════════════════════════════════

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.streamiq.ui.theme.*
import com.streamiq.ui.viewmodel.StreamIQViewModel
import com.streamiq.utils.*
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForecastScreen(viewModel: StreamIQViewModel, onBack: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    val isDark = uiState.isDarkTheme
    val bg = if (isDark) Background else LightBackground
    val textPrimary = if (isDark) TextPrimary else LightTextPrimary
    val textSecondary = if (isDark) TextSecondary else LightTextSecondary

    val today = LocalDate.now()
    val dayOfMonth = today.dayOfMonth
    val daysInMonth = YearMonth.now().lengthOfMonth()
    val daysLeft = daysInMonth - dayOfMonth

    val totalForecast = forecastMonthEnd(uiState.totalMonth, dayOfMonth, daysInMonth)
    val totalGoal = uiState.summaries.sumOf { it.stream.monthlyGoal }
    val forecastLbl = forecastLabel(totalForecast, totalGoal)
    val progressPercent = (dayOfMonth.toFloat() / daysInMonth).coerceIn(0f, 1f)
    val animatedForecast by animateFloatAsState(
        targetValue = (totalForecast / (if (totalForecast > 0) totalForecast else 1.0)).toFloat(),
        animationSpec = tween(1000), label = "forecast"
    )

    Scaffold(
        containerColor = bg,
        topBar = {
            TopAppBar(
                title = { Text("Forecast 🔮", color = textPrimary, fontWeight = FontWeight.Bold) },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Hero forecast card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Accent.copy(alpha = 0.1f))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🔮 Month-End Forecast",
                        fontSize = 13.sp, color = Accent,
                        fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        formatMoneyFull(totalForecast),
                        fontSize = 52.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                    Text("projected by end of ${today.month.name.lowercase().replaceFirstChar { it.uppercase() }}",
                        fontSize = 13.sp, color = textSecondary)

                    Spacer(Modifier.height(16.dp))

                    // Month progress bar
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Day $dayOfMonth of $daysInMonth",
                                fontSize = 11.sp, color = textSecondary)
                            Text("$daysLeft days left",
                                fontSize = 11.sp, color = textSecondary)
                        }
                        Spacer(Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(if (isDark) SurfaceVariant else LightSurfaceVariant)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(progressPercent)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(Brush.horizontalGradient(
                                        listOf(Accent.copy(0.6f), Accent)))
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Status badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Accent.copy(0.15f))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(forecastLbl, fontSize = 14.sp,
                            fontWeight = FontWeight.Bold, color = Accent)
                    }
                }
            }

            // Earned vs Forecast comparison
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    emoji = "💰",
                    label = "Earned so far",
                    value = formatMoneyFull(uiState.totalMonth),
                    color = Green,
                    isDark = isDark
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    emoji = "📈",
                    label = "Daily average",
                    value = formatMoney(if (dayOfMonth > 0) uiState.totalMonth / dayOfMonth else 0.0),
                    color = Accent,
                    isDark = isDark
                )
            }

            if (totalGoal > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        emoji = "🎯",
                        label = "Monthly goal",
                        value = formatMoneyFull(totalGoal),
                        color = Gold,
                        isDark = isDark
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        emoji = if (totalForecast >= totalGoal) "✅" else "⚡",
                        label = "Gap to goal",
                        value = formatMoney((totalGoal - uiState.totalMonth).coerceAtLeast(0.0)),
                        color = if (totalForecast >= totalGoal) Green else Red,
                        isDark = isDark
                    )
                }
            }

            // Per-stream forecasts
            Text("Stream Forecasts", fontWeight = FontWeight.Bold,
                fontSize = 16.sp, color = textPrimary)

            if (uiState.summaries.isEmpty()) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) Card else LightCard)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🔮", fontSize = 40.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("Add streams and log income to see forecasts",
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp, color = textSecondary)
                    }
                }
            } else {
                uiState.summaries.forEach { summary ->
                    val streamForecast = forecastMonthEnd(summary.monthAmount, dayOfMonth, daysInMonth)
                    val streamGoal = summary.stream.monthlyGoal
                    val streamLbl = forecastLabel(streamForecast, streamGoal)
                    val streamColor = streamColor(summary.stream.type)

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDark) Card else LightCard)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(summary.stream.type.emoji, fontSize = 20.sp)
                                    Column {
                                        Text(summary.stream.name,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp, color = textPrimary)
                                        Text(streamLbl, fontSize = 11.sp, color = streamColor)
                                    }
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(formatMoneyFull(streamForecast),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp, color = streamColor)
                                    Text("projected", fontSize = 10.sp, color = textSecondary)
                                }
                            }

                            if (streamGoal > 0) {
                                Spacer(Modifier.height(8.dp))
                                val pct = ((summary.monthAmount / streamGoal) * 100).roundToInt().coerceIn(0, 100)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Goal: ${formatMoney(streamGoal)}",
                                        fontSize = 11.sp, color = textSecondary)
                                    Text("$pct% reached", fontSize = 11.sp,
                                        color = if (pct >= 100) Green else textSecondary)
                                }
                                Spacer(Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(if (isDark) SurfaceVariant else LightSurfaceVariant)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth((pct / 100f).coerceIn(0f, 1f))
                                            .fillMaxHeight()
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(streamColor)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // What-if calculator
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Gold.copy(alpha = 0.1f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("💡 To Hit Your Goal",
                        fontWeight = FontWeight.Bold, fontSize = 14.sp, color = textPrimary)
                    Spacer(Modifier.height(8.dp))
                    if (totalGoal > 0 && daysLeft > 0) {
                        val needed = (totalGoal - uiState.totalMonth).coerceAtLeast(0.0)
                        val neededPerDay = needed / daysLeft
                        Text(
                            "You need ${formatMoney(neededPerDay)}/day for the next $daysLeft days " +
                            "to reach your ${formatMoney(totalGoal)} goal.",
                            fontSize = 13.sp, color = textSecondary, lineHeight = 18.sp
                        )
                    } else if (totalGoal <= 0) {
                        Text("Set monthly goals on your streams to see what you need per day to hit them.",
                            fontSize = 13.sp, color = textSecondary, lineHeight = 18.sp)
                    } else {
                        Text("🎉 Goal achieved! You've already hit your monthly target.",
                            fontSize = 13.sp, color = Green)
                    }
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier,
    emoji: String,
    label: String,
    value: String,
    color: Color,
    isDark: Boolean
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Card else LightCard)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(emoji, fontSize = 20.sp)
            Spacer(Modifier.height(4.dp))
            Text(label, fontSize = 10.sp,
                color = if (isDark) TextSecondary else LightTextSecondary)
            Text(value, fontSize = 18.sp,
                fontWeight = FontWeight.Bold, color = color)
        }
    }
}
