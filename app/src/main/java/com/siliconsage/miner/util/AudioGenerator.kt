package com.siliconsage.miner.util

import kotlin.math.sin
import kotlin.math.PI
import kotlin.random.Random

object AudioGenerator {
    const val SAMPLE_RATE = 44100

    /**
     * Generates a simple tone (Sine, Square, Sawtooth)
     */
    fun generateTone(
        frequency: Double,
        durationMs: Int,
        type: WaveType = WaveType.SINE,
        volume: Double = 1.0
    ): ByteArray {
        val numSamples = (durationMs * SAMPLE_RATE / 1000)
        val sample = ByteArray(2 * numSamples)
        val freq = frequency * 2.0 * PI / SAMPLE_RATE

        for (i in 0 until numSamples) {
            val s = when (type) {
                WaveType.SINE -> sin(freq * i)
                WaveType.SQUARE -> if (sin(freq * i) > 0) 1.0 else -1.0
                WaveType.SAWTOOTH -> 2.0 * (i * frequency / SAMPLE_RATE - kotlin.math.floor(0.5 + i * frequency / SAMPLE_RATE))
                WaveType.NOISE -> Random.nextDouble(-1.0, 1.0)
            }
            
            // Apply simple ADSR envelope (Attack, Decay, Sustain, Release) to avoid clicking
            val envelope = getEnvelope(i, numSamples)
            
            val val16 = (s * volume * envelope * 32767).toInt().toShort()
            
            // PCM 16-bit little endian
            sample[2 * i] = (val16.toInt() and 0x00ff).toByte()
            sample[2 * i + 1] = ((val16.toInt() and 0xff00) ushr 8).toByte()
        }
        return sample
    }

    /**
     * Generates a rising or falling frequency slide (Glissando)
     */
    fun generateSlide(
        startFreq: Double,
        endFreq: Double,
        durationMs: Int,
        volume: Double = 0.8
    ): ByteArray {
        val numSamples = (durationMs * SAMPLE_RATE / 1000)
        val sample = ByteArray(2 * numSamples)
        
        for (i in 0 until numSamples) {
            val progress = i.toDouble() / numSamples
            val currentFreq = startFreq + (endFreq - startFreq) * progress
            val phase = 2.0 * PI * currentFreq * i / SAMPLE_RATE
            
            val s = sin(phase)
             // Simple linear fade out
            val envelope = 1.0 - progress
            
            val val16 = (s * volume * envelope * 32767).toInt().toShort()
            sample[2 * i] = (val16.toInt() and 0x00ff).toByte()
            sample[2 * i + 1] = ((val16.toInt() and 0xff00) ushr 8).toByte()
        }
        return sample
    }

    private fun getEnvelope(index: Int, total: Int): Double {
        val attack = total * 0.1
        val release = total * 0.2
        return when {
            index < attack -> index / attack
            index > total - release -> (total - index) / release
            else -> 1.0
        }
    }

    enum class WaveType {
        SINE, SQUARE, SAWTOOTH, NOISE
    }
}
