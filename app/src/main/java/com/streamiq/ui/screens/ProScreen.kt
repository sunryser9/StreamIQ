package com.streamiq.ui.screens

// ═══════════════════════════════════════════════════════════════════════════
//  STREAMIQ PRO — $4.99/month
//  Free: Manual logging, Basic Stats, Export CSV, Tax Estimator, Expenses
//  Pro: AI Insights, Voice Logging, Unlimited Streams, Forecast, Stream Score
// ═══════════════════════════════════════════════════════════════════════════

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProScreen(viewModel: StreamIQViewModel, onBack: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    val isDark = uiState.isDarkTheme
    val bg = if (isDark) Background else LightBackground
    val textPrimary = if (isDark) TextPrimary else LightTextPrimary
    val textSecondary = if (isDark) TextSecondary else LightTextSecondary

    Scaffold(
        containerColor = bg,
        topBar = {
            TopAppBar(
                title = { Text("StreamIQ Pro", color = textPrimary, fontWeight = FontWeight.Bold) },
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
        ) {
            // Hero gradient header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF7B2FBE), Color(0xFF4A0080))
                        )
                    )
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("⚡", fontSize = 48.sp)
                    Spacer(Modifier.height(12.dp))
                    Text("StreamIQ Pro",
                        fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color.White)
                    Spacer(Modifier.height(8.dp))
                    Text("Everything you need to grow your income streams",
                        fontSize = 14.sp, color = Color.White.copy(0.8f),
                        textAlign = TextAlign.Center)
                    Spacer(Modifier.height(20.dp))
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("\$4", fontSize = 42.sp, fontWeight = FontWeight.Black,
                            color = Color.White)
                        Text(".99", fontSize = 24.sp, fontWeight = FontWeight.Bold,
                            color = Color.White.copy(0.9f))
                        Text(" / month", fontSize = 14.sp,
                            color = Color.White.copy(0.7f),
                            modifier = Modifier.padding(bottom = 6.dp))
                    }
                    Text("Cancel anytime", fontSize = 12.sp, color = Color.White.copy(0.6f))
                }
            }

            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // Free vs Pro comparison
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Free column
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDark) Card else LightCard)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("FREE", fontSize = 12.sp, fontWeight = FontWeight.Bold,
                                color = textSecondary, letterSpacing = 1.sp)
                            Spacer(Modifier.height(2.dp))
                            Text("Always free", fontSize = 11.sp, color = textSecondary)
                            Spacer(Modifier.height(12.dp))
                            listOf(
                                "Manual income logging",
                                "Expense tracking",
                                "Basic dashboard",
                                "CSV export",
                                "Tax Estimator",
                                "Up to 3 streams",
                                "Currency selector"
                            ).forEach {
                                FreeFeatureRow(it, textSecondary)
                                Spacer(Modifier.height(6.dp))
                            }
                        }
                    }

                    // Pro column
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.5.dp, Color(0xFF9C27B0)),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF9C27B0).copy(0.08f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("PRO ⚡", fontSize = 12.sp, fontWeight = FontWeight.Bold,
                                color = Color(0xFFCE93D8), letterSpacing = 1.sp)
                            Spacer(Modifier.height(2.dp))
                            Text("\$4.99 / month", fontSize = 11.sp,
                                color = Color(0xFFCE93D8))
                            Spacer(Modifier.height(12.dp))
                            listOf(
                                "Everything in Free",
                                "AI Insights 🧠",
                                "Voice logging 🎙️",
                                "Unlimited streams",
                                "Forecast 🔮",
                                "Stream Score ⚡",
                                "Priority support"
                            ).forEach {
                                ProFeatureRow(it)
                                Spacer(Modifier.height(6.dp))
                            }
                        }
                    }
                }

                // Subscribe button
                Button(
                    onClick = { /* TODO: Connect Google Play Billing */ },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF9C27B0))
                ) {
                    Text("Start Pro — \$4.99/month",
                        fontWeight = FontWeight.Bold, fontSize = 16.sp,
                        color = Color.White)
                }

                // Why pay? trust section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Accent.copy(0.08f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Why pay?", fontWeight = FontWeight.Bold,
                            fontSize = 14.sp, color = textPrimary)
                        Spacer(Modifier.height(8.dp))
                        listOf(
                            "StreamIQ is built by a solo developer — not a VC-funded corp",
                            "Pro revenue keeps the app ad-free and alive forever",
                            "You pay less in a month than one coffee — and this helps your income"
                        ).forEach { reason ->
                            Row(
                                modifier = Modifier.padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("💜", fontSize = 13.sp)
                                Text(reason, fontSize = 12.sp, color = textSecondary,
                                    lineHeight = 17.sp)
                            }
                        }
                    }
                }

                // No tricks
                Text(
                    "No ads. No data selling. No surprise charges. Cancel in 1 tap.",
                    fontSize = 12.sp, color = textSecondary, textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun FreeFeatureRow(text: String, color: Color) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Check, null,
            tint = color, modifier = Modifier.size(14.dp))
        Text(text, fontSize = 12.sp, color = color, lineHeight = 16.sp)
    }
}

@Composable
private fun ProFeatureRow(text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Check, null,
            tint = Color(0xFFCE93D8), modifier = Modifier.size(14.dp))
        Text(text, fontSize = 12.sp, color = Color(0xFFCE93D8), lineHeight = 16.sp)
    }
}
