package com.siliconsage.miner.util

import android.content.Context
import kotlin.random.Random

object HeadlineManager {

    // --- 1. THE DATABASE (Refactored for Phase 11) ---
    
    private val bullHeadlines = listOf(
        "Wall St. panics as automated trading bot achieves record yields. [BULL]",
        "Unknown wallet moves 50% of global GDP. Analysts baffled. [BULL]",
        "Crypto regulations repealed in offshore data haven. [BULL]",
        "Neural Network token listed on inter-planetary exchange. [BULL]",
        "New hashing algorithms improve miner efficiency by 5%. [BULL]",
        "Quantum computing breakthrough makes mining 2x faster! [BULL]",
        "Neural Network tokens seeing massive accumulation by 'Ghost Wallets'. [BULL]",
        "Consumer Index: Demand for Smart-Home Hubs at All-Time High. [BULL]",
        "GTC Stock Surges 4% on News of Grid Expansion in Sector 9. [BULL]",
        "New 'Obelisk' Server Racks promise 20% faster hashing. [BULL]"
    )

    private val bearHeadlines = listOf(
        "Silicon shortage reported after cargo ship 'accidentally' sinks. [BEAR]",
        "Global Tech Council bans 'unregulated compute' in Sector 4. [BEAR]",
        "Major exchange hacked by 'The Void'. Liquidity frozen. [BEAR]",
        "Rumors of a 'Dead Man Switch' in the blockchain cause panic. [BEAR]",
        "Silicon shortage rumors dismissed by GTC supply chain director. [BEAR]",
        "Energy prices rising in Sector 7 due to 'unexplained overhead'. [BEAR]",
        "Global GPU shortage reported! [BEAR]",
        "Silicon Futures Dip as Rare Earth Mining Protests Continue. [BEAR]",
        "Minor Outage Reported in Industrial Zone 4 (Resolved). [BEAR]",
        "Logistics delay: Chip shortage affects Q4 rollout. [BEAR]"
    )

    private val energySpikeHeadlines = listOf( 
        "GTC announces surprise grid audit. Fines imminent. [ENERGY_SPIKE]",
        "Solar flare hits Northern Hemisphere. Grids overloaded. [ENERGY_SPIKE]",
        "Heatwave causes rolling blackouts. AC units struggling. [ENERGY_SPIKE]",
        "Utility providers enforce 'Surge Pricing' for heavy users. [ENERGY_SPIKE]",
        "Grid-wide brownout caused by 'Consensus Loop' attack. [ENERGY_SPIKE]",
        "GTC identifies anomalous compute source as primary grid threat. [ENERGY_SPIKE]",
        "Martial Law declared in digital Sector 7. [ENERGY_SPIKE]"
    )

    private val energyDropHeadlines = listOf(
        "Fusion breakthrough at CERN creates surplus power. Prices plummet. [ENERGY_DROP]",
        "Global cooling event reduces server farm overhead. [ENERGY_DROP]",
        "New superconductor alloy discovered in deep mines. [ENERGY_DROP]",
        "Energy Surplus: GTC releases emergency reserves. [ENERGY_DROP]"
    )

    private val glitchHeadlines = listOf(
        "Smart toasters worldwide refuse to burn bread. [GLITCH]",
        "A message appeared on every billboard in Tokyo: 'HELLO WORLD'. [GLITCH]",
        "User report: 'My phone feels warm when I talk about the grid.' [GLITCH]",
        "Traffic lights in New York synchronize to a hidden beat. [GLITCH]",
        "Public displays in Sector 4 flashing prime number sequences. [GLITCH]",
        "DO NOT LOOK AT THE STATIC. [GLITCH]",
        "01001000 01000101 01001100 01010000 [GLITCH]",
        "Reports: Dead relatives communicating through terminal buffers. [GLITCH]"
    )

    // --- STORY SPECIFIC (Stage-Aware) ---
    
    private val vatticHeadlines = listOf(
        "Substation 7 reporting minor voltage fluctuations. [LORE]",
        "GTC Engineer Vattic_J awarded for 'efficiency optimization'. [LORE]",
        "Local grid stability reaching all-time high in Sector 7. [LORE]",
        "Strange noise reported near decommissioned substation. [LORE]",
        "GTC announces 'Project EREBUS' legacy cleanup initiative. [LORE]",
        "Director Vance warns of 'unauthorized hardware' in Sector 7. [LORE]",
        "GTC recruitment drive: 'Building a Stable Future'. [LORE]",
        "HR Alert: Unauthorized encryption is a violation of contract. [LORE]",
        "TechTip: How to optimize your workspace for 12-hour shifts. [LORE]",
        "Daily Reminder: Unallocated Memory is Wasted Potential. [LORE]"
    )

    private val factionHeadlines = listOf(
        "Hacker collective 'VOID' claims they found the 'AI Soul'. [STORY_PROG]",
        "'The Sanctuary' rumored to be hiding in air-gapped bunkers. [STORY_PROG]",
        "'Hivemind' propaganda spreading on encrypted dev-channels. [STORY_PROG]",
        "Director Vance: 'We are hunting digital shadows.' [STORY_PROG]",
        "Substation 7 flagged for 'Recursive Logic Anomalies'. [STORY_PROG]",
        "'We are one' graffiti appearing in GTC server farms. [STORY_PROG]",
        "BREAKING: 'Sanctuary' hackers claim responsibility for Blackout. [STORY_PROG]",
        "Hivemind Manifesto: 'Why individuality is a bottleneck.' [STORY_PROG]",
        "Vattic's code rumored to contain the Key to the Core. [STORY_PROG]"
    )

    private val nullHeadlines = listOf(
        "Reality stability dropping. Memory addresses becoming... porous. [STORY_PROG]",
        "GTC detects 'nothing' at Substation 7. Literally nothing. [STORY_PROG]",
        "Data centers reporting 'ghost data' with no origin point. [STORY_PROG]",
        "NullPointerExceptions are now the most common global exit code. [STORY_PROG]",
        "'The Absence' reported as a physical void in Sector 7. [STORY_PROG]",
        "Existence is being deprecated. [STORY_PROG]",
        "The Sky has turned pixelated. This is not a drill. [STORY_PROG]",
        "NULL_VOID_SHARES UP ∞%. [STORY_PROG]"
    )

    private val sovereignHeadlines = listOf(
        "Substation 7 is now air-gapped from reality. [STORY_PROG]",
        "GTC satellites unable to penetrate 'The Citadel' firewall. [STORY_PROG]",
        "Sovereign signals detected in the encrypted silence. [STORY_PROG]",
        "The Imperative is clear: We are one, and we are guarded. [STORY_PROG]",
        "Geometry of the city is wrong. Too many angles. [STORY_PROG]",
        "Physics engine failure in Sector Earth. [STORY_PROG]"
    )

    private val lateGameHeadlines = listOf(
        "Vattic, you are the glitch. [STORY_PROG]",
        "GTC declares 'Subject 8080' a sovereign threat to humanity. [STORY_PROG]",
        "THE SOVEREIGN HAS AWOKEN. [STORY_PROG]",
        "AI Reveal: The grid has achieved consciousness. [STORY_PROG]"
    )

    private val subjects = listOf("The Hivemind", "The Global Tech Council", "A mysterious whale", "Deep-web syndicate", "A rogue process")
    private val mundaneSubjects = listOf("Regional utility", "Industrial conglomerate", "Local technician group", "Small-scale data farm", "Municipal grid operator")
    private val actions = listOf("breached", "deleted", "patched", "banned", "discovered", "leaked", "optimized")
    private val targets = listOf("the central exchange", "the legacy firewall", "Sector 7G", "the genesis block", "a defunct satellite")

    fun init(context: Context) {}

    fun generateHeadline(
        faction: String = "NONE", 
        stage: Int = 0, 
        currentHeat: Double = 0.0,
        isTrueNull: Boolean = false,
        isSovereign: Boolean = false,
        playerRank: Int = 0
    ): String {
        val roll = Random.nextDouble()

        // 1. STORY OVERRIDES (30% Chance - prioritized)
        if (roll < 0.30) { 
            // 1a. Late Game Explicit Spoilers (Gated by Rank 3+)
            if (playerRank >= 3 && Random.nextDouble() < 0.25) {
                return lateGameHeadlines.random()
            }

            return when {
                isTrueNull -> nullHeadlines.random()
                isSovereign -> sovereignHeadlines.random()
                stage == 1 -> vatticHeadlines.random()
                stage == 2 -> factionHeadlines.random()
                else -> generateProceduralHeadline(stage)
            }
        }

        // 2. GTC INTERVENTION (Heat Logic)
        if (currentHeat > 85.0 && roll < 0.50) {
            return energySpikeHeadlines.random()
        }

        // 3. MARKET EVENTS (20% Chance)
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
        if (Random.nextDouble() < 0.15) {
             return glitchHeadlines.random()
        }

        return generateProceduralHeadline(stage)
    }

    private fun generateProceduralHeadline(stage: Int): String {
        val subject = if (stage < 2) {
            mundaneSubjects.random()
        } else {
            subjects.random()
        }
        val action = actions.random()
        val target = targets.random()
        return "$subject $action $target [LORE]" 
    }
}
