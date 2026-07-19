package com.hashfactory.game.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hashfactory.game.ui.GameViewModel

@Composable
fun GameScreen(vm: GameViewModel) {
    val state by vm.state.collectAsStateWithLifecycle()
    val terminalLog by vm.terminalLog.collectAsStateWithLifecycle()
    val offlineReport by vm.offlineReport.collectAsStateWithLifecycle()

    var tab by rememberSaveable { mutableIntStateOf(0) }
    var showBurnDialog by rememberSaveable { mutableStateOf(false) }

    Scaffold { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            TabRow(selectedTabIndex = tab) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("TERMINAL") })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("UPGRADES") })
            }
            when (tab) {
                0 -> FactoryScreen(
                    state = state,
                    config = vm.gameConfig,
                    terminalLog = terminalLog,
                    onTap = vm::onTap,
                    onBurnRequest = { showBurnDialog = true },
                )
                1 -> UpgradesScreen(
                    state = state,
                    onBuy = vm::onBuy,
                )
            }
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
