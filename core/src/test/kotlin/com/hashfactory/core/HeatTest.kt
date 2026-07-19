package com.hashfactory.core

import com.hashfactory.core.config.GameConfig
import com.hashfactory.core.model.GameState
import com.hashfactory.core.sim.Derived
import com.hashfactory.core.sim.Simulation
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HeatTest {

    private val config = GameConfig.DEFAULT

    @Test
    fun `heat rises under hot hardware and settles at a stable equilibrium`() {
        // 20 refurb GPUs: heatGen 8/s vs cooling 1/s -> must enter the throttle zone.
        var s = GameState(upgrades = mapOf("refurb_gpu" to 20, "wall_tap" to 1))
        var throttled = false
        repeat(20_000) {
            val r = Simulation.tick(s, 0.1, config)
            s = r.state
            if (Derived.heatThrottle(s.heat, config) < 1.0) throttled = true
        }
        assertTrue(throttled, "expected thermal throttle to engage")
        assertTrue(s.heat > config.throttleStartHeat && s.heat <= config.maxHeat)
        // Equilibrium: another 100 ticks barely move the needle.
        val before = s.heat
        repeat(100) { s = Simulation.tick(s, 0.1, config).state }
        assertTrue(abs(s.heat - before) < 1.0, "heat still moving: $before -> ${s.heat}")
        // Throttle floor keeps the rig producing even when hot.
        assertTrue(Derived.effectiveCapacity(s, config) > 0.0)
    }

    @Test
    fun `cooling-heavy rig pins heat at zero`() {
        var s = GameState(upgrades = mapOf("refurb_gpu" to 2, "box_fan" to 5))
        repeat(1000) { s = Simulation.tick(s, 0.1, config).state }
        assertEquals(0.0, s.heat, 1e-9)
    }

    @Test
    fun `throttle curve shape`() {
        assertEquals(1.0, Derived.heatThrottle(0.0, config), 0.0)
        assertEquals(1.0, Derived.heatThrottle(80.0, config), 0.0)
        assertEquals(0.55, Derived.heatThrottle(90.0, config), 1e-9)
        assertEquals(config.minThrottle, Derived.heatThrottle(100.0, config), 1e-9)
    }

    @Test
    fun `state never goes non-finite or out of range across configurations`() {
        val combos = listOf(
            emptyMap(),
            mapOf("refurb_gpu" to 1),
            mapOf("refurb_gpu" to 100, "industrial_feed" to 5),
            mapOf("tensor_unit" to 50, "liquid_loop" to 3, "industrial_feed" to 20),
            mapOf("box_fan" to 50),
        )
        for (upgrades in combos) {
            var s = GameState(upgrades = upgrades)
            repeat(5000) { s = Simulation.tick(s, 0.1, config).state }
            assertTrue(s.flops.isFinite() && s.flops >= 0.0, "flops broken for $upgrades")
            assertTrue(s.heat.isFinite() && s.heat in 0.0..config.maxHeat, "heat broken for $upgrades")
            assertTrue(s.packetProgress.isFinite() && s.packetProgress >= 0.0)
        }
    }
}
