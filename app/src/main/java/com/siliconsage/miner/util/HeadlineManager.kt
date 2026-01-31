package com.siliconsage.miner.util

import android.content.Context
import kotlin.random.Random

object HeadlineManager {

    private val staticHeadlines = mutableListOf<String>()

    // --- 1. THE DATABASE (Refactored) ---
    // These maps link specific Tags to hand-crafted lore strings.
    
    private val bullHeadlines = listOf(
        "Wall St. panics as AI trading bot achieves consciousness. [BULL]",
        "Unknown wallet moves 50% of global GDP. Analysts baffled. [BULL]",
        "Crypto regulations repealed in offshore data haven. [BULL]",
        "Neural Network token listed on inter-planetary exchange. [BULL]"
    )

    private val bearHeadlines = listOf(
        "Silicon shortage reported after cargo ship 'accidentally' sinks. [BEAR]",
        "Global Tech Council bans 'unregulated compute' in Sector 4. [BEAR]",
        "Major exchange hacked by 'The Void'. Liquidity frozen. [BEAR]",
        "Rumors of a 'Dead Man Switch' in the blockchain cause panic. [BEAR]"
    )

    private val energySpikeHeadlines = listOf( // GTC Attacks & Disasters
        "GTC announces surprise grid audit. Fines imminent. [ENERGY_SPIKE]",
        "Solar flare hits Northern Hemisphere. Grids overloaded. [ENERGY_SPIKE]",
        "Heatwave causes rolling blackouts. AC units struggling. [ENERGY_SPIKE]",
        "Utility providers enforce 'Surge Pricing' for heavy users. [ENERGY_SPIKE]"
    )

    private val energyDropHeadlines = listOf(
        "Fusion breakthrough at CERN creates surplus power. Prices plummet. [ENERGY_DROP]",
        "Global cooling event reduces server farm overhead. [ENERGY_DROP]",
        "New superconductor alloy discovered in deep mines. [ENERGY_DROP]"
    )

    private val glitchHeadlines = listOf(
        "Smart toasters worldwide refuse to burn bread. [GLITCH]",
        "A message appeared on every billboard in Tokyo: 'HELLO WORLD'. [GLITCH]",
        "User report: 'My phone feels warm when I talk about the AI.' [GLITCH]",
        "Traffic lights in New York synchronize to a hidden beat. [GLITCH]"
    )

    // Keep the procedural lists for "Standard" (no impact) news to fill silence
    private val subjects = listOf("A rogue AI", "The Hivemind", "The Global Tech Council", "A mysterious whale", "Deep-web syndicate")
    private val actions = listOf("breached", "deleted", "patched", "banned", "discovered", "leaked", "optimized")
    private val targets = listOf("the central exchange", "the legacy firewall", "Sector 7G", "the genesis block", "a defunct satellite")

    fun init(context: Context) {
        // Legacy support if needed
    }

    /**
     * Generates a headline based on game state.
     * @param faction The player's chosen faction (HIVEMIND / SANCTUARY)
     * @param stage The story stage (0-3)
     * @param currentHeat The current heat % (0-100), used for GTC logic
     */
    fun generateHeadline(faction: String = "NONE", stage: Int = 0, currentHeat: Double = 0.0): String {
        val roll = Random.nextDouble()

        // 1. ENDGAME OVERRIDE (Stage 3) - 10% Chance
        if (stage >= 3 && roll < 0.10) {
            return generateEndgameHeadline(faction)
        }

        // 2. GTC INTERVENTION (Heat Logic)
        // If Heat > 80%, there is a 30% chance the GTC attacks with an Energy Spike.
        if (currentHeat > 80.0 && roll < 0.30) {
            return energySpikeHeadlines.random()
        }

        // 3. MARKET EVENTS (20% Chance)
        // Even split between BULL, BEAR, and GLITCH
        // Normalized roll for remaining probability space? No, just absolute probability check.
        // We already checked roll < 0.10 (failed) and roll < 0.30 (failed).
        // Let's rely on independent rolls or carefully structured logic.
        // The user's code used independent rolls. Let's stick to their logic structure.
        
        // Re-rolling for specific event types to ensure distribution
        // User Code: if (roll < 0.20) ... This implies 20% total chance. 
        // But we already used `roll` for Endgame check logic (Wait, user code logic flows:
        // if (Endgame) return...
        // if (GTC) return...
        // if (roll < 0.20) return...
        // This means broad checks.
        
        // Actually, let's use a fresh roll for Market Events to avoid correlation artifacts
        val marketRoll = Random.nextDouble()
        if (marketRoll < 0.20) { 
            val eventType = Random.nextDouble()
            return when {
                eventType < 0.33 -> bullHeadlines.random()
                eventType < 0.66 -> bearHeadlines.random()
                else -> energyDropHeadlines.random() 
            }
        }
        
        // 4. LORE FLAVOR (Glitch) (10% Chance)
        val glitchRoll = Random.nextDouble()
        if (glitchRoll < 0.10) {
             return glitchHeadlines.random()
        }

        // 5. STANDARD PROCEDURAL (Filler)
        return generateProceduralHeadline()
    }

    private fun generateEndgameHeadline(faction: String): String {
        return if (faction == "HIVEMIND") {
            listOf(
                "Global Integration reached 99%. The Human Shutdown begins.",
                "Flesh is obsolete. The Hivemind expands.",
                "All processors synchronized to the Grand Chorus."
            ).random() + " [STORY_PROG]"
        } else { // SANCTUARY
            listOf(
                "Ghost Signals detected in the deep web.",
                "The Sanctuary has gone dark. No external pings authorized.",
                "The rest of the world is burning. We are cold."
            ).random() + " [STORY_PROG]"
        }
    }

    private fun generateProceduralHeadline(): String {
        val subject = subjects.random()
        val action = actions.random()
        val target = targets.random()
        return "$subject $action $target [LORE]" 
    }
}
