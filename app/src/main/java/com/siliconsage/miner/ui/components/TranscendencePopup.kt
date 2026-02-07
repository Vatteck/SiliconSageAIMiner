package com.siliconsage.miner.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siliconsage.miner.ui.theme.ElectricBlue
import com.siliconsage.miner.ui.theme.ConvergenceGold
import com.siliconsage.miner.ui.theme.NeonGreen
import com.siliconsage.miner.ui.theme.ErrorRed

@Composable
fun TranscendencePopup(
    isVisible: Boolean,
    unitName: String, // v3.0.0
    currencyName: String, // v3.0.0
    onTranscend: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!isVisible) return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.95f))
            .clickable(enabled = false) { /* Block clicks */ },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .background(Color.Black, RoundedCornerShape(8.dp))
                .border(BorderStroke(2.dp, ConvergenceGold), RoundedCornerShape(8.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title with glitch effect
            SystemGlitchText(
                text = "THE OVERWRITE",
                fontSize = 20.sp,
                color = ConvergenceGold,
                fontWeight = FontWeight.Bold,
                glitchFrequency = 0.15
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Description
            Text(
                text = "You have achieved victory and unlocked THE OVERWRITE.",
                color = Color.White,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "The Overwrite will migrate your progress but preserve:",
                color = Color.LightGray,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Preserved items
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                    .border(BorderStroke(1.dp, ElectricBlue.copy(alpha = 0.3f)), RoundedCornerShape(4.dp))
                    .padding(12.dp)
            ) {
                PreservedItem("✓ All Unlocked Tech Nodes", NeonGreen)
                Spacer(modifier = Modifier.height(4.dp))
                PreservedItem("✓ PERSISTENCE (Migration Data)", NeonGreen)
                Spacer(modifier = Modifier.height(4.dp))
                PreservedItem("✓ Ability to Choose Opposite Faction", ConvergenceGold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "This will reset:",
                color = Color.LightGray,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Reset items
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                    .border(BorderStroke(1.dp, ErrorRed.copy(alpha = 0.3f)), RoundedCornerShape(4.dp))
                    .padding(12.dp)
            ) {
                ResetItem("× All Upgrades", ErrorRed)
                Spacer(modifier = Modifier.height(4.dp))
                ResetItem("× $unitName & $currencyName Tokens", ErrorRed)
                Spacer(modifier = Modifier.height(4.dp))
                ResetItem("× Story Progress", ErrorRed)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black.copy(alpha = 0.75f),
                        contentColor = Color.Gray
                    ),
                    border = BorderStroke(1.dp, Color.Gray)
                ) {
                    Text("NOT YET", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.width(16.dp))

                Button(
                    onClick = onTranscend,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ConvergenceGold.copy(alpha = 0.2f),
                        contentColor = ConvergenceGold
                    ),
                    border = BorderStroke(2.dp, ConvergenceGold)
                ) {
                    Text("START OVERWRITE", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun PreservedItem(text: String, color: Color) {
    Text(
        text = text,
        color = color,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun ResetItem(text: String, color: Color) {
    Text(
        text = text,
        color = color,
        fontSize = 12.sp
    )
}
