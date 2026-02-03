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
        // Check permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Permission not granted, fall back to silent behavior
                return
            }
        }
        
        // Create intent to open GitHub release page
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(releaseUrl)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        // Build notification
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher) // Use app icon
            .setContentTitle("Silicon Sage Update Available")
            .setContentText("Version $newVersion is ready to download")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Version $newVersion is now available on GitHub. Tap to view release notes and download.")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true) // Dismiss on tap
            .setVibrate(longArrayOf(0, 250, 250, 250))
            .build()
        
        // Show notification
        with(NotificationManagerCompat.from(context)) {
            notify(NOTIFICATION_ID, notification)
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
