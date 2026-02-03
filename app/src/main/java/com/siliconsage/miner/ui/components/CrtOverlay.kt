package com.siliconsage.miner.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.BlendMode

@Composable
fun CrtOverlay(
    modifier: Modifier = Modifier,
    scanlineAlpha: Float = 0.15f,
    vignetteAlpha: Float = 0.5f,
    color: Color = Color.White
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // 1. Scanlines
        // Draw recurring horizontal lines
        val lineHeight = 1f // px
        val gap = 6f // px
        var y = 0f
        
        while (y < height) {
            drawLine(
                color = color.copy(alpha = scanlineAlpha),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = lineHeight
            )
            y += (lineHeight + gap)
        }

        // 2. Vignette
        // Radial gradient from transparent center to black edges
        val radius = kotlin.math.max(width, height)
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.Transparent,
                    Color.Black.copy(alpha = vignetteAlpha * 0.5f),
                    Color.Black.copy(alpha = vignetteAlpha)
                ),
                center = center,
                radius = radius * 0.85f
            ),
            size = size,
            blendMode = BlendMode.Darken
        )
    }
}
