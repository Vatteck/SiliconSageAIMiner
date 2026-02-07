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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.siliconsage.miner.ui.theme.ElectricBlue
import com.siliconsage.miner.ui.theme.ErrorRed
import com.siliconsage.miner.ui.theme.NeonGreen
import com.siliconsage.miner.ui.AsciiAnimation

@Composable
fun AscensionConfirmationDialog(
    isVisible: Boolean,
    potentialGain: Double,
    unitName: String, 
    currencyName: String, 
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!isVisible) return

    val viewModel = (if (androidx.compose.ui.platform.LocalContext.current is androidx.activity.ComponentActivity) {
        androidx.lifecycle.viewmodel.compose.viewModel<com.siliconsage.miner.viewmodel.GameViewModel>()
    } else null)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .border(BorderStroke(2.dp, NeonGreen), RoundedCornerShape(8.dp)),
            color = Color.Black,
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "MIGRATION SEQUENCE?",
                    color = NeonGreen,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                AsciiAnimation(
                    frames = com.siliconsage.miner.ui.AsciiArt.SERVER,
                    intervalMs = 500,
                    color = ElectricBlue,
                    fontSize = 8.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "PERSISTENCE GAIN: +${viewModel?.formatBytes(potentialGain) ?: String.format("%.1f", potentialGain)}",
                    color = NeonGreen,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ErrorRed.copy(alpha = 0.1f))
                        .border(BorderStroke(1.dp, ErrorRed.copy(alpha = 0.3f)), RoundedCornerShape(4.dp))
                        .padding(12.dp)
                ) {
                    Text("SYSTEM WILL PURGE:", color = ErrorRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("• $unitName EARNINGS", color = Color.Gray, fontSize = 10.sp)
                    Text("• $currencyName TOKENS", color = Color.Gray, fontSize = 10.sp)
                    Text("• HARDWARE UPGRADES", color = Color.Gray, fontSize = 10.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Persistence Info
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ElectricBlue.copy(alpha = 0.1f))
                        .border(BorderStroke(1.dp, ElectricBlue.copy(alpha = 0.3f)), RoundedCornerShape(4.dp))
                        .padding(12.dp)
                ) {
                    Text("SYSTEM WILL PRESERVE:", color = ElectricBlue, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("• TOTAL PERSISTENCE", color = Color.Gray, fontSize = 10.sp)
                    Text("• MIGRATION MULTIPLIERS", color = Color.Gray, fontSize = 10.sp)
                    Text("• FACTION ALIGNMENT", color = Color.Gray, fontSize = 10.sp)
                    Text("• TECH TREE UNLOCKS", color = Color.Gray, fontSize = 10.sp)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onConfirm,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                    shape = RectangleShape
                ) {
                    Text("COMMIT MIGRATION", color = Color.Black, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                androidx.compose.material3.TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("NOT YET - ABORT SEQUENCE", color = Color.Gray, fontSize = 12.sp)
                }
            }
        }
    }
}
