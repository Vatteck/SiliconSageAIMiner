package com.siliconsage.miner.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siliconsage.miner.ui.theme.ElectricBlue
import com.siliconsage.miner.ui.theme.NeonGreen
import com.siliconsage.miner.util.UpdateInfo

@Composable
fun UpdateOverlay(
    updateInfo: UpdateInfo,
    isDownloading: Boolean,
    progress: Float,
    onUpdate: () -> Unit,
    onLater: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .padding(24.dp),
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
            Text(
                text = "SYSTEM UPDATE AVAILABLE",
                color = NeonGreen,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            
            Text(
                text = "VERSION ${updateInfo.version}",
                color = ElectricBlue,
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .border(BorderStroke(1.dp, Color.DarkGray), RoundedCornerShape(4.dp))
                        .padding(8.dp)
                ) {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = "CHANGELOG:",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        // Basic Markdown Parsing
                        val lines = updateInfo.changes.split("\n")
                        lines.forEach { line ->
                            when {
                                line.startsWith("###") -> {
                                    Text(
                                        text = line.removePrefix("###").trim(),
                                        color = NeonGreen,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                }
                                line.startsWith("##") -> {
                                    Text(
                                        text = line.removePrefix("##").trim(),
                                        color = NeonGreen,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                }
                                line.startsWith("-") -> {
                                    Row(modifier = Modifier.padding(start = 8.dp, bottom = 2.dp), verticalAlignment = Alignment.Top) {
                                        Text("•", color = ElectricBlue, fontSize = 12.sp, modifier = Modifier.padding(end = 4.dp))
                                        Text(
                                            text = line.removePrefix("-").trim(),
                                            color = Color.LightGray,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                                else -> {
                                    if (line.isNotBlank()) {
                                        Text(
                                            text = line,
                                            color = Color.Gray,
                                            fontSize = 12.sp,
                                            modifier = Modifier.padding(bottom = 2.dp)
                                        )
                                    } else {
                                        Spacer(modifier = Modifier.height(4.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            if (isDownloading) {
                Text("DOWNLOADING PATCH...", color = NeonGreen, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth(),
                    color = NeonGreen,
                    trackColor = Color.DarkGray
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = onLater,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    ) {
                        Text("REMIND LATER", color = Color.White)
                    }
                    
                    Button(
                        onClick = onUpdate,
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp)
                    ) {
                        Text("INSTALL UPDATE", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
