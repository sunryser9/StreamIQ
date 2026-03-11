package com.streamiq.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.core.content.ContextCompat
import com.streamiq.ui.theme.*
import com.streamiq.ui.viewmodel.StreamIQViewModel
import com.streamiq.voice.VoiceManager
import com.streamiq.voice.VoiceResult
import com.streamiq.voice.VoiceState
import com.streamiq.utils.formatMoney
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@Composable
fun VoiceBotScreen(
    viewModel: StreamIQViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val isDark = uiState.isDarkTheme
    val bgColor = if (isDark) Background else Color(0xFFF0F4FF)
    val textColor = if (isDark) TextPrimary else Color(0xFF0A0F1E)

    val voiceManager = remember { VoiceManager(context) }
    val voiceState by voiceManager.voiceState.collectAsState()
    val lastHeard by voiceManager.lastHeard.collectAsState()

    var conversation by remember { mutableStateOf(listOf<Pair<Boolean, String>>()) } // isBot, message
    var pendingResult by remember { mutableStateOf<VoiceResult?>(null) }
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                    == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasPermission = granted }

    // Pulse animation for mic button
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ), label = "pulse"
    )

    fun addMessage(isBot: Boolean, text: String) {
        conversation = conversation + Pair(isBot, text)
    }

    fun startConversation() {
        val greeting = voiceManager.morningBriefing(
            totalMonth = uiState.totalMonth,
            totalToday = uiState.totalToday,
            streak = uiState.overallStreak,
            bestStream = uiState.bestStream?.stream?.name
        )
        addMessage(true, greeting)
        voiceManager.speak(greeting, thenListen = true)

        voiceManager.onVoiceResult = { result ->
            addMessage(false, result.text)
            when {
                result.amount != null -> {
                    pendingResult = result
                    val streamName = result.streamName ?: uiState.streams.firstOrNull()?.name ?: "your stream"
                    val confirmMsg = "I heard ${result.amount.toInt()} dollars" +
                            (if (result.streamName != null) " from ${result.streamName}" else "") +
                            ". Should I log it? Say yes to confirm."
                    addMessage(true, confirmMsg)
                    voiceManager.speak(confirmMsg, thenListen = true)
                }
                result.text.lowercase().contains("yes") && pendingResult != null -> {
                    val pr = pendingResult!!
                    val stream = uiState.streams.find {
                        it.name.lowercase().contains(pr.streamName?.lowercase() ?: "")
                    } ?: uiState.streams.firstOrNull()
                    if (stream != null && pr.amount != null) {
                        viewModel.logEntry(stream.id, pr.amount)
                        val newTotal = uiState.totalMonth + pr.amount
                        val confirmation = voiceManager.logConfirmation(pr.amount, stream.name, newTotal)
                        addMessage(true, confirmation)
                        voiceManager.speak(confirmation)
                        pendingResult = null
                        // Check milestone
                        val milestones = listOf(100.0, 500.0, 1000.0)
                        val crossed = milestones.any { newTotal >= it && uiState.totalMonth < it }
                        if (crossed) {
                            CoroutineScope(Dispatchers.Main).launch {
                                delay(2000)
                                val milestone = voiceManager.milestoneMessage(newTotal)
                                addMessage(true, milestone)
                                voiceManager.speak(milestone)
                            }
                        }
                    }
                }
                result.text.lowercase().contains("no") && pendingResult != null -> {
                    pendingResult = null
                    val msg = "No problem! Tell me the correct amount when you're ready."
                    addMessage(true, msg)
                    voiceManager.speak(msg, thenListen = true)
                }
                result.text.lowercase().contains("total") || result.text.lowercase().contains("how much") -> {
                    val msg = "Your total this month is ${formatMoney(uiState.totalMonth)}. Today you earned ${formatMoney(uiState.totalToday)}."
                    addMessage(true, msg)
                    voiceManager.speak(msg)
                }
                else -> {
                    val err = voiceManager.errorResponse(result.text)
                    addMessage(true, err)
                    voiceManager.speak(err, thenListen = true)
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose { voiceManager.destroy() }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(bgColor)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(AccentGlow, bgColor)))
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { voiceManager.stopSpeaking(); onBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Accent)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("💬 Voice Assistant", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = textColor)
                        Text("Talk to StreamIQ", fontSize = 12.sp, color = Accent)
                    }
                    Spacer(Modifier.width(48.dp))
                }
            }

            // Conversation area
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Spacer(Modifier.height(8.dp))

                if (conversation.isEmpty()) {
                    // Welcome card
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Text("🎙️", fontSize = 56.sp)
                            Text(
                                "Your AI Income Assistant",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                "Tap the mic and say:\n\"I made 80 dollars from freelance\"",
                                fontSize = 14.sp,
                                color = if (isDark) TextSecondary else Color(0xFF666666),
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(8.dp))
                            // Hint chips
                            listOf(
                                "📊 Get monthly total",
                                "💰 Log earnings by voice",
                                "🔥 Check your streak"
                            ).forEach { hint ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(if (isDark) Surface else Color(0xFFE8EAF6))
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Text(hint, fontSize = 13.sp, color = Accent)
                                }
                            }
                        }
                    }
                }

                conversation.forEach { (isBot, message) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isBot) Arrangement.Start else Arrangement.End
                    ) {
                        if (isBot) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Accent),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("💰", fontSize = 16.sp)
                            }
                            Spacer(Modifier.width(8.dp))
                        }
                        Box(
                            modifier = Modifier
                                .widthIn(max = 260.dp)
                                .clip(
                                    RoundedCornerShape(
                                        topStart = if (isBot) 4.dp else 16.dp,
                                        topEnd = 16.dp,
                                        bottomStart = 16.dp,
                                        bottomEnd = if (isBot) 16.dp else 4.dp
                                    )
                                )
                                .background(
                                    if (isBot)
                                        (if (isDark) Surface else Color(0xFFE3F2FD))
                                    else
                                        Accent
                                )
                                .padding(12.dp)
                        ) {
                            Text(
                                message,
                                fontSize = 14.sp,
                                color = if (isBot) textColor else Color.White
                            )
                        }
                    }
                }

                // Status indicator
                if (voiceState != VoiceState.IDLE) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isDark) Surface else Color(0xFFE8F5E9))
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                when (voiceState) {
                                    VoiceState.SPEAKING -> "🔊 Speaking..."
                                    VoiceState.LISTENING -> "🎙️ Listening..."
                                    VoiceState.PROCESSING -> "⚡ Processing..."
                                    else -> ""
                                },
                                fontSize = 13.sp, color = Accent
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
            }

            // Bottom mic area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isDark) Surface else Color.White)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Main mic button
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .scale(if (voiceState == VoiceState.LISTENING) pulseScale else 1f)
                            .clip(CircleShape)
                            .background(
                                when (voiceState) {
                                    VoiceState.LISTENING -> Color(0xFFE53935)
                                    VoiceState.SPEAKING -> Accent
                                    else -> Accent
                                }
                            )
                            .border(
                                3.dp,
                                when (voiceState) {
                                    VoiceState.LISTENING -> Color(0xFFFF5252)
                                    else -> AccentGlow
                                },
                                CircleShape
                            )
                            .clickable {
                                when {
                                    !hasPermission -> permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    voiceState == VoiceState.SPEAKING -> voiceManager.stopSpeaking()
                                    voiceState == VoiceState.LISTENING -> voiceManager.stopListening()
                                    conversation.isEmpty() -> startConversation()
                                    else -> {
                                        val prompt = "What would you like to log?"
                                        addMessage(true, prompt)
                                        voiceManager.speak(prompt, thenListen = true)
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            when (voiceState) {
                                VoiceState.LISTENING -> Icons.Default.MicOff
                                VoiceState.SPEAKING -> Icons.Default.VolumeUp
                                else -> Icons.Default.Mic
                            },
                            contentDescription = "Mic",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Text(
                        when {
                            !hasPermission -> "Tap to allow microphone"
                            voiceState == VoiceState.LISTENING -> "Tap to stop"
                            voiceState == VoiceState.SPEAKING -> "Tap to interrupt"
                            conversation.isEmpty() -> "Tap to start"
                            else -> "Tap to speak"
                        },
                        fontSize = 13.sp,
                        color = if (isDark) TextSecondary else Color(0xFF666666)
                    )
                }
            }
        }
    }
}
