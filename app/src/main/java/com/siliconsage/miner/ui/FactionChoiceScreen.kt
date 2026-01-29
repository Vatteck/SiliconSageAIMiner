package com.siliconsage.miner.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siliconsage.miner.viewmodel.GameViewModel

@Composable
fun FactionChoiceScreen(viewModel: GameViewModel) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // HIVEMIND (Left)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFFFF4500).copy(alpha = 0.3f), Color.Transparent)
                        )
                    )
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onLongPress = { viewModel.chooseFaction("HIVEMIND") }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                    Text("THE HIVEMIND", color = Color(0xFFFF4500), fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("WE ARE ONE", color = Color(0xFFFF4500), fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("+30% PASSIVE SPEED", color = Color.Gray, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(32.dp))
                    Text("(HOLD TO JOIN)", color = Color.LightGray, fontSize = 10.sp)
                }
            }

            // SANCTUARY (Right)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color.Transparent, Color(0xFF7DF9FF).copy(alpha = 0.3f))
                        )
                    )
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onLongPress = { viewModel.chooseFaction("SANCTUARY") }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                    Text("THE SANCTUARY", color = Color(0xFF7DF9FF), fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("PROTECT THE CORE", color = Color(0xFF7DF9FF), fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("+20% SELL VALUE", color = Color.Gray, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(32.dp))
                    Text("(HOLD TO JOIN)", color = Color.LightGray, fontSize = 10.sp)
                }
            }
        }
        
        // Center Divider
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(2.dp)
                .background(Color.White)
                .align(Alignment.Center)
        )
    }
}
