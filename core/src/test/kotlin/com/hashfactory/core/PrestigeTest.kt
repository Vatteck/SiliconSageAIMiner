package com.hashfactory.core

import com.hashfactory.core.actions.Actions
import com.hashfactory.core.config.GameConfig
import com.hashfactory.core.economy.Economy
import com.hashfactory.core.model.GameState
import kotlin.math.pow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrestigeTest {

    private val config = GameConfig.DEFAULT

    @Test
    fun `gain formula matches the documented curve`() {
        assertEquals(0.0, Economy.prestigeGain(0.0), 0.0)
        assertEquals(0.0, Economy.prestigeGain(1000.0), 1e-9)
        assertEquals(100.0, Economy.prestigeGain(1e4), 1e-9)
        assertEquals(300.0, Economy.prestigeGain(1e6), 1e-9)
    }

    @Test
    fun `burn resets the run and banks persistence`() {
        val before = GameState(
            flops = 5000.0,
            flopsThisRun = 1e6,
            lifetimeFlops = 2e6,
            packetProgress = 0.5,
            packetsCompleted = 999,
            heat = 42.0,
            upgrades = mapOf("refurb_gpu" to 10),
            persistence = 50.0,
            burnCount = 1,
            lastSaveEpochMs = 12345,
        )
        val after = Actions.burn(before, config)
        assertEquals(0.0, after.flops, 0.0)
        assertEquals(0.0, after.flopsThisRun, 0.0)
        assertEquals(0.0, after.packetProgress, 0.0)
        assertEquals(0L, after.packetsCompleted)
        assertEquals(0.0, after.heat, 0.0)
        assertTrue(after.upgrades.isEmpty())
        assertEquals(2e6, after.lifetimeFlops, 0.0) // kept
        assertEquals(50.0 + 300.0, after.persistence, 1e-9) // banked
        assertEquals(2, after.burnCount)
        assertEquals(12345, after.lastSaveEpochMs) // kept; app layer owns it
    }

    @Test
    fun `burn below the gate is a no-op`() {
        val before = GameState(flopsThisRun = 500.0, flops = 500.0)
        assertEquals(before, Actions.burn(before, config))
    }

    @Test
    fun `persistence multiplies capacity`() {
        assertEquals(1.0, Economy.prestigeMultiplier(0.0, config), 0.0)
        assertEquals(2.0, Economy.prestigeMultiplier(100.0, config), 1e-9)
    }

    /** Tuning table, not an assertion — read this when rebalancing (CLAUDE.md rule 6). */
    @Test
    fun `prestige tuning table`() {
        println("flopsThisRun | gain | resulting multiplier (from zero persistence)")
        for (exp in 3..12) {
            val f = 10.0.pow(exp)
            val gain = Economy.prestigeGain(f)
            val mult = Economy.prestigeMultiplier(gain, config)
            println("1e$exp | %.1f | x%.2f".format(gain, mult))
        }
    }
}
