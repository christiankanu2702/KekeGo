package com.example.utils

import android.content.Context
import android.media.AudioAttributes
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

class KekeVoiceNavigationHelper(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false
    var isVoiceEnabled: Boolean = true

    init {
        try {
            tts = TextToSpeech(context.applicationContext, this)
        } catch (e: Exception) {
            Log.e("KekeVoiceNav", "TTS Initialization error: ${e.message}")
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale("en", "NG")) ?: TextToSpeech.LANG_MISSING_DATA
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Fallback to UK or US English if Nigerian locale is not locally packaged
                tts?.setLanguage(Locale.UK)
            }
            tts?.setSpeechRate(0.95f) // Natural, clear cadence for road transit
            tts?.setPitch(1.0f)
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()
            tts?.setAudioAttributes(audioAttributes)
            isInitialized = true
            Log.i("KekeVoiceNav", "Keke Voice Navigation Engine ready.")
        } else {
            Log.w("KekeVoiceNav", "Failed to initialize TTS engine with status $status")
        }
    }

    fun speak(text: String, flushPrevious: Boolean = true) {
        if (!isVoiceEnabled || !isInitialized || tts == null) return
        try {
            val queueMode = if (flushPrevious) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
            val params = Bundle().apply {
                putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f)
            }
            tts?.speak(text, queueMode, params, "KEKE_NAV_" + System.currentTimeMillis())
        } catch (e: Exception) {
            Log.w("KekeVoiceNav", "Error speaking voice cue: ${e.message}")
        }
    }

    // Corridor Navigation Presets
    fun announceTripAccepted(pickupMarket: String) {
        speak("Trip accepted. Navigate to $pickupMarket for passenger pickup.")
    }

    fun announceArrivalAtPickup() {
        speak("You have arrived at the pickup location. Notify passenger.")
    }

    fun announceTripStarted(destinationMarket: String) {
        speak("Trip started. Proceed along the transit corridor towards $destinationMarket.")
    }

    fun announceManeuver(instruction: String, distanceMeters: Int) {
        speak("In $distanceMeters meters, $instruction.")
    }

    fun announceApproachingDestination(destinationMarket: String) {
        speak("Approaching $destinationMarket. Prepare to safely alight passenger.")
    }

    fun announceTripCompleted(fareNaira: Int) {
        speak("Trip completed. Agreed fare of $fareNaira Naira collected. Have a safe drive.")
    }

    fun announceSpeedWarning() {
        speak("Caution: Please maintain safe corridor speed.")
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
            tts = null
            isInitialized = false
        } catch (_: Exception) {}
    }
}
