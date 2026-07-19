package com.hashfactory.core.economy

import com.hashfactory.core.config.GameConfig
import com.hashfactory.core.config.UpgradeDef
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.pow

object Economy {

    /** Cost of the next single level when [owned] are already owned: base * r^owned. */
    fun singleCost(def: UpgradeDef, owned: Int): Double =
        def.baseCost * def.costGrowth.pow(owned)

    /** Closed-form cost of buying [count] levels starting from [owned]: geometric series. */
    fun bulkCost(def: UpgradeDef, owned: Int, count: Int): Double {
        if (count <= 0) return 0.0
        val r = def.costGrowth
        val first = singleCost(def, owned)
        return first * (r.pow(count) - 1.0) / (r - 1.0)
    }

    /** Largest count purchasable with [wallet], starting from [owned]. Exact. */
    fun maxAffordable(def: UpgradeDef, owned: Int, wallet: Double): Int {
        val first = singleCost(def, owned)
        if (wallet < first) return 0
        val r = def.costGrowth
        var k = floor(ln(wallet * (r - 1.0) / first + 1.0) / ln(r)).toInt()
        // Guard against floating-point edge error in the closed form.
        while (k > 0 && bulkCost(def, owned, k) > wallet) k--
        while (bulkCost(def, owned, k + 1) <= wallet) k++
        return k
    }

    /**
     * Legacy golden milestone formula: 2^(level / 25) with INTEGER division —
     * multiplier steps at 25/50/75… (docs/design/production-loop-golden-values.md).
     */
    fun milestoneMultiplier(level: Int): Double = 2.0.pow(level / 25)

    /** Per-level output including milestone: level * base * 2^(level/25). Golden formula. */
    fun hardwareOutput(baseValue: Double, level: Int): Double =
        level * baseValue * milestoneMultiplier(level)

    /** Persistence gained by Burning now: 100 * log10(flopsThisRun / 1000), floored at 0. */
    fun prestigeGain(flopsThisRun: Double): Double =
        if (flopsThisRun <= 0.0) 0.0 else max(0.0, 100.0 * log10(flopsThisRun / 1000.0))

    /** Global capacity multiplier from banked Persistence. */
    fun prestigeMultiplier(persistence: Double, config: GameConfig): Double =
        1.0 + max(0.0, persistence) * config.persistenceBonusPerPoint
}
