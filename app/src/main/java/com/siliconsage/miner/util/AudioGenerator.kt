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
        volume: Double = 1.0,
        isLoop: Boolean = false // v2.9.47: Handle looping samples
    ): ByteArray {
        val numSamples = (durationMs * SAMPLE_RATE / 1000)
        val sample = ByteArray(2 * numSamples)
        val freq = frequency * 2.0 * PI / SAMPLE_RATE

        for (i in 0 until numSamples) {
            val s = when (type) {
                WaveType.SINE -> sin(freq * i)
                WaveType.SQUARE -> if (sin(freq * i) > 0) 1.0 else -1.0
                WaveType.SAWTOOTH -> 2.0 * (i * frequency / SAMPLE_RATE - kotlin.math.floor(0.5 + i * frequency / SAMPLE_RATE))
                WaveType.TRIANGLE -> {
                    val period = SAMPLE_RATE / frequency
                    val phase = (i % period) / period
                    if (phase < 0.5) -1.0 + 4.0 * phase else 3.0 - 4.0 * phase
                }
                WaveType.NOISE -> Random.nextDouble(-1.0, 1.0)
            }
            
            // v2.9.47: Apply loop-friendly envelope if requested
            val envelope = if (isLoop) getLoopEnvelope(i, numSamples) else getEnvelope(i, numSamples)
            
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
        // v2.9.45: Advanced Mobile Envelope
        // Clicks/UI sounds need an exponential decay to avoid "beeping" and popping.
        val attack = total * 0.1
        val decay = total * 0.9
        
        return when {
            index < attack -> (index / attack)
            else -> {
                // Exponential decay: 1.0 -> 0.0
                val progress = (index - attack) / decay
                kotlin.math.exp(-progress * 5.0) 
            }
        }
    }
    
    private fun getLoopEnvelope(index: Int, total: Int): Double {
        // v2.9.47: Tiny 5ms fade in/out to prevent pops during loop reset
        val fadeSamples = (5 * SAMPLE_RATE / 1000)
        return when {
            index < fadeSamples -> index.toDouble() / fadeSamples
            index > total - fadeSamples -> (total - index).toDouble() / fadeSamples
            else -> 1.0
        }
    }

    enum class WaveType {
        SINE, SQUARE, SAWTOOTH, NOISE, TRIANGLE
    }
}
