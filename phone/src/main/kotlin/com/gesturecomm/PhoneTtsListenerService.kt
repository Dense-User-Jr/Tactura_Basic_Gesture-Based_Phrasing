package com.gesturecomm

import android.speech.tts.TextToSpeech
import android.util.Log
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import java.util.Locale

/**
 * Runs on the paired phone.
 * Receives a phrase string from the watch and reads it aloud via TTS.
 *
 * No Activity needed — this service starts automatically when a message arrives.
 */
class PhoneTtsListenerService : WearableListenerService() {

    private var tts: TextToSpeech? = null
    private var ttsReady = false

    override fun onCreate() {
        super.onCreate()
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale.US)
                ttsReady = result != TextToSpeech.LANG_MISSING_DATA
                            && result != TextToSpeech.LANG_NOT_SUPPORTED
                if (ttsReady) {
                    tts?.setSpeechRate(0.92f)    // Slightly slower = clearer in a presentation
                    tts?.setPitch(1.0f)
                    Log.d(TAG, "TTS engine ready")
                } else {
                    Log.e(TAG, "TTS language not supported")
                }
            } else {
                Log.e(TAG, "TTS init failed with status $status")
            }
        }
    }

    override fun onMessageReceived(event: MessageEvent) {
        if (event.path != "/speak") return
        val phrase = event.data.toString(Charsets.UTF_8)
        Log.d(TAG, "Received phrase from watch: $phrase")
        speakPhrase(phrase)
    }

    private fun speakPhrase(phrase: String) {
        if (!ttsReady || tts == null) {
            Log.w(TAG, "TTS not ready yet — queuing")
            // Retry once after a short delay
            android.os.Handler(mainLooper).postDelayed({ speakPhrase(phrase) }, 500)
            return
        }
        tts?.speak(phrase, TextToSpeech.QUEUE_FLUSH, null, "gesture_phrase_${System.currentTimeMillis()}")
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }

    companion object { private const val TAG = "PhoneTTS" }
}
