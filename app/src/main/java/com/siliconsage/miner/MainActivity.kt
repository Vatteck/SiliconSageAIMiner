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
        viewModel.loadTechTree(this)
        viewModel.checkForUpdates()
        
        // Init Engines
        com.siliconsage.miner.util.HapticManager.init(this)
        com.siliconsage.miner.util.SoundManager.init(this)
        com.siliconsage.miner.util.HeadlineManager.init(this)

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
        viewModel.onAppBackgrounded()
    }
    
    override fun onResume() {
        super.onResume()
        com.siliconsage.miner.util.SoundManager.resumeAll()
        viewModel.onAppForegrounded(this)
    }
}
