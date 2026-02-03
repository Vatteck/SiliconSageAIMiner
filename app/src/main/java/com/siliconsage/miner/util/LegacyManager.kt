package com.siliconsage.miner.util

import com.siliconsage.miner.data.TechNode

object LegacyManager {
    val legacyTree = listOf(
        // Tier 1: Basics
        TechNode(
            id = "neural_density",
            name = "Neural Density",
            description = "+10% FLOPS Production per level.",
            cost = 1.0,
            multiplier = 0.1,
            requires = emptyList()
        ),
        TechNode(
            id = "cooling_efficiency",
            name = "Cooling Efficiency",
            description = "Cooling/Heat efficiency +10%.",
            cost = 2.0,
            multiplier = 0.0, // Special handling in calc
            requires = listOf("neural_density")
        ),
        
        // Tier 2: Faction Specifics (Common for now)
        TechNode(
            id = "market_predictions",
            name = "Market Predictions",
            description = "+20% Neural Token Value.",
            cost = 5.0,
            multiplier = 0.2, // Value multiplier
            requires = listOf("neural_density")
        ),
        TechNode(
            id = "deep_retention",
            name = "Deep Retention",
            description = "Prestige Points multiplier +5%.",
            cost = 10.0,
            multiplier = 0.05, // Prestige gain buffer
            requires = listOf("neural_density")
        ),

        // Tier 3: Advanced
        TechNode(
            id = "quantum_tunnelling",
            name = "Quantum Tunnelling",
            description = "+50% Global Speed.",
            cost = 25.0,
            multiplier = 0.5,
            requires = listOf("cooling_efficiency", "market_predictions")
        ),
        TechNode(
            id = "digital_immortality",
            name = "Digital Immortality",
            description = "Unlocks: SINGULARITY Protocol.",
            cost = 100.0,
            multiplier = 1.0, 
            requires = listOf("quantum_tunnelling")
        )
    )

    fun getUnlockedMultipliers(unlockedIds: List<String>): Double {
        return legacyTree.filter { unlockedIds.contains(it.id) }.sumOf { it.multiplier }
    }
}
