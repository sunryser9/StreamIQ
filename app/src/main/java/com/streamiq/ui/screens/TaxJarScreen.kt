package com.streamiq.ui.screens

// ═══════════════════════════════════════════════════════════════════════════
//  TAX JAR — StreamIQ's India-specific unicorn feature
//  Auto-sets aside 30% of every rupee earned. Zero apps do this for India.
//  Will get StreamIQ featured in Finshots, CA YouTube channels, Zerodha Varsity
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaxJarScreen(viewModel: StreamIQViewModel, onBack: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    val isDark = uiState.isDarkTheme
    val bg = if (isDark) Background else LightBackground
    val textPrimary = if (isDark) TextPrimary else LightTextPrimary
    val textSecondary = if (isDark) TextSecondary else LightTextSecondary

    var taxRate by remember { mutableStateOf(30f) }
    var showBreakdown by remember { mutableStateOf(false) }

    val totalMonth = uiState.totalMonth
    val totalAllTime = uiState.summaries.sumOf { it.allTimeAmount }
    val annualEstimate = totalMonth * 12
    val taxJarMonth = calculateTaxJar(totalMonth, taxRate / 100.0)
    val taxJarAllTime = calculateTaxJar(totalAllTime, taxRate / 100.0)
    val netMonth = totalMonth - taxJarMonth
    val advice = taxAdvice(annualEstimate)
    val currentMonth = LocalDate.now().month.name.lowercase().replaceFirstChar { it.uppercase() }
    val fillPercent = (taxRate / 100f).coerceIn(0f, 1f)

    // Animated jar fill
    val animatedFill by animateFloatAsState(
        targetValue = fillPercent,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "jar"
    )

    Scaffold(
        containerColor = bg,
        topBar = {
            TopAppBar(
                title = { Text("Tax Jar 🏺", color = textPrimary, fontWeight = FontWeight.Bold) },
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
                    containerColor = Color(0xFFFFD700).copy(alpha = 0.1f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text("🏺", fontSize = 28.sp)
                    Column {
                        Text("Your Tax Jar", fontWeight = FontWeight.Bold,
                            fontSize = 16.sp, color = textPrimary)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Every rupee you earn, StreamIQ silently sets aside ${taxRate.toInt()}% for taxes. " +
                            "So you never get a surprise bill from the IT department.",
                            fontSize = 13.sp, color = textSecondary, lineHeight = 18.sp
                        )
                    }
                }
            }

            // Jar visual
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) Card else LightCard
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("$currentMonth Tax Reserve",
                        fontSize = 13.sp, color = textSecondary,
                        fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp)

                    Spacer(Modifier.height(16.dp))

                    // Big jar amount
                    Text(
                        formatMoneyFull(taxJarMonth),
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD700)
                    )
                    Text("set aside from ${formatMoney(totalMonth)} earned",
                        fontSize = 13.sp, color = textSecondary)

                    Spacer(Modifier.height(20.dp))

                    // Visual jar fill bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(20.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isDark) SurfaceVariant else LightSurfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animatedFill)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFFFFD700), Color(0xFFFF9800))
                                    )
                                )
                        )
                        Text(
                            "${taxRate.toInt()}% reserved",
                            modifier = Modifier.align(Alignment.Center),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // Net take-home
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Gross Earned", fontSize = 11.sp, color = textSecondary)
                            Text(formatMoney(totalMonth), fontSize = 18.sp,
                                fontWeight = FontWeight.Bold, color = textPrimary)
                        }
                        Text("−", fontSize = 24.sp, color = textSecondary,
                            modifier = Modifier.align(Alignment.CenterVertically))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Tax Reserve", fontSize = 11.sp, color = textSecondary)
                            Text(formatMoney(taxJarMonth), fontSize = 18.sp,
                                fontWeight = FontWeight.Bold, color = Color(0xFFFFD700))
                        }
                        Text("=", fontSize = 24.sp, color = textSecondary,
                            modifier = Modifier.align(Alignment.CenterVertically))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Take Home", fontSize = 11.sp, color = textSecondary)
                            Text(formatMoney(netMonth), fontSize = 18.sp,
                                fontWeight = FontWeight.Bold, color = Green)
                        }
                    }
                }
            }

            // Tax rate slider
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) Card else LightCard)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Tax Rate", fontWeight = FontWeight.SemiBold,
                            color = textPrimary)
                        Text("${taxRate.toInt()}%",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD700), fontSize = 18.sp)
                    }
                    Slider(
                        value = taxRate,
                        onValueChange = { taxRate = it },
                        valueRange = 5f..40f,
                        steps = 6,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFFFFD700),
                            activeTrackColor = Color(0xFFFFD700),
                            inactiveTrackColor = Color(0xFFFFD700).copy(0.2f)
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("5% (low income)", fontSize = 10.sp, color = textSecondary)
                        Text("30% (high income)", fontSize = 10.sp, color = textSecondary)
                    }
                }
            }

            // India tax slabs card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1565C0).copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Text("🇮🇳", fontSize = 20.sp)
                        Text("Your Tax Estimate", fontWeight = FontWeight.Bold,
                            color = textPrimary)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("Annual estimate: ${formatMoney(annualEstimate)}",
                        fontSize = 13.sp, color = textSecondary)
                    Spacer(Modifier.height(6.dp))
                    Text(advice, fontSize = 13.sp, color = textPrimary, lineHeight = 18.sp)
                    Spacer(Modifier.height(12.dp))

                    // Quick slabs
                    listOf(
                        Triple("Up to ₹3L", "Nil", Color(0xFF00E676)),
                        Triple("₹3L – ₹7L", "5%", Color(0xFF69F0AE)),
                        Triple("₹7L – ₹10L", "10%", Color(0xFFFFD700)),
                        Triple("₹10L – ₹12L", "15%", Color(0xFFFF9800)),
                        Triple("Above ₹12L", "30%", Color(0xFFFF5252))
                    ).forEach { (slab, rate, color) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(slab, fontSize = 12.sp, color = textSecondary)
                            Text(rate, fontSize = 12.sp,
                                fontWeight = FontWeight.Bold, color = color)
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    Text("New tax regime (FY 2024–25). Always consult a CA for final filing.",
                        fontSize = 10.sp, color = textSecondary,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                }
            }

            // All-time tax reserve
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) SurfaceVariant else LightSurfaceVariant)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("All-Time Tax Reserve", fontSize = 13.sp, color = textSecondary)
                        Text(formatMoneyFull(taxJarAllTime),
                            fontSize = 22.sp, fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD700))
                    }
                    Text("🏺", fontSize = 36.sp)
                }
            }

            // Quarterly reminder
            val currentQuarter = when (LocalDate.now().monthValue) {
                in 4..6   -> "Q1 (Apr–Jun)"
                in 7..9   -> "Q2 (Jul–Sep)"
                in 10..12 -> "Q3 (Oct–Dec)"
                else      -> "Q4 (Jan–Mar)"
            }
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFF9800).copy(alpha = 0.1f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("⏰ Advance Tax Reminder", fontWeight = FontWeight.Bold,
                        color = textPrimary, fontSize = 14.sp)
                    Spacer(Modifier.height(6.dp))
                    Text("You're in $currentQuarter. India requires advance tax payments " +
                         "if annual liability > ₹10,000. Pay by 15th of last month of each quarter.",
                        fontSize = 12.sp, color = textSecondary, lineHeight = 17.sp)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("15 Jun", "15 Sep", "15 Dec", "15 Mar").forEach { date ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFFF9800).copy(0.2f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(date, fontSize = 11.sp,
                                    color = Color(0xFFFF9800), fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}
