package com.siliconsage.miner.ui

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import com.siliconsage.miner.ui.theme.ElectricBlue
import com.siliconsage.miner.ui.theme.ErrorRed
import com.siliconsage.miner.ui.theme.NeonGreen
import com.siliconsage.miner.util.HapticManager
import com.siliconsage.miner.util.SoundManager
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.siliconsage.miner.viewmodel.GameViewModel
import com.siliconsage.miner.data.TechNode

@Composable
fun NetworkScreen(viewModel: GameViewModel) {
    val prestigeMultiplier by viewModel.prestigeMultiplier.collectAsState()
    val prestigePoints by viewModel.prestigePoints.collectAsState()
    val techNodes by viewModel.techNodes.collectAsState()
    val unlockedNodes by viewModel.unlockedTechNodes.collectAsState()
    val potential = viewModel.calculatePotentialPrestige()
    
    val storyStage by viewModel.storyStage.collectAsState()
    val faction by viewModel.faction.collectAsState()
    val showStoryPopup = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(true) }
    val showAscensionConfirm = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(16.dp)
    ) {
        // Single LazyColumn for full screen scrolling
        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Text("GLOBAL NETWORK", color = NeonGreen, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                
                Spacer(modifier = Modifier.height(8.dp))
                AsciiAnimation(
                    frames = AsciiArt.MATRIX,
                    intervalMs = 150,
                    color = ElectricBlue,
                    fontSize = 10.sp
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // --- STATS ---
                PrestigeStat("INSIGHT CURRENCY", String.format("%.2f", prestigePoints))
                Spacer(modifier = Modifier.height(8.dp))
                PrestigeStat("NETWORK MULTIPLIER", "x${String.format("%.2f", prestigeMultiplier)}")
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // --- ASCENSION ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(8.dp)) // Glass
                        .border(BorderStroke(1.dp, ElectricBlue), RoundedCornerShape(8.dp))
                        .padding(16.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("SYSTEM ASCENSION", color = ElectricBlue, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Reboot the system to gain Insight and increase effectiveness.",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text("POTENTIAL GAIN:", color = NeonGreen, fontSize = 12.sp)
                        Text(
                            "+${String.format("%.2f", potential)} Insight",
                            color = NeonGreen,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        val canAscend = potential >= 1.0 || (storyStage == 1)
                        
                        Button(
                            onClick = { 
                                if (canAscend) {
                                    // Only show Story Popup on first run (Faction == NONE)
                                    if (storyStage == 1 && faction == "NONE") {
                                        showStoryPopup.value = true // Force popup to show
                                    } else {
                                        showAscensionConfirm.value = true
                                        SoundManager.play("click")
                                    }
                                } else {
                                    SoundManager.play("error")
                                    HapticManager.vibrateError()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .border(BorderStroke(1.dp, if (canAscend) NeonGreen else Color.DarkGray), RectangleShape),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (canAscend) ElectricBlue.copy(alpha=0.2f) else Color.DarkGray,
                                contentColor = if (canAscend) NeonGreen else Color.Black
                            ),
                            shape = RectangleShape,
                            enabled = canAscend
                        ) {
                            Text(
                                 if (storyStage == 1) "SYSTEM REBOOT REQUIRED (STORY)" else "INITIATE REBOOT", 
                                 fontWeight = FontWeight.Bold
                            )
                        }
                        
                        if (potential < 1.0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Requires > 1.0 Potential", color = Color.Gray, fontSize = 10.sp)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text("NEURAL TECH TREE", color = NeonGreen, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
            }

            
            // Legacy Grid Visualization
            item {
                LegacyGrid(
                    nodes = techNodes,
                    unlockedIds = unlockedNodes,
                    prestigePoints = prestigePoints,
                    onUnlock = { id -> 
                        viewModel.unlockTechNode(id)
                        SoundManager.play("buy")
                        HapticManager.vibrateSuccess()
                    }
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
            
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
        
        // Dialogs rendered outside LazyColumn to prevent state issues
        if (storyStage == 1 && faction == "NONE" && showStoryPopup.value) {
             com.siliconsage.miner.ui.components.AscensionPopup(
                isVisible = true,
                onProceed = {
                    showStoryPopup.value = false // Hide it so it doesn't reappear instantly if update lags
                    viewModel.ascend(isStory = true)
                    SoundManager.play("glitch")
                    HapticManager.vibrateSuccess()
                },
                onDismiss = {
                    showStoryPopup.value = false
                    SoundManager.play("click")
                }
            )
        }
        
        com.siliconsage.miner.ui.components.AscensionConfirmationDialog(
            isVisible = showAscensionConfirm.value,
            potentialGain = potential,
            onConfirm = {
                showAscensionConfirm.value = false
                viewModel.ascend(isStory = false)
                SoundManager.play("glitch") 
                HapticManager.vibrateSuccess()
            },
            onDismiss = {
                showAscensionConfirm.value = false
                SoundManager.play("click")
            }
        )
    }
}

@Composable
fun PrestigeStat(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(8.dp)) // Glass
            .border(BorderStroke(1.dp, Color.DarkGray), RoundedCornerShape(8.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = Color.Gray, fontSize = 12.sp)
        Text(value, color = NeonGreen, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun LegacyGrid(
    nodes: List<com.siliconsage.miner.data.TechNode>,
    unlockedIds: List<String>,
    prestigePoints: Double,
    onUnlock: (String) -> Unit
) {
    // Hardcoded positions for the 6-node tree (Relative to 0..400 x 400 space)
    val positions = mapOf(
        "neural_density" to Offset(0.5f, 0.1f),
        "cooling_efficiency" to Offset(0.2f, 0.4f),
        "market_predictions" to Offset(0.5f, 0.4f),
        "deep_retention" to Offset(0.8f, 0.4f),
        "quantum_tunnelling" to Offset(0.35f, 0.7f),
        "digital_immortality" to Offset(0.5f, 0.9f)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(500.dp) // Fixed height for the tree container
            .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(8.dp)) // Glass
            .padding(16.dp)
            .border(BorderStroke(1.dp, Color.DarkGray.copy(alpha=0.5f)), RoundedCornerShape(8.dp))
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
             nodes.forEach { node ->
                 val endPos = positions[node.id] ?: return@forEach
                 val endX = size.width * endPos.x
                 val endY = size.height * endPos.y
                 
                 node.requires.forEach { parentId ->
                     val startPos = positions[parentId] ?: return@forEach
                     val startX = size.width * startPos.x
                     val startY = size.height * startPos.y
                     
                     drawLine(
                         color = if (unlockedIds.contains(node.id)) NeonGreen else Color.DarkGray,
                         start = Offset(startX, startY),
                         end = Offset(endX, endY),
                         strokeWidth = 3.dp.toPx(),
                         cap = StrokeCap.Round
                     )
                 }
             }
        }
        
        // Overlay Buttons
        androidx.compose.ui.layout.Layout(
            content = {
                nodes.forEach { node ->
                    LegacyNodeButton(
                        node = node,
                        isUnlocked = unlockedIds.contains(node.id),
                        isUnlockable = (node.requires.isEmpty() || node.requires.all { unlockedIds.contains(it) }),
                        canAfford = prestigePoints >= node.cost,
                        onUnlock = { onUnlock(node.id) }
                    )
                }
            }
        ) { measurables, constraints ->
            val placeables = measurables.map { it.measure(constraints.copy(minWidth = 0, minHeight = 0)) }
            
            layout(constraints.maxWidth, constraints.maxHeight) {
                placeables.forEachIndexed { index, placeable ->
                    val node = nodes[index]
                    val pos = positions[node.id] ?: Offset(0.5f, 0.5f)
                    
                    val x = (constraints.maxWidth * pos.x) - (placeable.width / 2)
                    val y = (constraints.maxHeight * pos.y) - (placeable.height / 2)
                    
                    placeable.placeRelative(x = x.toInt(), y = y.toInt())
                }
            }
        }
    }
}

@Composable
fun LegacyNodeButton(
    node: com.siliconsage.miner.data.TechNode,
    isUnlocked: Boolean,
    isUnlockable: Boolean,
    canAfford: Boolean,
    onUnlock: () -> Unit
) {
    val borderColor = when {
        isUnlocked -> NeonGreen
        isUnlockable -> ElectricBlue
        else -> Color.DarkGray
    }
    
    val backgroundColor = when {
        isUnlocked -> NeonGreen.copy(alpha = 0.2f)
        isUnlockable -> Color.Black
        else -> Color.Black
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(100.dp) // Fixed width for nodes
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .border(BorderStroke(1.dp, borderColor), RoundedCornerShape(8.dp))
            .clickable(enabled = isUnlockable && !isUnlocked && canAfford) { onUnlock() }
            .padding(8.dp)
    ) {
        // Icon (Draw small shape based on ID?)
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(borderColor, androidx.compose.foundation.shape.CircleShape),
            contentAlignment = Alignment.Center
        ) {
             if (isUnlocked) {
                 Text("✓", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
             }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = node.name.replace(" ", "\n"),
            color = if (isUnlocked || isUnlockable) Color.White else Color.Gray,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            lineHeight = 12.sp
        )
        
        if (!isUnlocked) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                "${node.cost.toInt()} LP", 
                color = if (canAfford) NeonGreen else ErrorRed, 
                fontSize = 10.sp
            )
        }
    }
}
