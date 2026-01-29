package com.siliconsage.miner.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import android.media.SoundPool
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
    
    // --- Independent Controls ---
    var sfxVolume = 0.5f
    
    var isSfxEnabled = true
        set(value) {
            field = value
            if (!value) {
                // Stop all active SFX
                soundPool?.autoPause()
            } else {
                soundPool?.autoResume()
            }
        }
        
    var bgmVolume = 0.8f
        set(value) {
            field = value
            updateBgmVolume()
        }
        
    var isBgmEnabled = true
        set(value) {
            field = value
            if (!value) {
                stopBgm()
            } else {
                startBgm()
            }
        }

    // --- Background Music ---
    private var bgmPlayer: MediaPlayer? = null
    private var isBgmPlaying = false
    private var bgmStage = 0 
    private var context: Context? = null
    
    // Custom Music Support
    var customMusicUri: String? = null 
    private const val PREFS_NAME = "audio_prefs"
    private const val KEY_CUSTOM_URI = "custom_bgm_uri"

    fun init(ctx: Context) {
        try {
            context = ctx.applicationContext
            
            // Load Preferences
            val prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
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
            startBgm()
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadSounds() {
        val ctx = context ?: return
        CoroutineScope(Dispatchers.IO).launch {
            // 1. Click
            val clickPcm = AudioGenerator.generateTone(350.0, 25, AudioGenerator.WaveType.SQUARE, 0.3)
            loadPcm(ctx, "click", clickPcm)

            // 2. Buy
            val note1 = AudioGenerator.generateTone(800.0, 40, AudioGenerator.WaveType.SINE, 0.3)
            val note2 = AudioGenerator.generateTone(1200.0, 40, AudioGenerator.WaveType.SINE, 0.3)
            val note3 = AudioGenerator.generateTone(1600.0, 80, AudioGenerator.WaveType.SINE, 0.2)
            loadPcm(ctx, "buy", note1 + note2 + note3)

            // 3. Error
            val errorPcm = AudioGenerator.generateTone(150.0, 400, AudioGenerator.WaveType.SAWTOOTH, 0.6)
            loadPcm(ctx, "error", errorPcm)
            
            // 4. Glitch
            val glitchPcm = AudioGenerator.generateTone(0.0, 100, AudioGenerator.WaveType.NOISE, 0.5)
            loadPcm(ctx, "glitch", glitchPcm)
            
            // 5. Market UP
            val marketUpPcm = AudioGenerator.generateSlide(400.0, 1200.0, 400, 0.4)
            loadPcm(ctx, "market_up", marketUpPcm)
            
            // 6. Market DOWN
            val marketDownPcm = AudioGenerator.generateSlide(300.0, 60.0, 500, 0.5)
            loadPcm(ctx, "market_down", marketDownPcm)

            // 7. Alarm
            val alarmPcm = AudioGenerator.generateTone(600.0, 200, AudioGenerator.WaveType.SAWTOOTH, 0.6)
            loadPcm(ctx, "alarm", alarmPcm)

            // 8. Hum (Lowered volume from 0.3 to 0.1 to be less annoying)
            val humPcm = AudioGenerator.generateTone(60.0, 500, AudioGenerator.WaveType.SQUARE, 0.1)
            loadPcm(ctx, "hum", humPcm)
            
            // 9. Type
            val typePcm = AudioGenerator.generateTone(800.0, 40, AudioGenerator.WaveType.NOISE, 0.05)
            loadPcm(ctx, "type", typePcm)
            
            // 10. Thrum (Overclock loop) - Low throbbing
            val thrumPcm = AudioGenerator.generateTone(50.0, 400, AudioGenerator.WaveType.SAWTOOTH, 0.2)
            loadPcm(ctx, "thrum", thrumPcm)
            
            // 11. Steam (Purge) - White noise fade
            val steamPcm = AudioGenerator.generateTone(0.0, 800, AudioGenerator.WaveType.NOISE, 0.4)
            // (Note: accurate ADSR would be better, but simple noise burst works for now)
            loadPcm(ctx, "steam", steamPcm)
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

    fun play(soundName: String, pan: Float = 0f, loop: Boolean = false) {
        if (!isSfxEnabled || isAppPaused) return
        val soundId = soundMap[soundName] ?: return
        
        val leftVol = sfxVolume * (if (pan > 0) 1f - pan else 1f)
        val rightVol = sfxVolume * (if (pan < 0) 1f + pan else 1f)
        
        val streamId = soundPool?.play(soundId, leftVol, rightVol, 1, if(loop) -1 else 0, 1f) ?: 0
        
        if (soundName == "hum") humStreamId = streamId
        if (soundName == "alarm") alarmStreamId = streamId
        if (soundName == "thrum") thrumStreamId = streamId
    }

    fun stop(soundName: String) {
        when(soundName) {
            "hum" -> { soundPool?.stop(humStreamId); humStreamId = 0 }
            "alarm" -> { soundPool?.stop(alarmStreamId); alarmStreamId = 0 }
            "thrum" -> { soundPool?.stop(thrumStreamId); thrumStreamId = 0 }
        }
    }

    // --- BGM Logic ---

    // Procedural State (Legacy fallback)
    private var staticAudioTrack: AudioTrack? = null
    
    fun setBgmStage(stage: Int) {
        bgmStage = stage
        // Asset BGM doesn't currently use stages, but we keep this for legacy logic if needed.
    }
    
    fun setCustomTrack(uri: String?) {
        customMusicUri = uri
        
        // Save to Prefs
        context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            ?.edit()
            ?.putString(KEY_CUSTOM_URI, uri)
            ?.apply()
            
        startBgm()
    }

    private fun startBgm() {
        if (!isBgmEnabled) return
        
        stopBgm()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (customMusicUri != null) {
                    playCustomBgm()
                } else {
                    playAssetBgm()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun playAssetBgm() {
        withContext(Dispatchers.Main) {
            try {
                val ctx = context ?: return@withContext
                val player = MediaPlayer()
                val descriptor = ctx.assets.openFd("bgm.wav")
                player.setDataSource(descriptor.fileDescriptor, descriptor.startOffset, descriptor.length)
                player.isLooping = true
                player.prepare()
                descriptor.close()
                setupPlayer(player)
            } catch (e: Exception) {
                e.printStackTrace()
                // If asset fails, fallback to procedural synth as a fail-safe
                playProceduralBgm()
            }
        }
    }

    private suspend fun playCustomBgm() {
        withContext(Dispatchers.Main) {
            try {
                val uri = android.net.Uri.parse(customMusicUri)
                val player = MediaPlayer()
                
                context?.let { ctx ->
                    player.setDataSource(ctx, uri)
                    player.isLooping = true
                    player.prepare()
                    setupPlayer(player)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                customMusicUri = null
                startBgm()
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
        }
    }
    
    private fun stopBgm() {
        try {
            if (bgmPlayer != null) {
                bgmPlayer?.stop()
                bgmPlayer?.release()
                bgmPlayer = null
            }
            if (staticAudioTrack != null) {
                staticAudioTrack?.stop()
                staticAudioTrack?.release()
                staticAudioTrack = null
            }
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
        if (isBgmPlaying) {
            bgmPlayer?.pause()
            staticAudioTrack?.pause()
        }
    }
    
    fun resumeAll() {
        isAppPaused = false
        if (isSfxEnabled) {
            soundPool?.autoResume()
        }
        if (isBgmEnabled && isBgmPlaying) {
            bgmPlayer?.start()
            staticAudioTrack?.play()
        }
    }

    fun release() {
        stopBgm()
        bgmPlayer?.release()
        bgmPlayer = null
        
        soundPool?.release()
        soundPool = null
    }
}
