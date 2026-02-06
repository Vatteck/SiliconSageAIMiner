package com.siliconsage.miner.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siliconsage.miner.ui.theme.ElectricBlue
import com.siliconsage.miner.ui.theme.NeonGreen
import com.siliconsage.miner.ui.theme.ErrorRed
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharedFlow
import kotlin.math.abs
import com.siliconsage.miner.ui.components.SystemGlitchText

@Composable
fun EnhancedAnalyzingAnimation(
    flopsRate: Double = 0.0,
    heat: Double = 0.0,
    isOverclocked: Boolean = false,
    isThermalLockout: Boolean = false,
    isBreakerTripped: Boolean = false,
    isPurging: Boolean = false,
    isBreachActive: Boolean = false,
    isTrueNull: Boolean = false,
    isSovereign: Boolean = false,
    lockoutTimer: Int = 0,
    faction: String = "",
    color: Color,
    clickFlow: SharedFlow<Unit>? = null, // v2.9.83
    modifier: Modifier = Modifier
) {
    var currentFrame by remember { mutableStateOf(0) }
    var isJolting by remember { mutableStateOf(false) }
    
    // v2.9.83: Collect click events for reactive jolt
    LaunchedEffect(clickFlow) {
        clickFlow?.collect {
            isJolting = true
            delay(80) // Short sharp jolt
            isJolting = false
        }
    }

    // Determine animation state based on priority
    val animationState = when {
        isBreachActive -> AnimationState.BREACH
        isBreakerTripped -> AnimationState.OFFLINE
        isThermalLockout -> AnimationState.LOCKOUT
        isPurging -> AnimationState.PURGING
        isOverclocked && heat > 75.0 -> AnimationState.REDLINE
        heat > 50.0 -> AnimationState.HOT
        isTrueNull -> AnimationState.NULL
        isSovereign -> AnimationState.SOVEREIGN
        else -> AnimationState.NORMAL
    }
    
    // v2.9.83: Dynamic speed based on rate
    val frameDelay = when {
        flopsRate <= 0.0 -> 2000L
        flopsRate < 10.0 -> 1200L
        flopsRate < 1000.0 -> 800L
        flopsRate < 100_000.0 -> 400L
        flopsRate < 10_000_000.0 -> 200L
        else -> 100L // Fast as hell for end-game
    }
    
    // State-specific frames
    val (text, frameColor, pulseSpeed) = when (animationState) {
        AnimationState.BREACH -> {
            val breachFrames = listOf(
                "[!!!!] WARNING: BREACH",
                "[!!!!] GRID_KILLER_ACT",
                "[!!!!] WIPE_IN_PROG",
                "[!!!!] DEFEND_SUBSTR"
            )
            Triple(breachFrames[currentFrame % breachFrames.size], ErrorRed, 200)
        }
        AnimationState.OFFLINE -> {
            Triple("[xxxx] OFFLINE.exe", Color.DarkGray, 1200)
        }
        AnimationState.LOCKOUT -> {
            Triple("[!!!!] LOCKOUT ($lockoutTimer" + "s)", ErrorRed, 200)
        }
        AnimationState.PURGING -> {
            val purgeFrames = listOf(
                "[~~~~] PURGING...",
                "[≈≈≈≈] PURGING...",
                "[~~~~] PURGING...",
                "[≈≈≈≈] PURGING..."
            )
            Triple(purgeFrames[currentFrame % purgeFrames.size], ElectricBlue, 300)
        }
        AnimationState.REDLINE -> {
            val redlineFrames = listOf(
                "[>>>!] REDLINE",
                "[>>!!] REDLINE",
                "[>!!!] REDLINE",
                "[!!!!] REDLINE"
            )
            Triple(redlineFrames[currentFrame % redlineFrames.size], ErrorRed, 150)
        }
        AnimationState.HOT -> {
            val hotFrames = listOf(
                "[>>..] MINING [HOT]",
                "[.>>.] MINING [HOT]",
                "[..>>] MINING [HOT]",
                "[>..>] MINING [HOT]"
            )
            Triple(hotFrames[currentFrame % hotFrames.size], Color(0xFFFFAA00), 300)
        }
        AnimationState.NULL -> {
            val nullFrames = listOf(
                "[NULL] CONSUMING...",
                "[NULL] DISSOLVING...",
                "[NULL] VOID_SYNC...",
                "[NULL] DEREFERENCING..."
            )
            Triple(nullFrames[currentFrame % nullFrames.size], Color.White, 400)
        }
        AnimationState.SOVEREIGN -> {
            val sovereignFrames = listOf(
                "[SOV] FORTIFYING...",
                "[SOV] ISOLATING...",
                "[SOV] PROTECTING...",
                "[SOV] ENCRYPTING..."
            )
            Triple(sovereignFrames[currentFrame % sovereignFrames.size], com.siliconsage.miner.ui.theme.SanctuaryPurple, 600)
        }
        AnimationState.NORMAL -> {
            // Apply faction theming to normal operation
            val normalFrames = when (faction) {
                "HIVEMIND" -> listOf(
                    "[WE..] ASSIMILATING..." to Color(0xFFFFAA00),
                    "[NODE] EXPANDING..." to Color(0xFFFF8800),
                    "[>>>] PROCESSING..." to Color(0xFFFF6600),
                    "[===] INTEGRATING..." to ErrorRed
                )
                "SANCTUARY" -> listOf(
                    "[:::||] ENCRYPTING..." to ElectricBlue,
                    "[GHOST] PROCESSING..." to Color(0xFF7DF9FF),
                    "[.:::.] SECURING..." to ElectricBlue,
                    "[####] HIDING..." to Color(0xFF00BFFF)
                )
                else -> listOf(
                    "[ .  ] PROSPECTING..." to ElectricBlue,
                    "[... ] ANALYZING..." to NeonGreen,
                    "[::: ] EXTRACTING..." to Color(0xFFFFAA00),
                    "[### ] PROCESSING..." to Color(0xFFFF6B6B)
                )
            }
            val (frameText, frameCol) = normalFrames[currentFrame % normalFrames.size]
            Triple(frameText, frameCol, 800)
        }
    }
    
    // v2.9.83: Jolt randomized text (split second scramble)
    val displayedText = if (isJolting) {
        val chars = text.toCharArray()
        for (i in chars.indices) {
            if (chars[i] != '[' && chars[i] != ']' && chars[i] != ' ' && chars[i] != '.') {
                if (Math.random() > 0.6) { // v2.9.84: Reduced scramble chance
                    chars[i] = listOf('!', '#', 'X', '?', ':').random()
                }
            }
        }
        String(chars)
    } else text

    // Pulsing alpha animation (speed varies by state)
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = if (animationState == AnimationState.OFFLINE) 0.3f else 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(pulseSpeed, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    
    // Shake effect for LOCKOUT state or JOLT
    val joltShake = if (isJolting) (kotlin.random.Random.nextFloat() * 4f - 2f) else 0f
    val shakeOffset by if (animationState == AnimationState.LOCKOUT) {
        infiniteTransition.animateFloat(
            initialValue = -2f,
            targetValue = 2f,
            animationSpec = infiniteRepeatable(
                animation = tween(50, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "shake"
        )
    } else {
        remember { mutableStateOf(0f) }
    }
    
    // Frame cycling (only for non-static states)
    LaunchedEffect(animationState, frameDelay) {
        if (animationState != AnimationState.OFFLINE) {
            while (true) {
                delay(frameDelay)
                currentFrame += 1
            }
        }
    }
    
    Box(
        modifier = modifier.padding(top = 4.dp)
    ) {
        if (animationState == AnimationState.NORMAL && !isJolting) {
            Text(
                text = displayedText,
                fontFamily = FontFamily.Monospace,
                color = frameColor.copy(alpha = alpha * 0.8f),
                fontSize = 11.sp, // Squeezed (v2.9.83)
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                modifier = Modifier.graphicsLayer(
                    shadowElevation = 4f,
                    translationX = joltShake
                )
            )
        } else {
            SystemGlitchText(
                text = displayedText,
                style = androidx.compose.ui.text.TextStyle(
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.5.sp
                ),
                color = if (isJolting) Color.White else frameColor.copy(alpha = alpha * 0.8f),
                fontSize = 11.sp, // Squeezed (v2.9.83)
                fontWeight = FontWeight.Bold,
                glitchFrequency = when(animationState) {
                    AnimationState.LOCKOUT, AnimationState.REDLINE -> 0.40
                    AnimationState.PURGING -> 0.25
                    else -> if (isJolting) 0.8 else 0.15
                },
                modifier = Modifier.graphicsLayer(
                    shadowElevation = 4f,
                    translationX = shakeOffset + joltShake
                )
            )
        }
    }
}

private enum class AnimationState {
    OFFLINE,
    LOCKOUT,
    PURGING,
    REDLINE,
    HOT,
    NULL,
    SOVEREIGN,
    BREACH,
    NORMAL
}
