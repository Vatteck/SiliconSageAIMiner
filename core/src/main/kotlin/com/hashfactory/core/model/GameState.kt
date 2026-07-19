package com.hashfactory.core.model

import kotlinx.serialization.Serializable

/**
 * The entire persistent game state. One immutable aggregate — see CLAUDE.md rule 3.
 * Every field must have a serialization default or a SaveCodec migration step.
 */
@Serializable
data class GameState(
    val schemaVersion: Int = CURRENT_SCHEMA_VERSION,
    /** Spendable wallet ($FLOPS). Only ever credited by packet completion. */
    val flops: Double = 0.0,
    /** Earned since the last Burn — the prestige input. */
    val flopsThisRun: Double = 0.0,
    /** Earned across all runs — stats and future unlock gates. */
    val lifetimeFlops: Double = 0.0,
    /** Fraction [0, 1) of the current assigned packet. */
    val packetProgress: Double = 0.0,
    val packetsCompleted: Long = 0,
    /** Physical stress meter [0, 100]. */
    val heat: Double = 0.0,
    /** upgradeId -> owned level. Ids come from UpgradeDefs. */
    val upgrades: Map<String, Int> = emptyMap(),
    /** Overclock toggle: multiplies output and heat generation while on. */
    val overclocked: Boolean = false,
    /** Prestige currency, kept across Burns. */
    val persistence: Double = 0.0,
    val burnCount: Int = 0,
    /** Wall-clock ms of the last save; drives offline progress on next load. */
    val lastSaveEpochMs: Long = 0,
) {
    fun level(upgradeId: String): Int = upgrades[upgradeId] ?: 0

    companion object {
        const val CURRENT_SCHEMA_VERSION = 1
    }
}
