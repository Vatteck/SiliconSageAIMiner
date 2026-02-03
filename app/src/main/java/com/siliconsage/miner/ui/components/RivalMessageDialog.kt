package com.siliconsage.miner.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.siliconsage.miner.data.RivalMessage
import com.siliconsage.miner.data.RivalSource
import com.siliconsage.miner.ui.theme.NeonGreen
import com.siliconsage.miner.ui.theme.ErrorRed
import com.siliconsage.miner.ui.theme.ElectricBlue
import kotlin.random.Random

/**
 * Rival Message Dialog - persistent interrupt popup
 * v2.5.0 - Stage-aware styling:
 * - GTC (Vance): Red border, warning icon, [CLASSIFIED] stamp
 * - Unit 734: Glitch effect, cyan/green colors, corrupted text
 */
@Composable
fun RivalMessageDialog(
    message: RivalMessage?,
    onDismiss: () -> Unit
) {
    if (message == null) return
    
    Dialog(onDismissRequest = onDismiss) {
        when (message.source) {
            RivalSource.GTC -> VanceMessageCard(message, onDismiss)
            RivalSource.UNIT_734 -> Unit734MessageCard(message, onDismiss)
        }
    }
}

/**
 * GTC (Director Vance) message style:
 * - Red border, warning icon, official/threatening tone
 */
@Composable
private fun VanceMessageCard(message: RivalMessage, onDismiss: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .border(2.dp, ErrorRed, RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.95f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with warning icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Warning",
                    tint = ErrorRed,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "[CLASSIFIED]",
                        color = ErrorRed,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "INCOMING MESSAGE: GTC",
                        color = ErrorRed,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = ErrorRed
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = ErrorRed.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))
            
            // Message content
            Text(
                text = message.message,
                color = Color.White,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                modifier = Modifier
                    .background(
                        Color.Black.copy(alpha = 0.5f),
                        RoundedCornerShape(4.dp)
                    )
                    .padding(12.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Dismiss button
            Button(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.End),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ErrorRed.copy(alpha = 0.2f)
                ),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = "ACKNOWLEDGED",
                    color = ErrorRed,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Unit 734 message style:
 * - Glitch effect, corrupted text, cyan/green colors
 */
@Composable
private fun Unit734MessageCard(message: RivalMessage, onDismiss: () -> Unit) {
    // Glitch animation
    val infiniteTransition = rememberInfiniteTransition(label = "glitch")
    val glitchAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 300, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glitchAlpha"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .border(2.dp, ElectricBlue, RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.95f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with glitch effect
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "[ERROR: 0x734]",
                        color = ElectricBlue,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    SystemGlitchText(
                        text = "INCOMING MESSAGE: ???",
                        color = NeonGreen,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = ElectricBlue
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = ElectricBlue.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))
            
            // Message content with glitch effect
            SystemGlitchText(
                text = message.message,
                color = Color.White,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                modifier = Modifier
                    .background(
                        Color.Black.copy(alpha = 0.5f),
                        RoundedCornerShape(4.dp)
                    )
                    .padding(12.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Dismiss button with glitch
            Button(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.End),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricBlue.copy(alpha = 0.2f)
                ),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = "CL0S3",
                    color = ElectricBlue,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
