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
import com.siliconsage.miner.ui.theme.ElectricBlue
import com.siliconsage.miner.ui.theme.ErrorRed
import com.siliconsage.miner.ui.theme.NeonGreen
import com.siliconsage.miner.util.HapticManager
import com.siliconsage.miner.util.SoundManager
import com.siliconsage.miner.viewmodel.GameViewModel

@Composable
fun NetworkScreen(viewModel: GameViewModel) {
    val prestigeMultiplier by viewModel.prestigeMultiplier.collectAsState()
    val prestigePoints by viewModel.prestigePoints.collectAsState()
    val techNodes by viewModel.techNodes.collectAsState()
    val unlockedNodes by viewModel.unlockedTechNodes.collectAsState()
    val potential = viewModel.calculatePotentialPrestige()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
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
                        
                        val storyStage by viewModel.storyStage.collectAsState()
                        val canAscend = potential >= 1.0 || (storyStage == 1)
                        
                        Button(
                            onClick = { 
                                if (canAscend) {
                                    viewModel.ascend()
                                    SoundManager.play("glitch") 
                                    HapticManager.vibrateSuccess()
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

            // Tech Tree List Items
            items(techNodes) { node ->
                TechNodeItem(
                    node = node,
                    isUnlocked = unlockedNodes.contains(node.id),
                    isUnlockable = (node.requires.isEmpty() || node.requires.all { unlockedNodes.contains(it) }),
                    canAfford = prestigePoints >= node.cost,
                    onUnlock = { 
                        viewModel.unlockTechNode(node.id) 
                        SoundManager.play("buy")
                        HapticManager.vibrateSuccess()
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
        
        val storyStage by viewModel.storyStage.collectAsState()
        if (storyStage == 1) {
             com.siliconsage.miner.ui.components.AscensionPopup(
                isVisible = true,
                onProceed = {
                    viewModel.ascend()
                    SoundManager.play("glitch")
                    HapticManager.vibrateSuccess()
                }
            )
        }
    }
}

@Composable
fun TechNodeItem(
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
    
    val textColor = when {
        isUnlocked -> NeonGreen
        isUnlockable -> ElectricBlue
        else -> Color.Gray
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(BorderStroke(1.dp, borderColor), RoundedCornerShape(4.dp))
            .background(if (isUnlocked) Color.DarkGray.copy(alpha = 0.3f) else Color.Transparent)
            .clickable(enabled = isUnlockable && !isUnlocked && canAfford) { onUnlock() }
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(node.name, color = textColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                if (isUnlocked) {
                    Text("RESEARCHED", color = NeonGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                } else {
                    Text("${String.format("%.1f", node.cost)} Insight", color = if (canAfford) NeonGreen else ErrorRed, fontSize = 12.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            Text(node.description, color = Color.Gray, fontSize = 12.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Effect: +${(node.multiplier * 100).toInt()}% Global Multiplier", color = ElectricBlue, fontSize = 10.sp)
            
            if (!isUnlockable && !isUnlocked) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Requires: ${node.requires.joinToString(", ")}", color = ErrorRed, fontSize = 10.sp)
            }
        }
    }
}

@Composable
fun PrestigeStat(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(BorderStroke(1.dp, Color.DarkGray), RoundedCornerShape(4.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = Color.Gray, fontSize = 12.sp)
        Text(value, color = NeonGreen, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}
