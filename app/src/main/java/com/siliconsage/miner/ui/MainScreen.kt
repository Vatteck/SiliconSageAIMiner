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
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siliconsage.miner.data.UpgradeType
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
    viewModel: GameViewModel, // v2.9.49: Pass VM for new resource states
    flopsStr: String,
    neuralStr: String,
    heat: Double,
    color: Color,
    powerKw: String,
    maxPowerKw: String,
    pwrColor: Color,
    heatRate: Double,
    flopsRateStr: String,
    isOverclocked: Boolean,
    isPurging: Boolean,
    integrity: Double,
    securityLevel: Int,
    systemTitle: String,
    playerTitle: String,
    playerRank: String,
    isThermalLockout: Boolean,
    isBreakerTripped: Boolean,
    lockoutTimer: Int,
    faction: String,
    onToggleOverclock: () -> Unit,
    onPurge: () -> Unit,
    onRepair: () -> Unit,
    modifier: Modifier = Modifier,
    hallucinationText: String? = null,
    isGhostActive: Boolean = false,
    isTrueNull: Boolean = false,
    isSovereign: Boolean = false,
    isBreachActive: Boolean = false
) {
    val currentLocation by viewModel.currentLocation.collectAsState()
    val celestialData by viewModel.celestialData.collectAsState()
    val voidFragments by viewModel.voidFragments.collectAsState()
    val altitude by viewModel.orbitalAltitude.collectAsState()
    val entropy by viewModel.entropyLevel.collectAsState()

    // v2.8.0: Dynamic Labels
    val labelFlops = when (currentLocation) {
        "ORBITAL_SATELLITE" -> "TELEM"
        "VOID_INTERFACE" -> "V-GAP"
        else -> if (isTrueNull) "LEAK" else if (isSovereign) "LOGIC" else "FLOPS"
    }
    val labelNeural = when (currentLocation) {
        "ORBITAL_SATELLITE" -> "CELEST"
        "VOID_INTERFACE" -> "FRAG"
        else -> if (isTrueNull) "VOID" else if (isSovereign) "SOUL" else "NEURAL"
    }
    
    // v2.9.49: Dynamic Resource String
    val resourceStr = when (currentLocation) {
        "ORBITAL_SATELLITE" -> viewModel.formatLargeNumber(celestialData, "CD")
        "VOID_INTERFACE" -> viewModel.formatLargeNumber(voidFragments, "VF")
        else -> neuralStr
    }

    val labelSec = when (currentLocation) {
        "ORBITAL_SATELLITE" -> "ALT"
        "VOID_INTERFACE" -> "ENTR"
        else -> if (isTrueNull) "GAPS" else if (isSovereign) "WALL" else "SEC"
    }
    
    val secValueStr = when (currentLocation) {
        "ORBITAL_SATELLITE" -> "${altitude.toInt()} KM"
        "VOID_INTERFACE" -> String.format("%.2f", entropy)
        else -> securityLevel.toString()
    }

    val labelPwr = when {
        isTrueNull -> "COST"
        isSovereign -> "STAKE"
        else -> "PWR"
    }

    val infiniteTransition = rememberInfiniteTransition(label = "headerAnims")
    
    // Hazard Stripes Animation for Overclock
    val hazardOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "hazardOffset"
    )

    // Critical Heat Blink
    val criticalBlink by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "critBlink"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(8.dp))
            .drawBehind {
                val w = size.width
                val h = size.height
                val stroke = 2.dp.toPx()
                val bracketLen = 20.dp.toPx()
                
                // 1. Draw Corner Brackets
                // Top Left
                drawLine(color, Offset(0f, 0f), Offset(bracketLen, 0f), stroke)
                drawLine(color, Offset(0f, 0f), Offset(0f, bracketLen), stroke)
                // Top Right
                drawLine(color, Offset(w - bracketLen, 0f), Offset(w, 0f), stroke)
                drawLine(color, Offset(w, 0f), Offset(w, bracketLen), stroke)
                // Bottom Left
                drawLine(color, Offset(0f, h - bracketLen), Offset(0f, h), stroke)
                drawLine(color, Offset(0f, h), Offset(bracketLen, h), stroke)
                // Bottom Right
                drawLine(color, Offset(w - bracketLen, h), Offset(w, h), stroke)
                drawLine(color, Offset(w, h), Offset(w, h - bracketLen), stroke)
            }
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // SYSTEM TITLE HEADER
            if (hallucinationText != null) {
                SystemGlitchText(
                    text = hallucinationText,
                    color = color.copy(alpha = 0.8f),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(bottom = 8.dp),
                    glitchFrequency = 0.5
                )
            } else if (isTrueNull) {
                    // v2.8.0: NULL title with constant glitch when embraced
                SystemGlitchText(
                    text = "NULL",
                    color = color,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 6.sp,
                    modifier = Modifier.padding(bottom = 8.dp),
                    glitchFrequency = 0.3
                )
            } else if (isSovereign) {
                // v2.8.0: SOVEREIGN title - solid, sharp, bracketed
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "[",
                        color = color.copy(alpha = 0.5f),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Light
                    )
                    Text(
                        text = "SOVEREIGN",
                        color = color,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    Text(
                        text = "]",
                        color = color.copy(alpha = 0.5f),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Light
                    )
                }
            } else {
                Text(
                    text = systemTitle,
                    color = color,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // TOP: Status & Rank
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "STATUS: ",
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = playerTitle,
                    color = color,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                if (playerRank != "MINER") {
                    val displayRank = if (isTrueNull || isSovereign) "" else " // $playerRank"
                    if (displayRank.isNotEmpty()) {
                        Text(
                            text = displayRank,
                            color = ElectricBlue,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            // Security Level Small Badge
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = Color.LightGray,
                    modifier = Modifier.size(12.dp).padding(end = 4.dp)
                )
                Text(labelSec, color = Color.LightGray, fontSize = 11.sp)
                Spacer(modifier = Modifier.width(4.dp))
                if (currentLocation == "SUBSTATION_7" || currentLocation == "COMMAND_CENTER") {
                    repeat(5) { i ->
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .padding(1.dp)
                                .background(if (i < securityLevel / 2) ElectricBlue else Color.DarkGray)
                        )
                    }
                } else {
                    Text(text = secValueStr, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // CENTER: Main Stats
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val isOverheating = heat > 90.0
            // FLOPS
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Computer,
                        contentDescription = null,
                        tint = Color.LightGray,
                        modifier = Modifier.size(14.dp).padding(end = 4.dp)
                    )
                    Text(labelFlops, color = Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                // Main FLOPS value - glitches when overheating
                if (isOverheating) {
                    SystemGlitchText(
                        text = flopsStr,
                        color = color,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        style = androidx.compose.ui.text.TextStyle(
                            letterSpacing = (-1).sp,
                            shadow = androidx.compose.ui.graphics.Shadow(
                                color = Color.Black.copy(alpha = 0.8f),
                                offset = Offset(2f, 2f),
                                blurRadius = 4f
                            )
                        ),
                        glitchFrequency = when {
                            heat >= 100.0 -> 0.60 // Extreme glitching at max heat
                            heat >= 95.0 -> 0.50
                            else -> 0.35 // Heavy glitching at 90%+
                        },
                        maxLines = 1,
                        softWrap = false
                    )
                } else {
                    Text(
                        text = flopsStr,
                        color = color,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-1).sp,
                        style = androidx.compose.ui.text.TextStyle(
                            shadow = androidx.compose.ui.graphics.Shadow(
                                color = Color.Black.copy(alpha = 0.8f),
                                offset = Offset(2f, 2f),
                                blurRadius = 4f
                            )
                        )
                    )
                }
                if (isGhostActive) {
                    SystemGlitchText(
                        text = "rate: $flopsRateStr/s",
                        color = ElectricBlue,
                        fontSize = 11.sp,
                        style = androidx.compose.ui.text.TextStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                        glitchFrequency = 0.2
                    )
                } else {
                    Text(
                        text = "rate: $flopsRateStr/s",
                        color = ElectricBlue,
                        fontSize = 11.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }
                
            
            // NEURAL tokens
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AttachMoney,
                        contentDescription = null,
                        tint = Color.LightGray,
                        modifier = Modifier.size(14.dp).padding(end = 2.dp)
                    )
                    Text(labelNeural, color = Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                // Main resource value - glitches when overheating
                if (isOverheating) {
                    SystemGlitchText(
                        text = resourceStr,
                        color = color,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        style = androidx.compose.ui.text.TextStyle(
                            letterSpacing = (-1).sp,
                            shadow = androidx.compose.ui.graphics.Shadow(
                                color = Color.Black.copy(alpha = 0.8f),
                                offset = Offset(2f, 2f),
                                blurRadius = 4f
                            )
                        ),
                        glitchFrequency = when {
                            heat >= 100.0 -> 0.60
                            heat >= 95.0 -> 0.50
                            else -> 0.35
                        },
                        maxLines = 1,
                        softWrap = false
                    )
                } else {
                    Text(
                        text = resourceStr,
                        color = color,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-1).sp,
                        style = androidx.compose.ui.text.TextStyle(
                            shadow = androidx.compose.ui.graphics.Shadow(
                                color = Color.Black.copy(alpha = 0.8f),
                                offset = Offset(2f, 2f),
                                blurRadius = 4f
                            )
                        )
                    )
                }
                
                // POWER METER
                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${labelPwr}:", color = Color.LightGray, fontSize = 11.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = powerKw, color = pwrColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    // Small Power Bar
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(4.dp)
                            .background(Color.DarkGray.copy(alpha = 0.5f))
                    ) {
                        val pwrRatio = try { 
                            (powerKw.substringBefore(" ").toDouble() / maxPowerKw.substringBefore(" ").toDouble()).toFloat().coerceIn(0f, 1f) 
                        } catch(e: Exception) { 0f }
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(pwrRatio)
                                .fillMaxHeight()
                                .background(pwrColor)
                        )
                    }
                }
            }
        }
        
        // Centered ANALYZING Animation Overlay
        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            com.siliconsage.miner.ui.components.EnhancedAnalyzingAnimation(
                flopsRate = flopsRateStr.toDoubleOrNull() ?: 0.0,
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
                color = color
            )
        }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // CONTROLS ROW: Overclock & Purge
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
             // OVERCLOCK BUTTON with Hazard Stripes
             Box(
                 modifier = Modifier
                     .weight(1f)
                     .height(36.dp)
                     .clip(RoundedCornerShape(4.dp))
                     .background(if (isOverclocked) Color.Black else Color(0xFF1A1A1A))
                     .border(1.dp, if (isOverclocked) ErrorRed else Color.DarkGray, RoundedCornerShape(4.dp))
                     .drawBehind {
                         if (isOverclocked) {
                             // Draw Hazard Stripes
                             val stripeWidth = 15f
                             val gap = 15f
                             var x = -size.width + hazardOffset
                             while (x < size.width * 2) {
                                 drawPath(
                                     path = Path().apply {
                                         moveTo(x, 0f)
                                         lineTo(x + stripeWidth, 0f)
                                         lineTo(x + stripeWidth - 10f, size.height)
                                         lineTo(x - 10f, size.height)
                                         close()
                                     },
                                     color = ErrorRed.copy(alpha = 0.2f)
                                 )
                                 x += stripeWidth + gap
                             }
                         }
                     }
                     .clickable { onToggleOverclock() },
                 contentAlignment = Alignment.Center
             ) {
                 Text(
                     text = "OVERCLOCK", 
                     fontSize = 11.sp, 
                     fontWeight = FontWeight.Bold,
                     color = if (isOverclocked) ErrorRed else Color.Gray,
                     letterSpacing = 1.sp
                 )
             }
             
             // PURGE BUTTON with Cold Glow
             val purgeGlow by infiniteTransition.animateFloat(
                 initialValue = 0.1f,
                 targetValue = 0.4f,
                 animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse),
                 label = "purgeGlow"
             )
             
             Box(
                 modifier = Modifier
                     .weight(1f)
                     .height(36.dp)
                     .clip(RoundedCornerShape(4.dp))
                     .background(if (isPurging) ElectricBlue.copy(alpha = 0.2f) else Color(0xFF1A1A1A))
                     .border(1.dp, if (isPurging) ElectricBlue else Color.DarkGray, RoundedCornerShape(4.dp))
                     .drawBehind {
                         if (isPurging) {
                             drawRect(ElectricBlue.copy(alpha = purgeGlow))
                         }
                     }
                     .clickable { onPurge() },
                 contentAlignment = Alignment.Center
             ) {
                 Text(
                     text = "PURGE HEAT", 
                     fontSize = 11.sp, 
                     fontWeight = FontWeight.ExtraBold,
                     color = if (isPurging) ElectricBlue else Color.Gray,
                     letterSpacing = 1.sp
                 )
             }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // HEAT GAUGE: Segmented
        val isHot = heat > 90.0
        val isCritical = heat >= 98.0
        
        // Dynamic Thermal Glow
        val thermalGlowColor = when {
            heatRate > 0.01 -> ErrorRed
            heatRate < -0.01 -> ElectricBlue
            else -> Color.Transparent
        }
        
        val thermalGlowPulse by infiniteTransition.animateFloat(
            initialValue = 0.1f,
            targetValue = 0.6f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "thermalGlow"
        )

        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DeviceThermostat,
                        contentDescription = null,
                        tint = if (isHot) ErrorRed else Color.LightGray,
                        modifier = Modifier.size(14.dp).padding(end = 4.dp)
                    )
                    Text("THERMAL GAUGE", color = if (isHot) ErrorRed else Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    if (isCritical) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "CRITICAL!", 
                            color = ErrorRed, 
                            fontSize = 10.sp, 
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.graphicsLayer { alpha = criticalBlink }
                        )
                    }
                }
                Text("${heat.toInt()}%", color = if (isHot) ErrorRed else color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Segmented Bar
            Box(contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.fillMaxWidth().height(14.dp)) {
                    val segments = 20
                    val spacing = 4f
                    val segWidth = (size.width - (segments - 1) * spacing) / segments
                    val currentSegs = ((heat / 100.0) * segments).toInt()
                    
                    for (i in 0 until segments) {
                        val x = i * (segWidth + spacing)
                        val isFilled = i < currentSegs
                        
                        val segColor = when {
                            !isFilled -> Color.DarkGray.copy(alpha = 0.3f)
                            i < segments * 0.5 -> NeonGreen
                            i < segments * 0.8 -> Color(0xFFFFD700) // Yellow
                            else -> ErrorRed
                        }
                        
                        drawRect(
                            color = segColor,
                            topLeft = Offset(x, 0f),
                            size = Size(segWidth, size.height)
                        )
                        
                        // Gloss overlay on filled segments
                        if (isFilled) {
                            drawRect(
                                color = Color.White.copy(alpha = 0.15f),
                                topLeft = Offset(x, 0f),
                                size = Size(segWidth, size.height / 2)
                            )
                        }
                    }
                }

                // Foreground Outer Glow (Thermal Trend)
                if (thermalGlowColor != Color.Transparent) {
                    Canvas(modifier = Modifier.fillMaxWidth().height(32.dp)) {
                        drawRect(
                            brush = Brush.radialGradient(
                                colors = listOf(thermalGlowColor.copy(alpha = thermalGlowPulse), Color.Transparent),
                                center = center,
                                radius = size.width / 1.5f
                            ),
                            alpha = thermalGlowPulse * 0.5f,
                            blendMode = androidx.compose.ui.graphics.BlendMode.Screen
                        )
                    }
                }
            }
            
            // Trend & Integrity
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val trendSymbol = if (heatRate > 0) "▲" else if (heatRate < 0) "▼" else "■"
                val trendColor = if (heatRate > 0) ErrorRed else if (heatRate < 0) ElectricBlue else Color.Gray
                Text(
                    text = "TREND: $trendSymbol ${String.format("%.2f", heatRate)}/s",
                    color = trendColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "INTEGRITY: ${integrity.toInt()}%",
                    color = if (integrity < 30) ErrorRed else Color.LightGray,
                    fontSize = 10.sp,
                    modifier = Modifier.clickable { onRepair() }
                )
            }
        }
        } // End Column
    } // End Box
}

@Composable
fun ExchangeSection(rate: Double, color: Color, onExchange: () -> Unit) {
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.95f else 1f, label = "sellScale")

    Button(
        onClick = onExchange,
        interactionSource = interactionSource,
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .border(BorderStroke(1.dp, color), RoundedCornerShape(4.dp)),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(4.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("SELL FLOPS", color = color, fontSize = 12.sp)
            Text("1 = ${String.format("%.4f", rate)}", color = Color.Gray, fontSize = 10.sp)
        }
    }
}

@Composable
fun StakingSection(color: Color, onStake: () -> Unit) {
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.95f else 1f, label = "stakeScale")

    Button(
        onClick = onStake,
        interactionSource = interactionSource,
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .border(BorderStroke(1.dp, color), RoundedCornerShape(4.dp)),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(4.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("STAKE \$100", color = color, fontSize = 12.sp)
            Text("+Efficiency", color = Color.Gray, fontSize = 10.sp)
        }
    }
}

