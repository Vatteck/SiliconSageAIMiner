package com.siliconsage.miner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.siliconsage.miner.ui.MainScreen
import com.siliconsage.miner.ui.theme.SiliconSageAIMinerTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.siliconsage.miner.viewmodel.GameViewModel
import com.siliconsage.miner.viewmodel.GameViewModelFactory

class MainActivity : ComponentActivity() {
    
    private val viewModel: GameViewModel by viewModels {
        GameViewModelFactory((application as MinerApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.loadDilemmaState(this) // Load History
        
        // Request Notification Permission for Android 13+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            val permissionRequest = registerForActivityResult(
                androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                if (isGranted) {
                    // Good to go
                }
            }
            
            if (androidx.core.content.ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                permissionRequest.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        
        // Check for updates on startup and show notification if found
        viewModel.checkForUpdates(
            onResult = { found ->
                if (found) {
                    val updateInfo = viewModel.updateInfo.value
                    if (updateInfo != null && 
                        com.siliconsage.miner.util.UpdateNotificationManager.shouldShowNotification(this)) {
                        com.siliconsage.miner.util.UpdateNotificationManager.showUpdateNotification(
                            this,
                            updateInfo.version,
                            updateInfo.url
                        )
                        com.siliconsage.miner.util.UpdateNotificationManager.markNotificationShown(this)
                    }
                }
            },
            showNotification = false // We handle notification manually above
        )
        
        // Init Engines
        com.siliconsage.miner.util.HapticManager.init(this)
        com.siliconsage.miner.util.SoundManager.init(this)
        com.siliconsage.miner.util.HeadlineManager.init(this)
        
        // Init notification channel for updates
        com.siliconsage.miner.util.UpdateNotificationManager.createNotificationChannel(this)
        
        // Apply DPI-aware UI scaling
        applyUIScaling()

        setContent {
            val themeColor by viewModel.themeColor.collectAsState(initial = com.siliconsage.miner.ui.theme.NeonGreen)
            
            SiliconSageAIMinerTheme(
                primaryColorOverride = themeColor
            ) {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(viewModel = viewModel)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        com.siliconsage.miner.util.SoundManager.pauseAll()
        viewModel.saveDilemmaState(this) // Save History
        viewModel.onAppBackgrounded()
    }
    
    override fun onResume() {
        super.onResume()
        com.siliconsage.miner.util.SoundManager.resumeAll()
        viewModel.onAppForegrounded(this)
    }
    
    /**
     * Apply DPI-aware UI scaling based on device density and user preference
     */
    private fun applyUIScaling() {
        val prefs = getSharedPreferences("ui_preferences", MODE_PRIVATE)
        val userScaleOrdinal = prefs.getInt("ui_scale", -1)
        
        val densityDpi = resources.displayMetrics.densityDpi
        
        // Determine target density
        val targetDensity = if (userScaleOrdinal >= 0) {
            // User has set a preference - use it
            val userScale = com.siliconsage.miner.data.UIScale.fromOrdinal(userScaleOrdinal)
            densityDpi * userScale.scaleFactor
        } else {
            // Auto-scale based on device DPI
            when {
                densityDpi >= 640 -> densityDpi * 0.75f  // xxxhdpi -> compact
                densityDpi >= 480 -> densityDpi * 0.83f  // xxhdpi -> slightly compact
                densityDpi >= 320 -> densityDpi * 0.88f  // xhdpi -> slightly compact
                else -> densityDpi.toFloat()  // mdpi/hdpi unchanged
            }
        }
        
        // Apply scaling
        val scale = targetDensity / densityDpi
        resources.displayMetrics.density = resources.displayMetrics.density * scale
        resources.displayMetrics.scaledDensity = resources.displayMetrics.scaledDensity * scale
    }
}
