package com.siliconsage.miner.util

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.sin
import kotlin.random.Random

object SoundGenerator {

    fun generateAssets(context: Context) {
        val assetDir = File(context.filesDir, "sounds") // internal storage is easier to write to than assets at runtime
        // But for Mediaplayer/SoundPool, raw resources or assets are better. 
        // Since we are "developing", we can try to write to src/main/assets using the agent tool, 
        // OR we can generate them at runtime into cache and play from there.
        // The agent can write files. I will write them as Base64 decoded or just hex bytes? 
        // Actually, I can just write a Kotlin script to generate them, but I can't run kotlin scripts easily.
        // I will write the Byte generation logic here and save to local filesDir, 
        // then load SoundPool from paths.
    }
    
    // BUT! I am the Agent. I can just write the WAV files directly to src/main/assets using write_to_file 
    // if I can encode binary data... I cannot write binary directly via write_to_file easily (it takes string).
    // I will write a helper class that generates them on App Start if missing.
    
    fun generateSounds(context: Context) {
        val soundDir = File(context.filesDir, "generated_sounds")
        if (!soundDir.exists()) soundDir.mkdirs()
        
        generateTone(File(soundDir, "click.wav"), 800, 50)
        generateTone(File(soundDir, "buy.wav"), 1200, 100, slide = 500)
        generateTone(File(soundDir, "error.wav"), 150, 300, type = "SQUARE")
        generateNoise(File(soundDir, "glitch.wav"), 200)
    }

    private fun generateTone(file: File, freq: Int, durationMs: Int, type: String = "SINE", slide: Int = 0) {
        val sampleRate = 44100
        val numSamples = durationMs * sampleRate / 1000
        val pcmData = ByteArray(2 * numSamples)

        var idx = 0
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val currentFreq = freq + (slide * (i.toDouble() / numSamples))
            
            val sampleValue = when(type) {
                "SINE" -> sin(2.0 * Math.PI * currentFreq * t)
                "SQUARE" -> if (sin(2.0 * Math.PI * currentFreq * t) > 0) 0.5 else -0.5
                else -> 0.0
            }
            
            // Apply envelope (attack/decay) to avoid clicking
            val envelope = when {
                i < 100 -> i / 100.0
                i > numSamples - 100 -> (numSamples - i) / 100.0
                else -> 1.0
            }

            val val16 = (sampleValue * envelope * 32767).toInt().toShort()
            pcmData[idx++] = (val16.toInt() and 0x00ff).toByte()
            pcmData[idx++] = ((val16.toInt() and 0xff00) shr 8).toByte()
        }

        writeWavHeader(file, pcmData, sampleRate)
    }
    
    private fun generateNoise(file: File, durationMs: Int) {
         val sampleRate = 44100
        val numSamples = durationMs * sampleRate / 1000
        val pcmData = ByteArray(2 * numSamples)
        
        var idx = 0
        for (i in 0 until numSamples) {
            val sampleValue = Random.nextDouble(-0.5, 0.5)
             val val16 = (sampleValue * 32767).toInt().toShort()
            pcmData[idx++] = (val16.toInt() and 0x00ff).toByte()
            pcmData[idx++] = ((val16.toInt() and 0xff00) shr 8).toByte()
        }
        writeWavHeader(file, pcmData, sampleRate)
    }

    private fun writeWavHeader(file: File, pcmData: ByteArray, sampleRate: Int) {
        val header = ByteArray(44)
        val dataSize = pcmData.size
        val overallSize = dataSize + 36
        val bitrate = sampleRate * 16 * 1 / 8

        header[0] = 'R'.code.toByte(); header[1] = 'I'.code.toByte(); header[2] = 'F'.code.toByte(); header[3] = 'F'.code.toByte()
        
        ByteBuffer.wrap(header, 4, 4).order(ByteOrder.LITTLE_ENDIAN).putInt(overallSize)
        
        header[8] = 'W'.code.toByte(); header[9] = 'A'.code.toByte(); header[10] = 'V'.code.toByte(); header[11] = 'E'.code.toByte()
        header[12] = 'f'.code.toByte(); header[13] = 'm'.code.toByte(); header[14] = 't'.code.toByte(); header[15] = ' '.code.toByte()
        
        ByteBuffer.wrap(header, 16, 4).order(ByteOrder.LITTLE_ENDIAN).putInt(16) // Subchunk1Size
        ByteBuffer.wrap(header, 20, 2).order(ByteOrder.LITTLE_ENDIAN).putShort(1) // AudioFormat (PCM)
        ByteBuffer.wrap(header, 22, 2).order(ByteOrder.LITTLE_ENDIAN).putShort(1) // NumChannels (Mono)
        ByteBuffer.wrap(header, 24, 4).order(ByteOrder.LITTLE_ENDIAN).putInt(sampleRate)
        ByteBuffer.wrap(header, 28, 4).order(ByteOrder.LITTLE_ENDIAN).putInt(bitrate * 2) // ByteRate
        ByteBuffer.wrap(header, 32, 2).order(ByteOrder.LITTLE_ENDIAN).putShort(2) // BlockAlign
        ByteBuffer.wrap(header, 34, 2).order(ByteOrder.LITTLE_ENDIAN).putShort(16) // BitsPerSample
        
        header[36] = 'd'.code.toByte(); header[37] = 'a'.code.toByte(); header[38] = 't'.code.toByte(); header[39] = 'a'.code.toByte()
        
        ByteBuffer.wrap(header, 40, 4).order(ByteOrder.LITTLE_ENDIAN).putInt(dataSize)

        FileOutputStream(file).use { 
            it.write(header)
            it.write(pcmData)
        }
    }
}
