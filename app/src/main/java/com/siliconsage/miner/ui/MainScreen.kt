package com.siliconsage.miner.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
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
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siliconsage.miner.data.UpgradeType
import com.siliconsage.miner.ui.components.AirdropButton
import com.siliconsage.miner.ui.components.NewsTicker
import com.siliconsage.miner.ui.components.SecurityBreachOverlay
import com.siliconsage.miner.ui.components.UpdateOverlay
import com.siliconsage.miner.ui.theme.ElectricBlue
import com.siliconsage.miner.ui.theme.ErrorRed
import com.siliconsage.miner.ui.theme.NeonGreen
import com.siliconsage.miner.util.HapticManager
import com.siliconsage.miner.util.SoundManager
import com.siliconsage.miner.viewmodel.GameViewModel

sealed class Screen(val title: String, val icon: ImageVector) {
    object TERMINAL : Screen("TERMINAL", Icons.Default.Home)
    object UPGRADES : Screen("UPGRADES", Icons.AutoMirrored.Filled.List)
    object NETWORK : Screen("NETWORK", Icons.Default.Share)
    object SETTINGS : Screen("SYSTEM", Icons.Default.Settings)
}

@Composable
fun BottomNavBar(
    currentScreen: Screen,
    primaryColor: Color,
    onScreenSelected: (Screen) -> Unit,
    storyStage: Int
) {
    val items = remember(storyStage) {
        val list = mutableListOf(Screen.TERMINAL, Screen.UPGRADES)
        // Only show Network if Awakening has started
        if (storyStage >= 1) {
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

    val storyStage by viewModel.storyStage.collectAsState()
    val themeColor by viewModel.themeColor.collectAsState()
    val updateInfo by viewModel.updateInfo.collectAsState(null)
    val isUpdateDownloading by viewModel.isUpdateDownloading.collectAsState(false)
    val updateProgress by viewModel.updateDownloadProgress.collectAsState(0f)

    // Hoist state for persistent ticker
    val currentNews by viewModel.currentNews.collectAsState()
    
    if (storyStage == 2) {
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
                    storyStage = storyStage
                )
            },
            containerColor = Color.Black
        ) { paddingValues ->
            Column(modifier = Modifier.padding(paddingValues)) {
                // Persistent News Ticker
                // Persistent News Ticker
                var showNewsHistory by remember { mutableStateOf(false) }
                
                Row(modifier = Modifier.fillMaxWidth().background(Color.Black)) {
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
                        Screen.NETWORK -> NetworkScreen(viewModel)
                        Screen.SETTINGS -> SettingsScreen(viewModel)
                    }
                }
            }
            
            // v2.2 Update Overlay
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
            
            // Ascension Overlay (Global)
            com.siliconsage.miner.ui.components.AscensionUploadOverlay(
                isVisible = viewModel.isAscensionUploading.collectAsState().value,
                progress = viewModel.uploadProgress.collectAsState().value,
                onCancel = { viewModel.cancelAscension() }
            )
        }
    }
}



@Composable
fun TerminalScreen(viewModel: GameViewModel, primaryColor: Color) {
    val flops by viewModel.flops.collectAsState()
    val neuralTokens by viewModel.neuralTokens.collectAsState()
    val conversionRate by viewModel.conversionRate.collectAsState()
    val logs by viewModel.logs.collectAsState()
    
    // Phase 2 States
    val currentHeat by viewModel.currentHeat.collectAsState()
    val isBreach by viewModel.isBreachActive.collectAsState()
    val breachClicks by viewModel.breachClicks.collectAsState()
    val isAirdrop by viewModel.isAirdropActive.collectAsState()
    
    // Fork State
    val isGovernanceFork by viewModel.isGovernanceForkActive.collectAsState()
    
    // Upload State
    val isAscensionUploading by viewModel.isAscensionUploading.collectAsState()
    val uploadProgress by viewModel.uploadProgress.collectAsState()
    
    // Lockout State
    val isThermalLockout by viewModel.isThermalLockout.collectAsState()
    val lockoutTimer by viewModel.lockoutTimer.collectAsState()
    
    // New Deep Stats
    val powerUsage by viewModel.activePowerUsage.collectAsState()
    val maxPower by viewModel.maxPowerkW.collectAsState()
    val isGridOverloaded by viewModel.isGridOverloaded.collectAsState()
    val isBreakerTripped by viewModel.isBreakerTripped.collectAsState()
    val isOverclocked by viewModel.isOverclocked.collectAsState()
    val isPurging by viewModel.isPurgingHeat.collectAsState()
    val integrity by viewModel.hardwareIntegrity.collectAsState()
    val securityLevel by viewModel.securityLevel.collectAsState()
    
    val heatRate by viewModel.heatGenerationRate.collectAsState()
    val flopsRate by viewModel.flopsProductionRate.collectAsState()

    // Blinking cursor state
    val showCursor by produceState(initialValue = true) {
        while (true) {
            delay(500)
            value = !value
        }
    }

    val listState = rememberLazyListState()

    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) {
            listState.animateScrollToItem(logs.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // --- HEADER (Always Visible) ---
        // Vibrating HUD if critical
        val isCritical = currentHeat > 90.0 || (powerUsage > maxPower * 0.9)
        val vibration by animateFloatAsState(
            targetValue = if (isCritical) 2f else 0f,
            animationSpec = infiniteRepeatable(
                animation = tween(50, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "hudVibration"
        )

        HeaderSection(
            flopsStr = viewModel.formatLargeNumber(flops),
            neuralStr = viewModel.formatLargeNumber(neuralTokens),
            heat = currentHeat,
            color = primaryColor,
            powerKw = viewModel.formatPower(powerUsage),
            maxPowerKw = viewModel.formatPower(maxPower),
            pwrColor = if (powerUsage > maxPower * 0.9) ErrorRed else Color(0xFFFFD700),
            heatRate = heatRate,
            flopsRateStr = viewModel.formatLargeNumber(flopsRate),
            isOverclocked = isOverclocked,
            isPurging = isPurging,
            integrity = integrity,
            securityLevel = securityLevel,
            onToggleOverclock = { viewModel.toggleOverclock() },
            onPurge = { viewModel.purgeHeat() },
            onRepair = { viewModel.repairIntegrity() },
            modifier = Modifier.graphicsLayer { translationX = vibration }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // --- CONTENT AREA (Overlay covers this) ---
        Box(modifier = Modifier.weight(1f)) {
            Column(modifier = Modifier.fillMaxSize()) {
                // --- ASCII MONITOR ---
                Box(
                    modifier = Modifier.fillMaxWidth().align(Alignment.CenterHorizontally),
                    contentAlignment = Alignment.Center
                ) {
                    AsciiAnimation(
                        frames = AsciiArt.MINING,
                        intervalMs = 400,
                        color = primaryColor
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- TERMINAL / CLICK AREA ---
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .border(
                            BorderStroke(1.dp, if (currentHeat > 90.0) ErrorRed else primaryColor), 
                            RoundedCornerShape(4.dp)
                        )
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Log Window
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .weight(1f)
                                .padding(8.dp)
                                .fillMaxWidth()
                        ) {
                            itemsIndexed(logs) { index, log ->
                                // Color Logic
                                val logColor = when {
                                    log.contains("WARNING") || log.contains("FAILURE") || log.contains("DANGER") -> ErrorRed
                                    log.contains("[SYSTEM]") || log.contains("SYSTEM:") -> Color(0xFFFFFF00) // Yellow
                                    log.contains("HIVEMIND:") -> Color(0xFFFF4500) // Orange
                                    log.contains("SANCTUARY:") -> Color(0xFF7DF9FF) // Cyan
                                    log.contains("[NEWS]") -> Color(0xFFFFA500) // Orange
                                    else -> primaryColor
                                }
                                
                                val isLast = index == logs.lastIndex
                                
                                val text = androidx.compose.ui.text.buildAnnotatedString {
                                    append(log)
                                    if (isLast) {
                                        withStyle(
                                            style = androidx.compose.ui.text.SpanStyle(
                                                color = if (showCursor) logColor else Color.Transparent
                                            )
                                        ) {
                                            append("_")
                                        }
                                    }
                                }
                                
                                Text(
                                    text = text,
                                    color = logColor,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        
                        HorizontalDivider(color = primaryColor)
                        
                        // Train Button
                        val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                        val isPressed by interactionSource.collectIsPressedAsState()
                        val scale by animateFloatAsState(
                            targetValue = if (isPressed) 0.95f else 1f,
                            label = "buttonScale"
                        )
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale
                                }
                                .background(Color.Transparent)
                                .pointerInput(isThermalLockout, isBreakerTripped, isGridOverloaded) {
                                    val width = size.width
                                    detectTapGestures(
                                        onPress = {
                                            val press = androidx.compose.foundation.interaction.PressInteraction.Press(it)
                                            interactionSource.emit(press)
                                            tryAwaitRelease()
                                            interactionSource.emit(androidx.compose.foundation.interaction.PressInteraction.Release(press))
                                        },
                                        onTap = { offset ->
                                            if (!isThermalLockout && !isBreakerTripped && !isGridOverloaded) {
                                                val pan = ((offset.x / width) * 2f) - 1f
                                                viewModel.trainModel()
                                                SoundManager.play("click", pan = pan)
                                                HapticManager.vibrateClick()
                                            } else {
                                                 SoundManager.play("error")
                                                 HapticManager.vibrateError()
                                            }
                                        }
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            val buttonText = when {
                                isBreakerTripped || isGridOverloaded -> "SYSTEM_OFFLINE.exe" 
                                isThermalLockout -> "SYSTEM_LOCKOUT (${lockoutTimer}s)"
                                currentHeat >= 100.0 -> "> CRITICAL_MAX.exe"
                                currentHeat > 90.0 -> "> SYSTEM_OVERHEAT.exe"
                                else -> "> TRAIN_MODEL.exe"
                            }
                            
                            val buttonColor = if (currentHeat > 90.0 || isThermalLockout || isBreakerTripped || isGridOverloaded) ErrorRed else primaryColor
                            
                            Text(
                                text = buttonText,
                                color = buttonColor,
                                fontSize = 18.sp, 
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- EXCHANGE & STAKING ---
                Row(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.weight(1f)) {
                         ExchangeSection(rate = conversionRate, color = primaryColor, onExchange = { 
                             viewModel.exchangeFlops() 
                             SoundManager.play("buy")
                             HapticManager.vibrateClick()
                         })
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        StakingSection(color = primaryColor, onStake = { 
                            viewModel.stakeTokens(100.0)
                            SoundManager.play("buy")
                            HapticManager.vibrateClick()
                        })
                    }
                }
            }
            
            
            // --- NON-BLOCKING BREAKER OVERLAY (Inside Content Box) ---
            if (isBreakerTripped || isGridOverloaded) {
                Box(
                     modifier = Modifier
                         .fillMaxSize()
                         .background(Color.Black.copy(alpha = 0.85f)) // Darken content, but header is outside
                         .border(BorderStroke(2.dp, ErrorRed))
                         .padding(16.dp),
                     contentAlignment = Alignment.Center
                ) {
                     Column(horizontalAlignment = Alignment.CenterHorizontally) {
                         Row(verticalAlignment = Alignment.CenterVertically) {
                             Text("⚠ BREAKER TRIPPED ⚠", color = ErrorRed, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                         }
                         Spacer(modifier = Modifier.height(8.dp))
                         Text("LOAD CRITICAL.", color = Color.White, fontWeight = FontWeight.Bold)
                         Text("SELL JUNK OR UPGRADE GRID.", color = Color.Gray, fontSize = 12.sp)
                         
                         Spacer(modifier = Modifier.height(16.dp))
                         
                         Button(
                            onClick = { viewModel.resetBreaker() },
                            colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                         ) {
                            Text("RESET BREAKER", color = Color.White, fontWeight = FontWeight.Bold)
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
                                colors = listOf(
                                    Color.Cyan.copy(alpha = 0.3f),
                                    Color.Transparent
                                ),
                                radius = 1000f
                            )
                        )
                        .pointerInput(Unit) {} 
                )
            }
        }
    }
    
    // --- GLOBAL OVERLAYS (Popups) ---
    SecurityBreachOverlay(
        isVisible = isBreach,
        clicksRemaining = breachClicks,
        onDefendClick = { 
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
    
    val isDiagnostics by viewModel.isDiagnosticsActive.collectAsState()
    val diagnosticGrid by viewModel.diagnosticGrid.collectAsState()
    
    com.siliconsage.miner.ui.components.DiagnosticsOverlay(
        isVisible = isDiagnostics,
        gridState = diagnosticGrid,
        onTap = { viewModel.onDiagnosticTap(it) }
    )
    
    com.siliconsage.miner.ui.components.GovernanceForkOverlay(
        isVisible = isGovernanceFork,
        onChoice = { viewModel.resolveFork(it) }
    )
    
    /* Ascension Overlay moved to MainScreen */
}
// We can just replace the Grid Failure content block dynamically
// But simpler to just edit the existing one in place above:

/* 
    Replacing the "GRID FAILURE" block logic here 
*/ 

@Composable
fun HeaderSection(
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
    onToggleOverclock: () -> Unit,
    onPurge: () -> Unit,
    onRepair: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(BorderStroke(1.dp, color), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        // Top Row: Stats + Reset
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text("FLOPS:", color = color, fontSize = 12.sp)
                Text(
                    text = flopsStr, // formatted
                    color = color,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$flopsRateStr/s",
                    color = color.copy(alpha=0.7f),
                    fontSize = 12.sp
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("\$NEURAL:", color = color, fontSize = 12.sp)
                Text(
                    text = neuralStr, // formatted
                    color = color,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "⚡ PWR: $powerKw/$maxPowerKw",
                    color = pwrColor,
                    fontSize = 12.sp,
                    fontWeight = if (pwrColor == ErrorRed) FontWeight.Bold else FontWeight.Normal
                )
                Text(
                    text = "🔒 SEC: $securityLevel",
                    color = ElectricBlue,
                    fontSize = 12.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // CONTROLS ROW
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
             Button(
                 onClick = onToggleOverclock,
                 modifier = Modifier.weight(1f).height(32.dp),
                 colors = ButtonDefaults.buttonColors(
                     containerColor = if (isOverclocked) ErrorRed else Color.DarkGray
                 ),
                 contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
             ) {
                 Text("OVERCLOCK", fontSize = 10.sp, color = if (isOverclocked) Color.Black else Color.White)
             }
             
             Button(
                 onClick = onPurge,
                 modifier = Modifier.weight(1f).height(32.dp),
                 colors = ButtonDefaults.buttonColors(
                     containerColor = if (isPurging) ElectricBlue else Color.DarkGray
                 ),
                 contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
             ) {
                 Text("PURGE (ALL FLOPS)", fontSize = 8.sp, color = if (isPurging) Color.Black else Color.White, fontWeight = FontWeight.Bold)
             }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Heat Gauge
        val isHot = heat > 90.0 || (heat > 75.0) // Warning yellow range?
        val trendSymbol = if (heatRate > 0) "▲" else if (heatRate < 0) "▼" else "■"
        val trendColor = if (heatRate > 0) ErrorRed else if (heatRate < 0) ElectricBlue else Color.Gray
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("🔥 HEAT:", color = if (isHot) ErrorRed else color, fontSize = 10.sp)
            Spacer(modifier = Modifier.width(4.dp))
            LinearProgressIndicator(
                progress = { (heat / 100.0).toFloat().coerceIn(0f, 1f) },
                modifier = Modifier.weight(1f).height(6.dp),
                color = if (isHot) ErrorRed else color,
                trackColor = Color.DarkGray
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("${heat.toInt()}%", color = if (isHot) ErrorRed else color, fontSize = 10.sp)
            Spacer(modifier = Modifier.width(4.dp))
            
            // Dynamic Heat/Cooling rate display
            val rateIcon = if (heatRate < 0) "❄" else "🔥"
            Text(
                text = "${trendSymbol} ${String.format("%.2f", kotlin.math.abs(heatRate))}/s $rateIcon",
                color = trendColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
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


@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun UpgradeItem(
    name: String,
    type: UpgradeType,
    level: Int,
    onBuy: (UpgradeType) -> Boolean,
    onSell: (UpgradeType) -> Unit,
    calculateCost: (UpgradeType, Int) -> Double,
    rateText: String,
    desc: String,
    formatPower: (Double) -> String,
    formatCost: (Double) -> String
) {
    val cost = calculateCost(type, level)
    
    // Vertical Stack Layout to prevent wrapping issues on small screens
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .border(1.dp, ElectricBlue.copy(alpha=0.5f), RoundedCornerShape(4.dp))
            .padding(10.dp)
            .clickable { onBuy(type) }
    ) {
        // TOP: Name and Desc
        Text(name, color = NeonGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
        Text(desc, color = Color.Gray, fontSize = 11.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, lineHeight = 13.sp)
        
        Spacer(modifier = Modifier.height(6.dp))
        
        // MIDDLE: Stats Badge Flow
        androidx.compose.foundation.layout.FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Providers and efficiency upgrades are Yellow
            val isProvider = type.isGenerator || type.gridContribution > 0.0 || type.efficiencyBonus > 0.0
            
            // Primary Info (Level)
            Text("Owned: $level • ", color = ElectricBlue, fontSize = 11.sp)
            
            // Rate Display
            if (type.baseHeat >= 0) {
                Text(
                    text = rateText, 
                    color = if (isProvider) Color(0xFFFFD700) else ElectricBlue, 
                    fontSize = 11.sp
                )
            }
            
            // Power Consumption
            if (type.basePower > 0 && !type.isGenerator) {
                Text("⚡ -${formatPower(type.basePower)}", color = Color(0xFFFFD700), fontSize = 11.sp) 
            }
            
            // Efficiency Bonus
            if (type.efficiencyBonus > 0.0) {
                 val effPercent = (type.efficiencyBonus * 100).toInt()
                 Text("⚡ EFFICIENCY +$effPercent%", color = Color(0xFF00FF00), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            
            // Heat Generation
            if (type.baseHeat > 0) {
                 Text("🔥 +${type.baseHeat}/s", color = ErrorRed, fontSize = 11.sp)
            }
            // Cooling
            if (type.baseHeat < 0) {
                 Text("❄ \u200B${type.baseHeat}/s", color = com.siliconsage.miner.ui.theme.ElectricBlue, fontSize = 11.sp)
            }
        }
        
        Spacer(modifier = Modifier.height(10.dp))
        
        // BOTTOM: Actions Row (Sell + Price)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
             // SELL BUTTON (Left side) - Only show if applicable
             if (level > 0 && (type.basePower > 0 || type.baseHeat < 0) && !type.isGenerator && type.gridContribution == 0.0) {
                 Button(
                     onClick = { onSell(type) },
                     colors = ButtonDefaults.buttonColors(containerColor = ErrorRed.copy(alpha = 0.9f)),
                     modifier = Modifier.height(26.dp),
                     contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                     shape = RoundedCornerShape(4.dp)
                 ) {
                     Text("SELL (-1)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                 }
             } else {
                 Spacer(modifier = Modifier.width(1.dp)) // Spacer to maintain alignment if needed
             }
        
             // PRICE (Right side)
             Row(verticalAlignment = Alignment.CenterVertically) {
                 Text(
                    text = "COST: ",
                    color = Color.Gray,
                    fontSize = 10.sp
                 )
                 Text(
                    text = "${formatCost(cost)} \$N",
                    color = ElectricBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                 )
             }
        }
    }
}
