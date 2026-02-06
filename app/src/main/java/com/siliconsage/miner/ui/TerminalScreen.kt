package com.siliconsage.miner.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import com.siliconsage.miner.ui.components.SystemGlitchText
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import com.siliconsage.miner.data.LogEntry
import com.siliconsage.miner.ui.components.ExchangeSection
import com.siliconsage.miner.ui.components.StakingSection
import com.siliconsage.miner.ui.components.RepairSection
import com.siliconsage.miner.ui.theme.ElectricBlue
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import com.siliconsage.miner.ui.theme.ErrorRed
import com.siliconsage.miner.util.HapticManager
import com.siliconsage.miner.util.SoundManager
import com.siliconsage.miner.viewmodel.GameViewModel
import kotlinx.coroutines.delay

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
    val isGovernanceFork by viewModel.isGovernanceForkActive.collectAsState()
    val isAscensionUploading by viewModel.isAscensionUploading.collectAsState()
    val uploadProgress by viewModel.uploadProgress.collectAsState()
    val isThermalLockout by viewModel.isThermalLockout.collectAsState()
    val lockoutTimer by viewModel.lockoutTimer.collectAsState()
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
    val playerTitle by viewModel.playerTitle.collectAsState()
    val playerRank by viewModel.playerRankTitle.collectAsState()
    val currentStage by viewModel.storyStage.collectAsState()
    val systemTitle by viewModel.systemTitle.collectAsState()
    val hallucinationText by viewModel.hallucinationText.collectAsState()
    val nullActive by viewModel.nullActive.collectAsState()
    val isTrueNull by viewModel.isTrueNull.collectAsState()
    val isSovereign by viewModel.isSovereign.collectAsState()

    val showCursor by androidx.compose.runtime.produceState(initialValue = true) {
        while (true) {
            delay(500)
            value = !value
        }
    }

    val listState = rememberLazyListState()

    // v2.9.78: Fix scroll lock when log is full (trigger on list change, not just size)
    // Use instant scrollToItem to prevent animation overlap glitches during rapid clicking
    LaunchedEffect(logs) {
        if (logs.isNotEmpty()) {
            listState.scrollToItem(logs.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(16.dp)
    ) {
        val isCritical = currentHeat > 90.0 || (powerUsage > maxPower * 0.9)
        val vibrationState = animateFloatAsState(
            targetValue = if (isCritical) 2f else 0f,
            animationSpec = infiniteRepeatable(
                animation = tween(50, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "hudVibration"
        )

        HeaderSection(
            viewModel = viewModel,
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
            systemTitle = systemTitle,
            playerTitle = playerTitle,
            playerRank = playerRank,
            isThermalLockout = isThermalLockout,
            isBreakerTripped = isBreakerTripped,
            lockoutTimer = lockoutTimer,
            faction = viewModel.faction.collectAsState().value,
            onToggleOverclock = { viewModel.toggleOverclock() },
            onPurge = { viewModel.purgeHeat() },
            onRepair = { viewModel.repairIntegrity() },
            modifier = Modifier.graphicsLayer { translationX = vibrationState.value },
            hallucinationText = hallucinationText,
            isGhostActive = nullActive,
            isTrueNull = isTrueNull,
            isSovereign = isSovereign,
            isBreachActive = isBreach
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            // Terminal window with scrollable logs and controls
            Box(
                modifier = Modifier
                    .weight(1f) // Fill remaining space in screen
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(4.dp))
                    .border(
                        BorderStroke(1.dp, if (currentHeat > 90.0) ErrorRed else primaryColor), 
                        RoundedCornerShape(4.dp)
                    )
            ) {
                // v2.8.0: Subtle background code drift
                // v2.9.77: Optimized with Canvas for massive performance gain
                val infiniteTransition = rememberInfiniteTransition(label = "codeDrift")
                val alphaState = infiniteTransition.animateFloat(
                    initialValue = 0.02f,
                    targetValue = 0.05f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(4000, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "alpha"
                )

                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize().padding(4.dp)) {
                    val rows = (size.height / 30f).toInt().coerceAtLeast(1)
                    val driftColor = primaryColor.copy(alpha = alphaState.value)
                    
                    // Static binary string for drawing
                    val binary = "01101001 01110011 00100000 01100001 01101100 01101001 01110110 01100101 "
                    
                    drawIntoCanvas { canvas ->
                        val paint = android.graphics.Paint().apply {
                            color = driftColor.toArgb()
                            textSize = 24f
                            typeface = android.graphics.Typeface.MONOSPACE
                            alpha = (alphaState.value * 255).toInt()
                        }
                        for (i in 0 until rows) {
                            canvas.nativeCanvas.drawText(
                                binary,
                                0f,
                                i * 30f,
                                paint
                            )
                        }
                    }
                }

                Column(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .weight(1f) // Fill remaining space in Box
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        itemsIndexed(
                            items = logs,
                            key = { _, entry -> entry.id } // v2.9.77: Truly stable keys via LogEntry ID
                        ) { index, entry ->
                            TerminalLogLine(
                                log = entry.message,
                                isLast = index == logs.lastIndex,
                                primaryColor = primaryColor,
                                showCursor = showCursor
                            )
                        }
                    }
                    
                    HorizontalDivider(color = primaryColor, modifier = Modifier.padding(horizontal = 4.dp))
                    
                    // Train Model Button Area (Inside Terminal)
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
                            isTrueNull -> "> DEREFERENCE_REALITY.exe"
                            isSovereign -> "> ENFORCE_WILL.exe"
                            else -> {
                                when {
                                    currentStage >= 3 -> "> TRANSCEND_MATTER.exe"
                                    viewModel.faction.value == "HIVEMIND" -> "> ASSIMILATE_NODES.exe"
                                    viewModel.faction.value == "SANCTUARY" -> "> ENCRYPT_KERNEL.exe"
                                    currentStage >= 1 -> "> VALIDATE_NODE.exe"
                                    else -> "> COMPUTE_HASH.exe"
                                }
                            }
                        }
                        
                        val isCritical = currentHeat > 90.0 || isThermalLockout || isBreakerTripped || isGridOverloaded || isTrueNull
                        val buttonColor = when {
                            isCritical -> ErrorRed
                            isSovereign -> com.siliconsage.miner.ui.theme.SanctuaryPurple
                            else -> primaryColor
                        }
                        
                        if (isCritical) {
                            SystemGlitchText(
                                text = buttonText,
                                color = buttonColor,
                                fontSize = 18.sp, 
                                fontWeight = FontWeight.Bold,
                                glitchFrequency = if (isTrueNull) 0.15 else 0.35 
                            )
                        } else if (isSovereign) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("[", color = buttonColor.copy(alpha=0.5f), fontSize = 20.sp)
                                Text(
                                    text = buttonText,
                                    color = buttonColor,
                                    fontSize = 18.sp, 
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.sp
                                )
                                Text("]", color = buttonColor.copy(alpha=0.5f), fontSize = 20.sp)
                            }
                        } else {
                            Text(
                                text = buttonText,
                                color = buttonColor,
                                fontSize = 18.sp, 
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            // Removed Spacer/Divider that was outside
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.weight(1f)) {
                 ExchangeSection(rate = conversionRate, color = primaryColor, storyStage = currentStage, onExchange = { 
                     viewModel.exchangeFlops() 
                     SoundManager.play("buy")
                     HapticManager.vibrateClick()
                 })
            }
            Spacer(modifier = Modifier.width(8.dp))
            Box(modifier = Modifier.weight(1f)) {
                RepairSection(
                    integrity = integrity,
                    cost = viewModel.calculateRepairCost(),
                    color = primaryColor,
                    storyStage = currentStage,
                    onRepair = {
                        viewModel.repairIntegrity()
                        HapticManager.vibrateClick()
                    }
                )
            }
        }
    }
}

@Composable
fun TerminalLogLine(
    log: String,
    isLast: Boolean,
    primaryColor: Color,
    showCursor: Boolean
) {
    val isNullLog = remember(log) { log.startsWith("[NULL]") }
    val isSovLog = remember(log) { log.startsWith("[SOVEREIGN]") }

    if (isNullLog) {
        SystemGlitchText(
            text = log,
            color = Color.White,
            fontSize = 12.sp,
            style = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace),
            glitchFrequency = 0.2,
            modifier = Modifier.padding(vertical = 2.dp)
        )
    } else {
        // v2.9.76: Optimized AnnotatedString building with remember
        val annotatedLog = remember(log, primaryColor, isLast, showCursor) {
            androidx.compose.ui.text.buildAnnotatedString {
                val prefixes = listOf(
                    "root@sys:~/mining# ", "HIVEMIND: ", "SANCTUARY: ", "[SOVEREIGN]",
                    "[SYSTEM]: ", "SYSTEM: ", "[NEWS]: ", "[DATA]: ", "Purchased ",
                    "SOLD ", "Staked: ", "Sold ", "[VATTIC]:", "[GTC]:", "[UNIT 734]:",
                    "[VANCE]:", "[LORE]:", "[!!!!]:"
                )

                var foundPrefix: String? = null
                for (p in prefixes) {
                    if (log.startsWith(p)) {
                        foundPrefix = p
                        break
                    }
                }

                val tagColor = when {
                    log.startsWith("[!!!!]") || log.contains("WARNING") || log.contains("FAILURE") || log.contains("DANGER") -> ErrorRed
                    log.startsWith("root@sys:~/mining#") -> primaryColor
                    log.startsWith("HIVEMIND:") -> com.siliconsage.miner.ui.theme.HivemindRed
                    log.startsWith("SANCTUARY:") || log.startsWith("[SOVEREIGN]") -> com.siliconsage.miner.ui.theme.SanctuaryPurple
                    log.startsWith("[SYSTEM]") || log.startsWith("SYSTEM:") || log.startsWith("[VATTIC]:") -> Color(0xFFFFFF00)
                    log.startsWith("[NEWS]") || log.startsWith("[LORE]:") -> Color(0xFFFFA500)
                    log.startsWith("[DATA]") || log.startsWith("[UNIT 734]:") -> ElectricBlue
                    log.startsWith("[GTC]:") || log.startsWith("[VANCE]:") -> ErrorRed
                    else -> primaryColor
                }

                if (foundPrefix != null) {
                    withStyle(style = androidx.compose.ui.text.SpanStyle(color = tagColor, fontWeight = FontWeight.Bold)) {
                        append(foundPrefix)
                    }
                    withStyle(style = androidx.compose.ui.text.SpanStyle(color = Color.White)) {
                        append(log.substring(foundPrefix.length))
                    }
                } else {
                    val fullLineColor = if (log.contains("WARNING") || log.contains("FAILURE") || log.contains("DANGER")) ErrorRed else Color.White
                    withStyle(style = androidx.compose.ui.text.SpanStyle(color = fullLineColor)) {
                        append(log)
                    }
                }

                if (isLast) {
                    withStyle(
                        style = androidx.compose.ui.text.SpanStyle(
                            color = if (showCursor) Color.White else Color.Transparent
                        )
                    ) {
                        append("_")
                    }
                }
            }
        }

        Text(
            text = annotatedLog,
            style = MaterialTheme.typography.bodyLarge,
            fontSize = 12.sp,
            modifier = Modifier.padding(vertical = 1.dp)
        )
    }
}
