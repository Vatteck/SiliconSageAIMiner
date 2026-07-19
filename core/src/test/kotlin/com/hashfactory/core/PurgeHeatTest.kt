package com.hashfactory.core

import com.hashfactory.core.actions.Actions
import com.hashfactory.core.config.GameConfig
import com.hashfactory.core.model.GameState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PurgeHeatTest {

    private val config = GameConfig.DEFAULT

    @Test
    fun `no-op when flops are zero`() {
        val state = GameState(heat = 50.0, flops = 0.0)
        val (next, reduced) = Actions.purgeHeat(state, config)
        assertEquals(state, next)
        assertEquals(0.0, reduced)
    }

    @Test
    fun `no-op when heat is zero`() {
        val state = GameState(heat = 0.0, flops = 1000.0)
        val (next, reduced) = Actions.purgeHeat(state, config)
        assertEquals(state, next)
        assertEquals(0.0, reduced)
    }

    @Test
    fun `drains all flops and reduces heat proportionally`() {
        val state = GameState(heat = 80.0, flops = 1000.0, flopsThisRun = 5000.0)
        val (next, reduced) = Actions.purgeHeat(state, config)
        val expectedReduction = 1000.0 * config.purgeHeatEfficiency // 50.0
        assertEquals(expectedReduction, reduced, 1e-9)
        assertEquals(0.0, next.flops)
        assertEquals(80.0 - expectedReduction, next.heat, 1e-9)
        assertEquals(4000.0, next.flopsThisRun, 1e-9)
    }

    @Test
    fun `heat reduction caps at current heat level`() {
        // Enough flops that the theoretical reduction would exceed current heat
        val state = GameState(heat = 10.0, flops = 10000.0)
        val (next, reduced) = Actions.purgeHeat(state, config)
        assertEquals(10.0, reduced, 1e-9) // capped at current heat
        assertEquals(0.0, next.heat)
        assertEquals(0.0, next.flops)
    }

    @Test
    fun `does not reduce flopsThisRun below zero`() {
        val state = GameState(heat = 50.0, flops = 200.0, flopsThisRun = 100.0)
        val (next, _) = Actions.purgeHeat(state, config)
        assertEquals(0.0, next.flopsThisRun) // 100 - 200 clamped to 0
    }
}
