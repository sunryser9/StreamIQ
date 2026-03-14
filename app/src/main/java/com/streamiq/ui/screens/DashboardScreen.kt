package com.streamiq.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.streamiq.data.StreamSummary
import com.streamiq.data.StreamType
import com.streamiq.ui.components.ConfettiOverlay
import com.streamiq.ui.theme.*
import com.streamiq.ui.viewmodel.StreamIQViewModel
import com.streamiq.utils.formatMoney
import com.streamiq.utils.streamColor
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun DashboardScreen(
    viewModel: StreamIQViewModel,
    onLogToday: () -> Unit,
    onAddStream: () -> Unit,
    onAnalytics: () -> Unit,
    onShare: () -> Unit,
    onToggleTheme: () -> Unit = {},
    onVoiceBot: () -> Unit = {},
    onTaxJar: () -> Unit = {},
    onStreamScore: () -> Unit = {},
    onForecast: () -> Unit = {},
    onInsights: () -> Unit = {},
    onExport: () -> Unit = {},
    onCurrency: () -> Unit = {},
    onPro: () -> Unit = {},
    onPro: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val today = LocalDate.now().format(DateTimeFormatter.ofPattern("EEE, MMM d"))
    var showConfetti by remember { mutableStateOf(false) }
    val isDark = uiState.isDarkTheme
    val bgColor = if (isDark) Background else Color(0xFFF0F4FF)

    // Trigger confetti when crossing $100, $500, $1000 milestones
    LaunchedEffect(uiState.totalMonth) {
        val milestones = listOf(100.0, 500.0, 1000.0, 5000.0)
        if (milestones.any { uiState.totalMonth >= it && uiState.totalMonth - (uiState.summaries.firstOrNull()?.todayAmount ?: 0.0) < it }) {
            showConfetti = true
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(bgColor)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(AccentGlow, Background)
                        )
                    )
                    .padding(24.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Image(
                                    painter = painterResource(com.streamiq.R.drawable.mascot_bolt),
                                    contentDescription = "StreamIQ mascot",
                                    modifier = Modifier.size(36.dp)
                                )
                                Text(
                                    "StreamIQ",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Accent
                                )
                            }
                            Text(today, fontSize = 13.sp, color = TextSecondary)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(
                                onClick = onToggleTheme,
                                modifier = Modifier.clip(CircleShape).background(SurfaceVariant).size(40.dp)
                            ) {
                                Icon(
                                    if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                                    "Toggle theme", tint = Gold, modifier = Modifier.size(18.dp)
                                )
                            }
                            IconButton(
                                onClick = onShare,
                                modifier = Modifier.clip(CircleShape).background(SurfaceVariant).size(40.dp)
                            ) {
                                Icon(Icons.Default.Share, "Share", tint = Accent, modifier = Modifier.size(18.dp))
                            }
                            IconButton(
                                onClick = onVoiceBot,
                                modifier = Modifier.clip(CircleShape).background(SurfaceVariant).size(40.dp)
                            ) {
                                Icon(Icons.Default.Mic, "Voice", tint = Color(0xFFE91E63), modifier = Modifier.size(18.dp))
                            }
                            IconButton(
                                onClick = onAddStream,
                                modifier = Modifier.clip(CircleShape).background(SurfaceVariant).size(40.dp)
                            ) {
                                Icon(Icons.Default.Add, "Add", tint = Accent, modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Big today number
                    Column {
                        Text("TODAY", fontSize = 11.sp, fontWeight = FontWeight.Bold,
                            color = TextMuted, letterSpacing = 2.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        AnimatedContent(targetState = uiState.totalToday) { total ->
                            Text(
                                formatMoney(total),
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Black,
                                color = if (total > 0) Green else TextPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Stats row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatChip(
                            label = "REVENUE",
                            value = formatMoney(uiState.totalMonth),
                            color = Accent,
                            modifier = Modifier.weight(1f)
                        )
                        StatChip(
                            label = "NET PROFIT",
                            value = formatMoney(uiState.netProfitMonth),
                            color = if (uiState.netProfitMonth >= uiState.totalMonth * 0.5) Green else Gold,
                            modifier = Modifier.weight(1f)
                        )
                        StatChip(
                            label = "STREAK",
                            value = "${uiState.overallStreak}d 🔥",
                            color = Gold,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // Show expense warning if expenses exist
                    if (uiState.totalExpensesMonth > 0) {
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Red.copy(0.08f))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Expenses this month",
                                fontSize = 12.sp, color = TextSecondary)
                            Text("−${formatMoney(uiState.totalExpensesMonth)}",
                                fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Red)
                        }
                    }
                }
            }

            // Best stream highlight
            uiState.bestStream?.let { best ->
                if (best.monthAmount > 0) {
                    BestStreamCard(best)
                }
            }

            // Streams list
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("YOUR STREAMS", fontSize = 11.sp, fontWeight = FontWeight.Bold,
                    color = TextMuted, letterSpacing = 2.sp)
                TextButton(onClick = onAnalytics) {
                    Text("Analytics →", color = Accent, fontSize = 12.sp)
                }
            }

            if (uiState.summaries.isEmpty()) {
                EmptyStreamsCard(onAddStream)
            } else {
                uiState.summaries.forEach { summary ->
                    StreamCard(summary = summary)
                }
            }

            // ── REVIEW NUDGE (shows after 7-day streak) ─────────────────
            if (uiState.overallStreak >= 7) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Gold.copy(alpha = 0.12f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("⭐", fontSize = 24.sp)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "${uiState.overallStreak} days logging — loving StreamIQ?",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                "A quick review helps other solopreneurs find this app 💰",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            // ── UNICORN FEATURE GRID ─────────────────────────────────────
            Spacer(modifier = Modifier.height(12.dp))

            // Pro upgrade banner (only show if not Pro)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clickable { onPro() },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF9C27B0).copy(0.12f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF9C27B0).copy(0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Text("⚡", fontSize = 22.sp)
                        Column {
                            Text("Unlock StreamIQ Pro",
                                fontWeight = FontWeight.Bold, fontSize = 14.sp,
                                color = TextPrimary)
                            Text("AI Insights · Voice · Forecast · \$4.99/mo",
                                fontSize = 12.sp, color = Color(0xFFCE93D8))
                        }
                    }
                    Text("→", fontSize = 18.sp, color = Color(0xFF9C27B0),
                        fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text("TOOLS", fontSize = 11.sp, fontWeight = FontWeight.Bold,
                color = TextMuted, letterSpacing = 2.sp,
                modifier = Modifier.padding(horizontal = 24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    UnicornFeatureCard("⚡", "Stream Score",
                        "Rate each stream 0–100", Accent, onStreamScore,
                        Modifier.weight(1f))
                    UnicornFeatureCard("🔮", "Forecast",
                        "Month-end projection", Color(0xFF9C27B0), onForecast,
                        Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    UnicornFeatureCard("🏺", "Tax Estimator",
                        "Calculates tax to set aside", Gold, onTaxJar,
                        Modifier.weight(1f))
                    UnicornFeatureCard("🧠", "AI Insights",
                        "Personalised tips", Green, onInsights,
                        Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    UnicornFeatureCard("📊", "Export CSV",
                        "To Excel or Sheets", Color(0xFF4CAF50), onExport,
                        Modifier.weight(1f))
                    UnicornFeatureCard("⚡", "Go Pro",
                        "Unlock AI & Voice", Gold, onPro,
                        Modifier.weight(1f))
                    UnicornFeatureCard("💱", "Currency",
                        "₹ $ € £ and more", Color(0xFF03A9F4), onCurrency,
                        Modifier.weight(1f))
                }
            }

            // ── PRIVACY FOOTER ───────────────────────────────────────────
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "🔒 All data stays on your device. No cloud. No ads. Ever.",
                fontSize = 10.sp,
                color = TextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(100.dp))
        }

        // FAB - Log Today
        ExtendedFloatingActionButton(
            onClick = onLogToday,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            containerColor = Accent,
            contentColor = Background
        ) {
            Icon(Icons.Default.Edit, "Log")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Log Today's Numbers", fontWeight = FontWeight.Bold)
        }

        // Confetti milestone celebration
        ConfettiOverlay(active = showConfetti, onDone = { showConfetti = false })
    }
}

@Composable
fun StatChip(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceVariant)
            .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Text(label, fontSize = 9.sp, color = TextMuted, letterSpacing = 1.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
fun BestStreamCard(summary: StreamSummary) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        streamColor(summary.stream.type).copy(alpha = 0.15f),
                        GoldDim.copy(alpha = 0.1f)
                    )
                )
            )
            .border(1.dp, Gold.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("👑", fontSize = 28.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("TOP EARNER THIS MONTH", fontSize = 9.sp, color = Gold,
                    letterSpacing = 1.sp, fontWeight = FontWeight.Bold)
                Text(summary.stream.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(formatMoney(summary.monthAmount), fontSize = 18.sp,
                    fontWeight = FontWeight.Black, color = Gold)
                if (summary.trend != 0.0) {
                    Text(
                        "${if (summary.trend > 0) "+" else ""}${summary.trend.toInt()}% vs last mo",
                        fontSize = 11.sp,
                        color = if (summary.trend > 0) Green else Red
                    )
                }
            }
        }
    }
}

@Composable
fun StreamCard(summary: StreamSummary) {
    val color = streamColor(summary.stream.type)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 5.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Card)
            .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Type icon
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(summary.stream.type.emoji, fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(summary.stream.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Text(summary.stream.type.label, fontSize = 11.sp, color = TextSecondary)
            // Goal progress bar
            if (summary.stream.monthlyGoal > 0) {
                val progress = (summary.monthAmount / summary.stream.monthlyGoal).coerceIn(0.0, 1.0).toFloat()
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                    color = if (progress >= 1f) Green else color,
                    trackColor = color.copy(alpha = 0.15f)
                )
                Text(
                    "${(progress * 100).toInt()}% of ${formatMoney(summary.stream.monthlyGoal)} goal",
                    fontSize = 9.sp, color = TextMuted
                )
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                formatMoney(summary.monthAmount),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = if (summary.monthAmount > 0) TextPrimary else TextMuted
            )
            if (summary.todayAmount > 0) {
                Text(
                    "+${formatMoney(summary.todayAmount)} today",
                    fontSize = 11.sp,
                    color = Green
                )
            } else {
                Text("no entry today", fontSize = 11.sp, color = TextMuted)
            }
        }

        if (summary.streak > 0) {
            Spacer(modifier = Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🔥", fontSize = 14.sp)
                Text("${summary.streak}", fontSize = 10.sp, color = Gold)
            }
        }
    }
}

@Composable
fun EmptyStreamsCard(onAddStream: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceVariant)
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("💡", fontSize = 40.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Text("Add your first stream", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Text("Track YouTube, websites, crypto and more", fontSize = 13.sp, color = TextSecondary, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onAddStream,
            colors = ButtonDefaults.buttonColors(containerColor = Accent),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("+ Add Stream", color = Background, fontWeight = FontWeight.Bold)
        }
    }
}





@Composable
fun UnicornFeatureCard(
    emoji: String,
    title: String,
    subtitle: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.25f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(emoji, fontSize = 22.sp)
            Spacer(Modifier.height(6.dp))
            Text(title, fontWeight = FontWeight.Bold,
                fontSize = 13.sp, color = TextPrimary)
            Text(subtitle, fontSize = 11.sp, color = TextSecondary, lineHeight = 14.sp)
        }
    }
}
