package com.streamiq.ui.screens

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.streamiq.ui.theme.*
import com.streamiq.ui.viewmodel.StreamIQViewModel
import com.streamiq.utils.AppCurrency
import com.streamiq.utils.selectedCurrency

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyScreen(viewModel: StreamIQViewModel, onBack: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    val isDark = uiState.isDarkTheme
    val bg = if (isDark) Background else LightBackground
    val textPrimary = if (isDark) TextPrimary else LightTextPrimary
    val textSecondary = if (isDark) TextSecondary else LightTextSecondary
    var picked by remember { mutableStateOf(selectedCurrency) }

    Scaffold(
        containerColor = bg,
        topBar = {
            TopAppBar(
                title = { Text("Currency 💱", color = textPrimary, fontWeight = FontWeight.Bold) },
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
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Accent.copy(0.1f))
            ) {
                Row(modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Text("💱", fontSize = 24.sp)
                    Text(
                        "Choose your primary currency. All amounts display in this currency.",
                        fontSize = 13.sp, color = textSecondary, lineHeight = 18.sp
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            AppCurrency.entries.forEach { currency ->
                val isSelected = picked == currency
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            picked = currency
                            selectedCurrency = currency
                            viewModel.setCurrency(currency)
                        },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Accent.copy(0.15f)
                        else if (isDark) Card else LightCard
                    ),
                    border = if (isSelected) BorderStroke(1.5.dp, Accent) else null
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Text(currency.flag, fontSize = 28.sp)
                            Column {
                                Text(currency.displayName, fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp, color = textPrimary)
                                Text("${currency.code} · ${currency.symbol}",
                                    fontSize = 12.sp, color = textSecondary)
                            }
                        }
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Accent),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Check, "Selected",
                                    tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}
