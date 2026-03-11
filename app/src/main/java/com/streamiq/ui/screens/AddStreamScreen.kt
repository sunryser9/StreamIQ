package com.streamiq.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.streamiq.data.StreamType
import com.streamiq.ui.theme.*
import com.streamiq.ui.viewmodel.StreamIQViewModel
import com.streamiq.utils.streamColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStreamScreen(viewModel: StreamIQViewModel, onDone: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    var streamName by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(StreamType.YOUTUBE) }
    var showDelete by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("Manage Streams", color = TextPrimary, fontWeight = FontWeight.Bold) },
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
            Text("Add New Stream", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(modifier = Modifier.height(16.dp))

            // Type selector
            Text("Type", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StreamType.entries.forEach { type ->
                    val selected = type == selectedType
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (selected) streamColor(type).copy(alpha = 0.2f) else SurfaceVariant)
                            .border(1.dp, if (selected) streamColor(type) else CardBorder, RoundedCornerShape(12.dp))
                            .clickable { selectedType = type }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(type.emoji, fontSize = 22.sp)
                            Text(type.label.split("/")[0], fontSize = 10.sp,
                                color = if (selected) streamColor(type) else TextSecondary)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = streamName,
                onValueChange = { streamName = it },
                label = { Text("Stream name") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Accent,
                    unfocusedBorderColor = CardBorder,
                    focusedLabelColor = Accent,
                    unfocusedLabelColor = TextSecondary,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = Accent
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            var monthlyGoalText by remember { mutableStateOf("") }
            OutlinedTextField(
                value = monthlyGoalText,
                onValueChange = { monthlyGoalText = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Monthly goal $ (optional)") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Gold,
                    unfocusedBorderColor = CardBorder,
                    focusedLabelColor = Gold,
                    unfocusedLabelColor = TextSecondary,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = Gold
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                ),
                leadingIcon = { Text("$", color = Gold, fontWeight = FontWeight.Bold) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    if (streamName.isNotBlank()) {
                        viewModel.addStream(streamName.trim(), selectedType)
                        val goal = monthlyGoalText.toDoubleOrNull() ?: 0.0
                        if (goal > 0) {
                            // goal will be set after stream is created — find by name
                            viewModel.setGoalForLastStream(goal)
                        }
                        streamName = ""
                        monthlyGoalText = ""
                    }
                },
                enabled = streamName.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Accent),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("+ Add Stream", color = Background, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            if (uiState.streams.isNotEmpty()) {
                Spacer(modifier = Modifier.height(32.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Your Streams", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    TextButton(onClick = { showDelete = !showDelete }) {
                        Text(if (showDelete) "Done" else "Edit", color = Accent, fontSize = 13.sp)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                uiState.streams.forEach { stream ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Card)
                            .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stream.type.emoji, fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(stream.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            Text(stream.type.label, fontSize = 11.sp, color = TextSecondary)
                        }
                        if (showDelete) {
                            IconButton(onClick = { viewModel.deleteStream(stream.id) }) {
                                Icon(Icons.Default.Delete, "Delete", tint = Red, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
