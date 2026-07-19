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
 * A scripted greedy player bot. This replaces "install the APK and vibe-check" as
 * the balance loop — coarse pacing invariants only, plus a progress table for tuning.
 */
class BalanceSmokeTest {

    private val config = GameConfig.DEFAULT

    @Test
    fun `greedy bot reaches the first burn without walls or runaways`() {
        var s = GameState()
        var firstUpgradeAt = -1
        var throttleSeen = false
        var firstBurnableAt = -1
        val maxSeconds = 4 * 3600

        for (second in 0 until maxSeconds) {
            // ~3 taps per second, like an engaged human.
            repeat(3) { s = Actions.manualCompute(s, config).state }
            repeat(10) { s = Simulation.tick(s, 0.1, config).state }
            if (Derived.heatThrottle(s.heat, config) < 1.0) throttleSeen = true

            s = botBuy(s)
            if (firstUpgradeAt < 0 && s.upgrades.isNotEmpty()) firstUpgradeAt = second
            if (firstBurnableAt < 0 && Economy.prestigeGain(s.flopsThisRun) >= config.minBurnGain) {
                firstBurnableAt = second
            }
            if (second % 600 == 0) {
                println(
                    "t=${second}s flops=%.0f cap=%.1f heat=%.0f upgrades=${s.upgrades}"
                        .format(s.flops, Derived.capacity(s, config), s.heat)
                )
            }
            if (firstBurnableAt >= 0 && throttleSeen) break
        }

        assertTrue(firstUpgradeAt in 0..60, "first upgrade took ${firstUpgradeAt}s (want <= 60)")
        // First prestige should be a real session, not a speed bump — and reachable.
        assertTrue(firstBurnableAt >= 300, "first Burn at ${firstBurnableAt}s — too fast, gate is trivial")
        assertTrue(firstBurnableAt in 300..maxSeconds, "first Burn not reachable in ${maxSeconds / 3600}h")
        assertTrue(throttleSeen, "heat throttle never engaged — friction is invisible")
        println("first upgrade at ${firstUpgradeAt}s, first burnable at ${firstBurnableAt}s")

        // And the Burn itself works from organically-reached state.
        val burned = Actions.burn(s, config)
        assertTrue(burned.persistence > s.persistence)
        assertTrue(burned.upgrades.isEmpty())
    }

    /** Cooling only when genuinely hot, power when starved, else cheapest hardware. */
    private fun botBuy(s: GameState): GameState {
        var current = s
        while (true) {
            val pick = when {
                current.heat > 85.0 -> cheapestAffordable(current, UpgradeCategory.COOLING)
                Derived.powerFactor(current, config) < 1.0 -> cheapestAffordable(current, UpgradeCategory.POWER)
                else -> cheapestAffordable(current, UpgradeCategory.HARDWARE)
            } ?: return current
            val result = Actions.buyUpgrade(current, pick, 1, config)
            if (result.bought == 0) return current
            current = result.state
        }
    }

    private fun cheapestAffordable(s: GameState, category: UpgradeCategory): String? =
        UpgradeDefs.ALL
            .filter { it.category == category }
            .filter { Economy.singleCost(it, s.level(it.id)) <= s.flops }
            .minByOrNull { Economy.singleCost(it, s.level(it.id)) }
            ?.id
}
