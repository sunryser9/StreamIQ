package com.streamiq.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale
import java.util.UUID

enum class VoiceState { IDLE, SPEAKING, LISTENING, PROCESSING }

data class VoiceResult(
    val text: String = "",
    val amount: Double? = null,
    val streamName: String? = null
)

class VoiceManager(private val context: Context) {

    private var tts: TextToSpeech? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private var isTtsReady = false

    private val _voiceState = MutableStateFlow(VoiceState.IDLE)
    val voiceState: StateFlow<VoiceState> = _voiceState

    private val _lastHeard = MutableStateFlow("")
    val lastHeard: StateFlow<String> = _lastHeard

    private val _parsedResult = MutableStateFlow<VoiceResult?>(null)
    val parsedResult: StateFlow<VoiceResult?> = _parsedResult

    var onSpeakingDone: (() -> Unit)? = null
    var onVoiceResult: ((VoiceResult) -> Unit)? = null

    init {
        initTTS()
        initSpeechRecognizer()
    }

    private fun initTTS() {
        try {
            tts = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    val result = tts?.setLanguage(Locale.US)
                    if (result == TextToSpeech.LANG_MISSING_DATA ||
                        result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        tts?.setLanguage(Locale.getDefault())
                    }
                    tts?.setSpeechRate(0.95f)
                    tts?.setPitch(1.05f)
                    isTtsReady = true
                    tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {
                            _voiceState.value = VoiceState.SPEAKING
                        }
                        override fun onDone(utteranceId: String?) {
                            _voiceState.value = VoiceState.IDLE
                            onSpeakingDone?.invoke()
                        }
                        override fun onError(utteranceId: String?) {
                            _voiceState.value = VoiceState.IDLE
                        }
                    })
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initSpeechRecognizer() {
        try {
            if (SpeechRecognizer.isRecognitionAvailable(context)) {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
                speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _voiceState.value = VoiceState.LISTENING
                    }
                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val heard = matches?.firstOrNull() ?: ""
                        _lastHeard.value = heard
                        _voiceState.value = VoiceState.PROCESSING
                        val parsed = parseVoiceInput(heard)
                        _parsedResult.value = parsed
                        onVoiceResult?.invoke(parsed)
                    }
                    override fun onError(error: Int) { _voiceState.value = VoiceState.IDLE }
                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {}
                    override fun onPartialResults(partialResults: Bundle?) {}
                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun speak(text: String, thenListen: Boolean = false) {
        if (!isTtsReady || tts == null) return
        try {
            tts?.stop()
            onSpeakingDone = if (thenListen) ({ startListening() }) else null
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, UUID.randomUUID().toString())
        } catch (e: Exception) {
            e.printStackTrace()
            _voiceState.value = VoiceState.IDLE
        }
    }

    fun startListening() {
        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1500L)
            }
            _voiceState.value = VoiceState.LISTENING
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            _voiceState.value = VoiceState.IDLE
        }
    }

    fun stopListening() {
        try { speechRecognizer?.stopListening() } catch (e: Exception) { e.printStackTrace() }
        _voiceState.value = VoiceState.IDLE
    }

    fun stopSpeaking() {
        try { tts?.stop() } catch (e: Exception) { e.printStackTrace() }
        _voiceState.value = VoiceState.IDLE
    }

    fun parseVoiceInput(input: String): VoiceResult {
        val lower = input.lowercase()
        val amountRegex = Regex("""(\d+(?:\.\d+)?)\s*(?:dollars?|bucks?)?""")
        val amount = amountRegex.find(lower)?.groupValues?.get(1)?.toDoubleOrNull()
        val streamName = when {
            lower.contains("youtube") || lower.contains("you tube") -> "YouTube"
            lower.contains("freelance") || lower.contains("client") || lower.contains("consulting") -> "Freelance"
            lower.contains("crypto") || lower.contains("bitcoin") -> "Crypto"
            lower.contains("website") || lower.contains("blog") || lower.contains("affiliate") -> "Website"
            lower.contains("domain") -> "Domain"
            else -> null
        }
        return VoiceResult(text = input, amount = amount, streamName = streamName)
    }

    fun morningBriefing(totalMonth: Double, totalToday: Double, streak: Int, bestStream: String?): String {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        val greeting = when {
            hour < 12 -> "Good morning"
            hour < 17 -> "Good afternoon"
            else -> "Good evening"
        }
        val streakText = if (streak > 1) "You are on a $streak day streak! " else ""
        val bestText = if (bestStream != null) "Your top earner is $bestStream. " else ""
        return "$greeting! Your StreamIQ update: " +
            "You have earned ${fmt(totalMonth)} this month. " +
            "Today so far: ${fmt(totalToday)}. " +
            streakText + bestText +
            "Ready to log today's earnings?"
    }

    fun milestoneMessage(amount: Double): String = when {
        amount >= 1000 -> "Incredible! You have crossed one thousand dollars this month! You are absolutely crushing it!"
        amount >= 500  -> "Wow! Five hundred dollars this month! That is amazing progress!"
        amount >= 100  -> "Yes! One hundred dollars logged! Your hustle is paying off!"
        else           -> "Great job logging your earnings! Every dollar counts!"
    }

    fun logConfirmation(amount: Double, streamName: String, totalMonth: Double): String =
        "${fmt(amount)} logged to $streamName. Your monthly total is now ${fmt(totalMonth)}. Amazing work!"

    fun errorResponse(heard: String): String = when {
        heard.isEmpty() -> "I did not catch that. Could you try again?"
        else -> "I heard: $heard. But I could not find an amount. Try saying: I made 50 dollars from freelance."
    }

    private fun fmt(amount: Double) = "${amount.toInt()} dollars"

    fun destroy() {
        try { tts?.stop(); tts?.shutdown() } catch (e: Exception) { e.printStackTrace() }
        try { speechRecognizer?.destroy() } catch (e: Exception) { e.printStackTrace() }
        tts = null
        speechRecognizer = null
    }
}
