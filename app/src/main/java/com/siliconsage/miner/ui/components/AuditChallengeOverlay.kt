package com.siliconsage.miner.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siliconsage.miner.ui.theme.ErrorRed
import com.siliconsage.miner.ui.theme.ElectricBlue
import com.siliconsage.miner.ui.theme.NeonGreen

@Composable
fun AuditChallengeOverlay(
    isVisible: Boolean,
    timer: Int,
    targetHeat: Double,
    currentHeat: Double,
    targetPower: Double,
    currentPower: Double
) {
    if (!isVisible) return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .width(320.dp) // Fixed width for non-blocking feel
                .border(2.dp, ElectricBlue, RoundedCornerShape(8.dp))
                .background(Color.Black.copy(alpha = 0.95f), RoundedCornerShape(8.dp))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = ElectricBlue,
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                "GTC SUDDEN AUDIT",
                color = ElectricBlue,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                "TIME REMAINING: ${timer}s",
                color = if (timer <= 10) ErrorRed else Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Heat Status
            AuditStatRow(
                label = "THERMAL LOAD",
                current = currentHeat,
                target = targetHeat,
                unit = "°C",
                isOk = currentHeat <= targetHeat
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Power Status
            AuditStatRow(
                label = "POWER DRAW",
                current = currentPower,
                target = targetPower,
                unit = "kW",
                isOk = currentPower <= targetPower
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                "ADJUST HARDWARE / PURGE HEAT IMMEDIATELY",
                color = Color.Gray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun AuditStatRow(
    label: String,
    current: Double,
    target: Double,
    unit: String,
    isOk: Boolean
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, color = Color.White, fontSize = 14.sp)
            Text(
                if (isOk) "COMPLIANT" else "NON-COMPLIANT",
                color = if (isOk) NeonGreen else ErrorRed,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        LinearProgressIndicator(
            progress = { (current / (target * 2f)).coerceIn(0.0, 1.0).toFloat() },
            modifier = Modifier.fillMaxWidth().height(8.dp),
            color = if (isOk) NeonGreen else ErrorRed,
            trackColor = Color.DarkGray
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("${String.format("%.1f", current)}$unit", color = if (isOk) NeonGreen else ErrorRed, fontSize = 12.sp)
            Text("TARGET: < ${String.format("%.1f", target)}$unit", color = Color.Gray, fontSize = 12.sp)
        }
    }
}
