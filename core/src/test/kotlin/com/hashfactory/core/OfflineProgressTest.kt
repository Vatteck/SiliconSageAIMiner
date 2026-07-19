package com.hashfactory.core

import com.hashfactory.core.config.GameConfig
import com.hashfactory.core.model.GameState
import com.hashfactory.core.sim.Simulation
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OfflineProgressTest {

    private val config = GameConfig.DEFAULT
    private val start = GameState(upgrades = mapOf("refurb_gpu" to 8, "box_fan" to 2))

    @Test
    fun `offline replay equals online ticking exactly`() {
        val offline = Simulation.simulateOffline(start, 3600.0, config)
        var online = start
        repeat(3600) { online = Simulation.tick(online, 1.0, config).state }
        assertEquals(online, offline.state)
        assertEquals(online.flops - start.flops, offline.flopsEarned, 1e-9)
        assertFalse(offline.wasCapped)
    }

    @Test
    fun `offline time is capped`() {
        val tenHours = 10.0 * 3600.0
        val result = Simulation.simulateOffline(start, tenHours, config)
        assertTrue(result.wasCapped)
        assertEquals(config.offlineCapSeconds, result.secondsSimulated, 0.0)
        val exactlyCap = Simulation.simulateOffline(start, config.offlineCapSeconds, config)
        assertEquals(exactlyCap.state, result.state)
    }

    @Test
    fun `zero or negative elapsed is a no-op`() {
        assertEquals(start, Simulation.simulateOffline(start, 0.0, config).state)
        assertEquals(start, Simulation.simulateOffline(start, -500.0, config).state)
        assertEquals(0.0, Simulation.simulateOffline(start, -500.0, config).flopsEarned, 0.0)
    }
}
