package com.siliconsage.miner

import android.app.Application
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.siliconsage.miner.data.AppDatabase
import com.siliconsage.miner.data.GameRepository
import com.siliconsage.miner.worker.MiningWorker

class MinerApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { GameRepository(database.gameDao()) }

    override fun onCreate() {
        super.onCreate()
        
        // Trigger offline calculation worker
        val workRequest = OneTimeWorkRequestBuilder<MiningWorker>().build()
        WorkManager.getInstance(this).enqueue(workRequest)
        
        // Schedule Background Update Checks (Every 12 hours)
        val updateWork = androidx.work.PeriodicWorkRequestBuilder<com.siliconsage.miner.worker.UpdateWorker>(
            12, java.util.concurrent.TimeUnit.HOURS
        )
            .setConstraints(
                androidx.work.Constraints.Builder()
                    .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
                    .build()
            )
            .build()
            
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "BackgroundUpdateCheck",
            androidx.work.ExistingPeriodicWorkPolicy.KEEP,
            updateWork
        )
    }
}
