package com.siliconsage.miner.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.foundation.border
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.animation.core.*
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

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val maxWidth = maxWidth
        val maxHeight = maxHeight

        // Random Position State (Calculated once when visible)
        var offsetX by remember { mutableStateOf(0.dp) }
        var offsetY by remember { mutableStateOf(0.dp) }
        
        // Bobbing Animation
        val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "airdropBob")
        val bobbingOffset by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = -10f,
            animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                animation = androidx.compose.animation.core.tween(1000, easing = androidx.compose.animation.core.LinearEasing),
                repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
            ),
            label = "bobbing"
        )

        LaunchedEffect(isVisible) {
             // Generate random position within safe bounds (padding)
             // Approx 80.dp size -> max offset = width - 100.dp
             val safeW = (maxWidth - 100.dp).coerceAtLeast(0.dp)
             val safeH = (maxHeight - 200.dp).coerceAtLeast(0.dp) // Avoid header/footer area
             
             offsetX = (kotlin.random.Random.nextDouble() * safeW.value).dp + 20.dp
             offsetY = (kotlin.random.Random.nextDouble() * safeH.value).dp + 100.dp // Offset from top
        }

        Box(
            modifier = Modifier
                .offset(x = offsetX, y = offsetY + bobbingOffset.dp)
                .size(64.dp)
                .background(NeonGreen.copy(alpha=0.2f), CircleShape)
                .border(2.dp, NeonGreen, CircleShape)
                .clickable { onClaimValues() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.Inventory, // Material 'Inventory' crate
                contentDescription = "Airdrop",
                tint = NeonGreen,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}
