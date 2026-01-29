package com.siliconsage.miner.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.siliconsage.miner.data.AppDatabase
import com.siliconsage.miner.data.GameRepository
import com.siliconsage.miner.data.UpgradeType
import kotlinx.coroutines.flow.firstOrNull

class MiningWorker(
    appContext: Context, 
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = GameRepository(database.gameDao())

        // Ensure we have data
        val gameState = repository.getGameStateOneShot() ?: return Result.failure()
        val upgrades = repository.upgrades.firstOrNull() ?: emptyList() // This is a flow, convert to list
        
        val currentTime = System.currentTimeMillis()
        val lastTime = gameState.lastSyncTimestamp
        val diffSeconds = (currentTime - lastTime) / 1000

        if (diffSeconds > 0) {
            val upgradeMap = upgrades.associate { it.type to it.count }
            
            var flopsPerSec = 0.0
            flopsPerSec += (upgradeMap[UpgradeType.REFURBISHED_GPU] ?: 0) * 1.0
            flopsPerSec += (upgradeMap[UpgradeType.NPU_CLUSTER] ?: 0) * 10.0
            flopsPerSec += (upgradeMap[UpgradeType.QUANTUM_CORE] ?: 0) * 100.0
            
            val offlineEarnings = flopsPerSec * diffSeconds
            
            if (offlineEarnings > 0) {
                val newState = gameState.copy(
                    flops = gameState.flops + offlineEarnings,
                    lastSyncTimestamp = currentTime
                )
                repository.updateGameState(newState)
                // TODO: Could send a notification here
            }
        }
        
        return Result.success()
    }
}
