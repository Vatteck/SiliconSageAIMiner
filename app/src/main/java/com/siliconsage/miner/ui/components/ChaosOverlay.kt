package com.siliconsage.miner.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siliconsage.miner.ui.theme.ErrorRed
import com.siliconsage.miner.ui.theme.NeonGreen

@Composable
fun SecurityBreachOverlay(
    isVisible: Boolean,
    clicksRemaining: Int,
    onDefendClick: () -> Unit
) {
    if (!isVisible) return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ErrorRed.copy(alpha = 0.8f))
            .clickable(enabled = false) {}, // Block touches
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Breach",
                tint = Color.White,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "NETWORK BREACH DETECTED!",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "TAP 'FIREWALL' TO DEFEND!",
                color = Color.White,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onDefendClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                Text(
                    text = "FIREWALL ($clicksRemaining)",
                    color = ErrorRed,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun AirdropButton(
    isVisible: Boolean,
    onClaimValues: () -> Unit
) {
    if (!isVisible) return

    Box(modifier = Modifier.fillMaxSize()) {
        Button(
            onClick = onClaimValues,
            modifier = Modifier
                .align(Alignment.TopEnd) // Randomize this later?
                .size(80.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
        ) {
           Text("DROP", color = Color.Black, fontWeight = FontWeight.Bold)
        }
    }
}
