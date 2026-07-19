package com.hashfactory.core

import com.hashfactory.core.config.GameConfig
import com.hashfactory.core.economy.Economy
import com.hashfactory.core.model.GameState
import com.hashfactory.core.sim.Derived
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Fixtures from docs/design/production-loop-golden-values.md — the legacy game's
 * audited production math. These numbers are load-bearing: they preserve behavior
 * parity with the game players already know.
 */
class GoldenValuesTest {

    private val config = GameConfig.DEFAULT

    private fun stateWith(id: String, level: Int) = GameState(upgrades = mapOf(id to level))

    @Test
    fun `refurbished gpu golden capacity values`() {
        // level * base * 2^(level/25), integer division milestones
        assertEquals(2.0, Derived.capacity(stateWith("refurb_gpu", 1), config), 1e-9)
        assertEquals(48.0, Derived.capacity(stateWith("refurb_gpu", 24), config), 1e-9)
        assertEquals(100.0, Derived.capacity(stateWith("refurb_gpu", 25), config), 1e-9)
    }

    @Test
    fun `dual gpu rig and asic golden values`() {
        assertEquals(8.0, Derived.capacity(stateWith("dual_gpu_rig", 1), config), 1e-9)
        assertEquals(35.0, Derived.capacity(stateWith("mining_asic", 1), config), 1e-9)
    }

    @Test
    fun `milestone multiplier steps at whole 25s only`() {
        assertEquals(1.0, Economy.milestoneMultiplier(1), 0.0)
        assertEquals(1.0, Economy.milestoneMultiplier(24), 0.0)
        assertEquals(2.0, Economy.milestoneMultiplier(25), 0.0)
        assertEquals(2.0, Economy.milestoneMultiplier(49), 0.0)
        assertEquals(4.0, Economy.milestoneMultiplier(50), 0.0)
        assertEquals(4.0, Economy.milestoneMultiplier(74), 0.0)
    }

    @Test
    fun `packet payout is capacity over ten with floor of one`() {
        assertEquals(1.0, Derived.packetPayout(0.0, config), 0.0)
        assertEquals(1.0, Derived.packetPayout(5.0, config), 0.0) // sub-10 capacity floors
        assertEquals(1.0, Derived.packetPayout(10.0, config), 0.0)
        assertEquals(10.0, Derived.packetPayout(100.0, config), 1e-9)
    }

    @Test
    fun `wallet rate equals capacity through the packet loop`() {
        // capacity C with payout C/10 completes 10 packets/s -> C flops/s.
        val state = stateWith("refurb_gpu", 25) // capacity 100, draw 25 > base 10 power!
        // Use a power-satisfied state instead: add industrial feed.
        val powered = state.copy(upgrades = state.upgrades + ("industrial_feed" to 1))
        val effCap = Derived.effectiveCapacity(powered, config)
        assertEquals(100.0, effCap, 1e-9)
        val payout = Derived.packetPayout(effCap, config)
        assertEquals(10.0, payout, 1e-9)
        assertEquals(10.0, effCap / payout, 1e-9) // packets per second
    }
}
