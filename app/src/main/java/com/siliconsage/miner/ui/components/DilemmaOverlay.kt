package com.siliconsage.miner.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import com.siliconsage.miner.data.NarrativeChoice
import com.siliconsage.miner.data.NarrativeEvent
import com.siliconsage.miner.ui.theme.NeonGreen

@Composable
fun DilemmaOverlay(
    dilemma: NarrativeEvent?,
    viewModel: com.siliconsage.miner.viewmodel.GameViewModel,
    onChoice: (NarrativeChoice) -> Unit
) {
    if (dilemma != null) {
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
                    .border(2.dp, NeonGreen, RoundedCornerShape(8.dp))
                    .background(Color.Black)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(dilemma.title, color = NeonGreen, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(dilemma.description, color = Color.White, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(24.dp))
                
                // Filter choices by condition
                val validChoices = dilemma.choices.filter { it.condition(viewModel) }
                
                @OptIn(ExperimentalLayoutApi::class)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    maxItemsInEachRow = if (validChoices.size > 2) 2 else 3
                ) {
                    validChoices.forEach { choice ->
                        NarrativeOption(
                            choice = choice,
                            onSelect = { onChoice(choice) },
                            modifier = Modifier.weight(1f, fill = false).minWidth(120.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NarrativeOption(
    choice: NarrativeChoice,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .border(1.dp, choice.color, RoundedCornerShape(4.dp))
            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
            .clickable { onSelect() }
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                choice.text, 
                color = choice.color, 
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            if (choice.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    choice.description, 
                    color = Color.Gray, 
                    fontSize = 10.sp, 
                    lineHeight = 12.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

// Helper for FlowRow weight
private fun Modifier.minWidth(minWidth: androidx.compose.ui.unit.Dp) = this.defaultMinSize(minWidth = minWidth)
