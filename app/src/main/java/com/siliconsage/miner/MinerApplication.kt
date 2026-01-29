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
    }
}
