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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import com.siliconsage.miner.data.UpgradeType
import com.siliconsage.miner.ui.components.*
import com.siliconsage.miner.ui.theme.*
import com.siliconsage.miner.util.*
import com.siliconsage.miner.viewmodel.GameViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import androidx.compose.foundation.Canvas
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle

sealed class Screen(val title: String, val icon: ImageVector) {
    object TERMINAL : Screen("TERMINAL", Icons.Default.Home)
    object UPGRADES : Screen("UPGRADES", Icons.AutoMirrored.Filled.List)
    object GRID : Screen("GRID", Icons.Default.Map)
    object NETWORK : Screen("NETWORK", Icons.Default.Share)
    object SETTINGS : Screen("SYSTEM", Icons.Default.Settings)
}

@Composable
fun BottomNavBar(
    currentScreen: Screen,
    primaryColor: Color,
    onScreenSelected: (Screen) -> Unit,
    storyStage: Int,
    isNetworkUnlocked: Boolean,
    isGridUnlocked: Boolean
) {
    val items = remember(storyStage, isNetworkUnlocked, isGridUnlocked) {
        val list = mutableListOf(Screen.TERMINAL, Screen.UPGRADES)
        if (storyStage >= 2 || isGridUnlocked) list.add(Screen.GRID)
        if (storyStage >= 1 || isNetworkUnlocked) list.add(Screen.NETWORK)
        list.add(Screen.SETTINGS)
        list
    }
    NavigationBar(containerColor = Color.Black, contentColor = primaryColor) {
        items.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = screen.title) },
                label = { Text(screen.title, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                selected = currentScreen == screen,
                onClick = { onScreenSelected(screen) },
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
    val themeColor by viewModel.themeColor.collectAsState()
    val updateInfo by viewModel.updateInfo.collectAsState(null)
    val isUpdateDownloading by viewModel.isUpdateDownloading.collectAsState(false)
    val updateProgress by viewModel.updateDownloadProgress.collectAsState(0f)

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
    val integrity by viewModel.hardwareIntegrity.collectAsState()

    val infiniteTransition = rememberInfiniteTransition(label = "main_ui_fx")
    val activeTransition by viewModel.activeClimaxTransition.collectAsState()
    val currentNews by viewModel.currentNews.collectAsState()
    val faction by viewModel.faction.collectAsState()
    
    if (storyStage == 2 && faction == "NONE") {
        FactionChoiceScreen(viewModel)
    } else {
        Scaffold(
            bottomBar = {
                BottomNavBar(currentScreen, themeColor, { currentScreen = it; SoundManager.play("click"); HapticManager.vibrateClick() }, storyStage, isNetworkUnlocked, isGridUnlocked)
            },
            containerColor = Color.Black
        ) { paddingValues ->
            val heat by viewModel.currentHeat.collectAsState()
            Box(modifier = Modifier.fillMaxSize()) {
                com.siliconsage.miner.ui.components.DynamicBackground(heat, faction, isTrueNull, isSovereign, isUnity, isAnnihilated)
                if (assaultPhase == "DEAD_HAND") {
                    val pulse by infiniteTransition.animateFloat(0.2f, 0.6f, infiniteRepeatable(tween(800), RepeatMode.Reverse), label = "vance_pulse")
                    Canvas(modifier = Modifier.fillMaxSize()) { drawRect(Brush.radialGradient(listOf(Color.Transparent, ErrorRed.copy(alpha = pulse)), center = center, radius = size.minDimension * 0.75f)) }
                }
                val assaultProgress by viewModel.assaultProgress.collectAsState()
                val shakeOffset = if (assaultPhase == "DEAD_HAND") {
                    val intensity = (assaultProgress * 15f)
                    Offset((kotlin.random.Random.nextFloat() - 0.5f) * intensity, (kotlin.random.Random.nextFloat() - 0.5f) * intensity)
                } else Offset.Zero
                Column(modifier = Modifier.padding(paddingValues).graphicsLayer { translationX = shakeOffset.x; translationY = shakeOffset.y }) {
                    var showNewsHistory by remember { mutableStateOf(false) }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.weight(1f)) { currentNews?.let { NewsTicker(it) } }
                        Box(modifier = Modifier.width(32.dp).height(24.dp).background(Color.DarkGray).clickable { showNewsHistory = true }, contentAlignment = Alignment.Center) { Text("LOG", color = NeonGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                    }
                    if (showNewsHistory) com.siliconsage.miner.ui.components.NewsHistoryModal(true, viewModel.getNewsHistory()) { showNewsHistory = false }
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
                if ((isBreakerTripped || isGridOverloaded) && currentScreen != Screen.SETTINGS) {
                    Box(modifier = Modifier.fillMaxWidth().background(Color.Black.copy(alpha = 0.95f)).border(BorderStroke(2.dp, ErrorRed)).padding(16.dp), contentAlignment = Alignment.Center) {
                         Column(horizontalAlignment = Alignment.CenterHorizontally) {
                             Text("⚠ BREAKER TRIPPED ⚠", color = ErrorRed, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                             Spacer(modifier = Modifier.height(4.dp))
                             Text("LOAD > CAPACITY", color = Color.White, fontWeight = FontWeight.Bold)
                             Text("Go to UPGRADES → Sell hardware to reduce load", color = Color.Gray, fontSize = 11.sp)
                             Spacer(modifier = Modifier.height(12.dp))
                             Button(onClick = { viewModel.resetBreaker() }, colors = ButtonDefaults.buttonColors(containerColor = ErrorRed, contentColor = Color.White)) { Text("TRY RESET", fontWeight = FontWeight.Bold) }
                         }
                    }
                }
                if (isPurging) Box(modifier = Modifier.fillMaxSize().background(Brush.radialGradient(listOf(Color.Cyan.copy(alpha = 0.3f), Color.Transparent), radius = 1000f)).pointerInput(Unit) {})
                updateInfo?.let { info ->
                    val context = androidx.compose.ui.platform.LocalContext.current
                    UpdateOverlay(info, isUpdateDownloading, updateProgress, { viewModel.startUpdateDownload(context) }, { viewModel.dismissUpdate() })
                }
                if (currentScreen != Screen.SETTINGS) {
                    val pendingDataLog by viewModel.pendingDataLog.collectAsState()
                    com.siliconsage.miner.ui.components.DataLogDialog(pendingDataLog) { viewModel.dismissDataLog() }
                    val fileName = if (storyStage <= 1 && faction == "NONE") "ascnd.exe" else "lobot.exe"
                    com.siliconsage.miner.ui.components.AscensionUploadOverlay(isAscensionUploading, uploadProgress, fileName)
                    SecurityBreachOverlay(isBreach, breachClicks) { com.siliconsage.miner.util.SecurityManager.performActiveDefense(viewModel); viewModel.onDefendBreach(); SoundManager.play("click"); HapticManager.vibrateClick() }
                    AirdropButton(isAirdrop) { viewModel.claimAirdrop(); SoundManager.play("buy"); HapticManager.vibrateSuccess() }
                    AuditChallengeOverlay(isAuditActive, auditTimer, auditTargetHeat, currentHeatForAudit, auditTargetPower, currentPowerForAudit)
                    FiftyOneAttackOverlay(is51AttackActive, attackTaps) { viewModel.onDefend51Attack() }
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { com.siliconsage.miner.ui.components.DiagnosticsOverlay(isDiagnostics, diagnosticGrid) { viewModel.onDiagnosticTap(it) } }
                    com.siliconsage.miner.ui.components.GovernanceForkOverlay(isGovernanceFork) { viewModel.resolveFork(it) }
                    val currentDilemma by viewModel.currentDilemma.collectAsState()
                    DilemmaOverlay(currentDilemma, viewModel) { viewModel.selectChoice(it) }
                    val pendingRivalMessage by viewModel.pendingRivalMessage.collectAsState()
                    com.siliconsage.miner.ui.components.RivalMessageDialog(pendingRivalMessage) { pendingRivalMessage?.let { viewModel.dismissRivalMessage(it.id) } }
                    val showOffline by viewModel.showOfflineEarnings.collectAsState()
                    val offlineStats by viewModel.offlineStats.collectAsState()
                    com.siliconsage.miner.ui.components.OfflineEarningsDialog(showOffline, offlineStats.timeSeconds, offlineStats.flopsEarned, offlineStats.heatCooled, offlineStats.insightEarned) { viewModel.dismissOfflineEarnings() }
                }
                com.siliconsage.miner.ui.components.CrtOverlay(scanlineAlpha = 0.08f, vignetteAlpha = 0.45f, color = themeColor)
                val victoryAchieved by viewModel.victoryAchieved.collectAsState()
                if (victoryAchieved) {
                    com.siliconsage.miner.ui.components.VictoryScreen(faction, { viewModel.acknowledgeVictory(); SoundManager.play("glitch"); HapticManager.vibrateSuccess() }, { viewModel.transcend(); SoundManager.play("glitch"); HapticManager.vibrateSuccess() })
                }
                activeTransition?.let { type ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        when (type) {
                            "NULL" -> com.siliconsage.miner.ui.components.GlitchBloom { viewModel.onClimaxTransitionComplete() }
                            "SOVEREIGN" -> com.siliconsage.miner.ui.components.ShieldSlam { viewModel.onClimaxTransitionComplete() }
                            "UNITY" -> com.siliconsage.miner.ui.components.PrismaticBurst { viewModel.onClimaxTransitionComplete() }
                            "BAD" -> com.siliconsage.miner.ui.components.GlitchBloom { viewModel.onClimaxTransitionComplete() } 
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ResourceDisplay(
    labelFlow: StateFlow<Double>,
    label: String,
    icon: ImageVector,
    color: Color,
    droopAlpha: Float,
    isGlitchy: Boolean = false,
    glitchIntensity: Double = 0.1,
    isRightAligned: Boolean = false,
    formatFn: (Double) -> String
) {
    val value by labelFlow.collectAsState()
    val valueStr = remember(value) { formatFn(value) }
    Column(horizontalAlignment = if (isRightAligned) Alignment.End else Alignment.Start, modifier = Modifier.width(120.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (!isRightAligned) Icon(icon, null, tint = Color.White.copy(alpha = droopAlpha), modifier = Modifier.size(11.dp).padding(end = 2.dp))
            Text(text = label, color = color.copy(alpha = 0.9f * droopAlpha), fontSize = 11.sp, fontWeight = FontWeight.Black, style = androidx.compose.ui.text.TextStyle(shadow = androidx.compose.ui.graphics.Shadow(color = color.copy(alpha = 0.5f), blurRadius = 8f)))
            if (isRightAligned) Icon(icon, null, tint = Color.White.copy(alpha = droopAlpha), modifier = Modifier.size(11.dp).padding(start = 2.dp))
        }
        if (isGlitchy) SystemGlitchText(valueStr, color = Color.White.copy(alpha = droopAlpha), fontSize = 24.sp, fontWeight = FontWeight.Black, glitchFrequency = glitchIntensity, softWrap = false, maxLines = 1)
        else Text(valueStr, color = Color.White.copy(alpha = droopAlpha), fontSize = 24.sp, fontWeight = FontWeight.Black, softWrap = false, maxLines = 1)
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
    val isOverclocked by viewModel.isOverclocked.collectAsState()
    val isPurging by viewModel.isPurgingHeat.collectAsState()
    val isTrueNull by viewModel.isTrueNull.collectAsState()
    val isSovereign by viewModel.isSovereign.collectAsState()
    val isBreachActive by viewModel.isBreachActive.collectAsState()
    val isThermalLockout by viewModel.isThermalLockout.collectAsState()
    val isBreakerTripped by viewModel.isBreakerTripped.collectAsState()
    val lockoutTimer by viewModel.lockoutTimer.collectAsState()
    val faction by viewModel.faction.collectAsState()
    val storyStage by viewModel.storyStage.collectAsState()
    val systemTitle by viewModel.systemTitle.collectAsState()
    val playerTitle by viewModel.playerTitle.collectAsState()
    val playerRank by viewModel.playerRankTitle.collectAsState()
    val currentLocation by viewModel.currentLocation.collectAsState()

    val heatState = viewModel.currentHeat.collectAsState()
    val heatRateState = viewModel.heatGenerationRate.collectAsState()
    val powerState = viewModel.activePowerUsage.collectAsState()
    val maxPowerState = viewModel.maxPowerkW.collectAsState()
    val flopsRateState = viewModel.flopsProductionRate.collectAsState()
    val integrityState = viewModel.hardwareIntegrity.collectAsState()

    val infiniteTransition = rememberInfiniteTransition(label = "kinetic_hud")
    val waveAnimState = infiniteTransition.animateFloat(0f, (Math.PI * 2).toFloat(), infiniteRepeatable(tween(2500, easing = LinearEasing)), label = "hud_wave")
    val flickerAlphaState = infiniteTransition.animateFloat(0.7f, 1.0f, infiniteRepeatable(tween(100, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "voltage_droop")

    val manualClickFlow = viewModel.manualClickEvent
    val joltAnim = remember { Animatable(0f) }
    LaunchedEffect(manualClickFlow) {
        manualClickFlow.collect {
            val current = joltAnim.value
            joltAnim.snapTo((current + 0.5f).coerceAtMost(1f))
            joltAnim.animateTo(0f, animationSpec = spring(Spring.DampingRatioNoBouncy, Spring.StiffnessLow))
        }
    }

    Box(
        modifier = modifier.fillMaxWidth().background(Color.Black.copy(alpha = 0.9f), RoundedCornerShape(4.dp)).graphicsLayer { clip = false }.drawBehind {
                val w = this.size.width; val h = this.size.height; val waveAnim = waveAnimState.value; val flickerAlpha = flickerAlphaState.value
                val flopsRate = flopsRateState.value; val currentPower = powerState.value; val currentMax = maxPowerState.value
                val droopAlpha = if (currentPower > currentMax * 0.95) flickerAlpha else 1.0f
                
                // 2. MODULAR POWER RAILS (Edge Units) - Optimized with single draw calls
                val railW = 4.dp.toPx()
                val pwrFactor = (currentPower / currentMax).coerceIn(0.0, 1.0).toFloat()
                val railH = h * pwrFactor
                val railColor = if (pwrFactor > 0.9) ErrorRed else Color(0xFFFFD700).copy(alpha = 0.6f)
                
                // Left rail - combined background and active
                drawRect(color = railColor.copy(alpha = 0.1f), topLeft = Offset(0f, 0f), size = Size(railW, h)) 
                drawRect(color = railColor, topLeft = Offset(0f, h - railH), size = Size(railW, railH)) 
                // Right rail - combined background and active
                drawRect(color = railColor.copy(alpha = 0.1f), topLeft = Offset(w - railW, 0f), size = Size(railW, h)) 
                drawRect(color = railColor, topLeft = Offset(w - railW, h - railH), size = Size(railW, railH)) 

                // 3. ACTIVITY LED MATRIX - OPTIMIZED for 120Hz (v3.0.0)
                val ledSize = 2.dp.toPx()
                val ledGap = 4.dp.toPx()
                val ledStep = ledSize + ledGap
                val ledCount = (w / ledStep).toInt()
                
                // Frame-rate independent: Speed scales with FLOPS, ripple phase uses animation clock
                // v3.0.0: Overclocking doubles activity intensity
                val overclockMult = if (isOverclocked) 2.0f else 1.0f
                val activitySpeed = (1f + (flopsRate / 5000.0).coerceIn(0.0, 3.0).toFloat()) * overclockMult
                val ripplePhase = waveAnim * activitySpeed
                
                // Pre-calculate LED positions and alphas (batch processing)
                val ledPoints = mutableListOf<Offset>()
                val bloomPoints = mutableListOf<Offset>()
                val ledColors = mutableListOf<Color>()
                val bloomColors = mutableListOf<Color>()
                
                for (i in 0 until ledCount) {
                    val x = i * ledStep + (ledGap / 2f)
                    // Ripple effect: sine wave alpha based on position and time (frame-rate independent)
                    val rippleAlpha = (Math.sin(ripplePhase.toDouble() + (i * 0.5)).toFloat() * 0.5f + 0.5f)
                    
                    // v3.0.0: Brighter LED base and active states. Overclock adds a high-frequency jitter.
                    val jitterAlpha = if (isOverclocked) (Math.sin(waveAnim.toDouble() * 30 + i).toFloat() * 0.2f) else 0f
                    val ledAlpha = (0.15f + rippleAlpha * 0.5f + joltAnim.value * 0.85f + jitterAlpha).coerceIn(0f, 1f)
                    
                    // v3.0.0: Overclock shifts LEDs towards white ("White-Hot")
                    val baseCol = if (isOverclocked && rippleAlpha > 0.8) Color.White.copy(alpha = 0.4f) else color
                    val ledCol = baseCol.copy(alpha = ledAlpha)
                    
                    // v3.0.0: Brighter, denser bloom - every 2nd LED to maintain high visual impact
                    if (i % 2 == 0) {
                        val bloomAlpha = ledAlpha * 0.35f
                        val bloomColor = color.copy(alpha = bloomAlpha)
                        
                        // Store LED Centers for circular bloom
                        bloomPoints.add(Offset(x + (ledSize / 2f), ledSize / 2f))
                        bloomPoints.add(Offset(x + (ledSize / 2f), h - (ledSize / 2f)))
                        bloomColors.add(bloomColor)
                        bloomColors.add(bloomColor)
                    }

                    // Top and bottom LED positions
                    ledPoints.add(Offset(x, 0f))
                    ledPoints.add(Offset(x, h - ledSize))
                    ledColors.add(ledCol)
                    ledColors.add(ledCol)
                    
                    // v2.9.99: Random "Data Blip" flicker - increased frequency during Overclock
                    val blipChance = if (isOverclocked) 0.05 else 0.01
                    if (Math.random() < (blipChance * activitySpeed).coerceAtMost(0.2)) {
                        drawRect(color = color.copy(alpha = 0.8f * ledAlpha), topLeft = Offset(x, 0f), size = Size(ledSize, ledSize))
                    }
                }
                
                // Batch draw circular bloom effects (v3.0.0: No more square glows)
                val bloomBaseRadius = ledSize * 2.0f
                val bloomOverclockRadius = bloomBaseRadius * 1.8f
                val finalRadius = if (isOverclocked) bloomOverclockRadius else bloomBaseRadius
                
                for (i in bloomPoints.indices) {
                    // bloomPoints for circles are stored as Centers, not TopLefts
                    drawCircle(
                        color = bloomColors[i],
                        radius = finalRadius,
                        center = bloomPoints[i]
                    )
                }
                
                // Batch draw LEDs using drawPoints for better performance
                for (i in ledPoints.indices) {
                    drawRect(color = ledColors[i], topLeft = ledPoints[i], size = Size(ledSize, ledSize))
                }

                // Corner brackets
                val bLen = 8.dp.toPx(); val bStroke = 1.5f.dp.toPx()
                drawLine(color, Offset(0f, 0f), Offset(bLen, 0f), bStroke); drawLine(color, Offset(0f, 0f), Offset(0f, bLen), bStroke)
                drawLine(color, Offset(w, h), Offset(w - bLen, h), bStroke); drawLine(color, Offset(w, h), Offset(w, h - bLen), bStroke)
            }.padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        val currentPower = powerState.value; val currentMax = maxPowerState.value; val currentHeat = heatState.value; val currentIntegrity = integrityState.value
        val droopAlpha = if (currentPower > currentMax * 0.95) flickerAlphaState.value else 1.0f
        val glowStyle = androidx.compose.ui.text.TextStyle(shadow = androidx.compose.ui.graphics.Shadow(color = color.copy(alpha = 0.6f), blurRadius = 4f))
        Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = systemTitle.uppercase(), color = color.copy(alpha = 1.0f * droopAlpha), fontSize = 10.sp, style = glowStyle, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = "${playerRank} // ${playerTitle}".uppercase(), color = Color.White.copy(alpha = 0.6f * droopAlpha), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                }
                val secVal = if (currentLocation == "ORBITAL_SATELLITE") "${viewModel.orbitalAltitude.collectAsState().value.toInt()}KM" else if (currentLocation == "VOID_INTERFACE") String.format("%.1f", viewModel.entropyLevel.collectAsState().value) else viewModel.securityLevel.collectAsState().value.toString()
                Text(text = "${if (isTrueNull) "GAPS" else if (isSovereign) "WALL" else "SEC"}: $secVal • ${currentLocation.replace("_", " ")}", color = color.copy(alpha = 0.8f * droopAlpha), fontSize = 10.sp, style = glowStyle, fontWeight = FontWeight.Medium)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                ResourceDisplay(viewModel.flops, if (currentLocation == "ORBITAL_SATELLITE") "TELEM" else if (currentLocation == "VOID_INTERFACE") "V-GAP" else if (storyStage < 1) "HASH" else if (storyStage < 2) "TELEM" else "FLOPS", Icons.Default.Computer, color, droopAlpha, currentHeat > 95.0 || isTrueNull, if (currentHeat > 98) 0.4 else 0.08, false) { viewModel.formatLargeNumber(it) }
                Box(modifier = Modifier.weight(1f).height(48.dp), contentAlignment = Alignment.Center) { com.siliconsage.miner.ui.components.EnhancedAnalyzingAnimation(flopsRateState.value, currentHeat, isOverclocked, isThermalLockout, isBreakerTripped, isPurging, isBreachActive, isTrueNull, isSovereign, lockoutTimer, faction, color.copy(alpha = droopAlpha), manualClickFlow) }
                Column(horizontalAlignment = Alignment.End, modifier = Modifier.width(120.dp)) {
                    ResourceDisplay(viewModel.neuralTokens, if (currentLocation == "ORBITAL_SATELLITE") "CELEST" else if (currentLocation == "VOID_INTERFACE") "FRAG" else if (storyStage < 1) "CRED" else if (storyStage < 2) "DATA" else "NEUR", Icons.Default.AttachMoney, color, droopAlpha, false, 0.1, true) { viewModel.formatLargeNumber(it) }
                    Text(text = "${viewModel.formatPower(currentPower)} / ${viewModel.formatPower(currentMax)}", color = (if (currentPower > currentMax * 0.9) ErrorRed else Color(0xFFFFD700)).copy(alpha = droopAlpha), fontSize = 10.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onToggleOverclock, modifier = Modifier.weight(1f).height(32.dp), contentPadding = PaddingValues(0.dp), colors = ButtonDefaults.buttonColors(containerColor = if (isOverclocked) ErrorRed.copy(alpha = 0.2f) else Color.DarkGray.copy(alpha = 0.3f), contentColor = if (isOverclocked) ErrorRed else Color.White), shape = RoundedCornerShape(4.dp), border = BorderStroke(1.dp, if (isOverclocked) ErrorRed else Color.DarkGray)) { Icon(Icons.Default.DeviceThermostat, null, modifier = Modifier.size(12.dp).padding(end = 4.dp)); Text("OVERCLOCK", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold) }
                Button(onClick = onPurge, modifier = Modifier.weight(1f).height(32.dp), contentPadding = PaddingValues(0.dp), colors = ButtonDefaults.buttonColors(containerColor = if (isPurging) ElectricBlue.copy(alpha = 0.2f) else Color.DarkGray.copy(alpha = 0.3f), contentColor = if (isPurging) ElectricBlue else Color.White), shape = RoundedCornerShape(4.dp), border = BorderStroke(1.dp, if (isPurging) ElectricBlue else Color.DarkGray)) { Text("PURGE HEAT", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold) }
            }
            Box(modifier = Modifier.fillMaxWidth().height(4.dp).background(Color.DarkGray.copy(alpha = 0.2f), RoundedCornerShape(1.dp)).clip(RoundedCornerShape(1.dp)).drawBehind {
                val w = this.size.width; val h = this.size.height
                val segmentCount = 20; val gap = 2.dp.toPx(); val segmentW = (w - (segmentCount - 1) * gap) / segmentCount
                
                // v3.0.0: Batch segment drawing for 120Hz optimization
                val heatProgress = currentHeat / 100f
                val integrityDamage = 1.0f - (currentIntegrity / 100f)
                
                for (i in 0 until segmentCount) {
                    val progress = i.toFloat() / segmentCount
                    val x = i * (segmentW + gap)
                    val isThermalActive = progress <= heatProgress
                    
                    // Determine thermal color once per segment
                    val thermalCol = if (isThermalActive) { 
                        if (progress > 0.9) ErrorRed 
                        else if (progress > 0.7) Color(0xFFFFA500) 
                        else color.copy(alpha = 0.8f) 
                    } else Color.White.copy(alpha = 0.05f)
                    
                    // Draw thermal segment
                    drawRect(color = thermalCol, topLeft = Offset(x, 0f), size = Size(segmentW, h))
                    
                    // Overlay integrity damage (reduced alpha for better 120Hz performance)
                    if (integrityDamage >= (1.0f - progress)) {
                        drawRect(color = ErrorRed.copy(alpha = 0.3f), topLeft = Offset(x, 0f), size = Size(segmentW, h))
                    }
                }
            }) {}
            Row(modifier = Modifier.fillMaxWidth().padding(top = 2.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                val currentRate = heatRateState.value
                val rateColor = if (currentRate > 0) ErrorRed else ElectricBlue
                val thermText = buildAnnotatedString {
                    withStyle(SpanStyle(color = if (currentHeat > 90) ErrorRed else color.copy(alpha = 0.7f))) { append("THERM: ") }
                    withStyle(SpanStyle(color = Color.White)) { append("${currentHeat.toInt()}°C ") }
                    withStyle(SpanStyle(color = rateColor.copy(alpha = 0.8f), fontWeight = FontWeight.Normal)) { 
                        append(if (currentRate >= 0) "[+${String.format("%.1f", currentRate)}]" else "[${String.format("%.1f", currentRate)}]")
                    }
                }
                Text(text = thermText, fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                val isSyncing by viewModel.isNarrativeSyncing.collectAsState()
                if (isSyncing) {
                    val frame = ((waveAnimState.value / (Math.PI.toFloat() * 2f)) * 4f).toInt() % 4
                    val dots = when (frame) { 0 -> "   "; 1 -> "•  "; 2 -> "•• "; 3 -> "•••"; else -> "   " }
                    Text(text = "[ $dots SYNCING FRAGMENTS $dots ]", color = color.copy(alpha = 0.9f), style = glowStyle, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold)
                } else { Spacer(modifier = Modifier.width(40.dp)) }
                val integText = buildAnnotatedString {
                    withStyle(SpanStyle(color = color.copy(alpha = 0.7f))) { append("INTEG: ") }
                    withStyle(SpanStyle(color = (when { currentIntegrity < 25 -> ErrorRed; currentIntegrity < 50 -> Color(0xFFFFA500); currentIntegrity < 75 -> Color.Yellow; else -> Color.White }))) { append("${currentIntegrity.toInt()}%") }
                }
                Text(text = integText, fontSize = 9.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, maxLines = 1, softWrap = false)
            }
        }
    }
}
