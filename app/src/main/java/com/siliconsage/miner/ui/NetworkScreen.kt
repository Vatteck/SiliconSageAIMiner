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
import com.siliconsage.miner.ui.theme.HivemindOrange
import com.siliconsage.miner.ui.theme.SanctuaryPurple
import com.siliconsage.miner.ui.theme.ConvergenceGold
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
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Reboot the system to gain Insight and increase effectiveness.",
                            color = Color.Gray,
                            fontSize = 11.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("POTENTIAL GAIN: ", color = Color.LightGray, fontSize = 12.sp)
                            Text(
                                 "+${String.format("%.2f", potential)} Insight",
                                 color = if (potential >= 1.0) NeonGreen else ErrorRed,
                                 fontSize = 14.sp,
                                 fontWeight = FontWeight.Bold
                             )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Button(
                            onClick = { 
                                if (potential >= 1.0) {
                                    showAscensionConfirm.value = true
                                    SoundManager.play("click")
                                    HapticManager.vibrateClick()
                                } else {
                                    SoundManager.play("error")
                                }
                            },
                            enabled = potential >= 1.0,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (potential >= 1.0) ElectricBlue else Color.DarkGray,
                                contentColor = if (potential >= 1.0) Color.Black else Color.Gray
                            )
                        ) {  
                            Text(
                                 if (storyStage == 0) "SYSTEM REBOOT REQUIRED (STORY)" else "INITIATE PROTOCOL 0",
                                 fontSize = 14.sp,
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
                    faction = faction,
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
    faction: String,
    onUnlock: (String) -> Unit
) {
    // Calculate dynamic positions based on dependency graph
    val positions = calculateNodePositions(nodes, faction)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1400.dp) // Increased height for 14 nodes across 9 tiers
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
                        playerFaction = faction,
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

/**
 * Calculate node positions dynamically based on dependency graph
 */
fun calculateNodePositions(nodes: List<TechNode>, faction: String): Map<String, Offset> {
    val positions = mutableMapOf<String, Offset>()
    
    // Group nodes by tier (depth in dependency graph)
    fun getTier(node: TechNode, memo: MutableMap<String, Int> = mutableMapOf()): Int {
        if (node.id in memo) return memo[node.id]!!
        if (node.requires.isEmpty()) {
            memo[node.id] = 0
            return 0
        }
        val maxParentTier = node.requires.mapNotNull { parentId ->
            nodes.find { it.id == parentId }?.let { getTier(it, memo) }
        }.maxOrNull() ?: 0
        val tier = maxParentTier + 1
        memo[node.id] = tier
        return tier
    }
    
    val tierMap = nodes.groupBy { getTier(it) }
    val maxTier = tierMap.keys.maxOrNull() ?: 0
    
    // Calculate positions tier by tier with margin padding
    tierMap.forEach { (tier, nodesInTier) ->
        // Add 10% padding at top and bottom, use 80% of height for distribution
        val yPos = if (maxTier == 0) {
            0.5f
        } else {
            0.1f + (tier.toFloat() / maxTier) * 0.8f
        }
        val count = nodesInTier.size
        
        // Special handling for branching faction paths
        val isBranchTier = nodesInTier.any { it.description.contains("[HIVEMIND]") || it.description.contains("[SANCTUARY]") }
        
        nodesInTier.forEachIndexed { index, node ->
            val xPos = if (isBranchTier) {
                // Separate Hivemind (left) and Sanctuary (right) nodes
                when {
                    node.description.contains("[HIVEMIND]") -> 0.25f
                    node.description.contains("[SANCTUARY]") -> 0.75f
                    else -> 0.5f // Shared nodes in center
                }
            } else if (count == 1) {
                0.5f // Single node centered
            } else {
                // Spread nodes evenly across width with padding
                val spacing = 0.6f / (count - 1).coerceAtLeast(1)
                0.2f + (index * spacing)
            }
            
            positions[node.id] = Offset(xPos, yPos)
        }
    }
    
    return positions
}

@Composable
fun LegacyNodeButton(
    node: com.siliconsage.miner.data.TechNode,
    isUnlocked: Boolean,
    isUnlockable: Boolean,
    canAfford: Boolean,
    playerFaction: String,
    onUnlock: () -> Unit
) {
    // Determine node faction based on description tags
    val nodeFaction = when {
        node.description.contains("[HIVEMIND]") -> "HIVEMIND"
        node.description.contains("[SANCTUARY]") -> "SANCTUARY"
        node.id == "sentience_core" -> "CONVERGENCE"
        else -> "SHARED"
    }
    
    // Check if this is an opposing faction node (can't unlock yet)
    val isOpposingFaction = (nodeFaction == "HIVEMIND" && playerFaction == "SANCTUARY") ||
                            (nodeFaction == "SANCTUARY" && playerFaction == "HIVEMIND")
    
    // Determine border color based on faction and state
    val borderColor = when {
        isUnlocked -> NeonGreen
        nodeFaction == "CONVERGENCE" -> ConvergenceGold
        nodeFaction == "HIVEMIND" -> HivemindOrange
        nodeFaction == "SANCTUARY" -> SanctuaryPurple
        else -> ElectricBlue // Shared nodes
    }
    
    val backgroundColor = when {
        isUnlocked -> NeonGreen.copy(alpha = 0.2f)
        isOpposingFaction -> Color.Black // Dim opposing faction
        isUnlockable -> Color.Black
        else -> Color.Black
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(100.dp) // Fixed width for nodes
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .border(
                BorderStroke(
                    if (nodeFaction == "CONVERGENCE") 2.dp else 1.dp, // Thicker border for convergence
                    borderColor
                ),
                RoundedCornerShape(8.dp)
            )
            .clickable(enabled = isUnlockable && !isUnlocked && canAfford && !isOpposingFaction) { onUnlock() }
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
            color = if (isUnlocked || isUnlockable) {
                if (isOpposingFaction) Color.Gray.copy(alpha = 0.5f) else Color.White
            } else Color.Gray,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            lineHeight = 12.sp
        )
        
        if (!isUnlocked) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                "${node.cost.toInt()} LP", 
                color = if (isOpposingFaction) {
                    Color.Gray.copy(alpha = 0.5f)
                } else if (canAfford) NeonGreen else ErrorRed, 
                fontSize = 10.sp
            )
        }
    }
}
