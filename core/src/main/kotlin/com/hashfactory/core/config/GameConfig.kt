package com.hashfactory.core.config

/**
 * Every tunable number in the simulation that isn't a per-upgrade value.
 * Sim code must reference these, never inline literals (CLAUDE.md rule 6).
 */
data class GameConfig(
    /** Free power budget before any power upgrades (lets the first GPUs run). */
    val basePowerCapacity: Double = 10.0,
    /** Passive heat dissipation with no cooling upgrades, heat/s. */
    val baseHeatDissipation: Double = 1.0,
    /** Heat level where thermal throttling begins. */
    val throttleStartHeat: Double = 80.0,
    val maxHeat: Double = 100.0,
    /** Capacity multiplier floor when heat is pinned at max. */
    val minThrottle: Double = 0.1,
    /** Packet payout = effectiveCapacity / this (golden legacy behavior: ~10 packets/s). */
    val packetPayoutDivisor: Double = 10.0,
    val minPacketPayout: Double = 1.0,
    /** One tap injects at least this much flops-worth of packet work. */
    val manualBaseWork: Double = 1.0,
    /** One tap also scales with capacity: this fraction of a second of full output. */
    val manualCapacityFraction: Double = 0.25,
    val offlineCapSeconds: Double = 8.0 * 3600.0,
    /** Chunk size for offline replay, seconds. */
    val offlineStepSeconds: Double = 1.0,
    /** Capacity multiplier per Persistence point: 1 + persistence * this. */
    val persistenceBonusPerPoint: Double = 0.01,
    /** Burn is allowed once prestigeGain(flopsThisRun) reaches this (300 = 1e6 flops earned). */
    val minBurnGain: Double = 300.0,
) {
    companion object {
        val DEFAULT = GameConfig()
    }
}
