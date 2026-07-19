package com.hashfactory.core

import com.hashfactory.core.actions.Actions
import com.hashfactory.core.config.GameConfig
import com.hashfactory.core.model.GameState
import com.hashfactory.core.persistence.SaveCodec
import com.hashfactory.core.sim.Derived
import com.hashfactory.core.sim.Simulation
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OverclockTest {

    private val config = GameConfig.DEFAULT

    // Enough power headroom that only the overclock multiplier differs.
    private val base = GameState(upgrades = mapOf("refurb_gpu" to 5, "wall_tap" to 2))

    @Test
    fun `overclock multiplies capacity by the configured factor`() {
        val on = base.copy(overclocked = true)
        assertEquals(
            Derived.capacity(base, config) * config.overclockOutputMult,
            Derived.capacity(on, config),
            1e-9,
        )
    }

    @Test
    fun `overclock multiplies heat generation by the configured factor`() {
        val on = base.copy(overclocked = true)
        assertEquals(
            Derived.heatGeneration(base, config) * config.overclockHeatMult,
            Derived.heatGeneration(on, config),
            1e-9,
        )
    }

    @Test
    fun `overclocked rig earns more but runs hotter over the same interval`() {
        var normal = base
        var hot = base.copy(overclocked = true)
        repeat(600) {
            normal = Simulation.tick(normal, 0.1, config).state
            hot = Simulation.tick(hot, 0.1, config).state
        }
        assertTrue(hot.flops > normal.flops, "overclock should out-earn: ${hot.flops} vs ${normal.flops}")
        assertTrue(hot.heat > normal.heat, "overclock should run hotter: ${hot.heat} vs ${normal.heat}")
    }

    @Test
    fun `toggle flips the flag and nothing else`() {
        val on = Actions.toggleOverclock(base)
        assertTrue(on.overclocked)
        assertEquals(base, Actions.toggleOverclock(on))
    }

    @Test
    fun `burn resets overclock`() {
        val ready = base.copy(overclocked = true, flopsThisRun = 1e9)
        assertFalse(Actions.burn(ready, config).overclocked)
    }

    @Test
    fun `saves without the field decode to overclock off`() {
        val preOverclockSave = """{"schemaVersion":1,"flops":50.0,"upgrades":{"refurb_gpu":3}}"""
        assertFalse(SaveCodec.decode(preOverclockSave).overclocked)
    }

    @Test
    fun `overclock state survives a save round trip`() {
        val on = base.copy(overclocked = true)
        assertEquals(on, SaveCodec.decode(SaveCodec.encode(on)))
    }
}
