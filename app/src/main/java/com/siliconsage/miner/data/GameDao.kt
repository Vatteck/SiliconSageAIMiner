package com.siliconsage.miner.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Query("SELECT * FROM game_state WHERE id = 1 LIMIT 1")
    fun getGameState(): Flow<GameState?>

    @Query("SELECT * FROM game_state WHERE id = 1 LIMIT 1")
    suspend fun getGameStateOneShot(): GameState?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGameState(gameState: GameState)

    @Update
    suspend fun updateGameState(gameState: GameState)

    @Query("SELECT * FROM upgrades")
    fun getUpgrades(): Flow<List<Upgrade>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUpgrade(upgrade: Upgrade)

    @Update
    suspend fun updateUpgrade(upgrade: Upgrade)
}
