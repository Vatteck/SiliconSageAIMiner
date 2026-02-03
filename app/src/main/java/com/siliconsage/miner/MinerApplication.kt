package com.siliconsage.miner

import android.app.Application
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.siliconsage.miner.data.AppDatabase
import com.siliconsage.miner.data.GameRepository
import com.siliconsage.miner.worker.MiningWorker

class MinerApplication : Application() {
    val database by lazy { 
        try {
            AppDatabase.getDatabase(this)
        } catch (e: Exception) {
            // Nuclear reset on any DB initialization failure
            android.util.Log.e("MinerApp", "DB init failed, wiping: ${e.message}")
            deleteDatabase("silicon_sage_db")
            AppDatabase.getDatabase(this)
        }
    }
    val repository by lazy { GameRepository(database.gameDao()) }

    override fun onCreate() {
        super.onCreate()
        
        // v2.8.0: Global exception handler for graceful crashes
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            android.util.Log.e("MinerApp", "Uncaught exception: ${throwable.message}", throwable)
            // Could add crash recovery logic here
            defaultHandler?.uncaughtException(thread, throwable)
        }
        
        try {
            // Trigger offline calculation worker
            val workRequest = OneTimeWorkRequestBuilder<MiningWorker>().build()
            WorkManager.getInstance(this).enqueue(workRequest)
            
            // Schedule Background Update Checks (Pre-release: Every 1 hour)
            val updateWork = androidx.work.PeriodicWorkRequestBuilder<com.siliconsage.miner.worker.UpdateWorker>(
                1, java.util.concurrent.TimeUnit.HOURS
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
        } catch (e: Exception) {
            android.util.Log.e("MinerApp", "Worker init failed: ${e.message}")
        }
    }
}
