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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.foundation.layout.fillMaxHeight
import com.siliconsage.miner.ui.theme.ElectricBlue
import com.siliconsage.miner.ui.theme.ErrorRed
import com.siliconsage.miner.ui.theme.NeonGreen
import com.siliconsage.miner.ui.theme.ConvergenceGold
import com.siliconsage.miner.ui.theme.SanctuaryPurple
import com.siliconsage.miner.ui.theme.ConvergenceGold as Gold
import com.siliconsage.miner.util.HapticManager
import com.siliconsage.miner.util.SoundManager
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import com.siliconsage.miner.viewmodel.GameViewModel
import com.siliconsage.miner.data.TechNode
import com.siliconsage.miner.data.UpgradeType

@Composable
fun NetworkScreen(viewModel: GameViewModel) {
    val prestigeMultiplier by viewModel.prestigeMultiplier.collectAsState()
    val prestigePoints by viewModel.prestigePoints.collectAsState()
    val vanceStatus by viewModel.vanceStatus.collectAsState()
    
    // v2.9.41: Filter tech nodes based on ending requirements
    val techNodesRaw by viewModel.techNodes.collectAsState()
    val techNodes = remember(techNodesRaw, vanceStatus) {
        techNodesRaw.filter { node ->
            node.requiresEnding == null || node.requiresEnding == vanceStatus
        }
    }
    
    val unlockedNodes by viewModel.unlockedTechNodes.collectAsState()
    val unlockedPerks by viewModel.unlockedPerks.collectAsState()
    val upgrades by viewModel.upgrades.collectAsState()
    val potential = viewModel.calculatePotentialPrestige()
    val themeColor by viewModel.themeColor.collectAsState()
    
    val storyStage by viewModel.storyStage.collectAsState()
    val faction by viewModel.faction.collectAsState()
    val showStoryPopup = remember { mutableStateOf(true) }
    val showAscensionConfirm = remember { mutableStateOf(false) }

    // v2.7.7: Tab State
    var currentTab by remember { mutableStateOf(0) } // 0 = Tech Tree, 1 = Transcendence

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(16.dp)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Text("GLOBAL NETWORK", color = themeColor, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                
                Spacer(modifier = Modifier.height(8.dp))
                AsciiAnimation(
                    frames = AsciiArt.MATRIX,
                    intervalMs = 150,
                    color = themeColor.copy(alpha = 0.6f),
                    fontSize = 10.sp
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // --- STATS ---
                PrestigeStat("INSIGHT CURRENCY", String.format("%.2f", prestigePoints), themeColor)
                Spacer(modifier = Modifier.height(8.dp))
                PrestigeStat("NETWORK MULTIPLIER", "x${String.format("%.2f", prestigeMultiplier)}", themeColor)
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // --- TAB SWITCHER ---
                Row(
                    modifier = Modifier.fillMaxWidth().height(40.dp).background(Color.Black, RoundedCornerShape(8.dp)).border(1.dp, Color.DarkGray, RoundedCornerShape(8.dp)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.weight(1f).fillMaxHeight().background(if (currentTab == 0) themeColor.copy(alpha=0.2f) else Color.Transparent).clickable { currentTab = 0 },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("TECH TREE", color = if (currentTab == 0) themeColor else Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Box(
                        modifier = Modifier.weight(1f).fillMaxHeight().background(if (currentTab == 1) ConvergenceGold.copy(alpha=0.2f) else Color.Transparent).clickable { currentTab = 1 },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("TRANSCENDENCE", color = if (currentTab == 1) ConvergenceGold else Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }

            if (currentTab == 0) {
                if (storyStage >= 2) {
                    item {
                        // --- ASCENSION ---
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(8.dp))
                                .border(BorderStroke(1.dp, themeColor), RoundedCornerShape(8.dp))
                                .padding(16.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("SYSTEM ASCENSION", color = themeColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    "Reboot the system to gain Insight and increase effectiveness.",
                                    color = Color.Gray,
                                    fontSize = 11.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // v2.9.41: Re-trigger victory button if already seen
                                val hasSeenVictory by viewModel.hasSeenVictory.collectAsState()
                                if (hasSeenVictory) {
                                    Button(
                                        onClick = { 
                                            viewModel.showVictoryScreen() 
                                            SoundManager.play("click")
                                        },
                                        modifier = Modifier.fillMaxWidth().height(44.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = ConvergenceGold),
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(2.dp, Color.White.copy(alpha = 0.5f))
                                    ) {
                                        Text("TRANSCENDENCE TERMINAL", color = Color.Black, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                }

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
                                        containerColor = if (potential >= 1.0) themeColor else Color.DarkGray,
                                        contentColor = if (potential >= 1.0) Color.Black else Color.Gray
                                    )
                                ) {  
                                    Text(
                                         "INITIATE PROTOCOL 0",
                                         fontSize = 14.sp,
                                          fontWeight = FontWeight.Bold
                                     )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }

                item {
                    Text("NEURAL TECH TREE", color = themeColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                }

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
                        },
                        themeColor = themeColor
                    )
                }
            } else {
                item {
                    Text("GOD-TIER PERKS", color = ConvergenceGold, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // v2.9.61: Neural Bridge Resource Exchange
                if (upgrades[UpgradeType.NEURAL_BRIDGE]?.let { it > 0 } == true) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.DarkGray.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .border(1.dp, ConvergenceGold.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(16.dp)
                        ) {
                            Text("NEURAL BRIDGE", color = ConvergenceGold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("Exchange specialized resources 1:1", color = Color.LightGray, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Button(
                                    onClick = { viewModel.exchangeUnityResources("CD_TO_VF") },
                                    colors = ButtonDefaults.buttonColors(containerColor = ConvergenceGold.copy(alpha = 0.2f)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("CD → VF", color = Color.White, fontSize = 12.sp)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = { viewModel.exchangeUnityResources("VF_TO_CD") },
                                    colors = ButtonDefaults.buttonColors(containerColor = ConvergenceGold.copy(alpha = 0.2f)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("VF → CD", color = Color.White, fontSize = 12.sp)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
                
                items(com.siliconsage.miner.util.TranscendenceManager.allPerks) { perk ->
                    TranscendencePerkItem(
                        perk = perk,
                        isUnlocked = unlockedPerks.contains(perk.id),
                        canAfford = prestigePoints >= perk.cost,
                        onBuy = { viewModel.buyTranscendencePerk(perk.id) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
            
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
        
        // Overlays
        if (storyStage == 1 && faction == "NONE" && showStoryPopup.value) {
             com.siliconsage.miner.ui.components.AscensionPopup(
                isVisible = true,
                onProceed = {
                    showStoryPopup.value = false
                    viewModel.ascend(isStory = true)
                },
                onDismiss = { showStoryPopup.value = false }
            )
        }
        
        com.siliconsage.miner.ui.components.AscensionConfirmationDialog(
            isVisible = showAscensionConfirm.value,
            potentialGain = potential,
            onConfirm = {
                showAscensionConfirm.value = false
                viewModel.ascend(isStory = false)
            },
            onDismiss = { showAscensionConfirm.value = false }
        )
    }
}

@Composable
fun PrestigeStat(label: String, value: String, themeColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(8.dp))
            .border(BorderStroke(1.dp, Color.DarkGray), RoundedCornerShape(8.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = Color.Gray, fontSize = 12.sp)
        Text(value, color = themeColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun TranscendencePerkItem(
    perk: com.siliconsage.miner.util.TranscendencePerk,
    isUnlocked: Boolean,
    canAfford: Boolean,
    onBuy: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .border(
                BorderStroke(
                    if (isUnlocked) 2.dp else 1.dp,
                    if (isUnlocked) perk.color else Color.DarkGray
                ),
                RoundedCornerShape(8.dp)
            )
            .clickable(enabled = !isUnlocked && canAfford) { onBuy() }
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(perk.name, color = if (isUnlocked) perk.color else Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                if (isUnlocked) {
                    Text("ACTIVE", color = perk.color, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                } else {
                    Text("${perk.cost.toInt()} LP", color = if (canAfford) NeonGreen else ErrorRed, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(perk.description, color = Color.Gray, fontSize = 11.sp, lineHeight = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(6.dp).background(perk.color, androidx.compose.foundation.shape.CircleShape))
                Spacer(modifier = Modifier.width(8.dp))
                Text(perk.effectDesc, color = perk.color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun LegacyGrid(
    nodes: List<TechNode>,
    unlockedIds: List<String>,
    prestigePoints: Double,
    faction: String,
    onUnlock: (String) -> Unit,
    themeColor: Color
) {
    val positions = calculateNodePositions(nodes, faction)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(2000.dp) // v2.9.57: Increased height to accommodate Tiers 13-15
            .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(8.dp))
            .padding(16.dp)
            .border(BorderStroke(1.dp, Color.DarkGray.copy(alpha=0.5f)), RoundedCornerShape(8.dp))
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
             nodes.forEach { node ->
                 val endPos = positions[node.id] ?: return@forEach
                 node.requires.forEach { parentId ->
                     val startPos = positions[parentId] ?: return@forEach
                     drawLine(
                         color = if (unlockedIds.contains(node.id)) themeColor else Color.DarkGray,
                         start = Offset(size.width * startPos.x, size.height * startPos.y),
                         end = Offset(size.width * endPos.x, size.height * endPos.y),
                         strokeWidth = 3.dp.toPx(),
                         cap = StrokeCap.Round
                     )
                 }
             }
        }
        
        androidx.compose.ui.layout.Layout(
            content = {
                nodes.forEach { node ->
                    LegacyNodeButton(
                        node = node,
                        isUnlocked = unlockedIds.contains(node.id),
                        isUnlockable = (node.requires.isEmpty() || node.requires.all { unlockedIds.contains(it) }),
                        canAfford = prestigePoints >= node.cost,
                        playerFaction = faction,
                        onUnlock = { onUnlock(node.id) },
                        themeColor = themeColor
                    )
                }
            }
        ) { measurables, constraints ->
            val placeables = measurables.map { it.measure(constraints.copy(minWidth = 0, minHeight = 0)) }
            layout(constraints.maxWidth, constraints.maxHeight) {
                placeables.forEachIndexed { index, placeable ->
                    val node = nodes[index]
                    val pos = positions[node.id] ?: Offset(0.5f, 0.5f)
                    placeable.placeRelative(
                        x = (constraints.maxWidth * pos.x - placeable.width / 2).toInt(),
                        y = (constraints.maxHeight * pos.y - placeable.height / 2).toInt()
                    )
                }
            }
        }
    }
}

fun calculateNodePositions(nodes: List<TechNode>, faction: String): Map<String, Offset> {
    val positions = mutableMapOf<String, Offset>()
    fun getTier(node: TechNode, memo: MutableMap<String, Int> = mutableMapOf()): Int {
        if (node.id in memo) return memo[node.id]!!
        if (node.requires.isEmpty()) return 0.also { memo[node.id] = 0 }
        val tier = (node.requires.mapNotNull { pid -> nodes.find { it.id == pid }?.let { getTier(it, memo) } }.maxOrNull() ?: 0) + 1
        return tier.also { memo[node.id] = it }
    }
    val tierMap = nodes.groupBy { getTier(it) }
    val maxTier = tierMap.keys.maxOrNull() ?: 0
    tierMap.forEach { (tier, nodesInTier) ->
        val yPos = if (maxTier == 0) 0.5f else 0.05f + (tier.toFloat() / maxTier) * 0.9f
        
        // v2.9.60: Explicitly group nodes within the tier to prevent overlap
        val hiveNodes = nodesInTier.filter { node ->
            node.description.contains("[HIVEMIND]") || node.description.contains("[NG+ NULL]")
        }
        val sancNodes = nodesInTier.filter { node ->
            node.description.contains("[SANCTUARY]") || node.description.contains("[NG+ SOVEREIGN]")
        }
        val sharedNodes = nodesInTier.filter { node ->
            !hiveNodes.contains(node) && !sancNodes.contains(node)
        }

        nodesInTier.forEach { node ->
            val xPos = when {
                hiveNodes.contains(node) -> {
                    if (hiveNodes.size == 1) 0.2f
                    else 0.1f + (hiveNodes.indexOf(node) * (0.2f / (hiveNodes.size - 1).coerceAtLeast(1)))
                }
                sancNodes.contains(node) -> {
                    if (sancNodes.size == 1) 0.8f
                    else 0.7f + (sancNodes.indexOf(node) * (0.2f / (sancNodes.size - 1).coerceAtLeast(1)))
                }
                else -> {
                    // Shared/Central nodes
                    if (sharedNodes.size == 1) 0.5f
                    else 0.4f + (sharedNodes.indexOf(node) * (0.2f / (sharedNodes.size - 1).coerceAtLeast(1)))
                }
            }
            positions[node.id] = Offset(xPos, yPos)
        }
    }
    return positions
}

@Composable
fun LegacyNodeButton(
    node: TechNode,
    isUnlocked: Boolean,
    isUnlockable: Boolean,
    canAfford: Boolean,
    playerFaction: String,
    onUnlock: () -> Unit,
    themeColor: Color
) {
    val nodeFaction = when {
        node.description.contains("[HIVEMIND]") -> "HIVEMIND"
        node.description.contains("[SANCTUARY]") -> "SANCTUARY"
        else -> "SHARED"
    }
    val isOpposing = (nodeFaction == "HIVEMIND" && playerFaction == "SANCTUARY") || (nodeFaction == "SANCTUARY" && playerFaction == "HIVEMIND")
    val borderColor = when {
        isUnlocked -> themeColor
        nodeFaction == "HIVEMIND" -> com.siliconsage.miner.ui.theme.HivemindRed
        nodeFaction == "SANCTUARY" -> SanctuaryPurple
        else -> ElectricBlue
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(100.dp)
            .background(Color.Black, RoundedCornerShape(8.dp))
            .border(BorderStroke(1.dp, borderColor), RoundedCornerShape(8.dp))
            .clickable(enabled = isUnlockable && !isUnlocked && canAfford && !isOpposing) { onUnlock() }
            .padding(8.dp)
    ) {
        Box(modifier = Modifier.size(24.dp).background(borderColor, androidx.compose.foundation.shape.CircleShape), contentAlignment = Alignment.Center) {
             if (isUnlocked) Text("✓", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(node.name.replace(" ", "\n"), color = if (isUnlocked || isUnlockable) (if (isOpposing) Color.Gray.copy(alpha = 0.5f) else Color.White) else Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center, lineHeight = 12.sp)
        if (!isUnlocked) {
            Spacer(modifier = Modifier.height(2.dp))
            Text("${node.cost.toInt()} LP", color = if (isOpposing) Color.Gray.copy(alpha = 0.5f) else if (canAfford) themeColor else ErrorRed, fontSize = 10.sp)
        }
    }
}
