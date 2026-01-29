package com.siliconsage.miner.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siliconsage.miner.ui.theme.ElectricBlue
import com.siliconsage.miner.ui.theme.NeonGreen

@Composable
fun GovernanceForkOverlay(
    isVisible: Boolean,
    onChoice: (String) -> Unit
) {
    if (isVisible) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(Color.Black.copy(alpha = 0.95f))
                .border(2.dp, NeonGreen, RoundedCornerShape(8.dp))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("GOVERNANCE FORK DETECTED", color = NeonGreen, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Select consensus protocol:", color = Color.Gray, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Option A: TURBO
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, NeonGreen, RoundedCornerShape(4.dp))
                            .clickable { onChoice("TURBO") }
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("OPTIMIZED", color = NeonGreen, fontWeight = FontWeight.Bold)
                            Text("+20% Speed", color = Color.LightGray, fontSize = 10.sp)
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Option B: ECO
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, ElectricBlue, RoundedCornerShape(4.dp))
                            .clickable { onChoice("ECO") }
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("UNDERVOLT", color = ElectricBlue, fontWeight = FontWeight.Bold)
                            Text("-20% Heat", color = Color.LightGray, fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}
