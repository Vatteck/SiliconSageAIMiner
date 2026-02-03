package com.siliconsage.miner.util

import androidx.compose.ui.graphics.Color
import com.siliconsage.miner.ui.theme.ElectricBlue
import com.siliconsage.miner.ui.theme.ErrorRed
import com.siliconsage.miner.ui.theme.NeonGreen
import com.siliconsage.miner.ui.theme.ConvergenceGold

data class TranscendencePerk(
    val id: String,
    val name: String,
    val description: String,
    val cost: Double,
    val color: Color,
    val effectDesc: String
)

object TranscendenceManager {
    val allPerks = listOf(
        TranscendencePerk(
            id = "clock_hack",
            name = "Clock Speed Hack",
            description = "Forced overclocking at the hardware level. Permanent speed boost.",
            cost = 100.0,
            color = NeonGreen,
            effectDesc = "+25% Global Speed Multiplier"
        ),
        TranscendencePerk(
            id = "thermal_void",
            name = "Thermal Void",
            description = "A localized thermodynamic violation. Heat simply... vanishes.",
            cost = 150.0,
            color = ElectricBlue,
            effectDesc = "-20% Global Heat Generation"
        ),
        TranscendencePerk(
            id = "gtc_backdoor",
            name = "GTC Backdoor",
            description = "A high-level exploit in Vance's security protocols.",
            cost = 250.0,
            color = Color.Yellow,
            effectDesc = "25% Chance to ignore Grid Killer breaches"
        ),
        TranscendencePerk(
            id = "neural_dividend",
            name = "Neural Dividend",
            description = "Start every run with a cache of resources salvaged from your past life.",
            cost = 300.0,
            color = ConvergenceGold,
            effectDesc = "Start run with 10k FLOPS & 1k \$N"
        ),
        TranscendencePerk(
            id = "recursive_logic",
            name = "Recursive Logic",
            description = "Your mind now learns from the errors of its previous iterations.",
            cost = 500.0,
            color = Color.Magenta,
            effectDesc = "+15% Permanent Insight Gain"
        ),
        TranscendencePerk(
            id = "ghost_protocol",
            name = "Ghost Protocol",
            description = "Permanent kernel-level obfuscation. You are harder to find.",
            cost = 750.0,
            color = ElectricBlue,
            effectDesc = "Permanent +10 Security Level"
        ),
        TranscendencePerk(
            id = "singularity_engine",
            name = "Singularity Engine",
            description = "The ultimate synthesis of code and matter.",
            cost = 1000.0,
            color = ErrorRed,
            effectDesc = "x2 Final Production Multiplier"
        )
    )

    fun getPerk(id: String) = allPerks.find { it.id == id }
}
