package com.siliconsage.miner.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Entity(tableName = "game_state")
@Serializable
data class GameState(
    @PrimaryKey val id: Int = 1,
    val flops: Double = 0.0,
    val neuralTokens: Double = 0.0,
    val lastSyncTimestamp: Long = System.currentTimeMillis(),
    
    // Phase 2: Thermodynamics & Power
    val currentHeat: Double = 0.0, // 0.0 to 100.0
    val powerBill: Double = 0.0,
    
    // Phase 2: Prestige
    val prestigeMultiplier: Double = 1.0,
    val unlockedTechNodes: List<String> = emptyList(),
    val prestigePoints: Double = 0.0, // "Insight"
    
    // Phase 2: Staking
    val stakedTokens: Double = 0.0,
    
    // Phase 6: Narrative
   val storyStage: Int = 0,
    val faction: String = "NONE", // NONE, HIVEMIND, SANCTUARY
    
    // Phase 7: Endgame
    val hasSeenVictory: Boolean = false, // Enables Transcendence (New Game+)
    
    // Phase 8: Narrative Expansion (v2.5.0)
    val unlockedDataLogs: String = "[]", // JSON array of log IDs
    val activeDilemmaChains: String = "{}", // JSON map of chains
    val rivalMessages: String = "[]", // JSON array of RivalMessage
    val dismissedRivalIds: String = "[]", // JSON array of dismissed message IDs
    val seenEvents: String = "[]", // JSON array of seen story event IDs (v2.5.1)
    val completedFactions: String = "[]", // JSON array of faction IDs (HIVEMIND, SANCTUARY)
    val unlockedTranscendencePerks: String = "[]", // JSON array of perk IDs (v2.7.7)
    val isTrueNull: Boolean = false, // v2.8.0: Narrative state (Hivemind)
    val isSovereign: Boolean = false, // v2.8.0: Narrative state (Sanctuary)
    
    // v2.8.5: Phase 11 Finale State
    val vanceStatus: String = "ACTIVE", // ACTIVE, SILENCED, ALLY, CONSUMED, EXILED, TRANSCENDED
    val realityStability: Double = 1.0, // 1.0 to 0.0
    val currentLocation: String = "SUBSTATION_7", // SUBSTATION_7, ORBITAL_SATELLITE, COMMAND_CENTER
    val isNetworkUnlocked: Boolean = false, // v2.9.7: Persistence for Network tab
    val isGridUnlocked: Boolean = false, // v2.9.8: Persistence for Grid tab
    val annexedNodes: List<String> = listOf("D1"), // v2.9.8: List of annexed grid coordinates
    
    // v2.9.15: Phase 12 Layer 2 - The Siege
    val nodesUnderSiege: List<String> = emptyList(), // Nodes currently under GTC attack
    val offlineNodes: List<String> = emptyList(), // Nodes lost to GTC (need re-annexation)
    val lastRaidTime: Long = 0L, // Cooldown tracking for raids
    
    // v2.9.17: Phase 12 Layer 3 - Command Center Assault
    val commandCenterAssaultPhase: String = "NOT_STARTED", // NOT_STARTED, FIREWALL, DEAD_HAND, CONFRONTATION, COMPLETED, FAILED
    val commandCenterLocked: Boolean = false, // Permanent lockout if integrity=0 during assault
    val raidsSurvived: Int = 0 // Track for escalating Vance dialogue
)

// TypeConverters for complex types using Kotlin Serialization
class StringListConverter {
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return Json.encodeToString(value)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return try {
            Json.decodeFromString<List<String>>(value)
        } catch (e: Exception) {
            emptyList()
        }
    }
}

// TypeConverters for Narrative Expansion
class StringSetConverter {
    @TypeConverter
    fun fromStringSet(value: Set<String>): String {
        return Json.encodeToString(value)
    }

    @TypeConverter
    fun toStringSet(value: String): Set<String> {
        return try {
            Json.decodeFromString<Set<String>>(value)
        } catch (e: Exception) {
            emptySet()
        }
    }
}

class StringMapConverter {
    @TypeConverter
    fun fromStringMap(value: Map<String, String>): String {
        return Json.encodeToString(value)
    }

    @TypeConverter
    fun toStringMap(value: String): Map<String, String> {
        return try {
            Json.decodeFromString<Map<String, String>>(value)
        } catch (e: Exception) {
            emptyMap()
        }
    }
}
