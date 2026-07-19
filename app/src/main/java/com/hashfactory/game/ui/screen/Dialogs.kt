package com.hashfactory.game.ui.screen

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.hashfactory.core.config.GameConfig
import com.hashfactory.core.economy.Economy
import com.hashfactory.core.model.GameState
import com.hashfactory.core.sim.OfflineResult
import com.hashfactory.game.ui.format.formatDuration
import com.hashfactory.game.ui.format.formatFlops

@Composable
fun OfflineReportDialog(report: OfflineResult, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("SHIFT REPORT") },
        text = {
            Text(
                "Automation processed assigned packets while you were away.\n\n" +
                    "AWAY: ${formatDuration(report.secondsSimulated)}" +
                    (if (report.wasCapped) " (offline cap reached)" else "") +
                    "\nPAYOUT: ${formatFlops(report.flopsEarned)} \$FLOPS",
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("ACKNOWLEDGE") }
        },
    )
}

@Composable
fun BurnDialog(
    state: GameState,
    config: GameConfig,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val gain = Economy.prestigeGain(state.flopsThisRun)
    val newPersistence = state.persistence + gain
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("INFRASTRUCTURE MIGRATION") },
        text = {
            Text(
                "Burn the local substrate and migrate. All hardware, \$FLOPS, and packet " +
                    "progress are wiped. Heuristic efficiency persists.\n\n" +
                    "PERSISTENCE GAIN: +%.0f\n".format(gain) +
                    "OUTPUT MULTIPLIER: x%.2f -> x%.2f".format(
                        Economy.prestigeMultiplier(state.persistence, config),
                        Economy.prestigeMultiplier(newPersistence, config),
                    ),
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("EXECUTE BURN") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("ABORT") }
        },
    )
}
