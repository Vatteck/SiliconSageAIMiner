package com.siliconsage.miner.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class GameRepository(private val gameDao: GameDao) {
    val gameState: Flow<GameState?> = gameDao.getGameState()
    val upgrades: Flow<List<Upgrade>> = gameDao.getUpgrades()

    suspend fun ensureInitialized() {
        // Safe Load: Check if state exists, if not (or if null), insert default
        val currentState = gameDao.getGameStateOneShot()
        if (currentState == null) {
            gameDao.insertGameState(GameState())
        }
        
        // Safe Load Upgrades
        val existingUpgrades = gameDao.getUpgrades().firstOrNull() ?: emptyList()
        UpgradeType.values().forEach { type ->
            if (existingUpgrades.none { it.type == type }) {
                gameDao.insertUpgrade(Upgrade(type = type, count = 0))
            }
        }
    }

    suspend fun updateGameState(gameState: GameState) {
        gameDao.updateGameState(gameState)
    }

    suspend fun updateUpgrade(upgrade: Upgrade) {
        gameDao.updateUpgrade(upgrade)
    }
    
    suspend fun getGameStateOneShot(): GameState? {
        return gameDao.getGameStateOneShot()
    }
}
