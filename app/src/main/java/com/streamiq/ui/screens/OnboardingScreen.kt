package com.streamiq.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.streamiq.data.StreamType
import com.streamiq.ui.theme.*
import com.streamiq.ui.viewmodel.StreamIQViewModel

@Composable
fun OnboardingScreen(viewModel: StreamIQViewModel, onComplete: () -> Unit) {
    var step by remember { mutableIntStateOf(0) }
    var streamName by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(StreamType.YOUTUBE) }
    val addedStreams = remember { mutableStateListOf<Pair<String, StreamType>>() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        AnimatedContent(targetState = step, transitionSpec = {
            slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
        }) { currentStep ->
            when (currentStep) {
                0 -> OnboardingWelcome(onNext = { step = 1 })
                1 -> OnboardingAddStreams(
                    streamName = streamName,
                    onNameChange = { streamName = it },
                    selectedType = selectedType,
                    onTypeChange = { selectedType = it },
                    addedStreams = addedStreams,
                    onAdd = {
                        if (streamName.isNotBlank()) {
                            addedStreams.add(Pair(streamName, selectedType))
                            streamName = ""
                        }
                    },
                    onDone = {
                        addedStreams.forEach { (name, type) ->
                            viewModel.addStream(name, type)
                        }
                        viewModel.completeOnboarding()
                        onComplete()
                    }
                )
            }
        }
    }
}

@Composable
fun OnboardingWelcome(onNext: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("⚡", fontSize = 64.sp)
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "StreamIQ",
            fontSize = 40.sp,
            fontWeight = FontWeight.Black,
            color = Accent
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Income Stream Tracker",
            fontSize = 16.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
        Text(
            "Built for solopreneurs with multiple income sources",
            fontSize = 12.sp,
            color = TextMuted,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(48.dp))

        listOf(
            "📊" to "See ALL your income in one place",
            "🔥" to "Track which stream earns most",
            "📈" to "Know your real hourly rate per stream",
            "🚀" to "Share your wins with the world"
        ).forEach { (emoji, text) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(emoji, fontSize = 24.sp)
                Spacer(modifier = Modifier.width(16.dp))
                Text(text, color = TextPrimary, fontSize = 15.sp)
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Accent),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                "Set Up My Streams →",
                color = Background,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "🔒 All data stays on your device. No cloud. No accounts. No ads.",
            fontSize = 11.sp,
            color = TextMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun OnboardingAddStreams(
    streamName: String,
    onNameChange: (String) -> Unit,
    selectedType: StreamType,
    onTypeChange: (StreamType) -> Unit,
    addedStreams: List<Pair<String, StreamType>>,
    onAdd: () -> Unit,
    onDone: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        Text("Add Your Streams", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Text("What income sources do you have?", color = TextSecondary, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(32.dp))

        // Type selector
        Text("Stream Type", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StreamType.entries.forEach { type ->
                val selected = type == selectedType
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selected) Accent.copy(alpha = 0.2f) else SurfaceVariant)
                        .border(1.dp, if (selected) Accent else CardBorder, RoundedCornerShape(12.dp))
                        .clickable { onTypeChange(type) }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(type.emoji, fontSize = 20.sp)
                        Text(type.label.split("/")[0], fontSize = 10.sp, color = if (selected) Accent else TextSecondary)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Name input
        OutlinedTextField(
            value = streamName,
            onValueChange = onNameChange,
            label = { Text("Stream name (e.g. Tech Reviews, Blog #1)") },
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

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onAdd,
            enabled = streamName.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariant),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("+ Add Stream", color = Accent, fontWeight = FontWeight.Bold)
        }

        if (addedStreams.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            Text("Added Streams", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            addedStreams.forEach { (name, type) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceVariant)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(type.emoji, fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(name, color = TextPrimary, fontSize = 14.sp)
                    Spacer(modifier = Modifier.weight(1f))
                    Text("✓", color = Green, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onDone,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Accent),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    "Launch My Dashboard →",
                    color = Background,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
