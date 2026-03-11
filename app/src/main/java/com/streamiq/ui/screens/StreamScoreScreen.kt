package com.streamiq.ui.screens

// ═══════════════════════════════════════════════════════════════════════════
//  STREAM SCORE — The feature that makes StreamIQ a coach, not just a tracker
//  Scores each stream 0–100. No competitor does this.
//  "My app told me to quit freelance and focus on YouTube — income doubled"
// ═══════════════════════════════════════════════════════════════════════════

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import com.streamiq.data.StreamSummary
import com.streamiq.ui.theme.*
import com.streamiq.ui.viewmodel.StreamIQViewModel
import com.streamiq.utils.*
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StreamScoreScreen(viewModel: StreamIQViewModel, onBack: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    val isDark = uiState.isDarkTheme
    val bg = if (isDark) Background else LightBackground
    val textPrimary = if (isDark) TextPrimary else LightTextPrimary
    val textSecondary = if (isDark) TextSecondary else LightTextSecondary

    // Calculate scores for all streams
    val scoredStreams = remember(uiState.summaries, uiState.entries) {
        uiState.summaries.map { summary ->
            val thisMonth = LocalDate.now().toString().substring(0, 7)
            val lastMonth = LocalDate.now().minusMonths(1).toString().substring(0, 7)
            val entriesThisMonth = uiState.entries.count {
                it.streamId == summary.stream.id && it.date.startsWith(thisMonth) && it.amount > 0
            }
            val lastMonthAmt = uiState.entries
                .filter { it.streamId == summary.stream.id && it.date.startsWith(lastMonth) }
                .sumOf { it.amount }
            val score = calculateStreamScore(
                monthAmount = summary.monthAmount,
                lastMonthAmount = lastMonthAmt,
                streak = summary.streak,
                allTimeAmount = summary.allTimeAmount,
                entriesThisMonth = entriesThisMonth
            )
            Pair(summary, score)
        }.sortedByDescending { it.second }
    }

    val topStream = scoredStreams.firstOrNull()
    val deadStreams = scoredStreams.filter { it.second < 20 }

    Scaffold(
        containerColor = bg,
        topBar = {
            TopAppBar(
                title = { Text("Stream Scores ⚡", color = textPrimary, fontWeight = FontWeight.Bold) },
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
            // Hero explainer
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Accent.copy(alpha = 0.1f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("⚡ What is Stream Score?",
                        fontWeight = FontWeight.Bold, fontSize = 16.sp, color = textPrimary)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Each stream gets a score 0–100 based on 3 factors: " +
                        "consistency (how often it earns), growth (trending up or down?), " +
                        "and reliability (streak length). Use this to decide where to invest your energy.",
                        fontSize = 13.sp, color = textSecondary, lineHeight = 18.sp
                    )
                }
            }

            // Top performer highlight
            if (topStream != null && topStream.second >= 60) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF00E676).copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("👑", fontSize = 28.sp)
                        Column {
                            Text("DOUBLE DOWN ON THIS",
                                fontSize = 10.sp, fontWeight = FontWeight.Bold,
                                color = Green, letterSpacing = 1.sp)
                            Text(topStream.first.stream.name,
                                fontSize = 18.sp, fontWeight = FontWeight.Bold,
                                color = textPrimary)
                            Text("Score ${topStream.second}/100 — your strongest stream this month",
                                fontSize = 12.sp, color = textSecondary)
                        }
                    }
                }
            }

            // Dead stream alert
            if (deadStreams.isNotEmpty()) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Red.copy(alpha = 0.1f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Text("⚠️", fontSize = 20.sp)
                            Text("${deadStreams.size} Stream${if (deadStreams.size > 1) "s" else ""} Need Attention",
                                fontWeight = FontWeight.Bold, color = Red)
                        }
                        Spacer(Modifier.height(6.dp))
                        deadStreams.forEach { (summary, score) ->
                            Text("• ${summary.stream.name}: score $score/100 — barely earning",
                                fontSize = 13.sp, color = textSecondary)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text("Consider reviving these or focusing energy elsewhere.",
                            fontSize = 12.sp, color = textSecondary,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                    }
                }
            }

            // All stream scores
            Text("All Streams", fontWeight = FontWeight.Bold,
                fontSize = 16.sp, color = textPrimary)

            if (scoredStreams.isEmpty()) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) Card else LightCard)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("📊", fontSize = 40.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("Add streams and log income to see scores",
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp, color = textSecondary)
                    }
                }
            } else {
                scoredStreams.forEach { (summary, score) ->
                    StreamScoreCard(
                        summary = summary,
                        score = score,
                        isDark = isDark,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary
                    )
                }
            }

            // Score legend
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) SurfaceVariant else LightSurfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Score Guide", fontWeight = FontWeight.Bold,
                        fontSize = 14.sp, color = textPrimary)
                    Spacer(Modifier.height(8.dp))
                    listOf(
                        Triple("80–100", "🔥 Thriving", Color(0xFF00E676)),
                        Triple("60–79",  "📈 Growing",  Color(0xFF69F0AE)),
                        Triple("40–59",  "😐 Stable",   Color(0xFFFFD700)),
                        Triple("20–39",  "⚠️ Weak",     Color(0xFFFF9800)),
                        Triple("0–19",   "💀 Dead",     Color(0xFFFF5252))
                    ).forEach { (range, label, color) ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(range, fontSize = 12.sp, color = textSecondary)
                            Text(label, fontSize = 12.sp, color = color,
                                fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun StreamScoreCard(
    summary: StreamSummary,
    score: Int,
    isDark: Boolean,
    textPrimary: Color,
    textSecondary: Color
) {
    val color = scoreColor(score)
    val label = scoreLabel(score)
    val animatedScore by animateIntAsState(
        targetValue = score,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "score"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
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
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(summary.stream.type.emoji, fontSize = 22.sp)
                    Column {
                        Text(summary.stream.name, fontWeight = FontWeight.Bold,
                            fontSize = 15.sp, color = textPrimary)
                        Text(label, fontSize = 12.sp, color = color)
                    }
                }

                // Score circle
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.15f))
                        .border(2.dp, color, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("$animatedScore",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = color)
                }
            }

            Spacer(Modifier.height(12.dp))

            // Score bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (isDark) SurfaceVariant else LightSurfaceVariant)
            ) {
                val animatedWidth by animateFloatAsState(
                    targetValue = score / 100f,
                    animationSpec = tween(800, easing = FastOutSlowInEasing),
                    label = "bar"
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedWidth)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(4.dp))
                        .background(Brush.horizontalGradient(listOf(color.copy(0.6f), color)))
                )
            }

            Spacer(Modifier.height(12.dp))

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ScoreStat("This month", formatMoney(summary.monthAmount), textSecondary, textPrimary)
                ScoreStat("Streak", "${summary.streak}d 🔥", textSecondary, textPrimary)
                ScoreStat("Trend",
                    if (summary.trend >= 0) "+${String.format("%.0f", summary.trend)}%"
                    else "${String.format("%.0f", summary.trend)}%",
                    textSecondary,
                    if (summary.trend >= 0) Green else Red)
            }

            // One action tip
            val tip = getStreamTip(summary, score)
            if (tip.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(color.copy(0.08f))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("💡", fontSize = 14.sp)
                    Text(tip, fontSize = 12.sp, color = textSecondary, lineHeight = 16.sp)
                }
            }
        }
    }
}

@Composable
private fun ScoreStat(label: String, value: String, labelColor: Color, valueColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = valueColor)
        Text(label, fontSize = 10.sp, color = labelColor)
    }
}

private fun getStreamTip(summary: StreamSummary, score: Int): String = when {
    score >= 80 -> "Thriving. Scale this — more content, more clients, more hours."
    score >= 60 && summary.trend > 0 -> "Growing fast. Keep your current pace."
    score >= 40 && summary.trend < 0 -> "Stable but declining. Post/work more consistently."
    score < 20 && summary.streak == 0 -> "No income in days. One small action today can restart momentum."
    score < 30 -> "Weak performance. Consider: is this worth your time vs other streams?"
    else -> ""
}
