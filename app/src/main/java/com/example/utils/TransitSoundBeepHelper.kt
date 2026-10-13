package com.example.utils

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object TransitSoundBeepHelper {

    /**
     * Plays the signature Bolt-style dual-tone incoming ride alert beep
     * paired with a rhythmic haptic vibration pulse.
     */
    fun playDispatchBeep(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Dual high-pitch beep characteristic of ride-hailing dispatch
                val toneGen = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 95)
                toneGen.startTone(ToneGenerator.TONE_PROP_BEEP2, 140)
                delay(160)
                toneGen.startTone(ToneGenerator.TONE_PROP_ACK, 220)
                delay(240)
                toneGen.release()
            } catch (_: Exception) {
                // Fallback graceful degradation
            }
        }

        triggerHapticPulse(context, longPulse = false)
    }

    /**
     * Plays a single subtle message beep for incoming in-app chat.
     */
    fun playChatMessageBeep(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val toneGen = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 80)
                toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 100)
                delay(120)
                toneGen.release()
            } catch (_: Exception) {
            }
        }
        triggerHapticPulse(context, longPulse = false)
    }

    /**
     * Plays a high-urgency siren pattern for SOS distress alerts.
     */
    fun playEmergencyDistressBeep(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val toneGen = ToneGenerator(AudioManager.STREAM_ALARM, 100)
                for (i in 0..2) {
                    toneGen.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, 250)
                    delay(300)
                }
                toneGen.release()
            } catch (_: Exception) {
            }
        }
        triggerHapticPulse(context, longPulse = true)
    }

    private fun triggerHapticPulse(context: Context, longPulse: Boolean) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = if (longPulse) {
                    VibrationEffect.createWaveform(longArrayOf(0, 180, 100, 180), -1)
                } else {
                    VibrationEffect.createOneShot(120, VibrationEffect.DEFAULT_AMPLITUDE)
                }
                vibrator?.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(if (longPulse) 350 else 120)
            }
        } catch (_: Exception) {
        }
    }
}
