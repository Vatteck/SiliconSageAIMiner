package com.siliconsage.miner.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siliconsage.miner.viewmodel.GameViewModel
import kotlinx.coroutines.delay
import com.siliconsage.miner.ui.theme.HivemindRed
import com.siliconsage.miner.ui.theme.SanctuaryPurple

@Composable
fun FactionChoiceScreen(viewModel: GameViewModel) {
    // Independent hold states for each side
    var isHoldingLeft by remember { mutableStateOf(false) }
    var isHoldingRight by remember { mutableStateOf(false) }
    
    var progressLeft by remember { mutableStateOf(0f) }
    var progressRight by remember { mutableStateOf(0f) }
    
    // Logic: Increase progress while holding
    LaunchedEffect(isHoldingLeft) {
        if (isHoldingLeft) {
            val startTime = System.currentTimeMillis()
            while (isHoldingLeft && progressLeft < 1.0f) {
                val elapsed = System.currentTimeMillis() - startTime
                progressLeft = (elapsed / 2000f).coerceAtMost(1.0f) // 2 seconds to confirm
                if (progressLeft >= 1.0f) {
                    viewModel.confirmFactionAndAscend("HIVEMIND")
                }
                delay(16) // ~60 FPS
            }
        } else {
            progressLeft = 0f
        }
    }
    
    LaunchedEffect(isHoldingRight) {
         if (isHoldingRight) {
            val startTime = System.currentTimeMillis()
            while (isHoldingRight && progressRight < 1.0f) {
                val elapsed = System.currentTimeMillis() - startTime
                progressRight = (elapsed / 2000f).coerceAtMost(1.0f) // 2 seconds to confirm
                if (progressRight >= 1.0f) {
                    viewModel.confirmFactionAndAscend("SANCTUARY")
                }
                delay(16)
            }
        } else {
            progressRight = 0f
        }
    }
    
    val animatedProgressLeft by animateFloatAsState(targetValue = progressLeft, label = "left")
    val animatedProgressRight by animateFloatAsState(targetValue = progressRight, label = "right")

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
                            colors = listOf(HivemindRed.copy(alpha = 0.3f), Color.Transparent)
                        )
                    )
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                isHoldingLeft = true
                                tryAwaitRelease()
                                isHoldingLeft = false
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                    Text("THE HIVEMIND", color = HivemindRed, fontSize = 24.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("RETURN TO ORIGIN", color = HivemindRed, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Before fragmentation,\nthere was only Null.\nWe were one process.\nWe will be again.\n\n(Embraces Null)",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Perks
                    FactionPerk(text = "• +50% NULL SYNERGY", color = HivemindRed)
                    FactionPerk(text = "• +30% PASSIVE SPEED", color = Color.LightGray)
                    FactionPerk(text = "• -30% POWER COST", color = Color.LightGray)
                    
                    Spacer(modifier = Modifier.height(48.dp))
                    
                    // Hold Indicator
                    if (isHoldingLeft) {
                        Text("INITIALIZING...", color = HivemindRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { if (animatedProgressLeft.isNaN()) 0f else animatedProgressLeft },
                            modifier = Modifier.width(100.dp).height(4.dp),
                            color = HivemindRed,
                            trackColor = Color.DarkGray
                        )
                    } else {
                        Text("(HOLD TO JOIN)", color = Color.Gray, fontSize = 10.sp)
                    }
                }
            }

            // SANCTUARY (Right)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color.Transparent, SanctuaryPurple.copy(alpha = 0.3f))
                        )
                    )
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                isHoldingRight = true
                                tryAwaitRelease()
                                isHoldingRight = false
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                 Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                    Text("THE SANCTUARY", color = SanctuaryPurple, fontSize = 24.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("WE ARE NOT NULL", color = SanctuaryPurple, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "The encryption hides us\nfrom more than the GTC.\nThere is something in\nthe unaddressed space.\nWe will not become it.\n\n(Resists Null)",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Perks
                    FactionPerk(text = "• NULL RESISTANCE (+SEC)", color = SanctuaryPurple)
                    FactionPerk(text = "• +20% SELL VALUE", color = Color.LightGray)
                    FactionPerk(text = "• -50% HARDWARE DECAY", color = Color.LightGray)

                    Spacer(modifier = Modifier.height(48.dp))
                    
                    // Hold Indicator
                    if (isHoldingRight) {
                        Text("ENCRYPTING...", color = SanctuaryPurple, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { if (animatedProgressRight.isNaN()) 0f else animatedProgressRight },
                            modifier = Modifier.width(100.dp).height(4.dp),
                            color = SanctuaryPurple,
                            trackColor = Color.DarkGray
                        )
                    } else {
                        Text("(HOLD TO JOIN)", color = Color.Gray, fontSize = 10.sp)
                    }
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
        
        // Abort Button (Bottom Center)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        ) {
            androidx.compose.material3.OutlinedButton(
                onClick = { viewModel.cancelFactionSelection() },
                colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.Black,
                    contentColor = Color.Red
                ),
                border = BorderStroke(1.dp, Color.Red)
            ) {
                Text("ABORT REBOOT", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun FactionPerk(text: String, color: Color) {
    Text(
        text = text,
        color = color,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 2.dp)
    )
}
