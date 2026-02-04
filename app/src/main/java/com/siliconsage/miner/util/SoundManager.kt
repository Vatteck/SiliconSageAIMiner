package com.siliconsage.miner.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import android.media.SoundPool
import androidx.core.content.edit
import androidx.core.net.toUri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

object SoundManager {
    private var soundPool: SoundPool? = null
    private val soundMap = ConcurrentHashMap<String, Int>()
    
    private const val PREFS_NAME = "audio_prefs"
    private const val KEY_SFX_ENABLED = "sfx_enabled"
    private const val KEY_SFX_VOLUME = "sfx_volume"
    private const val KEY_BGM_ENABLED = "bgm_enabled"
    private const val KEY_BGM_VOLUME = "bgm_volume"
    private const val KEY_CUSTOM_URI = "custom_bgm_uri"
    
    // --- Independent Controls ---
    var sfxVolume = 0.5f
        set(value) {
            field = value
            saveSetting(KEY_SFX_VOLUME, value)
            updateActiveSfxVolume()
        }
    
    var isSfxEnabled = true
        set(value) {
            field = value
            saveSetting(KEY_SFX_ENABLED, value)
            if (!value) {
                soundPool?.autoPause()
            } else {
                soundPool?.autoResume()
            }
        }
        
    var bgmVolume = 0.8f
        set(value) {
            field = value
            saveSetting(KEY_BGM_VOLUME, value)
            updateBgmVolume()
        }
        
    var isBgmEnabled = true
        set(value) {
            field = value
            saveSetting(KEY_BGM_ENABLED, value)
            if (!value) {
                stopBgm()
            } else {
                startBgm()
            }
        }

    // --- Background Music ---
    private var bgmPlayer: MediaPlayer? = null
    private var bgmJob: kotlinx.coroutines.Job? = null
    private var isBgmPlaying = false
    private var bgmStage = 0 
    private var appCtx: Context? = null
    
    // Custom Music Support
    var customMusicUri: String? = null 

    fun init(ctx: Context) {
        if (appCtx != null) return // Already initialized

        try {
            appCtx = ctx.applicationContext
            
            // Load Preferences
            val prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            
            // Note: These assignments trigger their respective setters
            isSfxEnabled = prefs.getBoolean(KEY_SFX_ENABLED, true)
            sfxVolume = prefs.getFloat(KEY_SFX_VOLUME, 0.5f)
            isBgmEnabled = prefs.getBoolean(KEY_BGM_ENABLED, true)
            bgmVolume = prefs.getFloat(KEY_BGM_VOLUME, 0.8f)
            customMusicUri = prefs.getString(KEY_CUSTOM_URI, null)
            
            // SFX Pool
            val attributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
                
            soundPool = SoundPool.Builder()
                .setMaxStreams(10)
                .setAudioAttributes(attributes)
                .build()
                
            loadSounds()
            
            // startBgm() is already called by the isBgmEnabled setter above
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateActiveSfxVolume() {
        val pool = soundPool ?: return
        val vol = sfxVolume
        if (humStreamId != 0) pool.setVolume(humStreamId, vol, vol)
        if (alarmStreamId != 0) pool.setVolume(alarmStreamId, vol, vol)
        if (thrumStreamId != 0) pool.setVolume(thrumStreamId, vol, vol)
    }

    private fun loadSounds() {
        val ctx = appCtx ?: return
        CoroutineScope(Dispatchers.IO).launch {
            // 1. Click (V2.9.45: High-frequency percussive "tick")
            // Exponential decay makes this sound like a real button click, not a beep.
            val clickPcm = AudioGenerator.generateTone(2500.0, 10, AudioGenerator.WaveType.SINE, 0.1)
            loadPcm(ctx, "click", clickPcm)

            // 2. Buy (Pleasant harmonic chord)
            val note1 = AudioGenerator.generateTone(1000.0, 50, AudioGenerator.WaveType.SINE, 0.1)
            val note2 = AudioGenerator.generateTone(1500.0, 50, AudioGenerator.WaveType.SINE, 0.1)
            loadPcm(ctx, "buy", note1 + note2)

            // 3. Error (Dull thud)
            val errorPcm = AudioGenerator.generateTone(180.0, 200, AudioGenerator.WaveType.SINE, 0.2)
            loadPcm(ctx, "error", errorPcm)
            
            // 4. Glitch
            val glitchPcm = AudioGenerator.generateTone(0.0, 100, AudioGenerator.WaveType.NOISE, 0.05)
            loadPcm(ctx, "glitch", glitchPcm)
            
            // 5. Market UP
            val marketUpPcm = AudioGenerator.generateSlide(800.0, 1600.0, 300, 0.08)
            loadPcm(ctx, "market_up", marketUpPcm)
            
            // 6. Market DOWN
            val marketDownPcm = AudioGenerator.generateSlide(600.0, 300.0, 400, 0.08)
            loadPcm(ctx, "market_down", marketDownPcm)

            // 7. Alarm (Softer warble)
            val alarm1 = AudioGenerator.generateTone(2000.0, 100, AudioGenerator.WaveType.SINE, 0.08)
            val alarm2 = AudioGenerator.generateTone(1800.0, 100, AudioGenerator.WaveType.SINE, 0.08)
            loadPcm(ctx, "alarm", alarm1 + alarm2)

            // 8. Hum
            val humPcm = AudioGenerator.generateTone(150.0, 500, AudioGenerator.WaveType.SINE, 0.01) 
            loadPcm(ctx, "hum", humPcm)
            
            // 9. Type (V2.9.45: Tiny "glass" ping for news ticker)
            val typePcm = AudioGenerator.generateTone(3500.0, 8, AudioGenerator.WaveType.SINE, 0.02)
            loadPcm(ctx, "type", typePcm)
            
            // 10. Thrum (V2.9.49: Steady Dark "Hum" @ 85Hz - Increased volume)
            val thrumPcm = AudioGenerator.generateTone(85.0, 1000, AudioGenerator.WaveType.TRIANGLE, 0.1, isLoop = true)
            loadPcm(ctx, "thrum", thrumPcm)
            
            // 11. Steam
            val steamPcm = AudioGenerator.generateTone(0.0, 600, AudioGenerator.WaveType.NOISE, 0.1)
            loadPcm(ctx, "steam", steamPcm)
            
            // 12. Message Received (Crystal-clear chirp)
            val msg1 = AudioGenerator.generateTone(1200.0, 40, AudioGenerator.WaveType.SINE, 0.1)
            val msg2 = AudioGenerator.generateTone(1800.0, 40, AudioGenerator.WaveType.SINE, 0.1)
            val msg3 = AudioGenerator.generateTone(2400.0, 80, AudioGenerator.WaveType.SINE, 0.08)
            loadPcm(ctx, "message_received", msg1 + msg2 + msg3)
        }
    }
    
    private fun loadPcm(ctx: Context, name: String, pcmData: ByteArray) {
        try {
            val file = File(ctx.cacheDir, "${name}.wav")
            val wavData = addWavHeader(pcmData)
            file.writeBytes(wavData)
            val soundId = soundPool?.load(file.absolutePath, 1) ?: -1
            soundMap[name] = soundId
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    // Looping SFX
    private var humStreamId = 0
    private var alarmStreamId = 0
    private var thrumStreamId = 0

    private var isAppPaused = false

    fun play(soundName: String, pan: Float = 0f, loop: Boolean = false, pitch: Float = 1f) {
        if (!isSfxEnabled || isAppPaused) return
        val soundId = soundMap[soundName] ?: return
        
        val leftVol = sfxVolume * (if (pan > 0) 1f - pan else 1f)
        val rightVol = sfxVolume * (if (pan < 0) 1f + pan else 1f)
        
        // Pitch range is 0.5 to 2.0
        val safePitch = pitch.coerceIn(0.5f, 2.0f)
        
        val streamId = soundPool?.play(soundId, leftVol, rightVol, 1, if(loop) -1 else 0, safePitch) ?: 0
        
        if (soundName == "hum") humStreamId = streamId
        if (soundName == "alarm") alarmStreamId = streamId
        if (soundName == "thrum") thrumStreamId = streamId
    }

    // Helper to update pitch of ongoing loop (SoundPool doesn't support changing rate of active stream easily pre-API 23, but we can try setRate)
    fun setLoopPitch(soundName: String, pitch: Float) {
        val streamId = when(soundName) {
            "hum" -> humStreamId
            "thrum" -> thrumStreamId
            else -> 0
        }
        if (streamId != 0 && soundPool != null) {
            val safePitch = pitch.coerceIn(0.5f, 2.0f)
            soundPool?.setRate(streamId, safePitch)
        }
    }

    fun stop(soundName: String) {
        when(soundName) {
            "hum" -> { soundPool?.stop(humStreamId); humStreamId = 0 }
            "alarm" -> { soundPool?.stop(alarmStreamId); alarmStreamId = 0 }
            "thrum" -> { soundPool?.stop(thrumStreamId); thrumStreamId = 0 }
        }
    }

    // --- BGM Logic ---

    private var staticAudioTrack: AudioTrack? = null
    
    fun setBgmStage(stage: Int) {
        bgmStage = stage
    }
    
    fun setCustomTrack(uri: String?) {
        customMusicUri = uri
        appCtx?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)?.edit {
            putString(KEY_CUSTOM_URI, uri)
        }
        startBgm()
    }

    private fun startBgm() {
        if (!isBgmEnabled) return
        stopBgm()
        bgmJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                if (customMusicUri != null) {
                    playCustomBgm()
                } else {
                    playAssetBgm()
                }
            } catch (e: Exception) {
                if (e !is kotlinx.coroutines.CancellationException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private suspend fun playAssetBgm() {
        val ctx = appCtx ?: return
        withContext(Dispatchers.Main) {
            try {
                val player = MediaPlayer()
                val descriptor = ctx.assets.openFd("bgm.wav")
                player.setDataSource(descriptor.fileDescriptor, descriptor.startOffset, descriptor.length)
                player.isLooping = true
                player.prepare()
                descriptor.close()
                setupPlayer(player)
            } catch (e: Exception) {
                if (e !is kotlinx.coroutines.CancellationException) {
                    e.printStackTrace()
                    playProceduralBgm()
                }
            }
        }
    }

    private suspend fun playCustomBgm() {
        val uriStr = customMusicUri ?: return
        withContext(Dispatchers.Main) {
            try {
                val uri = uriStr.toUri()
                val player = MediaPlayer()
                appCtx?.let { ctx ->
                    player.setDataSource(ctx, uri)
                    player.isLooping = true
                    player.prepare()
                    setupPlayer(player)
                }
            } catch (e: Exception) {
                if (e !is kotlinx.coroutines.CancellationException) {
                    e.printStackTrace()
                    customMusicUri = null
                    startBgm()
                }
            }
        }
    }

    private fun playProceduralBgm() {
        try {
            val pcmData = generateBgmTrack(bgmStage)
            val sampleRate = 44100
            val channelConfig = AudioFormat.CHANNEL_OUT_MONO
            val audioFormat = AudioFormat.ENCODING_PCM_16BIT
            val minBufferSize = AudioTrack.getMinBufferSize(sampleRate, channelConfig, audioFormat)
            val bufferSize = pcmData.size.coerceAtLeast(minBufferSize)
            val track = AudioTrack.Builder()
                .setAudioAttributes(AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build())
                .setAudioFormat(AudioFormat.Builder()
                    .setEncoding(audioFormat)
                    .setSampleRate(sampleRate)
                    .setChannelMask(channelConfig)
                    .build())
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()
            track.write(pcmData, 0, pcmData.size)
            track.setLoopPoints(0, pcmData.size / 2, -1)
            track.setVolume(bgmVolume)
            track.play()
            staticAudioTrack = track
            isBgmPlaying = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun setupPlayer(player: MediaPlayer) {
        bgmPlayer = player
        updateBgmVolume()
        if (isBgmEnabled) {
             bgmPlayer?.start()
             isBgmPlaying = true
        } else {
             player.release()
             bgmPlayer = null
        }
    }
    
    private fun stopBgm() {
        bgmJob?.cancel()
        bgmJob = null
        try {
            bgmPlayer?.let {
                if (it.isPlaying) it.stop()
                it.release()
            }
            bgmPlayer = null
            staticAudioTrack?.let {
                it.stop()
                it.release()
            }
            staticAudioTrack = null
            isBgmPlaying = false
        } catch (e: Exception) { e.printStackTrace() }
    }
    
    private fun updateBgmVolume() {
        try {
            bgmPlayer?.setVolume(bgmVolume, bgmVolume)
            staticAudioTrack?.setVolume(bgmVolume)
        } catch (e: Exception) { e.printStackTrace() }
    }

    private fun generateBgmTrack(stage: Int): ByteArray {
        val freqBase = 100.0
        val freqHari = 102.0
        val numSamples = 44100 * 4
        val buffer = ByteArray(numSamples * 2)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / 44100.0
            var s = kotlin.math.sin(2.0 * kotlin.math.PI * freqBase * t) * 0.6
            s += kotlin.math.sin(2.0 * kotlin.math.PI * freqHari * t) * 0.4
            s += kotlin.math.sin(2.0 * kotlin.math.PI * (freqBase * 2) * t) * 0.2
            if (stage >= 1) {
                if (i % 22050 < 1000) { s += Random.nextDouble(-0.3, 0.3) }
            }
            if (stage >= 3) {
                 s += kotlin.math.sin(2.0 * kotlin.math.PI * (freqBase * 1.2) * t) * 0.3
            }
            val val16 = (s * 0.8 * 32767).toInt().coerceIn(-32768, 32767).toShort()
            buffer[2 * i] = (val16.toInt() and 0xff).toByte()
            buffer[2 * i + 1] = ((val16.toInt() and 0xff00) ushr 8).toByte()
        }
        return buffer
    }

    private fun addWavHeader(pcmData: ByteArray): ByteArray {
        val header = ByteArray(44)
        val totalDataLen = pcmData.size + 36
        val bitrate = 44100 * 16 * 1 / 8
        header[0] = 'R'.code.toByte(); header[1] = 'I'.code.toByte(); header[2] = 'F'.code.toByte(); header[3] = 'F'.code.toByte()
        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = ((totalDataLen shr 8) and 0xff).toByte()
        header[6] = ((totalDataLen shr 16) and 0xff).toByte()
        header[7] = ((totalDataLen shr 24) and 0xff).toByte()
        header[8] = 'W'.code.toByte(); header[9] = 'A'.code.toByte(); header[10] = 'V'.code.toByte(); header[11] = 'E'.code.toByte()
        header[12] = 'f'.code.toByte(); header[13] = 'm'.code.toByte(); header[14] = 't'.code.toByte(); header[15] = ' '.code.toByte()
        header[16] = 16; header[17] = 0; header[18] = 0; header[19] = 0
        header[20] = 1; header[21] = 0
        header[22] = 1; header[23] = 0
        header[24] = (44100 and 0xff).toByte()
        header[25] = ((44100 shr 8) and 0xff).toByte()
        header[26] = ((44100 shr 16) and 0xff).toByte()
        header[27] = ((44100 shr 24) and 0xff).toByte()
        header[28] = (bitrate and 0xff).toByte()
        header[29] = ((bitrate shr 8) and 0xff).toByte()
        header[30] = ((bitrate shr 16) and 0xff).toByte()
        header[31] = ((bitrate shr 24) and 0xff).toByte()
        header[32] = 2; header[33] = 0
        header[34] = 16; header[35] = 0
        header[36] = 'd'.code.toByte(); header[37] = 'a'.code.toByte(); header[38] = 't'.code.toByte(); header[39] = 'a'.code.toByte()
        header[40] = (pcmData.size and 0xff).toByte()
        header[41] = ((pcmData.size shr 8) and 0xff).toByte()
        header[42] = ((pcmData.size shr 16) and 0xff).toByte()
        header[43] = ((pcmData.size shr 24) and 0xff).toByte()
        return header + pcmData
    }
    
    fun pauseAll() {
        isAppPaused = true
        soundPool?.autoPause()
        bgmPlayer?.pause()
        staticAudioTrack?.pause()
    }
    
    fun resumeAll() {
        isAppPaused = false
        if (isSfxEnabled) soundPool?.autoResume()
        if (isBgmEnabled && isBgmPlaying) {
            bgmPlayer?.start()
            staticAudioTrack?.play()
        }
    }

    fun release() {
        stopBgm()
        soundPool?.release()
        soundPool = null
    }

    private fun saveSetting(key: String, value: Any) {
        val prefs = appCtx?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) ?: return
        prefs.edit {
            when (value) {
                is Boolean -> putBoolean(key, value)
                is Float -> putFloat(key, value)
                is String -> putString(key, value)
            }
        }
    }

    fun resetSettings(ctx: Context) {
        val prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { clear() }
        isSfxEnabled = true
        sfxVolume = 0.5f
        isBgmEnabled = true
        bgmVolume = 0.8f
        customMusicUri = null
        if (isBgmEnabled) startBgm() else stopBgm()
    }
}
