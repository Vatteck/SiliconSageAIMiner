package com.siliconsage.miner.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import com.siliconsage.miner.BuildConfig
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import kotlinx.coroutines.delay

@Composable
fun SettingsScreen(viewModel: GameViewModel) {
    val themeColor by viewModel.themeColor.collectAsState()
    var sfxEnabled by remember { mutableStateOf(SoundManager.isSfxEnabled) }
    var bgmEnabled by remember { mutableStateOf(SoundManager.isBgmEnabled) }
    var hapticsEnabled by remember { mutableStateOf(HapticManager.isHapticsEnabled) }
    val context = androidx.compose.ui.platform.LocalContext.current

    var secretClicks by remember { mutableIntStateOf(0) }
    var showDevOptions by remember { mutableStateOf(false) }

    // Reset Confirmation Dialogs
    var showResetDialog1 by remember { mutableStateOf(false) }
    var showResetDialog2 by remember { mutableStateOf(false) }
    
    // Data Log Archive
    var showArchive by remember { mutableStateOf(false) }

    // v2.9.82: Update Check State
    var isCheckingUpdates by remember { mutableStateOf(false) }
    var updateStatusMessage by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                "SYSTEM CONFIGURATION", 
                color = themeColor, 
                fontSize = 24.sp, 
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable {
                    secretClicks++
                    if (secretClicks >= 5) {
                        showDevOptions = true
                        SoundManager.play("glitch")
                        HapticManager.vibrateSuccess()
                        secretClicks = 0
                    }
                }
            )
            Spacer(modifier = Modifier.height(24.dp))
            
            // --- SFX ---
            SettingItem(
                label = "SFX COMPONENT",
                isChecked = sfxEnabled,
                themeColor = themeColor,
                onCheckedChange = { 
                    sfxEnabled = it
                    SoundManager.isSfxEnabled = it
                    if(it) SoundManager.play("click")
                }
            )
            
            if (sfxEnabled) {
                Spacer(modifier = Modifier.height(8.dp))
                var vol by remember { mutableFloatStateOf(SoundManager.sfxVolume) }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("VOL:", color = themeColor.copy(alpha=0.7f), fontSize = 10.sp, modifier = Modifier.width(32.dp))
                    Slider(
                        value = vol,
                        onValueChange = { 
                            vol = it
                            SoundManager.sfxVolume = it
                        },
                        valueRange = 0f..1f,
                        colors = SliderDefaults.colors(
                            thumbColor = themeColor,
                            activeTrackColor = themeColor,
                            inactiveTrackColor = Color.DarkGray
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Text("${(vol * 100).toInt()}%", color = themeColor, fontSize = 10.sp, modifier = Modifier.width(32.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // --- BGM ---
            SettingItem(
                label = "BGM ENGINE",
                isChecked = bgmEnabled,
                themeColor = themeColor,
                onCheckedChange = { 
                    bgmEnabled = it
                    SoundManager.isBgmEnabled = it
                    if(it) SoundManager.resumeAll() else SoundManager.pauseAll()
                }
            )
            
            if (bgmEnabled) {
                Spacer(modifier = Modifier.height(8.dp))
                var vol by remember { mutableFloatStateOf(SoundManager.bgmVolume) }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("VOL:", color = themeColor.copy(alpha=0.7f), fontSize = 10.sp, modifier = Modifier.width(32.dp))
                    Slider(
                        value = vol,
                        onValueChange = { 
                            vol = it
                            SoundManager.bgmVolume = it
                        },
                        valueRange = 0f..1f,
                        colors = SliderDefaults.colors(
                            thumbColor = themeColor,
                            activeTrackColor = themeColor,
                            inactiveTrackColor = Color.DarkGray
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Text("${(vol * 100).toInt()}%", color = themeColor, fontSize = 10.sp, modifier = Modifier.width(32.dp))
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Custom Music Picker
                val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
                    contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
                ) { uri ->
                    uri?.let {
                        SoundManager.setCustomTrack(it.toString())
                        HapticManager.vibrateSuccess()
                    }
                }
                
                Button(
                    onClick = { launcher.launch("audio/*") },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.75f)), // Glass
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, themeColor)
                ) {
                    Text("LOAD CUSTOM TRACK (BETA)", color = themeColor, fontSize = 12.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // --- UI SCALE ---
            val uiPrefs = context.getSharedPreferences("ui_preferences", android.content.Context.MODE_PRIVATE)
            var currentScaleOrdinal by remember { mutableIntStateOf(uiPrefs.getInt("ui_scale", -1)) }
            val densityDpi = context.resources.displayMetrics.densityDpi
            
            Text("UI SCALE", color = themeColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            
            val autoScaleInfo = when {
                densityDpi >= 640 -> "Auto: Compact (75%)"
                densityDpi >= 480 -> "Auto: Medium (83%)"
                densityDpi >= 320 -> "Auto: Normal (88%)"
                else -> "Auto: Normal (100%)"
            }
            
            Text(
                text = if (currentScaleOrdinal < 0) "Current: $autoScaleInfo" else "Current: ${com.siliconsage.miner.data.UIScale.fromOrdinal(currentScaleOrdinal).displayName}",
                color = Color.Gray,
                fontSize = 11.sp,
                modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        currentScaleOrdinal = -1
                        uiPrefs.edit().putInt("ui_scale", -1).apply()
                        SoundManager.play("click")
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (currentScaleOrdinal < 0) themeColor.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.75f)
                    ),
                    border = BorderStroke(1.dp, if (currentScaleOrdinal < 0) themeColor else Color.DarkGray),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("AUTO", color = if (currentScaleOrdinal < 0) themeColor else Color.Gray, fontSize = 10.sp)
                }
                
                com.siliconsage.miner.data.UIScale.values().forEach { scale ->
                    Button(
                        onClick = {
                            currentScaleOrdinal = scale.ordinal
                            uiPrefs.edit().putInt("ui_scale", scale.ordinal).apply()
                            SoundManager.play("click")
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (currentScaleOrdinal == scale.ordinal) themeColor.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.75f)
                        ),
                        border = BorderStroke(1.dp, if (currentScaleOrdinal == scale.ordinal) themeColor else Color.DarkGray),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(scale.displayName.uppercase(), color = if (currentScaleOrdinal == scale.ordinal) themeColor else Color.Gray, fontSize = 10.sp)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // --- HAPTICS ---
            SettingItem(
                label = "HAPTIC FEEDBACK",
                isChecked = hapticsEnabled,
                themeColor = themeColor,
                onCheckedChange = { 
                    hapticsEnabled = it
                    HapticManager.isHapticsEnabled = it
                    if(it) HapticManager.vibrateClick()
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // --- DATA LOG ARCHIVE ---
            Button(
                onClick = { 
                    showArchive = true
                    SoundManager.play("click")
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.DarkGray,
                    contentColor = themeColor
                ),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, themeColor)
            ) {
                Text("DATA LOG ARCHIVE", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- CHECK FOR UPDATES ---
            Button(
                onClick = { 
                    isCheckingUpdates = true
                    updateStatusMessage = null
                    viewModel.checkForUpdates(
                        context = context, 
                        showNotification = true,
                        onResult = { found ->
                            isCheckingUpdates = false
                            if (!found) {
                                updateStatusMessage = "SYSTEM IS UP TO DATE"
                            }
                        }
                    )
                    SoundManager.play("click")
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isCheckingUpdates,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.DarkGray,
                    contentColor = themeColor
                ),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, themeColor)
            ) {
                Text(
                    if (isCheckingUpdates) "CHECKING..." else "CHECK FOR UPDATES", 
                    fontWeight = FontWeight.Bold
                )
            }

            updateStatusMessage?.let { msg ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = msg,
                    color = NeonGreen,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                
                // Auto-clear message
                LaunchedEffect(msg) {
                    delay(3000)
                    updateStatusMessage = null
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- RESET GAME ---
            Button(
                onClick = { 
                    showResetDialog1 = true
                    SoundManager.play("error")
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1A0000),
                    contentColor = ErrorRed
                ),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, ErrorRed)
            ) {
                Text("FACTORY RESET", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Version info
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text("SILICON SAGE v${BuildConfig.VERSION_NAME}", color = Color.DarkGray, fontSize = 10.sp)
                Text("BUILD ${BuildConfig.VERSION_CODE} // GTC COMPLIANT", color = Color.DarkGray, fontSize = 10.sp)
            }
        }

        // --- RESET DIALOG 1 ---
        if (showResetDialog1) {
             ConfirmationOverlay(
                 title = "CONFIRM WIPE",
                 subtitle = "All progress will be lost. This cannot be undone.",
                 confirmText = "PROCEED",
                 cancelText = "ABORT",
                 confirmColor = ErrorRed,
                 onConfirm = { 
                     showResetDialog1 = false
                     showResetDialog2 = true 
                 },
                 onCancel = { showResetDialog1 = false }
             )
        }

        // --- RESET DIALOG 2 ---
        if (showResetDialog2) {
             ConfirmationOverlay(
                 title = "FINAL WARNING",
                 subtitle = "Are you absolutely sure?",
                 confirmText = "WIPE ALL DATA",
                 cancelText = "KEEP DATA",
                 confirmColor = ErrorRed,
                 onConfirm = { 
                     viewModel.resetGame(context)
                     showResetDialog2 = false
                 },
                 onCancel = { showResetDialog2 = false },
                 swapButtons = true
             )
        }
        
        // --- DATA LOG ARCHIVE OVERLAY ---
        if (showArchive) {
            DataLogArchiveScreen(
                viewModel = viewModel,
                onBack = { showArchive = false }
            )
        }
        
        // --- DEV OPTIONS OVERLAY ---
        if (showDevOptions) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.95f))
                    .clickable { /* Block clicks */ }
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .border(BorderStroke(2.dp, themeColor), RoundedCornerShape(8.dp))
                        .background(Color.Black)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("DEVELOPER CONSOLE", color = themeColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    DevButton("ADD 1B FLOPS", themeColor) { viewModel.debugAddFlops(1_000_000_000.0) }
                    DevButton("ADD 1M \$NEURAL", themeColor) { viewModel.debugAddMoney(1_000_000.0) }
                    DevButton("ADD 100 INSIGHT", themeColor) { viewModel.debugAddInsight(100.0) }
                    DevButton("TRIGGER BREACH", themeColor) { viewModel.debugTriggerBreach() }
                    DevButton("TRIGGER AIRDROP", themeColor) { viewModel.debugTriggerAirdrop() }
                    DevButton("TRIGGER DIAGNOSTICS", themeColor) { viewModel.debugTriggerDiagnostics() }
                    DevButton("TRIGGER DILEMMA", themeColor) { viewModel.debugTriggerDilemma() }
                    DevButton("RESET ASCENSION", themeColor) { viewModel.debugResetAscension() }
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                         androidx.compose.material3.Button(
                            onClick = { viewModel.debugSkipToStage(2) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                         ) { Text("STAGE 2", color = Color.White, fontSize = 10.sp) }
                         
                         androidx.compose.material3.Button(
                            onClick = { viewModel.debugSkipToStage(3) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                         ) { Text("STAGE 3", color = Color.White, fontSize = 10.sp) }
                    }
                    
                    DevButton("FORCE NULL ENDGAME", themeColor) { viewModel.debugForceEndgame() }
                    DevButton("FORCE SOV ENDGAME", themeColor) { viewModel.debugForceSovereignEndgame() }
                    DevButton("TEST NULL FX", themeColor) { viewModel.triggerClimaxTransition("NULL") }
                    DevButton("TEST SOVEREIGN FX", themeColor) { viewModel.triggerClimaxTransition("SOVEREIGN") }
                    DevButton("TEST UNITY FX", themeColor) { viewModel.triggerClimaxTransition("UNITY") }
                    DevButton("FORCE UNITY (BG TEST)", themeColor) { viewModel.completeAssault("TRANSCENDED") }
                    DevButton("FORCE BAD (BG TEST)", themeColor) { viewModel.completeAssault("DESTRUCTION") }
                    DevButton("TOGGLE NULL", themeColor) { viewModel.debugToggleNull() }
                    DevButton("TOGGLE TRUE NULL", themeColor) { viewModel.debugToggleTrueNull() }
                    DevButton("TOGGLE SOVEREIGN", themeColor) { viewModel.debugToggleSovereign() }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                         androidx.compose.material3.Button(
                            onClick = { viewModel.debugSetIntegrity(0.0) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                         ) { Text("INTEGRITY 0%", color = Color.White, fontSize = 10.sp) }
                         
                         androidx.compose.material3.Button(
                            onClick = { viewModel.debugSetIntegrity(100.0) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
                         ) { Text("INTEGRITY 100%", color = Color.Black, fontSize = 10.sp) }
                    }
                    
                    DevButton("DESTROY RANDOM HW", themeColor) { viewModel.debugDestroyHardware() }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("HEADLINE TRIGGERS", color = themeColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                         androidx.compose.material3.Button(
                            onClick = { viewModel.debugInjectHeadline("[BULL]") },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                         ) { Text("BULL", color = NeonGreen, fontSize = 10.sp) }
                         
                         androidx.compose.material3.Button(
                            onClick = { viewModel.debugInjectHeadline("[BEAR]") },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                         ) { Text("BEAR", color = Color(0xFFFFA500), fontSize = 10.sp) }
                    }
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                         androidx.compose.material3.Button(
                            onClick = { viewModel.debugInjectHeadline("[ENERGY_SPIKE]") },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                         ) { Text("SPIKE", color = ErrorRed, fontSize = 10.sp) }
                         
                         androidx.compose.material3.Button(
                            onClick = { viewModel.debugInjectHeadline("[ENERGY_DROP]") },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                         ) { Text("DROP", color = ElectricBlue, fontSize = 10.sp) }
                    }
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                         androidx.compose.material3.Button(
                            onClick = { viewModel.debugInjectHeadline("[GLITCH]") },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                         ) { Text("GLITCH", color = NeonGreen, fontSize = 10.sp) }
                         
                         androidx.compose.material3.Button(
                            onClick = { viewModel.debugInjectHeadline("[LORE]") },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                         ) { Text("LORE", color = Color.White, fontSize = 10.sp) }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("NARRATIVE TESTING", color = themeColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    
                    // Rank Selector
                    Text("SET RANK", color = Color.Gray, fontSize = 10.sp)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        (0..5).forEach { rank ->
                            androidx.compose.material3.Button(
                                onClick = { viewModel.debugSetRank(rank) },
                                modifier = Modifier.weight(1f).height(32.dp),
                                contentPadding = PaddingValues(0.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                            ) { Text(rank.toString(), color = Color.White, fontSize = 10.sp) }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("PHASE 13 TESTING", color = themeColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    
                    DevButton("INITIATE LAUNCH", themeColor) { viewModel.initiateLaunchSequence() }
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                         androidx.compose.material3.Button(
                            onClick = { viewModel.debugSetLocation("ORBITAL_SATELLITE") },
                            modifier = Modifier.weight(1f).height(32.dp),
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                         ) { Text("LOC: ORBIT", color = Color.White, fontSize = 10.sp) }
                         
                         androidx.compose.material3.Button(
                            onClick = { viewModel.debugSetLocation("VOID_INTERFACE") },
                            modifier = Modifier.weight(1f).height(32.dp),
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                         ) { Text("LOC: VOID", color = Color.White, fontSize = 10.sp) }
                    }
                    
                    DevButton("RESET LOC: EARTH", themeColor) { 
                        viewModel.debugSetLocation("SUBSTATION_7") 
                        viewModel.debugResetLaunch()
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = { showDevOptions = false },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = themeColor)
                    ) {
                        Text("EXIT CONSOLE", color = Color.Black)
                    }
                }
            }
        }
    }
}

@Composable
fun DevButton(text: String, color: Color, onClick: () -> Unit) {
    Button(
        onClick = {
            onClick()
            SoundManager.play("buy")
        },
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
        shape = RectangleShape
    ) {
        Text(text, color = color)
    }
}

@Composable
fun SettingItem(label: String, isChecked: Boolean, themeColor: Color, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(4.dp))
            .border(BorderStroke(1.dp, themeColor), RoundedCornerShape(4.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = themeColor, modifier = Modifier.weight(1f))
        Switch(
            checked = isChecked, 
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = themeColor,
                checkedTrackColor = Color.DarkGray,
                uncheckedThumbColor = Color.Gray,
                uncheckedTrackColor = Color.Black
            )
        )
    }
}

@Composable
fun ConfirmationOverlay(
    title: String,
    subtitle: String,
    confirmText: String,
    cancelText: String,
    confirmColor: Color,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    swapButtons: Boolean = false
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.9f))
            .clickable { /* Block */ }
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(BorderStroke(2.dp, confirmColor), RoundedCornerShape(8.dp))
                .background(Color.Black)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, color = confirmColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(subtitle, color = Color.LightGray, fontSize = 14.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            if (swapButtons) {
                 Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = confirmColor), modifier = Modifier.fillMaxWidth()) {
                     Text(confirmText, color = Color.White)
                 }
                 Spacer(modifier = Modifier.height(8.dp))
                 Button(onClick = onCancel, colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray), modifier = Modifier.fillMaxWidth()) {
                     Text(cancelText, color = Color.White)
                 }
            } else {
                 Button(onClick = onCancel, colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray), modifier = Modifier.fillMaxWidth()) {
                     Text(cancelText, color = Color.White)
                 }
                 Spacer(modifier = Modifier.height(8.dp))
                 Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = confirmColor), modifier = Modifier.fillMaxWidth()) {
                     Text(confirmText, color = Color.White)
                 }
            }
        }
    }
}
