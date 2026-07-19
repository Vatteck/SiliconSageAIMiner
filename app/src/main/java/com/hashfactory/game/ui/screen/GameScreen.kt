package com.hashfactory.game.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hashfactory.game.ui.GameViewModel

@Composable
fun GameScreen(vm: GameViewModel) {
    val state by vm.state.collectAsStateWithLifecycle()
    val terminalLog by vm.terminalLog.collectAsStateWithLifecycle()
    val offlineReport by vm.offlineReport.collectAsStateWithLifecycle()
    val dataset by vm.activeDataset.collectAsStateWithLifecycle()
    val tileStates by vm.tileStates.collectAsStateWithLifecycle()
    val cardBatch by vm.cardBatch.collectAsStateWithLifecycle()
    val cardIndex by vm.cardIndex.collectAsStateWithLifecycle()

    var tab by rememberSaveable { mutableIntStateOf(0) }
    var showBurnDialog by rememberSaveable { mutableStateOf(false) }

    Scaffold { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            // Tabs
            TabRow(selectedTabIndex = tab) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("TERMINAL") })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("UPGRADES") })
                Tab(selected = tab == 2, onClick = { tab = 2 }, text = { Text("VALIDATE") })
                Tab(selected = tab == 3, onClick = { tab = 3 }, text = { Text("DATASETS") })
            }

            // Persistent stats strip
            StatsStrip(state = state, config = vm.gameConfig)

            // Content area — changes per tab
            Column(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (tab) {
                    0 -> FactoryScreen(
                        state = state,
                        config = vm.gameConfig,
                        onTap = vm::onTap,
                        onToggleOverclock = vm::onToggleOverclock,
                        onPurgeHeat = vm::onPurgeHeat,
                        onBurnRequest = { showBurnDialog = true },
                    )
                    1 -> UpgradesScreen(
                        state = state,
                        config = vm.gameConfig,
                        onBuy = vm::onBuy,
                    )
                    2 -> CardValidatorScreen(
                        batch = cardBatch,
                        currentIndex = cardIndex,
                        flops = state.flops,
                        onPurchase = vm::onPurchaseCardBatch,
                        onSwipe = vm::onSwipeCard,
                        onCashOut = vm::onCashOutCards,
                        onAbandon = vm::onAbandonCards,
                    )
                    3 -> DatasetScreen(
                        dataset = dataset,
                        tileStates = tileStates,
                        flops = state.flops,
                        onPurchase = vm::onPurchaseDataset,
                        onToggleTile = vm::onToggleTile,
                        onNextPage = vm::onNextPage,
                        onSubmit = vm::onSubmitDataset,
                        onCancel = vm::onCancelDataset,
                    )
                }
            }

            // Persistent log strip at bottom
            LogStrip(log = terminalLog)
        }
    }

    offlineReport?.let { report ->
        OfflineReportDialog(report = report, onDismiss = vm::dismissOfflineReport)
    }
    if (showBurnDialog) {
        BurnDialog(
            state = state,
            config = vm.gameConfig,
            onConfirm = {
                vm.onBurn()
                showBurnDialog = false
            },
            onDismiss = { showBurnDialog = false },
        )
    }
}
