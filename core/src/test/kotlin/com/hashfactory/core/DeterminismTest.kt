package com.hashfactory.core

import com.hashfactory.core.config.GameConfig
import com.hashfactory.core.model.GameState
import com.hashfactory.core.sim.Simulation
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DeterminismTest {

    private val config = GameConfig.DEFAULT
    private val start = GameState(
        upgrades = mapOf("refurb_gpu" to 5, "box_fan" to 1),
    )

    @Test
    fun `same inputs produce structurally identical outputs`() {
        var a = start
        var b = start
        repeat(500) {
            a = Simulation.tick(a, 0.1, config).state
            b = Simulation.tick(b, 0.1, config).state
        }
        assertEquals(a, b)
    }

    @Test
    fun `coarse and fine timesteps agree within packet quantization`() {
        var fine = start
        repeat(100) { fine = Simulation.tick(fine, 0.1, config).state }
        val coarse = Simulation.tick(start, 10.0, config).state

        // capacity 10 -> payout 1.0; wallet totals may differ by at most one
        // in-flight packet's worth, heat by integration granularity.
        assertEquals(coarse.flops, fine.flops, 1.5)
        assertEquals(coarse.heat, fine.heat, 1.0)
        assertTrue(fine.flops > 0.0)
    }
}
