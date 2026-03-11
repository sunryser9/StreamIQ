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
    val amounts = remember { mutableStateMapOf<String, String>() }

    // Pre-fill with today's existing entries
    LaunchedEffect(uiState.entries) {
        uiState.streams.forEach { stream ->
            val existing = viewModel.getTodayEntryForStream(stream.id)
            if (existing > 0 && !amounts.containsKey(stream.id)) {
                amounts[stream.id] = existing.toInt().toString()
            }
        }
    }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("Log Today", color = TextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onDone) {
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
            Text(
                "How much did each stream earn today?",
                color = TextSecondary,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Enter $0 if nothing today — streaks track active days.",
                color = TextMuted,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(24.dp))

            if (uiState.streams.isEmpty()) {
                Text("No streams yet. Add streams first!", color = TextSecondary)
            }

            uiState.streams.forEach { stream ->
                StreamEntryRow(
                    stream = stream,
                    value = amounts[stream.id] ?: "",
                    onValueChange = { amounts[stream.id] = it }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Total preview
            val total = amounts.values.sumOf { it.toDoubleOrNull() ?: 0.0 }
            if (total > 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Green.copy(alpha = 0.1f))
                        .border(1.dp, Green.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Today's Total", color = TextSecondary, fontWeight = FontWeight.Bold)
                    Text(formatMoney(total), color = Green, fontWeight = FontWeight.Black, fontSize = 18.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Button(
                onClick = {
                    amounts.forEach { (streamId, amountStr) ->
                        val amount = amountStr.toDoubleOrNull() ?: 0.0
                        viewModel.logEntry(streamId, amount)
                    }
                    onDone()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Accent),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Save Today's Log ✓", color = Background, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun StreamEntryRow(stream: IncomeStream, value: String, onValueChange: (String) -> Unit) {
    val color = streamColor(stream.type)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Card)
            .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(stream.type.emoji, fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(stream.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Text(stream.type.label, fontSize = 11.sp, color = TextSecondary)
        }

        Spacer(modifier = Modifier.width(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("$", color = TextSecondary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.width(100.dp),
                placeholder = { Text("0", color = TextMuted) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = color,
                    unfocusedBorderColor = CardBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = color
                ),
                shape = RoundedCornerShape(10.dp),
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            )
        }
    }
}
