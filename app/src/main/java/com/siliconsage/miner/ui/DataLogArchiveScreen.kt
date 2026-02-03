package com.siliconsage.miner.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siliconsage.miner.viewmodel.GameViewModel
import com.siliconsage.miner.util.DataLogManager

@Composable
fun DataLogArchiveScreen(
    viewModel: GameViewModel,
    onBack: () -> Unit
) {
    val unlockedLogs by viewModel.unlockedDataLogs.collectAsState()
    val themeColor by viewModel.themeColor.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = themeColor
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "DATA LOG ARCHIVE",
                color = themeColor,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(DataLogManager.allDataLogs) { log ->
                val isUnlocked = unlockedLogs.contains(log.id)
                DataLogCard(
                    log = log,
                    isUnlocked = isUnlocked,
                    themeColor = themeColor
                )
            }
        }
    }
}

@Composable
private fun DataLogCard(
    log: com.siliconsage.miner.data.DataLog,
    isUnlocked: Boolean,
    themeColor: Color
) {
    var isExpanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (isUnlocked) themeColor else Color.DarkGray,
                shape = RoundedCornerShape(8.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.75f)
        ),
        onClick = if (isUnlocked) { { isExpanded = !isExpanded } } else { {} }
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isUnlocked) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = Color.DarkGray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = log.id,
                        color = if (isUnlocked) themeColor.copy(alpha=0.7f) else Color.DarkGray,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isUnlocked) log.title else "ENCRYPTED",
                        color = if (isUnlocked) themeColor else Color.DarkGray,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                if (isUnlocked) {
                    Text(
                        text = if (isExpanded) "−" else "+",
                        color = themeColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            if (isExpanded && isUnlocked) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = themeColor.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = log.content,
                    color = Color.White,
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                    modifier = Modifier
                        .background(
                            Color.Black.copy(alpha = 0.5f),
                            RoundedCornerShape(4.dp)
                        )
                        .padding(8.dp)
                )
            }
        }
    }
}
