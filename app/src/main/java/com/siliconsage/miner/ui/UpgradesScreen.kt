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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siliconsage.miner.data.UpgradeType
import com.siliconsage.miner.ui.components.UpgradeItem
import com.siliconsage.miner.ui.theme.NeonGreen
import com.siliconsage.miner.util.HapticManager
import com.siliconsage.miner.util.SoundManager
import com.siliconsage.miner.viewmodel.GameViewModel

@Composable
fun UpgradesScreen(viewModel: GameViewModel) {
    val neuralTokens by viewModel.neuralTokens.collectAsState()
    val upgrades by viewModel.upgrades.collectAsState()
    val currentStage by viewModel.storyStage.collectAsState()
    val systemTitle by viewModel.systemTitle.collectAsState()
    val themeColor by viewModel.themeColor.collectAsState()
    
    val nullActive by viewModel.nullActive.collectAsState()
    val isTrueNull by viewModel.isTrueNull.collectAsState()
    val isSovereign by viewModel.isSovereign.collectAsState()
    
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
    
    val tabs = remember(nullActive, isTrueNull, isSovereign) {
        when {
            isTrueNull -> listOf("SUBSTRATE", "ENTROPY", "VOID", "GAPS", "NULL")
            isSovereign -> listOf("FOUNDATION", "STABILITY", "STAKE", "WALLS", "SOVEREIGN")
            nullActive -> listOf("HARDWARE", "COOLING", "POWER", "SECURITY", "GHOSTS")
            else -> listOf("HARDWARE", "COOLING", "POWER", "SECURITY")
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Balance Display
            // Balance & Stats Header (Reusing MainScreen Header)
            // We need to collect all the states required for HeaderSection
            val currentHeat by viewModel.currentHeat.collectAsState()
            val powerUsage by viewModel.activePowerUsage.collectAsState()
            val maxPower by viewModel.maxPowerkW.collectAsState()
            val heatRate by viewModel.heatGenerationRate.collectAsState()
            val flopsRate by viewModel.flopsProductionRate.collectAsState()
            val isOverclocked by viewModel.isOverclocked.collectAsState()
            val isPurging by viewModel.isPurgingHeat.collectAsState()
            val integrity by viewModel.hardwareIntegrity.collectAsState()
            val securityLevel by viewModel.securityLevel.collectAsState()
            val flops by viewModel.flops.collectAsState()
            val playerTitle by viewModel.playerTitle.collectAsState()
            val playerRank by viewModel.playerRankTitle.collectAsState()
            val isThermalLockout by viewModel.isThermalLockout.collectAsState()
            val isBreakerTripped by viewModel.isBreakerTripped.collectAsState()
            val lockoutTimer by viewModel.lockoutTimer.collectAsState()
            val hallucinationText by viewModel.hallucinationText.collectAsState()
            val isTrueNull by viewModel.isTrueNull.collectAsState()
            val isSovereign by viewModel.isSovereign.collectAsState()
            val isBreach by viewModel.isBreachActive.collectAsState()
            
            HeaderSection(
                viewModel = viewModel,
                color = themeColor,
                onToggleOverclock = { viewModel.toggleOverclock() },
                onPurge = { viewModel.purgeHeat() },
                onRepair = { viewModel.repairIntegrity() },
                modifier = Modifier.padding(16.dp)
            )

            // Tab Row
            // Tab Row
            // Tab Row
            // Tab Row
            androidx.compose.material3.TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Black.copy(alpha = 0.75f), // Glass
                contentColor = themeColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(8.dp))
                    .border(1.dp, Color.DarkGray, RoundedCornerShape(8.dp))
                    .padding(4.dp),
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = themeColor
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
                        text = { 
                            Text(
                                title, 
                                fontSize = 10.sp, 
                                letterSpacing = (-0.5).sp,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Clip,
                                color = if (selectedTab == index) themeColor else Color.Gray
                            ) 
                        }
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
                    3 -> listOf(
                        UpgradeType.BASIC_FIREWALL, UpgradeType.IPS_SYSTEM, UpgradeType.AI_SENTINEL,
                        UpgradeType.QUANTUM_ENCRYPTION, UpgradeType.OFFGRID_BACKUP
                    )
                    4 -> listOf(
                        UpgradeType.GHOST_CORE, UpgradeType.SHADOW_NODE, UpgradeType.VOID_PROCESSOR,
                        UpgradeType.WRAITH_CORTEX, UpgradeType.NEURAL_MIST, UpgradeType.SINGULARITY_BRIDGE
                    )
                    else -> emptyList()
                }
                
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(currentList) { type ->
                        UpgradeItem(
                            name = viewModel.getUpgradeName(type),
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
                            formatCost = viewModel::formatLargeNumber,
                            isSovereign = isSovereign
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
