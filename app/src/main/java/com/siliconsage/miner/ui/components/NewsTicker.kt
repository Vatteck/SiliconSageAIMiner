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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siliconsage.miner.ui.theme.NeonGreen
import com.siliconsage.miner.ui.theme.ErrorRed
import com.siliconsage.miner.ui.theme.ElectricBlue
import com.siliconsage.miner.util.SoundManager
import kotlinx.coroutines.delay

@Composable
fun NewsTicker(
    news: String,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ticker")
    
    // Parse the news to get color and clean text
    val (displayText, baseColor) = remember(news) {
        var text = news
        var color = NeonGreen // Default

        when {
            text.contains("[BULL]") -> {
                text = text.replace("[BULL]", "").trim()
                color = NeonGreen
            }
            text.contains("[BEAR]") -> {
                text = text.replace("[BEAR]", "").trim()
                color = Color(0xFFFFA500) // Orange
            }
            text.contains("[ENERGY_SPIKE]") -> {
                text = text.replace("[ENERGY_SPIKE]", "").trim()
                color = ErrorRed
            }
            text.contains("[ENERGY_DROP]") -> {
                text = text.replace("[ENERGY_DROP]", "").trim()
                color = ElectricBlue
            }
            text.contains("[GLITCH]") -> {
                text = text.replace("[GLITCH]", "").trim()
                color = ElectricBlue // Placeholder, overridden by effect
            }
            text.contains("[STORY_PROG]") -> {
                text = text.replace("[STORY_PROG]", "").trim()
                color = Color(0xFFFFD700) // Gold
            }
            text.contains("[LORE]") -> {
                text = text.replace("[LORE]", "").trim()
                color = Color.White
            }
            // Fallback for any other tags or no tags
            else -> {
                // Formatting clean up just in case
                text = text.replace(Regex("\\[.*?\\]"), "").trim()
            }
        }
        text to color
    }

    // Dynamic range based on clean text length
    val textLength = displayText.length
    val estimatedWidth = textLength * 8f + 200f // Rough estimate in dp
    
    val offsetX by infiniteTransition.animateFloat(
        initialValue = 500f, // Start from right side
        targetValue = -estimatedWidth, // End way off-screen left
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (estimatedWidth * 15).toInt().coerceAtLeast(5000), 
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "tickerOffset"
    )

    androidx.compose.runtime.LaunchedEffect(news) {
        if (news.isNotEmpty()) {
            val startTime = System.currentTimeMillis()
            while (System.currentTimeMillis() - startTime < 2000) {
                SoundManager.play("type")
                delay(180) 
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(24.dp)
            .background(Color.Black.copy(alpha = 0.8f))
            .clipToBounds()
    ) {
        if (displayText.isNotEmpty()) {
            val isGlitch = news.contains("[GLITCH]")
            
            // Glitch visual effect logic
            var finalColor = baseColor
            if (isGlitch) {
                 val flicker by infiniteTransition.animateFloat(
                    initialValue = 0f, targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(100, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "glitchFlicker"
                )
                finalColor = if (flicker > 0.5f) ErrorRed else ElectricBlue
            }
            
            Text(
                text = ">>> MARKET UPDATE: $displayText <<<",
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
