package com.example.audio

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface VoiceSearchState {
    data object Idle : VoiceSearchState
    data class Listening(
        val partialText: String = "",
        val rmsDb: Float = 0f,
        val languageTag: String = "id-ID"
    ) : VoiceSearchState

    data class ResultReady(
        val recognizedText: String,
        val isConfident: Boolean,
        val languageTag: String
    ) : VoiceSearchState

    data class Error(
        val message: String,
        val needsSystemIntentFallback: Boolean = false
    ) : VoiceSearchState
}

/**
 * Android TV Voice Search Manager using SpeechRecognizer with system RecognizerIntent fallback.
 */
class VoiceSearchManager(
    private val context: Context
) {
    private var speechRecognizer: SpeechRecognizer? = null

    private val _state = MutableStateFlow<VoiceSearchState>(VoiceSearchState.Idle)
    val state: StateFlow<VoiceSearchState> = _state.asStateFlow()

    fun isRecognitionAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }

    fun createSystemVoiceIntent(languageTag: String): Intent {
        return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageTag)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, languageTag)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(
                RecognizerIntent.EXTRA_PROMPT,
                if (languageTag == "id-ID") "Ucapkan judul lagu atau nama penyanyi..."
                else "Say a song title or singer name..."
            )
        }
    }

    fun startListening(languageTag: String = "id-ID") {
        if (!isRecognitionAvailable()) {
            _state.value = VoiceSearchState.Error(
                message = "Gunakan mikrofon sistem Google TV atau ketik judul lagu.",
                needsSystemIntentFallback = true
            )
            return
        }

        stopInternal()

        val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
        speechRecognizer = recognizer

        recognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                _state.value = VoiceSearchState.Listening(
                    partialText = "",
                    rmsDb = 0f,
                    languageTag = languageTag
                )
            }

            override fun onBeginningOfSpeech() {}

            override fun onRmsChanged(rmsdB: Float) {
                val current = _state.value
                if (current is VoiceSearchState.Listening) {
                    _state.value = current.copy(rmsDb = rmsdB.coerceIn(0f, 12f))
                }
            }

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {}

            override fun onError(error: Int) {
                val needsFallback = error == SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS ||
                    error == SpeechRecognizer.ERROR_CLIENT
                val msg = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Gagal merekam audio mikrofon remote/TV."
                    SpeechRecognizer.ERROR_CLIENT -> "Mengalihkan ke mikrofon sistem Google TV..."
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Izin mikrofon belum diberikan. Tekan 'Mic Sistem TV' atau 'Ulangi Suara'."
                    SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT ->
                        "Koneksi internet diperlukan untuk pengenalan suara."
                    SpeechRecognizer.ERROR_NO_MATCH -> "Suara tidak dikenali. Silakan ucapkan judul lagu atau penyanyi lebih jelas."
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Layanan suara sedang sibuk, coba sebentar lagi."
                    SpeechRecognizer.ERROR_SERVER -> "Server pengenalan suara sedang bermasalah."
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Tidak ada suara terdeteksi. Tekan 'Ulangi Suara' untuk mencoba lagi."
                    else -> "Pengenalan suara gagal (kode $error)."
                }
                _state.value = VoiceSearchState.Error(
                    message = msg,
                    needsSystemIntentFallback = needsFallback
                )
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val scores = results?.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)
                val bestText = matches?.firstOrNull()?.trim().orEmpty()
                val confidence = scores?.firstOrNull() ?: 0.85f

                if (bestText.isNotEmpty()) {
                    _state.value = VoiceSearchState.ResultReady(
                        recognizedText = bestText,
                        isConfident = confidence >= 0.65f && bestText.length >= 2,
                        languageTag = languageTag
                    )
                } else {
                    _state.value = VoiceSearchState.Error("Tidak ada teks yang dikenali. Silakan coba lagi.")
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val partial = partialResults
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                    ?.trim()
                    .orEmpty()
                val current = _state.value
                if (current is VoiceSearchState.Listening && partial.isNotEmpty()) {
                    _state.value = current.copy(partialText = partial)
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        _state.value = VoiceSearchState.Listening(languageTag = languageTag)
        recognizer.startListening(createSystemVoiceIntent(languageTag))
    }

    fun setDirectResult(text: String, languageTag: String) {
        val clean = text.trim()
        if (clean.isNotEmpty()) {
            _state.value = VoiceSearchState.ResultReady(
                recognizedText = clean,
                isConfident = true,
                languageTag = languageTag
            )
        }
    }

    fun resetState() {
        stopInternal()
        _state.value = VoiceSearchState.Idle
    }

    private fun stopInternal() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.cancel()
            speechRecognizer?.destroy()
        } catch (_: Exception) {
        }
        speechRecognizer = null
    }
}
