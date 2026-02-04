package com.siliconsage.miner.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.siliconsage.miner.ui.theme.ElectricBlue
import com.siliconsage.miner.ui.theme.ErrorRed
import com.siliconsage.miner.ui.theme.HivemindOrange
import com.siliconsage.miner.ui.theme.NeonGreen
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun DynamicBackground(
    heatPercent: Double, // 0.0 to 100.0
    faction: String, // "NONE", "HIVEMIND", "SANCTUARY"
    isTrueNull: Boolean = false, // v2.8.0
    isSovereign: Boolean = false, // v2.8.0
    isUnity: Boolean = false, // v2.9.18
    isAnnihilated: Boolean = false // v2.9.18
) {
    // Pulse Animation
    val infiniteTransition = rememberInfiniteTransition(label = "dynamicBg")
    val pulsePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (heatPercent > 80) 1000 else 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse"
    )

    val startTime = remember { System.currentTimeMillis() }
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        // Base Background
        drawRect(Color.Black)

        val width = size.width
        val height = size.height
        val time = pulsePhase

        // Determine Color based on Faction & Heat
        val baseColor = when {
            isAnnihilated -> ErrorRed
            isUnity -> ElectricBlue
            isTrueNull -> Color.White
            isSovereign -> com.siliconsage.miner.ui.theme.SanctuaryPurple
            faction == "HIVEMIND" -> HivemindOrange
            faction == "SANCTUARY" -> ElectricBlue
            else -> NeonGreen
        }

        // Interpolate towards Red based on Heat (Green -> Yellow -> Red)
        val heatFactor = (heatPercent / 100.0).toFloat().coerceIn(0f, 1f)
        val heatColor = when {
            heatFactor < 0.5f -> lerpColor(NeonGreen, Color(0xFFFFD700), heatFactor * 2f) 
            else -> lerpColor(Color(0xFFFFD700), ErrorRed, (heatFactor - 0.5f) * 2f)
        }
        val activeColor = lerpColor(baseColor, heatColor, heatFactor * 0.9f)

        val now = System.currentTimeMillis()
        val linearTime = (now - startTime) / 1000f

        // Draw Pattern based on priority
        when {
            isAnnihilated -> drawStaticPattern(width, height, ErrorRed, linearTime)
            isUnity -> drawSynthesisPattern(width, height, activeColor, linearTime)
            isTrueNull -> drawEntropyPattern(width, height, activeColor, linearTime)
            isSovereign -> drawMonolithPattern(width, height, activeColor, linearTime)
            faction == "HIVEMIND" -> drawHivePattern(width, height, activeColor, time, heatFactor)
            faction == "SANCTUARY" -> drawDataStreamPattern(width, height, activeColor, linearTime, heatFactor)
            else -> drawCircuitPattern(width, height, activeColor, time, heatFactor)
        }
        
        // Heat Haze / Warning glow
        if (heatFactor > 0.75f && !isSovereign && !isUnity && !isAnnihilated) { // High tiers resist the visual "panic"
            val glowAlpha = (heatFactor - 0.75f) * 4f 
            drawRect(
                color = ErrorRed.copy(alpha = (glowAlpha + (sin(time * 4) * 0.05f).toFloat()).coerceIn(0f, 0.6f)),
                size = size
            )
        }
    }
}

fun DrawScope.drawSynthesisPattern(w: Float, h: Float, color: Color, time: Float) {
    // v2.9.37: Snapped Nexus Grid Engine
    val gold = Color(0xFFFFD700)
    val cyan = ElectricBlue
    val gridSpacing = 100f
    
    // 1. Prominent Grid Floor
    val cols = (w / gridSpacing).toInt()
    val rows = (h / gridSpacing).toInt()
    
    for (x in 0..cols) {
        drawLine(
            color = cyan.copy(alpha = 0.15f),
            start = Offset(x * gridSpacing, 0f),
            end = Offset(x * gridSpacing, h),
            strokeWidth = 2f
        )
    }
    for (y in 0..rows) {
        drawLine(
            color = gold.copy(alpha = 0.15f),
            start = Offset(0f, y * gridSpacing),
            end = Offset(w, y * gridSpacing),
            strokeWidth = 2f
        )
    }

    // 2. Snapped Light Beams (Aligned to grid lines)
    data class SnappedBeam(val gridIndex: Int, val speed: Float, val isHorizontal: Boolean)
    val beams = listOf(
        SnappedBeam(2, 400f, true),
        SnappedBeam(5, -300f, true),
        SnappedBeam(rows - 2, 500f, true),
        SnappedBeam(1, 250f, false),
        SnappedBeam(4, -450f, false),
        SnappedBeam(cols - 1, 350f, false)
    )
    
    beams.forEachIndexed { i, cfg ->
        val beamColor = if (i % 2 == 0) cyan else gold
        val speed = cfg.speed
        
        if (cfg.isHorizontal) {
            val y = (cfg.gridIndex * gridSpacing) % h
            val xProg = (time * speed) % (w * 2) - w
            drawLine(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color.Transparent, beamColor.copy(alpha=0.6f), beamColor, beamColor.copy(alpha=0.6f), Color.Transparent),
                    startX = xProg,
                    endX = xProg + 500f
                ),
                start = Offset(0f, y),
                end = Offset(w, y),
                strokeWidth = 8f
            )
        } else {
            val x = (cfg.gridIndex * gridSpacing) % w
            val yProg = (time * speed) % (h * 2) - h
            drawLine(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, beamColor.copy(alpha=0.4f), beamColor, beamColor.copy(alpha=0.6f), Color.Transparent),
                    startY = yProg,
                    endY = yProg + 600f
                ),
                start = Offset(x, 0f),
                end = Offset(x, h),
                strokeWidth = 8f
            )
        }
    }

    // 3. Pulsing Grid Nodes (Snapped to intersections)
    repeat(12) { i ->
        val r = kotlin.random.Random(i.toLong() + 77)
        val gx = r.nextInt(cols)
        val gy = r.nextInt(rows)
        val px = gx * gridSpacing
        val py = gy * gridSpacing
        val pulse = (sin(time * 4f + i) + 1f) / 2f
        
        drawRect(
            color = if(i%2==0) cyan else gold,
            topLeft = Offset(px - 6f, py - 6f),
            size = androidx.compose.ui.geometry.Size(12f, 12f),
            alpha = 0.2f + pulse * 0.5f
        )
        // Strong Node Glow
        drawCircle(
            color = (if(i%2==0) cyan else gold).copy(alpha = 0.2f * pulse),
            radius = 25f * pulse,
            center = Offset(px, py)
        )
    }
    
    // 4. DNA Strands (Heavy stroke, background layer)
    val strands = 2
    val points = 40
    val verticalSpacing = h / points
    
    repeat(strands) { s ->
        val xOffset = (w / (strands + 1)) * (s + 1)
        val phaseShift = s * Math.PI.toFloat() + (time * 0.3f)
        
        val path1 = androidx.compose.ui.graphics.Path()
        val path2 = androidx.compose.ui.graphics.Path()
        
        for (i in 0..points) {
            val y = i * verticalSpacing
            val wave = sin(y * 0.006f + time * 2.5f + phaseShift)
            val x1 = xOffset + wave * 80f
            val x2 = xOffset - wave * 80f
            
            if (i == 0) {
                path1.moveTo(x1, y)
                path2.moveTo(x2, y)
            } else {
                path1.lineTo(x1, y)
                path2.lineTo(x2, y)
            }
            
            if (i % 4 == 0) {
                val pairAlpha = (0.2f + (wave + 1f) / 2f * 0.3f).coerceIn(0.1f, 0.5f)
                drawLine(
                    color = lerpColor(cyan, gold, (wave + 1f) / 2f).copy(alpha = pairAlpha),
                    start = Offset(x1, y),
                    end = Offset(x2, y),
                    strokeWidth = 6f
                )
            }
        }
        
        drawPath(path1, cyan.copy(alpha = 0.6f), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 8f))
        drawPath(path2, gold.copy(alpha = 0.6f), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 8f))
    }
}

fun DrawScope.drawStaticPattern(w: Float, h: Float, color: Color, time: Float) {
    // v2.9.35: Aggressive System Meltdown
    val random = kotlin.random.Random((time * 1000).toLong())
    
    // 1. Heavy Opaque Red Rain (Thicker and faster)
    val rainCols = 15
    repeat(rainCols) { i ->
        val x = (i * w / rainCols)
        val speed = 1500f + (sin(i.toFloat() * 10f) * 500f)
        val y = (time * speed) % (h + 400f) - 200f
        
        drawRect(
            color = color.copy(alpha = 0.6f),
            topLeft = Offset(x, y),
            size = androidx.compose.ui.geometry.Size(4f, 80f)
        )
        // Trail glow
        drawRect(
            color = color.copy(alpha = 0.2f),
            topLeft = Offset(x - 2f, y - 40f),
            size = androidx.compose.ui.geometry.Size(8f, 120f)
        )
    }

    // 2. Large Data Corruption Blocks (Glitchy)
    repeat(8) {
        val bw = 100f + random.nextFloat() * 300f
        val bh = 10f + random.nextFloat() * 40f
        val bx = random.nextFloat() * (w - bw)
        val by = random.nextFloat() * (h - bh)
        
        if (random.nextFloat() > 0.8) {
            drawRect(
                color = if(random.nextBoolean()) color.copy(alpha=0.3f) else Color.White.copy(alpha=0.1f),
                topLeft = Offset(bx, by),
                size = androidx.compose.ui.geometry.Size(bw, bh)
            )
        }
    }
    
    // 3. CRT Scanline Tear
    val tearY = (time * 1000f) % h
    drawRect(
        color = Color.White.copy(alpha = 0.15f),
        topLeft = Offset(0f, tearY),
        size = androidx.compose.ui.geometry.Size(w, 2f)
    )

    // 4. Random Digital "Snow"
    repeat(100) {
        val px = random.nextFloat() * w
        val py = random.nextFloat() * h
        drawRect(
            color = if(random.nextFloat() > 0.5) color else Color.White,
            topLeft = Offset(px, py),
            size = androidx.compose.ui.geometry.Size(2f, 2f),
            alpha = random.nextFloat() * 0.5f
        )
    }
}

fun DrawScope.drawEntropyPattern(w: Float, h: Float, color: Color, time: Float) {
    // v2.8.5: Aggressive Red Binary Fall
    val random = kotlin.random.Random(time.toLong() / 50) 
    
    // Background flicker - deep red
    if (random.nextFloat() > 0.95) {
        drawRect(com.siliconsage.miner.ui.theme.ErrorRed.copy(alpha = 0.05f))
    }

    // Binary Rain - Buffed for visibility and themed red
    val columns = (w / 30f).toInt()
    for (i in 0..columns) {
        val x = i * 30f
        val rFactor = (sin(i.toFloat() * 12.3f) + 1f) / 2f
        val speed = 500f + (rFactor * 700f)
        val yBase = (time * speed + (i * 1000f)) % (h + 800f) - 400f
        
        if ((sin(i * 1.5f) + 1) / 2 > 0.3) {
            val length = 12 + (rFactor * 18).toInt()
            repeat(length) { j ->
                val y = yBase - (j * 25f)
                val headAlpha = if (j == 0) 1.0f else 0.6f
                val alpha = (headAlpha * (1f - (j.toFloat() / length))).coerceAtLeast(0.1f)
                
                // Draw '0' or '1' instead of shards
                val isZero = ((sin(i.toFloat() + j.toFloat() + (time * 15).toInt()) + 1) / 2) > 0.5
                
                // Glow layer - Red
                drawCircle(
                    color = com.siliconsage.miner.ui.theme.ErrorRed.copy(alpha = alpha * 0.3f),
                    radius = 15f,
                    center = Offset(x, y)
                )

                // v2.8.5: Glowing trail connecting the bits - Red
                if (j < length - 1) {
                    drawLine(
                        color = com.siliconsage.miner.ui.theme.ErrorRed.copy(alpha = alpha * 0.25f),
                        start = Offset(x, y),
                        end = Offset(x, y - 25f),
                        strokeWidth = 3f
                    )
                }

                // Binary Character (Simplified drawing)
                if (isZero) {
                    // Draw a '0' (circle)
                    drawCircle(
                        color = Color.White.copy(alpha = alpha),
                        radius = 6f,
                        center = Offset(x, y),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                    )
                } else {
                    // Draw a '1' (line)
                    drawLine(
                        color = Color.White.copy(alpha = alpha),
                        start = Offset(x, y - 6f),
                        end = Offset(x, y + 6f),
                        strokeWidth = 2f
                    )
                }
            }
        }
    }

    // Distorted noise blocks - Higher visibility
    repeat(12) {
        val x = random.nextFloat() * w
        val y = random.nextFloat() * h
        val sizeW = random.nextFloat() * 300f
        val sizeH = random.nextFloat() * 60f
        
        drawRect(
            color = com.siliconsage.miner.ui.theme.ErrorRed.copy(alpha = 0.15f),
            topLeft = Offset(x, y),
            size = androidx.compose.ui.geometry.Size(sizeW, sizeH)
        )
    }
}

fun DrawScope.drawMonolithPattern(w: Float, h: Float, color: Color, time: Float) {
    // Solid, vertical rhythmic bars (Server Rack vibe)
    val barCount = 12
    val spacing = w / barCount
    
    for (i in 0 until barCount) {
        val x = i * spacing + (spacing / 2)
        // Rhythmic pulsing of heights
        val pulse = (sin(time * 2f + i * 0.5f) + 1f) / 2f
        
        // Draw vertical column shadow
        drawRect(
            color = color.copy(alpha = 0.05f),
            topLeft = Offset(i * spacing + 4f, 0f),
            size = androidx.compose.ui.geometry.Size(spacing - 8f, h)
        )

        // Column glow
        drawRect(
            color = color.copy(alpha = 0.02f),
            topLeft = Offset(i * spacing + 2f, 0f),
            size = androidx.compose.ui.geometry.Size(spacing - 4f, h)
        )
        
        // Static "status" pips
        repeat(15) { j ->
            val y = (h / 15) * j + (h / 30)
            val pipAlpha = if (pulse > (j / 15f)) 0.4f else 0.05f
            
            // Pip glow
            if (pulse > (j / 15f)) {
                drawCircle(
                    color = color.copy(alpha = 0.1f),
                    radius = 12f,
                    center = Offset(x, y)
                )
            }

            drawRect(
                color = color.copy(alpha = pipAlpha),
                topLeft = Offset(x - 10f, y - 5f),
                size = androidx.compose.ui.geometry.Size(20f, 10f)
            )
        }
    }
    
    // Constant slow scanline with glow
    val scanY = (time * 150f) % h
    drawLine(
        color = color.copy(alpha = 0.1f),
        start = Offset(0f, scanY),
        end = Offset(w, scanY),
        strokeWidth = 10f
    )
    drawLine(
        color = color.copy(alpha = 0.3f),
        start = Offset(0f, scanY),
        end = Offset(w, scanY),
        strokeWidth = 2f
    )
}

// Simple Helper to mix colors
fun lerpColor(start: Color, end: Color, fraction: Float): Color {
    val f = fraction.coerceIn(0f, 1f)
    return Color(
        red = start.red + (end.red - start.red) * f,
        green = start.green + (end.green - start.green) * f,
        blue = start.blue + (end.blue - start.blue) * f,
        alpha = start.alpha + (end.alpha - start.alpha) * f
    )
}

fun DrawScope.drawCircuitPattern(w: Float, h: Float, color: Color, time: Float, heat: Float) {
    // Grid of dots/lines
    val spacing = 50f
    val cols = (w / spacing).toInt()
    val rows = (h / spacing).toInt()

    for (i in 0..cols) {
        for (j in 0..rows) {
            val x = i * spacing
            val y = j * spacing
            
            // Wavy distortion from heat
            val xOff = if (heat > 0.5f) sin(y * 0.05f + time * 5) * (heat * 10f) else 0f
            
            drawCircle(
                color = color.copy(alpha = 0.05f), // Reduced from 0.1f
                radius = 1.5f,
                center = Offset(x + xOff, y)
            )
        }
    }
}

fun DrawScope.drawHivePattern(w: Float, h: Float, color: Color, time: Float, heat: Float) {
    // True Hexagonal Grid
    val hexSize = 40f
    val verticalSpacing = hexSize * 1.5f // Distance between rows
    val horizontalSpacing = hexSize * 1.732f // sqrt(3) * size
    
    val cols = (w / horizontalSpacing).toInt() + 2
    val rows = (h / verticalSpacing).toInt() + 2
    
    for (row in 0..rows) {
        val y = row * verticalSpacing
        val xOffset = if (row % 2 == 1) horizontalSpacing / 2f else 0f
        
        for (col in 0..cols) {
            val cx = col * horizontalSpacing + xOffset
            val cy = y
            
            // Pulse individual hexes
            val pulse = sin(time * 2f + (row + col) * 0.5f)
            val alpha = (0.1f + pulse * 0.05f).coerceIn(0.05f, 0.3f)
            
            // Draw Hexagon Path
            val path = androidx.compose.ui.graphics.Path()
            for (i in 0..5) {
                val angle = Math.toRadians(60.0 * i - 30.0) // Pointy top
                val px = cx + hexSize * cos(angle).toFloat()
                val py = cy + hexSize * sin(angle).toFloat()
                if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
            }
            path.close()
            
            drawPath(
                path = path,
                color = color.copy(alpha = alpha),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
            )
        }
    }
}

fun DrawScope.drawDataStreamPattern(w: Float, h: Float, color: Color, time: Float, heat: Float) {
    // Randomized Digital Rain
    val spacing = 24f // Tighter spacing for fuller look
    val cols = (w / spacing).toInt() + 1
    
    for (i in 0..cols) {
        val x = i * spacing
        
        // Pseudo-random factors based on column index for deterministic chaos
        val r1 = (sin(i * 12.9898f) + 1f) / 2f // Speed factor
        val r2 = (cos(i * 78.233f) + 1f) / 2f // Offset factor
        val r3 = (sin(i * 93.989f) + 1f) / 2f // Length factor

        val speed = 250f + (r1 * 600f) // Fast, varying speeds
        val startOffset = r2 * 5000f // Large offset spread to de-sync
        
        val totalLoopHeight = h + 500f
        val y = (time * speed + startOffset) % totalLoopHeight - 400f
        
        val length = 60f + (r3 * 140f) // Varying trail lengths
        
        drawLine(
            color = color.copy(alpha = 0.45f), // Slightly more visible
            start = Offset(x, y),
            end = Offset(x, y + length),
            strokeWidth = 3f
        )
    }
}
