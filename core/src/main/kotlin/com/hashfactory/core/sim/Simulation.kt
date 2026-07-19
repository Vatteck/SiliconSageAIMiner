package com.hashfactory.core.sim

import com.hashfactory.core.config.GameConfig
import com.hashfactory.core.model.GameState
import kotlin.math.floor
import kotlin.math.min

sealed interface GameEvent {
    data class PacketsCompleted(val count: Long, val payout: Double) : GameEvent
    data object ThrottleStarted : GameEvent
    data object ThrottleEnded : GameEvent
}

data class TickResult(val state: GameState, val events: List<GameEvent>)

data class OfflineResult(
    val state: GameState,
    val flopsEarned: Double,
    val secondsSimulated: Double,
    val wasCapped: Boolean,
)

/**
 * The single deterministic simulation step. Pure function of (state, dt, config):
 * no clocks, no randomness, no I/O. The ONLY place $FLOPS enter the wallet
 * (CLAUDE.md rule 1).
 */
object Simulation {

    fun tick(state: GameState, dtSeconds: Double, config: GameConfig = GameConfig.DEFAULT): TickResult {
        if (dtSeconds <= 0.0 || !dtSeconds.isFinite()) return TickResult(state, emptyList())

        val powerFactor = Derived.powerFactor(state, config)
        val throttle = Derived.heatThrottle(state.heat, config)
        val effCap = Derived.capacity(state, config) * powerFactor * throttle
        val payout = Derived.packetPayout(effCap, config)

        // Assigned-work queue: capacity flows into packet progress; completed
        // packets pay the wallet. Hardware itself never touches `flops`.
        val packetsPerSec = if (effCap > 0.0) effCap / payout else 0.0
        val (worked, completed) = creditWork(state, packetsPerSec * dtSeconds, payout)

        // Heat integrates: generation is throttled with the rig, so the system
        // self-stabilizes at an equilibrium instead of death-spiraling.
        val heatDelta = Derived.heatGeneration(state) * powerFactor * throttle -
            Derived.cooling(state, config)
        val newHeat = (worked.heat + heatDelta * dtSeconds)
            .sane()
            .coerceIn(0.0, config.maxHeat)

        val next = worked.copy(heat = newHeat)

        val events = buildList {
            if (completed > 0) add(GameEvent.PacketsCompleted(completed, payout))
            val wasThrottled = state.heat > config.throttleStartHeat
            val isThrottled = newHeat > config.throttleStartHeat
            if (!wasThrottled && isThrottled) add(GameEvent.ThrottleStarted)
            if (wasThrottled && !isThrottled) add(GameEvent.ThrottleEnded)
        }
        return TickResult(next, events)
    }

    /**
     * Offline progress = the same tick replayed in fixed chunks. A closed-form
     * shortcut would be a second economy implementation that drifts from the real
     * one; replay guarantees offline == online by construction (heat equilibrium
     * included).
     */
    fun simulateOffline(
        state: GameState,
        elapsedSeconds: Double,
        config: GameConfig = GameConfig.DEFAULT,
    ): OfflineResult {
        val elapsed = if (elapsedSeconds.isFinite()) elapsedSeconds.coerceAtLeast(0.0) else 0.0
        val capped = elapsed > config.offlineCapSeconds
        val total = min(elapsed, config.offlineCapSeconds)
        var current = state
        var remaining = total
        while (remaining > 0.0) {
            val step = min(config.offlineStepSeconds, remaining)
            current = tick(current, step, config).state
            remaining -= step
        }
        return OfflineResult(
            state = current,
            flopsEarned = current.flops - state.flops,
            secondsSimulated = total,
            wasCapped = capped,
        )
    }

    /**
     * Advance the packet queue by [packets] (fractional), crediting completed
     * packets at [payout] $FLOPS each. Shared by the tick and manual compute.
     */
    internal fun creditWork(state: GameState, packets: Double, payout: Double): Pair<GameState, Long> {
        val progress = (state.packetProgress + packets).sane()
        val completed = floor(progress).toLong().coerceAtLeast(0L)
        if (completed <= 0L) return state.copy(packetProgress = progress.coerceAtLeast(0.0)) to 0L
        val earned = (completed * payout).sane()
        return state.copy(
            packetProgress = progress - completed,
            packetsCompleted = state.packetsCompleted + completed,
            flops = (state.flops + earned).sane(),
            flopsThisRun = (state.flopsThisRun + earned).sane(),
            lifetimeFlops = (state.lifetimeFlops + earned).sane(),
        ) to completed
    }
}

internal fun Double.sane(fallback: Double = 0.0): Double = if (isFinite()) this else fallback
