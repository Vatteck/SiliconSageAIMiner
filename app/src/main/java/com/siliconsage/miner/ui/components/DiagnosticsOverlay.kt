package com.siliconsage.miner.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import com.siliconsage.miner.ui.theme.ErrorRed
import com.siliconsage.miner.ui.theme.NeonGreen

@Composable
fun DiagnosticsOverlay(
    isVisible: Boolean,
    gridState: List<Boolean>,
    onTap: (Int) -> Unit
) {
    if (isVisible) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(Color.Black.copy(alpha = 0.9f))
                .border(2.dp, ErrorRed, RoundedCornerShape(8.dp))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("NETWORK INSTABILITY DETECTED", color = ErrorRed, fontWeight = FontWeight.Bold)
                Text("-50% EFFICIENCY", color = ErrorRed, fontSize = 12.sp)
                Text("Tap corrupted nodes to repair", color = Color.Gray, fontSize = 10.sp)
                
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(250.dp).fillMaxWidth()
                ) {
                    items(9) { index ->
                        val isCorrupted = gridState.getOrElse(index) { false }
                        Box(
                            modifier = Modifier
                                .height(60.dp)
                                .background(
                                    if (isCorrupted) ErrorRed.copy(alpha = 0.6f) else NeonGreen.copy(alpha = 0.2f),
                                    RoundedCornerShape(4.dp)
                                )
                                .border(
                                    1.dp,
                                    if (isCorrupted) ErrorRed else ElectricBlue,
                                    RoundedCornerShape(4.dp)
                                )
                                .clickable { onTap(index) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isCorrupted) {
                                Text("!", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
