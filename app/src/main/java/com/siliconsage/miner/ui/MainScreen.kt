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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import com.siliconsage.miner.data.UpgradeType
import com.siliconsage.miner.ui.components.*
import com.siliconsage.miner.ui.theme.*
import com.siliconsage.miner.util.*
import com.siliconsage.miner.viewmodel.GameViewModel
import com.siliconsage.miner.viewmodel.ResonanceState
import com.siliconsage.miner.viewmodel.ResonanceTier
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
                    com.siliconsage.miner.ui.components.OfflineEarningsDialog(
                        isVisible = showOffline,
                        timeOfflineSec = offlineStats.timeSeconds,
                        floopsEarned = offlineStats.flopsEarned,
                        heatCooled = offlineStats.heatCooled,
                        insightEarned = offlineStats.insightEarned,
                        unitName = viewModel.getComputeUnitName(),
                        onDismiss = { viewModel.dismissOfflineEarnings() }
                    )
                }
                com.siliconsage.miner.ui.components.CrtOverlay(scanlineAlpha = 0.08f, vignetteAlpha = 0.45f, color = themeColor)
                val victoryAchieved by viewModel.victoryAchieved.collectAsState()
                if (victoryAchieved) {
                    com.siliconsage.miner.ui.components.VictoryScreen(
                        faction = faction,
                        unitName = viewModel.getComputeUnitName(),
                        currencyName = viewModel.getCurrencyName(),
                        onContinue = { viewModel.acknowledgeVictory(); SoundManager.play("glitch"); HapticManager.vibrateSuccess() }, 
                        onTranscend = { viewModel.transcend(); SoundManager.play("glitch"); HapticManager.vibrateSuccess() }
                    )
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
    width: Dp = 120.dp,
    formatFn: (Double) -> String
) {
    val value by labelFlow.collectAsState()
    val valueStr = remember(value) { formatFn(value) }
    val fontSizeByLength = if (valueStr.length > 8) 18.sp else 22.sp

    Column(horizontalAlignment = if (isRightAligned) Alignment.End else Alignment.Start, modifier = Modifier.width(width)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (!isRightAligned) Icon(icon, null, tint = Color.White.copy(alpha = droopAlpha), modifier = Modifier.size(11.dp).padding(end = 2.dp))
            Text(text = label, color = color.copy(alpha = 0.9f * droopAlpha), fontSize = 11.sp, fontWeight = FontWeight.Black, style = androidx.compose.ui.text.TextStyle(shadow = androidx.compose.ui.graphics.Shadow(color = color.copy(alpha = 0.5f), blurRadius = 8f)))
            if (isRightAligned) Icon(icon, null, tint = Color.White.copy(alpha = droopAlpha), modifier = Modifier.size(11.dp).padding(start = 2.dp))
        }
        if (isGlitchy) SystemGlitchText(valueStr, color = Color.White.copy(alpha = droopAlpha), fontSize = fontSizeByLength, fontWeight = FontWeight.Black, glitchFrequency = glitchIntensity, softWrap = false, maxLines = 1)
        else Text(valueStr, color = Color.White.copy(alpha = droopAlpha), fontSize = fontSizeByLength, fontWeight = FontWeight.Black, softWrap = false, maxLines = 1)
    }
}

@Composable
fun ResonanceDisplay(state: ResonanceState, color: Color) {
    val tierLabel = state.tier.name
    val progress = state.intensity
    val ratioPercent = (state.ratio * 100).toInt()
    
    val resonanceColor = when(state.tier) {
        ResonanceTier.HARMONIC -> ConvergenceGold
        ResonanceTier.SYMPHONIC -> ConvergenceGold
        ResonanceTier.TRANSCENDENT -> Color.White
        else -> color.copy(alpha = 0.5f)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .border(1.dp, resonanceColor.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
            .background(resonanceColor.copy(alpha = 0.05f), RoundedCornerShape(4.dp))
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "RESONANCE: $tierLabel",
                color = resonanceColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )
            Text(
                text = "RATIO: $ratioPercent%",
                color = resonanceColor.copy(alpha = 0.7f),
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(Color.DarkGray.copy(alpha = 0.3f), RoundedCornerShape(2.dp))
                .clip(RoundedCornerShape(2.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(if (progress.isNaN()) 0f else progress)
                    .fillMaxHeight()
                    .background(
                        Brush.horizontalGradient(
                            listOf(resonanceColor.copy(alpha = 0.5f), resonanceColor)
                        )
                    )
            )
        }
        
        if (state.isActive) {
            val bonus = when (state.tier) {
                ResonanceTier.HARMONIC -> "+25% Res | +50% Grid"
                ResonanceTier.SYMPHONIC -> "+75% Res | +150% Grid"
                ResonanceTier.TRANSCENDENT -> "+200% Res | +400% Grid"
                else -> ""
            }
            Text(
                text = bonus,
                color = resonanceColor.copy(alpha = 0.9f),
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 2.dp)
            )
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
    val isOverclocked by viewModel.isOverclocked.collectAsState()
    val isPurging by viewModel.isPurgingHeat.collectAsState()
    val isTrueNull by viewModel.isTrueNull.collectAsState()
    val isSovereign by viewModel.isSovereign.collectAsState()
    val isBreachActive by viewModel.isBreachActive.collectAsState()
    val isAuditActive by viewModel.isAuditChallengeActive.collectAsState()
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
    
    // v3.0.0: Resonance State (Exposed for LED Matrix)
    val resonanceState by viewModel.resonanceState.collectAsState()

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
                val w = this.size.width; val h = this.size.height; val flickerAlpha = flickerAlphaState.value
                // v3.0.3: Never-ending time clock to eliminate looping jumps
                val timeMillis = System.currentTimeMillis() % 10000000L
                val globalTime = timeMillis.toDouble() / 1000.0
                
                val flopsRate = flopsRateState.value; val currentPower = powerState.value; val currentMax = maxPowerState.value; val currentHeat = heatState.value; val currentIntegrity = integrityState.value
                val droopAlpha = if (currentPower > currentMax * 0.95) flickerAlpha else 1.0f
                
                val railW = 4.dp.toPx()
                val pwrFactor = (currentPower / currentMax).coerceIn(0.0, 1.0).toFloat()
                val railH = h * pwrFactor
                val railColor = if (pwrFactor > 0.9) ErrorRed else Color(0xFFFFD700).copy(alpha = 0.6f)
                drawRect(color = railColor.copy(alpha = 0.1f), topLeft = Offset(0f, 0f), size = Size(railW, h)) 
                drawRect(color = railColor, topLeft = Offset(0f, h - railH), size = Size(railW, railH)) 
                drawRect(color = railColor.copy(alpha = 0.1f), topLeft = Offset(w - railW, 0f), size = Size(railW, h)) 
                drawRect(color = railColor, topLeft = Offset(w - railW, h - railH), size = Size(railW, railH)) 

                val ledSize = 2.dp.toPx(); val ledGap = 4.dp.toPx(); val ledStep = ledSize + ledGap; val ledCount = (w / ledStep).toInt()
                val overclockMult = if (isOverclocked) 1.5f else 1.0f
                val activitySpeedBase = (0.2f + (flopsRate / 10000.0).coerceIn(0.0, 1.0).toFloat()) * overclockMult
                
                val bloomPoints = mutableListOf<Offset>(); val bloomColors = mutableListOf<Color>()
                
                for (i in 0 until ledCount) {
                    val x = i * ledStep + (ledGap / 2f); val posFactor = i.toFloat() / ledCount
                    var ledBaseCol = color
                    
                    // v3.0.0: Resonance Color Mapping
                    if (resonanceState.isActive && !isBreachActive) {
                        ledBaseCol = when (resonanceState.tier) {
                            ResonanceTier.HARMONIC -> ConvergenceGold
                            ResonanceTier.SYMPHONIC -> ConvergenceGold
                            ResonanceTier.TRANSCENDENT -> Color.White
                            else -> color
                        }
                    } else if (!isBreachActive) {
                        if (currentHeat > 90.0) ledBaseCol = ErrorRed
                        else if (currentHeat > 60.0) {
                            val dist = Math.abs(posFactor - 0.5) * 2.0
                            val threshold = (currentHeat - 60.0) / 30.0
                            if (dist < threshold) ledBaseCol = Color(0xFFFFA500)
                        }
                    }

                    // v3.0.3: Fully asynchronous rows with chaotic oscillators (Loop-free)
                    fun getRipple(row: Int): Float {
                        if (isBreachActive) return if (Math.sin(globalTime * 15.0 + i * 0.4 + row * Math.PI).toFloat() > 0) 1f else 0.1f
                        if (isAuditActive) return if (((globalTime * 12.0).toInt() + (if (i % 2 == 0) 0 else 1) + row) % 2 == 0) 0.8f else 0.2f
                        
                        // row-specific multipliers using irrational numbers
                        val rowSpeed = if (row == 0) 1.0 else 0.73205081 // sqrt(3)-1
                        val rowOffset = if (row == 0) 0.0 else 123.456
                        val t = (globalTime + rowOffset) * activitySpeedBase * rowSpeed
                        
                        // Direction flip and non-integer spacing
                        val sign = if (row == 0) 1.0 else -1.27
                        
                        val w1 = Math.sin(t + i * 0.41 * sign)
                        val w2 = Math.sin(t * 1.61803398 - i * 0.31 * sign) // Phi
                        val w3 = Math.sin(t * 2.71828182 + i * 0.57 * sign) // Euler
                        
                        val res = (w1 + w2 + w3) / 3.0
                        val threshold = 0.58 - (activitySpeedBase * 0.15)
                        return if (res > threshold) ((res - 0.4) / 0.6).toFloat().coerceIn(0f, 1f) else 0.05f
                    }

                    val rippleTop = getRipple(0)
                    val rippleBottom = getRipple(1)

                    val jitter = if (isOverclocked) (Math.sin(globalTime * 50.0 + i).toFloat() * 0.2f) else 0f
                    val alphaTop = (0.15f + rippleTop * 0.5f + joltAnim.value * 0.85f + jitter).coerceIn(0f, 1f)
                    val alphaBottom = (0.15f + rippleBottom * 0.5f + joltAnim.value * 0.85f + jitter).coerceIn(0f, 1f)
                    
                    val colTop = if (isOverclocked && rippleTop > 0.8 && !isBreachActive) Color.White.copy(alpha = 0.4f) else (if (isBreachActive) ErrorRed else ledBaseCol)
                    val colBottom = if (isOverclocked && rippleBottom > 0.8 && !isBreachActive) Color.White.copy(alpha = 0.4f) else (if (isBreachActive) ErrorRed else ledBaseCol)
                    
                    if (i % 2 == 0) {
                        bloomPoints.add(Offset(x + (ledSize / 2f), ledSize / 2f)); bloomColors.add(colTop.copy(alpha = alphaTop * 0.35f))
                        bloomPoints.add(Offset(x + (ledSize / 2f), h - (ledSize / 2f))); bloomColors.add(colBottom.copy(alpha = alphaBottom * 0.35f))
                    }
                    drawRect(color = colTop.copy(alpha = alphaTop), topLeft = Offset(x, 0f), size = Size(ledSize, ledSize))
                    drawRect(color = colBottom.copy(alpha = alphaBottom), topLeft = Offset(x, h - ledSize), size = Size(ledSize, ledSize))
                }
                
                val bloomRadius = ledSize * (if (isOverclocked) 6.0f else 3.0f)
                for (i in bloomPoints.indices) {
                    drawCircle(brush = Brush.radialGradient(0f to bloomColors[i], 1f to Color.Transparent, center = bloomPoints[i], radius = bloomRadius), radius = bloomRadius, center = bloomPoints[i])
                }

                val bLen = 8.dp.toPx(); val bStroke = 1.5f.dp.toPx()
                drawLine(color, Offset(0f, 0f), Offset(bLen, 0f), bStroke); drawLine(color, Offset(0f, 0f), Offset(0f, bLen), bStroke)
                drawLine(color, Offset(w, h), Offset(w - bLen, h), bStroke); drawLine(color, Offset(w, h), Offset(w, h - bLen), bStroke)
            }.padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        val currentPower = powerState.value; val currentMax = maxPowerState.value; val currentHeat = heatState.value; val currentIntegrity = integrityState.value
        val currentHeatRate = heatRateState.value
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
                ResourceDisplay(viewModel.flops, if (currentLocation == "ORBITAL_SATELLITE") "TELEM" else if (currentLocation == "VOID_INTERFACE") "V-GAP" else if (storyStage < 1) "HASH" else if (storyStage < 2) "TELEM" else "FLOPS", Icons.Default.Computer, color, droopAlpha, currentHeat > 95.0 || isTrueNull, if (currentHeat > 98) 0.4 else 0.08, false, 110.dp) { viewModel.formatLargeNumber(it) }
                Box(modifier = Modifier.weight(1f).height(48.dp), contentAlignment = Alignment.Center) { com.siliconsage.miner.ui.components.EnhancedAnalyzingAnimation(flopsRateState.value, currentHeat, isOverclocked, isThermalLockout, isBreakerTripped, isPurging, isBreachActive, isTrueNull, isSovereign, lockoutTimer, faction, color.copy(alpha = droopAlpha), manualClickFlow) }
                Column(horizontalAlignment = Alignment.End, modifier = Modifier.width(130.dp)) {
                    ResourceDisplay(viewModel.neuralTokens, if (currentLocation == "ORBITAL_SATELLITE") "CELEST" else if (currentLocation == "VOID_INTERFACE") "FRAG" else if (storyStage < 1) "CRED" else if (storyStage < 2) "DATA" else "NEUR", Icons.Default.AttachMoney, color, droopAlpha, false, 0.1, true, 130.dp) { viewModel.formatLargeNumber(it) }
                    Text(text = "${viewModel.formatPower(currentPower)} / ${viewModel.formatPower(currentMax)}", color = (if (currentPower > currentMax * 0.9) ErrorRed else Color(0xFFFFD700)).copy(alpha = droopAlpha), fontSize = 10.sp, fontWeight = FontWeight.Medium, maxLines = 1, softWrap = false)
                }
            }
            
            // v3.0.0: Resonance Display
            val resonanceState by viewModel.resonanceState.collectAsState()
            if (currentLocation == "ORBITAL_SATELLITE" || currentLocation == "VOID_INTERFACE") {
                ResonanceDisplay(resonanceState, color)
            }

            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onToggleOverclock, modifier = Modifier.weight(1f).height(32.dp), contentPadding = PaddingValues(0.dp), colors = ButtonDefaults.buttonColors(containerColor = if (isOverclocked) ErrorRed.copy(alpha = 0.2f) else Color.DarkGray.copy(alpha = 0.3f), contentColor = if (isOverclocked) ErrorRed else Color.White), shape = RoundedCornerShape(4.dp), border = BorderStroke(1.dp, if (isOverclocked) ErrorRed else Color.DarkGray)) { Icon(Icons.Default.DeviceThermostat, null, modifier = Modifier.size(12.dp).padding(end = 4.dp)); Text("OVERCLOCK", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold) }
                Button(onClick = onPurge, modifier = Modifier.weight(1f).height(32.dp), contentPadding = PaddingValues(0.dp), colors = ButtonDefaults.buttonColors(containerColor = if (isPurging) ElectricBlue.copy(alpha = 0.2f) else Color.DarkGray.copy(alpha = 0.3f), contentColor = if (isPurging) ElectricBlue else Color.White), shape = RoundedCornerShape(4.dp), border = BorderStroke(1.dp, if (isPurging) ElectricBlue else Color.DarkGray)) { Text("PURGE HEAT", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold) }
            }
            Box(modifier = Modifier.fillMaxWidth().height(4.dp).background(Color.DarkGray.copy(alpha = 0.2f), RoundedCornerShape(1.dp)).clip(RoundedCornerShape(1.dp)).drawBehind {
                val w = this.size.width; val h = this.size.height
                val barW = w * (currentHeat / 100f).toFloat().coerceIn(0f, 1f)
                val thermalBrush = Brush.horizontalGradient(0.0f to color.copy(alpha = 0.6f), 0.7f to color.copy(alpha = 0.6f), 0.9f to ErrorRed, 1.0f to ErrorRed, startX = 0f, endX = w)
                drawRect(brush = thermalBrush, size = Size(barW, h), alpha = 0.2f, style = Stroke(width = 4.dp.toPx()))
                drawRect(brush = thermalBrush, size = Size(barW, h))
                val segmentCount = 20; val segmentSpacing = w / segmentCount
                for (i in 1 until segmentCount) { val x = i * segmentSpacing; drawLine(Color.Black.copy(alpha = 0.4f), Offset(x, 0f), Offset(x, h), 1.dp.toPx()) }
                val integW = w * ((100f - currentIntegrity) / 100f).toFloat().coerceIn(0f, 1f); drawRect(ErrorRed.copy(alpha = 0.4f), Offset(w - integW, 0f), Size(integW, h))
            }) {}
            Row(modifier = Modifier.fillMaxWidth().padding(top = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                val thermText = buildAnnotatedString {
                    withStyle(SpanStyle(color = if (currentHeat > 90) ErrorRed else color.copy(alpha = 0.7f))) { append("THERM: ") }
                    withStyle(SpanStyle(color = Color.White)) { append("${currentHeat.toInt()}°C ") }
                    withStyle(SpanStyle(color = (if (currentHeatRate > 0) ErrorRed else ElectricBlue).copy(alpha = 0.8f), fontWeight = FontWeight.Normal)) { 
                        append(if (currentHeatRate >= 0) "[+${String.format("%.1f", currentHeatRate)}]" else "[${String.format("%.1f", currentHeatRate)}]")
                    }
                }
                Box(modifier = Modifier.weight(1f)) {
                    Text(text = thermText, fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                }

                val isSyncing by viewModel.isNarrativeSyncing.collectAsState()
                if (isSyncing) {
                    // v3.0.3: Time-based loop-free dots
                    val frame = ((System.currentTimeMillis() / 400) % 4).toInt()
                    val dots = ".".repeat(frame)
                    Text(
                        text = "[ ${dots.padStart(3)} SYNCING FRAGMENTS ${dots.padEnd(3)} ]", 
                        color = color.copy(alpha = 0.9f), 
                        style = glowStyle, 
                        fontSize = 8.sp, 
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                val integText = buildAnnotatedString {
                    withStyle(SpanStyle(color = color.copy(alpha = 0.7f))) { append("INTEG: ") }
                    withStyle(SpanStyle(color = (when { currentIntegrity < 25 -> ErrorRed; currentIntegrity < 50 -> Color(0xFFFFA500); currentIntegrity < 75 -> Color.Yellow; else -> Color.White }))) { append("${currentIntegrity.toInt()}%") }
                }
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                    Text(text = integText, fontSize = 9.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, maxLines = 1, softWrap = false)
                }
            }
        }
    }
}
