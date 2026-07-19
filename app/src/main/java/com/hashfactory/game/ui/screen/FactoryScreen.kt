package com.hashfactory.game.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hashfactory.core.config.GameConfig
import com.hashfactory.core.economy.Economy
import com.hashfactory.core.model.GameState
import com.hashfactory.core.sim.Derived
import com.hashfactory.game.ui.format.formatFlops
import com.hashfactory.game.ui.theme.CrtSurface
import com.hashfactory.game.ui.theme.TerminalAmber
import com.hashfactory.game.ui.theme.TerminalGreen
import com.hashfactory.game.ui.theme.TerminalGreenDim
import com.hashfactory.game.ui.theme.TerminalRed

@Composable
fun FactoryScreen(
    state: GameState,
    config: GameConfig,
    terminalLog: List<String>,
    onTap: () -> Unit,
    onBurnRequest: () -> Unit,
) {
    val effectiveCapacity = Derived.effectiveCapacity(state, config)
    val powerDraw = Derived.powerDraw(state)
    val powerCapacity = Derived.powerCapacity(state, config)
    val throttle = Derived.heatThrottle(state.heat, config)
    val burnGain = Economy.prestigeGain(state.flopsThisRun)

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("GTC REMOTE COMPUTE // SHIFT ACTIVE", style = MaterialTheme.typography.bodySmall, color = TerminalGreenDim)

        // Wallet
        Column {
            Text("\$FLOPS BALANCE", style = MaterialTheme.typography.bodySmall, color = TerminalGreenDim)
            Text(formatFlops(state.flops), style = MaterialTheme.typography.displayMedium, color = TerminalGreen)
            Text(
                "ASSIGNED THROUGHPUT: ${formatFlops(effectiveCapacity)}/s" +
                    if (throttle < 1.0) "  [THROTTLED]" else "",
                style = MaterialTheme.typography.bodyMedium,
                color = if (throttle < 1.0) TerminalAmber else MaterialTheme.colorScheme.onBackground,
            )
        }

        // Assigned packet progress
        Column {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("ASSIGNED PACKET", style = MaterialTheme.typography.bodySmall, color = TerminalGreenDim)
                Text("COMPLETED: ${state.packetsCompleted}", style = MaterialTheme.typography.bodySmall, color = TerminalGreenDim)
            }
            Spacer(Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { state.packetProgress.toFloat().coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(10.dp),
                color = TerminalGreen,
                trackColor = CrtSurface,
            )
        }

        // Heat + power meters
        Meter(
            label = "HEAT",
            value = state.heat,
            max = config.maxHeat,
            barColor = when {
                state.heat > config.throttleStartHeat -> TerminalRed
                state.heat > 60.0 -> TerminalAmber
                else -> TerminalGreen
            },
            detail = if (throttle < 1.0) "THROTTLE %.0f%%".format(throttle * 100) else null,
        )
        Meter(
            label = "POWER",
            value = powerDraw,
            max = powerCapacity,
            barColor = if (powerDraw > powerCapacity) TerminalRed else TerminalGreen,
            detail = "%s / %s kW".format(formatFlops(powerDraw), formatFlops(powerCapacity)) +
                if (powerDraw > powerCapacity) "  [STARVED]" else "",
        )

        // Terminal log strip
        Column(
            Modifier
                .fillMaxWidth()
                .background(CrtSurface, RoundedCornerShape(4.dp))
                .padding(8.dp),
        ) {
            terminalLog.forEach { line ->
                Text("> $line", style = MaterialTheme.typography.bodySmall, color = TerminalGreenDim)
            }
        }

        Spacer(Modifier.height(4.dp))

        // Manual compute
        Button(
            onClick = onTap,
            modifier = Modifier.fillMaxWidth().height(72.dp),
            shape = RoundedCornerShape(6.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TerminalGreen),
        ) {
            Text("COMPUTE HASH", style = MaterialTheme.typography.headlineSmall)
        }

        // The Burn
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            OutlinedButton(
                onClick = onBurnRequest,
                enabled = burnGain >= config.minBurnGain,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    if (burnGain >= config.minBurnGain)
                        "INFRASTRUCTURE MIGRATION READY [+%.0f PERSISTENCE]".format(burnGain)
                    else
                        "MIGRATION LOCKED — EARN ${formatFlops(1e6)} \$FLOPS THIS SHIFT",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            if (state.burnCount > 0) {
                Text(
                    "PERSISTENCE: %.0f (x%.2f OUTPUT) // BURNS: %d"
                        .format(state.persistence, Economy.prestigeMultiplier(state.persistence, config), state.burnCount),
                    style = MaterialTheme.typography.bodySmall,
                    color = TerminalGreenDim,
                )
            }
        }
    }
}

@Composable
private fun Meter(label: String, value: Double, max: Double, barColor: androidx.compose.ui.graphics.Color, detail: String?) {
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = TerminalGreenDim)
            if (detail != null) Text(detail, style = MaterialTheme.typography.bodySmall, color = TerminalGreenDim)
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { if (max > 0.0) (value / max).toFloat().coerceIn(0f, 1f) else 0f },
            modifier = Modifier.fillMaxWidth().height(8.dp),
            color = barColor,
            trackColor = CrtSurface,
        )
    }
}
