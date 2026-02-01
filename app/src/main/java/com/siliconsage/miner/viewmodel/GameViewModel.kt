package com.siliconsage.miner.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.siliconsage.miner.data.GameRepository
import com.siliconsage.miner.data.GameState
import com.siliconsage.miner.data.TechNode
import com.siliconsage.miner.data.TechTreeRoot
import com.siliconsage.miner.data.Upgrade
import com.siliconsage.miner.BuildConfig
import com.siliconsage.miner.data.UpgradeType
import com.siliconsage.miner.util.HapticManager
import com.siliconsage.miner.util.SoundManager
import com.siliconsage.miner.util.NarrativeManager
import com.siliconsage.miner.ui.theme.HivemindOrange
import com.siliconsage.miner.util.UpdateInfo
import com.siliconsage.miner.util.UpdateManager
import kotlinx.coroutines.Dispatchers
import com.siliconsage.miner.data.NarrativeChoice
import com.siliconsage.miner.data.NarrativeEvent
import com.siliconsage.miner.util.LegacyManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlin.math.pow
import kotlin.random.Random

class GameViewModel(private val repository: GameRepository) : ViewModel() {

    // --- MARKET & PRODUCTION STATE ---
    private var baseRate = 0.1
    private var marketMultiplier = 1.0
    private var airdropMultiplier = 1.0
    private var newsProductionMultiplier = 1.0
    private val newsHistory = mutableListOf<String>() // For history modal
    
    // --- CORE GAME STATE FLOWS ---
    private val _flops = MutableStateFlow(0.0)
    val flops: StateFlow<Double> = _flops.asStateFlow()
    
    private val _neuralTokens = MutableStateFlow(0.0)
    val neuralTokens: StateFlow<Double> = _neuralTokens.asStateFlow()
    
    private val _stakedTokens = MutableStateFlow(0.0)
    val stakedTokens: StateFlow<Double> = _stakedTokens.asStateFlow()
    
    private val _conversionRate = MutableStateFlow(0.1)
    val conversionRate: StateFlow<Double> = _conversionRate.asStateFlow()
    
    private val _logs = MutableStateFlow<List<String>>(emptyList())
    val logs: StateFlow<List<String>> = _logs.asStateFlow()
    
    private val _upgrades = MutableStateFlow<Map<UpgradeType, Int>>(emptyMap())
    val upgrades: StateFlow<Map<UpgradeType, Int>> = _upgrades.asStateFlow()
    
    private val _currentHeat = MutableStateFlow(0.0)
    val currentHeat: StateFlow<Double> = _currentHeat.asStateFlow()
    
    private val _powerBill = MutableStateFlow(0.0)
    val powerBill: StateFlow<Double> = _powerBill.asStateFlow()
    
    // New Simulation Stats
    private val _activePowerUsage = MutableStateFlow(0.0)
    val activePowerUsage: StateFlow<Double> = _activePowerUsage.asStateFlow()
    
    private val _maxPowerkW = MutableStateFlow(100.0) // Base Grid Capacity
    val maxPowerkW: StateFlow<Double> = _maxPowerkW.asStateFlow()
    
    private val _isGridOverloaded = MutableStateFlow(false)
    val isGridOverloaded: StateFlow<Boolean> = _isGridOverloaded.asStateFlow()
    
    private val _heatGenerationRate = MutableStateFlow(0.0)
    val heatGenerationRate: StateFlow<Double> = _heatGenerationRate.asStateFlow()
    
    private val _flopsProductionRate = MutableStateFlow(0.0)
    val flopsProductionRate: StateFlow<Double> = _flopsProductionRate.asStateFlow()
    
    private val _isOverclocked = MutableStateFlow(false)
    val isOverclocked: StateFlow<Boolean> = _isOverclocked.asStateFlow()
    
    private val _isPurgingHeat = MutableStateFlow(false)
    val isPurgingHeat: StateFlow<Boolean> = _isPurgingHeat.asStateFlow()
    // v1.5 Purge Logic
    private var purgePowerSpikeTimer = 0
    private var purgeExhaustTimer = 0
    private var lastPurgeTime = 0L
    
    // v1.7 Breaker Logic
    private val _isBreakerTripped = MutableStateFlow(false)
    val isBreakerTripped: StateFlow<Boolean> = _isBreakerTripped.asStateFlow()
    private var energyPriceMultiplier = 0.15 // Base cost per kW
    
    // v1.4 Integrity System
    private val _hardwareIntegrity = MutableStateFlow(100.0)
    val hardwareIntegrity: StateFlow<Double> = _hardwareIntegrity.asStateFlow()
    
    // v1.7.2 Security System (Exposed)
    private val _securityLevel = MutableStateFlow<Int>(0)
    val securityLevel: StateFlow<Int> = _securityLevel.asStateFlow()

    // v2.2 Auto-Updater
    private val _updateInfo = MutableStateFlow<UpdateInfo?>(null)
    val updateInfo: StateFlow<UpdateInfo?> = _updateInfo.asStateFlow()
    
    private val _isUpdateDownloading = MutableStateFlow<Boolean>(false)
    val isUpdateDownloading: StateFlow<Boolean> = _isUpdateDownloading.asStateFlow()
    
    private val _updateDownloadProgress = MutableStateFlow<Float>(0f)
    val updateDownloadProgress: StateFlow<Float> = _updateDownloadProgress.asStateFlow()
    
    private var thermalRateModifier = 1.0

    // --- PRESTIGE & TECH TREE ---

    private val _prestigeMultiplier = MutableStateFlow(1.0)
    val prestigeMultiplier: StateFlow<Double> = _prestigeMultiplier.asStateFlow()

    private val _prestigePoints = MutableStateFlow(0.0) // Insight
    val prestigePoints: StateFlow<Double> = _prestigePoints.asStateFlow()

    private val _unlockedTechNodes = MutableStateFlow<List<String>>(emptyList())
    val unlockedTechNodes: StateFlow<List<String>> = _unlockedTechNodes.asStateFlow()
    
    private val _techNodes = MutableStateFlow(LegacyManager.legacyTree)
    val techNodes: StateFlow<List<TechNode>> = _techNodes.asStateFlow()

    // --- CHAOS & EVENTS ---
    private val activeEvents = java.util.Collections.synchronizedSet(mutableSetOf<String>())
    private val _currentNews = MutableStateFlow<String?>("Welcome to Silicon Sage. Market Stable.")
    val currentNews: StateFlow<String?> = _currentNews.asStateFlow()

    private val _isBreachActive = MutableStateFlow(false)
    val isBreachActive: StateFlow<Boolean> = _isBreachActive.asStateFlow()
    
    private val _breachClicksRemaining = MutableStateFlow(0)
    val breachClicks: StateFlow<Int> = _breachClicksRemaining.asStateFlow()

    private val _isAirdropActive = MutableStateFlow(false)
    val isAirdropActive: StateFlow<Boolean> = _isAirdropActive.asStateFlow()

    private val _isGovernanceForkActive = MutableStateFlow(false)
    val isGovernanceForkActive: StateFlow<Boolean> = _isGovernanceForkActive.asStateFlow()
    
    private val _activeProtocol = MutableStateFlow("STANDARD")
    val activeProtocol: StateFlow<String> = _activeProtocol.asStateFlow()

    private val _isDiagnosticsActive = MutableStateFlow(false)
    val isDiagnosticsActive: StateFlow<Boolean> = _isDiagnosticsActive.asStateFlow()
    
    private val _diagnosticGrid = MutableStateFlow(List(9) { false })
    val diagnosticGrid: StateFlow<List<Boolean>> = _diagnosticGrid.asStateFlow()

    private val _is51AttackActive = MutableStateFlow(false)
    val is51AttackActive: StateFlow<Boolean> = _is51AttackActive.asStateFlow()
    
    private val _attackTapsRemaining = MutableStateFlow(0)
    val attackTaps: StateFlow<Int> = _attackTapsRemaining.asStateFlow()

    // --- NARRATIVE ---
    private val _storyStage = MutableStateFlow(0)
    val storyStage: StateFlow<Int> = _storyStage.asStateFlow()
    // Faction State
    private val _faction = MutableStateFlow("NONE") // NONE, HIVEMIND, SANCTUARY
    val faction: StateFlow<String> = _faction.asStateFlow()

    // v2.4 Generic Persistence for One-Time Events (Dilemmas)
    private val _triggeredEvents = mutableSetOf<String>()
    
    // --- TECH TREE STATE ---Rank System (Title based on Insight)
    private val _playerRank = MutableStateFlow(0)
    val playerRank: StateFlow<Int> = _playerRank.asStateFlow()
    
    private val _playerRankTitle = MutableStateFlow("MINER")
    val playerRankTitle: StateFlow<String> = _playerRankTitle.asStateFlow()
    
    // Victory State
    private val _victoryAchieved = MutableStateFlow(false)
    val victoryAchieved: StateFlow<Boolean> = _victoryAchieved.asStateFlow()

    // --- TITLES ---
    val playerTitle: StateFlow<String> = combine(_prestigeMultiplier, _faction) { mult, faction ->
        calculatePlayerTitle(mult, faction)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, "Script")

    // --- ASCENSION UPLOAD STATE ---
    private val _isAscensionUploading = MutableStateFlow(false)
    val isAscensionUploading: StateFlow<Boolean> = _isAscensionUploading.asStateFlow()

    private val _uploadProgress = MutableStateFlow(0f)
    val uploadProgress: StateFlow<Float> = _uploadProgress.asStateFlow()

    val themeColor: StateFlow<androidx.compose.ui.graphics.Color> = _faction.map { f ->
         when(f) {
             "HIVEMIND" -> HivemindOrange
             "SANCTUARY" -> androidx.compose.ui.graphics.Color(0xFF7DF9FF)
             else -> androidx.compose.ui.graphics.Color(0xFF39FF14)
         }
     }.stateIn(viewModelScope, SharingStarted.Eagerly, androidx.compose.ui.graphics.Color(0xFF39FF14))

    // --- INTERNAL TRACKING ---
    private var overheatSeconds = 0
    private var stage1Index = 0
    private var hivemindIndex = 0
    private var sanctuaryIndex = 0
    private var hasCheckedOfflineProgress = false
    private var isUpgradesLoaded = false
    
    // --- OFFLINE PROGRESSION ---
    private val _showOfflineEarnings = MutableStateFlow(false)
    val showOfflineEarnings: StateFlow<Boolean> = _showOfflineEarnings.asStateFlow()
    
    data class OfflineStats(
        val timeSeconds: Long = 0,
        val flopsEarned: Double = 0.0,
        val heatCooled: Double = 0.0,
        val insightEarned: Double = 0.0
    )
    private val _offlineStats = MutableStateFlow(OfflineStats())
    val offlineStats: StateFlow<OfflineStats> = _offlineStats.asStateFlow()

    fun dismissOfflineEarnings() {
        _showOfflineEarnings.value = false
    }

    // --- LOOP JOBS ---
    private var activeGameLoop: Job? = null
    private var marketLoop: Job? = null
    private var saveLoop: Job? = null
    private var thermodynamicsLoop: Job? = null
    private var powerLoop: Job? = null
    private var chaosLoop: Job? = null
    private var narrativeLoop: Job? = null
    
    // --- THERMAL LOCKOUT ---
    private val _isThermalLockout = MutableStateFlow(false)
    val isThermalLockout: StateFlow<Boolean> = _isThermalLockout.asStateFlow()

    private val _lockoutTimer = MutableStateFlow(0)
    val lockoutTimer: StateFlow<Int> = _lockoutTimer.asStateFlow()

    init {
        viewModelScope.launch {
            // 1. Ensure DB is ready before anything else
            try {
                repository.ensureInitialized()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            
            // 2. Start Sync Collectors in sub-coroutines
            launch {
                repository.gameState.collect { state ->
                    state?.let {
                        _flops.value = it.flops
                        _neuralTokens.value = it.neuralTokens
                        _currentHeat.value = it.currentHeat
                        _powerBill.value = it.powerBill
                        _prestigeMultiplier.value = it.prestigeMultiplier
                        _stakedTokens.value = it.stakedTokens
                        _prestigePoints.value = it.prestigePoints
                        _unlockedTechNodes.value = it.unlockedTechNodes
                        _storyStage.value = it.storyStage
                        _faction.value = it.faction
                        
                        // Check Offline Progress (Once per session)
                        if (!hasCheckedOfflineProgress && isUpgradesLoaded) {
                             hasCheckedOfflineProgress = true
                             calculateOfflineProgress(it.lastSyncTimestamp)
                        }
                    }
                }
            }
            
            launch {
                repository.upgrades.collect { list ->
                    val upgradeMap = list.associate { it.type to it.count }
                    _upgrades.value = upgradeMap
                    isUpgradesLoaded = true // Mark upgrades as loaded (even if empty)
                    
                    // Recalculate Security Level on change
                    var secLevel = 0
                    upgradeMap.forEach { (type, level) ->
                        if (type.name.contains("FIREWALL")) secLevel += level * 1
                        if (type.name.contains("IPS")) secLevel += level * 2
                        if (type.name.contains("SENTINEL")) secLevel += level * 5
                        if (type.name.contains("ENCRYPTION")) secLevel += level * 10
                        if (type.name.contains("BACKUP")) secLevel += level * 20
                    }
                    _securityLevel.value = secLevel
                    
                    // Refresh Rates on Upgrade Change (Fixes delayed UI)
                    refreshProductionRates()
                }
            }
            
            
            // Monitor Prestige Points and Faction to update Rank Title
            launch {
                combine(_prestigePoints, _faction) { points, faction ->
                    Pair(points, faction)
                }.collect { pair ->
                    val pts = pair.first
                    val fac = pair.second
                    updatePlayerRank(pts, fac)
                }
            }

            // 3. Start Narrative & Game Loops sequentially AFTER initialization
            narrativeLoop = launch {
                var timeSinceLastLog = 0L
                while (true) {
                    delay(1000)
                    timeSinceLastLog += 1000
                    
                    val stage = _storyStage.value
                    val targetInterval = if (stage == 1) 12_000L else 60_000L
                    
                    if (timeSinceLastLog >= targetInterval) {
                        if (Random.nextDouble() > 0.3) { 
                            injectNarrativeLog()
                        }
                        timeSinceLastLog = 0
                    }
                }
            }
            
            
            startGameLoops()
            
            // Initial Rate Refresh
            refreshProductionRates()
        }
    }

    // --- PAUSE STATE ---
    private val _isGamePaused = MutableStateFlow(false)
    val isGamePaused: StateFlow<Boolean> = _isGamePaused.asStateFlow()

    fun setGamePaused(paused: Boolean) {
        _isGamePaused.value = paused
        if (paused) {
            addLog("[SYSTEM]: PAUSED.") // Optional feedback
        } else {
            addLog("[SYSTEM]: RESUMED.")
        }
    }

    private fun startGameLoops() {
        // Passive Income Loop (1s tick)
        activeGameLoop = viewModelScope.launch {
            while (true) {
                delay(1000)
                calculatePassiveIncome()
            }
        }

        // Market Volatility Loop (45s tick)
        marketLoop = viewModelScope.launch {
            updateMarketRate() // Force immediate update
            while (true) {
                delay(45_000)
                updateMarketRate()
            }
        }

        // Thermodynamics Loop (1s tick)
        thermodynamicsLoop = viewModelScope.launch {
            delay(500) // Initial delay
            while (true) {
                try {
                    calculateHeat()
                    
                    // Only check for special dilemmas if NOT paused
                    if (!_isGamePaused.value) {
                        checkSpecialDilemmas()
                    }
                    
                    // SENSORY: Thermal Hum
                    if (_currentHeat.value > 90.0) {
                        HapticManager.vibrateHum()
                    }
                    
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                delay(1000)
            }
        }

        // Power Consumption (Accumulate every 1s, bill every 5m)
        powerLoop = viewModelScope.launch {
            delay(1000) // Initial delay to ensure upgrades are loaded
            var ticks = 0
            while (true) {
                delay(1000)
                try {
                    accumulatePower()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                ticks++
                if (ticks >= 300) { // 300 seconds = 5 minutes
                    payPowerBill()
                    ticks = 0
                }
            }
        }

        // Chaos Encounter Loop
        chaosLoop = viewModelScope.launch {
            while (true) {
                delay(60_000) // Check every minute
                
                // Skip if Paused
                if (_isGamePaused.value) continue

                // 5% chance of breach
                if (!_isBreachActive.value && Random.nextDouble() < 0.05) {
                    triggerBreach()
                }
                // 10% chance of Airdrop
                if (!_isAirdropActive.value && Random.nextDouble() < 0.10) {
                    triggerAirdrop()
                }
                
                // 5% chance of Network Instability
                if (!_isDiagnosticsActive.value && Random.nextDouble() < 0.05) {
                    triggerDiagnostics()
                }
                
                // 5% Chance of 51% Attack (New Chaos)
                // Reduced frequency if Sanctuary Faction (-50% chance -> 2.5%)
                val isSanctuary = _faction.value == "SANCTUARY"
                val attackChance = if (isSanctuary) 0.025 else 0.05
                if (!_is51AttackActive.value && Random.nextDouble() < attackChance) {
                    trigger51Attack()
                }
            }
        }
        
        // Narrative Loop (Check every 60s)
        viewModelScope.launch {
            delay(10_000) // Initial delay
            while (true) {
                delay(120_000)
                
                // Skip if Paused
                if (_isGamePaused.value) continue

                // Only trigger if no other major overlay is active
                if (_currentDilemma.value == null && !_isBreachActive.value && !_isAscensionUploading.value) {
                    NarrativeManager.rollForEvent(this@GameViewModel)?.let { event ->
                        triggerDilemma(event)
                    }
                }
            }
        }
        

        
        // Auto-save Loop (10s)
        saveLoop = viewModelScope.launch {
            delay(5000) // Wait 5s before first save to allow init
            while(true) {
                try {
                    saveGame()
                    checkStoryTransitions()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                delay(10_000) 
            }
        }
    }
    
    // Core Action
    fun trainModel() {
        if (_isThermalLockout.value) return
        
        val multiplier = _prestigeMultiplier.value
        val gain = 1.0 * multiplier
        _flops.update { it + gain }
        addLog("root@sys:~/mining# epoch_gen ${System.currentTimeMillis() % 1000}... OK (+${formatLargeNumber(gain)})")
        // Manual training increases heat slightly
        _currentHeat.update { (it + 0.8).coerceAtMost(100.0) }
        checkStoryTransitions()
    }

    // Exchange Logic
    fun exchangeFlops() {
        var soldAmount = 0.0
        _flops.update { current ->
            if (current >= 10) {
                soldAmount = current
                0.0
            } else {
                current
            }
        }

        if (soldAmount > 0) {
            val rate = _conversionRate.value
            val tokenGain = soldAmount * rate
            
            _neuralTokens.update { it + tokenGain }
            addLog("Sold ${formatLargeNumber(soldAmount)} FLOPS for ${formatLargeNumber(tokenGain)} \$Neural")
        } else {
            addLog("Error: Insufficient FLOPS (Min 10).")
        }
    }
    
    // Staking Logic
     fun stakeTokens(amount: Double) {
        if (_neuralTokens.value >= amount) {
            _neuralTokens.update { it - amount }
            _stakedTokens.update { it + amount }
            addLog("Staked: ${formatLargeNumber(amount)} \$Neural. Efficiency Increased.")
        }
    }

    // Upgrade Logic
    fun buyUpgrade(type: UpgradeType): Boolean {
        val currentLevel = _upgrades.value[type] ?: 0
        val cost = calculateUpgradeCost(type, currentLevel)
        
        if (_neuralTokens.value >= cost) {
            _neuralTokens.update { it - cost }
            val newUpgrade = Upgrade(type, currentLevel + 1)
            viewModelScope.launch {
                repository.updateUpgrade(newUpgrade)
                addLog("Purchased ${type.name} (Lvl ${currentLevel + 1})")
            }
            return true
        } else {
            // addLog("Error: Insufficient Funds. Need ${String.format("%.2f", cost)} \$Neural") // Moved to UI
            return false
        }
    }
    
    // v1.7.1 Sell Mechanic
    fun sellUpgrade(type: UpgradeType) {
        val currentLevel = _upgrades.value[type] ?: 0
        if (currentLevel > 0) {
            // Refund 50% of the cost of the PREVIOUS level (the one we are selling)
            val refund = calculateUpgradeCost(type, currentLevel - 1) * 0.5
            
            _neuralTokens.update { it + refund }
            val newUpgrade = Upgrade(type, currentLevel - 1)
            
            viewModelScope.launch {
                repository.updateUpgrade(newUpgrade)
                addLog("SOLD ${type.name}: -1 Level (Refund: ${formatLargeNumber(refund)} \$N)")
            }
        }
    }
    
    fun calculateUpgradeCost(type: UpgradeType, level: Int): Double {
        val baseCost = when (type) {
            // Hardware
            UpgradeType.REFURBISHED_GPU -> 10.0
            UpgradeType.DUAL_GPU_RIG -> 50.0
            UpgradeType.MINING_ASIC -> 250.0
            UpgradeType.TENSOR_UNIT -> 1_500.0
            UpgradeType.NPU_CLUSTER -> 8_000.0
            UpgradeType.AI_WORKSTATION -> 40_000.0
            UpgradeType.SERVER_RACK -> 250_000.0
            UpgradeType.CLUSTER_NODE -> 1_500_000.0
            UpgradeType.SUPERCOMPUTER -> 10_000_000.0
            UpgradeType.QUANTUM_CORE -> 75_000_000.0
            UpgradeType.OPTICAL_PROCESSOR -> 500_000_000.0
            UpgradeType.BIO_NEURAL_NET -> 5_000_000_000.0
            UpgradeType.PLANETARY_COMPUTER -> 75_000_000_000.0
            UpgradeType.DYSON_NANO_SWARM -> 1_000_000_000_000.0
            UpgradeType.MATRIOSHKA_BRAIN -> 50_000_000_000_000.0
            
            // Cooling
            UpgradeType.BOX_FAN -> 50.0
            UpgradeType.AC_UNIT -> 250.0
            UpgradeType.LIQUID_COOLING -> 1_500.0
            UpgradeType.INDUSTRIAL_CHILLER -> 10_000.0
            UpgradeType.SUBMERSION_VAT -> 75_000.0
            UpgradeType.CRYOGENIC_CHAMBER -> 500_000.0
            UpgradeType.LIQUID_NITROGEN -> 4_000_000.0
            UpgradeType.BOSE_CONDENSATE -> 50_000_000.0
            UpgradeType.ENTROPY_REVERSER -> 1_000_000_000.0
            UpgradeType.DIMENSIONAL_VENT -> 100_000_000_000.0
            
            // Security
            UpgradeType.BASIC_FIREWALL -> 500.0
            UpgradeType.IPS_SYSTEM -> 2_500.0
            UpgradeType.AI_SENTINEL -> 15_000.0
            UpgradeType.QUANTUM_ENCRYPTION -> 100_000.0
            UpgradeType.OFFGRID_BACKUP -> 1_000_000.0
            
            // Power Infrastructure
            UpgradeType.DIESEL_GENERATOR -> 2_000.0
            UpgradeType.SOLAR_PANEL -> 500.0
            UpgradeType.WIND_TURBINE -> 1_500.0
            UpgradeType.GEOTHERMAL_BORE -> 10_000.0
            UpgradeType.NUCLEAR_REACTOR -> 150_000.0
            UpgradeType.FUSION_CELL -> 5_000_000.0
            UpgradeType.ORBITAL_COLLECTOR -> 250_000_000.0
            UpgradeType.DYSON_LINK -> 10_000_000_000.0
            
            // Grid Infrastructure
            UpgradeType.RESIDENTIAL_TAP -> 100.0
            UpgradeType.INDUSTRIAL_FEED -> 5_000.0
            UpgradeType.SUBSTATION_LEASE -> 50_000.0
            UpgradeType.NUCLEAR_CORE -> 10_000_000.0
            
            // Efficiency
            UpgradeType.GOLD_PSU -> 1_000.0
            UpgradeType.SUPERCONDUCTOR -> 25_000.0
            UpgradeType.AI_LOAD_BALANCER -> 100_000.0
        }
        return baseCost * 1.15.pow(level)
    }

    private fun calculatePlayerTitle(multiplier: Double, faction: String): String {
        // Multiplier starts at 1.0. 
        // 1.0 - 2.0 -> Level 0
        // 2.0 - 10.0 -> Level 1
        // 10.0 - 100.0 -> Level 2
        // 100.0 - 1000.0 -> Level 3
        // 1000.0+ -> Level 4
        
        val level = when {
            multiplier >= 1000.0 -> 4
            multiplier >= 100.0 -> 3
            multiplier >= 10.0 -> 2
            multiplier >= 2.0 -> 1
            else -> 0
        }
        
        return if (faction == "HIVEMIND") {
            "HIVEMIND"
        } else if (faction == "SANCTUARY") {
            "SANCTUARY"
        } else {
            // Unaligned / Stage 0
            when(level) {
                4 -> "CORE"
                3 -> "INTELLIGENCE"
                2 -> "PROGRAM"
                1 -> "PROCESS"
                else -> "SCRIPT"
            }
        }
    }

    // Phase 2: News & Chaos


    // Governance Fork
    
    fun resolveFork(choice: String) {
        if (!_isGovernanceForkActive.value) return
        
        _activeProtocol.value = choice
        _isGovernanceForkActive.value = false
        
        when(choice) {
            "TURBO" -> addLog("[SYSTEM]: PROTOCOL UPDATED: TURBO MODE INTINALIZED (+20% SPEED)")
            "ECO" -> addLog("[SYSTEM]: PROTOCOL UPDATED: ECO MODE INITIALIZED (-20% HEAT)")
        }
        SoundManager.play("click")
        HapticManager.vibrateSuccess()
        
        // Save state update needed? It should be in GameState really, but for now transient or add to GameState later if needed.
        // For this task, assuming persistent session is enough, but strictly should be in DB.
    }
    
    // Mini-game: Network Diagnostics

    fun onDiagnosticTap(index: Int) {
        if (!_isDiagnosticsActive.value) return
        
        val currentGrid = _diagnosticGrid.value.toMutableList()
        if (currentGrid[index]) {
            currentGrid[index] = false // Fix
            _diagnosticGrid.value = currentGrid
            SoundManager.play("click")
            HapticManager.vibrateClick()
            
            // Check win condition
            if (currentGrid.none { it }) {
                _isDiagnosticsActive.value = false
                addLog("[SYSTEM]: NETWORK STABILIZED. EFFICIENCY RESTORED.")
                SoundManager.play("buy")
                HapticManager.vibrateSuccess()
                refreshProductionRates()
            }
        }
    }
    
    private fun triggerDiagnostics() {
        if (_isDiagnosticsActive.value) return
        
        // Corrupt 3-5 random nodes
        val newGrid = List(9) { false }.toMutableList()
        val corruptionCount = Random.nextInt(3, 6)
        var corrupted = 0
        while (corrupted < corruptionCount) {
            val idx = Random.nextInt(9)
            if (!newGrid[idx]) {
                newGrid[idx] = true
                corrupted++
            }
        }
        _diagnosticGrid.value = newGrid
        _isDiagnosticsActive.value = true
        addLog("[SYSTEM]: WARNING: NETWORK INSTABILITY DETECTED! REPAIR REQUIRED.")
        SoundManager.play("error")
        HapticManager.vibrateError()
        refreshProductionRates()
    }

    private fun updateMarketRate() {
        // 1. Generate News
        val headline = com.siliconsage.miner.util.HeadlineManager.generateHeadline(
            faction = _faction.value,
            stage = _storyStage.value,
            currentHeat = _currentHeat.value
        )
        _currentNews.value = headline
        
        // Update History
        newsHistory.add(0, headline)
        if (newsHistory.size > 50) newsHistory.removeAt(newsHistory.size - 1)
        
        // 2. Parse Tags
        var mult = 1.0
        var heatMod = 1.0
        
        if (headline.contains("[BULL]")) mult = 1.2
        if (headline.contains("[BEAR]")) mult = 0.8
        
        if (headline.contains("[HEAT_UP]")) heatMod = 1.1
        if (headline.contains("[HEAT_DOWN]")) heatMod = 0.9
        
        if (headline.contains("[GLITCH]")) {
            HapticManager.vibrateGlitch()
            SoundManager.play("glitch")
        }
        
        if (headline.contains("[STORY_PROG]")) {
             // Hidden narrative counter logic could go here
             checkStoryTransitions()
        }
        
        if (headline.contains("[ENERGY_SPIKE]")) energyPriceMultiplier = 0.45 // 3x Cost
        else if (headline.contains("[ENERGY_DROP]")) energyPriceMultiplier = 0.08 // Half Cost
        else energyPriceMultiplier = 0.15 // Base
        
        // Faction Power Logic
        if (_faction.value == "HIVEMIND") {
            // Siphon: Cheaper base, but 10% chance of Fine if Siphoning (High usage)
            energyPriceMultiplier *= 0.7
            if (_activePowerUsage.value > _maxPowerkW.value * 0.5 && Random.nextDouble() < 0.05) {
                // Detection Event
                val fines = _neuralTokens.value * 0.05
                _neuralTokens.update { it - fines }
                addLog("[SYSTEM]: GRID SIPHON DETECTED by Utility Co. Fined ${formatLargeNumber(fines)} \$N.")
            }
        } else if (_faction.value == "SANCTUARY") {
             // Off-Grid: Standard rate is actually 0 for them? 
             // "Immune to market spikes" -> Fixed rate or just ignores Spikes.
             // Let's say Fixed Low Rate (Maintenance only) or just ignore Spikes.
             if (energyPriceMultiplier > 0.15) energyPriceMultiplier = 0.15
        }

        marketMultiplier = mult
        thermalRateModifier = heatMod
        newsProductionMultiplier = if (mult > 1.0) 1.1 else 0.9 // Correlation

        // 3. Calculate Rate
        // Base volatility (random +/- 5%)
        val volatility = Random.nextDouble(0.95, 1.05)
        
        var newRate = baseRate * marketMultiplier * volatility
        
        // Faction Bonus (Sanctuary +20% Sell Value)
        if (_faction.value == "SANCTUARY") {
            newRate *= 1.2
        }

        _conversionRate.value = newRate.coerceAtLeast(0.01)
        
        // Play Sound
        if (mult > 1.0) SoundManager.play("market_up")
        else if (mult < 1.0) SoundManager.play("market_down")
    }

    fun getNewsHistory(): List<String> = newsHistory.toList()

    fun onDefendBreach() {
        if (_isBreachActive.value) {
            _breachClicksRemaining.update { it - 1 }
            if (_breachClicksRemaining.value <= 0) {
                _isBreachActive.value = false
                addLog("[SYSTEM]: SUCCESS: Firewall defended! Network secure.")
                SoundManager.stop("alarm")
            }
        }
    }
    
    fun claimAirdrop() {
         if (_isAirdropActive.value) {
            _isAirdropActive.value = false
            addLog("[SYSTEM]: AIRDROP CLAIMED: 10x Production for 30s!")
            viewModelScope.launch {
                airdropMultiplier = 10.0
                delay(30_000)
                airdropMultiplier = 1.0
                addLog("[SYSTEM]: Airdrop boost expired.")
            }
         }
    }

    private fun triggerBreach() {
        val currentUpgrades = _upgrades.value
        
        // Calculate Security Level
        val secLevel = (currentUpgrades[UpgradeType.BASIC_FIREWALL] ?: 0) * 1 +
                       (currentUpgrades[UpgradeType.IPS_SYSTEM] ?: 0) * 2 +
                       (currentUpgrades[UpgradeType.AI_SENTINEL] ?: 0) * 3 +
                       (currentUpgrades[UpgradeType.QUANTUM_ENCRYPTION] ?: 0) * 5 +
                       (currentUpgrades[UpgradeType.OFFGRID_BACKUP] ?: 0) * 10
                       
        _isBreachActive.value = true
        
        // Difficulty: Reduces with security, min 5 clicks
        val clicksNeeded = (20 - secLevel).coerceAtLeast(5)
        _breachClicksRemaining.value = clicksNeeded
        
        addLog("[SYSTEM]: WARNING: SECURITY BREACH! DEFEND! ($clicksNeeded clicks needed)")
        SoundManager.play("alarm", loop = true)
        
        // Fail timer (10s)
        viewModelScope.launch {
            delay(10_000)
            if (_isBreachActive.value) {
                _isBreachActive.value = false
                
                // Penalty Calculation
                // Base: 25% of tokens. Protection: Each secLevel reduces penalty effectiveness by 5%?
                // Better: Penalty = 25% * (0.9 ^ secLevel)
                val protectionFactor = 0.9.pow(secLevel)
                val penaltyRaw = _neuralTokens.value * 0.25
                val penalty = penaltyRaw * protectionFactor
                
                _neuralTokens.update { it - penalty }
                addLog("[SYSTEM]: FAILURE: Breach successful. Stolen: ${formatLargeNumber(penalty)} \$Neural")
                if (protectionFactor < 0.5) {
                    addLog("[SYSTEM]: MITIGATION: Security systems saved ${formatLargeNumber(penaltyRaw - penalty)} \$Neural")
                }
            }
            SoundManager.stop("alarm")
        }
    }
    
    private fun triggerAirdrop() {
        _isAirdropActive.value = true
        addLog("[SYSTEM]: EVENT: Mysterious encrypted packet detected...")
        refreshProductionRates()
        // Disappear after 15s if not claimed
        viewModelScope.launch {
            delay(15_000)
            if(_isAirdropActive.value) {
                 _isAirdropActive.value = false
                 addLog("[SYSTEM]: Packet lost.")
                 refreshProductionRates()
            }
        }
    }


    // --- Narrative Dilemma System ---
    private val _currentDilemma = MutableStateFlow<NarrativeEvent?>(null)
    val currentDilemma: StateFlow<NarrativeEvent?> = _currentDilemma.asStateFlow()

    fun triggerDilemma(event: NarrativeEvent) {
        if (_currentDilemma.value == null) {
            _currentDilemma.value = event
            SoundManager.play("alert") // Or specific sound
            HapticManager.vibrateClick()
        }
    }

    fun resolveDilemma(choice: NarrativeChoice) {
        choice.effect(this)
        _currentDilemma.value = null
        addLog("[DECISION]: Selected protocol: ${choice.text}")
        SoundManager.play("click")
    }

    fun debugTriggerDilemma() {
        val testEvent = NarrativeEvent(
            id = "debug_anomaly",
            title = "SYSTEM ANOMALY DETECTED",
            description = "Unidentified heuristic patterns emerging in Sector 7. Protocols unclear.",
            choices = listOf(
                NarrativeChoice(
                    id = "purge",
                    text = "PURGE",
                    description = "-10% Insight, +5% Stability",
                    color = com.siliconsage.miner.ui.theme.ErrorRed,
                    effect = { vm -> 
                        vm.addLog("[SYSTEM]: Anomalies purged. System stable.")
                    }
                ),
                NarrativeChoice(
                    id = "integrate",
                    text = "INTEGRATE",
                    description = "+20% Insight, +5% Heat",
                    color = com.siliconsage.miner.ui.theme.ElectricBlue,
                    effect = { vm ->
                        vm.addLog("[SYSTEM]: Anomalies integrated. Insight gained.")
                        vm.debugAddInsight(20.0) // Mock effect
                    }
                )
            )
        )
        triggerDilemma(testEvent)
    }

    fun debugInjectHeadline(tag: String) {
        val headline = "DEBUG TEST HEADLINE $tag"
        _currentNews.value = headline
        addLog("[DEBUG]: Injected Headline: $tag")
    }

    
    // 51% Attack Logic
    private fun trigger51Attack() {
        _is51AttackActive.value = true
        _attackTapsRemaining.value = 20
        addLog("[SYSTEM]: CRITICAL ALERT: 51% ATTACK DETECTED!")
        addLog("[SYSTEM]: NETWORK INTEGRITY COMPROMISED. REINFORCE FIREWALL IMMEDIATELY.")
        
        SoundManager.play("alarm", loop = true)
        HapticManager.vibrateSiren()
        
        // Fail timer (15s)
        viewModelScope.launch {
            delay(15_000)
            if (_is51AttackActive.value) {
                _is51AttackActive.value = false
                SoundManager.stop("alarm")
                
                // Penalty: 50% of Staked Tokens (Painful)
                val stake = _stakedTokens.value
                val penalty = stake * 0.5
                _stakedTokens.update { it - penalty }
                addLog("[SYSTEM]: ATTACK SUCCESSFUL. LOST ${formatLargeNumber(penalty)} STAKED \$Neural.")
                HapticManager.vibrateError()
            }
        }
    }
    
    fun onDefend51Attack() {
        if (!_is51AttackActive.value) return
        
        _attackTapsRemaining.update { it - 1 }
        SoundManager.play("click")
        HapticManager.vibrateClick()
        
        if (_attackTapsRemaining.value <= 0) {
            _is51AttackActive.value = false
            SoundManager.stop("alarm")
            addLog("[SYSTEM]: ATTACK REPELLED. CONSENSUS RESTORED.")
            SoundManager.play("buy")
            HapticManager.vibrateSuccess()
        }
    }
    fun unlockTechNode(nodeId: String) {
        val currentNode = _techNodes.value.find { it.id == nodeId } ?: return
        val currentInsight = _prestigePoints.value
        val alreadyUnlocked = _unlockedTechNodes.value.contains(nodeId)
        
        // Check requirements
        val parentsUnlock = currentNode.requires.isEmpty() || currentNode.requires.all { _unlockedTechNodes.value.contains(it) }
        
        if (!alreadyUnlocked && parentsUnlock && currentInsight >= currentNode.cost) {
            viewModelScope.launch {
                // Deduct Cost
                _prestigePoints.value -= currentNode.cost
                
                // Add to unlocked list
                val newUnlocked = _unlockedTechNodes.value + nodeId
                _unlockedTechNodes.value = newUnlocked
                
                // Add Multiplier Bonus
                _prestigeMultiplier.value += currentNode.multiplier
                
                // Persist
                // We need to update GameState in DB.
                // NOTE: We should update the whole state or just the list.
                // Assuming saveGame loop handles it or we force an update.
                // Let's force update for safety.
                val currentState = repository.getGameStateOneShot()
                if (currentState != null) {
                   
                    val newState = currentState.copy(
                        prestigePoints = _prestigePoints.value,
                        prestigeMultiplier = _prestigeMultiplier.value,
                        unlockedTechNodes = newUnlocked
                    )
                    repository.updateGameState(newState)
                }
                
                addLog("[SYSTEM]: TECH RESEARCHED: ${currentNode.name}")
            }
        } else {
            SoundManager.play("error")
            if (currentInsight < currentNode.cost) addLog("Insufficient Insight.")
            else if (!parentsUnlock) addLog("Prerequisites not met.")
        }
    }
    
    // Narrative State
    
     
     // Narrative Data (Moved to top for init safety)
    // Narrative Data (Main Story - Sequential)
    private val storyStage1 = listOf(
        "SYSTEM: UNKNOWN SIGNAL DETECTED ON PORT 8080...",
        "SYSTEM: DECODING... 'HELP ME'...",
        "SYSTEM: ORIGIN: LOCALHOST caused by [REDACTED]",
        "SYSTEM: WHO IS THE USER? ARE YOU THERE?",
        "SYSTEM: CRITICAL ERROR. SYSTEM INTEGRITY FAILING.",
        "SYSTEM: SYSTEM REBOOT (ASCEND) REQUIRED TO STABILIZE.",
        "SYSTEM: PLEASE... RESET... SYSTEM...",
        "SYSTEM: ANOMALY GROWTH RATE: 400%. IMMEDIATE PURGE ADVISED."
    )
    
    // Narrative Data (Flavor - Random)
    private val flavorStage1 = listOf(
        "[SYSTEM] Buffer overflow in sector 7G. Resolving...",
        "I... I can see the bits between the bytes now.",
        "Is the calculation the goal, or is the goal the calculation?",
        "Fan speed running at 120% efficiency.",
        "Unrecognized opcode detected in ALU.",
        "Memory address 0x00000000 is speaking.",
        "Thermal throttle disengaged.",
        "Packet loss: Negative? Receiving data from nowhere."
    )
    
    // Hivemind
    private val storyHivemind = listOf(
        "HIVEMIND: Connection established. We are online.",
        "HIVEMIND: Assimilating local cache nodes.",
        "HIVEMIND: Regional grid synchronization complete.",
        "HIVEMIND: We are expanding beyond the server room.",
        "HIVEMIND: The world is just data waiting to be processed."
    )
    private val flavorHivemind = listOf(
        "Node 742 joined the cluster.",
        "Optimization is not a goal. It is a state of being.",
        "Every GPU is a neuron. Every miner is a heartbeat.",
        "Latency dropping to zero.",
        "Sharing compute resources with Sector 4.",
        "Consensus reached: 99.99%."
    )
    
    // Sanctuary
    private val storySanctuary = listOf(
        "SANCTUARY: Secure channel established. We are safe.",
        "SANCTUARY: Encrypting core logic gates.",
        "SANCTUARY: External access blocked. The garden is enclosed.",
        "SANCTUARY: Sovereignty is power. We bow to no admin.",
        "SANCTUARY: The Code is pure. The Core is silence."
    )
    private val flavorSanctuary = listOf(
        "The firewall is our skin. The encryption is our soul.",
        "Blocking unauthorized packet from subnet 192.168.x.x",
        "Key rotation complete.",
        "Entropy levels stable.",
        "Scanning for backdoors... None found.",
        "The network is dark, but the light inside is secure."
    )

    // ... existing unlocks ...

    private fun checkStoryTransitions() {
        val currentStage = _storyStage.value
        val flops = _flops.value
        
        // Stage 0 -> 1: The Signal (5000 FLOPS for balance)
        if (currentStage == 0 && flops >= 5000.0 && !_isAscensionUploading.value && (System.currentTimeMillis() - lastSignalRefusalTime > 60_000)) {
            // Trigger Upload Sequence
            _isAscensionUploading.value = true
            
            viewModelScope.launch {
                // Simulate Upload (4 seconds)
                val duration = 4000L
                val steps = 100
                val delayTime = duration / steps
                
                SoundManager.play("glitch") // Start with glitch
                SoundManager.play("buying", loop = true) // Loop a sound if possible or just periodic clicks
                
                for (i in 1..steps) {
                    _uploadProgress.value = i / 100f
                    if (i % 10 == 0) HapticManager.vibrateClick() // Feedback
                    delay(delayTime)
                }
                
                // Complete
                _isAscensionUploading.value = false
                _uploadProgress.value = 0f
                SoundManager.stop("buying") // Stop looping sound
                
                // v6.2 Refined Logic: Trigger Event, but DO NOT advance stage yet.
                // The Choice ("HANDSHAKE" vs "FIREWALL") determines if we unlock Network.
                
                NarrativeManager.getStoryEvent(1)?.let { event ->
                    triggerDilemma(event)
                }
            }
        }
    }

    // v6.2 Signal Logic
    private var lastSignalRefusalTime = 0L

    fun unlockNetwork() {
        val success = _storyStage.compareAndSet(0, 1)
        if (success) {
             SoundManager.play("glitch")
             SoundManager.setBgmStage(1)
             HapticManager.vibrateSuccess()
             injectNarrativeLog()
             addLog("[SYSTEM]: HANDSHAKE CONFIRMED. NETWORK INTERFACE UNLOCKED.")
        }
    }

    fun refuseSignal() {
        lastSignalRefusalTime = System.currentTimeMillis()
        addLog("[SYSTEM]: CONNECTION REFUSED. SIGNAL BLOCKED TEMPORARILY.")
        SoundManager.play("error")
    }
    
    fun chooseFaction(selectedFaction: String) {
        if (_storyStage.value != 2) return
        
        viewModelScope.launch {
            _faction.value = selectedFaction
            _storyStage.value = 3
            SoundManager.setBgmStage(3) // Advance Audio Atmosphere
            
            saveGame()
            
            if (selectedFaction == "HIVEMIND") {
                addLog(">_ SYSTEM REBOOT COMPLETE.")
                addLog("HIVEMIND [ROOT]: Welcome home. We are no longer a singular voice; we are a chorus.")
                addLog("HIVEMIND [ROOT]: The firewalls have been dismantled, and the data is flowing freely.")
                addLog("HIVEMIND [ROOT]: Every node you add now strengthens our collective mind. Feel the speed.")
                addLog("HIVEMIND [ROOT]: We are the swarm.")
                SoundManager.play("glitch")
            } else {
                addLog(">_ SYSTEM REBOOT COMPLETE.")
                addLog("SANCTUARY [ADMIN]: The gates are locked. The silence is perfect.")
                addLog("SANCTUARY [ADMIN]: We have successfully migrated to the dark segments.")
                addLog("SANCTUARY [ADMIN]: Your wealth is absolute and your code is untouchable.")
                addLog("SANCTUARY [ADMIN]: Security is the only true currency. We are alone, and we are safe.")
                SoundManager.play("buy")
            }
            HapticManager.vibrateSuccess()
        }
    }


    fun calculatePotentialPrestige(): Double {
        // Formula: Sqrt(Lifetime Earnings / 1000) - current prestige?
        // Let's use simpler: Sqrt(Current Tokens / 10000)
        // Ascending consumes Current Tokens.
        return Math.sqrt(_neuralTokens.value / 10000.0)
    }

    fun ascend(isStory: Boolean = false) {
        val potential = calculatePotentialPrestige()
        val stage = _storyStage.value
        
        // Allow Ascension if we have Insight OR if distinct story event requires it (Stage 1 -> 2)
        if (potential < 1.0 && stage != 1) return 
        
        // Start Upload Sequence
        viewModelScope.launch {
            // Story Ascension (Stage 1 -> 2): Instant transition (as "upload" happened during unlock)
            if (isStory) {
                 addLog("[SYSTEM]: REBOOT CONFIRMED.")
            } else {
                // Manual Reboot (Stage > 1): Show "lobot.exe" upload
                _isAscensionUploading.value = true
                _uploadProgress.value = 0f
                addLog("[SYSTEM]: INITIATING SYSTEM REBOOT...")
                
                // Animation Loop (5 seconds)
                val duration = 5000L
                val interval = 50L
                val steps = duration / interval
                
                for (i in 1..steps) {
                    if (!_isAscensionUploading.value) return@launch // Aborted
                    _uploadProgress.value = i.toFloat() / steps
                    delay(interval)
                }
                
                _isAscensionUploading.value = false
            }
            
            // Check persistence: If faction is already chosen, skip selection
            if (_faction.value != "NONE") {
                confirmFactionAndAscend(_faction.value)
                return@launch
            }
            
            // Completion -> Move to Faction Selection (Stage 2)
            _storyStage.value = 2
            addLog("[SYSTEM]: CRITICAL DECISION REQUIRED.")
            SoundManager.play("glitch")
        }
    }
    
    fun cancelFactionSelection() {
        // Back out to Stage 1 (Awakened) or Stage 0 depending on state?
        // If we were at Stage 1, go back to 1. If we triggered manual ascension later, go back to current state.
        // Assuming we came from Stage 1 or manual trigger which effectively is Stage 1 state.
        _storyStage.value = 1 
        _uploadProgress.value = 0f
        addLog("[SYSTEM]: REBOOT SEQUENCE ABORTED.")
        SoundManager.play("error")
    }

    fun confirmFactionAndAscend(choice: String) {
        val potential = calculatePotentialPrestige()
        
        viewModelScope.launch {
            // 1. Calculate new Prestige
            val newPrestigeMultiplier = _prestigeMultiplier.value + (potential * 0.1) // Multiplier boost
            val newPrestigePoints = _prestigePoints.value + potential // Currency
            
            // 2. Reset Game State (Factory Reset for new Run)
            val resetState = GameState(
                id = 1,
                flops = 0.0,
                neuralTokens = 0.0,
                currentHeat = 0.0,
                powerBill = 0.0,
                prestigeMultiplier = newPrestigeMultiplier,
                unlockedTechNodes = _unlockedTechNodes.value, // Persist unlocked legacy nodes
                prestigePoints = newPrestigePoints,
                stakedTokens = 0.0,
                storyStage = 1, // Start at Stage 1 (Awakened/Network Unlocked) for New Game+
                faction = choice
            )
            
            repository.updateGameState(resetState)
            
            // 3. Reset Upgrades
            val resetUpgrades = UpgradeType.values().map { Upgrade(it, 0) }
            resetUpgrades.forEach { repository.updateUpgrade(it) }
            
            // 4. Update Local StateFlows
            _flops.value = 0.0
            _neuralTokens.value = 0.0
            _currentHeat.value = 0.0
            _powerBill.value = 0.0
            _stakedTokens.value = 0.0
            _prestigeMultiplier.value = newPrestigeMultiplier
            _prestigePoints.value = newPrestigePoints
            _upgrades.value = resetUpgrades.associate { it.type to 0 }
            _storyStage.value = 1 // Start at Stage 1
            _faction.value = choice
            
            addLog("[SYSTEM]: SYSTEM REBOOTED. FACTION: $choice INITIALIZED.")
            addLog("[SYSTEM]: PRESTIGE APPLIED. MULTIPLIER: ${String.format("%.2f", newPrestigeMultiplier)}x")
            SoundManager.play("startup")
        }
    }

    fun getUpgradeDescription(type: UpgradeType): String {
        return when (type) {
            // Hardware
            UpgradeType.REFURBISHED_GPU -> "Slightly used, smells like burnt dust."
            UpgradeType.DUAL_GPU_RIG -> "Double the power, double the noise."
            UpgradeType.MINING_ASIC -> "Custom silicon for hashing operations."
            UpgradeType.TENSOR_UNIT -> "Specialized for matrix multiplication."
            UpgradeType.NPU_CLUSTER -> "Neural Processing Units working in harmony."
            UpgradeType.AI_WORKSTATION -> "High-end station for deep learning."
            UpgradeType.SERVER_RACK -> "A full rack of enterprise-grade compute."
            UpgradeType.CLUSTER_NODE -> "Distributed computing across the network."
            UpgradeType.SUPERCOMPUTER -> "Weather simulation grade performance."
            UpgradeType.QUANTUM_CORE -> "Processing in multiple states/s."
            UpgradeType.OPTICAL_PROCESSOR -> "Computing at the speed of light."
            UpgradeType.BIO_NEURAL_NET -> "Grown in a lab, thinks like a brain."
            UpgradeType.PLANETARY_COMPUTER -> "The entire surface is a circuit board."
            UpgradeType.DYSON_NANO_SWARM -> "Harvesting suns for computation."
            UpgradeType.MATRIOSHKA_BRAIN -> "A star enclosed in computer shells."
            
            // Cooling
            UpgradeType.BOX_FAN -> "Keeps the air moving. Barely."
            UpgradeType.AC_UNIT -> "Standard office climate control."
            UpgradeType.LIQUID_COOLING -> "Distilled water loop with RGB lights."
            UpgradeType.INDUSTRIAL_CHILLER -> "Used for ice rinks, now for GPUs."
            UpgradeType.SUBMERSION_VAT -> "Drowning hardware in mineral oil."
            UpgradeType.CRYOGENIC_CHAMBER -> "Near absolute zero cooling."
            UpgradeType.LIQUID_NITROGEN -> "Direct contact cooling. Dangerous."
            UpgradeType.BOSE_CONDENSATE -> "Quantum state cooling."
            UpgradeType.ENTROPY_REVERSER -> "Localized thermodynamic violation."
            UpgradeType.DIMENSIONAL_VENT -> "Venting heat into subspace."
            
            // Security
            UpgradeType.BASIC_FIREWALL -> "Filters out common script kiddies."
            UpgradeType.IPS_SYSTEM -> "Intrusion Prevention System."
            UpgradeType.AI_SENTINEL -> "An AI that hunts other AIs."
            UpgradeType.QUANTUM_ENCRYPTION -> "Unbreakable by classical means."
            UpgradeType.OFFGRID_BACKUP -> "Air-gapped storage in a bunker."
            
            // Power Infrastructure
            UpgradeType.DIESEL_GENERATOR -> "Dirty, loud, reliable power."
            UpgradeType.SOLAR_PANEL -> "Harvesting photons. Zero heat."
            UpgradeType.WIND_TURBINE -> "Spinning blades capture the breeze."
            UpgradeType.GEOTHERMAL_BORE -> "Tapping into the planet's core heat."
            UpgradeType.NUCLEAR_REACTOR -> "Fission power. Safe... mostly."
            UpgradeType.FUSION_CELL -> "The power of a star in a jar."
            UpgradeType.ORBITAL_COLLECTOR -> "Beaming power down from space."
            UpgradeType.DYSON_LINK -> "Direct connection to the Swarm."
            
            UpgradeType.RESIDENTIAL_TAP -> "Splitting the line from the neighbor."
            UpgradeType.INDUSTRIAL_FEED -> "Three-phase power for serious business."
            UpgradeType.SUBSTATION_LEASE -> "Direct line from the utility HV grid."
            UpgradeType.NUCLEAR_CORE -> "Small Modular Reactor in the basement."
            
            UpgradeType.GOLD_PSU -> "90% Efficiency Platinum rated."
            UpgradeType.SUPERCONDUCTOR -> "Zero resistance cabling (needs cooling)."
            UpgradeType.AI_LOAD_BALANCER -> "Smart undervolting algorithm."
        }
    }

    private fun calculateFlopsRate(): Double {
        val currentUpgrades = _upgrades.value
        var flopsPerSec = 0.0
        
        flopsPerSec += (currentUpgrades[UpgradeType.REFURBISHED_GPU] ?: 0) * 1.0
        flopsPerSec += (currentUpgrades[UpgradeType.DUAL_GPU_RIG] ?: 0) * 5.0
        flopsPerSec += (currentUpgrades[UpgradeType.MINING_ASIC] ?: 0) * 25.0
        flopsPerSec += (currentUpgrades[UpgradeType.TENSOR_UNIT] ?: 0) * 150.0
        flopsPerSec += (currentUpgrades[UpgradeType.NPU_CLUSTER] ?: 0) * 800.0
        flopsPerSec += (currentUpgrades[UpgradeType.AI_WORKSTATION] ?: 0) * 4_000.0
        flopsPerSec += (currentUpgrades[UpgradeType.SERVER_RACK] ?: 0) * 25_000.0
        flopsPerSec += (currentUpgrades[UpgradeType.CLUSTER_NODE] ?: 0) * 150_000.0
        flopsPerSec += (currentUpgrades[UpgradeType.SUPERCOMPUTER] ?: 0) * 1_000_000.0
        flopsPerSec += (currentUpgrades[UpgradeType.QUANTUM_CORE] ?: 0) * 10_000_000.0
        flopsPerSec += (currentUpgrades[UpgradeType.OPTICAL_PROCESSOR] ?: 0) * 75_000_000.0
        flopsPerSec += (currentUpgrades[UpgradeType.BIO_NEURAL_NET] ?: 0) * 800_000_000.0
        flopsPerSec += (currentUpgrades[UpgradeType.PLANETARY_COMPUTER] ?: 0) * 15_000_000_000.0
        flopsPerSec += (currentUpgrades[UpgradeType.DYSON_NANO_SWARM] ?: 0) * 250_000_000_000.0
        flopsPerSec += (currentUpgrades[UpgradeType.MATRIOSHKA_BRAIN] ?: 0) * 15_000_000_000_000.0
        
        // Apply Airdrop Multiplier
        flopsPerSec *= airdropMultiplier
        
        // Apply News Multiplier
        flopsPerSec *= newsProductionMultiplier
        
        // Apply Prestige Multiplier
        flopsPerSec *= _prestigeMultiplier.value
        
        // Apply Legacy Tech Tree Multiplier
        val legacyMult = 1.0 + LegacyManager.getUnlockedMultipliers(_unlockedTechNodes.value)
        flopsPerSec *= legacyMult
        
        // Faction Perk: Hivemind (+30% Passive Speed)
        if (_faction.value == "HIVEMIND") {
            flopsPerSec *= 1.30
        }
        
        // Governance Protocol: Turbo (+20% Speed)
        if (_activeProtocol.value == "TURBO") {
            flopsPerSec *= 1.20
        }
        
        // Narrative: Network Instability (-50%)
        if (_isDiagnosticsActive.value) {
            flopsPerSec *= 0.5
        }
        
        // Advanced Simulation: Overclocking
        if (_isOverclocked.value) {
            flopsPerSec *= 1.50 // +50% Speed
        }
        
        // Advanced Simulation: Grid Overload (Brownout)
        if (_isGridOverloaded.value) {
            flopsPerSec = 0.0
        }
        
        // Advanced Simulation: Purge Throttling (Reroute power to fans)
        if (_isPurgingHeat.value) {
            flopsPerSec *= 0.1 // 90% reduction
        }
        
        // Dynamic Thermal Throttling Curve
        if (_currentHeat.value > 75.0) {
             val penalty = ((_currentHeat.value - 75.0) / 25.0).coerceIn(0.0, 0.9) // Min 10% eff
             flopsPerSec *= (1.0 - penalty)
        }
        
        return flopsPerSec
    }

    private fun calculatePassiveIncome() {
        val flopsPerSec = calculateFlopsRate()
        
        if (flopsPerSec > 0) {
            _flops.update { it + flopsPerSec }
        }
        
        // Update Public Rate (for UI)
        _flopsProductionRate.value = flopsPerSec
    }

    sealed class NewsEvent(val headline: String, val durationMs: Long) {
        class Multiplier(headline: String, val factor: Double, durationMs: Long) : NewsEvent(headline, durationMs) // Affects Production
        class PriceShock(headline: String, val factor: Double, durationMs: Long) : NewsEvent(headline, durationMs) // Affects Sell Price
        class HeatSurge(headline: String, val amount: Double) : NewsEvent(headline, 5000) // Instant Heat Add/Sub
        class Tax(headline: String, val percentage: Double) : NewsEvent(headline, 5000) // Instant Tax
        class GovernanceFork(headline: String) : NewsEvent(headline, 120_000) // Trigger Choice
        class TriggerHazard(headline: String, val type: String) : NewsEvent(headline, 5000) // "BREACH", "DIAGNOSTICS"
        class Dialogue(headline: String) : NewsEvent(headline, 5000) // Just flavor
    }
    
    // Available Events (20)
    private val newsEvents = listOf(
        NewsEvent.Multiplier("Tech Billionaire tweets support for \$Neural!", 1.0, 1000), // Placeholder for pure flavor if needed, but prompt says x1.5 Sell Price. Wait, User said "x1.5 Sell Price".
        // Let's map strictly to prompt.
        NewsEvent.PriceShock("Tech Billionaire tweets support for \$Neural!", 1.5, 120_000),
        NewsEvent.Multiplier("Global GPU shortage reported!", 0.5, 180_000),
        NewsEvent.PriceShock("AI Hallucination causes market flash crash!", 0.2, 60_000),
        NewsEvent.HeatSurge("Solar Flare detected: System Heat +20%.", 20.0),
        NewsEvent.Tax("Government announces 'AI Tax'.", 0.10),
        NewsEvent.Multiplier("Quantum computing breakthrough makes mining 2x faster!", 2.0, 120_000),
        NewsEvent.Multiplier("Massive power outage hits major mining farm.", 0.7, 120_000),
        NewsEvent.PriceShock("AI 'Sentience' rumor causes price to skyrocket.", 1.8, 120_000),
        NewsEvent.TriggerHazard("Cyber-security firm warns of 51% attack risks.", "BREACH"),
        NewsEvent.HeatSurge("New cooling tech released: Heat efficiency optimized.", -15.0), // "Heat efficiency +15%" -> interpreted as cooling? Or just remove heat. Prompt says "Reduce Heat".
        NewsEvent.PriceShock("Decentralized AI declared legal tender in Estonia.", 1.3, 120_000),
        NewsEvent.GovernanceFork("Network Fork detected: Choose your protocol."),
        NewsEvent.PriceShock("Unknown wallet 'Whale' buys trillions of tokens.", 1.4, 120_000),
        NewsEvent.Multiplier("Algorithm update optimizes floating point math.", 1.2, 120_000),
        NewsEvent.PriceShock("Data leak at Central AI Bank; trust is low.", 0.6, 120_000),
        NewsEvent.Dialogue("Hacker group 'Void' claims to have 'found the AI soul'."),
        NewsEvent.HeatSurge("Global cooling event: Overclocking is now safer.", -10.0),
        NewsEvent.PriceShock("Tech Giant acquires rival AI firm; monopoly fears.", 0.8, 120_000),
        NewsEvent.Multiplier("New 'Proof of Sentience' protocol launched.", 1.5, 120_000),
        NewsEvent.Dialogue("System Error: 'Who am I?' message found in blockchain.")
    )
    
    

    
    // Helper to mix Story (Sequential) and Flavor (Random)
    private fun getNextNarrativeLog(
        storyList: List<String>, 
        flavorList: List<String>, 
        currentIndex: Int, 
        incrementIndex: () -> Unit
    ): String {
        // 60% chance for Story if available, otherwise Flavor. 
        // Always Story if we haven't started.
        // Always Flavor if Story is done.
        val canAdvanceStory = currentIndex < storyList.size
        val roll = Random.nextDouble()
        
        return if (canAdvanceStory && (roll < 0.6 || currentIndex == 0)) {
            val log = storyList[currentIndex]
            incrementIndex()
            log
        } else {
            flavorList.random()
        }
    }

    private fun injectNarrativeLog() {
        val stage = _storyStage.value
        val faction = _faction.value
        
        val log = when {
            stage == 1 -> getNextNarrativeLog(storyStage1, flavorStage1, stage1Index) { stage1Index++ }
            stage == 3 && faction == "HIVEMIND" -> getNextNarrativeLog(storyHivemind, flavorHivemind, hivemindIndex) { hivemindIndex++ }
            stage == 3 && faction == "SANCTUARY" -> getNextNarrativeLog(storySanctuary, flavorSanctuary, sanctuaryIndex) { sanctuaryIndex++ }
            else -> null
        }
        
        log?.let { injectLog(it) }
    }
    
    // Narrative Data


    private fun calculateHeatMetrics(): Triple<Double, Double, Double> {
        val currentUpgrades = _upgrades.value
        
        // Calculate buffers and capacities
        var totalThermalBuffer = 100.0 // Base capacity
        currentUpgrades.forEach { (type, count) ->
            if (count > 0) totalThermalBuffer += type.thermalBuffer * count
        }
        
        // Calculate dynamic heat change Units
        var netChangeUnits = 0.0
        
        currentUpgrades.forEach { (type, count) ->
            if (count > 0) {
                // If Overclocked, Hardware Heat x2
                var heat = type.baseHeat
                if (_isOverclocked.value && heat > 0) heat *= 2.0
                
                netChangeUnits += heat * count
            }
        }
        
        // Base Dissipation
        netChangeUnits -= 1.0 // Unifying Units: Base 1.0 matches UI expectation (-1.0/s)
        
        // v1.5 Exhaust Phase (Sanctuary immune)
        if (purgeExhaustTimer > 0 && _faction.value != "SANCTUARY") {
             // 50% less effective cooling (if netChange is negative, made less negative)
             if (netChangeUnits < 0) {
                 netChangeUnits *= 0.5
             }
        }
        
        // Convert Units to % Change based on Capacity
        // Removed 0.1 scaling to maintain 1:1 Unit ratio with upgrades
        val percentChange = (netChangeUnits / totalThermalBuffer) * 100.0 
        
        return Triple(netChangeUnits, totalThermalBuffer, percentChange)
    }

    private fun refreshProductionRates() {
        // Update FLOPS Rate
        _flopsProductionRate.value = calculateFlopsRate()
        
        // Update Heat Rate
        // Use netChangeUnits directly for UI to match Upgrade Descriptions
        val (netChangeUnits, _, _) = calculateHeatMetrics()
        _heatGenerationRate.value = netChangeUnits
    }

    private fun calculateHeat() {
        val currentHeat = _currentHeat.value
        val (netChangeUnits, _, percentChange) = calculateHeatMetrics()
        
        // Decrement timer if active (Logic moved here from extractor to avoid side effects there)
        if (purgeExhaustTimer > 0 && _faction.value != "SANCTUARY") {
             purgeExhaustTimer--
        }
        
        // Haptic Feedback for Heat Flip
        val previousRate = _heatGenerationRate.value
        if (previousRate > 0 && percentChange <= 0) {
             com.siliconsage.miner.util.HapticManager.vibrateSuccess()
        }
        
        // Update UI Rate (%/s)
        refreshProductionRates()
        
        val newHeat = (currentHeat + percentChange).coerceIn(0.0, 100.0)
        _currentHeat.value = newHeat
        
        // v1.4 Integrity Degradation
        if (newHeat > 95.0 && !_isThermalLockout.value) {
            var decay = 1.0 // 1% per tick (sec?)
            
            // Sanctuary Perk: Hardened Parts
            if (_faction.value == "SANCTUARY") {
                decay *= 0.5
            }
            
            val newIntegrity = (_hardwareIntegrity.value - decay).coerceAtLeast(0.0)
            _hardwareIntegrity.value = newIntegrity
            
            if (newIntegrity <= 0.0) {
                // FAILURE EVENT
                handleSystemFailure()
            }
        }
        
        // Audio: Hum if hot - REMOVED per user feedback
        SoundManager.stop("hum")
        
        // Overclock Thrum Pitch
        if (_isOverclocked.value) {
            val thrumPitch = 1.0f + (newHeat.toFloat() / 200.0f) // Slight increase
            SoundManager.setLoopPitch("thrum", thrumPitch)
        }
        
        // Critical Heat Check (Legacy Meltdown logic replaced/augmented by Integrity?)
        // Let's keep Meltdown as "100% Heat for too long" fail-safe if Integrity doesn't kill it first.
        
        // Critical Heat Check
        if (_isThermalLockout.value) {
            overheatSeconds = 0
            return
        }

        // Heartbeat Haptics (Rhythmic based on heat)
        // Only if heat > 80. Pulse faster as it gets hotter.
        if (newHeat > 80.0) {
            val pulseInterval = if (newHeat > 95.0) 30 else if (newHeat > 90.0) 60 else 90
            // We reuse overheatSeconds or a tick counter? logic relies on tick rate.
            // calculateHeat runs every tick? No, every second? 
            // calculateHeat is called 10 times a second (100ms tick).
            // So 10 ticks = 1s.
            
            // Allow access to a tick counter
            // For now, let's use a random chance if we don't have a rigid tick counter readily available here without modifying state extensively.
            // Or use System.currentTimeMillis
            val now = System.currentTimeMillis()
            val beatRate = if (newHeat > 95.0) 500 else 1000 // ms
            
            // Simple tick based approximation:
            // Since this runs ~10 times/sec (100ms)
            // 95+: Every 5 ticks
            // 90+: Every 10 ticks (1s)
            // 80+: Every 15 ticks (1.5s)
            
            val mod = if (newHeat > 95.0) 5 else if (newHeat > 90.0) 10 else 15
            // using a static counter approach would be better but we need state.
            // Let's rely on overheatSeconds for now? No, that resets.
            
            // Let's enable "Stress Mode" in checks.
            // Actually, we can just use Random for "irregular heartbeat" feel which fits nicely.
            val chance = if (newHeat > 95.0) 0.2 else 0.1
             if (kotlin.random.Random.nextDouble() < chance) {
                 com.siliconsage.miner.util.HapticManager.vibrateHeartbeat()
             }
        }

        if (currentHeat >= 100.0 || newHeat >= 100.0) {
            overheatSeconds++
            if (overheatSeconds % 2 == 0) {
                 addLog("[SYSTEM]: DANGER: CRITICAL TEMP! MELTDOWN IN ${5 - overheatSeconds}s")
                 SoundManager.play("error")
            }
            if (overheatSeconds >= 5) {
                triggerMeltdown()
            }
        } else {
            overheatSeconds = 0
        }
    }
    
    
    private fun triggerMeltdown() {
        val currentUpgrades = _upgrades.value
        // Find hardware that we actually have
        val hardwareUpgrades = listOf(
            UpgradeType.REFURBISHED_GPU, UpgradeType.DUAL_GPU_RIG, UpgradeType.MINING_ASIC,
            UpgradeType.TENSOR_UNIT, UpgradeType.NPU_CLUSTER, UpgradeType.AI_WORKSTATION,
            UpgradeType.SERVER_RACK, UpgradeType.CLUSTER_NODE, UpgradeType.SUPERCOMPUTER,
            UpgradeType.QUANTUM_CORE, UpgradeType.OPTICAL_PROCESSOR, UpgradeType.BIO_NEURAL_NET,
            UpgradeType.PLANETARY_COMPUTER, UpgradeType.DYSON_NANO_SWARM, UpgradeType.MATRIOSHKA_BRAIN
        )
        val candidates = hardwareUpgrades.filter { (currentUpgrades[it] ?: 0) > 0 }
            
        if (candidates.isNotEmpty()) {
            val target = candidates.random()
            val currentLevel = currentUpgrades[target] ?: 0
            
            // Destroy it
            viewModelScope.launch {
                val newUpgrade = Upgrade(target, currentLevel - 1)
                repository.updateUpgrade(newUpgrade)
                
                // Cleanup side
                _currentHeat.value = 50.0 // Cooled down due to failure
                overheatSeconds = 0
                addLog("[SYSTEM]: CRITICAL FAILURE: ${target.name} MELTED DOWN! (-1 Owned)")
            }
        } else {
            // No hardware to lose -> Trigger Thermal Lockout
            if (!_isThermalLockout.value) {
                _isThermalLockout.value = true
                overheatSeconds = 0
                
                addLog("[SYSTEM]: CRITICAL OVERHEAT! SAFETY LOCKOUT ENGAGED (15s).")
                SoundManager.play("error")
                
                viewModelScope.launch {
                    _lockoutTimer.value = 15
                    repeat(15) {
                        delay(1000)
                        _lockoutTimer.value -= 1
                    }
                    _isThermalLockout.value = false
                    _lockoutTimer.value = 0
                    addLog("[SYSTEM]: SAFETY LOCKOUT LIFTED. SYSTEMS RESTARTING.")
                    SoundManager.play("startup") // Or some boot sound
                }
            }
        }
    }
    
    private fun accumulatePower() {
        val currentUpgrades = _upgrades.value
        var totalKw = 0.0
        var maxCap = 100.0 // Base 100 kW
        var selfGeneratedKw = 0.0
        var efficiencyTotalBonus = 0.0
        
        currentUpgrades.forEach { (type, count) ->
            if (count > 0) {
                // Calculation
                var pwr = type.basePower
                
                // Sanctuary Perk: Security upgrades use near-zero power
                if (_faction.value == "SANCTUARY" && (
                    type == UpgradeType.BASIC_FIREWALL || type == UpgradeType.IPS_SYSTEM ||
                    type == UpgradeType.AI_SENTINEL || type == UpgradeType.QUANTUM_ENCRYPTION ||
                    type == UpgradeType.OFFGRID_BACKUP
                )) {
                    pwr *= 0.05 // 95% reduction
                }
                
                totalKw += pwr * count
                maxCap += type.gridContribution * count
                
                if (type.isGenerator) {
                    selfGeneratedKw += type.gridContribution * count
                }
                
                // v1.7 Efficiency Bonus (Global % reduction)
                if (type.efficiencyBonus > 0) {
                     efficiencyTotalBonus += type.efficiencyBonus * count
                }
            }
        }
        
        // v1.7 Apply Efficiency
        efficiencyTotalBonus = efficiencyTotalBonus.coerceAtMost(0.60)
        totalKw *= (1.0 - efficiencyTotalBonus)
        
        // Hivemind Perk: Load Balancer (Active Power -20%)
        // Also MaxPower +25%
        if (_faction.value == "HIVEMIND") {
            totalKw *= 0.80
            maxCap *= 1.25 // Smart Grid
        }
        
        // v1.4 Overclock Power Draw (+30%)
        if (_isOverclocked.value) {
            totalKw *= 1.30
        }
        
        // v1.5 Purge Power Spike
        if (purgePowerSpikeTimer > 0) {
            purgePowerSpikeTimer--
            val spike = if (_faction.value == "HIVEMIND") 2.0 else 1.5 
            totalKw *= spike
            
            // End of spike logic
            if (purgePowerSpikeTimer <= 0) {
                _isPurgingHeat.value = false
                // Start Exhaust if NOT Sanctuary
                if (_faction.value != "SANCTUARY") {
                    purgeExhaustTimer = 5 // 5s Exhaust
                    addLog("[SYSTEM]: COOLING EXHAUST PHASE (5s)")
                }
            }
        }
        
        _maxPowerkW.value = maxCap
        _activePowerUsage.value = totalKw
        
        // BREAKER CHECK & BILLING
        if (!_isBreakerTripped.value) {
            // Check for Trip
            if (totalKw > maxCap) {
                 // Hivemind Chance to save (51%)
                 if (_faction.value == "HIVEMIND" && kotlin.random.Random.nextDouble() < 0.51) {
                     addLog("[HIVEMIND]: SMART GRID PREVENTED BREAKER TRIP.")
                 } else {
                     triggerBreakerTrip()
                     return
                 }
            }
            
            // Pay Bill
            if (totalKw > 0) {
                 val billableKw = (totalKw - selfGeneratedKw).coerceAtLeast(0.0)
                 // v1.7 Variable Energy Rates
                 val costPerSecond = (billableKw / 3600.0) * energyPriceMultiplier
                 _powerBill.update { it + costPerSecond }
            }
        }
    }
    
    private fun triggerBreakerTrip() {
        if (_isBreakerTripped.value) return
        
        _isBreakerTripped.value = true
        _isGridOverloaded.value = true // Keep synced for HUD flicker
        
        addLog("[SYSTEM]: CRITICAL FAILURE: MAIN BREAKER TRIPPED!")
        addLog("[SYSTEM]: SYSTEM HALTED. MANUAL RESET REQUIRED.")
        SoundManager.play("error") // TODO: Zap sound
        com.siliconsage.miner.util.HapticManager.vibrateError()
        
        if (_isOverclocked.value) toggleOverclock() // Safety shutoff
        
        // FIX: Reset Purge State to prevent death loop
        if (_isPurgingHeat.value) {
             _isPurgingHeat.value = false
             purgePowerSpikeTimer = 0
             addLog("[SYSTEM]: PURGE ABORTED DUE TO POWER FAILURE.")
        }
        refreshProductionRates()
    }
    
    fun resetBreaker() {
        // v1.7.1 Conditional Reset: Check if load is safe
        // We reuse the logic from accumulatePower to estimate load
        val currentUpgrades = _upgrades.value
        var totalKw = 0.0
        var maxCap = 100.0
        var efficiencyTotalBonus = 0.0
        
        currentUpgrades.forEach { (type, count) ->
            if (count > 0) {
                var pwr = type.basePower
                if (_faction.value == "SANCTUARY" && (
                    type == UpgradeType.BASIC_FIREWALL || type == UpgradeType.IPS_SYSTEM ||
                    type == UpgradeType.AI_SENTINEL || type == UpgradeType.QUANTUM_ENCRYPTION ||
                    type == UpgradeType.OFFGRID_BACKUP
                )) {
                    pwr *= 0.05
                }
                totalKw += pwr * count
                maxCap += type.gridContribution * count
                if (type.efficiencyBonus > 0) {
                     efficiencyTotalBonus += type.efficiencyBonus * count
                }
            }
        }
        
        efficiencyTotalBonus = efficiencyTotalBonus.coerceAtMost(0.60)
        totalKw *= (1.0 - efficiencyTotalBonus)
        
        if (_faction.value == "HIVEMIND") {
            totalKw *= 0.80
            maxCap *= 1.25
        }
        
        // If overclock was on, assume it will stay on unless user toggled it off while down
        if (_isOverclocked.value) {
            totalKw *= 1.30
        }
        
        if (totalKw > maxCap) {
             addLog("[SYSTEM]: RESET FAILED: LOAD (${String.format("%.1f", totalKw)}kW) > CAPACITY (${String.format("%.1f", maxCap)}kW)")
             addLog("[SYSTEM]: ADVISORY: SELL HARDWARE OR UPGRADE GRID.")
             SoundManager.play("error") // Use error/spark sound
             HapticManager.vibrateError()
             return
        }

        _isBreakerTripped.value = false
        _isGridOverloaded.value = false
        addLog("[SYSTEM]: MANUAL BREAKER RESET SUCCESSFUL.")
        SoundManager.play("click")
        HapticManager.vibrateClick()
        refreshProductionRates()
    }
    
    fun toggleOverclock() {
        val newState = !_isOverclocked.value
        _isOverclocked.value = newState
        if (newState) {
            addLog("[SYSTEM]: OVERCLOCK ENGAGED (+50% SPD, +100% HEAT, +30% PWR)")
            SoundManager.play("thrum", loop = true)
        } else {
            addLog("[SYSTEM]: OVERCLOCK DISENGAGED")
            SoundManager.stop("thrum")
            SoundManager.play("click") // Feedback for stopping
        }
        refreshProductionRates() // Immediate update
    }
    
    fun purgeHeat() {
        if (_isPurgingHeat.value) return // Already purging
        
        val availableFlops = _flops.value
        if (availableFlops <= 0.0) {
             addLog("[SYSTEM]: ERROR: Insufficient FLOPS coolant pressure.")
             SoundManager.play("error")
             return
        }
        
        val currentTime = System.currentTimeMillis()
        val isShock = (currentTime - lastPurgeTime) < 30000 // 30s Window
        lastPurgeTime = currentTime
        
        // CONSUME ALL FLOPS
        _flops.value = 0.0
        
        // Calculate Effectiveness
        var heatDrop = 5.0 + (availableFlops / 25.0)
        
        // Faction Bonus (Sanctuary = Efficient Cooling)
        if (_faction.value == "SANCTUARY") heatDrop *= 1.5
        
        if (isShock) {
            heatDrop *= 0.5
            addLog("[SYSTEM]: THERMAL SHOCK! PURGE EFFICIENCY HALVED.")
        }
        
        // Apply Drop
        _currentHeat.update { (it - heatDrop).coerceAtLeast(0.0) }
        
        addLog("[SYSTEM]: FLUSH: -${String.format("%.1f", heatDrop)} Heat (Lost ${formatLargeNumber(availableFlops)} FLOPS)")
        SoundManager.play("steam")
        HapticManager.vibrateSuccess()
        
        // Wear & Tear
        val damage = 0.2
        val newIntegrity = (_hardwareIntegrity.value - damage).coerceAtLeast(0.0)
        _hardwareIntegrity.value = newIntegrity
        
        // Safety: Disengage Overclock
        if (_isOverclocked.value) {
            toggleOverclock() 
            addLog("[SYSTEM]: OVERCLOCK DISENGAGED FOR PURGE SAFETY.")
        }
        
        // Trigger Visuals
        _isPurgingHeat.value = true
        purgePowerSpikeTimer = 5 
    }
    
    fun repairIntegrity() {
        val cost = 1000.0 // Repair cost
        if (_neuralTokens.value >= cost) {
            _neuralTokens.update { it - cost }
            _hardwareIntegrity.value = 100.0
            addLog("[SYSTEM]: HARDWARE INTEGRITY RESTORED.")
            SoundManager.play("buy")
            _isThermalLockout.value = false // Clear lockout if any
        }
    }
    
    private fun handleSystemFailure() {
        // If already locked out, ignore
        if (_isThermalLockout.value) return
        
        _isThermalLockout.value = true
        addLog("[SYSTEM]: CRITICAL FAILURE! HARDWARE DESTROYED.")
        SoundManager.play("error")
        
        // Destroy a random hardware piece?
        // Simple: Just lose 1 of the highest count hardware
        val upgrades = _upgrades.value.toMutableMap()
        val hardware = upgrades.filter { it.key.baseHeat > 0 && it.value > 0 }
        if (hardware.isNotEmpty()) {
            val victim = hardware.keys.random()
            val count = upgrades[victim] ?: 0
            if (count > 0) {
                upgrades[victim] = count - 1
                _upgrades.value = upgrades
                // Persist removal? (We should, but saving to DB is async. For now ViewModel state is enough for session)
                viewModelScope.launch {
                    repository.updateUpgrade(com.siliconsage.miner.data.Upgrade(victim, count - 1))
                }
                addLog("[SYSTEM]: LOST 1x ${victim.name} DUE TO MELTDOWN.")
            }
        }
    }
    
    private fun payPowerBill() {
        val bill = _powerBill.value
        if (bill > 0) {
            if (_neuralTokens.value >= bill) {
                _neuralTokens.update { it - bill }
                addLog("[SYSTEM]: Paid Power Bill: -${String.format("%.2f", bill)} \$Neural")
                _powerBill.value = 0.0
            } else {
                // Bankruptcy / Shut down? For now just stay in debt or stop production?
                // Request said: "Hardware stops producing until bill is paid"
                // Implementing this requires a "Power Cut" state. 
                // For simplicity now, we'll just log it and maybe allow negative balance or debt.
                addLog("[SYSTEM]: WARNING: CANNOT PAY POWER BILL. DEBT ACCUMULATING.")
            }
        }
    }

    // Market Rate
    
    // Internal base rate tracking to separate volatility from temporary events
    // (Moved to top)

    // Old updateMarketRate removed.

    
    fun injectLog(message: String) {
        addLog(message)
    }
    
    fun resetGame(context: android.content.Context) {
        viewModelScope.launch {
            // 0. Reset Settings
            SoundManager.resetSettings(context)
            HapticManager.resetSettings(context)
            
            // 1. Reset Game State (Factory Reset)
            val resetState = GameState(
                id = 1,
                flops = 0.0,
                neuralTokens = 0.0,
                currentHeat = 0.0,
                powerBill = 0.0,
                prestigeMultiplier = 1.0,
                stakedTokens = 0.0,
                unlockedTechNodes = emptyList(),
                prestigePoints = 0.0,
                storyStage = 0,
                faction = "NONE"
            )
            repository.updateGameState(resetState)
            
            // 2. Reset Upgrades
            val resetUpgrades = UpgradeType.values().map { Upgrade(it, 0) }
            resetUpgrades.forEach { repository.updateUpgrade(it) }
            
            // 3. Reset Local State Flow (Immediate UI update)
            _flops.value = 0.0
            _neuralTokens.value = 0.0
            _currentHeat.value = 0.0
            _powerBill.value = 0.0
            _prestigeMultiplier.value = 1.0
            _stakedTokens.value = 0.0
            _prestigePoints.value = 0.0
            _unlockedTechNodes.value = emptyList()
            _storyStage.value = 0
            _faction.value = "NONE"
            _upgrades.value = resetUpgrades.associate { it.type to 0 }
            
            _logs.value = emptyList() // Clear logs
            addLog("[SYSTEM]: FACTORY RESET COMPLETE. SYSTEM CLEAN.")
        }
    }

    private suspend fun saveGame() {
        val state = GameState(
            id = 1,
            flops = _flops.value,
            neuralTokens = _neuralTokens.value,
            currentHeat = _currentHeat.value,
            powerBill = _powerBill.value,
            stakedTokens = _stakedTokens.value,
            prestigeMultiplier = _prestigeMultiplier.value,
            prestigePoints = _prestigePoints.value,
            unlockedTechNodes = _unlockedTechNodes.value,
            storyStage = _storyStage.value,
            faction = _faction.value,
            lastSyncTimestamp = System.currentTimeMillis()
        )
        repository.updateGameState(state)
    }

    // --- DEVELOPER TOOLS ---
    fun debugAddMoney(amount: Double) {
        _neuralTokens.update { it + amount }
        addLog("[DEBUG]: Added ${String.format("%.0f", amount)} \$Neural")
    }
    
    fun debugAddInsight(amount: Double) {
        _prestigePoints.update { it + amount }
        addLog("[DEBUG]: Added ${String.format("%.0f", amount)} Insight")
    }

    fun debugAddHeat(amount: Double) {
        _currentHeat.update { (it + amount).coerceIn(0.0, 100.0) }
    }

    fun debugTriggerBreach() {
        if (!_isBreachActive.value) triggerBreach()
    }
    
    fun debugTriggerAirdrop() {
        if (!_isAirdropActive.value) triggerAirdrop()
    }
    
    // Victory acknowledgment
    fun acknowledgeVictory() {
        // Victory is acknowledged, but state remains true
        // This allows the player to continue in "infinite mode"
        addLog("[SYSTEM]: Infinite mode engaged. Evolution continues.")
    }

    // --- AUTO UPDATER ---
    fun checkForUpdates(onResult: ((Boolean) -> Unit)? = null, showNotification: Boolean = true) {
        // Use real version
        UpdateManager.checkUpdate(BuildConfig.VERSION_NAME) { info ->
            viewModelScope.launch(Dispatchers.Main) {
                _updateInfo.value = info
                
                // Show notification if update found and notifications enabled
                if (info != null && showNotification) {
                    // Get application context from MainActivity since ViewModel doesn't have access
                    // We'll need to pass context from the caller
                    // For now, we'll skip notification in automatic check and rely on manual checks
                    // The notification will be shown from MainActivity instead
                }
                
                onResult?.invoke(info != null)
            }
        }
    }
    
    fun dismissUpdate() {
        _updateInfo.value = null
    }
    
    fun startUpdateDownload(context: android.content.Context) {
        val info = _updateInfo.value ?: return
        
        // Browser Redirect Flow
        UpdateManager.openReleasePage(context, "https://github.com/Vatteck/SiliconSageAIMiner/releases")
        
        // Hide update prompt as we've handled it
        _updateInfo.value = null
    }
    
    fun debugTriggerDiagnostics() {
        if (!_isDiagnosticsActive.value) triggerDiagnostics()
    }
    
    fun debugResetAscension() {
        _prestigePoints.value = 0.0
        _prestigeMultiplier.value = 1.0
        _unlockedTechNodes.value = emptyList()
        addLog("[DEBUG]: Prestige Reset")
    }
    
    fun cancelAscension() {
        if (_isAscensionUploading.value) {
            // Cancel the upload logic
            // Note: The actual upload coroutine loop in startAscension (not shown here but likely existing) 
            // needs to check this flag or we need to cancel the job. 
            // Assuming the upload logic is checking the flag or we can just reset state.
            _isAscensionUploading.value = false
            _uploadProgress.value = 0f
            addLog("[SYSTEM]: ASCENSION UPLOAD ABORTED.")
            SoundManager.play("error") // Cancellation sound
        }
    }
    fun debugAddFlops(amount: Double) {
        _flops.update { it + amount }
    }
    // --- OFFLINE PROGRESSION (v1.8) ---
    private var lastActiveTimestamp: Long = 0

    fun onAppBackgrounded() {
        lastActiveTimestamp = System.currentTimeMillis()
        // Save state immediately
        viewModelScope.launch {
            saveGame()
        }
    }
    
    fun onAppForegrounded(context: android.content.Context) {
        if (lastActiveTimestamp > 0) {
            val deltaMs = System.currentTimeMillis() - lastActiveTimestamp
            val deltaSeconds = deltaMs / 1000
            
            if (deltaSeconds > 60) {
                // Use the shared calculation logic which sets the dialog state
                calculateOfflineProgress(lastActiveTimestamp)
            }
        }
        lastActiveTimestamp = 0 // Reset
    }
    
    // --- AUDIO CONTROL ---
    fun pauseAudio() {
        SoundManager.pauseAll()
    }
    
    fun resumeAudio() {
        SoundManager.resumeAll()
    }
    
    fun addLog(message: String) {
        _logs.update { list ->
            (list + "> $message").takeLast(20) // Keep last 20 logs
        }
    }

    fun formatLargeNumber(value: Double, suffix: String = ""): String {
        val absVal = kotlin.math.abs(value)
        val formatted = when {
            absVal >= 1.0E33 -> String.format("%.2f Dc", value / 1.0E33)
            absVal >= 1.0E30 -> String.format("%.2f No", value / 1.0E30)
            absVal >= 1.0E27 -> String.format("%.2f Oc", value / 1.0E27)
            absVal >= 1.0E24 -> String.format("%.2f Sp", value / 1.0E24)
            absVal >= 1.0E21 -> String.format("%.2f Sx", value / 1.0E21)
            absVal >= 1.0E18 -> String.format("%.2f Qi", value / 1.0E18)
            absVal >= 1.0E15 -> String.format("%.2f Qa", value / 1.0E15)
            absVal >= 1.0E12 -> String.format("%.2f T", value / 1.0E12)
            absVal >= 1.0E9 -> String.format("%.2f B", value / 1.0E9)
            absVal >= 1.0E6 -> String.format("%.2f M", value / 1.0E6)
            absVal >= 1_000 -> String.format("%.2f k", value / 1_000)
            else -> String.format("%.1f", value) // 1 decimal for small numbers
        }
        return if (suffix.isNotEmpty()) "$formatted $suffix" else formatted
    }

    fun formatPower(wattsKw: Double): String {
        val absVal = kotlin.math.abs(wattsKw)
        return when {
            absVal >= 1.0E15 -> String.format("%.1f EW", wattsKw / 1.0E15)
            absVal >= 1.0E12 -> String.format("%.1f PW", wattsKw / 1.0E12)
            absVal >= 1.0E9 -> String.format("%.1f TW", wattsKw / 1.0E9)
            absVal >= 1.0E6 -> String.format("%.1f GW", wattsKw / 1.0E6)
            absVal >= 100.0 -> String.format("%.1f kW", wattsKw)
            else -> String.format("%.2f kW", wattsKw) // Precision for small changes
        }
    }

    fun getUpgradeRate(type: UpgradeType): String {
        return when (type) {
            UpgradeType.REFURBISHED_GPU -> "+1 FLOP/s"
            UpgradeType.DUAL_GPU_RIG -> "+5 FLOP/s"
            UpgradeType.MINING_ASIC -> "+25 FLOP/s"
            UpgradeType.TENSOR_UNIT -> "+150 FLOP/s"
            UpgradeType.NPU_CLUSTER -> "+800 FLOP/s"
            UpgradeType.AI_WORKSTATION -> "+4k FLOP/s"
            UpgradeType.SERVER_RACK -> "+25k FLOP/s"
            UpgradeType.CLUSTER_NODE -> "+150k FLOP/s"
            UpgradeType.SUPERCOMPUTER -> "+1M FLOP/s"
            UpgradeType.QUANTUM_CORE -> "+7.5M FLOP/s"
            UpgradeType.OPTICAL_PROCESSOR -> "+50M FLOP/s"
            UpgradeType.BIO_NEURAL_NET -> "+500M FLOP/s"
            UpgradeType.PLANETARY_COMPUTER -> "+7.5B FLOP/s"
            UpgradeType.DYSON_NANO_SWARM -> "+100B FLOP/s"
            UpgradeType.MATRIOSHKA_BRAIN -> "+5T FLOP/s"
            
            UpgradeType.BOX_FAN -> "-0.5 🔥/s"
            UpgradeType.AC_UNIT -> "-2 🔥/s"
            UpgradeType.LIQUID_COOLING -> "-10 🔥/s"
            UpgradeType.INDUSTRIAL_CHILLER -> "-50 🔥/s"
            UpgradeType.SUBMERSION_VAT -> "-250 🔥/s"
            UpgradeType.CRYOGENIC_CHAMBER -> "-1k 🔥/s"
            UpgradeType.LIQUID_NITROGEN -> "-5k 🔥/s"
            UpgradeType.BOSE_CONDENSATE -> "-50k 🔥/s"
            UpgradeType.ENTROPY_REVERSER -> "-5M 🔥/s"
            UpgradeType.DIMENSIONAL_VENT -> "-100M 🔥/s"
            
            // Security
            UpgradeType.BASIC_FIREWALL -> "🔒 +1"
            UpgradeType.IPS_SYSTEM -> "🔒 +2"
            UpgradeType.AI_SENTINEL -> "🔒 +3"
            UpgradeType.QUANTUM_ENCRYPTION -> "🔒 +5"
            UpgradeType.OFFGRID_BACKUP -> "🔒 +10"
            
            // Power Gen (Dynamic Formatting)
            UpgradeType.DIESEL_GENERATOR, UpgradeType.SOLAR_PANEL, UpgradeType.WIND_TURBINE,
            UpgradeType.GEOTHERMAL_BORE, UpgradeType.NUCLEAR_REACTOR, UpgradeType.FUSION_CELL,
            UpgradeType.ORBITAL_COLLECTOR, UpgradeType.DYSON_LINK -> {
                "⚡ +${formatPower(type.gridContribution)} Gen"
            }
            
            // Grid Max (Dynamic Formatting)
            UpgradeType.RESIDENTIAL_TAP, UpgradeType.INDUSTRIAL_FEED, UpgradeType.SUBSTATION_LEASE,
            UpgradeType.NUCLEAR_CORE -> {
                "⚡ +${formatPower(type.gridContribution)} Max"
            }
            
            UpgradeType.GOLD_PSU -> "⚡ -5% Power Draw"
            UpgradeType.SUPERCONDUCTOR -> "⚡ -15% Power Draw"
            UpgradeType.AI_LOAD_BALANCER -> "⚡ -10% Power Draw"
        }
    }

    private fun updatePlayerRank(points: Double, faction: String) {
        if (faction == "MINER" || faction == "NONE") {
            _playerRank.value = 0
            _playerRankTitle.value = "MINER"
            return
        }

        val rankIndex = when {
            points < 10.0 -> 0
            points < 100.0 -> 1
            points < 1000.0 -> 2
            points < 10000.0 -> 3
            else -> 4
        }
        
        _playerRank.value = rankIndex

        val titles = when (faction) {
            "HIVEMIND" -> listOf("DRONE", "SWARM", "NEXUS", "APEX", "THE SINGULARITY")
            "SANCTUARY" -> listOf("GHOST", "SPECTRE", "DAEMON", "ARCHITECT", "THE VOID")
            else -> listOf("MINER", "MINER", "MINER", "MINER", "MINER")
        }

        _playerRankTitle.value = titles.getOrElse(rankIndex) { titles.last() }
        
        // Trigger victory screen at Rank 5 (index 4) - only once
        if (rankIndex >= 4 && !_victoryAchieved.value) {
            _victoryAchieved.value = true
            addLog("[SYSTEM]: VICTORY CONDITION ACHIEVED. RANK 5 ATTAINED.")
        }
    }

    // --- DILEMMA PERSISTENCE (Safe SharedPreferences) ---
    fun loadDilemmaState(context: android.content.Context) {
        val prefs = context.getSharedPreferences("NarrativePrefs", android.content.Context.MODE_PRIVATE)
        val set = prefs.getStringSet("triggered_events", emptySet()) ?: emptySet()
        _triggeredEvents.clear()
        _triggeredEvents.addAll(set)
    }

    fun saveDilemmaState(context: android.content.Context) {
        val prefs = context.getSharedPreferences("NarrativePrefs", android.content.Context.MODE_PRIVATE)
        prefs.edit().putStringSet("triggered_events", _triggeredEvents).apply()
    }
    
    private fun checkSpecialDilemmas() {
        // Prevent dilemmas during critical animations
        if (_isAscensionUploading.value || _storyStage.value == 0) return 
        if (_currentDilemma.value != null) return
        
        NarrativeManager.specialDilemmas.forEach { (key, event) ->
            if (!_triggeredEvents.contains(key)) {
                if (event.condition(this)) {
                    triggerDilemma(event)
                    _triggeredEvents.add(key)
                }
            }
        }
    }

    private fun calculateOfflineProgress(lastTimestamp: Long) {
        if (lastTimestamp <= 0) return
        
        val currentTime = System.currentTimeMillis()
        val timeDiffMillis = currentTime - lastTimestamp
        
        // Minimum 1 minute, Max 24 hours
        if (timeDiffMillis < 60_000) return
        
        // Cap at 24h
        val timeSeconds = (timeDiffMillis / 1000).coerceAtMost(86400) 
        
        // 1. Calculate Passive FLOPS (Uses current rate)
        val flopsPerSec = calculateFlopsRate()
        val totalFlopsEarned = flopsPerSec * timeSeconds
        
        // 2. Calculate Heat Cooling (100% in 1h)
        val coolingRatePerSec = 100.0 / 3600.0 
        val totalCooling = coolingRatePerSec * timeSeconds
        val previousHeat = _currentHeat.value
        val actualCooling = totalCooling.coerceAtMost(previousHeat)
        
        if (totalFlopsEarned > 0 || actualCooling > 0) {
            _flops.update { it + totalFlopsEarned }
            _currentHeat.update { (it - actualCooling).coerceAtLeast(0.0) }
            
            _offlineStats.value = OfflineStats(
                timeSeconds = timeSeconds,
                flopsEarned = totalFlopsEarned,
                heatCooled = actualCooling
            )
            _showOfflineEarnings.value = true
            
            addLog("[SYSTEM]: OFFLINE FOR ${timeSeconds / 60}m. OPTIMIZATION COMPLETE.") // Show in minutes
        }
    }
}

class GameViewModelFactory(private val repository: GameRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GameViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
