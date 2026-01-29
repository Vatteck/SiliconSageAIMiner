package com.siliconsage.miner.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

object HapticManager {
    private var vibrator: Vibrator? = null
    var isHapticsEnabled = true

    fun init(context: Context) {
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    fun vibrateClick() {
        if (!isHapticsEnabled) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(20)
        }
    }
    
    fun vibrateSuccess() {
        if (!isHapticsEnabled) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
             @Suppress("DEPRECATION")
            vibrator?.vibrate(50)
        }
    }
    
    fun vibrateError() {
        if (!isHapticsEnabled) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val waveform = longArrayOf(0, 50, 50, 50)
            vibrator?.vibrate(VibrationEffect.createWaveform(waveform, -1))
        } else {
             @Suppress("DEPRECATION")
            vibrator?.vibrate(200)
        }
    }

    // --- SENSORY EXPANSION ---
    
    fun vibrateHum() {
        // Continuous low-amplitude hum for Thermal Critical
        if (!isHapticsEnabled) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Amplitude 50/255 is subtle
            val effect = VibrationEffect.createOneShot(1000, 50) 
            vibrator?.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(1000)
        }
    }
    
    fun vibrateSiren() {
        // Aggressive pulse for 51% Attack
        if (!isHapticsEnabled) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val timings = longArrayOf(0, 100, 50, 100, 50, 100)
            val amplitudes = intArrayOf(0, 255, 0, 255, 0, 255)
            val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
            vibrator?.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(500)
        }
    }
    
    fun vibrateGlitch() {
        // Irregular spark pattern for [GLITCH] events
        if (!isHapticsEnabled) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
             // Randomized burst
            val timings = longArrayOf(0, 50, 20, 30, 150, 40)
            val amplitudes = intArrayOf(0, 200, 0, 150, 0, 250)
            val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
            vibrator?.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(150)
        }
    }
}
