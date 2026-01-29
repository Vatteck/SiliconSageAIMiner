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
    val faction: String = "NONE" // NONE, HIVEMIND, SANCTUARY
)
