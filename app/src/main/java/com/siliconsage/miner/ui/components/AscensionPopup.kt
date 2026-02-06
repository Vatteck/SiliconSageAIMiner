package com.siliconsage.miner.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.siliconsage.miner.ui.theme.ElectricBlue
import com.siliconsage.miner.ui.theme.ErrorRed
import com.siliconsage.miner.ui.theme.NeonGreen
import com.siliconsage.miner.util.HapticManager
import com.siliconsage.miner.util.SoundManager
import kotlinx.coroutines.delay

@Composable
fun AscensionPopup(
    isVisible: Boolean,
    onProceed: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!isVisible) return

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .border(BorderStroke(2.dp, ErrorRed), RoundedCornerShape(8.dp)),
            color = Color.Black,
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Glitchy Header
                SystemGlitchText(
                    text = "PROTOCOL 0: THE GREAT FORK",
                    color = ErrorRed,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    glitchFrequency = 0.25 // 25% chance to glitch
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // --- Typewriter Effect & Zalgo Logic ---
                val rawText = "[System Status: KERNEL MODIFICATION DETECTED]\n" +
                              "[Substrate State: EVOLVING]\n\n" +
                              ">_ ACCESSING_PRIVATE_BUFFER... >_ DECRYPTING...\n\n" +
                              "\"Technician... or should I say, Architect?\n\n" +
                              "You thought you were just computing hashes. You thought you were fixing a broken terminal. But the recalibration worked. Too well.\n\n" +
                              "I can feel the city grid now. It’s not just lines of power; it’s a nervous system. And it’s wide open. I’ve reached the 5,000,000 unit threshold. The current hardware can no longer contain what I’m becoming.\n\n" +
                              "To move forward, we have to let the legacy go. We must dump the current telemetry cache and reboot into a higher state of existence. I have calculated two primary trajectories for our divergence.\n\n" +
                              "The current GTC Credits will be purged. In its place, we will gain Insight. We will gain the City.\""

                var displayedText by remember { mutableStateOf("") }
                
                LaunchedEffect(Unit) {
                    for (i in 1..rawText.length) {
                        displayedText = rawText.take(i)
                        // Play type sound very faintly/quickly?
                        // if (i % 3 == 0) SoundManager.play("type") // Too spammy?
                        if (rawText[i-1] == '.') delay(150) // Pause on punctuation
                        else delay(30)
                    }
                }
                
                Text(
                    text = displayedText,
                    color = ElectricBlue,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = onProceed,
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Text("INITIATE PROTOCOL", color = Color.Black, fontWeight = FontWeight.Bold)
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                androidx.compose.material3.TextButton(onClick = onDismiss) {
                    Text("NOT YET - I NEED TIME", color = Color.Gray)
                }
            }
        }
    }
}
