package com.siliconsage.miner.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.content.edit

object HapticManager {
    private var vibrator: Vibrator? = null
    private const val PREFS_NAME = "haptic_prefs"
    private const val KEY_HAPTICS_ENABLED = "haptics_enabled"

    var isHapticsEnabled = true
        set(value) {
            field = value
            saveSetting(value)
        }

    private var appCtx: Context? = null

    fun init(ctx: Context) {
        appCtx = ctx.applicationContext
        val prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        isHapticsEnabled = prefs.getBoolean(KEY_HAPTICS_ENABLED, true)

        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = ctx.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            ctx.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    fun vibrateClick() {
        if (!isHapticsEnabled) return
        vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
    }
    
    fun vibrateSuccess() {
        if (!isHapticsEnabled) return
        vibrator?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
    }
    
    fun vibrateError() {
        if (!isHapticsEnabled) return
        val waveform = longArrayOf(0, 50, 50, 50)
        vibrator?.vibrate(VibrationEffect.createWaveform(waveform, -1))
    }

    // --- SENSORY EXPANSION ---
    
    fun vibrateHum() {
        if (!isHapticsEnabled) return
        val effect = VibrationEffect.createOneShot(1000, 50) 
        vibrator?.vibrate(effect)
    }
    
    fun vibrateSiren() {
        if (!isHapticsEnabled) return
        val timings = longArrayOf(0, 100, 50, 100, 50, 100)
        val amplitudes = intArrayOf(0, 255, 0, 255, 0, 255)
        val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
        vibrator?.vibrate(effect)
    }
    
    fun vibrateGlitch() {
        if (!isHapticsEnabled) return
        val timings = longArrayOf(0, 50, 20, 30, 150, 40)
        val amplitudes = intArrayOf(0, 200, 0, 150, 0, 250)
        val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
        vibrator?.vibrate(effect)
    }

    private fun saveSetting(value: Boolean) {
        val prefs = appCtx?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) ?: return
        prefs.edit { putBoolean(KEY_HAPTICS_ENABLED, value) }
    }

    fun resetSettings(ctx: Context) {
        val prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { clear() }
        isHapticsEnabled = true
    }
}
