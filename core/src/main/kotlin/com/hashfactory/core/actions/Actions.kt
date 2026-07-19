package com.hashfactory.core.actions

import com.hashfactory.core.config.GameConfig
import com.hashfactory.core.config.UpgradeDefs
import com.hashfactory.core.economy.Economy
import com.hashfactory.core.model.GameState
import com.hashfactory.core.sim.Derived
import com.hashfactory.core.sim.GameEvent
import com.hashfactory.core.sim.Simulation
import com.hashfactory.core.sim.TickResult
import kotlin.math.max
import kotlin.math.min

data class PurchaseResult(val state: GameState, val bought: Int, val spent: Double)

/** All player-initiated state transitions. Pure; the ViewModel just dispatches here. */
object Actions {

    /**
     * One tap = a fixed injection of packet work: at least manualBaseWork flops-worth,
     * scaling with capacity so taps stay relevant past the first minutes (spine doc:
     * manual compute is a stable accelerator, not tutorial sludge).
     */
    fun manualCompute(state: GameState, config: GameConfig = GameConfig.DEFAULT): TickResult {
        val effCap = Derived.effectiveCapacity(state, config)
        val payout = Derived.packetPayout(effCap, config)
        val work = max(config.manualBaseWork, effCap * config.manualCapacityFraction)
        val (next, completed) = Simulation.creditWork(state, work / payout, payout)
        val events = if (completed > 0) listOf(GameEvent.PacketsCompleted(completed, payout)) else emptyList()
        return TickResult(next, events)
    }

    /**
     * Buy up to [requestedCount] levels (Int.MAX_VALUE = buy max affordable).
     * Partial fills are allowed; cost comes from the closed-form geometric series.
     */
    fun buyUpgrade(
        state: GameState,
        upgradeId: String,
        requestedCount: Int,
        config: GameConfig = GameConfig.DEFAULT,
    ): PurchaseResult {
        val def = UpgradeDefs.byId[upgradeId] ?: return PurchaseResult(state, 0, 0.0)
        if (requestedCount <= 0) return PurchaseResult(state, 0, 0.0)
        val owned = state.level(upgradeId)
        val count = min(requestedCount, Economy.maxAffordable(def, owned, state.flops))
        if (count <= 0) return PurchaseResult(state, 0, 0.0)
        val cost = Economy.bulkCost(def, owned, count)
        val next = state.copy(
            flops = (state.flops - cost).coerceAtLeast(0.0),
            upgrades = state.upgrades + (upgradeId to owned + count),
        )
        return PurchaseResult(next, count, cost)
    }

    /** Overclock: more output for more heat. A pure toggle — the multipliers live in config. */
    fun toggleOverclock(state: GameState): GameState =
        state.copy(overclocked = !state.overclocked)

    /**
     * Emergency heat dump: consumes ALL current $FLOPS for an instant heat reduction.
     * Reduction = flops * config.purgeHeatEfficiency. No-op when flops == 0.
     * Returns the resulting state and how much heat was actually reduced.
     */
    fun purgeHeat(state: GameState, config: GameConfig = GameConfig.DEFAULT): Pair<GameState, Double> {
        if (state.flops <= 0.0 || state.heat <= 0.0) return state to 0.0
        val reduction = (state.flops * config.purgeHeatEfficiency).coerceAtMost(state.heat)
        return state.copy(
            flops = 0.0,
            flopsThisRun = (state.flopsThisRun - state.flops).coerceAtLeast(0.0),
            heat = state.heat - reduction,
        ) to reduction
    }

    /**
     * The Burn: prestige reset. Wipes the run (wallet, packets, heat, upgrades),
     * banks Persistence, keeps lifetime stats. No-op unless the gain gate is met.
     */
    fun burn(state: GameState, config: GameConfig = GameConfig.DEFAULT): GameState {
        val gain = Economy.prestigeGain(state.flopsThisRun)
        if (gain < config.minBurnGain) return state
        return state.copy(
            flops = 0.0,
            flopsThisRun = 0.0,
            packetProgress = 0.0,
            packetsCompleted = 0,
            heat = 0.0,
            overclocked = false,
            upgrades = emptyMap(),
            persistence = state.persistence + gain,
            burnCount = state.burnCount + 1,
        )
    }
}
