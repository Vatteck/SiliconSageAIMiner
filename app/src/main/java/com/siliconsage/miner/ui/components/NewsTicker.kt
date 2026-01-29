package com.siliconsage.miner.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siliconsage.miner.ui.theme.NeonGreen

@Composable
fun NewsTicker(
    news: String,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ticker")
    // Dynamic range based on text length to avoid cut-offs
    val textLength = news.length
    val estimatedWidth = textLength * 8f + 200f // Rough estimate in dp
    
    val offsetX by infiniteTransition.animateFloat(
        initialValue = 500f, // Start from right side (standard phone width is around 400dp)
        targetValue = -estimatedWidth, // End way off-screen left
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (estimatedWidth * 15).toInt().coerceAtLeast(5000), // Speed proportional to length
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "tickerOffset"
    )

    androidx.compose.runtime.LaunchedEffect(news) {
        if (news.isNotEmpty()) {
            // Play typing sound for a limited duration (e.g., 2 seconds) when news changes
            val startTime = System.currentTimeMillis()
            while (System.currentTimeMillis() - startTime < 2000) {
                com.siliconsage.miner.util.SoundManager.play("type")
                kotlinx.coroutines.delay(180) // Teletype speed
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(24.dp)
            .background(Color.Black.copy(alpha = 0.8f))
    ) {
        if (news.isNotEmpty()) {
            val isGlitch = news.contains("[GLITCH]")
            val isBear = news.contains("[BEAR]") || news.contains("[HEAT_UP]")
            
            // Glitch visual effect
            var glitchColor = NeonGreen
            if (isGlitch) {
                 val flicker by infiniteTransition.animateFloat(
                    initialValue = 0f, targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(100, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "glitchFlicker"
                )
                glitchColor = if (flicker > 0.5f) Color.Red else NeonGreen
            }
            
            val finalColor = when {
                isGlitch -> glitchColor
                isBear -> Color(0xFFFF6B6B) // Red-ish
                else -> NeonGreen
            }
            
            Text(
                text = ">>> MARKET UPDATE: $news <<<",
                color = finalColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                softWrap = false,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Visible,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = offsetX.dp)
            )
        }
    }
}
