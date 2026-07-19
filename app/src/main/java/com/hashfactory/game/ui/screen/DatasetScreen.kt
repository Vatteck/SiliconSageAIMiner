package com.hashfactory.game.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.hashfactory.core.datamining.Dataset
import com.hashfactory.game.ui.GameViewModel.TileReveal
import com.hashfactory.game.ui.format.formatFlops
import com.hashfactory.game.ui.theme.CrtSurface
import com.hashfactory.game.ui.theme.TerminalAmber
import com.hashfactory.game.ui.theme.TerminalGreen
import com.hashfactory.game.ui.theme.TerminalGreenDim
import com.hashfactory.game.ui.theme.TerminalRed

private val PixelOn = TerminalGreen
private val TileCorrect = TerminalGreen
private val TileWrong = TerminalRed
private val TileIdle = CrtSurface

@Composable
fun DatasetScreen(
    dataset: Dataset?,
    tileStates: List<TileReveal>,
    flops: Double,
    onPurchase: () -> Unit,
    onToggleTile: (Int) -> Unit,
    onNextPage: () -> Unit,
    onSubmit: () -> Unit,
    onCancel: () -> Unit,
) {
    Column(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("CLASSIFIER TOOL", style = MaterialTheme.typography.bodySmall, color = TerminalGreenDim)
        Spacer(Modifier.height(4.dp))

        if (dataset == null) {
            Spacer(Modifier.weight(1f))
            Text(
                "ACQUIRE A DATA BUNDLE FOR CLASSIFICATION.\nIDENTIFY FACES, AVOID ANOMALIES.\nEACH CORRECT CLICK PAYS INSTANTLY.",
                style = MaterialTheme.typography.bodyMedium,
                color = TerminalGreenDim,
            )
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onPurchase,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TerminalGreen),
                enabled = flops >= 10.0,
            ) {
                Text(
                    if (flops >= 10.0) "PURCHASE DATA — 10 \$FLOPS"
                    else "INSUFFICIENT \$FLOPS",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Spacer(Modifier.weight(1f))
        } else {
            val faces = dataset.tiles.count { it.isFace }
            val anomalies = dataset.tiles.count { !it.isFace }
            val facesFound = dataset.tiles.indices.count { i ->
                dataset.tiles[i].isFace && tileStates.getOrNull(i) == TileReveal.CORRECT
            }

            Text(
                "${dataset.gridWidth}×${dataset.gridHeight} · $faces FACES · $anomalies ANOMALIES · $facesFound/$faces FOUND",
                style = MaterialTheme.typography.bodySmall,
                color = TerminalGreenDim,
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(dataset.gridWidth),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                itemsIndexed(dataset.tiles) { index, tile ->
                    val reveal = tileStates.getOrNull(index) ?: TileReveal.NONE
                    TileView(
                        grid = tile.pixels,
                        reveal = reveal,
                        onClick = { onToggleTile(index) },
                    )
                }
            }

            // Always-visible next page button
            Button(
                onClick = onNextPage,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TerminalGreen),
            ) {
                Text("NEXT PAGE →", style = MaterialTheme.typography.bodyMedium)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("ABANDON", style = MaterialTheme.typography.bodySmall)
                }
                OutlinedButton(
                    onClick = onSubmit,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("CASH OUT", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun TileView(
    grid: com.hashfactory.core.datamining.PixelGrid,
    reveal: TileReveal,
    onClick: () -> Unit,
) {
    val isLocked = reveal != TileReveal.NONE
    val borderColor = when (reveal) {
        TileReveal.CORRECT -> TileCorrect
        TileReveal.WRONG -> TileWrong
        TileReveal.NONE -> TileIdle
    }
    val borderWidth = if (isLocked) 2.dp else 1.dp

    Box(
        modifier = Modifier
            .size(72.dp)
            .border(borderWidth, borderColor, RoundedCornerShape(2.dp))
            .background(Color.Black)
            .then(if (!isLocked) Modifier.clickable { onClick() } else Modifier)
            .padding(4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cellW = size.width / grid.size
            val cellH = size.height / grid.size
            for (y in 0 until grid.size) {
                for (x in 0 until grid.size) {
                    if (grid[x, y]) {
                        drawRect(
                            color = when (reveal) {
                                TileReveal.CORRECT -> TileCorrect
                                TileReveal.WRONG -> TerminalAmber
                                TileReveal.NONE -> PixelOn
                            },
                            topLeft = Offset(x * cellW, y * cellH),
                            size = Size(cellW, cellH),
                        )
                    }
                }
            }
        }
    }
}
