package com.streamiq.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.*
import com.streamiq.data.IncomeStream
import com.streamiq.ui.theme.*
import com.streamiq.ui.viewmodel.StreamIQViewModel
import com.streamiq.utils.formatMoney
import com.streamiq.utils.streamColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogTodayScreen(viewModel: StreamIQViewModel, onDone: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    val isDark = uiState.isDarkTheme
    val bg = if (isDark) Background else LightBackground
    val cardBg = if (isDark) Card else LightCard
    val textPrimary = if (isDark) TextPrimary else LightTextPrimary
    val textSecondary = if (isDark) TextSecondary else LightTextSecondary
    val textMuted = if (isDark) TextMuted else LightTextMuted
    val border = if (isDark) CardBorder else LightCardBorder

    val incomeAmounts = remember { mutableStateMapOf<String, String>() }
    val expenseAmounts = remember { mutableStateMapOf<String, String>() }
    // Toggle: true = logging income, false = logging expense
    var logMode by remember { mutableStateOf(true) }

    LaunchedEffect(uiState.entries) {
        uiState.streams.forEach { stream ->
            val existing = viewModel.getTodayEntryForStream(stream.id)
            if (existing > 0 && !incomeAmounts.containsKey(stream.id)) {
                incomeAmounts[stream.id] = existing.toInt().toString()
            }
        }
    }

    val totalIncome = incomeAmounts.values.sumOf { it.toDoubleOrNull() ?: 0.0 }
    val totalExpense = expenseAmounts.values.sumOf { it.toDoubleOrNull() ?: 0.0 }
    val netProfit = totalIncome - totalExpense

    Scaffold(
        containerColor = bg,
        topBar = {
            TopAppBar(
                title = { Text("Log Today", color = textPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onDone) {
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
                .padding(24.dp)
        ) {

            // Income / Expense toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isDark) SurfaceVariant else LightSurfaceVariant)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Income tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (logMode) Green.copy(0.2f) else Color.Transparent)
                        .border(
                            if (logMode) 1.dp else 0.dp,
                            if (logMode) Green.copy(0.5f) else Color.Transparent,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { logMode = true }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "💰 Income",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (logMode) Green else textSecondary
                    )
                }
                // Expense tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (!logMode) Red.copy(0.2f) else Color.Transparent)
                        .border(
                            if (!logMode) 1.dp else 0.dp,
                            if (!logMode) Red.copy(0.5f) else Color.Transparent,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { logMode = false }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "💸 Expense",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (!logMode) Red else textSecondary
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            if (logMode) {
                Text("How much did each stream earn today?",
                    color = textSecondary, fontSize = 14.sp)
                Spacer(Modifier.height(4.dp))
                Text("Enter 0 if nothing today — streaks track active days.",
                    color = textMuted, fontSize = 12.sp)
            } else {
                Text("Log today's business expenses.",
                    color = textSecondary, fontSize = 14.sp)
                Spacer(Modifier.height(4.dp))
                Text("Hosting, software, equipment, ads, etc.",
                    color = textMuted, fontSize = 12.sp)
            }

            Spacer(Modifier.height(20.dp))

            if (uiState.streams.isEmpty()) {
                Text("No streams yet. Add streams first!", color = textSecondary)
            }

            if (logMode) {
                // Income entries per stream
                uiState.streams.forEach { stream ->
                    StreamEntryRow(
                        stream = stream,
                        value = incomeAmounts[stream.id] ?: "",
                        onValueChange = { incomeAmounts[stream.id] = it },
                        accentColor = streamColor(stream.type),
                        prefix = "+",
                        cardBg = cardBg,
                        border = border,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary,
                        textMuted = textMuted,
                        currencySymbol = com.streamiq.utils.selectedCurrency.symbol
                    )
                    Spacer(Modifier.height(12.dp))
                }
            } else {
                // Expense entries — common categories + per-stream
                val expenseCategories = listOf(
                    "hosting" to "🖥️" to "Hosting / Server",
                    "software" to "💻" to "Software / Tools",
                    "ads" to "📣" to "Ads / Marketing",
                    "equipment" to "🎙️" to "Equipment / Hardware",
                    "freelancer" to "👤" to "Freelancer / Outsourcing",
                    "other" to "📦" to "Other Expenses"
                )
                expenseCategories.forEach { (keyEmoji, label) ->
                    val (key, emoji) = keyEmoji
                    ExpenseEntryRow(
                        label = label,
                        emoji = emoji,
                        value = expenseAmounts[key] ?: "",
                        onValueChange = { expenseAmounts[key] = it },
                        cardBg = cardBg,
                        border = border,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary,
                        textMuted = textMuted,
                        currencySymbol = com.streamiq.utils.selectedCurrency.symbol
                    )
                    Spacer(Modifier.height(10.dp))
                }
            }

            Spacer(Modifier.height(20.dp))

            // Net profit summary — always visible if any numbers entered
            if (totalIncome > 0 || totalExpense > 0) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (netProfit >= 0) Green.copy(0.08f) else Red.copy(0.08f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Revenue", fontSize = 13.sp, color = textSecondary)
                            Text(formatMoney(totalIncome), fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold, color = Green)
                        }
                        if (totalExpense > 0) {
                            Spacer(Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Expenses", fontSize = 13.sp, color = textSecondary)
                                Text("−${formatMoney(totalExpense)}", fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold, color = Red)
                            }
                            Divider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = if (isDark) CardBorder else LightCardBorder
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("NET PROFIT", fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold, color = textPrimary)
                                Text(
                                    "${if (netProfit >= 0) "+" else ""}${formatMoney(netProfit)}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (netProfit >= 0) Green else Red
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            Button(
                onClick = {
                    // Save income entries
                    incomeAmounts.forEach { (streamId, amountStr) ->
                        val amount = amountStr.toDoubleOrNull() ?: 0.0
                        viewModel.logEntry(streamId, amount)
                    }
                    // Save expenses as negative entries on a special "expenses" stream
                    if (expenseAmounts.values.any { (it.toDoubleOrNull() ?: 0.0) > 0 }) {
                        viewModel.logExpenses(expenseAmounts.mapValues {
                            it.value.toDoubleOrNull() ?: 0.0 })
                    }
                    onDone()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Accent),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Save Today's Log ✓", color = Background,
                    fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun StreamEntryRow(
    stream: IncomeStream,
    value: String,
    onValueChange: (String) -> Unit,
    accentColor: androidx.compose.ui.graphics.Color,
    prefix: String = "+",
    cardBg: androidx.compose.ui.graphics.Color,
    border: androidx.compose.ui.graphics.Color,
    textPrimary: androidx.compose.ui.graphics.Color,
    textSecondary: androidx.compose.ui.graphics.Color,
    textMuted: androidx.compose.ui.graphics.Color,
    currencySymbol: String = "$"
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(cardBg)
            .border(1.dp, border, RoundedCornerShape(14.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(accentColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(stream.type.emoji, fontSize = 18.sp)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(stream.name, fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold, color = textPrimary)
            Text(stream.type.label, fontSize = 11.sp, color = textSecondary)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(currencySymbol, color = Green, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.width(100.dp),
                placeholder = { Text("0", color = textMuted) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accentColor,
                    unfocusedBorderColor = border,
                    focusedTextColor = textPrimary,
                    unfocusedTextColor = textPrimary,
                    cursorColor = accentColor
                ),
                shape = RoundedCornerShape(10.dp),
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(
                    fontWeight = FontWeight.Bold, fontSize = 16.sp)
            )
        }
    }
}

@Composable
fun ExpenseEntryRow(
    label: String,
    emoji: String,
    value: String,
    onValueChange: (String) -> Unit,
    cardBg: androidx.compose.ui.graphics.Color,
    border: androidx.compose.ui.graphics.Color,
    textPrimary: androidx.compose.ui.graphics.Color,
    textSecondary: androidx.compose.ui.graphics.Color,
    textMuted: androidx.compose.ui.graphics.Color,
    currencySymbol: String = "$"
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(cardBg)
            .border(1.dp, border, RoundedCornerShape(14.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Red.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Text(emoji, fontSize = 18.sp)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(label, modifier = Modifier.weight(1f),
            fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = textPrimary)
        Spacer(modifier = Modifier.width(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("−$currencySymbol", color = Red,
                fontSize = 14.sp, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.width(100.dp),
                placeholder = { Text("0", color = textMuted) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Red,
                    unfocusedBorderColor = border,
                    focusedTextColor = textPrimary,
                    unfocusedTextColor = textPrimary,
                    cursorColor = Red
                ),
                shape = RoundedCornerShape(10.dp),
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(
                    fontWeight = FontWeight.Bold, fontSize = 16.sp)
            )
        }
    }
}
