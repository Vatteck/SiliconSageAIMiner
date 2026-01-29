package com.siliconsage.miner.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.siliconsage.miner.ui.theme.ElectricBlue
import com.siliconsage.miner.ui.theme.NeonGreen

@Composable
fun NewsHistoryModal(
    isVisible: Boolean,
    history: List<String>,
    onDismiss: () -> Unit
) {
    if (!isVisible) return

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f)
                .border(BorderStroke(1.dp, NeonGreen), RoundedCornerShape(8.dp)),
            color = Color.Black,
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("MARKET INTELLIGENCE", color = NeonGreen, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = NeonGreen)
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(history) { item ->
                        val color = when {
                            item.contains("[BEAR]") || item.contains("[HEAT_UP]") || item.contains("[GLITCH]") -> Color(0xFFFF6B6B) // Light Red
                            item.contains("[BULL]") || item.contains("[HEAT_DOWN]") -> Color(0xFF6BFF6B) // Light Green
                            item.contains("[STORY_PROG]") -> ElectricBlue
                            else -> Color.Gray
                        }
                        
                        Text(
                            text = "> $item",
                            color = color,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
