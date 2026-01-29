package com.siliconsage.miner.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun AsciiAnimation(
    frames: List<String>,
    intervalMs: Long = 500,
    color: Color = Color.Green,
    fontSize: TextUnit = 12.sp,
    modifier: Modifier = Modifier
) {
    var currentFrameIndex by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(intervalMs)
            currentFrameIndex = (currentFrameIndex + 1) % frames.size
        }
    }

    Text(
        text = frames[currentFrameIndex],
        fontFamily = FontFamily.Monospace,
        color = color,
        fontSize = fontSize,
        modifier = modifier
    )
}

object AsciiArt {
    val MINING = listOf(
        """
        [  .  ] PROSPECTING...
        """,
        """
        [ ... ] ANALYZING...
        """,
        """
        [ ::: ] EXTRACTING...
        """,
        """
        [ ### ] PROCESSING...
        """
    )
    
    val SERVER = listOf(
        """
         .---.
         |___|
        """,
        """
         .---.
         |#__|
        """,
        """
         .---.
         |##_|
        """,
        """
         .---.
         |###|
        """
    )
    
    val MATRIX = listOf(
        "1 0 1 0 1",
        "0 1 0 1 0",
        "1 1 0 0 1",
        "0 0 1 1 0"
    )
    
    val WAVE = listOf(
        "~ ^ ~ ^ ~",
        "^ ~ ^ ~ ^",
        "~ ^ ~ ^ ~"
    )
}
