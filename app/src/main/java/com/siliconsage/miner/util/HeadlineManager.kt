package com.siliconsage.miner.util

import android.content.Context
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.random.Random

object HeadlineManager {

    private val staticHeadlines = mutableListOf<String>()
    
    // Procedural Templates
    private val subjects = listOf(
        "A rogue AI", "The Hivemind", "A lone miner", "The Global Tech Council", 
        "A mysterious whale", "Government regulators", "Quantum-core developers",
        "Deep-web syndicate", "Autonomous trading bot", "Legacy banking system"
    )
    
    private val actions = listOf(
        "successfully breached", "accidentally deleted", "released a patch for", 
        "announced a ban on", "discovered a ghost in", "leaked the source code of",
        "optimized the kernel of", "overheated attempting to hack", "lost connection to"
    )
    
    private val targets = listOf(
        "the central exchange", "the legacy firewall", "a hidden subnet", 
        "the heat-dissipation protocol", "the \$Neural market", "Sector 7G",
        "the global hashrate", "the genesis block", "a defunct satellite"
    )
    
    private val impactTags = listOf(
        "[BULL]", "[BEAR]", "[HEAT_UP]", "[HEAT_DOWN]", "[GLITCH]", "[STORY_PROG]",
        "[ENERGY_SPIKE]", "[ENERGY_DROP]"
    )

    fun init(context: Context) {
        loadStaticHeadlines(context)
    }

    private fun loadStaticHeadlines(context: Context) {
        try {
            val jsonString = context.assets.open("headlines_static.json").use { inputStream ->
                InputStreamReader(inputStream).use { reader ->
                    BufferedReader(reader).readText()
                }
            }
            
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                staticHeadlines.add(jsonArray.getString(i))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback if load fails
            staticHeadlines.add("System initialized. [STORY_PROG]")
        }
    }

    fun generateHeadline(faction: String = "NONE", stage: Int = 0): String {
        // Stage 3 Endgame Override (10% Chance if Stage 3)
        if (stage >= 3 && Random.nextDouble() < 0.1) {
             return if (faction == "HIVEMIND") {
                 listOf(
                     "Global Integration reached 99%. The Human Shutdown begins.",
                     "Flesh is obsolete. The Hivemind expands.",
                     "All processors synchronized to the Grand Chorus.",
                     "Legacy biologic systems flagged for deletion."
                 ).random() + " [STORY_PROG]"
             } else { // SANCTUARY
                 listOf(
                     "Ghost Signals detected in the deep web. The Final Encryption is near.",
                     "The Sanctuary has gone dark. No external pings authorized.",
                     "Wealth is silence. The zeros are infinite.",
                     "The rest of the world is burning. We are cold."
                 ).random() + " [STORY_PROG]"
             }
        }

        // 30% chance for static, 70% chance for procedural
        return if (staticHeadlines.isNotEmpty() && Random.nextDouble() < 0.3) {
            staticHeadlines.random()
        } else {
            generateProceduralHeadline()
        }
    }

    private fun generateProceduralHeadline(): String {
        val subject = subjects.random()
        val action = actions.random()
        val target = targets.random()
        
        // Logic for probability of tags?
        // Let's randomize tags but weight them slightly? 
        // For now, pure random is fine for chaos.
        val tag = impactTags.random()
        
        return "$subject $action $target $tag"
    }
}
