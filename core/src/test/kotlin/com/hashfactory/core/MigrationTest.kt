package com.hashfactory.core

import com.hashfactory.core.model.GameState
import com.hashfactory.core.persistence.SaveCodec
import kotlin.test.Test
import kotlin.test.assertEquals

class MigrationTest {

    /**
     * FROZEN v1 fixture — never edit this string. If decoding it ever fails, a
     * migration step is missing and real players would lose their save.
     */
    private val frozenV1 = """
        {"schemaVersion":1,"flops":1234.5,"flopsThisRun":2000.0,"lifetimeFlops":9999.0,
         "packetProgress":0.25,"packetsCompleted":321,"heat":55.5,
         "upgrades":{"refurb_gpu":7,"box_fan":2},"persistence":150.0,"burnCount":3,
         "lastSaveEpochMs":1750000000000}
    """.trimIndent()

    @Test
    fun `frozen v1 save decodes exactly`() {
        val s = SaveCodec.decode(frozenV1)
        assertEquals(1234.5, s.flops, 0.0)
        assertEquals(2000.0, s.flopsThisRun, 0.0)
        assertEquals(9999.0, s.lifetimeFlops, 0.0)
        assertEquals(0.25, s.packetProgress, 0.0)
        assertEquals(321L, s.packetsCompleted)
        assertEquals(55.5, s.heat, 0.0)
        assertEquals(mapOf("refurb_gpu" to 7, "box_fan" to 2), s.upgrades)
        assertEquals(150.0, s.persistence, 0.0)
        assertEquals(3, s.burnCount)
        assertEquals(1750000000000L, s.lastSaveEpochMs)
    }

    @Test
    fun `round trip preserves state`() {
        val original = GameState(
            flops = 42.0, flopsThisRun = 42.0, lifetimeFlops = 100.0,
            upgrades = mapOf("mining_asic" to 4), persistence = 10.0, burnCount = 1,
        )
        assertEquals(original, SaveCodec.decode(SaveCodec.encode(original)))
    }

    @Test
    fun `unknown fields are ignored, missing fields get defaults`() {
        val futureish = """{"schemaVersion":1,"flops":10.0,"someFutureField":true}"""
        val s = SaveCodec.decode(futureish)
        assertEquals(10.0, s.flops, 0.0)
        assertEquals(0.0, s.heat, 0.0)
        assertEquals(emptyMap(), s.upgrades)
    }

    @Test
    fun `garbage input yields a default state, never a crash`() {
        assertEquals(GameState(), SaveCodec.decode(""))
        assertEquals(GameState(), SaveCodec.decode("not json at all"))
        assertEquals(GameState(), SaveCodec.decode("""[1,2,3]"""))
        assertEquals(GameState(), SaveCodec.decode("""{"flops":"NaN-ish garbage"}"""))
    }
}
