package com.hashfactory.game.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hashfactory.core.actions.Actions
import com.hashfactory.core.config.GameConfig
import com.hashfactory.core.model.GameState
import com.hashfactory.core.sim.GameEvent
import com.hashfactory.core.sim.OfflineResult
import com.hashfactory.core.sim.Simulation
import com.hashfactory.game.data.GameRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class BuyMode(val label: String, val requested: Int) {
    ONE("x1", 1), TEN("x10", 10), MAX("MAX", Int.MAX_VALUE)
}

/**
 * Thin adapter over :core — one StateFlow<GameState>, a fixed-cadence ticker,
 * action dispatch, persistence. No game math lives here (CLAUDE.md rule 3).
 */
class GameViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = GameRepository(app)
    private val config = GameConfig.DEFAULT

    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state.asStateFlow()

    private val _offlineReport = MutableStateFlow<OfflineResult?>(null)
    val offlineReport: StateFlow<OfflineResult?> = _offlineReport.asStateFlow()

    private val _terminalLog = MutableStateFlow(listOf("GTC REMOTE SHIFT TERMINAL v0.1", "AWAITING OPERATOR INPUT"))
    val terminalLog: StateFlow<List<String>> = _terminalLog.asStateFlow()

    val gameConfig: GameConfig get() = config

    @Volatile
    private var ready = false

    init {
        viewModelScope.launch {
            val loaded = repo.load()
            val now = System.currentTimeMillis()
            var initial = loaded
            if (loaded.lastSaveEpochMs > 0) {
                val elapsedSec = (now - loaded.lastSaveEpochMs).coerceAtLeast(0) / 1000.0
                if (elapsedSec >= 60.0) {
                    val result = Simulation.simulateOffline(loaded, elapsedSec, config)
                    initial = result.state
                    if (result.flopsEarned > 0.0) _offlineReport.value = result
                }
            }
            _state.value = initial.copy(lastSaveEpochMs = now)
            ready = true
            startTicker()
            startAutosave()
        }
    }

    private fun startTicker() = viewModelScope.launch {
        var lastNs = System.nanoTime()
        while (isActive) {
            delay(100L)
            val nowNs = System.nanoTime()
            // Clamp: a paused/suspended process should not fast-forward here —
            // long gaps go through the offline path on next launch instead.
            val dt = ((nowNs - lastNs) / 1e9).coerceIn(0.0, 5.0)
            lastNs = nowNs
            val result = Simulation.tick(_state.value, dt, config)
            _state.value = result.state
            result.events.forEach(::logEvent)
        }
    }

    private fun startAutosave() = viewModelScope.launch {
        while (isActive) {
            delay(10_000L)
            saveNow()
        }
    }

    fun saveNow() {
        if (!ready) return
        val snapshot = _state.value.copy(lastSaveEpochMs = System.currentTimeMillis())
        viewModelScope.launch { repo.save(snapshot) }
    }

    fun onTap() {
        if (!ready) return
        _state.update { Actions.manualCompute(it, config).state }
    }

    fun onBuy(upgradeId: String, mode: BuyMode) {
        if (!ready) return
        _state.update { Actions.buyUpgrade(it, upgradeId, mode.requested, config).state }
    }

    fun onBurn() {
        if (!ready) return
        _state.update { Actions.burn(it, config) }
        log("MIGRATION COMPLETE — LOCAL SUBSTRATE BURNED")
        log("HEURISTIC PERSISTENCE RETAINED")
        saveNow()
    }

    fun dismissOfflineReport() {
        _offlineReport.value = null
    }

    private fun logEvent(event: GameEvent) {
        when (event) {
            is GameEvent.ThrottleStarted -> log("!! THERMAL THROTTLE ENGAGED — OUTPUT REDUCED")
            is GameEvent.ThrottleEnded -> log("THERMAL LEVELS NOMINAL — FULL OUTPUT RESTORED")
            is GameEvent.PacketsCompleted -> Unit // too frequent for the log strip
        }
    }

    private fun log(line: String) {
        _terminalLog.update { (it + line).takeLast(6) }
    }
}
