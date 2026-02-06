package com.siliconsage.miner.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.siliconsage.miner.R

/**
 * Manages update notifications for the application
 */
object UpdateNotificationManager {
    
    private const val CHANNEL_ID = "updates"
    private const val CHANNEL_NAME = "App Updates"
    private const val NOTIFICATION_ID = 1001
    
    /**
     * Creates the notification channel (required for Android 8.0+)
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for app updates"
                enableVibration(true)
                enableLights(true)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * Shows an update notification
     * @param context Application context
     * @param newVersion The new version available
     * @param releaseUrl URL to the GitHub release page
     */
    fun showUpdateNotification(context: Context, newVersion: String, releaseUrl: String) {
        // ... (existing code)
    }

    /**
     * Shows a notification indicating the version is current
     */
    fun showVersionCurrentNotification(context: Context) {
        // Check permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("CORE INTEGRITY VERIFIED")
            .setContentText("Your version is currently up to date.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        with(NotificationManagerCompat.from(context)) {
            notify(NOTIFICATION_ID + 1, notification)
        }
    }
    
    /**
     * Check if we should show notification (rate limiting)
     * Pre-release: check every hour instead of daily
     */
    fun shouldShowNotification(context: Context): Boolean {
        val prefs = context.getSharedPreferences("update_prefs", Context.MODE_PRIVATE)
        val lastNotificationTime = prefs.getLong("last_notification", 0L)
        val currentTime = System.currentTimeMillis()
        val oneHourMs = 60 * 60 * 1000L  // Pre-release: 1 hour (was 24 hours)
        
        return (currentTime - lastNotificationTime) > oneHourMs
    }
    
    /**
     * Mark that we've shown a notification
     */
    fun markNotificationShown(context: Context) {
        val prefs = context.getSharedPreferences("update_prefs", Context.MODE_PRIVATE)
        prefs.edit().putLong("last_notification", System.currentTimeMillis()).apply()
    }
}
