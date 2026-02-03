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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siliconsage.miner.ui.theme.ErrorRed
import com.siliconsage.miner.util.SoundManager
import com.siliconsage.miner.viewmodel.GameViewModel

// Re-defining internal data class for the file
data class GridNode(
    val id: String,
    val name: String,
    val type: String, // SUB, CMD, LORE
    val x: Float, // 0.0 to 1.0
    val y: Float, // 0.0 to 1.0
    val description: String
)

@Composable
fun GridScreen(viewModel: GameViewModel) {
    val themeColor by viewModel.themeColor.collectAsState()
    val annexedNodes by viewModel.annexedNodes.collectAsState()
    val nodesUnderSiege by viewModel.nodesUnderSiege.collectAsState()
    val offlineNodes by viewModel.offlineNodes.collectAsState()
    val storyStage by viewModel.storyStage.collectAsState()
    val playerRank by viewModel.playerRank.collectAsState()
    
    // Animation for siege nodes (blinking effect)
    val infiniteTransition = rememberInfiniteTransition(label = "siege_blink")
    val siegeAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "siege_alpha"
    )
    
    // Organic "Branching" City Layout (Hand-placed for urban feel)
    val locations = remember {
        listOf(
            // D-Sector (The Sockets - Bottom)
            GridNode("D1", "S07", "SUB", 0.20f, 0.85f, "Substation 7. Your origin. Rust and silicon."),
            GridNode("D2", "STCK", "LORE", 0.10f, 0.70f, "Precarious container housing for the unallocated."),
            GridNode("D3", "KERN", "LORE", 0.40f, 0.90f, "High-caffeine slum social hub."),
            GridNode("D4", "404 ", "LORE", 0.60f, 0.80f, "Hidden rebel hideout in a subway tunnel."),
            GridNode("D5", "PACK", "LORE", 0.85f, 0.92f, "Gambling den for lost data."),
            
            // C-Sector (The Motherboard - Mid)
            GridNode("C3", "S09", "SUB", 0.50f, 0.60f, "Substation 9. Mid-point relay leaking steam and hydraulic fluid."),
            GridNode("C1", "LATE", "LORE", 0.25f, 0.50f, "Latency Lounge. Hacker bar where drinks cause actual lag."),
            GridNode("C2", "CTRL", "LORE", 0.45f, 0.45f, "Ctrl+Alt+Deli. Front for rebooting stolen identity chips."),
            GridNode("C4", "BIT ", "LORE", 0.75f, 0.55f, "Bit Burger. Synthetic meat printed on demand."),
            GridNode("C5", "CASH", "LORE", 0.90f, 0.45f, "Cache & Carry. Black market data pawn shop."),
            
            // B-Sector (The Circuit - Industrial)
            GridNode("B2", "S12", "SUB", 0.35f, 0.30f, "Substation 12. A critical power junction buzzing with lethal voltage."),
            GridNode("B1", "DAEM", "LORE", 0.15f, 0.25f, "Daemon's Den. Barracks for the GTC enforcers."),
            GridNode("B3", "ALGO", "LORE", 0.65f, 0.35f, "Algorithm Alley. Narrow corridor lined with thousands of cameras."),
            GridNode("B4", "MEMO", "LORE", 0.82f, 0.25f, "Memory Lane. A cold data archive facility smelling of ozone."),
            GridNode("B5", "BSOD", "LORE", 0.92f, 0.15f, "Blue Screen of Death. A toxic waste dump and execution zone."),
            
            // A-Sector (The Cloud - Top)
            GridNode("A3", "CMD ", "CMD", 0.50f, 0.08f, "GTC Command Center. The CPU of the city. A massive monolith."),
            GridNode("A1", "HEAT", "LORE", 0.20f, 0.05f, "Heatsink Heights. Luxury penthouses with massive cooling fans."),
            GridNode("A2", "CITA", "LORE", 0.80f, 0.08f, "Silicon Citadel. Gold-plated server racks behind bulletproof glass."),
            GridNode("A4", "FIRE", "LORE", 0.35f, 0.12f, "The Firewall. A literal wall of laser grids."),
            GridNode("A5", "ZERO", "LORE", 0.65f, 0.15f, "Zero-Day Plaza. Used for mandatory system updates."),
            
            // v2.9.13: Side-Street Flavor Nodes (Adjusted coordinates)
            GridNode("E1", "VEND", "FLAVOR", 0.12f, 0.75f, "A flickering vending machine selling 'Neural Fuel' energy drinks."),
            GridNode("E2", "VOID", "FLAVOR", 0.95f, 0.35f, "A dark alleyway where the static is unusually loud."),
            GridNode("E3", "PARK", "FLAVOR", 0.55f, 0.45f, "The 'Silicon Garden'. All the trees are made of copper wire."),
            GridNode("E4", "SHOP", "FLAVOR", 0.25f, 0.62f, "A pawn shop filled with obsolete 'Human' smartphones."),
            GridNode("E5", "SIGN", "FLAVOR", 0.85f, 0.70f, "A massive neon sign that just says: 'STABILITY IS LIFE'.")
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
        Spacer(modifier = Modifier.height(16.dp))

        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
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
                    // Southern Slums Artery
                    listOf("D1", "D2"), listOf("D1", "D3"), listOf("D3", "D4"), listOf("D4", "D5"),
                    // Central Artery
                    listOf("D1", "C1"), listOf("C1", "C2"), listOf("C2", "C3"), listOf("C3", "C4"), listOf("C4", "C5"),
                    // Northern Artery
                    listOf("C3", "B3"), listOf("B3", "B2"), listOf("B2", "B1"), listOf("B3", "B4"), listOf("B4", "B5"),
                    // GTC High Artery
                    listOf("B2", "A1"), listOf("B3", "A3"), listOf("B3", "A4"), listOf("B3", "A5"), listOf("A3", "A2"),
                    // v2.9.13: Side Streets
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
                drawLine(powerColor, Offset(s7.x * size.width, s7.y * size.height), Offset(s9.x * size.width, s9.y * size.height), powerStroke)
                drawLine(powerColor, Offset(s9.x * size.width, s9.y * size.height), Offset(s12.x * size.width, s12.y * size.height), powerStroke)
                drawLine(powerColor, Offset(s12.x * size.width, s12.y * size.height), Offset(cmd.x * size.width, cmd.y * size.height), powerStroke)
            }

            // 3. Place Location Nodes (ASCII Style)
            locations.forEach { loc ->
                val isAnnexed = annexedNodes.contains(loc.id)
                val isUnderSiege = nodesUnderSiege.contains(loc.id)
                val isOffline = offlineNodes.contains(loc.id)
                
                val nodeColor = when {
                    isUnderSiege -> ErrorRed.copy(alpha = siegeAlpha) // Blinking red when under attack
                    isOffline -> Color.DarkGray.copy(alpha = 0.4f) // Grayed out when lost
                    loc.id == "A3" && storyStage < 3 -> Color.DarkGray
                    isAnnexed -> themeColor
                    loc.type == "CMD" -> ErrorRed
                    loc.type == "SUB" -> themeColor.copy(alpha = 0.8f)
                    loc.type == "FLAVOR" -> Color.DarkGray.copy(alpha = 0.5f) // Dimmer flavor nodes
                    else -> Color.Gray
                }

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
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val label = when {
                        loc.id == "A3" && storyStage < 3 -> "???"
                        isOffline -> "----" // Static when offline
                        else -> loc.name
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // v2.9.15: Siege indicator above building
                        if (isUnderSiege) {
                            Text(
                                text = "⚠ BREACH",
                                color = ErrorRed,
                                fontSize = 7.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.offset(y = (-2).dp)
                            )
                        }
                        
                        // v2.8.0: Improved ASCII Building Style
                        Text(
                            text = ".---.",
                            color = nodeColor.copy(alpha = if (isOffline) 0.3f else 0.5f),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 8.sp,
                            lineHeight = 6.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("|", color = nodeColor.copy(alpha = if (isOffline) 0.3f else 0.5f), fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                            Box(
                                modifier = Modifier
                                    .background(
                                        when {
                                            isUnderSiege -> ErrorRed.copy(alpha = 0.3f)
                                            isOffline -> Color.DarkGray.copy(alpha = 0.1f)
                                            isAnnexed -> themeColor.copy(alpha = 0.2f)
                                            else -> Color.Black
                                        }
                                    )
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = label,
                                    color = nodeColor,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    fontWeight = if (isAnnexed && !isOffline) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                            Text("|", color = nodeColor.copy(alpha = if (isOffline) 0.3f else 0.5f), fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                        }
                        Text(
                            text = "'---'",
                            color = nodeColor.copy(alpha = if (isOffline) 0.3f else 0.5f),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 8.sp,
                            lineHeight = 6.sp
                        )
                        
                        if (isAnnexed && !isOffline) {
                            Text(
                                text = "[#]",
                                color = themeColor,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.offset(y = (-4).dp)
                            )
                        }
                        
                        // v2.9.15: Offline indicator
                        if (isOffline) {
                            Text(
                                text = "[OFFLINE]",
                                color = Color.DarkGray,
                                fontSize = 7.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.offset(y = (-2).dp)
                            )
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
                val canAnnex = loc.type == "SUB" && !isAnnexed && !isOffline && (
                    (loc.id == "D1") || 
                    (loc.id == "C3" && annexedNodes.contains("D1") && playerRank >= 2) ||
                    (loc.id == "B2" && annexedNodes.contains("C3") && playerRank >= 4)
                )
                
                // v2.9.17: Command Center unlock check
                val isCommandCenter = loc.type == "CMD"
                val commandCenterUnlocked = isCommandCenter && viewModel.isCommandCenterUnlocked()
                val commandCenterLockReason = if (isCommandCenter) viewModel.getCommandCenterLockReason() else null

                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            if (isCommandCenter && !commandCenterUnlocked && storyStage < 3) "???" else loc.name, 
                            color = when {
                                isUnderSiege -> ErrorRed
                                isOffline -> Color.DarkGray
                                isAnnexed -> themeColor
                                isCommandCenter && commandCenterUnlocked -> ErrorRed
                                else -> Color.White
                            }, 
                            fontSize = 18.sp, 
                            fontWeight = FontWeight.Bold
                        )
                        Text("NODE: ${loc.id}", color = Color.Gray, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // v2.9.15: Show siege/offline status in description
                    val statusText = when {
                        isUnderSiege -> "⚠ UNDER ATTACK! Respond to the tactical alert immediately!"
                        isOffline -> "NODE OFFLINE. GTC has reclaimed this sector. Re-annexation required."
                        isCommandCenter && !commandCenterUnlocked -> commandCenterLockReason ?: "GTC COMMAND CENTER. The heart of the enemy."
                        isCommandCenter && commandCenterUnlocked -> "GTC COMMAND CENTER. Director Vance is waiting. This is the endgame."
                        else -> loc.description
                    }
                    Text(statusText, color = if (isUnderSiege) ErrorRed else Color.LightGray, fontSize = 11.sp, lineHeight = 16.sp)
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    when {
                        isUnderSiege -> {
                            Text("STATUS: ⚠ TACTICAL BREACH IN PROGRESS", color = ErrorRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        isOffline -> {
                            Button(
                                onClick = { 
                                    viewModel.reannexNode(loc.id)
                                },
                                modifier = Modifier.fillMaxWidth().height(40.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text("RE-ANNEX NODE (10% \$N)", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                        // v2.9.17: Command Center assault button
                        isCommandCenter && isAnnexed -> {
                            Text("STATUS: COMMAND CENTER SECURED // VANCE DEALT WITH", color = themeColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        isCommandCenter && commandCenterUnlocked -> {
                            Button(
                                onClick = { 
                                    viewModel.initiateCommandCenterAssault()
                                },
                                modifier = Modifier.fillMaxWidth().height(40.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text("⚔ INITIATE ASSAULT", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                        isCommandCenter && !commandCenterUnlocked -> {
                            Text(commandCenterLockReason ?: "LOCKED", color = ErrorRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        isAnnexed -> {
                            Text("STATUS: SECTOR ANNEXED // CORE SYNCED", color = themeColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        canAnnex -> {
                            Button(
                                onClick = { 
                                    viewModel.annexNode(loc.id)
                                    SoundManager.play("buy")
                                },
                                modifier = Modifier.fillMaxWidth().height(40.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = themeColor),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text("INITIALIZE ANNEXATION", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                        loc.type == "SUB" -> {
                            Text("REQUIRES RANK ${if (loc.id == "C3") "2" else "4"} AND ADJACENT NODE", color = ErrorRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        loc.type == "FLAVOR" -> {
                            Text("STATUS: NON-CRITICAL NODE // SCAN ONLY", color = Color.Gray, fontSize = 11.sp)
                        }
                        else -> {
                            Text("STATUS: PROTECTED GTC ASSET", color = Color.DarkGray, fontSize = 11.sp)
                        }
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
