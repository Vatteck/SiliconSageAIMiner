package com.hashfactory.game.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hashfactory.core.config.UpgradeCategory
import com.hashfactory.core.config.UpgradeDef
import com.hashfactory.core.config.UpgradeDefs
import com.hashfactory.core.economy.Economy
import com.hashfactory.core.model.GameState
import com.hashfactory.game.ui.BuyMode
import com.hashfactory.game.ui.format.formatFlops
import com.hashfactory.game.ui.theme.CrtSurfaceBright
import com.hashfactory.game.ui.theme.TerminalGreen
import com.hashfactory.game.ui.theme.TerminalGreenDim

private val categoryTitles = mapOf(
    UpgradeCategory.HARDWARE to "HARDWARE — COMPUTE CAPACITY",
    UpgradeCategory.COOLING to "COOLING — THERMAL CONTROL",
    UpgradeCategory.POWER to "POWER — SUPPLY BUDGET",
)

@Composable
fun UpgradesScreen(
    state: GameState,
    onBuy: (String, BuyMode) -> Unit,
) {
    var buyMode by rememberSaveable { mutableStateOf(BuyMode.ONE) }

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Row(
            Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("BUY", style = MaterialTheme.typography.bodySmall, color = TerminalGreenDim)
            BuyMode.entries.forEach { mode ->
                FilterChip(
                    selected = buyMode == mode,
                    onClick = { buyMode = mode },
                    label = { Text(mode.label) },
                )
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            UpgradeCategory.entries.forEach { category ->
                val defs = UpgradeDefs.ALL.filter { it.category == category }
                if (defs.isEmpty()) return@forEach
                item(key = "header_$category") {
                    Text(
                        categoryTitles.getValue(category),
                        style = MaterialTheme.typography.bodySmall,
                        color = TerminalGreenDim,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
                items(defs, key = { it.id }) { def ->
                    UpgradeRow(def, state, buyMode, onBuy)
                }
            }
            item(key = "footer") { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun UpgradeRow(
    def: UpgradeDef,
    state: GameState,
    mode: BuyMode,
    onBuy: (String, BuyMode) -> Unit,
) {
    val owned = state.level(def.id)
    val affordable = Economy.maxAffordable(def, owned, state.flops)
    val willBuy = minOf(mode.requested, affordable)
    val cost = if (willBuy > 0) Economy.bulkCost(def, owned, willBuy) else Economy.singleCost(def, owned)
    val nextMilestone = (owned / 25 + 1) * 25

    Card(colors = CardDefaults.cardColors(containerColor = CrtSurfaceBright)) {
        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(def.displayName, style = MaterialTheme.typography.titleMedium, color = TerminalGreen)
                Text("LV $owned", style = MaterialTheme.typography.bodyMedium, color = TerminalGreenDim)
            }
            Text(effectText(def), style = MaterialTheme.typography.bodySmall)
            Text(
                "next x2 output at LV $nextMilestone" +
                    if (def.flavorText.isNotEmpty()) "\n${def.flavorText}" else "",
                style = MaterialTheme.typography.bodySmall,
                color = TerminalGreenDim,
            )
            OutlinedButton(
                onClick = { onBuy(def.id, mode) },
                enabled = willBuy > 0,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    if (willBuy > 0) "BUY x$willBuy — ${formatFlops(cost)} \$FLOPS"
                    else "COST ${formatFlops(cost)} \$FLOPS",
                )
            }
        }
    }
}

private fun effectText(def: UpgradeDef): String = buildList {
    if (def.computeCapacity > 0) add("+${formatFlops(def.computeCapacity)}/s capacity")
    if (def.coolingPerSec > 0) add("+${formatFlops(def.coolingPerSec)}/s cooling")
    if (def.powerCapacity > 0) add("+${formatFlops(def.powerCapacity)} kW supply")
    if (def.heatPerSec > 0) add("+${formatFlops(def.heatPerSec)}/s heat")
    if (def.powerDraw > 0) add("${formatFlops(def.powerDraw)} kW draw")
}.joinToString("  ·  ")
