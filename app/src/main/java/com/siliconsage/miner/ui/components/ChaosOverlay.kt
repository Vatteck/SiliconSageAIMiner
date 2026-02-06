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
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siliconsage.miner.ui.theme.ErrorRed
import com.siliconsage.miner.ui.theme.NeonGreen
import kotlinx.coroutines.delay

@Composable
fun SecurityBreachOverlay(
    isVisible: Boolean,
    clicksRemaining: Int,
    onDefendClick: () -> Unit
) {
    if (!isVisible) return

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val maxWidth = maxWidth
        val maxHeight = maxHeight

        // Random Target Position State
        var targetX by remember { mutableStateOf(0.dp) }
        var targetY by remember { mutableStateOf(0.dp) }

        // Animation for Target Movement - v2.8.0 Slower movement
        val transition = rememberInfiniteTransition(label = "targetMovement")
        val offsetX by transition.animateFloat(
            initialValue = 0f,
            targetValue = targetX.value,
            animationSpec = infiniteRepeatable(
                animation = tween(4000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "offsetX"
        )
        val offsetY by transition.animateFloat(
            initialValue = 0f,
            targetValue = targetY.value,
            animationSpec = infiniteRepeatable(
                animation = tween(5000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "offsetY"
        )

        // Target Pulse/Scan Animation
        val pulse by transition.animateFloat(
            initialValue = 1f,
            targetValue = 1.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "targetPulse"
        )

        val backgroundAlpha by transition.animateFloat(
            initialValue = 0.3f,
            targetValue = 0.5f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bgAlpha"
        )

        LaunchedEffect(isVisible) {
            val safeW = (maxWidth - 120.dp).coerceAtLeast(0.dp)
            val safeH = (maxHeight - 250.dp).coerceAtLeast(0.dp)
            
            // Randomly update target position - v2.8.0 Slower updates
            while(true) {
                targetX = (kotlin.random.Random.nextDouble() * safeW.value).dp + 20.dp
                targetY = (kotlin.random.Random.nextDouble() * safeH.value).dp + 120.dp
                delay(4000)
            }
        }

        // Darkening background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ErrorRed.copy(alpha = backgroundAlpha))
                .clickable(enabled = false) {}, // Block accidental background taps
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 80.dp)
            ) {
                SystemGlitchText(
                    text = "NETWORK BREACH DETECTED!",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    glitchFrequency = 0.4
                )
                Text(
                    text = "NEUTRALIZE THE UPLINK",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Light
                )
            }
        }

        // Moving Target Button - Cyber Reticle Style
        Box(
            modifier = Modifier
                .offset(x = offsetX.dp, y = offsetY.dp)
                .size(100.dp)
                .graphicsLayer { 
                    scaleX = pulse
                    scaleY = pulse
                }
                .border(2.dp, ErrorRed, CircleShape)
                .padding(4.dp) // Gap for reticle look
                .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                .background(Color.Black.copy(alpha = 0.9f), CircleShape)
                .clickable { onDefendClick() },
            contentAlignment = Alignment.Center
        ) {
            // Inner Reticle Crosshair
            Box(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                // Horizontal
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(ErrorRed.copy(alpha = 0.5f)).align(Alignment.Center))
                // Vertical
                Box(modifier = Modifier.fillMaxHeight().width(1.dp).background(ErrorRed.copy(alpha = 0.5f)).align(Alignment.Center))
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Breach",
                    tint = ErrorRed,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = clicksRemaining.toString(),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
fun FiftyOneAttackOverlay(
    isVisible: Boolean,
    tapsRemaining: Int,
    onTap: () -> Unit
) {
    if (!isVisible) return

    val infiniteTransition = rememberInfiniteTransition(label = "51attack")
    val backgroundAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(100, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bgAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ErrorRed.copy(alpha = backgroundAlpha))
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(32.dp)
                .background(Color.Black)
                .border(2.dp, ErrorRed)
                .padding(24.dp)
        ) {
            SystemGlitchText(
                text = "51% ATTACK IN PROGRESS",
                color = ErrorRed,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                glitchFrequency = 0.5
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "NETWORK INTEGRITY COMPROMISED",
                color = Color.White,
                fontSize = 14.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = onTap,
                colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                shape = RectangleShape,
                modifier = Modifier.fillMaxWidth().height(64.dp)
            ) {
                Text(
                    text = "REINFORCE FIREWALL ($tapsRemaining)",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "SPAM TO REPEL ATTACK",
                color = Color.Gray,
                fontSize = 11.sp
            )
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
