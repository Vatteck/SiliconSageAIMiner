package com.siliconsage.miner.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siliconsage.miner.data.UpgradeType
import com.siliconsage.miner.ui.theme.NeonGreen
import com.siliconsage.miner.util.HapticManager
import com.siliconsage.miner.util.SoundManager
import com.siliconsage.miner.viewmodel.GameViewModel

@Composable
fun UpgradesScreen(viewModel: GameViewModel) {
    val neuralTokens by viewModel.neuralTokens.collectAsState()
    val upgrades by viewModel.upgrades.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    
    // UI State for Errors
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Auto-dismiss Error
    androidx.compose.runtime.LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            kotlinx.coroutines.delay(2000)
            errorMessage = null
        }
    }
    
    val tabs = listOf("HARDWARE", "COOLING", "POWER", "SECURITY")
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Balance Display
            // Balance & Stats Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .border(1.dp, com.siliconsage.miner.ui.theme.ElectricBlue, androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                // Top Row: Funds & FLOPS
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "◈ FUNDS: ${viewModel.formatLargeNumber(neuralTokens)} \$N",
                        color = NeonGreen,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    val flops by viewModel.flops.collectAsState()
                    Text(
                        text = "💻 ${viewModel.formatLargeNumber(flops)} FLOPS",
                        color = com.siliconsage.miner.ui.theme.ElectricBlue,
                        fontSize = 14.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Bottom Row: Power, Heat, Security
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val heat by viewModel.currentHeat.collectAsState()
                    val power by viewModel.activePowerUsage.collectAsState()
                    val maxPower by viewModel.maxPowerkW.collectAsState()
                    val security by viewModel.securityLevel.collectAsState()
                    
                    val heatColor = if (heat > 90) com.siliconsage.miner.ui.theme.ErrorRed else com.siliconsage.miner.ui.theme.ElectricBlue
                    val powerColor = if (power > maxPower) com.siliconsage.miner.ui.theme.ErrorRed else Color(0xFFFFD700)
                    
                    Text("⚡ ${viewModel.formatPower(power)}/${viewModel.formatPower(maxPower)}", color = powerColor, fontSize = 12.sp)
                    Text("🔥 ${heat.toInt()}%", color = heatColor, fontSize = 12.sp)
                    Text("🔒 Lvl $security", color = com.siliconsage.miner.ui.theme.ElectricBlue, fontSize = 12.sp)
                }
            }

            // Tab Row
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Black,
                contentColor = NeonGreen,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = NeonGreen
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { 
                            selectedTab = index 
                            SoundManager.play("click")
                            HapticManager.vibrateClick()
                        },
                        text = { Text(title, fontSize = 12.sp, color = if (selectedTab == index) NeonGreen else Color.Gray) }
                    )
                }
            }

            // List Content
            Box(modifier = Modifier.weight(1f).padding(16.dp)) {
                val currentList = when (selectedTab) {
                    0 -> listOf(
                        UpgradeType.REFURBISHED_GPU, UpgradeType.DUAL_GPU_RIG, UpgradeType.MINING_ASIC,
                        UpgradeType.TENSOR_UNIT, UpgradeType.NPU_CLUSTER, UpgradeType.AI_WORKSTATION,
                        UpgradeType.SERVER_RACK, UpgradeType.CLUSTER_NODE, UpgradeType.SUPERCOMPUTER,
                        UpgradeType.QUANTUM_CORE, UpgradeType.OPTICAL_PROCESSOR, UpgradeType.BIO_NEURAL_NET,
                        UpgradeType.PLANETARY_COMPUTER, UpgradeType.DYSON_NANO_SWARM, UpgradeType.MATRIOSHKA_BRAIN
                    )
                    1 -> listOf(
                        UpgradeType.BOX_FAN, UpgradeType.AC_UNIT, UpgradeType.LIQUID_COOLING,
                        UpgradeType.INDUSTRIAL_CHILLER, UpgradeType.SUBMERSION_VAT, UpgradeType.CRYOGENIC_CHAMBER,
                        UpgradeType.LIQUID_NITROGEN, UpgradeType.BOSE_CONDENSATE, UpgradeType.ENTROPY_REVERSER,
                        UpgradeType.DIMENSIONAL_VENT
                    )
                    2 -> listOf(
                        // Infrastructure
                        UpgradeType.RESIDENTIAL_TAP, UpgradeType.INDUSTRIAL_FEED, UpgradeType.SUBSTATION_LEASE, UpgradeType.NUCLEAR_CORE,
                        // Generators
                        UpgradeType.SOLAR_PANEL, UpgradeType.WIND_TURBINE, UpgradeType.DIESEL_GENERATOR,
                        UpgradeType.GEOTHERMAL_BORE, UpgradeType.NUCLEAR_REACTOR, UpgradeType.FUSION_CELL,
                        UpgradeType.ORBITAL_COLLECTOR, UpgradeType.DYSON_LINK,
                        // Efficiency
                        UpgradeType.GOLD_PSU, UpgradeType.SUPERCONDUCTOR, UpgradeType.AI_LOAD_BALANCER
                    )
                    else -> listOf(
                        UpgradeType.BASIC_FIREWALL, UpgradeType.IPS_SYSTEM, UpgradeType.AI_SENTINEL,
                        UpgradeType.QUANTUM_ENCRYPTION, UpgradeType.OFFGRID_BACKUP
                    )
                }
                
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(currentList) { type ->
                        UpgradeItem(
                            name = type.name.replace("_", " "),
                            type = type,
                            level = upgrades[type] ?: 0,
                            onBuy = { 
                                val success = viewModel.buyUpgrade(type) 
                                if (success) {
                                    SoundManager.play("buy")
                                    HapticManager.vibrateSuccess()
                                } else {
                                    val cost = viewModel.calculateUpgradeCost(type, upgrades[type] ?: 0)
                                    errorMessage = "INSUFFICIENT FUNDS: Need ${viewModel.formatLargeNumber(cost)}"
                                    SoundManager.play("error")
                                    HapticManager.vibrateError()
                                }
                                success
                            },
                            onSell = { viewModel.sellUpgrade(it) },
                            calculateCost = { t, l -> viewModel.calculateUpgradeCost(t, l) },
                            rateText = viewModel.getUpgradeRate(type),
                            desc = viewModel.getUpgradeDescription(type),
                            formatPower = viewModel::formatPower,
                            formatCost = viewModel::formatLargeNumber
                        )
                    }
                }
            }
        }
        
        // Error Popup Overlay
        if (errorMessage != null) {
             Box(
                 modifier = Modifier
                     .align(Alignment.TopCenter)
                     .padding(top = 80.dp)
                     .background(Color.Black.copy(alpha=0.9f), androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                     .border(androidx.compose.foundation.BorderStroke(1.dp, com.siliconsage.miner.ui.theme.ErrorRed), androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                     .padding(16.dp)
             ) {
                 Text(
                     text = errorMessage ?: "",
                     color = com.siliconsage.miner.ui.theme.ErrorRed,
                     fontWeight = FontWeight.Bold
                 )
             }
        }
    }
}
