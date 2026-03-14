package com.streamiq.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.streamiq.data.*
import com.streamiq.utils.AppCurrency
import com.streamiq.utils.selectedCurrency
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

data class StreamIQUiState(
    val streams: List<IncomeStream> = emptyList(),
    val entries: List<DailyEntry> = emptyList(),
    val summaries: List<StreamSummary> = emptyList(),
    val totalToday: Double = 0.0,
    val totalMonth: Double = 0.0,
    val totalExpensesMonth: Double = 0.0,
    val overallStreak: Int = 0,
    val bestStream: StreamSummary? = null,
    val onboardingComplete: Boolean = false,
    val isLoading: Boolean = true,
    val isDarkTheme: Boolean = true,
    val currency: AppCurrency = AppCurrency.USD
) {
    val netProfitMonth: Double get() = totalMonth - totalExpensesMonth
}

enum class ProFeature {
    AI_INSIGHTS, VOICE_LOGGING, FORECAST, STREAM_SCORE, UNLIMITED_STREAMS
}

class StreamIQViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = StreamIQRepository(application)
    private val _uiState = MutableStateFlow(StreamIQUiState())
    val uiState: StateFlow<StreamIQUiState> = _uiState.asStateFlow()

    // ── Pro gate ─────────────────────────────────────────────────────────────
    private val _isPro = mutableStateOf(false)
    val isPro: Boolean get() = _isPro.value
    val maxFreeStreams = 3

    init {
        viewModelScope.launch {
            combine(
                repo.getStreamsFlow(),
                repo.getEntriesFlow(),
                repo.getOnboardingFlow(),
                repo.getExpensesFlow()
            ) { streams, entries, onboarding, expenses ->
                val summaries = repo.getSummaries(streams, entries)
                StreamIQUiState(
                    streams = streams,
                    entries = entries,
                    summaries = summaries,
                    totalToday = repo.getTotalToday(entries),
                    totalMonth = repo.getTotalMonth(entries),
                    totalExpensesMonth = repo.getTotalExpensesMonth(expenses),
                    overallStreak = repo.getOverallStreak(entries),
                    bestStream = repo.getBestStream(summaries),
                    onboardingComplete = onboarding,
                    isLoading = false,
                    isDarkTheme = _uiState.value.isDarkTheme,
                    currency = _uiState.value.currency
                )
            }.collect { _uiState.value = it }
        }
    }

    fun addStream(name: String, type: StreamType) {
        viewModelScope.launch {
            repo.saveStream(IncomeStream(id = UUID.randomUUID().toString(), name = name, type = type))
        }
    }

    fun deleteStream(streamId: String) {
        viewModelScope.launch { repo.deleteStream(streamId) }
    }

    fun updateStreamGoal(streamId: String, goal: Double) {
        viewModelScope.launch { repo.updateStreamGoal(streamId, goal) }
    }

    fun setGoalForLastStream(goal: Double) {
        viewModelScope.launch {
            kotlinx.coroutines.delay(300)
            val last = _uiState.value.streams.lastOrNull() ?: return@launch
            repo.updateStreamGoal(last.id, goal)
        }
    }

    fun logEntry(streamId: String, amount: Double, note: String = "") {
        viewModelScope.launch {
            repo.saveEntry(DailyEntry(
                streamId = streamId,
                date = java.time.LocalDate.now().toString(),
                amount = amount,
                note = note
            ))
        }
    }

    fun logEntryForDate(streamId: String, date: String, amount: Double) {
        viewModelScope.launch {
            repo.saveEntry(DailyEntry(streamId = streamId, date = date, amount = amount))
        }
    }

    fun logExpenses(expenses: Map<String, Double>) {
        viewModelScope.launch {
            repo.saveExpenses(java.time.LocalDate.now().toString(), expenses)
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch { repo.setOnboardingComplete() }
    }

    fun toggleTheme() {
        _uiState.value = _uiState.value.copy(isDarkTheme = !_uiState.value.isDarkTheme)
    }

    fun setCurrency(currency: AppCurrency) {
        selectedCurrency = currency
        _uiState.value = _uiState.value.copy(currency = currency)
    }

    fun getTodayEntryForStream(streamId: String): Double {
        val today = java.time.LocalDate.now().toString()
        return _uiState.value.entries
            .filter { it.streamId == streamId && it.date == today }
            .sumOf { it.amount }
    }

    fun getDeadStreams(): List<StreamSummary> {
        val cutoff = java.time.LocalDate.now().minusDays(14).toString()
        return _uiState.value.summaries.filter { summary ->
            val lastEntry = _uiState.value.entries
                .filter { it.streamId == summary.stream.id && it.amount > 0 }
                .maxByOrNull { it.date }
            lastEntry == null || lastEntry.date < cutoff
        }
    }

    fun isProFeature(feature: ProFeature): Boolean = when (feature) {
        ProFeature.AI_INSIGHTS       -> isPro
        ProFeature.VOICE_LOGGING     -> isPro
        ProFeature.FORECAST          -> isPro
        ProFeature.STREAM_SCORE      -> isPro
        ProFeature.UNLIMITED_STREAMS -> isPro
    }

    fun canAddStream(): Boolean =
        isPro || _uiState.value.streams.size < maxFreeStreams
}
