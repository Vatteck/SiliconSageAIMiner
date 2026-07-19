package com.hashfactory.core

import com.hashfactory.core.config.GameConfig
import com.hashfactory.core.model.GameState
import com.hashfactory.core.sim.Derived
import kotlin.test.Test
import kotlin.test.assertEquals

class PowerTest {

    private val config = GameConfig.DEFAULT

    @Test
    fun `power factor is one when draw fits the budget`() {
        // 5 GPUs draw 5 vs base capacity 10.
        val s = GameState(upgrades = mapOf("refurb_gpu" to 5))
        assertEquals(1.0, Derived.powerFactor(s, config), 0.0)
    }

    @Test
    fun `overdraw scales capacity proportionally`() {
        // 20 GPUs draw 20 vs base 10 -> factor 0.5; capacity 40 -> effective 20.
        val s = GameState(upgrades = mapOf("refurb_gpu" to 20))
        assertEquals(0.5, Derived.powerFactor(s, config), 1e-9)
        assertEquals(20.0, Derived.effectiveCapacity(s, config), 1e-9)
    }

    @Test
    fun `power upgrades restore the budget`() {
        val s = GameState(upgrades = mapOf("refurb_gpu" to 20, "wall_tap" to 1))
        assertEquals(1.0, Derived.powerFactor(s, config), 0.0)
    }

    @Test
    fun `no hardware means full factor`() {
        assertEquals(1.0, Derived.powerFactor(GameState(), config), 0.0)
    }
}
