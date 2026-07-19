package com.hashfactory.core.sim

import com.hashfactory.core.config.GameConfig
import com.hashfactory.core.config.UpgradeDefs
import com.hashfactory.core.economy.Economy
import com.hashfactory.core.model.GameState
import kotlin.math.max

/**
 * Pure read-only projections of GameState. Everything the UI shows and the tick
 * consumes is derived here from the upgrade table — no cached rate fields in state.
 */
object Derived {

    /** Total compute capacity in flops/s, including milestone and prestige multipliers. */
    fun capacity(state: GameState, config: GameConfig): Double {
        var sum = 0.0
        for (def in UpgradeDefs.ALL) {
            if (def.computeCapacity <= 0.0) continue
            sum += Economy.hardwareOutput(def.computeCapacity, state.level(def.id))
        }
        return sum * Economy.prestigeMultiplier(state.persistence, config)
    }

    fun powerDraw(state: GameState): Double {
        var sum = 0.0
        for (def in UpgradeDefs.ALL) sum += def.powerDraw * state.level(def.id)
        return sum
    }

    fun powerCapacity(state: GameState, config: GameConfig): Double {
        var sum = config.basePowerCapacity
        for (def in UpgradeDefs.ALL) sum += def.powerCapacity * state.level(def.id)
        return sum
    }

    /**
     * Static power budget factor in (0, 1]: draw within capacity runs at full speed;
     * overdraw scales the whole rig down proportionally. A derived constraint, not a
     * meter — it does not integrate over time (spine doc: distinct meters).
     */
    fun powerFactor(state: GameState, config: GameConfig): Double {
        val draw = powerDraw(state)
        if (draw <= 0.0) return 1.0
        return (powerCapacity(state, config) / draw).coerceAtMost(1.0).coerceAtLeast(0.0)
    }

    fun heatGeneration(state: GameState): Double {
        var sum = 0.0
        for (def in UpgradeDefs.ALL) sum += def.heatPerSec * state.level(def.id)
        return sum
    }

    fun cooling(state: GameState, config: GameConfig): Double {
        var sum = config.baseHeatDissipation
        for (def in UpgradeDefs.ALL) sum += def.coolingPerSec * state.level(def.id)
        return sum
    }

    /** 1.0 below throttleStartHeat, falling linearly to minThrottle at maxHeat. */
    fun heatThrottle(heat: Double, config: GameConfig): Double {
        if (heat <= config.throttleStartHeat) return 1.0
        val span = config.maxHeat - config.throttleStartHeat
        if (span <= 0.0) return config.minThrottle
        val t = ((heat - config.throttleStartHeat) / span).coerceIn(0.0, 1.0)
        return 1.0 - t * (1.0 - config.minThrottle)
    }

    /** Capacity actually applied to the work queue this instant. */
    fun effectiveCapacity(state: GameState, config: GameConfig): Double =
        capacity(state, config) * powerFactor(state, config) * heatThrottle(state.heat, config)

    /** Golden legacy behavior: payout = capacity / 10 (min 1), so ~10 packets/s. */
    fun packetPayout(effectiveCapacity: Double, config: GameConfig): Double =
        max(config.minPacketPayout, effectiveCapacity / config.packetPayoutDivisor)
}
