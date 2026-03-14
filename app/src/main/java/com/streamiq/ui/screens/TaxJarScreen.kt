package com.streamiq.ui.screens

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

    var taxRate by remember { mutableStateOf(25f) }
    val totalMonth = uiState.totalMonth
    val totalAllTime = uiState.summaries.sumOf { it.allTimeAmount }
    val taxJarMonth = calculateTaxJar(totalMonth, taxRate / 100.0)
    val taxJarAllTime = calculateTaxJar(totalAllTime, taxRate / 100.0)
    val netMonth = totalMonth - taxJarMonth
    val currentMonth = LocalDate.now().month.name.lowercase().replaceFirstChar { it.uppercase() }
    val animatedFill by animateFloatAsState(
        targetValue = (taxRate / 100f).coerceIn(0f, 1f),
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "jar"
    )

    Scaffold(
        containerColor = bg,
        topBar = {
            TopAppBar(
                title = { Text("Tax Estimator 🧾", color = textPrimary, fontWeight = FontWeight.Bold) },
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
                    containerColor = Color(0xFFFFD700).copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text("🏺", fontSize = 28.sp)
                    Column {
                        Text("Your Tax Estimator", fontWeight = FontWeight.Bold,
                            fontSize = 16.sp, color = textPrimary)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Set your local tax rate below. StreamIQ automatically " +
                            "calculates how much to estimated from every rupee you earn — " +
                            "so tax time is never a surprise.",
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
                    containerColor = if (isDark) Card else LightCard)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("$currentMonth Tax Estimate",
                        fontSize = 13.sp, color = textSecondary,
                        fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp)

                    Spacer(Modifier.height(16.dp))

                    Text(
                        formatMoneyFull(taxJarMonth),
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD700)
                    )
                    Text("estimated from ${formatMoney(totalMonth)} earned",
                        fontSize = 13.sp, color = textSecondary)

                    Spacer(Modifier.height(20.dp))

                    // Jar fill bar
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
                            "${taxRate.toInt()}% estimated",
                            modifier = Modifier.align(Alignment.Center),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // Gross / Tax / Net
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
                            Text("Tax Estimate", fontSize = 11.sp, color = textSecondary)
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

            // Tax rate slider — user sets their own rate
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
                        Column {
                            Text("Your Tax Rate", fontWeight = FontWeight.SemiBold,
                                color = textPrimary)
                            Text("Set based on your local tax laws",
                                fontSize = 11.sp, color = textSecondary)
                        }
                        Text("${taxRate.toInt()}%",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD700), fontSize = 24.sp)
                    }
                    Spacer(Modifier.height(8.dp))
                    Slider(
                        value = taxRate,
                        onValueChange = { taxRate = it },
                        valueRange = 5f..50f,
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
                        Text("5%", fontSize = 11.sp, color = textSecondary)
                        Text("50%", fontSize = 11.sp, color = textSecondary)
                    }

                    Spacer(Modifier.height(12.dp))

                    // Common rate chips for quick selection
                    Text("Common rates:", fontSize = 12.sp, color = textSecondary)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(15, 20, 25, 30, 40).forEach { rate ->
                            val isSelected = taxRate.toInt() == rate
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(
                                        if (isSelected) Color(0xFFFFD700)
                                        else Color(0xFFFFD700).copy(0.15f)
                                    )
                                    .clickable { taxRate = rate.toFloat() }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("$rate%",
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.Black else Color(0xFFFFD700))
                            }
                        }
                    }
                }
            }

            // General advice card — no country-specific numbers
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Accent.copy(alpha = 0.08f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("💡 Why set aside tax as you earn?",
                        fontWeight = FontWeight.Bold, fontSize = 14.sp, color = textPrimary)
                    Spacer(Modifier.height(8.dp))
                    listOf(
                        "Most countries require self-employed people to pay tax periodically — not just at year end.",
                        "Setting aside a percentage with every payment means no nasty surprises.",
                        "Check your local tax authority for the right rate — a tax professional can advise.",
                        "The jar balance shown here is a guide. Always confirm with your accountant."
                    ).forEach { tip ->
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("•", color = Accent, fontSize = 14.sp)
                            Text(tip, fontSize = 12.sp, color = textSecondary, lineHeight = 17.sp)
                        }
                    }
                }
            }

            // All-time reserve
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
                        Text("All-Time Tax Estimate", fontSize = 13.sp, color = textSecondary)
                        Text(formatMoneyFull(taxJarAllTime),
                            fontSize = 22.sp, fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD700))
                        Text("at ${taxRate.toInt()}% rate",
                            fontSize = 11.sp, color = textSecondary)
                    }
                    Text("🏺", fontSize = 36.sp)
                }
            }

            // Disclaimer
            Text(
                "This is an estimate only. Consult a qualified tax professional for advice specific to your situation and country.",
                fontSize = 11.sp,
                color = textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )

            Spacer(Modifier.height(40.dp))
        }
    }
}
