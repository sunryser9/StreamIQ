package com.streamiq.ui.screens

// CSV Export + Income Report Card (viral share)
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.streamiq.ui.theme.*
import com.streamiq.data.DailyEntry
import com.streamiq.data.StreamSummary
import com.streamiq.ui.viewmodel.StreamIQViewModel
import com.streamiq.utils.*
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportScreen(viewModel: StreamIQViewModel, onBack: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    val isDark = uiState.isDarkTheme
    val bg = if (isDark) Background else LightBackground
    val textPrimary = if (isDark) TextPrimary else LightTextPrimary
    val textSecondary = if (isDark) TextSecondary else LightTextSecondary
    val context = LocalContext.current
    var exportDone by remember { mutableStateOf(false) }
    var shareReport by remember { mutableStateOf(false) }

    val currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM yyyy"))

    Scaffold(
        containerColor = bg,
        topBar = {
            TopAppBar(
                title = { Text("Export & Share 📤", color = textPrimary, fontWeight = FontWeight.Bold) },
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

            // CSV Export
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) Card else LightCard)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Text("📊", fontSize = 28.sp)
                        Column {
                            Text("Export to CSV", fontWeight = FontWeight.Bold,
                                fontSize = 16.sp, color = textPrimary)
                            Text("Open in Excel, Google Sheets, or any spreadsheet app",
                                fontSize = 12.sp, color = textSecondary)
                        }
                    }
                    Spacer(Modifier.height(16.dp))

                    // Preview stats
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${uiState.summaries.size}", fontSize = 22.sp,
                                fontWeight = FontWeight.Bold, color = Accent)
                            Text("Streams", fontSize = 11.sp, color = textSecondary)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${uiState.entries.size}", fontSize = 22.sp,
                                fontWeight = FontWeight.Bold, color = Accent)
                            Text("Entries", fontSize = 11.sp, color = textSecondary)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(formatMoney(uiState.summaries.sumOf { it.allTimeAmount }),
                                fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Green)
                            Text("All-Time", fontSize = 11.sp, color = textSecondary)
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val csv = buildCsv(uiState.summaries, uiState.entries)
                            shareCsv(context, csv)
                            exportDone = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Accent)
                    ) {
                        Text(
                            if (exportDone) "✅ Exported! Export Again" else "📊 Export CSV",
                            fontWeight = FontWeight.Bold, fontSize = 15.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }

            // Viral Income Report Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Gold.copy(alpha = 0.1f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Text("📣", fontSize = 28.sp)
                        Column {
                            Text("Share Income Report", fontWeight = FontWeight.Bold,
                                fontSize = 16.sp, color = textPrimary)
                            Text("Generate your $currentMonth income card — share on Twitter, WhatsApp, LinkedIn",
                                fontSize = 12.sp, color = textSecondary)
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Preview the report text
                    val reportText = buildIncomeReport(uiState.summaries, uiState.totalMonth)
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDark) SurfaceVariant else LightSurfaceVariant)
                    ) {
                        Text(
                            reportText,
                            modifier = Modifier.padding(14.dp),
                            fontSize = 13.sp,
                            color = textSecondary,
                            lineHeight = 20.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, reportText)
                            }
                            context.startActivity(Intent.createChooser(intent, "Share Income Report"))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Gold)
                    ) {
                        Text("📣 Share Report", fontWeight = FontWeight.Bold,
                            fontSize = 15.sp, color = Color.Black,
                            modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
            }

            // Transparency Mode
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF9C27B0).copy(alpha = 0.1f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Text("🌐", fontSize = 28.sp)
                        Column {
                            Text("Transparency Mode", fontWeight = FontWeight.Bold,
                                fontSize = 16.sp, color = textPrimary)
                            Text("Creators who share income reports grow followers 3x faster",
                                fontSize = 12.sp, color = textSecondary)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Share your monthly income breakdown publicly — like top creators do. " +
                        "Builds trust, attracts collaborations, and proves your business is real.",
                        fontSize = 13.sp, color = textSecondary, lineHeight = 18.sp
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = {
                            val tweet = buildTwitterThread(uiState.summaries, uiState.totalMonth)
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, tweet)
                            }
                            context.startActivity(Intent.createChooser(intent, "Share on Twitter/X"))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFF9C27B0))
                    ) {
                        Text("🐦 Share on Twitter/X",
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF9C27B0),
                            modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

private fun buildCsv(
    summaries: List<StreamSummary>,
    entries: List<DailyEntry>
): String {
    val sb = StringBuilder()
    sb.appendLine("Date,Stream,Type,Amount,Note,Currency")
    val streamMap = summaries.associate { it.stream.id to it.stream }
    entries.sortedBy { it.date }.forEach { entry ->
        val stream = streamMap[entry.streamId]
        val name = stream?.name ?: "Unknown"
        val type = stream?.type?.label ?: "Other"
        val note = entry.note.replace(",", ";")
        sb.appendLine("${entry.date},\"$name\",\"$type\",${entry.amount},\"$note\",${selectedCurrency.code}")
    }
    // Summary section
    sb.appendLine()
    sb.appendLine("SUMMARY")
    sb.appendLine("Stream,This Month,All Time,Streak,Trend%")
    summaries.forEach { s ->
        sb.appendLine("\"${s.stream.name}\",${s.monthAmount},${s.allTimeAmount},${s.streak},${String.format("%.1f", s.trend)}")
    }
    return sb.toString()
}

private fun shareCsv(context: Context, csv: String) {
    try {
        val fileName = "StreamIQ_Export_${LocalDate.now()}.csv"
        val file = File(context.cacheDir, fileName)
        file.writeText(csv)
        val uri: Uri = FileProvider.getUriForFile(
            context, "${context.packageName}.provider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "StreamIQ Export — ${LocalDate.now()}")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Export CSV"))
    } catch (e: Exception) {
        // fallback: share as text
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, csv)
        }
        context.startActivity(Intent.createChooser(intent, "Export CSV"))
    }
}

private fun buildIncomeReport(summaries: List<StreamSummary>, totalMonth: Double): String {
    val month = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM yyyy"))
    val sb = StringBuilder()
    sb.appendLine("📊 $month Income Report")
    sb.appendLine("━━━━━━━━━━━━━━━━━━━━")
    sb.appendLine("Total: ${formatMoneyFull(totalMonth)}")
    sb.appendLine()
    summaries.filter { it.monthAmount > 0 }.sortedByDescending { it.monthAmount }.forEach { s ->
        val pct = if (totalMonth > 0) (s.monthAmount / totalMonth * 100).toInt() else 0
        sb.appendLine("${s.stream.type.emoji} ${s.stream.name}: ${formatMoney(s.monthAmount)} ($pct%)")
    }
    sb.appendLine()
    sb.appendLine("Tracked with StreamIQ 📱")
    return sb.toString()
}

private fun buildTwitterThread(summaries: List<StreamSummary>, totalMonth: Double): String {
    val month = LocalDate.now().format(DateTimeFormatter.ofPattern("MMM yyyy"))
    val streams = summaries.filter { it.monthAmount > 0 }.sortedByDescending { it.monthAmount }
    val sb = StringBuilder()
    sb.append("My $month income report 🧵\n\n")
    sb.append("Total: ${formatMoneyFull(totalMonth)}\n")
    sb.append("Streams: ${streams.size}\n\n")
    streams.take(3).forEach { s ->
        val pct = if (totalMonth > 0) (s.monthAmount / totalMonth * 100).toInt() else 0
        sb.append("${s.stream.type.emoji} ${s.stream.name} → ${formatMoney(s.monthAmount)} ($pct%)\n")
    }
    sb.append("\nTracked every day with @StreamIQ 📱\n#incomestreams #solopreneur #buildinpublic")
    return sb.toString()
}
