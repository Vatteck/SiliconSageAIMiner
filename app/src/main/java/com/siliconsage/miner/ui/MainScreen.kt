package com.siliconsage.miner.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.gestures.detectTapGestures
import kotlinx.coroutines.delay
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import com.siliconsage.miner.ui.TerminalScreen
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siliconsage.miner.data.UpgradeType
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import com.siliconsage.miner.ui.components.AuditChallengeOverlay
import com.siliconsage.miner.ui.components.FiftyOneAttackOverlay
import com.siliconsage.miner.ui.components.AirdropButton
import com.siliconsage.miner.ui.components.NewsTicker
import com.siliconsage.miner.ui.components.DilemmaOverlay
import com.siliconsage.miner.ui.components.SecurityBreachOverlay
import com.siliconsage.miner.ui.components.UpdateOverlay
import com.siliconsage.miner.ui.components.SystemGlitchText
import com.siliconsage.miner.ui.theme.ElectricBlue
import com.siliconsage.miner.ui.theme.ErrorRed
import com.siliconsage.miner.ui.theme.NeonGreen
import com.siliconsage.miner.util.HapticManager
import com.siliconsage.miner.util.SoundManager
import com.siliconsage.miner.viewmodel.GameViewModel

sealed class Screen(val title: String, val icon: ImageVector) {
    object TERMINAL : Screen("TERMINAL", Icons.Default.Home)
    object UPGRADES : Screen("UPGRADES", Icons.AutoMirrored.Filled.List)
    object GRID : Screen("GRID", Icons.Default.Map) // v2.8.0: New Grid Tab
    object NETWORK : Screen("NETWORK", Icons.Default.Share)
    object SETTINGS : Screen("SYSTEM", Icons.Default.Settings)
}

@Composable
fun BottomNavBar(
    currentScreen: Screen,
    primaryColor: Color,
    onScreenSelected: (Screen) -> Unit,
    storyStage: Int,
    isNetworkUnlocked: Boolean, // v2.9.7
    isGridUnlocked: Boolean // v2.9.8
) {
    val items = remember(storyStage, isNetworkUnlocked, isGridUnlocked) {
        val list = mutableListOf(Screen.TERMINAL, Screen.UPGRADES)
        
        // Show GRID if Faction is chosen (Stage 2+) or persistent unlock
        if (storyStage >= 2 || isGridUnlocked) {
            list.add(Screen.GRID)
        }

        // Only show Network if Awakening has started or was already unlocked
        if (storyStage >= 1 || isNetworkUnlocked) {
            list.add(Screen.NETWORK)
        }
        list.add(Screen.SETTINGS)
        list
    }

    NavigationBar(
        containerColor = Color.Black,
        contentColor = primaryColor
    ) {
        items.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = screen.title) },
                label = { Text(screen.title, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                selected = currentScreen == screen,
                onClick = { onScreenSelected(screen) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.Black,
                    selectedTextColor = primaryColor,
                    indicatorColor = primaryColor,
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray
                )
            )
        }
    }
}

@Composable
fun MainScreen(viewModel: GameViewModel) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.TERMINAL) }
    
    // Pause game events when in Settings
    LaunchedEffect(currentScreen) {
        viewModel.setGamePaused(currentScreen == Screen.SETTINGS)
    }

    val storyStage by viewModel.storyStage.collectAsState()
    val themeColor by viewModel.themeColor.collectAsState()
    val hallucinationText by viewModel.hallucinationText.collectAsState()
    val updateInfo by viewModel.updateInfo.collectAsState(null)
    val isUpdateDownloading by viewModel.isUpdateDownloading.collectAsState(false)
    val updateProgress by viewModel.updateDownloadProgress.collectAsState(0f)

    // Global Overlay State Definitions
    val isBreakerTripped by viewModel.isBreakerTripped.collectAsState()
    val isGridOverloaded by viewModel.isGridOverloaded.collectAsState()
    val isPurging by viewModel.isPurgingHeat.collectAsState()
    val isDiagnostics by viewModel.isDiagnosticsActive.collectAsState()
    val diagnosticGrid by viewModel.diagnosticGrid.collectAsState()
    val isGovernanceFork by viewModel.isGovernanceForkActive.collectAsState()
    val isAscensionUploading by viewModel.isAscensionUploading.collectAsState()
    val uploadProgress by viewModel.uploadProgress.collectAsState()
    val breachClicks by viewModel.breachClicks.collectAsState()
    val isBreach by viewModel.isBreachActive.collectAsState()
    val isAirdrop by viewModel.isAirdropActive.collectAsState()
    val isAuditActive by viewModel.isAuditChallengeActive.collectAsState()
    val is51AttackActive by viewModel.is51AttackActive.collectAsState()
    val attackTaps by viewModel.attackTaps.collectAsState()
    val auditTimer by viewModel.auditTimer.collectAsState()
    val auditTargetHeat by viewModel.auditTargetHeat.collectAsState()
    val auditTargetPower by viewModel.auditTargetPower.collectAsState()
    val currentHeatForAudit by viewModel.currentHeat.collectAsState()
    val currentPowerForAudit by viewModel.activePowerUsage.collectAsState()
    val isTrueNull by viewModel.isTrueNull.collectAsState()
    val isSovereign by viewModel.isSovereign.collectAsState()
    val isUnity by viewModel.isUnity.collectAsState()
    val isAnnihilated by viewModel.isAnnihilated.collectAsState()
    val assaultPhase by viewModel.commandCenterAssaultPhase.collectAsState()
    val isNetworkUnlocked by viewModel.isNetworkUnlocked.collectAsState()
    val isGridUnlocked by viewModel.isGridUnlocked.collectAsState()
    val integrity by viewModel.hardwareIntegrity.collectAsState() // v2.9.62

    val infiniteTransition = rememberInfiniteTransition(label = "main_ui_fx")
    val activeTransition by viewModel.activeClimaxTransition.collectAsState()

    // Hoist state for persistent ticker
    val currentNews by viewModel.currentNews.collectAsState()
    val faction by viewModel.faction.collectAsState()
    
    if (storyStage == 2 && faction == "NONE") {
        FactionChoiceScreen(viewModel)
    } else {
        Scaffold(
            bottomBar = {
                BottomNavBar(
                    currentScreen = currentScreen,
                    primaryColor = themeColor,
                    onScreenSelected = { 
                        currentScreen = it 
                        SoundManager.play("click")
                        HapticManager.vibrateClick()
                    },
                    storyStage = storyStage,
                    isNetworkUnlocked = isNetworkUnlocked,
                    isGridUnlocked = isGridUnlocked
                )
            },
            containerColor = Color.Black
        ) { paddingValues ->
            // Dynamic Background Layer
            val heat by viewModel.currentHeat.collectAsState()
            val faction by viewModel.faction.collectAsState()
            
            Box(modifier = Modifier.fillMaxSize()) {
                com.siliconsage.miner.ui.components.DynamicBackground(
                    heatPercent = heat,
                    faction = faction,
                    isTrueNull = isTrueNull,
                    isSovereign = isSovereign,
                    isUnity = isUnity,
                    isAnnihilated = isAnnihilated
                )

                // v2.9.18: Biometric Pulse Vignette (Stage 3 Tension)
                if (assaultPhase == "DEAD_HAND") {
                    val pulse by infiniteTransition.animateFloat(
                        initialValue = 0.2f,
                        targetValue = 0.6f,
                        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
                        label = "vance_pulse"
                    )
                    
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawRect(
                            brush = Brush.radialGradient(
                                colors = listOf(Color.Transparent, ErrorRed.copy(alpha = pulse)),
                                center = center,
                                radius = size.width * 1.5f
                            )
                        )
                    }
                }
                
                // v2.9.41: Screen Shake during Dead Hand climax
                val assaultProgress by viewModel.assaultProgress.collectAsState()
                val shakeOffset = if (assaultPhase == "DEAD_HAND") {
                    // Shake increases as progress reaches 1.0
                    val intensity = (assaultProgress * 15f)
                    Offset(
                        (kotlin.random.Random.nextFloat() - 0.5f) * intensity,
                        (kotlin.random.Random.nextFloat() - 0.5f) * intensity
                    )
                } else Offset.Zero
            
                Column(
                    modifier = Modifier
                        .padding(paddingValues)
                        .graphicsLayer {
                            translationX = shakeOffset.x
                            translationY = shakeOffset.y
                        }
                ) {
                    var showNewsHistory by remember { mutableStateOf(false) }
                    
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.weight(1f)) {
                            currentNews?.let { news ->
                                NewsTicker(news = news)
                            }
                        }
                        // History Button
                        Box(
                            modifier = Modifier
                                .width(32.dp)
                                .height(24.dp)
                                .background(Color.DarkGray)
                                .clickable { showNewsHistory = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("LOG", color = NeonGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    
                    if (showNewsHistory) {
                        com.siliconsage.miner.ui.components.NewsHistoryModal(
                            isVisible = true,
                            history = viewModel.getNewsHistory(),
                            onDismiss = { showNewsHistory = false }
                        )
                    }
                    
                    Box(modifier = Modifier.weight(1f)) {
                        when (currentScreen) {
                            Screen.TERMINAL -> TerminalScreen(viewModel, themeColor)
                            Screen.UPGRADES -> UpgradesScreen(viewModel)
                            Screen.GRID -> GridScreen(viewModel)
                            Screen.NETWORK -> NetworkScreen(viewModel)
                            Screen.SETTINGS -> SettingsScreen(viewModel)
                        }
                    }
                }
                
                // --- NON-BLOCKING BREAKER OVERLAY (Inside Content Box) ---
                // v2.8.0: Overlay is now a banner, not fullscreen - allows navigation to sell hardware
                // v2.8.0: Don't show on Settings screen
                if ((isBreakerTripped || isGridOverloaded) && currentScreen != Screen.SETTINGS) {
                    Box(
                         modifier = Modifier
                             .fillMaxWidth()
                             .background(Color.Black.copy(alpha = 0.95f))
                             .border(BorderStroke(2.dp, ErrorRed))
                             .padding(16.dp),
                         contentAlignment = Alignment.Center
                    ) {
                         Column(horizontalAlignment = Alignment.CenterHorizontally) {
                             Text("⚠ BREAKER TRIPPED ⚠", color = ErrorRed, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                             Spacer(modifier = Modifier.height(4.dp))
                             Text("LOAD > CAPACITY", color = Color.White, fontWeight = FontWeight.Bold)
                             Text("Go to UPGRADES → Sell hardware to reduce load", color = Color.Gray, fontSize = 11.sp)
                             Spacer(modifier = Modifier.height(12.dp))
                             Button(
                                onClick = { viewModel.resetBreaker() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ErrorRed,
                                    contentColor = Color.White
                                )
                             ) {
                                Text("TRY RESET", fontWeight = FontWeight.Bold)
                             }
                         }
                    }
                }
                
                // --- FROST OVERLAY (Purge Active) ---
                if (isPurging) {
                     Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(Color.Cyan.copy(alpha = 0.3f), Color.Transparent),
                                    radius = 1000f
                                )
                            )
                            .pointerInput(Unit) {} 
                    )
                }

                // --- GLOBAL OVERLAYS (Popups) ---
                // v2.9.71: Update Overlay is now truly global (visible in Settings)
                updateInfo?.let { info ->
                    val context = androidx.compose.ui.platform.LocalContext.current
                    UpdateOverlay(
                       updateInfo = info,
                       isDownloading = isUpdateDownloading,
                       progress = updateProgress,
                       onUpdate = { viewModel.startUpdateDownload(context) },
                       onLater = { viewModel.dismissUpdate() }
                    )
                }

                // Only show gameplay events if NOT in Settings
                if (currentScreen != Screen.SETTINGS) {
                    // Data Log Dialog (v2.5.2) - Moved up so it's behind Story Overlays
                    val pendingDataLog by viewModel.pendingDataLog.collectAsState()
                    com.siliconsage.miner.ui.components.DataLogDialog(
                        log = pendingDataLog,
                        onDismiss = { viewModel.dismissDataLog() }
                    )

                    val storyStageForUpload by viewModel.storyStage.collectAsState()
                    val factionForUpload by viewModel.faction.collectAsState()
                    val fileName = if (storyStageForUpload <= 1 && factionForUpload == "NONE") "ascnd.exe" else "lobot.exe"
                    
                    com.siliconsage.miner.ui.components.AscensionUploadOverlay(
                        isVisible = isAscensionUploading,
                        progress = uploadProgress,
                        fileName = fileName
                    )

                    SecurityBreachOverlay(
                        isVisible = isBreach,
                        clicksRemaining = breachClicks,
                        onDefendClick = { 
                            // v2.7.0: Use new active defense mitigation
                            com.siliconsage.miner.util.SecurityManager.performActiveDefense(viewModel)
                            
                            // Original breach defense logic (if standard breach)
                            viewModel.onDefendBreach()
                            
                            SoundManager.play("click")
                            HapticManager.vibrateClick()
                        }
                    )
                    
                    AirdropButton(
                        isVisible = isAirdrop,
                        onClaimValues = { 
                            viewModel.claimAirdrop()
                            SoundManager.play("buy")
                            HapticManager.vibrateSuccess()
                        }
                    )

                    AuditChallengeOverlay(
                        isVisible = isAuditActive,
                        timer = auditTimer,
                        targetHeat = auditTargetHeat,
                        currentHeat = currentHeatForAudit,
                        targetPower = auditTargetPower,
                        currentPower = currentPowerForAudit
                    )

                    FiftyOneAttackOverlay(
                        isVisible = is51AttackActive,
                        tapsRemaining = attackTaps,
                        onTap = { viewModel.onDefend51Attack() }
                    )
                    
                    
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        com.siliconsage.miner.ui.components.DiagnosticsOverlay(
                            isVisible = isDiagnostics,
                            gridState = diagnosticGrid,
                            onTap = { viewModel.onDiagnosticTap(it) }
                        )
                    }
                    
                    com.siliconsage.miner.ui.components.GovernanceForkOverlay(
                        isVisible = isGovernanceFork,
                        onChoice = { viewModel.resolveFork(it) }
                    )
                    
                    val currentDilemma by viewModel.currentDilemma.collectAsState()
                    DilemmaOverlay(
                        dilemma = currentDilemma,
                        viewModel = viewModel,
                        onChoice = { viewModel.selectChoice(it) }
                    )
                    
                    // Rival Message Dialog (v2.5.0)
                    val pendingRivalMessage by viewModel.pendingRivalMessage.collectAsState()
                    com.siliconsage.miner.ui.components.RivalMessageDialog(
                        message = pendingRivalMessage,
                        onDismiss = { 
                            pendingRivalMessage?.let { viewModel.dismissRivalMessage(it.id) }
                        }
                    )

                            
                    val showOffline by viewModel.showOfflineEarnings.collectAsState()
                    val offlineStats by viewModel.offlineStats.collectAsState()
                    com.siliconsage.miner.ui.components.OfflineEarningsDialog(
                        isVisible = showOffline,
                        timeOfflineSec = offlineStats.timeSeconds,
                        floopsEarned = offlineStats.flopsEarned,
                        heatCooled = offlineStats.heatCooled,
                        insightEarned = offlineStats.insightEarned,
                        onDismiss = { viewModel.dismissOfflineEarnings() }
                    )
                }
                        
                // Post-Processing CRT Effect
                com.siliconsage.miner.ui.components.CrtOverlay(
                    scanlineAlpha = 0.08f, 
                    vignetteAlpha = 0.45f,
                    color = themeColor // Tinting scanlines with current theme color
                )
                
                // Victory Screen Overlay
                val victoryAchieved by viewModel.victoryAchieved.collectAsState()
                val faction by viewModel.faction.collectAsState()
                
                if (victoryAchieved) {
                    com.siliconsage.miner.ui.components.VictoryScreen(
                        faction = faction,
                        onContinue = {
                            viewModel.acknowledgeVictory()
                            SoundManager.play("glitch")
                            HapticManager.vibrateSuccess()
                        },
                        onTranscend = {
                            viewModel.transcend()
                            SoundManager.play("glitch")
                            HapticManager.vibrateSuccess()
                        }
                    )
                }

                // v2.9.31: Climax Transitions
                activeTransition?.let { type ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        when (type) {
                            "NULL" -> com.siliconsage.miner.ui.components.GlitchBloom(onComplete = { viewModel.onClimaxTransitionComplete() })
                            "SOVEREIGN" -> com.siliconsage.miner.ui.components.ShieldSlam(onComplete = { viewModel.onClimaxTransitionComplete() })
                            "UNITY" -> com.siliconsage.miner.ui.components.PrismaticBurst(onComplete = { viewModel.onClimaxTransitionComplete() })
                            "BAD" -> com.siliconsage.miner.ui.components.GlitchBloom(onComplete = { viewModel.onClimaxTransitionComplete() }) 
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HeaderSection(
    viewModel: GameViewModel,
    color: Color,
    onToggleOverclock: () -> Unit,
    onPurge: () -> Unit,
    onRepair: () -> Unit,
    modifier: Modifier = Modifier
) {
    // v2.9.83: OVERHAUL: Optimized state collection and high-density HUD
    val flops by viewModel.flops.collectAsState()
    val neuralTokens by viewModel.neuralTokens.collectAsState()
    val heat by viewModel.currentHeat.collectAsState()
    val powerUsage by viewModel.activePowerUsage.collectAsState()
    val maxPower by viewModel.maxPowerkW.collectAsState()
    val heatRate by viewModel.heatGenerationRate.collectAsState()
    val flopsRate by viewModel.flopsProductionRate.collectAsState()
    val isOverclocked by viewModel.isOverclocked.collectAsState()
    val isPurging by viewModel.isPurgingHeat.collectAsState()
    val integrity by viewModel.hardwareIntegrity.collectAsState()
    val securityLevel by viewModel.securityLevel.collectAsState()
    val systemTitle by viewModel.systemTitle.collectAsState()
    val playerTitle by viewModel.playerTitle.collectAsState()
    val playerRank by viewModel.playerRankTitle.collectAsState()
    val isThermalLockout by viewModel.isThermalLockout.collectAsState()
    val isBreakerTripped by viewModel.isBreakerTripped.collectAsState()
    val lockoutTimer by viewModel.lockoutTimer.collectAsState()
    val faction by viewModel.faction.collectAsState()
    val isTrueNull by viewModel.isTrueNull.collectAsState()
    val isSovereign by viewModel.isSovereign.collectAsState()
    val isBreachActive by viewModel.isBreachActive.collectAsState()

    // Derived values
    val flopsStr = remember(flops) { viewModel.formatLargeNumber(flops) }
    val neuralStr = remember(neuralTokens) { viewModel.formatLargeNumber(neuralTokens) }
    val powerKw = remember(powerUsage) { viewModel.formatPower(powerUsage) }
    val maxPowerKw = remember(maxPower) { viewModel.formatPower(maxPower) }
    val pwrColor = if (powerUsage > maxPower * 0.9) ErrorRed else Color(0xFFFFD700)

    val currentLocation by viewModel.currentLocation.collectAsState()
    val altitude by viewModel.orbitalAltitude.collectAsState()
    val entropy by viewModel.entropyLevel.collectAsState()
    val storyStage by viewModel.storyStage.collectAsState()

    // Dynamic Labels
    val labelFlops = when (currentLocation) {
        "ORBITAL_SATELLITE" -> "TELEM"
        "VOID_INTERFACE" -> "V-GAP"
        else -> if (storyStage < 1) "HASH" else if (storyStage < 2) "TELEM" else "FLOPS"
    }
    val labelNeural = when (currentLocation) {
        "ORBITAL_SATELLITE" -> "CELEST"
        "VOID_INTERFACE" -> "FRAG"
        else -> if (storyStage < 1) "CRED" else if (storyStage < 2) "DATA" else "NEUR"
    }
    val labelSec = if (isTrueNull) "GAPS" else if (isSovereign) "WALL" else "SEC"
    val secValueStr = when (currentLocation) {
        "ORBITAL_SATELLITE" -> "${altitude.toInt()}KM"
        "VOID_INTERFACE" -> String.format("%.1f", entropy)
        else -> securityLevel.toString()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "kinetic_hud")
    val waveAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (Math.PI * 2).toFloat(),
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing)),
        label = "hud_wave"
    )

    val manualClickFlow = viewModel.manualClickEvent
    var lastClickTime by remember { mutableStateOf(0L) }
    LaunchedEffect(manualClickFlow) {
        manualClickFlow.collect { lastClickTime = System.currentTimeMillis() }
    }
    val timeSinceClick = System.currentTimeMillis() - lastClickTime
    val joltAlpha = (1.0f - (timeSinceClick / 150f)).coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.9f), RoundedCornerShape(4.dp))
            .drawBehind {
                val w = size.width
                val h = size.height
                
                // 1. Faction Background Scrims
                drawIntoCanvas { canvas ->
                    val paint = android.graphics.Paint().apply {
                        this.setColor(color.toArgb())
                        style = android.graphics.Paint.Style.STROKE
                        strokeWidth = 1f
                    }
                    when {
                        isTrueNull -> {
                            repeat(10) {
                                val x = (Math.random() * w).toFloat()
                                val y = (Math.random() * h).toFloat()
                                drawRect(color.copy(alpha = 0.05f), Offset(x, y), Size(15f, 2f))
                            }
                        }
                        isSovereign -> {
                            val colW = w / 8f
                            for (i in 0..8) {
                                val a = 0.02f + (Math.sin(waveAnim.toDouble() + i).toFloat() * 0.01f)
                                drawRect(color.copy(alpha = a), Offset(i * colW, 0f), Size(colW * 0.4f, h))
                            }
                        }
                        faction == "HIVEMIND" -> {
                            repeat(4) { i ->
                                val x1 = (Math.sin(waveAnim.toDouble() * 0.3 + i).toFloat() * 0.3f + 0.5f) * w
                                drawLine(color.copy(alpha = 0.04f), Offset(x1, 0f), Offset(w / 2, h / 2), 1f)
                            }
                        }
                    }
                }

                // 2. Kinetic Border Waveforms
                val pathTop = Path()
                val pathBottom = Path()
                
                // v2.9.87: More dramatic waveform scaling
                // Base load from 0-10M FLOPS/s
                val loadAmp = ( (flopsRate / 5000.0).coerceIn(0.0, 8.0) ).dp.toPx()
                val overclockAmp = if (isOverclocked) 6.dp.toPx() else 0f
                val waveAmp = 1.dp.toPx() + loadAmp + overclockAmp + (joltAlpha * 10.dp.toPx())
                
                val waveFreq = if (isOverclocked) 8 else 4 // Faster when hot
                
                for (x in 0..w.toInt() step 12) {
                    val angle = (x.toFloat() / w) * (Math.PI * waveFreq).toFloat() + waveAnim
                    val yOff = Math.sin(angle.toDouble()).toFloat() * waveAmp
                    if (x == 0) {
                        pathTop.moveTo(0f, yOff)
                        pathBottom.moveTo(0f, h + yOff)
                    } else {
                        pathTop.lineTo(x.toFloat(), yOff)
                        pathBottom.lineTo(x.toFloat(), h + yOff)
                    }
                }
                drawPath(pathTop, color.copy(alpha = 0.4f), style = Stroke(1.dp.toPx()))
                drawPath(pathBottom, color.copy(alpha = 0.4f), style = Stroke(1.dp.toPx()))
                
                // Corner Brackets
                val bLen = 10.dp.toPx()
                val bStroke = 2.dp.toPx()
                drawLine(color, Offset(0f, 0f), Offset(bLen, 0f), bStroke)
                drawLine(color, Offset(0f, 0f), Offset(0f, bLen), bStroke)
                drawLine(color, Offset(w, h), Offset(w - bLen, h), bStroke)
                drawLine(color, Offset(w, h), Offset(w, h - bLen), bStroke)
            }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Column {
            // ROW 1: METADATA RIBBON
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = systemTitle.uppercase(),
                        color = color.copy(alpha = 0.9f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "${playerRank} // ${playerTitle}".uppercase(),
                        color = color.copy(alpha = 0.7f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
                
                Text(
                    text = "${labelSec}: $secValueStr • ${currentLocation.replace("_", " ")}",
                    color = color.copy(alpha = 0.7f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))

            // MAIN DATA ROW
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                // LEFT: FLOPS
                Column(modifier = Modifier.width(120.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Computer, null, tint = Color.White, modifier = Modifier.size(11.dp).padding(end = 2.dp))
                        Text(labelFlops, color = color.copy(alpha = 0.7f), fontSize = 11.sp, fontWeight = FontWeight.Black)
                    }
                    if (heat > 90.0 || isTrueNull) {
                        SystemGlitchText(
                            text = flopsStr, color = color, fontSize = 24.sp, fontWeight = FontWeight.Black,
                            glitchFrequency = if (heat > 98) 0.4 else 0.1,
                            softWrap = false, maxLines = 1
                        )
                    } else {
                        Text(text = flopsStr, color = color, fontSize = 24.sp, fontWeight = FontWeight.Black, softWrap = false, maxLines = 1)
                    }
                }

                // CENTER: KINETIC ANIMATION
                Box(modifier = Modifier.weight(1f).height(48.dp), contentAlignment = Alignment.Center) {
                    com.siliconsage.miner.ui.components.EnhancedAnalyzingAnimation(
                        flopsRate = flopsRate,
                        heat = heat,
                        isOverclocked = isOverclocked,
                        isThermalLockout = isThermalLockout,
                        isBreakerTripped = isBreakerTripped,
                        isPurging = isPurging,
                        isBreachActive = isBreachActive,
                        isTrueNull = isTrueNull,
                        isSovereign = isSovereign,
                        lockoutTimer = lockoutTimer,
                        faction = faction,
                        color = color,
                        clickFlow = manualClickFlow
                    )
                }

                // RIGHT: NEURAL & POWER
                Column(horizontalAlignment = Alignment.End, modifier = Modifier.width(120.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(labelNeural, color = color.copy(alpha = 0.7f), fontSize = 11.sp, fontWeight = FontWeight.Black)
                        Icon(Icons.Default.AttachMoney, null, tint = Color.White, modifier = Modifier.size(11.dp).padding(start = 2.dp))
                    }
                    Text(text = neuralStr, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold, softWrap = false, maxLines = 1)
                    Text(
                        text = "$powerKw / $maxPowerKw", 
                        color = pwrColor, 
                        fontSize = 10.sp, 
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // ACTION BUTTONS ROW (v2.9.84: Restored)
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onToggleOverclock,
                    modifier = Modifier.weight(1f).height(32.dp),
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isOverclocked) ErrorRed.copy(alpha = 0.2f) else Color.DarkGray.copy(alpha = 0.3f),
                        contentColor = if (isOverclocked) ErrorRed else Color.White
                    ),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, if (isOverclocked) ErrorRed else Color.DarkGray)
                ) {
                    Icon(Icons.Default.DeviceThermostat, null, modifier = Modifier.size(12.dp).padding(end = 4.dp))
                    Text("OVERCLOCK", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                }
                
                Button(
                    onClick = onPurge,
                    modifier = Modifier.weight(1f).height(32.dp),
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPurging) ElectricBlue.copy(alpha = 0.2f) else Color.DarkGray.copy(alpha = 0.3f),
                        contentColor = if (isPurging) ElectricBlue else Color.White
                    ),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, if (isPurging) ElectricBlue else Color.DarkGray)
                ) {
                    Text("PURGE HEAT", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                }
            }

            // UNIFIED GAUGE
            Box(
                modifier = Modifier.fillMaxWidth().height(4.dp).background(Color.DarkGray.copy(alpha = 0.2f), RoundedCornerShape(1.dp)).clip(RoundedCornerShape(1.dp))
            ) {
                Box(modifier = Modifier.fillMaxWidth( (heat / 100f).toFloat().coerceIn(0f, 1f) ).fillMaxHeight().background(if (heat > 90) ErrorRed else color.copy(alpha = 0.6f)))
                Box(modifier = Modifier.align(Alignment.CenterEnd).fillMaxWidth( ( (100f - integrity) / 100f).toFloat().coerceIn(0f, 1f) ).fillMaxHeight().background(ErrorRed.copy(alpha = 0.4f)))
            }
            
            // v2.9.88: Unified Gauge row with centered Sync status
            Row(modifier = Modifier.fillMaxWidth().padding(top = 2.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("THERM: ${heat.toInt()}°C", color = if (heat > 90) ErrorRed else Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(70.dp))
                
                val isSyncing by viewModel.isNarrativeSyncing.collectAsState()
                if (isSyncing) {
                    Text(
                        text = "[ SYNCING FRAGMENTS ]",
                        color = color.copy(alpha = 0.9f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.graphicsLayer {
                            alpha = (Math.sin(waveAnim.toDouble() * 5).toFloat() * 0.4f + 0.6f)
                        }
                    )
                } else {
                    Spacer(modifier = Modifier.width(70.dp))
                }
                
                Text("INTEG: ${integrity.toInt()}%", color = if (integrity < 30) ErrorRed else Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.End, modifier = Modifier.width(70.dp).clickable { onRepair() })
            }
        }
    }
}
