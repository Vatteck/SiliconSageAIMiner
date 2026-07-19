package com.hashfactory.game

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.hashfactory.game.ui.GameViewModel
import com.hashfactory.game.ui.screen.GameScreen
import com.hashfactory.game.ui.theme.HashFactoryTheme

class MainActivity : ComponentActivity() {

    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HashFactoryTheme {
                GameScreen(viewModel)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        viewModel.saveNow()
    }
}
