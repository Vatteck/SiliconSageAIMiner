package com.hashfactory.game.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.hashfactory.core.config.GameConfig
import com.hashfactory.core.model.GameState
import com.hashfactory.core.sim.Derived
import com.hashfactory.game.ui.format.formatFlops
import com.hashfactory.game.ui.theme.CrtSurface
import com.hashfactory.game.ui.theme.TerminalAmber
import com.hashfactory.game.ui.theme.TerminalGreen
import com.hashfactory.game.ui.theme.TerminalGreenDim
import com.hashfactory.game.ui.theme.TerminalRed

/**
 * Persistent status strip — always visible below tabs.
 * Shows $FLOPS, heat with rate, and power status in one compact row.
 */
@Composable
fun StatsStrip(state: GameState, config: GameConfig) {
    val heat = state.heat
    val throttle = Derived.heatThrottle(heat, config)
    val netHeatRate = Derived.heatGeneration(state, config) * Derived.powerFactor(state, config) * throttle -
        Derived.cooling(state, config)
    val powerDraw = Derived.powerDraw(state)
    val powerCapacity = Derived.powerCapacity(state, config)
    val powerFactor = Derived.powerFactor(state, config)
    val heatArrow = if (netHeatRate >= 0) "↗" else "↘"
    val heatColor = when {
        throttle < 1.0 -> TerminalRed
        heat > 60 -> TerminalAmber
        else -> TerminalGreenDim
    }
    val pwrColor = if (powerFactor < 0.5) TerminalRed else TerminalGreenDim

    Row(
        Modifier
            .fillMaxWidth()
            .background(CrtSurface, RoundedCornerShape(4.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            "${formatFlops(state.flops)} \$FLOPS",
            style = MaterialTheme.typography.bodySmall,
            color = TerminalGreen,
            maxLines = 1,
        )
        Text(
            "HEAT ${"%.0f".format(heat)}° $heatArrow${"%.0f".format(kotlin.math.abs(netHeatRate))}/s" +
                (if (state.overclocked) " [OC]" else ""),
            style = MaterialTheme.typography.bodySmall,
            color = heatColor,
            maxLines = 1,
        )
        Text(
            "PWR ${formatFlops(powerDraw)}/${formatFlops(powerCapacity)}kW" +
                (if (powerFactor < 0.5) " ⚡" else ""),
            style = MaterialTheme.typography.bodySmall,
            color = pwrColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * Persistent log strip — always visible at the bottom.
 * Shows the last few terminal log lines.
 */
@Composable
fun LogStrip(log: List<String>) {
    Column(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .background(CrtSurface, RoundedCornerShape(4.dp))
            .padding(6.dp),
        verticalArrangement = Arrangement.spacedBy(1.dp),
    ) {
        log.forEach { line ->
            Text(
                "> $line",
                style = MaterialTheme.typography.bodySmall,
                color = TerminalGreenDim,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
