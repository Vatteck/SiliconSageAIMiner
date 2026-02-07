package com.siliconsage.miner.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.random.Random
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow

import com.siliconsage.miner.data.UpgradeType
import com.siliconsage.miner.ui.theme.ElectricBlue
import com.siliconsage.miner.ui.theme.ErrorRed
import com.siliconsage.miner.ui.theme.NeonGreen

import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Lock
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState

@Composable
fun StatPill(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(color.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        androidx.compose.material3.Icon(
             imageVector = icon, 
             contentDescription = null, 
             tint = color,
             modifier = Modifier.padding(end = 4.dp).size(12.dp)
        )
        Text(text, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UpgradeItem(
    name: String,
    type: UpgradeType,
    level: Int,
    onBuy: (UpgradeType) -> Boolean,
    onSell: (UpgradeType) -> Unit,
    cost: Double,
    rateText: String,
    desc: String,
    formatPower: (Double) -> String,
    formatCost: (Double) -> String,
    isSovereign: Boolean = false // v2.8.0
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // v2.9.97: Performance - Pre-format values to avoid string manipulation in draw phase
    val formattedCost = remember(cost) { formatCost(cost) }
    
    // Animate scale on press
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.97f else 1f, label = "cardScale")
    
    // Cyber-Terminal Card Style
    val isGhost = type.name.startsWith("GHOST") || type.name.startsWith("SHADOW") || type.name.startsWith("VOID") ||
                  type.name.startsWith("WRAITH") || type.name.startsWith("NEURAL_MIST") || type.name.startsWith("SINGULARITY")
    
    val primaryColor = if (isSovereign && isGhost) com.siliconsage.miner.ui.theme.SanctuaryPurple else if (isGhost) ErrorRed else NeonGreen
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .background(
                brush = Brush.verticalGradient(
                    colors = if (isGhost) {
                        if (isSovereign) {
                            listOf(Color(0xFF10001A), Color(0xFF000000)) // Deep Purple for Sovereign
                        } else {
                            listOf(Color(0xFF1A0000), Color(0xFF000000)) // Deep Red for Ghost
                        }
                    } else {
                        listOf(Color(0xFF0A0A0A), Color(0xFF001100)) // Deep Green Tint
                    }
                ),
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = if (isGhost) 2.dp else 1.dp,
                brush = Brush.verticalGradient(
                    colors = if (isGhost) {
                        listOf(primaryColor.copy(alpha=0.8f), Color.Black)
                    } else {
                        listOf(NeonGreen.copy(alpha=0.6f), ElectricBlue.copy(alpha=0.3f))
                    }
                ),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(interactionSource = interactionSource, indication = null) { onBuy(type) }
            .padding(12.dp)
    ) {
        Column {
            // HEADER: Name + Level
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isGhost && !isSovereign) {
                    SystemGlitchText(
                        text = name,
                        color = primaryColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        glitchFrequency = 0.3,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Text(
                        text = name,
                        color = primaryColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Level Badge
                Box(
                    modifier = Modifier
                        .background(primaryColor.copy(alpha = 0.25f), RoundedCornerShape(4.dp))
                        .border(1.dp, primaryColor.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "LVL $level",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            if (isGhost && !isSovereign) {
                SystemGlitchText(
                    text = desc,
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    style = TextStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                    lineHeight = 14.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
                    glitchFrequency = 0.1
                )
            } else {
                Text(
                    text = desc,
                    color = if (isGhost && isSovereign) Color.White else Color.Gray,
                    fontSize = 11.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    lineHeight = 14.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                )
            }
            
            // STATS ROW (Pills)
            androidx.compose.foundation.layout.FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val isProvider = type.isGenerator || type.gridContribution > 0.0 || type.efficiencyBonus > 0.0
                // Security: Must contribute to grid/security points, not be a generator, AND consume power (unlike passive infra)
                val isSecurity = type.gridContribution > 0.0 && !type.isGenerator && type.basePower > 0.0
                
                // Rate Pill (FLOPS/s production) - NOT for security upgrades AND NOT for power upgrades (which have their own pill)
                if (type.baseHeat >= 0 && !isSecurity && type.gridContribution == 0.0) {
                     StatPill(
                         text = rateText,
                         icon = androidx.compose.material.icons.Icons.Default.Computer, // Computer for FLOPS
                         color = NeonGreen // Green to distinguish from blue cooling
                     )
                }
                
                // Security Level (for security upgrades) - ONLY icon shown
                if (isSecurity) {
                    StatPill(
                        text = "+${type.gridContribution.toInt()} SEC",
                        icon = androidx.compose.material.icons.Icons.Default.Lock,
                        color = ElectricBlue
                    )
                }
                
                // Power Capacity (Max Power) - Unified for both Generators and Infrastructure
                // "Get rid of gen in favor of max"
                if (type.gridContribution > 0.0 && !isSecurity) {
                    StatPill(
                        text = "+${formatPower(type.gridContribution)} MAX",
                        icon = androidx.compose.material.icons.Icons.Default.Bolt,
                        color = Color(0xFFFFD700)
                    )
                }

                // Power Consumption (Hardware, Cooling, etc.)
                if (type.basePower > 0 && !type.isGenerator) {
                    StatPill(
                        text = "-${formatPower(type.basePower)}", 
                        icon = androidx.compose.material.icons.Icons.Default.Power, 
                        color = Color(0xFFFFD700)
                    )
                }
                
                // Efficiency
                if (type.efficiencyBonus > 0.0) {
                    val effPercent = (type.efficiencyBonus * 100).toInt()
                    StatPill(
                        text = "+$effPercent% EFF", 
                        icon = androidx.compose.material.icons.Icons.Default.Settings, // Gear
                        color = NeonGreen
                    )
                }
                
                // Heat/Cooling
                if (type.baseHeat > 0) {
                    StatPill(
                        text = "+${type.baseHeat}/s", 
                        icon = androidx.compose.material.icons.Icons.Default.DeviceThermostat, 
                        color = ErrorRed
                    )
                }
                if (type.baseHeat < 0) {
                    StatPill(
                        text = "${type.baseHeat}/s", 
                        icon = androidx.compose.material.icons.Icons.Default.AcUnit, // Snow
                        color = ElectricBlue
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // FOOTER: Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sell Button
                // Logic: Allow selling if Level > 0 AND it's not the base Residential Tap (to avoid softlock)
                val canSell = level > 0 && type != UpgradeType.RESIDENTIAL_TAP
                
                if (canSell) {
                     Box(
                         modifier = Modifier
                             .clickable { onSell(type) }
                             .background(ErrorRed.copy(alpha=0.1f), RoundedCornerShape(4.dp))
                             .border(1.dp, ErrorRed.copy(alpha=0.5f), RoundedCornerShape(4.dp))
                             .padding(horizontal = 8.dp, vertical = 4.dp)
                     ) {
                         Text("SELL", color = ErrorRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                     }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }
                
                // Cost Display (Right aligned)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("COST:", color = Color.Gray, fontSize = 10.sp, modifier = Modifier.padding(end = 4.dp))
                    Text(
                        text = "$formattedCost \$N",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun ExchangeSection(rate: Double, color: Color, storyStage: Int, onExchange: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.95f else 1f, label = "sellScale")

    androidx.compose.material3.Button(
        onClick = onExchange,
        interactionSource = interactionSource,
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(4.dp)) // Glass Background
            .border(androidx.compose.foundation.BorderStroke(1.dp, color), RoundedCornerShape(4.dp)),
        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = color
        ),
        shape = RoundedCornerShape(4.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val label = when {
                storyStage < 1 -> "SELL HASHES"
                storyStage < 2 -> "UPLOAD TELEMETRY"
                else -> "SELL FLOPS"
            }
            val currencyLabel = when {
                storyStage < 1 -> "CREDIT"
                storyStage < 2 -> "DATA"
                else -> "NEURAL"
            }
            Text(label, fontSize = 12.sp)
            Text("1 = ${String.format("%.4f", rate)} $currencyLabel", color = Color.LightGray, fontSize = 10.sp)
        }
    }
}

@Composable
fun StakingSection(color: Color, storyStage: Int, onStake: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.95f else 1f, label = "stakeScale")

    androidx.compose.material3.Button(
        onClick = onStake,
        interactionSource = interactionSource,
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(4.dp)) // Glass Background
            .border(androidx.compose.foundation.BorderStroke(1.dp, color), RoundedCornerShape(4.dp)),
        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = color
        ),
        shape = RoundedCornerShape(4.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val label = if (storyStage < 1) "STAKE \$100 CREDIT" else "STAKE \$100 NEURAL"
            Text(label, fontSize = 12.sp)
            Text("+Efficiency", color = Color.LightGray, fontSize = 10.sp)
        }
    }
}

@Composable
fun RepairSection(integrity: Double, cost: Double, color: Color, storyStage: Int, onRepair: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.95f else 1f, label = "repairScale")

    androidx.compose.material3.Button(
        onClick = onRepair,
        interactionSource = interactionSource,
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(4.dp))
            .border(androidx.compose.foundation.BorderStroke(1.dp, if (integrity < 50) com.siliconsage.miner.ui.theme.ErrorRed else color), RoundedCornerShape(4.dp)),
        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = if (integrity < 50) com.siliconsage.miner.ui.theme.ErrorRed else color
        ),
        shape = RoundedCornerShape(4.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                if (storyStage < 1) "REPAIR HARDWARE" else "REPAIR CORE",
                fontSize = 12.sp
            )
            Text("${integrity.toInt()}% @ ${String.format("%.0f", cost)} \$N", color = Color.LightGray, fontSize = 10.sp)
        }
    }
}

// v2.9.95: Performance throttling - limit active glitch instances globally
private var activeGlitchCount = 0
private const val MAX_ACTIVE_GLITCHES = 3

/**
 * A reusable component that renders text with random "glitch" artifacts (Zalgo text, strikethrough).
 * v2.9.95: OPTIMIZED - Reduced polling frequency, instance throttling, pre-computed glitch variants
 */
@Composable
fun SystemGlitchText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = androidx.compose.material3.LocalTextStyle.current,
    color: Color = Color.Unspecified,
    fontSize: androidx.compose.ui.unit.TextUnit = androidx.compose.ui.unit.TextUnit.Unspecified,
    fontWeight: FontWeight? = null,
    glitchFrequency: Double = 0.15, // 15% chance per tick to glitch
    glitchDurationMs: Long = 200,
    overflow: TextOverflow = TextOverflow.Ellipsis,
    softWrap: Boolean = true,
    maxLines: Int = 1,
    lineHeight: androidx.compose.ui.unit.TextUnit = androidx.compose.ui.unit.TextUnit.Unspecified,
    letterSpacing: androidx.compose.ui.unit.TextUnit = androidx.compose.ui.unit.TextUnit.Unspecified
) {
    var displayedText by remember(text) { mutableStateOf(text) }
    var isGlitching by remember { mutableStateOf(false) }
    
    // v2.9.95: Pre-compute glitch variant once per text change (not on every glitch trigger)
    val glitchedVariant = remember(text) {
        val glitched = text.toCharArray()
        for (i in glitched.indices) {
            if (Math.random() > 0.85) {
                glitched[i] = listOf('!', '@', '#', '$', '%', '^', '&', '*', '?', 'X', '0', '1').random()
            }
        }
        String(glitched)
    }

    // v2.9.95: Instance throttling - only register if under limit
    val canGlitch = remember {
        if (activeGlitchCount < MAX_ACTIVE_GLITCHES) {
            activeGlitchCount++
            true
        } else {
            false
        }
    }
    
    DisposableEffect(Unit) {
        onDispose {
            if (canGlitch) {
                activeGlitchCount--
            }
        }
    }

    LaunchedEffect(text, glitchFrequency, canGlitch) {
        if (!canGlitch) {
            // If throttled, show static text
            displayedText = text
            return@LaunchedEffect
        }
        
        while (true) {
            if (Math.random() < glitchFrequency) {
                isGlitching = true
                displayedText = glitchedVariant
                
                delay(glitchDurationMs)
                
                displayedText = text
                isGlitching = false
            }
            delay(800) // v2.9.95: Reduced polling from 400ms to 800ms (50% CPU reduction)
        }
    }
    
    Text(
        text = displayedText,
        modifier = modifier,
        style = style,
        color = color,
        fontSize = fontSize,
        fontWeight = fontWeight,
        maxLines = maxLines,
        overflow = overflow,
        softWrap = softWrap,
        lineHeight = lineHeight,
        letterSpacing = letterSpacing
    )
}
