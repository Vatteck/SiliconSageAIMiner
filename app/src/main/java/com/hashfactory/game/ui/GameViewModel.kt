package com.hashfactory.game.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hashfactory.core.actions.Actions
import com.hashfactory.core.config.GameConfig
import com.hashfactory.core.datamining.CardBatch
import com.hashfactory.core.datamining.CardValidator
import com.hashfactory.core.datamining.Dataset
import com.hashfactory.core.datamining.DatasetFactory
import com.hashfactory.core.datamining.scoreDataset
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

    private val _activeDataset = MutableStateFlow<Dataset?>(null)
    val activeDataset: StateFlow<Dataset?> = _activeDataset.asStateFlow()

    private val _datasetClicks = MutableStateFlow<Set<Int>>(emptySet())
    val datasetClicks: StateFlow<Set<Int>> = _datasetClicks.asStateFlow()

    /** Per-tile reveal state: NONE → CORRECT or WRONG after click, locked thereafter. */
    enum class TileReveal { NONE, CORRECT, WRONG }

    private val _tileStates = MutableStateFlow<List<TileReveal>>(emptyList())
    val tileStates: StateFlow<List<TileReveal>> = _tileStates.asStateFlow()

    private var datasetPage = 0
    private var datasetSeed = 0L

    private val _cardBatch = MutableStateFlow<CardBatch?>(null)
    val cardBatch: StateFlow<CardBatch?> = _cardBatch.asStateFlow()

    private val _cardIndex = MutableStateFlow(0)
    val cardIndex: StateFlow<Int> = _cardIndex.asStateFlow()

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

    fun onToggleOverclock() {
        if (!ready) return
        _state.update { Actions.toggleOverclock(it) }
        log(
            if (_state.value.overclocked) "!! OVERCLOCK ENGAGED — OUTPUT x2, THERMAL LOAD x2"
            else "OVERCLOCK DISENGAGED — STOCK CLOCKS RESTORED",
        )
    }

    fun onBurn() {
        if (!ready) return
        _state.update { Actions.burn(it, config) }
        log("MIGRATION COMPLETE — LOCAL SUBSTRATE BURNED")
        log("HEURISTIC PERSISTENCE RETAINED")
        saveNow()
    }

    fun onPurgeHeat() {
        if (!ready) return
        _state.update { old ->
            val (next, reduced) = Actions.purgeHeat(old, config)
            if (reduced > 0.0) log("HEAT DUMPED — %.0f° REDUCED AT COST OF ALL \$FLOPS".format(reduced))
            next
        }
    }

    fun dismissOfflineReport() {
        _offlineReport.value = null
    }

    // ── dataset mining ────────────────────────────────────────────────

    private fun generatePage(): Dataset {
        return DatasetFactory.generate(
            id = "ds-$datasetSeed",
            width = 4,
            height = 4,
            faceRatio = 0.5,
            cost = 0.0, // cost already paid on purchase
            rewardPerFace = 50.0,
            penaltyPerMistake = 25.0,
            baseSeed = datasetSeed + datasetPage * 1000,
        )
    }

    fun onPurchaseDataset() {
        if (!ready || _activeDataset.value != null) return
        val cost = 10.0
        val current = _state.value
        if (current.flops < cost) return
        _state.update { it.copy(flops = it.flops - cost) }
        datasetSeed = System.currentTimeMillis()
        datasetPage = 0
        val ds = generatePage()
        _activeDataset.value = ds
        _tileStates.value = ds.tiles.map { TileReveal.NONE }
        log("DATASET BUNDLE ACQUIRED — PAGE 1")
    }

    fun onToggleTile(index: Int) {
        val ds = _activeDataset.value ?: return
        if (index !in ds.tiles.indices) return
        val states = _tileStates.value
        if (states[index] != TileReveal.NONE) return // already revealed, locked

        val tile = ds.tiles[index]
        val newReveal = if (tile.isFace) TileReveal.CORRECT else TileReveal.WRONG
        val payout = if (tile.isFace) ds.rewardPerFace else -ds.penaltyPerMistake

        _state.update { it.copy(flops = (it.flops + payout).coerceAtLeast(0.0)) }
        _tileStates.update { it.toMutableList().also { list -> list[index] = newReveal } }
        _datasetClicks.update { it + index }

        if (tile.isFace) log("FACE CONFIRMED — +${"%.0f".format(ds.rewardPerFace)} \$FLOPS")
        else log("ANOMALY DETECTED — −${"%.0f".format(ds.penaltyPerMistake)} \$FLOPS")

        // Auto-advance when all faces on this page are found
        val updatedStates = _tileStates.value
        val allFacesFound = ds.tiles.indices.all { i ->
            if (ds.tiles[i].isFace) updatedStates[i] == TileReveal.CORRECT else true
        }
        if (allFacesFound) {
            datasetPage++
            val nextDs = generatePage()
            _activeDataset.value = nextDs
            _tileStates.value = nextDs.tiles.map { TileReveal.NONE }
            _datasetClicks.value = emptySet()
            log("ALL FACES FOUND — PAGE ${datasetPage + 1} LOADED")
        }
    }

    fun onNextPage() {
        val ds = _activeDataset.value ?: return
        // Only allow next page when all faces on current page are found
        val allFacesFound = ds.tiles.indices.all { i ->
            if (ds.tiles[i].isFace) _tileStates.value[i] == TileReveal.CORRECT else true
        }
        if (!allFacesFound) return

        datasetPage++
        val nextDs = generatePage()
        _activeDataset.value = nextDs
        _tileStates.value = nextDs.tiles.map { TileReveal.NONE }
        _datasetClicks.value = emptySet()
        log("PAGE ${datasetPage + 1} LOADED")
    }

    fun onSubmitDataset() {
        val ds = _activeDataset.value ?: return
        val facesFound = ds.tiles.indices.count { i -> ds.tiles[i].isFace && _tileStates.value[i] == TileReveal.CORRECT }
        val mistakes = ds.tiles.indices.count { i -> !ds.tiles[i].isFace && _tileStates.value[i] == TileReveal.WRONG }
        val totalFaces = ds.tiles.count { it.isFace }
        log("BUNDLE COMPLETE — $datasetPage PAGE(S), $facesFound/$totalFaces FACES FOUND ON FINAL PAGE")
        _activeDataset.value = null
        _tileStates.value = emptyList()
        _datasetClicks.value = emptySet()
    }

    fun onCancelDataset() {
        if (_activeDataset.value == null) return
        _activeDataset.value = null
        _tileStates.value = emptyList()
        _datasetClicks.value = emptySet()
        log("DATASET ABANDONED")
    }

    // ── card validation ────────────────────────────────────────────────

    fun onPurchaseCardBatch() {
        if (!ready || _cardBatch.value != null) return
        val cost = 15.0
        val current = _state.value
        if (current.flops < cost) return
        _state.update { it.copy(flops = it.flops - cost) }
        val batch = CardValidator.generateBatch(size = 12, cost = cost)
        _cardBatch.value = batch
        _cardIndex.value = 0
        log("DATA BATCH LOADED — ${batch.profiles.size} RECORDS FOR VALIDATION")
    }

    fun onSwipeCard(approved: Boolean) {
        val batch = _cardBatch.value ?: return
        val idx = _cardIndex.value
        if (idx >= batch.profiles.size) return
        val profile = batch.profiles[idx]
        val correct = approved == profile.isValid
        val payout = if (correct) batch.rewardPerCorrect else -batch.penaltyPerWrong

        _state.update { it.copy(flops = (it.flops + payout).coerceAtLeast(0.0)) }
        _cardIndex.value = idx + 1

        if (correct) {
            val action = if (approved) "APPROVED" else "FLAGGED"
            log("$action — CORRECT +${"%.0f".format(batch.rewardPerCorrect)} \$FLOPS")
        } else {
            val reason = profile.flagReason ?: "UNKNOWN"
            log("WRONG — $reason · −${"%.0f".format(batch.penaltyPerWrong)} \$FLOPS")
        }
    }

    fun onCashOutCards() {
        if (_cardBatch.value == null) return
        val total = _cardIndex.value
        log("VALIDATION COMPLETE — $total RECORDS PROCESSED")
        _cardBatch.value = null
        _cardIndex.value = 0
    }

    fun onAbandonCards() {
        if (_cardBatch.value == null) return
        _cardBatch.value = null
        _cardIndex.value = 0
        log("VALIDATION ABANDONED")
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
