package com.siliconsage.miner.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.siliconsage.miner.BuildConfig
import com.siliconsage.miner.util.UpdateManager
import com.siliconsage.miner.util.UpdateNotificationManager
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class UpdateWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return suspendCancellableCoroutine { continuation ->
            // Use UpdateManager to check for updates
            UpdateManager.checkUpdate(BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE) { info, success ->
                if (info != null) {
                    // Update found!
                    // Check rate limiting
                    if (UpdateNotificationManager.shouldShowNotification(applicationContext)) {
                        // Show notification
                        UpdateNotificationManager.showUpdateNotification(
                            applicationContext,
                            info.version,
                            info.url
                        )
                        // Mark as shown
                        UpdateNotificationManager.markNotificationShown(applicationContext)
                    }
                }
                
                // Always return success so it keeps rescheduling (if periodic)
                continuation.resume(Result.success())
            }
        }
    }
}
