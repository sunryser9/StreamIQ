package com.streamiq.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "streamiq_prefs")

enum class StreamType(val emoji: String, val label: String, val color: Long) {
    YOUTUBE("▶", "YouTube", 0xFFFF0000),
    WEBSITE("🌐", "Website/Blog", 0xFF2196F3),
    CRYPTO("₿", "Crypto/Trading", 0xFFF7931A),
    DOMAIN("🔗", "Domain Names", 0xFF9C27B0),
    FREELANCE("💼", "Freelance", 0xFF4CAF50),
    OTHER("💡", "Other", 0xFF607D8B)
}

data class IncomeStream(
    val id: String,
    val name: String,
    val type: StreamType,
    val isActive: Boolean = true,
    val createdDate: String = LocalDate.now().toString(),
    val monthlyGoal: Double = 0.0
)

data class DailyEntry(
    val streamId: String,
    val date: String,
    val amount: Double,
    val note: String = ""
)

data class StreamSummary(
    val stream: IncomeStream,
    val todayAmount: Double,
    val monthAmount: Double,
    val allTimeAmount: Double,
    val streak: Int,
    val trend: Double // percentage change vs last month
)

class StreamIQRepository(private val context: Context) {

    private val STREAMS_KEY = stringPreferencesKey("streams")
    private val ENTRIES_KEY = stringPreferencesKey("entries")
    private val ONBOARDING_KEY = booleanPreferencesKey("onboarding_complete")
    private val EXPENSES_KEY = stringPreferencesKey("daily_expenses")

    // --- Streams ---
    fun getStreamsFlow(): Flow<List<IncomeStream>> =
        context.dataStore.data.map { prefs ->
            parseStreams(prefs[STREAMS_KEY] ?: "[]")
        }

    suspend fun saveStream(stream: IncomeStream) {
        context.dataStore.edit { prefs ->
            val current = parseStreams(prefs[STREAMS_KEY] ?: "[]").toMutableList()
            val existing = current.indexOfFirst { it.id == stream.id }
            if (existing >= 0) current[existing] = stream else current.add(stream)
            prefs[STREAMS_KEY] = streamsToJson(current)
        }
    }

    suspend fun deleteStream(streamId: String) {
        context.dataStore.edit { prefs ->
            val current = parseStreams(prefs[STREAMS_KEY] ?: "[]").toMutableList()
            current.removeAll { it.id == streamId }
            prefs[STREAMS_KEY] = streamsToJson(current)
        }
    }

    suspend fun updateStreamGoal(streamId: String, goal: Double) {
        context.dataStore.edit { prefs ->
            val current = parseStreams(prefs[STREAMS_KEY] ?: "[]").toMutableList()
            val idx = current.indexOfFirst { it.id == streamId }
            if (idx >= 0) current[idx] = current[idx].copy(monthlyGoal = goal)
            prefs[STREAMS_KEY] = streamsToJson(current)
        }
    }

    // --- Entries ---
    fun getEntriesFlow(): Flow<List<DailyEntry>> =
        context.dataStore.data.map { prefs ->
            parseEntries(prefs[ENTRIES_KEY] ?: "[]")
        }

    suspend fun saveEntry(entry: DailyEntry) {
        context.dataStore.edit { prefs ->
            val current = parseEntries(prefs[ENTRIES_KEY] ?: "[]").toMutableList()
            current.removeAll { it.streamId == entry.streamId && it.date == entry.date }
            if (entry.amount > 0 || entry.note.isNotEmpty()) current.add(entry)
            prefs[ENTRIES_KEY] = entriesToJson(current)
        }
    }

    // --- Onboarding ---
    fun getOnboardingFlow(): Flow<Boolean> =
        context.dataStore.data.map { prefs -> prefs[ONBOARDING_KEY] ?: false }

    suspend fun setOnboardingComplete() {
        context.dataStore.edit { prefs -> prefs[ONBOARDING_KEY] = true }
    }

    // --- Computed ---
    fun getSummaries(streams: List<IncomeStream>, entries: List<DailyEntry>): List<StreamSummary> {
        val today = LocalDate.now().toString()
        val thisMonth = today.substring(0, 7)
        val lastMonth = LocalDate.now().minusMonths(1).toString().substring(0, 7)

        return streams.filter { it.isActive }.map { stream ->
            val streamEntries = entries.filter { it.streamId == stream.id }
            val todayAmt = streamEntries.filter { it.date == today }.sumOf { it.amount }
            val monthAmt = streamEntries.filter { it.date.startsWith(thisMonth) }.sumOf { it.amount }
            val lastMonthAmt = streamEntries.filter { it.date.startsWith(lastMonth) }.sumOf { it.amount }
            val allTime = streamEntries.sumOf { it.amount }
            val trend = if (lastMonthAmt > 0) ((monthAmt - lastMonthAmt) / lastMonthAmt) * 100 else 0.0
            val streak = calculateStreak(streamEntries)
            StreamSummary(stream, todayAmt, monthAmt, allTime, streak, trend)
        }.sortedByDescending { it.monthAmount }
    }

    fun getTotalToday(entries: List<DailyEntry>): Double {
        val today = LocalDate.now().toString()
        return entries.filter { it.date == today }.sumOf { it.amount }
    }

    fun getTotalMonth(entries: List<DailyEntry>): Double {
        val thisMonth = LocalDate.now().toString().substring(0, 7)
        return entries.filter { it.date.startsWith(thisMonth) }.sumOf { it.amount }
    }

    fun getOverallStreak(entries: List<DailyEntry>): Int {
        var streak = 0
        var date = LocalDate.now()
        while (true) {
            val dateStr = date.toString()
            if (entries.any { it.date == dateStr && it.amount > 0 }) {
                streak++
                date = date.minusDays(1)
            } else break
        }
        return streak
    }

    fun getBestStream(summaries: List<StreamSummary>): StreamSummary? =
        summaries.maxByOrNull { it.monthAmount }

    private fun calculateStreak(entries: List<DailyEntry>): Int {
        var streak = 0
        var date = LocalDate.now()
        while (true) {
            val dateStr = date.toString()
            if (entries.any { it.date == dateStr && it.amount > 0 }) {
                streak++
                date = date.minusDays(1)
            } else break
        }
        return streak
    }

    // --- Expense tracking ---
    suspend fun saveExpenses(date: String, expenses: Map<String, Double>) {
        context.dataStore.edit { prefs ->
            val current = parseExpenseMap(prefs[EXPENSES_KEY] ?: "{}")
            current[date] = expenses
            prefs[EXPENSES_KEY] = expenseMapToJson(current)
        }
    }

    fun getExpensesFlow(): Flow<Map<String, Map<String, Double>>> =
        context.dataStore.data.map { prefs ->
            parseExpenseMap(prefs[EXPENSES_KEY] ?: "{}")
        }

    fun getTotalExpensesMonth(expenseMap: Map<String, Map<String, Double>>): Double {
        val thisMonth = LocalDate.now().toString().substring(0, 7)
        return expenseMap.entries
            .filter { it.key.startsWith(thisMonth) }
            .sumOf { entry -> entry.value.values.sum() }
    }

    // --- JSON Serialization ---
    private fun parseStreams(json: String): List<IncomeStream> {
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map {
                val obj = arr.getJSONObject(it)
                IncomeStream(
                    id = obj.getString("id"),
                    name = obj.getString("name"),
                    type = StreamType.valueOf(obj.getString("type")),
                    isActive = obj.optBoolean("isActive", true),
                    createdDate = obj.optString("createdDate", LocalDate.now().toString()),
                    monthlyGoal = obj.optDouble("monthlyGoal", 0.0)
                )
            }
        } catch (e: Exception) { emptyList() }
    }

    private fun streamsToJson(streams: List<IncomeStream>): String {
        val arr = JSONArray()
        streams.forEach {
            arr.put(JSONObject().apply {
                put("id", it.id)
                put("name", it.name)
                put("type", it.type.name)
                put("isActive", it.isActive)
                put("createdDate", it.createdDate)
                put("monthlyGoal", it.monthlyGoal)
            })
        }
        return arr.toString()
    }

    private fun parseEntries(json: String): List<DailyEntry> {
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map {
                val obj = arr.getJSONObject(it)
                DailyEntry(
                    streamId = obj.getString("streamId"),
                    date = obj.getString("date"),
                    amount = obj.getDouble("amount"),
                    note = obj.optString("note", "")
                )
            }
        } catch (e: Exception) { emptyList() }
    }

    private fun entriesToJson(entries: List<DailyEntry>): String {
        val arr = JSONArray()
        entries.forEach {
            arr.put(JSONObject().apply {
                put("streamId", it.streamId)
                put("date", it.date)
                put("amount", it.amount)
                put("note", it.note)
            })
        }
        return arr.toString()
    }

    private fun parseExpenseMap(json: String): MutableMap<String, Map<String, Double>> {
        return try {
            val obj = JSONObject(json)
            val result = mutableMapOf<String, Map<String, Double>>()
            obj.keys().forEach { date ->
                val dayObj = obj.getJSONObject(date)
                val dayMap = mutableMapOf<String, Double>()
                dayObj.keys().forEach { cat -> dayMap[cat] = dayObj.getDouble(cat) }
                result[date] = dayMap
            }
            result
        } catch (e: Exception) { mutableMapOf() }
    }

    private fun expenseMapToJson(map: Map<String, Map<String, Double>>): String {
        val obj = JSONObject()
        map.forEach { (date, cats) ->
            val dayObj = JSONObject()
            cats.forEach { (cat, amt) -> dayObj.put(cat, amt) }
            obj.put(date, dayObj)
        }
        return obj.toString()
    }
}
