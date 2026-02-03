package com.siliconsage.miner.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siliconsage.miner.ui.theme.ElectricBlue
import com.siliconsage.miner.ui.theme.NeonGreen
import com.siliconsage.miner.ui.AsciiArt

@Composable
fun OfflineEarningsDialog(
    isVisible: Boolean,
    timeOfflineSec: Long,
    floopsEarned: Double,
    heatCooled: Double,
    insightEarned: Double,
    onDismiss: () -> Unit
) {
    if (!isVisible) return

    // Format Usage
    fun formatLargeNumber(value: Double): String {
        val suffixes = listOf("", "k", "M", "G", "T", "P", "E", "Z", "Y")
        var v = value
        var suffixIndex = 0
        while (v >= 1000 && suffixIndex < suffixes.size - 1) {
            v /= 1000
            suffixIndex++
        }
        return String.format("%.2f%s", v, suffixes[suffixIndex])
    }

    fun formatTime(seconds: Long): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return if (h > 0) "${h}h ${m}m ${s}s" else "${m}m ${s}s"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.9f))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(BorderStroke(2.dp, NeonGreen), RoundedCornerShape(8.dp))
                .background(Color.Black)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "SYSTEM RESUMED",
                color = NeonGreen,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // ASCII Logo or Art
            com.siliconsage.miner.ui.AsciiAnimation(
                frames = com.siliconsage.miner.ui.AsciiArt.SERVER,
                intervalMs = 500,
                color = ElectricBlue,
                fontSize = 8.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Stats Block
            Text("TIME OFFLINE: ${formatTime(timeOfflineSec)}", color = Color.Gray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            
            StatRow("FLOPS MINED", "+${formatLargeNumber(floopsEarned)}")
            if (heatCooled > 0) {
                StatRow("HEAT DISSIPATED", "-${String.format("%.1f", heatCooled)}%")
            }
            if (insightEarned > 0) {
                StatRow("INSIGHT GAINED", "+${String.format("%.1f", insightEarned)}")
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                shape = RectangleShape,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("ACKNOWLEDGE", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.Gray, fontSize = 14.sp)
        Text(value, color = ElectricBlue, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}
