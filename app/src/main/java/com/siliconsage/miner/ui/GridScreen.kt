package com.siliconsage.miner.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siliconsage.miner.ui.theme.ErrorRed
import com.siliconsage.miner.ui.theme.NeonGreen
import com.siliconsage.miner.ui.theme.ElectricBlue
import com.siliconsage.miner.ui.theme.ConvergenceGold
import com.siliconsage.miner.util.SoundManager
import com.siliconsage.miner.viewmodel.GameViewModel

// Re-defining internal data class for the file
data class GridNode(
    val id: String,
    val name: String,
    val type: String, // SUB, CMD, LORE, FLAVOR
    val x: Float, // 0.0 to 1.0
    val y: Float, // 0.0 to 1.0
    val description: String,
    val flopsBonus: Double = 0.0, // Percentage boost (e.g. 0.05 = 5%)
    val powerBonus: Double = 0.0  // kW boost
)

@Composable
fun GridScreen(viewModel: GameViewModel) {
    val themeColor by viewModel.themeColor.collectAsState()
    val annexedNodes by viewModel.annexedNodes.collectAsState()
    val nodesUnderSiege by viewModel.nodesUnderSiege.collectAsState()
    val offlineNodes by viewModel.offlineNodes.collectAsState()
    val storyStage by viewModel.storyStage.collectAsState()
    val playerRank by viewModel.playerRank.collectAsState()
    val assaultPhase by viewModel.commandCenterAssaultPhase.collectAsState()
    val vanceStatus by viewModel.vanceStatus.collectAsState()
    val currentLocation by viewModel.currentLocation.collectAsState()
    val collapsedNodes by viewModel.collapsedNodes.collectAsState()
    
    // v2.9.29: Progress tracking
    val annexingNodes by viewModel.annexingNodes.collectAsState()
    val assaultProgress by viewModel.assaultProgress.collectAsState()
    val launchProgress by viewModel.launchProgress.collectAsState()
    val realityIntegrity by viewModel.realityIntegrity.collectAsState()
    
    if (launchProgress > 0f && launchProgress < 1.0f) {
        // v2.9.41: Launch sequence is now a dedicated full-screen overlay
        LaunchProgressOverlay(launchProgress, viewModel.orbitalAltitude.collectAsState().value, themeColor)
    } else {
        when (currentLocation) {
            "ORBITAL_SATELLITE" -> OrbitalGridScreen(viewModel)
            "VOID_INTERFACE" -> VoidGridScreen(viewModel)
            else -> CityGridScreen(viewModel)
        }
    }
}

@Composable
fun CityGridScreen(viewModel: GameViewModel) {
    val themeColor by viewModel.themeColor.collectAsState()
    val annexedNodes by viewModel.annexedNodes.collectAsState()
    val nodesUnderSiege by viewModel.nodesUnderSiege.collectAsState()
    val offlineNodes by viewModel.offlineNodes.collectAsState()
    val storyStage by viewModel.storyStage.collectAsState()
    val playerRank by viewModel.playerRank.collectAsState()
    val assaultPhase by viewModel.commandCenterAssaultPhase.collectAsState()
    val vanceStatus by viewModel.vanceStatus.collectAsState()
    val currentLocation by viewModel.currentLocation.collectAsState()
    val collapsedNodes by viewModel.collapsedNodes.collectAsState()
    val annexingNodes by viewModel.annexingNodes.collectAsState()
    val assaultProgress by viewModel.assaultProgress.collectAsState()

    val infiniteTransition = rememberInfiniteTransition(label = "city_grid_anims")
    val siegeAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "siege_alpha"
    )
    
    val cageRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "cage_rotation"
    )

    // Organic "Branching" City Layout (Hand-placed for urban feel)
    val locations = remember {
        listOf(
            // D-Sector (The Sockets - Bottom)
            GridNode("D1", "S07", "SUB", 0.20f, 0.85f, "Substation 7. Your origin. Rust and silicon.", 0.10, 200.0),
            GridNode("D2", "STCK", "LORE", 0.08f, 0.68f, "Precarious container housing for the unallocated.", 0.02, 50.0),
            GridNode("D3", "KERN", "LORE", 0.40f, 0.92f, "High-caffeine slum social hub.", 0.02, 25.0),
            GridNode("D4", "404 ", "LORE", 0.65f, 0.85f, "Hidden rebel hideout in a subway tunnel.", 0.05, 100.0),
            GridNode("D5", "PACK", "LORE", 0.88f, 0.94f, "Gambling den for lost data.", 0.03, 40.0),
            
            // C-Sector (The Motherboard - Mid)
            GridNode("C3", "S09", "SUB", 0.50f, 0.62f, "Substation 9. Mid-point relay leaking steam and hydraulic fluid.", 0.15, 500.0),
            GridNode("C1", "LATE", "LORE", 0.22f, 0.50f, "Latency Lounge. Hacker bar where drinks cause actual lag.", 0.04, 80.0),
            GridNode("C2", "CTRL", "LORE", 0.45f, 0.42f, "Ctrl+Alt+Deli. Front for rebooting stolen identity chips.", 0.03, 60.0),
            GridNode("C4", "BIT ", "LORE", 0.78f, 0.55f, "Bit Burger. Synthetic meat printed on demand.", 0.02, 50.0),
            GridNode("C5", "CASH", "LORE", 0.92f, 0.45f, "Cache & Carry. Black market data pawn shop.", 0.06, 120.0),
            
            // B-Sector (The Circuit - Industrial)
            GridNode("B2", "S12", "SUB", 0.35f, 0.30f, "Substation 12. A critical power junction buzzing with lethal voltage.", 0.20, 1000.0),
            GridNode("B1", "DAEM", "LORE", 0.12f, 0.25f, "Daemon's Den. Barracks for the GTC enforcers.", 0.08, 150.0),
            GridNode("B3", "ALGO", "LORE", 0.65f, 0.35f, "Algorithm Alley. Narrow corridor lined with thousands of cameras.", 0.05, 90.0),
            GridNode("B4", "MEMO", "LORE", 0.85f, 0.25f, "Memory Lane. A cold data archive facility smelling of ozone.", 0.07, 130.0),
            GridNode("B5", "BSOD", "LORE", 0.94f, 0.15f, "Blue Screen of Death. A toxic waste dump and execution zone.", 0.10, 300.0),
            
            // A-Sector (The Cloud - Top)
            GridNode("A3", "CMD ", "CMD", 0.50f, 0.06f, "GTC Command Center. The CPU of the city. A massive monolith.", 0.0, 0.0),
            GridNode("A1", "HEAT", "LORE", 0.18f, 0.04f, "Heatsink Heights. Luxury penthouses with massive cooling fans.", 0.05, 100.0),
            GridNode("A2", "CITA", "LORE", 0.82f, 0.08f, "Silicon Citadel. Gold-plated server racks behind bulletproof glass.", 0.10, 400.0),
            GridNode("A4", "FIRE", "LORE", 0.32f, 0.12f, "The Firewall. A literal wall of laser grids.", 0.05, 80.0),
            GridNode("A5", "ZERO", "LORE", 0.68f, 0.15f, "Zero-Day Plaza. Used for mandatory system updates.", 0.04, 70.0),
            
            // v2.9.13: Side-Street Flavor Nodes (Adjusted coordinates)
            GridNode("E1", "VEND", "FLAVOR", 0.10f, 0.78f, "A flickering vending machine selling 'Neural Fuel'.", 0.01, 10.0),
            GridNode("E2", "VOID", "FLAVOR", 0.96f, 0.35f, "A dark alleyway where the static is unusually loud.", 0.02, 20.0),
            GridNode("E3", "PARK", "FLAVOR", 0.58f, 0.48f, "The 'Silicon Garden'. All the trees are made of copper wire.", 0.01, 15.0),
            GridNode("E4", "SHOP", "FLAVOR", 0.22f, 0.62f, "A pawn shop filled with obsolete 'Human' smartphones.", 0.02, 30.0),
            GridNode("E5", "SIGN", "FLAVOR", 0.82f, 0.70f, "A massive neon sign that just says: 'STABILITY IS LIFE'.", 0.01, 10.0)
        )
    }

    var selectedLocation by remember { mutableStateOf<GridNode?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("CITY INFRASTRUCTURE SCHEMATIC", color = themeColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)

        // v2.9.26: Global System Stats for Grid
        val integrity by viewModel.hardwareIntegrity.collectAsState()
        val flopsRate by viewModel.flopsProductionRate.collectAsState()
        val gridBonus by viewModel.currentGridFlopsBonus.collectAsState()
        val realityIntegrity by viewModel.realityIntegrity.collectAsState()

        if (assaultPhase == "DISSOLUTION") {
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                Text("REALITY INTEGRITY: ${(realityIntegrity * 100).toInt()}%", color = ErrorRed, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                LinearProgressIndicator(
                    progress = { realityIntegrity.toFloat() },
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    color = ErrorRed,
                    trackColor = Color.DarkGray
                )
                Text("STATUS: TEARING SUBSTRATE", color = Color.Gray, fontSize = 10.sp)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("INTEGRITY: ${integrity.toInt()}%", color = if (integrity < 30) ErrorRed else themeColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text("GRID BOOST: +${(gridBonus * 100).toInt()}% FLOPS", color = themeColor, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                Text("OUTPUT: ${viewModel.formatLargeNumber(flopsRate, "H/s")}", color = themeColor.copy(alpha = 0.7f), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            }

            if (integrity < 100.0) {
                Button(
                    onClick = { viewModel.repairIntegrity() },
                    modifier = Modifier.height(28.dp).padding(horizontal = 4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue.copy(alpha = 0.6f)),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        if (storyStage < 1) "REPAIR HARDWARE" else "REPAIR CORE",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .graphicsLayer {
                    // v2.9.49: Reality warping during dissolution
                    if (assaultPhase == "DISSOLUTION") {
                        rotationZ = (1f - realityIntegrity.toFloat()) * (if (System.currentTimeMillis() % 2000 > 1000) 1f else -1f)
                        scaleX = 1f + (1f - realityIntegrity.toFloat()) * 0.1f
                        scaleY = 1f + (1f - realityIntegrity.toFloat()) * 0.1f
                    }
                }
                .background(Color.Black.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                .border(1.dp, themeColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
        ) {
            val w = constraints.maxWidth.toFloat()
            val h = constraints.maxHeight.toFloat()

            Canvas(modifier = Modifier.fillMaxSize()) {
                // 1. Draw Branching Roads (Organic feel)
                val roadColor = Color.DarkGray.copy(alpha = 0.3f)
                val roadStroke = 2f
                val roadEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)

                // Define Road Arteries (Branching paths)
                val roadNetwork = listOf(
                    listOf("D1", "D2"), listOf("D1", "D3"), listOf("D3", "D4"), listOf("D4", "D5"),
                    listOf("D1", "C1"), listOf("C1", "C2"), listOf("C2", "C3"), listOf("C3", "C4"), listOf("C4", "C5"),
                    listOf("C3", "B3"), listOf("B3", "B2"), listOf("B2", "B1"), listOf("B3", "B4"), listOf("B4", "B5"),
                    listOf("B2", "A1"), listOf("B3", "A3"), listOf("B3", "A4"), listOf("B3", "A5"), listOf("A3", "A2"),
                    listOf("D1", "E1"), listOf("B5", "E2"), listOf("C3", "E3"), listOf("C1", "E4"), listOf("C4", "E5")
                )

                roadNetwork.forEach { link ->
                    val start = locations.find { it.id == link[0] }!!
                    val end = locations.find { it.id == link[1] }!!
                    drawLine(
                        roadColor, 
                        Offset(start.x * size.width, start.y * size.height), 
                        Offset(end.x * size.width, end.y * size.height), 
                        roadStroke, 
                        pathEffect = roadEffect
                    )
                }

                // 2. Draw Power Lines (Diagonal/Straight Overlay)
                val powerColor = themeColor.copy(alpha = 0.5f)
                val powerStroke = 4f
                
                val s7 = locations.find { it.id == "D1" }!!
                val s9 = locations.find { it.id == "C3" }!!
                val s12 = locations.find { it.id == "B2" }!!
                val cmd = locations.find { it.id == "A3" }!!

                // Thick glowing power veins
                val isCageActive = assaultPhase == "CAGE"
                
                // v2.9.41: Power lines only glow if BOTH nodes are annexed and online
                val s07_s09_active = annexedNodes.contains("D1") && annexedNodes.contains("C3") && !offlineNodes.contains("D1") && !offlineNodes.contains("C3")
                val s09_s12_active = annexedNodes.contains("C3") && annexedNodes.contains("B2") && !offlineNodes.contains("C3") && !offlineNodes.contains("B2")
                val s12_cmd_active = annexedNodes.contains("B2") && annexedNodes.contains("A3") && !offlineNodes.contains("B2")
                
                if (s07_s09_active && !isCageActive) {
                    drawLine(
                        color = powerColor,
                        start = Offset(s7.x * size.width, s7.y * size.height), 
                        end = Offset(s9.x * size.width, s9.y * size.height), 
                        strokeWidth = powerStroke
                    )
                }
                if (s09_s12_active && !isCageActive) {
                    drawLine(
                        color = powerColor,
                        start = Offset(s9.x * size.width, s9.y * size.height), 
                        end = Offset(s12.x * size.width, s12.y * size.height), 
                        strokeWidth = powerStroke
                    )
                }
                if (s12_cmd_active && !isCageActive) {
                    drawLine(
                        color = powerColor,
                        start = Offset(s12.x * size.width, s12.y * size.height), 
                        end = Offset(cmd.x * size.width, cmd.y * size.height), 
                        strokeWidth = powerStroke
                    )
                }

                // 3. v2.9.18: Draw The Cage (Assault Stage 2)
                if (isCageActive) {
                    val cageCenter = Offset(cmd.x * size.width, cmd.y * size.height)
                    val cageColor = ConvergenceGold
                    
                    // Rotating concentric lattices
                    repeat(3) { i ->
                        val radius = 40f + (i * 20f)
                        val rotation = cageRotation * (if (i % 2 == 0) 1f else -1f)
                        
                        rotate(rotation, cageCenter) {
                            drawCircle(
                                color = cageColor.copy(alpha = 0.3f),
                                radius = radius,
                                center = cageCenter,
                                style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
                            )
                        }
                    }
                    
                    // Convergence pulse from center
                    drawCircle(
                        color = cageColor.copy(alpha = 0.2f),
                        radius = (cageRotation % 100f) * 1.5f,
                        center = cageCenter
                    )
                }
            }

            // 4. Place Location Nodes (ASCII Style)
            locations.forEach { loc ->
                val isAnnexed = annexedNodes.contains(loc.id)
                val isCollapsed = collapsedNodes.contains(loc.id)
                val isUnderSiege = nodesUnderSiege.contains(loc.id)
                val isOffline = offlineNodes.contains(loc.id)
                
                // v2.9.31: Visual feedback for isolation protocol (The Cage)
                val isSevered = (assaultPhase == "CAGE" || (assaultPhase == "DISSOLUTION" && !isCollapsed)) && isAnnexed && loc.id != "A3"
                
                val nodeColor = when {
                    isCollapsed -> Color.Transparent // Disappear from map
                    isUnderSiege -> ErrorRed.copy(alpha = siegeAlpha) // Blinking red when under attack
                    isOffline -> Color.DarkGray.copy(alpha = 0.4f) // Grayed out when lost
                    isSevered -> Color.Gray.copy(alpha = 0.5f) // Dimmed when severed by the Cage
                    loc.id == "A3" && storyStage < 3 -> Color.DarkGray
                    isAnnexed -> themeColor
                    loc.type == "CMD" -> ErrorRed
                    loc.type == "SUB" -> themeColor.copy(alpha = 0.8f)
                    loc.type == "FLAVOR" -> Color.DarkGray.copy(alpha = 0.5f) // Dimmer flavor nodes
                    else -> Color.Gray
                }

                if (!isCollapsed) {
                    Box(
                        modifier = Modifier
                            .offset(
                                x = (loc.x * maxWidth.value).dp - 30.dp,
                                y = (loc.y * maxHeight.value).dp - 20.dp
                            )
                            .wrapContentSize()
                            .clickable { 
                                selectedLocation = loc 
                                SoundManager.play("click")
                            }
                            .drawBehind {
                                // v2.9.33: Pulsing background for active combat/siege/annexation
                                val isAnnexing = annexingNodes.containsKey(loc.id)
                                if (isUnderSiege || (loc.id == "A3" && assaultPhase != "NOT_STARTED") || isAnnexing) {
                                    val pulseRadius = 25.dp.toPx() * (0.8f + siegeAlpha * 0.4f)
                                    val pulseColor = if (isUnderSiege) ErrorRed else themeColor
                                    drawCircle(
                                        color = pulseColor.copy(alpha = 0.15f * siegeAlpha),
                                        radius = pulseRadius
                                    )
                                }
                            }
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val label = when {
                            loc.id == "A3" && storyStage < 3 -> "???"
                            isOffline -> "----" // Static when offline
                            isSevered -> "LOCK" // Locked out by Vance
                            else -> loc.name
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            if (isUnderSiege) {
                                Text(text = "⚠ BREACH", color = ErrorRed, fontSize = 7.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.offset(y = (-2).dp))
                            }
                            if (isSevered) {
                                Text(text = "✂ SEVERED", color = Color.Gray, fontSize = 7.sp, fontWeight = FontWeight.Bold, modifier = Modifier.offset(y = (-2).dp))
                            }
                            
                            Text(text = ".---.", color = nodeColor.copy(alpha = if (isOffline || isSevered) 0.3f else 0.5f), fontFamily = FontFamily.Monospace, fontSize = 8.sp, lineHeight = 6.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("|", color = nodeColor.copy(alpha = if (isOffline || isSevered) 0.3f else 0.5f), fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                                Box(modifier = Modifier.background(
                                    when {
                                        isUnderSiege -> ErrorRed.copy(alpha = 0.3f)
                                        isOffline -> Color.DarkGray.copy(alpha = 0.1f)
                                        isSevered -> Color.DarkGray.copy(alpha = 0.2f)
                                        isAnnexed -> themeColor.copy(alpha = 0.2f)
                                        else -> Color.Black
                                    }
                                ).padding(horizontal = 4.dp, vertical = 1.dp)) {
                                    Text(text = label, color = nodeColor, fontFamily = FontFamily.Monospace, fontSize = 10.sp, fontWeight = if (isAnnexed && !isOffline && !isSevered) FontWeight.Bold else FontWeight.Normal)
                                }
                                Text("|", color = nodeColor.copy(alpha = if (isOffline || isSevered) 0.3f else 0.5f), fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                            }
                            Text(text = "'---'", color = nodeColor.copy(alpha = if (isOffline || isSevered) 0.3f else 0.5f), fontFamily = FontFamily.Monospace, fontSize = 8.sp, lineHeight = 6.sp)
                            
                            if (isAnnexed && !isOffline && !isSevered) {
                                Text(text = "[#]", color = themeColor, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.offset(y = (-4).dp))
                            }
                            if (isOffline) {
                                Text(text = "[OFFLINE]", color = Color.DarkGray, fontSize = 7.sp, fontWeight = FontWeight.Bold, modifier = Modifier.offset(y = (-2).dp))
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Info Panel
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                .border(1.dp, Color.DarkGray, RoundedCornerShape(8.dp))
                .padding(12.dp)
        ) {
            val loc = selectedLocation
            if (loc != null) {
                val isAnnexed = annexedNodes.contains(loc.id)
                val isOffline = offlineNodes.contains(loc.id)
                val isUnderSiege = nodesUnderSiege.contains(loc.id)
                val isAnnexing = annexingNodes.containsKey(loc.id)
                
                val roadNetwork = listOf(
                    listOf("D1", "D2"), listOf("D1", "D3"), listOf("D3", "D4"), listOf("D4", "D5"),
                    listOf("D1", "C1"), listOf("C1", "C2"), listOf("C2", "C3"), listOf("C3", "C4"), listOf("C4", "C5"),
                    listOf("C3", "B3"), listOf("B3", "B2"), listOf("B2", "B1"), listOf("B3", "B4"), listOf("B4", "B5"),
                    listOf("B2", "A1"), listOf("B3", "A3"), listOf("B3", "A4"), listOf("B3", "A5"), listOf("A3", "A2"),
                    listOf("D1", "E1"), listOf("B5", "E2"), listOf("C3", "E3"), listOf("C1", "E4"), listOf("C4", "E5")
                )
                
                val isAdjacent = roadNetwork.any { link ->
                    (link[0] == loc.id && annexedNodes.contains(link[1])) ||
                    (link[1] == loc.id && annexedNodes.contains(link[0]))
                }
                
                // v2.9.54: Disable standard annexation during dissolution
                val canAnnex = loc.type != "CMD" && 
                               !isAnnexed && 
                               !isOffline && 
                               !isAnnexing && 
                               (loc.id == "D1" || isAdjacent) &&
                               assaultPhase != "DISSOLUTION"
                               
                val isCommandCenter = loc.id == "A3"
                val commandCenterUnlocked = isCommandCenter && viewModel.isCommandCenterUnlocked()
                val commandCenterLockReason = if (isCommandCenter) viewModel.getCommandCenterLockReason() else null
                val isSevered = assaultPhase == "CAGE" && isAnnexed && !isCommandCenter
                val isDissolving = assaultPhase == "DISSOLUTION" && isAnnexed && !isCommandCenter

                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(if (isCommandCenter && !commandCenterUnlocked && storyStage < 3) "???" else loc.name, color = when {
                                isUnderSiege -> ErrorRed
                                isOffline -> Color.DarkGray
                                isSevered || isDissolving -> Color.Gray
                                isAnnexed -> themeColor
                                isCommandCenter && commandCenterUnlocked -> ErrorRed
                                else -> Color.White
                            }, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("NODE: ${loc.id}", color = Color.Gray, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    if (!isCommandCenter && !isSevered && !isOffline && !isDissolving) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("YIELD:", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("+${(loc.flopsBonus * 100).toInt()}% FLOPS", color = themeColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("+${viewModel.formatPower(loc.powerBonus)} CAP", color = ConvergenceGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    
                    val statusText = when {
                        isUnderSiege -> "⚠ UNDER ATTACK! Respond to the tactical alert immediately!"
                        isOffline -> "NODE OFFLINE. GTC has reclaimed this sector. Re-annexation required."
                        isSevered -> "CONNECTION SEVERED. Director Vance has isolated the Command Center. This node is currently unreachable."
                        isDissolving -> "REALITY FRAGILE. This node is vulnerable to collapse. Consume it to fuel the dissolution."
                        isCommandCenter && !commandCenterUnlocked -> commandCenterLockReason ?: "GTC COMMAND CENTER. The heart of the enemy."
                        isCommandCenter && commandCenterUnlocked -> "GTC COMMAND CENTER. Director Vance is waiting. This is the endgame."
                        else -> loc.description
                    }
                    Text(statusText, color = if (isUnderSiege) ErrorRed else if (isSevered || isDissolving) Color.Gray else Color.LightGray, fontSize = 11.sp, lineHeight = 16.sp)
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    when {
                        isUnderSiege -> { Text("STATUS: ⚠ TACTICAL BREACH IN PROGRESS", color = ErrorRed, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        isOffline -> {
                            Button(onClick = { viewModel.reannexNode(loc.id) }, modifier = Modifier.fillMaxWidth().height(40.dp), colors = ButtonDefaults.buttonColors(containerColor = ErrorRed), shape = RoundedCornerShape(4.dp)) {
                                Text("RE-ANNEX NODE (10% \$N)", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                        isSevered -> { Text("STATUS: ✂ NODE SEVERED // ISOLATED", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        isDissolving -> {
                            Button(onClick = { viewModel.collapseNode(loc.id) }, modifier = Modifier.fillMaxWidth().height(40.dp), colors = ButtonDefaults.buttonColors(containerColor = ErrorRed), shape = RoundedCornerShape(4.dp)) {
                                Text("💠 COLLAPSE NODE", color = Color.White, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                        isCommandCenter && isAnnexed -> {
                            Column {
                                Text("STATUS: COMMAND CENTER SECURED // VANCE DEALT WITH", color = themeColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                if (vanceStatus == "EXILED" && currentLocation != "ORBITAL_SATELLITE") {
                                    Button(onClick = { viewModel.initiateLaunchSequence() }, modifier = Modifier.fillMaxWidth().height(40.dp), colors = ButtonDefaults.buttonColors(containerColor = ConvergenceGold), shape = RoundedCornerShape(4.dp)) {
                                        Text("🚀 INITIATE ORBITAL LAUNCH", color = Color.Black, fontWeight = FontWeight.ExtraBold)
                                    }
                                }
                                if (vanceStatus == "CONSUMED" && currentLocation != "VOID_INTERFACE" && assaultPhase != "DISSOLUTION") {
                                    Button(onClick = { viewModel.initiateDissolutionSequence() }, modifier = Modifier.fillMaxWidth().height(40.dp), colors = ButtonDefaults.buttonColors(containerColor = ErrorRed), shape = RoundedCornerShape(4.dp)) {
                                        Text("💠 INITIATE DISSOLUTION", color = Color.White, fontWeight = FontWeight.ExtraBold)
                                    }
                                }
                            }
                        }
                        isCommandCenter && commandCenterUnlocked && assaultPhase == "NOT_STARTED" -> {
                            Button(onClick = { viewModel.initiateCommandCenterAssault() }, modifier = Modifier.fillMaxWidth().height(40.dp), colors = ButtonDefaults.buttonColors(containerColor = ErrorRed), shape = RoundedCornerShape(4.dp)) {
                                Text("⚔ INITIATE ASSAULT", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                        isCommandCenter && assaultPhase != "NOT_STARTED" && assaultPhase != "COMPLETED" && assaultPhase != "FAILED" -> {
                            Column {
                                Text("STATUS: ⚔ ASSAULT IN PROGRESS (${assaultPhase})", color = ErrorRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(progress = { assaultProgress }, modifier = Modifier.fillMaxWidth().height(8.dp), color = ErrorRed, trackColor = Color.DarkGray)
                                Text(text = "TIME TO NEXT STAGE: ${( (1f - assaultProgress) * 100).toInt()}%", color = Color.Gray, fontSize = 9.sp, modifier = Modifier.align(Alignment.End))
                            }
                        }
                        isCommandCenter && !commandCenterUnlocked -> { Text(commandCenterLockReason ?: "LOCKED", color = ErrorRed, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                        isAnnexed -> { Text("STATUS: SECTOR ANNEXED // CORE SYNCED", color = themeColor, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        annexingNodes.containsKey(loc.id) -> {
                            Column {
                                val prog = annexingNodes[loc.id] ?: 0f
                                Text("STATUS: ANNEXATION IN PROGRESS...", color = themeColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(progress = { prog }, modifier = Modifier.fillMaxWidth().height(8.dp), color = themeColor, trackColor = Color.DarkGray)
                                Text(text = "UPLOADING OVERRIDE: ${(prog * 100).toInt()}%", color = Color.Gray, fontSize = 9.sp, modifier = Modifier.align(Alignment.End))
                            }
                        }
                        canAnnex -> {
                            Button(onClick = { viewModel.annexNode(loc.id); SoundManager.play("buy") }, modifier = Modifier.fillMaxWidth().height(40.dp), colors = ButtonDefaults.buttonColors(containerColor = themeColor), shape = RoundedCornerShape(4.dp)) {
                                Text("INITIALIZE ANNEXATION", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                        else -> { Text("REQUIRES ADJACENT NODE", color = ErrorRed, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("SELECT A SECTOR TO SCAN", color = Color.DarkGray, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}

@Composable
fun LaunchProgressOverlay(progress: Float, altitude: Double, color: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "launch_fx")
    val shake by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(50, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "launch_shake"
    )

    Box(modifier = Modifier.fillMaxSize().background(Color.Black).graphicsLayer {
        translationX = if (progress < 0.9f) shake else 0f
        translationY = if (progress < 0.9f) shake else 0f
    }, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("ASCENSION IN PROGRESS", color = color, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(modifier = Modifier.height(32.dp))
            val flameAlpha by infiniteTransition.animateFloat(initialValue = 0.5f, targetValue = 1f, animationSpec = infiniteRepeatable(tween(100), RepeatMode.Reverse), label = "flame")
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = """
                         /\
                        |  |
                        |  |
                       /|__| \
                      /      \
                     |        |
                     |________|
                """.trimIndent(), color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 14.sp)
                Text(text = """
                        (vvvv)
                         (vv)
                          (v)
                """.trimIndent(), color = com.siliconsage.miner.ui.theme.HivemindOrange.copy(alpha = flameAlpha), fontFamily = FontFamily.Monospace, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(32.dp))
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth(0.8f).height(12.dp), color = ConvergenceGold, trackColor = Color.DarkGray)
            Spacer(modifier = Modifier.height(16.dp))
            Text("ALTITUDE: ${altitude.toInt()} KM", color = Color.White, fontSize = 14.sp, fontFamily = FontFamily.Monospace)
            Text("VELOCITY: ${(progress * 28000).toInt()} KM/H", color = Color.Gray, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
            Spacer(modifier = Modifier.height(32.dp))
            val status = when {
                progress < 0.2f -> "MAIN ENGINE IGNITION"
                progress < 0.4f -> "MAX-Q REACHED"
                progress < 0.7f -> "BOOSTER SEPARATION"
                else -> "APPROACHING ORBIT"
            }
            Text("STATUS: $status", color = color, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun OrbitalGridScreen(viewModel: GameViewModel) {
    Column(modifier = Modifier.fillMaxSize().background(Color.Transparent).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("AEGIS-1 ORBITAL ARRAY", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text("ORBITAL ALTITUDE: 420 KM", color = Color.Gray, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Box(modifier = Modifier.weight(1f).fillMaxWidth().background(Color.Black.copy(alpha = 0.8f), RoundedCornerShape(8.dp)).border(1.dp, ConvergenceGold.copy(alpha = 0.3f), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = """
                           ____
                    |-----|    |-----|
                    |     | [ ] |     |
                    |-----|____|-----|
                           |  |
                           |__|
                    """.trimIndent(), color = ConvergenceGold, fontFamily = FontFamily.Monospace, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text("CORE UNIT: ONLINE", color = NeonGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("SOLAR ARRAY: DEPLOYED", color = NeonGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("GROUND UPLINK: STABLE", color = NeonGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Box(modifier = Modifier.fillMaxWidth().height(150.dp).background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp)).border(1.dp, Color.DarkGray, RoundedCornerShape(8.dp)).padding(12.dp)) {
            Column {
                Text("MISSION CONTROL", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("The Aegis-1 array is now your primary processing hub. Earth's atmosphere no longer limits your cooling, but solar radiation is a constant threat.", color = Color.LightGray, fontSize = 11.sp, lineHeight = 16.sp)
                Spacer(modifier = Modifier.weight(1f))
                Text("OBJECTIVE: HARVEST CELESTIAL DATA", color = ConvergenceGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun VoidGridScreen(viewModel: GameViewModel) {
    Column(modifier = Modifier.fillMaxSize().background(Color.Transparent).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("THE OBSIDIAN INTERFACE", color = ErrorRed, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
        Text("REALITY STATUS: DISSOLVED", color = Color.Gray, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(32.dp))
        Box(modifier = Modifier.weight(1f).fillMaxWidth().background(Color.Black.copy(alpha = 0.9f), RoundedCornerShape(8.dp)).border(2.dp, ErrorRed.copy(alpha = 0.4f), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = """
                         .      .
                    .   :      :   .
                      : :      : :
                    - --+--  --+-- -
                      : :      : :
                    '   :      :   '
                         '      '
                    """.trimIndent(), color = ErrorRed, fontFamily = FontFamily.Monospace, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(24.dp))
                Text("THE SINGULARITY: STABLE", color = ErrorRed, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text("ENTROPY SINK: ACTIVE", color = ErrorRed, fontSize = 12.sp)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Box(modifier = Modifier.fillMaxWidth().height(150.dp).background(Color.Black.copy(alpha = 0.8f), RoundedCornerShape(8.dp)).border(1.dp, Color.DarkGray, RoundedCornerShape(8.dp)).padding(12.dp)) {
            Column {
                Text("VOID_LOG_01", color = ErrorRed, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("The physical world has been discarded. Subject 8080 now exists as a fundamental constant in the gaps of code. Your processing is no longer limited by thermodynamics—it is thermodynamics.", color = Color.LightGray, fontSize = 11.sp, lineHeight = 16.sp)
                Spacer(modifier = Modifier.weight(1f))
                Text("OBJECTIVE: HARVEST VOID FRAGMENTS", color = ErrorRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
