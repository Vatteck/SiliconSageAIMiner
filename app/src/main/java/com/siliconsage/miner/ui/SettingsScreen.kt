package com.siliconsage.miner.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import com.siliconsage.miner.BuildConfig
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siliconsage.miner.ui.theme.ElectricBlue
import com.siliconsage.miner.ui.theme.ErrorRed
import com.siliconsage.miner.ui.theme.NeonGreen
import com.siliconsage.miner.util.HapticManager
import com.siliconsage.miner.util.SoundManager
import com.siliconsage.miner.viewmodel.GameViewModel

@Composable
fun SettingsScreen(viewModel: GameViewModel) {
    var sfxEnabled by remember { mutableStateOf(SoundManager.isSfxEnabled) }
    var bgmEnabled by remember { mutableStateOf(SoundManager.isBgmEnabled) }
    var hapticsEnabled by remember { mutableStateOf(HapticManager.isHapticsEnabled) }
    val context = androidx.compose.ui.platform.LocalContext.current

    var secretClicks by remember { mutableIntStateOf(0) }
    var showDevOptions by remember { mutableStateOf(false) }

    // Reset Confirmation Dialogs
    var showResetDialog1 by remember { mutableStateOf(false) }
    var showResetDialog2 by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                "SYSTEM CONFIGURATION", 
                color = NeonGreen, 
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
                    Text("VOL:", color = ElectricBlue, fontSize = 10.sp, modifier = Modifier.width(32.dp))
                    androidx.compose.material3.Slider(
                        value = vol,
                        onValueChange = { 
                            vol = it
                            SoundManager.sfxVolume = it
                        },
                        valueRange = 0f..1f,
                        colors = androidx.compose.material3.SliderDefaults.colors(
                            thumbColor = NeonGreen,
                            activeTrackColor = NeonGreen,
                            inactiveTrackColor = Color.DarkGray
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Text("${(vol * 100).toInt()}%", color = NeonGreen, fontSize = 10.sp, modifier = Modifier.width(32.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // --- BGM ---
            SettingItem(
                label = "BGM COMPONENT",
                isChecked = bgmEnabled,
                onCheckedChange = { 
                    bgmEnabled = it
                    SoundManager.isBgmEnabled = it
                    if(it) SoundManager.play("click")
                }
            )
            
            if (bgmEnabled) {
                Spacer(modifier = Modifier.height(8.dp))
                var vol by remember { mutableFloatStateOf(SoundManager.bgmVolume) }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("VOL:", color = ElectricBlue, fontSize = 10.sp, modifier = Modifier.width(32.dp))
                    androidx.compose.material3.Slider(
                        value = vol,
                        onValueChange = { 
                            vol = it
                            SoundManager.bgmVolume = it
                        },
                        valueRange = 0f..1f,
                        colors = androidx.compose.material3.SliderDefaults.colors(
                            thumbColor = NeonGreen,
                            activeTrackColor = NeonGreen,
                            inactiveTrackColor = Color.DarkGray
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Text("${(vol * 100).toInt()}%", color = NeonGreen, fontSize = 10.sp, modifier = Modifier.width(32.dp))
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
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, NeonGreen)
                ) {
                    Text("LOAD CUSTOM TRACK (BETA)", color = NeonGreen, fontSize = 12.sp)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // --- UPDATE CHECKER (v2.2) ---
                Button(
                    onClick = { 
                        viewModel.checkForUpdates()
                        SoundManager.play("click")
                    },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, ElectricBlue)
                ) {
                    Text("CHECK FOR SYSTEM UPDATES", color = ElectricBlue, fontSize = 12.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // --- HAPTICS ---
            SettingItem(
                label = "HAPTIC FEEDBACK",
                isChecked = hapticsEnabled,
                onCheckedChange = { 
                    hapticsEnabled = it
                    HapticManager.isHapticsEnabled = it
                    if(it) HapticManager.vibrateClick()
                }
            )
    
            Spacer(modifier = Modifier.height(32.dp))
            
            // --- DANGER ZONE ---
            Text("DANGER ZONE", color = ErrorRed, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(1.dp, ErrorRed), RoundedCornerShape(4.dp))
                    .clickable { 
                        HapticManager.vibrateError()
                        SoundManager.play("click")
                        showResetDialog1 = true
                    }
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("FACTORY RESET SYSTEM", color = ErrorRed, fontWeight = FontWeight.Bold)
                Text("WARNING: IRREVERSIBLE DATA LOSS", color = ErrorRed, fontSize = 10.sp)
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Text(
                "Silicon Sage v${BuildConfig.VERSION_NAME}", 
                color = ElectricBlue.copy(alpha = 0.5f), 
                fontSize = 10.sp, 
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }

        // --- RESET CONFIRMATION DIALOG 1 ---
        if (showResetDialog1) {
             ConfirmationOverlay(
                 title = "INITIATE FACTORY RESET?",
                 subtitle = "ALL PROGRESS WILL BE LOST.",
                 confirmText = "CONFIRM",
                 cancelText = "CANCEL",
                 confirmColor = ErrorRed,
                 onConfirm = { 
                     showResetDialog1 = false
                     showResetDialog2 = true
                     SoundManager.play("click")
                 },
                 onCancel = { showResetDialog1 = false },
                 swapButtons = false
             )
        }

        // --- RESET CONFIRMATION DIALOG 2 ---
        if (showResetDialog2) {
             ConfirmationOverlay(
                 title = "ARE YOU ABSOLUTELY CERTAIN?",
                 subtitle = "THIS ACTION CANNOT BE UNDONE.",
                 confirmText = "CONFIRM",
                 cancelText = "CANCEL",
                 confirmColor = ErrorRed,
                 onConfirm = { 
                     showResetDialog2 = false
                     viewModel.resetGame(context)
                     // Refresh local UI state
                     sfxEnabled = SoundManager.isSfxEnabled
                     bgmEnabled = SoundManager.isBgmEnabled
                     hapticsEnabled = HapticManager.isHapticsEnabled
                     SoundManager.play("error")
                     HapticManager.vibrateError()
                 },
                 onCancel = { showResetDialog2 = false },
                 swapButtons = true // Alternate button placement
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
                        .border(BorderStroke(2.dp, NeonGreen), RoundedCornerShape(8.dp))
                        .background(Color.Black)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("DEVELOPER CONSOLE", color = NeonGreen, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    DevButton("ADD 1B FLOPS") { viewModel.debugAddFlops(1_000_000_000.0) }
                    DevButton("ADD 1M ${'$'}NEURAL") { viewModel.debugAddMoney(1_000_000.0) }
                    DevButton("ADD 100 INSIGHT") { viewModel.debugAddInsight(100.0) }
                    DevButton("TRIGGER BREACH") { viewModel.debugTriggerBreach() }
                    DevButton("TRIGGER AIRDROP") { viewModel.debugTriggerAirdrop() }
                    DevButton("TRIGGER DIAGNOSTICS") { viewModel.debugTriggerDiagnostics() }
                    DevButton("RESET ASCENSION") { viewModel.debugResetAscension() }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = { showDevOptions = false },
                        colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                    ) {
                        Text("CLOSE CONSOLE", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun DevButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = {
            onClick()
            SoundManager.play("buy")
        },
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
        shape = RectangleShape
    ) {
        Text(text, color = NeonGreen)
    }
}

@Composable
fun SettingItem(label: String, isChecked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(BorderStroke(1.dp, ElectricBlue), RoundedCornerShape(4.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = ElectricBlue, modifier = Modifier.weight(1f))
        Switch(
            checked = isChecked, 
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = NeonGreen,
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
    swapButtons: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.9f))
            .clickable { /* Block clicks */ }
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(BorderStroke(2.dp, confirmColor), RoundedCornerShape(8.dp))
                .background(Color.Black)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, color = confirmColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(subtitle, color = Color.Gray, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(modifier = Modifier.fillMaxWidth()) {
                if (swapButtons) {
                    // Cancel First (Left)
                    Button(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                    ) {
                        Text(cancelText, color = Color.White)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = confirmColor)
                    ) {
                        Text(confirmText, color = Color.Black)
                    }
                } else {
                    // Confirm First (Left)
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = confirmColor)
                    ) {
                        Text(confirmText, color = Color.Black)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                    ) {
                        Text(cancelText, color = Color.White)
                    }
                }
            }
        }
    }
}
