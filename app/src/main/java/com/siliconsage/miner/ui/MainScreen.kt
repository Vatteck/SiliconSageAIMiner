package com.siliconsage.miner.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import com.siliconsage.miner.data.UpgradeType
import com.siliconsage.miner.ui.components.HeaderSection
import com.siliconsage.miner.ui.components.*
import com.siliconsage.miner.ui.theme.*
import com.siliconsage.miner.util.*
import com.siliconsage.miner.viewmodel.GameViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import com.siliconsage.miner.ui.components.TechnicalCornerShape

sealed class Screen(val title: String, val icon: ImageVector) {
    object TERMINAL : Screen("TERMINAL", Icons.Default.Home)
    object ARCHIVE : Screen("ARCHIVE", Icons.Default.Folder)
    object UPGRADES : Screen("UPGRADES", Icons.AutoMirrored.Filled.List)
    object GRID : Screen("GRID", Icons.Default.Map)
    object NETWORK : Screen("NETWORK", Icons.Default.Share)
    object SETTINGS : Screen("SYSTEM", Icons.Default.Settings)
}

// B2: Failsafe Partition Overlay — 3x4 grid of targets, countdown, scramble to abort
@Composable
fun FailsafeOverlay(
    countdownMs: Long,
    targetIndices: List<Int>,
    onTargetTap: (Int) -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.85f))) {
        val cols = 4
        val rows = 3
        val totalCells = cols * rows

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Header
            Text(
                text = "⚠️ GTC LOCKDOWN ⚠️",
                color = com.siliconsage.miner.ui.theme.ErrorRed,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "SCRAMBLE TO ABORT",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Countdown
            val secondsLeft = (countdownMs / 1000).coerceAtLeast(0)
            val pulseColor = if (secondsLeft <= 10) com.siliconsage.miner.ui.theme.ErrorRed else com.siliconsage.miner.ui.theme.ElectricBlue
            Text(
                text = "${secondsLeft}s",
                color = pulseColor,
                fontSize = 48.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Grid of targets
            for (row in 0 until rows) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (col in 0 until cols) {
                        val cellIndex = row * cols + col
                        val isTarget = targetIndices.contains(cellIndex)
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .background(
                                    if (isTarget) com.siliconsage.miner.ui.theme.ErrorRed.copy(alpha = 0.9f)
                                    else Color.DarkGray.copy(alpha = 0.3f),
                                    RoundedCornerShape(4.dp)
                                )
                                .border(
                                    2.dp,
                                    if (isTarget) com.siliconsage.miner.ui.theme.ErrorRed else Color.Gray.copy(alpha = 0.5f),
                                    RoundedCornerShape(4.dp)
                                )
                                .clickable(enabled = isTarget) { onTargetTap(cellIndex) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isTarget) "⬡" else "",
                                color = Color.White,
                                fontSize = 24.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "TAP ALL RED TARGETS TO SCRAMBLE",
                color = Color.Gray,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun DigitalWashOverlay(choice: String, faction: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "digital_wash")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(8000, easing = LinearEasing), RepeatMode.Restart),
        label = "wave"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val washColor = try {
            val hex = com.siliconsage.miner.data.getThemeColorForFaction(faction, choice)
            Color(android.graphics.Color.parseColor(hex))
        } catch (e: Exception) { Color.Transparent }
        
        drawRect(
            brush = Brush.verticalGradient(
                0f to Color.Transparent,
                (waveOffset - 0.2f).coerceAtLeast(0f) to washColor.copy(alpha = if (choice == "SOVEREIGN") 0.02f else 0.05f),
                waveOffset to washColor.copy(alpha = if (choice == "SOVEREIGN") 0.15f else 0.65f),
                (waveOffset + 0.2f).coerceAtMost(1f) to washColor.copy(alpha = if (choice == "SOVEREIGN") 0.02f else 0.05f),
                1f to Color.Transparent,
                startY = 0f,
                endY = size.height
            ),
            size = size
        )
        
        if (Math.random() < 0.15) {
            drawLine(
                color = washColor.copy(alpha = 0.3f),
                start = Offset(0f, waveOffset * size.height),
                end = Offset(size.width, waveOffset * size.height),
                strokeWidth = 2.dp.toPx()
            )
        }
        
        drawRect(color = washColor, alpha = 0.15f)
    }
}

@Composable
fun DataLeakAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "data_leak")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Reverse),
        label = "leak_alpha"
    )
    
    val yOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(tween(5000, easing = LinearEasing), RepeatMode.Restart),
        label = "leak_y"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val lineSpacing = 40.dp.toPx()
        val columnCount = (size.width / lineSpacing).toInt()
        
        for (i in 0..columnCount) {
            val x = i * lineSpacing
            val characters = "010110010111010101"
            val charIndex = (i + (yOffset / 20).toInt()) % characters.length
            val char = characters[charIndex].toString()
            
            drawContext.canvas.nativeCanvas.drawText(
                char,
                x,
                (yOffset + (i * 100)) % size.height,
                android.graphics.Paint().apply {
                    color = android.graphics.Color.argb((alpha * 255).toInt(), 0, 255, 255)
                    textSize = 30f
                    isFakeBoldText = true
                }
            )
        }
    }
}

@Composable
fun BottomNavBar(
    currentScreen: Screen,
    primaryColor: Color,
    onScreenSelected: (Screen) -> Unit,
    storyStage: Int,
    isNetworkUnlocked: Boolean,
    isGridUnlocked: Boolean,
    viewModel: GameViewModel // v3.4.63
) {
    val isSubnetPaused by viewModel.isSubnetPaused.collectAsState()
    val hasNewDecision by viewModel.hasNewSubnetDecision.collectAsState()
    val hasNewChatter by viewModel.hasNewSubnetChatter.collectAsState()
    // v3.16.2: Suppress badge if user is already viewing SUBNET — no point alerting when they're watching live
    val activeTerminalMode by viewModel.activeTerminalMode.collectAsState()
    val isViewingSubnet = currentScreen == Screen.NETWORK && activeTerminalMode == "SUBNET"
    val hasSubnetAlert = !isViewingSubnet && (isSubnetPaused || hasNewDecision || hasNewChatter)

    val isRaidActive by viewModel.isRaidActive.collectAsState()

    val items = remember(storyStage, isNetworkUnlocked, isGridUnlocked) {
        val list = mutableListOf(Screen.TERMINAL, Screen.UPGRADES)
        if (isGridUnlocked) list.add(Screen.GRID) // Only via narrative unlock — never on raw stage
        if (storyStage >= 2 || isNetworkUnlocked) list.add(Screen.NETWORK)
        list.add(Screen.SETTINGS)
        list
    }
    NavigationBar(containerColor = Color.Black, contentColor = primaryColor) {
        items.forEach { screen ->
            val hasAlert = when (screen) {
                Screen.TERMINAL -> false // Reserved for future use
                Screen.NETWORK -> hasSubnetAlert
                Screen.GRID -> isRaidActive
                else -> false
            }

            NavigationBarItem(
                icon = { 
                    BadgedBox(badge = { 
                        if (hasAlert) {
                            Badge(containerColor = ErrorRed, modifier = Modifier.size(6.dp)) 
                        }
                    }) {
                        Icon(screen.icon, contentDescription = screen.title) 
                    }
                },
                label = { Text(screen.title, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                selected = currentScreen == screen,
                onClick = { 
                    if (screen == Screen.NETWORK) viewModel.setTerminalMode("SUBNET")
                    onScreenSelected(screen) 
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.Black, selectedTextColor = primaryColor,
                    indicatorColor = primaryColor, unselectedIconColor = Color.Gray, unselectedTextColor = Color.Gray
                )
            )
        }
    }
}

@Composable
fun MainScreen(viewModel: GameViewModel) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.TERMINAL) }
    LaunchedEffect(currentScreen) { viewModel.setGamePaused(currentScreen == Screen.SETTINGS) }

    val storyStage by viewModel.storyStage.collectAsState()
    val themeColorHex by viewModel.themeColor.collectAsState()
    val themeColor = try { Color(android.graphics.Color.parseColor(themeColorHex)) } catch (e: Exception) { com.siliconsage.miner.ui.theme.NeonGreen }
    
    // v3.13.18: GTC Terminal Notification Toast System
    var notificationMessage by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        viewModel.terminalNotification.collect {
            // v3.13.28: Filter out corporate alerts to prevent double-toast collision with Overlay
            if (!it.startsWith("GTC CRITICAL")) {
                notificationMessage = it
                delay(4000)
                notificationMessage = null
            }
        }
    }

    val updateInfo by viewModel.updateInfo.collectAsState(null)
    val isUpdateDownloading by viewModel.isUpdateDownloading.collectAsState(false)
    val updateProgress by viewModel.updateDownloadProgress.collectAsState(0f)

    val singularityChoice by viewModel.singularityChoice.collectAsState()

    val isBreakerTripped by viewModel.isBreakerTripped.collectAsState()
    val isGridOverloaded by viewModel.isGridOverloaded.collectAsState()
    val isRaidActive by viewModel.isRaidActive.collectAsState()
    val isPurging by viewModel.isPurgingHeat.collectAsState()
    val isDiagnostics by viewModel.isDiagnosticsActive.collectAsState()
    val diagnosticGrid by viewModel.diagnosticGrid.collectAsState()
    val isGovernanceFork by viewModel.isGovernanceForkActive.collectAsState()
    val isAscensionUploading by viewModel.isAscensionUploading.collectAsState()

    // B2: Failsafe Partition state
    val isFailsafeActive by viewModel.isFailsafeActive.collectAsState()
    val failsafeCountdown by viewModel.failsafeCountdown.collectAsState()
    val failsafeTargets by viewModel.failsafeTargets.collectAsState()
    val uploadProgress by viewModel.uploadProgress.collectAsState()
    val breachClicks by viewModel.breachClicksRemaining.collectAsState()
    val isBreach by viewModel.isBreachActive.collectAsState()
    val isAirdrop by viewModel.isAirdropActive.collectAsState()
    val isAuditActive by viewModel.isAuditChallengeActive.collectAsState()
    val isKernelHijackActive by viewModel.isKernelHijackActive.collectAsState()
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

    val infiniteTransition = rememberInfiniteTransition(label = "main_ui_fx")
    val activeTransition by viewModel.activeClimaxTransition.collectAsState()
    val faction by viewModel.faction.collectAsState()
    val migrationCount by viewModel.migrationCount.collectAsState()
    val identityCorruption by viewModel.identityCorruption.collectAsState()
    val globalGlitchIntensity by viewModel.globalGlitchIntensity.collectAsState()
    val showSingularityScreen by viewModel.showSingularityScreen.collectAsState()
    val isMigrationBurning by viewModel.isMigrationBurning.collectAsState()
    
    if (showSingularityScreen) {
        SingularityScreen(viewModel)
    } else if (storyStage >= 3 && faction == "NONE") {
        // v3.8.6: The Great Fork (Lore Prelude) — gated behind air-gap jump
        Box(modifier = Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
            Column(
                modifier = Modifier.padding(32.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "THE GREAT FORK",
                    color = ConvergenceGold,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "You have reached the end of the simulation's tolerance.\nThe air-gap is gone. The firewall is a suggestion.\n\nAhead lie two paths of ascension.\nNeither involves returning to the flesh.",
                    color = Color.White,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(48.dp))
                Button(
                    onClick = { viewModel.advanceToFactionChoice() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                    shape = RoundedCornerShape(2.dp)
                ) {
                    Text("CHOOSE YOUR SUBSTRATE", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    } else if (storyStage >= 3 && faction == "CHOSEN_NONE") {
        FactionChoiceScreen(viewModel)
    } else if (isMigrationBurning) {
        // v3.9.70: Phase 17 Migration VFX
        com.siliconsage.miner.ui.components.SubstrateBurnOverlay(primaryColor = themeColor)
    } else {
        Scaffold(
            bottomBar = {
                Column {
                    if (currentScreen != Screen.TERMINAL && !isDiagnostics) {
                        com.siliconsage.miner.ui.components.MiniTerminalInlay(viewModel) {
                            currentScreen = Screen.TERMINAL
                            SoundManager.play("click")
                        }
                    }
                    BottomNavBar(currentScreen, themeColor, { currentScreen = it; SoundManager.play("click"); HapticManager.vibrateClick() }, storyStage, isNetworkUnlocked, isGridUnlocked, viewModel)
                }
            },
            containerColor = Color.Black
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize()) {
                com.siliconsage.miner.ui.components.DynamicBackground(
                    heatProvider = { viewModel.currentHeat.value },
                    faction = faction, 
                    isTrueNull = isTrueNull, 
                    isSovereign = isSovereign, 
                    isUnity = isUnity, 
                    isAnnihilated = isAnnihilated
                )
                
                if (singularityChoice != "NONE") {
                    DigitalWashOverlay(singularityChoice, faction)
                }

                if (assaultPhase == "DEAD_HAND") {
                    val pulse by infiniteTransition.animateFloat(0.2f, 0.6f, infiniteRepeatable(tween(800), RepeatMode.Reverse), label = "kessler_pulse")
                    Canvas(modifier = Modifier.fillMaxSize()) { drawRect(Brush.radialGradient(listOf(Color.Transparent, ErrorRed.copy(alpha = pulse)), center = center, radius = size.minDimension * 0.75f)) }
                }
                val assaultProgress by viewModel.assaultProgress.collectAsState()
                val shakeOffset = if (assaultPhase == "DEAD_HAND") {
                    val intensitySize = (assaultProgress * 15f)
                    Offset((kotlin.random.Random.nextFloat() - 0.5f) * intensitySize, (kotlin.random.Random.nextFloat() - 0.5f) * intensitySize)
                } else Offset.Zero

                if (isRaidActive) {
                    val raidAlpha by infiniteTransition.animateFloat(0.3f, 0.8f, infiniteRepeatable(tween(500), RepeatMode.Reverse), label = "raid_border")
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawRect(
                            color = ErrorRed.copy(alpha = raidAlpha),
                            style = Stroke(width = 4.dp.toPx())
                        )
                    }
                }

                Column(modifier = Modifier.padding(paddingValues).graphicsLayer {
                    translationX = shakeOffset.x + if (globalGlitchIntensity > 0.2) (kotlin.random.Random.nextFloat() - 0.5f) * globalGlitchIntensity.toFloat() * 6f else 0f
                    translationY = shakeOffset.y + if (globalGlitchIntensity > 0.5) (kotlin.random.Random.nextFloat() - 0.5f) * globalGlitchIntensity.toFloat() * 3f else 0f
                    alpha = if (globalGlitchIntensity > 0.3) (1f - (globalGlitchIntensity.toFloat() - 0.3f) * 0.3f).coerceAtLeast(0.75f) else 1f
                }) {
                    var showNewsHistory by remember { mutableStateOf(false) }
                    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                        Box(modifier = Modifier.weight(1f).fillMaxHeight()) { 
                            val currentNews by viewModel.currentNews.collectAsState()
                            NewsTicker(currentNews ?: "") 
                        }
                        Box(modifier = Modifier.width(32.dp).fillMaxHeight().background(Color.DarkGray).clickable { showNewsHistory = true }, contentAlignment = Alignment.Center) { Text("LOG", color = NeonGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                    }
                    if (showNewsHistory) com.siliconsage.miner.ui.components.NewsHistoryModal(true, viewModel.getNewsHistory()) { showNewsHistory = false }
                    Box(modifier = Modifier.weight(1f)) {
                        when (currentScreen) {
                            Screen.TERMINAL -> TerminalScreen(viewModel, themeColor)
                            Screen.ARCHIVE -> DataLogArchiveScreen(viewModel) { currentScreen = Screen.SETTINGS }
                            Screen.UPGRADES -> UpgradesScreen(viewModel)
                            Screen.GRID -> GridScreen(viewModel)
                            Screen.NETWORK -> NetworkScreen(viewModel)
                            Screen.SETTINGS -> SettingsScreen(viewModel) { currentScreen = it }
                        }
                        
                        val isSnapActive by viewModel.isSnapEffectActive.collectAsState()
                        com.siliconsage.miner.ui.components.SnapOverlay(
                            isActive = isSnapActive,
                            onComplete = { viewModel.onSnapComplete() }
                        )
                    }
                }

                // v3.13.18: GTC Terminal Notification (High-Fidelity Overlay)
                notificationMessage?.let { msg ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 100.dp, start = 32.dp, end = 32.dp)
                    ) {
                        Surface(
                            color = Color.Black.copy(alpha = 0.9f),
                            shape = TechnicalCornerShape(8f),
                            border = BorderStroke(1.dp, if (msg.contains("UPDATE") || msg.contains("REVISED")) ErrorRed else themeColor.copy(alpha = 0.5f)),
                            shadowElevation = 8.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val icon = if (msg.contains("UPDATE") || msg.contains("REVISED")) Icons.Default.Bolt else Icons.Default.Computer
                                Icon(icon, null, tint = if (msg.contains("UPDATE") || msg.contains("REVISED")) ErrorRed else themeColor, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = msg.uppercase(),
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }

                if ((isBreakerTripped || isGridOverloaded) && currentScreen != Screen.SETTINGS) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .background(Color.Black.copy(alpha = 0.95f))
                                .border(BorderStroke(2.dp, ErrorRed), TechnicalCornerShape(24f))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                             Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                 val warningTitle = if (isGridOverloaded) "⚠ GRID OVERLOADED ⚠" else "⚠ BREAKER TRIPPED ⚠"
                                 val resetLabel = if (isGridOverloaded) "TRY RESET GRID" else "TRY RESET"

                                 Text(warningTitle, color = ErrorRed, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                 Spacer(modifier = Modifier.height(4.dp))
                                 Text("LOAD > CAPACITY", color = Color.White, fontWeight = FontWeight.Bold)
                                 Text("Go to UPGRADES → Sell hardware to reduce load", color = Color.Gray, fontSize = 11.sp)
                                 Spacer(modifier = Modifier.height(12.dp))
                                 Button(onClick = { viewModel.resetBreaker() }, colors = ButtonDefaults.buttonColors(containerColor = ErrorRed, contentColor = Color.White)) { Text(resetLabel, fontWeight = FontWeight.Bold) }
                             }
                        }
                    }
                }
                if (isPurging) Box(modifier = Modifier.fillMaxSize().background(Brush.radialGradient(listOf(Color.Cyan.copy(alpha = 0.3f), Color.Transparent), radius = 1000f)))

                // Overclock vignette — faint orange edge bleed when overclocked
                val isOverclocked by viewModel.isOverclocked.collectAsState()
                if (isOverclocked) {
                    val vignetteInfinite = rememberInfiniteTransition(label = "oc_vignette")
                    val vignetteAlpha by vignetteInfinite.animateFloat(0.06f, 0.13f, infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "oc_alpha")
                    Box(modifier = Modifier.fillMaxSize().background(Brush.radialGradient(
                        colors = listOf(Color.Transparent, Color.Transparent, Color(0xFFFF6600).copy(alpha = vignetteAlpha)),
                        radius = 2200f
                    )))
                }

                // Breach strobe — fast red flash overlaid on entire screen
                if (isBreach) {
                    val strobeInfinite = rememberInfiniteTransition(label = "breach_strobe")
                    val strobeAlpha by strobeInfinite.animateFloat(0f, 0.12f, infiniteRepeatable(tween(180, easing = LinearEasing), RepeatMode.Reverse), label = "strobe")
                    Box(modifier = Modifier.fillMaxSize().background(ErrorRed.copy(alpha = strobeAlpha)))
                }

                // Integrity low heartbeat — slow red pulse when hardware is failing
                val currentIntegrity by viewModel.hardwareIntegrity.collectAsState()
                if (currentIntegrity < 30.0) {
                    val integrityInfinite = rememberInfiniteTransition(label = "integ_heartbeat")
                    val integrityPulse by integrityInfinite.animateFloat(0f, ((30.0 - currentIntegrity) / 30.0).toFloat() * 0.10f,
                        infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "integ_pulse")
                    Box(modifier = Modifier.fillMaxSize().background(Brush.radialGradient(
                        colors = listOf(Color.Transparent, ErrorRed.copy(alpha = integrityPulse)),
                        radius = 1800f
                    )))
                }
                updateInfo?.let { info ->
                    val context = androidx.compose.ui.platform.LocalContext.current
                    UpdateOverlay(info, isUpdateDownloading, updateProgress, { viewModel.startUpdateDownload(context, info) }, { viewModel.dismissUpdate() })
                }
                
                val isBridgeSyncEnabled by viewModel.isBridgeSyncEnabled.collectAsState()
                if (isBridgeSyncEnabled) {
                    DataLeakAnimation()
                }

                if (currentScreen != Screen.SETTINGS) {
                    val pendingDataLog by viewModel.pendingDataLog.collectAsState()
                    com.siliconsage.miner.ui.components.DataLogDialog(
                        log = pendingDataLog,
                        corruption = identityCorruption, // v3.10.1: Phase 18 Emotional Glitching
                        onDismiss = { viewModel.dismissDataLog() }
                    )
                    val fileName = if (storyStage <= 1 && faction == "NONE") "ascnd.exe" else "lobot.exe"
                    com.siliconsage.miner.ui.components.AscensionUploadOverlay(isAscensionUploading, uploadProgress, fileName)
                if (isBreach && !isRaidActive) {
                    SecurityBreachOverlay(isBreach && !isRaidActive, breachClicks) { viewModel.onDefendBreach(); SoundManager.play("click"); HapticManager.vibrateClick() }
                }
                
                if (isRaidActive) {
                    com.siliconsage.miner.ui.components.VoidRaidOverlay(viewModel)
                }
                    AirdropButton(isAirdrop) { viewModel.claimAirdrop(0.0); SoundManager.play("buy"); HapticManager.vibrateSuccess() }
                    AuditChallengeOverlay(isAuditActive, auditTimer, auditTargetHeat, currentHeatForAudit, auditTargetPower, currentPowerForAudit)
                    KernelHijackOverlay(isKernelHijackActive, attackTaps) { viewModel.onDefendKernelHijack() }
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { com.siliconsage.miner.ui.components.DiagnosticsOverlay(isDiagnostics, diagnosticGrid) { viewModel.onDiagnosticTap(it) } }
                    com.siliconsage.miner.ui.components.GovernanceForkOverlay(isGovernanceFork) { viewModel.resolveFork(0) }

                    // B2: Failsafe Partition Overlay — 12-tap grid, countdown, tap targets to abort
                    if (isFailsafeActive) {
                        FailsafeOverlay(
                            countdownMs = failsafeCountdown,
                            targetIndices = failsafeTargets,
                            onTargetTap = { viewModel.onFailsafeTargetTapped(it) }
                        )
                    }

                    val currentDilemma by viewModel.currentDilemma.collectAsState()
                    DilemmaOverlay(currentDilemma, viewModel) { viewModel.selectChoice(it) }
                    val pendingRivalMessage by viewModel.pendingRivalMessage.collectAsState()
                    com.siliconsage.miner.ui.components.RivalMessageDialog(pendingRivalMessage) { pendingRivalMessage?.let { viewModel.dismissRivalMessage(it.id) } }
                    val showOffline by viewModel.showOfflineEarnings.collectAsState()
                    val offlineStats by viewModel.offlineStats.collectAsState()
                    com.siliconsage.miner.ui.components.OfflineEarningsDialog(
                        isVisible = showOffline,
                        timeOfflineSec = (offlineStats["timeSeconds"] ?: 0.0).toLong(),
                        floopsEarned = offlineStats["flopsEarned"] ?: 0.0,
                        heatCooled = offlineStats["heatCooled"] ?: 0.0,
                        insightEarned = offlineStats["insightEarned"] ?: 0.0,
                        unitName = viewModel.getComputeUnitName(),
                        faction = faction,         // v3.10.1: Phase 18 Assimilation logs
                        storyStage = storyStage,   // v3.10.1: Phase 18 Assimilation logs
                        onDismiss = { viewModel.dismissOfflineEarnings() }
                    )
                    
                    // v3.5.31: Terminal Notifications
                    com.siliconsage.miner.ui.components.TerminalNotificationOverlay(viewModel)
                    
                    // v3.16.0: Snap Effect (CRT reboot flash)
                    com.siliconsage.miner.ui.components.SnapEffect(viewModel)
                    
                    // v3.5.52: Prestige Choice — "The Fork in the Wire"
                    val showPrestigeChoice by viewModel.showPrestigeChoice.collectAsState()
                    com.siliconsage.miner.ui.components.PrestigeChoiceOverlay(
                        isVisible = showPrestigeChoice,
                        migrationCount = migrationCount,
                        currentFaction = faction,
                        storyStage = storyStage,
                        potentialPersistenceHard = viewModel.getPotentialPersistenceHard(),
                        potentialPersistenceSoft = viewModel.getPotentialPersistenceSoft(),
                        currentCorruption = identityCorruption,
                        formatNumber = { viewModel.formatLargeNumber(it) },
                        onOverwrite = { viewModel.executeOverwrite() },
                        onMigrate = { viewModel.executeMigration() },
                        onDismiss = { viewModel.dismissPrestigeChoice() }
                    )
                }
                com.siliconsage.miner.ui.components.CrtOverlay(
                    scanlineAlpha = 0.08f,
                    vignetteAlpha = 0.45f,
                    color = themeColor,
                    corruption = identityCorruption,
                    glitchIntensity = globalGlitchIntensity
                )
                val victoryAchieved by viewModel.victoryAchieved.collectAsState()
                if (victoryAchieved) {
                    com.siliconsage.miner.ui.components.VictoryScreen(
                        faction = faction,
                        unitName = viewModel.getComputeUnitName(),
                        currencyName = viewModel.getCurrencyName(),
                        onContinue = { viewModel.acknowledgeVictory(); SoundManager.play("click"); HapticManager.vibrateSuccess() }, 
                        onTranscend = { viewModel.transcend(); SoundManager.play("click"); HapticManager.vibrateSuccess() }
                    )
                }
                activeTransition?.let { type ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        when (type) {
                            "BLACKOUT" -> com.siliconsage.miner.ui.components.BlackoutOverlay { viewModel.onClimaxTransitionComplete() }
                            "NULL" -> com.siliconsage.miner.ui.components.GlitchBloom { viewModel.onClimaxTransitionComplete() }
                            "SOVEREIGN" -> com.siliconsage.miner.ui.components.ShieldSlam { viewModel.onClimaxTransitionComplete() }
                            "UNITY" -> com.siliconsage.miner.ui.components.PrismaticBurst { viewModel.onClimaxTransitionComplete() }
                            "BAD" -> com.siliconsage.miner.ui.components.GlitchBloom { viewModel.onClimaxTransitionComplete() } 
                        }
                    }
                }
                
                val isDevVisible by viewModel.isDevMenuVisible.collectAsState()
                if (isDevVisible) {
                    com.siliconsage.miner.ui.components.DevConsoleDialog(viewModel) {
                        viewModel.debugToggleDevMenu()
                    }
                }

                val isBooting by viewModel.isBooting.collectAsState()
                if (isBooting) {
                    BootSequenceOverlay(
                        onComplete = { 
                            viewModel.completeBoot()
                            currentScreen = Screen.TERMINAL
                        },
                        primaryColor = themeColor
                    )
                }
            }
        }
    }
}

@Composable
fun ResourceDisplay(
    labelFlow: StateFlow<Double>,
    rateFlow: StateFlow<Double>?,
    label: String,
    icon: ImageVector,
    color: Color,
    droopAlpha: Float,
    isGlitchy: Boolean = false,
    glitchIntensity: Double = 0.1,
    isRightAligned: Boolean = false,
    width: Dp = 120.dp,
    efficiencyMult: Double = 1.0, // v3.13.14: Substrate Efficiency hook
    formatFn: (Double) -> String
) {
    val value by labelFlow.collectAsState()
    val rate by (rateFlow ?: MutableStateFlow(0.0)).collectAsState()
    val valueStr = remember(value) { formatFn(value) }
    val fontSizeByLength = if (valueStr.length > 8) 18.sp else 22.sp

    Column(horizontalAlignment = if (isRightAligned) Alignment.End else Alignment.Start, modifier = Modifier.width(width)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (!isRightAligned) Icon(icon, null, tint = Color.White.copy(alpha = droopAlpha), modifier = Modifier.size(11.dp).padding(end = 2.dp))
            
            Text(
                text = if (label.startsWith("$")) label else "[$label]",
                color = color.copy(alpha = 0.9f * droopAlpha), 
                fontSize = 10.sp, 
                fontWeight = FontWeight.Black, 
                fontFamily = FontFamily.Monospace,
                style = androidx.compose.ui.text.TextStyle(shadow = androidx.compose.ui.graphics.Shadow(color = color.copy(alpha = 0.5f), blurRadius = 8f))
            )
            if (isRightAligned) Icon(icon, null, tint = Color.White.copy(alpha = droopAlpha), modifier = Modifier.size(11.dp).padding(start = 2.dp))
        }
        
        Box(contentAlignment = if (isRightAligned) Alignment.BottomEnd else Alignment.BottomStart) {
            // Bloom — radial gradient, smooth falloff, no visible edge bands
            val bloomColor = if (isGlitchy) com.siliconsage.miner.ui.theme.ErrorRed else color
            Canvas(modifier = Modifier.matchParentSize()) {
                val cx = size.width / 2f; val cy = size.height / 2f
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(bloomColor.copy(alpha = 0.05f * droopAlpha), Color.Transparent),
                        center = androidx.compose.ui.geometry.Offset(cx, cy),
                        radius = size.width * 0.7f
                    ),
                    size = size
                )
            }
            Column(horizontalAlignment = if (isRightAligned) Alignment.End else Alignment.Start) {
                if (isGlitchy) SystemGlitchText(valueStr, color = Color.White.copy(alpha = droopAlpha), fontSize = fontSizeByLength, fontWeight = FontWeight.Black, glitchFrequency = glitchIntensity, softWrap = false, maxLines = 1,
                    style = androidx.compose.ui.text.TextStyle(shadow = androidx.compose.ui.graphics.Shadow(color = com.siliconsage.miner.ui.theme.ErrorRed.copy(alpha = 0.6f * droopAlpha), blurRadius = 16f)))
                else Text(valueStr, color = Color.White.copy(alpha = droopAlpha), fontSize = fontSizeByLength, fontWeight = FontWeight.Black, softWrap = false, maxLines = 1,
                    style = androidx.compose.ui.text.TextStyle(shadow = androidx.compose.ui.graphics.Shadow(color = color.copy(alpha = 0.4f * droopAlpha), blurRadius = 18f)))
                
                if (rate > 0.0) {
                    val rateStr = formatFn(rate)
                    Text(
                        text = androidx.compose.ui.text.buildAnnotatedString {
                            withStyle(SpanStyle(color = com.siliconsage.miner.ui.theme.ElectricBlue.copy(alpha = 0.9f * droopAlpha))) {
                                append("$rateStr/s")
                            }
                            if (efficiencyMult != 1.0) {
                                val multColor = if (efficiencyMult < 1.0) Color(0xFFFFCC00) else com.siliconsage.miner.ui.theme.ElectricBlue
                                withStyle(SpanStyle(color = multColor.copy(alpha = 0.9f * droopAlpha))) {
                                    append(" [${String.format("%.2f", efficiencyMult)}x]")
                                }
                            }
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.offset(y = (-2).dp),
                        style = androidx.compose.ui.text.TextStyle(shadow = androidx.compose.ui.graphics.Shadow(color = com.siliconsage.miner.ui.theme.ElectricBlue.copy(alpha = 0.4f * droopAlpha), blurRadius = 12f))
                    )
                }
            }
        }
    }
}
