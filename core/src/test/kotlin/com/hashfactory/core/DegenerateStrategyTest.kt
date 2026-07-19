package com.hashfactory.core

import com.hashfactory.core.actions.Actions
import com.hashfactory.core.config.GameConfig
import com.hashfactory.core.config.UpgradeCategory
import com.hashfactory.core.config.UpgradeDefs
import com.hashfactory.core.economy.Economy
import com.hashfactory.core.model.GameState
import com.hashfactory.core.sim.Derived
import com.hashfactory.core.sim.Simulation
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Bots that play suboptimally or recklessly. Each must verify the game doesn't
 * brick, NaN, or death-spiral under degenerate strategies.
 */
class DegenerateStrategyTest {

    private val config = GameConfig.DEFAULT

    // ── helpers ──────────────────────────────────────────────────────────

    private fun allFieldsFinite(s: GameState): Boolean =
        s.flops.isFinite() && s.flopsThisRun.isFinite() && s.lifetimeFlops.isFinite() &&
        s.heat.isFinite() && s.packetProgress.isFinite() && s.persistence.isFinite()

    private fun buyCheapest(s: GameState, category: UpgradeCategory): GameState {
        val id = UpgradeDefs.ALL
            .filter { it.category == category }
            .filter { Economy.singleCost(it, s.level(it.id)) <= s.flops }
            .minByOrNull { Economy.singleCost(it, s.level(it.id)) }?.id
            ?: return s
        return Actions.buyUpgrade(s, id, 1, config).state
    }

    private fun tickAndTap(state: GameState, dt: Double, taps: Int): GameState {
        var s = state
        repeat(taps) { s = Actions.manualCompute(s, config).state }
        return Simulation.tick(s, dt, config).state
    }

    // ── tests ────────────────────────────────────────────────────────────

    @Test
    fun `cooling-only pacifist never bricks and stays cool`() {
        var s = GameState()
        var maxHeat = 0.0
        var reachedFlops = false

        for (second in 0 until 600) { // 10 game-minutes
            s = tickAndTap(s, 0.1, 1)
            s = buyCheapest(s, UpgradeCategory.COOLING)
            maxHeat = maxOf(maxHeat, s.heat)
            if (s.flops > 1.0) reachedFlops = true
            assertTrue(allFieldsFinite(s), "NaN/Inf at second $second")
        }

        assertTrue(reachedFlops, "pacifist never earned a single flop — game is bricked")
        assertTrue(maxHeat < config.maxHeat * 0.8, "heat hit %.0f with cooling only — runaway".format(maxHeat))
    }

    @Test
    fun `reckless overclocker self-stabilizes and still progresses`() {
        // Start with a few GPUs so heat generation is meaningful from tick 0.
        var s = GameState(
            overclocked = true,
            upgrades = mapOf("refurb_gpu" to 3),
        )
        var throttleSeen = false

        // Run for 600s — long enough for heat to build and settle.
        for (second in 0 until 600) {
            s = tickAndTap(s, 0.1, 3)
            s = buyCheapest(s, UpgradeCategory.HARDWARE)
            if (Derived.heatThrottle(s.heat, config) < 1.0) throttleSeen = true
            assertTrue(allFieldsFinite(s), "NaN/Inf at second $second")
        }

        // Tick another 60s to confirm it hasn't death-spiraled.
        for (second in 0 until 60) {
            s = tickAndTap(s, 0.1, 3)
            s = buyCheapest(s, UpgradeCategory.HARDWARE)
        }

        assertTrue(throttleSeen, "overclock never triggered throttle — friction missing")
        assertTrue(s.flops > 0.0, "overclocker earned nothing — game is bricked")
        assertTrue(s.heat < config.maxHeat, "heat hit max — death spiral")
    }

    @Test
    fun `purge spammer stays in control and never cheats economy`() {
        var s = GameState()
        var purgeCount = 0
        var totalHeatBefore = 0.0
        var totalHeatAfter = 0.0

        // Let the bot earn some flops first, so it has something to burn.
        for (second in 0 until 120) {
            s = tickAndTap(s, 0.1, 5)
            s = buyCheapest(s, UpgradeCategory.HARDWARE)
        }

        // Now spam purges whenever heat > 50 for another 300s.
        for (second in 0 until 300) {
            s = tickAndTap(s, 0.1, 3)
            s = buyCheapest(s, UpgradeCategory.HARDWARE)

            if (s.heat > 50.0 && s.flops > 0.0) {
                val heatBefore = s.heat
                val flopsBefore = s.flops
                val (next, reduced) = Actions.purgeHeat(s, config)
                if (reduced > 0.0) {
                    purgeCount++
                    totalHeatBefore += heatBefore
                    totalHeatAfter += next.heat
                    // Purge must actually reduce heat.
                    assertTrue(next.heat < heatBefore, "purge did not reduce heat")
                    // Must drain all flops.
                    assertTrue(next.flops == 0.0, "purge left flops behind: %.1f".format(next.flops))
                    // Can't reduce flopsThisRun below zero.
                    assertTrue(next.flopsThisRun >= 0.0, "flopsThisRun went negative: %.1f".format(next.flopsThisRun))
                }
                s = next
            }

            assertTrue(allFieldsFinite(s), "NaN/Inf at second ${second + 120}")
        }

        assertTrue(purgeCount > 1, "purge never triggered — heat never crossed 50")
        assertTrue(totalHeatAfter < totalHeatBefore, "purges had zero net cooling effect")
        // Bot shouldn't be completely broke — taps keep feeding the wallet.
        assertTrue(s.flops >= 0.0, "flops went negative")
    }

    @Test
    fun `level 500 hardware stress — no NaN, overflow, or runaway values`() {
        val s = GameState(
            upgrades = mapOf("refurb_gpu" to 500, "wall_tap" to 100),
            flops = 1e12, // effectively infinite for this test
        )
        val cap = Derived.capacity(s, config)
        val heatGen = Derived.heatGeneration(s, config)
        val powerDraw = Derived.powerDraw(s)
        val powerCap = Derived.powerCapacity(s, config)

        // All values must be finite and positive.
        assertTrue(cap.isFinite() && cap > 0.0, "capacity: $cap")
        assertTrue(heatGen.isFinite() && heatGen > 0.0, "heatGeneration: $heatGen")
        assertTrue(powerDraw.isFinite(), "powerDraw: $powerDraw")
        assertTrue(powerCap.isFinite(), "powerCapacity: $powerCap")

        // Tick the sim — heat should settle, not explode.
        var current = s
        for (step in 0 until 600) {
            current = Simulation.tick(current, 0.1, config).state
            assertTrue(allFieldsFinite(current), "NaN/Inf at tick $step")
            assertTrue(current.heat in 0.0..config.maxHeat, "heat out of range: ${current.heat}")
        }

        // The rig should have earned something meaningful.
        assertTrue(current.flops > s.flops, "level 500 rig earned nothing over 60s — dead sim")
    }
}
