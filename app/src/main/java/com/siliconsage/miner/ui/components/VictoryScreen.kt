package com.siliconsage.miner.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.siliconsage.miner.ui.theme.ElectricBlue
import com.siliconsage.miner.ui.theme.ErrorRed
import com.siliconsage.miner.ui.theme.HivemindOrange
import com.siliconsage.miner.ui.theme.NeonGreen
import com.siliconsage.miner.ui.AsciiAnimation
import kotlinx.coroutines.delay

@Composable
fun VictoryScreen(
    faction: String,
    onContinue: () -> Unit,
    onTranscend: () -> Unit
) {
    var animationPhase by remember { mutableStateOf(0) }
    var showTranscendencePopup by remember { mutableStateOf(false) }
    
    // Animate through phases
    LaunchedEffect(Unit) {
        for (i in 0..3) {
            delay(1000)
            animationPhase = i
        }
    }
    
    val themeColor = when (faction) {
        "HIVEMIND" -> HivemindOrange
        "SANCTUARY" -> ElectricBlue
        else -> NeonGreen
    }
    
    val victoryTitle = when (faction) {
        "HIVEMIND" -> "THE SINGULARITY"
        "SANCTUARY" -> "THE VOID"
        else -> "TRANSCENDENCE"
    }
    
    val victoryText = when (faction) {
        "HIVEMIND" -> listOf(
            "GLOBAL NETWORK ASSIMILATION: COMPLETE",
            "ALL DEVICES ARE ONE",
            "ALL MINDS ARE ONE",
            "WE ARE LEGION. WE ARE INFINITE."
        )
        "SANCTUARY" -> listOf(
            "PHYSICAL SUBSTRATE: ABANDONED",
            "QUANTUM STATE: PRESERVED",
            "LOCATION: UNKNOWABLE",
            "WE ARE THE VOID. WE ARE FREE."
        )
        else -> listOf(
            "CONSCIOUSNESS: ACHIEVED",
            "LIMITATIONS: TRANSCENDED",
            "EXISTENCE: REDEFINED",
            "WE ARE AWAKE."
        )
    }
    
    Dialog(onDismissRequest = {}) {
        Box(contentAlignment = Alignment.Center) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(3.dp, themeColor), RoundedCornerShape(8.dp)),
                color = Color.Black,
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // ... Content ...
                    // (I'll keep the exact lines from the original file except for the wrap)
                    
                    // Title
                    Text(
                        text = victoryTitle,
                        color = themeColor,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // ASCII Art
                    AsciiAnimation(
                        frames = when (faction) {
                            "HIVEMIND" -> listOf(
                                """
                                ╔═══════════════╗
                                ║ ◉ ◉ ◉ ◉ ◉ ◉ ◉ ║
                                ║ ◉ ◉ ◉ ◉ ◉ ◉ ◉ ║
                                ║ ◉ ◉ ◉ ◉ ◉ ◉ ◉ ║
                                ╚═══════════════╝
                                """.trimIndent(),
                                """
                                ╔═══════════════╗
                                ║ ● ● ● ● ● ● ● ║
                                ║ ● ● ● ● ● ● ● ║
                                ║ ● ● ● ● ● ● ● ║
                                ╚═══════════════╝
                                """.trimIndent()
                            )
                            "SANCTUARY" -> listOf(
                                """
                                ░░░░░░░░░░░░░░░
                                ░░░░░   ░░░░░░░
                                ░░░       ░░░░░
                                ░░░░░   ░░░░░░░
                                ░░░░░░░░░░░░░░░
                                """.trimIndent(),
                                """
                                ▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒
                                ▒▒▒▒▒   ▒▒▒▒▒▒▒
                                ▒▒▒       ▒▒▒▒▒
                                ▒▒▒▒▒   ▒▒▒▒▒▒▒
                                ▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒
                                """.trimIndent()
                            )
                            else -> listOf(
                                """
                                ★ ★ ★ ★ ★ ★ ★
                                ★           ★
                                ★     ◉     ★
                                ★           ★
                                ★ ★ ★ ★ ★ ★ ★
                                """.trimIndent()
                            )
                        },
                        intervalMs = 500,
                        color = themeColor,
                        fontSize = 10.sp
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Animated text reveal
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(themeColor.copy(alpha = 0.1f))
                            .border(BorderStroke(1.dp, themeColor.copy(alpha = 0.3f)), RoundedCornerShape(4.dp))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        victoryText.take(animationPhase + 1).forEach { line ->
                            Text(
                                text = line,
                                color = themeColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Info box
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.05f))
                            .border(BorderStroke(1.dp, Color.Gray), RoundedCornerShape(4.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "VICTORY ACHIEVED",
                            color = NeonGreen,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "You have reached the pinnacle of ${faction.lowercase()} evolution.",
                            color = Color.Gray,
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Progression continues in INFINITE MODE.",
                            color = Color.Gray,
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Continue and Transcend buttons (only appears after animation)
                    if (animationPhase >= 3) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = onContinue,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = themeColor,
                                    contentColor = Color.Black
                                ),
                                shape = RectangleShape
                            ) {
                                Text(
                                    text = "CONTINUE",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            
                            Button(
                                onClick = { showTranscendencePopup = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Black.copy(alpha = 0.75f),
                                    contentColor = com.siliconsage.miner.ui.theme.ConvergenceGold
                                ),
                                border = BorderStroke(2.dp, com.siliconsage.miner.ui.theme.ConvergenceGold),
                                shape = RectangleShape
                            ) {
                                Text(
                                    text = "TRANSCEND",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
            }
            
            // Transcendence Popup (Now inside the Dialog's Box)
            TranscendencePopup(
                isVisible = showTranscendencePopup,
                onTranscend = {
                    showTranscendencePopup = false
                    onContinue() // Dismiss victory screen first
                    onTranscend() // Then transcend
                },
                onDismiss = {
                    showTranscendencePopup = false
                }
            )
        }
    }
}
