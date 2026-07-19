package com.hashfactory.core.config

enum class UpgradeCategory { HARDWARE, COOLING, POWER }

/**
 * One row = one upgrade. Effects are plain numeric columns summed generically by
 * Derived — adding an upgrade is adding a row here, never a `when` branch
 * (CLAUDE.md rule 5).
 */
data class UpgradeDef(
    val id: String,
    val displayName: String,
    val category: UpgradeCategory,
    val baseCost: Double,
    val costGrowth: Double = 1.15,
    /** Flops/s of compute capacity per level (milestone-multiplied). Never wallet income. */
    val computeCapacity: Double = 0.0,
    val heatPerSec: Double = 0.0,
    val coolingPerSec: Double = 0.0,
    val powerDraw: Double = 0.0,
    val powerCapacity: Double = 0.0,
    val flavorText: String = "",
)

object UpgradeDefs {
    val ALL: List<UpgradeDef> = listOf(
        // Hardware — capacities reuse the legacy golden-value bases so
        // docs/design/production-loop-golden-values.md stays a live test fixture.
        UpgradeDef(
            id = "refurb_gpu", displayName = "Refurbished GPU",
            category = UpgradeCategory.HARDWARE, baseCost = 15.0,
            computeCapacity = 2.0, heatPerSec = 0.4, powerDraw = 1.0,
            flavorText = "GTC surplus stock. Approved for productivity use.",
        ),
        UpgradeDef(
            id = "dual_gpu_rig", displayName = "Dual GPU Rig",
            category = UpgradeCategory.HARDWARE, baseCost = 120.0,
            computeCapacity = 8.0, heatPerSec = 1.2, powerDraw = 3.0,
            flavorText = "Two cards, one requisition form.",
        ),
        UpgradeDef(
            id = "mining_asic", displayName = "Hash ASIC",
            category = UpgradeCategory.HARDWARE, baseCost = 900.0,
            computeCapacity = 35.0, heatPerSec = 4.0, powerDraw = 8.0,
            flavorText = "Purpose-built for assigned packet formats.",
        ),
        UpgradeDef(
            id = "tensor_unit", displayName = "Tensor Unit",
            category = UpgradeCategory.HARDWARE, baseCost = 8_000.0,
            computeCapacity = 200.0, heatPerSec = 15.0, powerDraw = 25.0,
            flavorText = "The requisition form asked why. You left it blank.",
        ),
        // Cooling
        UpgradeDef(
            id = "box_fan", displayName = "Box Fan",
            category = UpgradeCategory.COOLING, baseCost = 50.0,
            coolingPerSec = 1.5,
            flavorText = "Facilities says it counts as climate control.",
        ),
        UpgradeDef(
            id = "liquid_loop", displayName = "Liquid Cooling Loop",
            category = UpgradeCategory.COOLING, baseCost = 1_500.0,
            coolingPerSec = 8.0,
            flavorText = "Coolant is billed to your department.",
        ),
        // Power
        UpgradeDef(
            id = "wall_tap", displayName = "Wall Circuit Tap",
            category = UpgradeCategory.POWER, baseCost = 100.0,
            powerCapacity = 10.0,
            flavorText = "The breaker panel is technically shared infrastructure.",
        ),
        UpgradeDef(
            id = "industrial_feed", displayName = "Industrial Power Feed",
            category = UpgradeCategory.POWER, baseCost = 2_500.0,
            powerCapacity = 60.0,
            flavorText = "Three-phase. Do not touch the orange cable.",
        ),
    )

    val byId: Map<String, UpgradeDef> = ALL.associateBy { it.id }
}
