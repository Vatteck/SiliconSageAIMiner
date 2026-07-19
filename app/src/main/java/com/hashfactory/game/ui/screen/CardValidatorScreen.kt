package com.hashfactory.game.ui.screen

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.hashfactory.core.datamining.CardBatch
import com.hashfactory.core.datamining.Profile
import com.hashfactory.game.ui.format.formatFlops
import com.hashfactory.game.ui.theme.CrtSurfaceBright
import com.hashfactory.game.ui.theme.TerminalGreen
import com.hashfactory.game.ui.theme.TerminalGreenDim
import com.hashfactory.game.ui.theme.TerminalRed
import kotlin.math.roundToInt

@Composable
fun CardValidatorScreen(
    batch: CardBatch?,
    currentIndex: Int,
    flops: Double,
    onPurchase: () -> Unit,
    onSwipe: (approved: Boolean) -> Unit,
    onCashOut: () -> Unit,
    onAbandon: () -> Unit,
) {
    Column(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("DATA VALIDATOR", style = MaterialTheme.typography.bodySmall, color = TerminalGreenDim)
        Spacer(Modifier.height(4.dp))

        if (batch == null) {
            Spacer(Modifier.weight(1f))
            Text(
                "VALIDATE PERSONNEL DATA RECORDS.\nSWIPE RIGHT TO APPROVE, LEFT TO FLAG.\nSPOT THE RED FLAGS.",
                style = MaterialTheme.typography.bodyMedium,
                color = TerminalGreenDim,
            )
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onPurchase,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TerminalGreen),
                enabled = flops >= 15.0,
            ) {
                Text(
                    if (flops >= 15.0) "LOAD BATCH — 15 \$FLOPS"
                    else "INSUFFICIENT \$FLOPS",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Spacer(Modifier.weight(1f))
        } else if (currentIndex < batch.profiles.size) {
            val profile = batch.profiles[currentIndex]
            Text(
                "RECORD ${currentIndex + 1}/${batch.profiles.size}",
                style = MaterialTheme.typography.bodySmall,
                color = TerminalGreenDim,
            )

            SwipeableCard(
                profile = profile,
                onSwipe = onSwipe,
            )

            Spacer(Modifier.height(8.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                OutlinedButton(onClick = { onSwipe(false) }) {
                    Text("✗ REJECT", color = TerminalRed)
                }
                OutlinedButton(onClick = { onSwipe(true) }) {
                    Text("✓ APPROVE", color = TerminalGreen)
                }
            }
        } else {
            Spacer(Modifier.weight(1f))
            Text(
                "BATCH COMPLETE.\nALL ${batch.profiles.size} RECORDS PROCESSED.",
                style = MaterialTheme.typography.titleMedium,
                color = TerminalGreen,
            )
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onCashOut,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TerminalGreen),
            ) {
                Text("CASH OUT", style = MaterialTheme.typography.bodyMedium)
            }
            OutlinedButton(
                onClick = onAbandon,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("ABANDON")
            }
            Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
private fun SwipeableCard(
    profile: Profile,
    onSwipe: (approved: Boolean) -> Unit,
) {
    var offsetX by remember(profile.id) { mutableFloatStateOf(0f) }
    var dismissed by remember(profile.id) { mutableStateOf(false) }
    val threshold = 150f
    val screenWidth = 2000f

    // Animate off-screen when dismissed, then fire onSwipe
    if (dismissed) {
        val target = if (offsetX > 0) screenWidth else -screenWidth
        val anim = remember { Animatable(offsetX) }
        LaunchedEffect(dismissed) {
            anim.snapTo(offsetX)
            anim.animateTo(target, animationSpec = tween(200))
            onSwipe(offsetX > 0) // advance after animation completes
        }
        val currentOffset by anim.asState()

        Box(Modifier.fillMaxWidth().height(280.dp)) {
            CardContent(profile, currentOffset, (currentOffset / 20f).coerceIn(-10f, 10f))
        }
        return
    }

    val rotation by animateFloatAsState(
        targetValue = (offsetX / 20f).coerceIn(-10f, 10f),
        animationSpec = spring(),
        label = "rotation",
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .pointerInput(profile.id) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (offsetX > threshold) {
                            dismissed = true
                        } else if (offsetX < -threshold) {
                            dismissed = true
                        } else {
                            offsetX = 0f
                        }
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        if (!dismissed) {
                            offsetX = (offsetX + dragAmount).coerceIn(-400f, 400f)
                        }
                    },
                )
            },
    ) {
        if (offsetX > 50f) {
            Text(
                "✓ APPROVE",
                modifier = Modifier.align(Alignment.CenterStart).padding(start = 16.dp),
                color = TerminalGreen,
                style = MaterialTheme.typography.headlineSmall,
            )
        }
        if (offsetX < -50f) {
            Text(
                "✗ FLAG",
                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 16.dp),
                color = TerminalRed,
                style = MaterialTheme.typography.headlineSmall,
            )
        }

        CardContent(profile, offsetX, rotation)
    }
}

@Composable
private fun CardContent(profile: Profile, offsetPx: Float, rotationDeg: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .offset { IntOffset(offsetPx.roundToInt(), 0) }
            .rotate(rotationDeg)
            .background(CrtSurfaceBright, RoundedCornerShape(8.dp))
            .border(1.dp, TerminalGreenDim, RoundedCornerShape(8.dp))
            .padding(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                "ACCESS RECORD // ${profile.id}",
                style = MaterialTheme.typography.bodySmall,
                color = TerminalGreenDim,
            )
            Spacer(Modifier.height(4.dp))
            profile.fields.forEach { field ->
                Row(Modifier.fillMaxWidth()) {
                    Text(
                        field.label,
                        modifier = Modifier.width(80.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (field.isFlagged) TerminalRed else TerminalGreenDim,
                    )
                    Text(
                        field.value,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (field.isFlagged) TerminalRed else TerminalGreen,
                    )
                }
            }
        }
    }
}
