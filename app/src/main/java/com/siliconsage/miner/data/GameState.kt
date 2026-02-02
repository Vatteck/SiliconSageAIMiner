package com.siliconsage.miner.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "game_state")
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
    val seenEvents: String = "[]" // JSON array of seen story event IDs (v2.5.1)
)

// TypeConverters for complex types
class StringListConverter {
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val type = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(value, type) ?: emptyList()
    }
}

// TypeConverters for Narrative Expansion
class StringSetConverter {
    @TypeConverter
    fun fromStringSet(value: Set<String>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toStringSet(value: String): Set<String> {
        val type = object : TypeToken<Set<String>>() {}.type
        return Gson().fromJson(value, type) ?: emptySet()
    }
}

class StringMapConverter {
    @TypeConverter
    fun fromStringMap(value: Map<String, String>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toStringMap(value: String): Map<String, String> {
        val type = object : TypeToken<Map<String, String>>() {}.type
        return Gson().fromJson(value, type) ?: emptyMap()
    }
}
