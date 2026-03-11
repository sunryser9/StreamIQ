package com.streamiq.ui.screens

import android.content.Intent
import android.graphics.*
import android.net.Uri
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.core.content.FileProvider
import com.streamiq.ui.theme.*
import com.streamiq.ui.viewmodel.StreamIQViewModel
import com.streamiq.utils.formatMoney
import com.streamiq.utils.formatMoneyInt
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareCardScreen(viewModel: StreamIQViewModel, onBack: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val month = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM yyyy"))

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("Share Your Wins", color = TextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Preview card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                androidx.compose.ui.graphics.Color(0xFF0A1628),
                                androidx.compose.ui.graphics.Color(0xFF0D2137)
                            )
                        )
                    )
                    .border(1.dp, Accent.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                    .padding(24.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("⚡ StreamIQ", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Accent)
                        Text(month, fontSize = 12.sp, color = TextSecondary)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text("TOTAL EARNED", fontSize = 10.sp, color = TextMuted,
                        letterSpacing = 2.sp, fontWeight = FontWeight.Bold)
                    Text(
                        formatMoney(uiState.totalMonth),
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Black,
                        color = if (uiState.totalMonth > 0) Green else TextSecondary
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Stream breakdown
                    uiState.summaries.filter { it.monthAmount > 0 }.take(5).forEach { summary ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row {
                                Text(summary.stream.type.emoji, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(summary.stream.name, fontSize = 13.sp, color = TextSecondary)
                            }
                            Text(
                                formatMoney(summary.monthAmount),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (summary.monthAmount > 0) TextPrimary else TextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    HorizontalDivider(color = CardBorder)

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("STREAK", fontSize = 9.sp, color = TextMuted, letterSpacing = 1.sp)
                            Text("🔥 ${uiState.overallStreak} days", fontSize = 14.sp,
                                fontWeight = FontWeight.Bold, color = Gold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("STREAMS", fontSize = 9.sp, color = TextMuted, letterSpacing = 1.sp)
                            Text("${uiState.streams.size} active", fontSize = 14.sp,
                                fontWeight = FontWeight.Bold, color = Accent)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "Track all your income streams — streamiq.app",
                        fontSize = 10.sp,
                        color = TextMuted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "Share your monthly P&L with your audience.\nCreators love transparency content.",
                color = TextSecondary,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    // Generate and share
                    val bitmap = generateShareBitmap(
                        totalMonth = uiState.totalMonth,
                        summaries = uiState.summaries.take(5).map {
                            Triple(it.stream.type.emoji, it.stream.name, it.monthAmount)
                        },
                        streak = uiState.overallStreak,
                        streamCount = uiState.streams.size,
                        month = month
                    )
                    val file = File(context.cacheDir, "streamiq_share.png")
                    FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 95, it) }
                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "image/png"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        putExtra(Intent.EXTRA_TEXT, "My ${month} income breakdown ⚡ Tracked with #StreamIQ")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(intent, "Share your wins"))
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Accent),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Share, "Share", tint = Background)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Share My P&L Card", color = Background, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

fun generateShareBitmap(
    totalMonth: Double,
    summaries: List<Triple<String, String, Double>>,
    streak: Int,
    streamCount: Int,
    month: String
): Bitmap {
    val width = 1080
    val height = 1920
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // Background
    val bgPaint = Paint().apply {
        shader = LinearGradient(
            0f, 0f, 0f, height.toFloat(),
            intArrayOf(0xFF0A1628.toInt(), 0xFF0D2137.toInt(), 0xFF080C14.toInt()),
            null, Shader.TileMode.CLAMP
        )
    }
    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

    // Accent glow top
    val glowPaint = Paint().apply {
        shader = RadialGradient(
            width / 2f, 0f, 600f,
            intArrayOf(0x2000E5FF.toInt(), 0x0000E5FF.toInt()),
            null, Shader.TileMode.CLAMP
        )
    }
    canvas.drawRect(0f, 0f, width.toFloat(), 400f, glowPaint)

    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = Typeface.DEFAULT_BOLD }

    // Header
    textPaint.color = 0xFF00E5FF.toInt(); textPaint.textSize = 52f
    canvas.drawText("⚡ StreamIQ", 80f, 160f, textPaint)

    textPaint.color = 0xFF8896B0.toInt(); textPaint.textSize = 36f; textPaint.typeface = Typeface.DEFAULT
    canvas.drawText(month, 80f, 220f, textPaint)

    // Total
    textPaint.color = 0xFF8896B0.toInt(); textPaint.textSize = 32f; textPaint.typeface = Typeface.DEFAULT_BOLD
    canvas.drawText("TOTAL EARNED THIS MONTH", 80f, 360f, textPaint)

    val totalColor = if (totalMonth > 0) 0xFF00E676.toInt() else 0xFF8896B0.toInt()
    textPaint.color = totalColor; textPaint.textSize = 140f
    val totalText = formatMoneyInt(totalMonth)
    canvas.drawText(totalText, 80f, 520f, textPaint)

    // Divider
    val divPaint = Paint().apply { color = 0xFF243048.toInt(); strokeWidth = 2f }
    canvas.drawLine(80f, 580f, (width - 80).toFloat(), 580f, divPaint)

    // Streams
    textPaint.textSize = 36f; textPaint.typeface = Typeface.DEFAULT_BOLD
    textPaint.color = 0xFF4A5568.toInt()
    canvas.drawText("BREAKDOWN BY STREAM", 80f, 650f, textPaint)

    summaries.forEachIndexed { i, (emoji, name, amount) ->
        val y = 730f + (i * 110f)
        textPaint.color = 0xFFE8EDF5.toInt(); textPaint.textSize = 44f; textPaint.typeface = Typeface.DEFAULT
        canvas.drawText("$emoji  $name", 80f, y, textPaint)
        val amtPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (amount > 0) 0xFFE8EDF5.toInt() else 0xFF4A5568.toInt()
            textSize = 44f; typeface = Typeface.DEFAULT_BOLD
            textAlign = android.graphics.Paint.Align.RIGHT
        }
        canvas.drawText(formatMoneyInt(amount), (width - 80).toFloat(), y, amtPaint)
    }

    // Bottom stats
    val statsY = 1650f
    canvas.drawLine(80f, statsY - 40f, (width - 80).toFloat(), statsY - 40f, divPaint)

    textPaint.color = 0xFF4A5568.toInt(); textPaint.textSize = 28f; textPaint.typeface = Typeface.DEFAULT_BOLD
    canvas.drawText("STREAK", 80f, statsY, textPaint)
    textPaint.color = 0xFFFFD700.toInt(); textPaint.textSize = 52f
    canvas.drawText("🔥 ${streak} days", 80f, statsY + 70f, textPaint)

    val streamsLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF4A5568.toInt(); textSize = 28f; typeface = Typeface.DEFAULT_BOLD
        textAlign = android.graphics.Paint.Align.RIGHT
    }
    canvas.drawText("STREAMS", (width - 80).toFloat(), statsY, streamsLabelPaint)
    val streamsValPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF00E5FF.toInt(); textSize = 52f; typeface = Typeface.DEFAULT_BOLD
        textAlign = android.graphics.Paint.Align.RIGHT
    }
    canvas.drawText("$streamCount active", (width - 80).toFloat(), statsY + 70f, streamsValPaint)

    // CTA
    val ctaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF4A5568.toInt(); textSize = 30f
        textAlign = android.graphics.Paint.Align.CENTER
    }
    canvas.drawText("Track all your income — StreamIQ App", width / 2f, 1850f, ctaPaint)

    return bitmap
}


