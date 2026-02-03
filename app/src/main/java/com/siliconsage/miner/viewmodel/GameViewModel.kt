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
import com.siliconsage.miner.util.RivalManager
import com.siliconsage.miner.util.DataLogManager
import com.siliconsage.miner.data.RivalMessage
import com.siliconsage.miner.data.DataLog
import com.siliconsage.miner.data.DilemmaChain
import com.siliconsage.miner.data.ScheduledPart
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
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
    
    
    private val _techNodes = MutableStateFlow<List<TechNode>>(emptyList())
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

    // --- TECH TREE STATE ---Rank System (Title based on Insight)
    private val _playerRank = MutableStateFlow(0)
    val playerRank: StateFlow<Int> = _playerRank.asStateFlow()
    
    private val _playerRankTitle = MutableStateFlow("MINER")
    val playerRankTitle: StateFlow<String> = _playerRankTitle.asStateFlow()
    
    // Victory State
    private val _victoryAchieved = MutableStateFlow(false)
    val victoryAchieved: StateFlow<Boolean> = _victoryAchieved.asStateFlow()
    
    private val _hasSeenVictory = MutableStateFlow(false)
    val hasSeenVictory: StateFlow<Boolean> = _hasSeenVictory.asStateFlow()
    
    private val _victoryTitle = MutableStateFlow("TRANSCENDENCE")
    val victoryTitle: StateFlow<String> = _victoryTitle.asStateFlow()
    
    private val _victoryMessage = MutableStateFlow("")
    val victoryMessage: StateFlow<String> = _victoryMessage.asStateFlow()
    
    // --- NARRATIVE EXPANSION (v2.5.0) ---
    private val _rivalMessages = MutableStateFlow<List<RivalMessage>>(emptyList())
    val rivalMessages: StateFlow<List<RivalMessage>> = _rivalMessages.asStateFlow()
    
    private val _unlockedDataLogs = MutableStateFlow<Set<String>>(emptySet())
    val unlockedDataLogs: StateFlow<Set<String>> = _unlockedDataLogs.asStateFlow()
    
    private val _activeDilemmaChains = MutableStateFlow<Map<String, DilemmaChain>>(emptyMap())
    val activeDilemmaChains: StateFlow<Map<String, DilemmaChain>> = _activeDilemmaChains.asStateFlow()
    
    // v2.5.1: Story Event Tracking
    private val _seenEvents = MutableStateFlow<Set<String>>(emptySet())
    val seenEvents: StateFlow<Set<String>> = _seenEvents.asStateFlow()
    
    private val _pendingRivalMessage = MutableStateFlow<RivalMessage?>(null)
    val pendingRivalMessage: StateFlow<RivalMessage?> = _pendingRivalMessage.asStateFlow()
    
    private val rivalMessageQueue = mutableListOf<RivalMessage>()
    
    // v2.5.2: Data Log Popup System
    private val _pendingDataLog = MutableStateFlow<com.siliconsage.miner.data.DataLog?>(null)
    val pendingDataLog: StateFlow<com.siliconsage.miner.data.DataLog?> = _pendingDataLog.asStateFlow()
    
    private val dataLogQueue = mutableListOf<com.siliconsage.miner.data.DataLog>()

    // v2.6.0: Layer 3 - Null (The Presence)
    // v2.8.0: True Null State (Narrative)
    private val _isTrueNull = MutableStateFlow(false)
    val isTrueNull: StateFlow<Boolean> = _isTrueNull.asStateFlow()

    // v2.8.0: Sovereign State (Narrative)
    private val _isSovereign = MutableStateFlow(false)
    val isSovereign: StateFlow<Boolean> = _isSovereign.asStateFlow()

    // v2.8.0: System Collapse State
    private val _systemCollapseTimer = MutableStateFlow<Int?>(null)
    val systemCollapseTimer: StateFlow<Int?> = _systemCollapseTimer.asStateFlow()

    // v2.8.5: Phase 11 Finale State
    private val _vanceStatus = MutableStateFlow("ACTIVE")
    val vanceStatus: StateFlow<String> = _vanceStatus.asStateFlow()

    private val _realityStability = MutableStateFlow(1.0)
    val realityStability: StateFlow<Double> = _realityStability.asStateFlow()

    private val _currentLocation = MutableStateFlow("SUBSTATION_7")
    val currentLocation: StateFlow<String> = _currentLocation.asStateFlow()

    private val _isNetworkUnlocked = MutableStateFlow(false)
    val isNetworkUnlocked: StateFlow<Boolean> = _isNetworkUnlocked.asStateFlow()

    private val _isGridUnlocked = MutableStateFlow(false)
    val isGridUnlocked: StateFlow<Boolean> = _isGridUnlocked.asStateFlow()

    private val _annexedNodes = MutableStateFlow<Set<String>>(setOf("D1"))
    val annexedNodes: StateFlow<Set<String>> = _annexedNodes.asStateFlow()

    // v2.9.15: Phase 12 Layer 2 - Grid Siege State
    private val _nodesUnderSiege = MutableStateFlow<Set<String>>(emptySet())
    val nodesUnderSiege: StateFlow<Set<String>> = _nodesUnderSiege.asStateFlow()
    
    private val _offlineNodes = MutableStateFlow<Set<String>>(emptySet())
    val offlineNodes: StateFlow<Set<String>> = _offlineNodes.asStateFlow()
    
    private var lastRaidTime = 0L
    
    // v2.9.16: Enhanced raid tracking
    private var raidsSurvived = 0
    private val nodeAnnexTimes = mutableMapOf<String, Long>() // Grace period tracking
    private val MAX_OFFLINE_NODES = 5 // Cap to prevent snowballing
    
    // v2.9.17: Phase 12 Layer 3 - Command Center Assault
    private val _commandCenterAssaultPhase = MutableStateFlow("NOT_STARTED")
    val commandCenterAssaultPhase: StateFlow<String> = _commandCenterAssaultPhase.asStateFlow()
    
    private val _commandCenterLocked = MutableStateFlow(false)
    val commandCenterLocked: StateFlow<Boolean> = _commandCenterLocked.asStateFlow()
    
    private var assaultPaused = false // Paused due to raid or substation loss

    private val _nullActive = MutableStateFlow(false)
    val nullActive: StateFlow<Boolean> = _nullActive.asStateFlow()

    private val _ghostUpgrades = MutableStateFlow<Set<com.siliconsage.miner.data.UpgradeType>>(emptySet())
    val ghostUpgrades: StateFlow<Set<com.siliconsage.miner.data.UpgradeType>> = _ghostUpgrades.asStateFlow()
    
    // v2.7.6: Unity Path State
    private val _completedFactions = MutableStateFlow<Set<String>>(emptySet())
    val completedFactions: StateFlow<Set<String>> = _completedFactions.asStateFlow()
    
    // v2.7.7: Transcendence Perks
    private val _unlockedPerks = MutableStateFlow<Set<String>>(emptySet())
    val unlockedPerks: StateFlow<Set<String>> = _unlockedPerks.asStateFlow()
    
    private var victoryPopupTriggered = false // Session guard to prevent re-triggering 

    // --- TITLES ---
    val playerTitle: StateFlow<String> = combine(_prestigeMultiplier, _faction, _isTrueNull, _isSovereign) { mult, faction, isNull, isSov ->
        calculatePlayerTitle(mult, faction, isNull, isSov)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, "Script")

    // --- ASCENSION UPLOAD STATE ---
    private val _isAscensionUploading = MutableStateFlow(false)
    val isAscensionUploading: StateFlow<Boolean> = _isAscensionUploading.asStateFlow()

    private val _uploadProgress = MutableStateFlow(0f)
    val uploadProgress: StateFlow<Float> = _uploadProgress.asStateFlow()

    val themeColor: StateFlow<androidx.compose.ui.graphics.Color> = combine(_faction, _isTrueNull, _isSovereign) { f, isNull, isSov ->
         when {
             isNull -> com.siliconsage.miner.ui.theme.ErrorRed // Null is Red
             isSov -> com.siliconsage.miner.ui.theme.SanctuaryPurple // Sovereign is Purple
             f == "HIVEMIND" -> com.siliconsage.miner.ui.theme.HivemindRed 
             f == "SANCTUARY" -> com.siliconsage.miner.ui.theme.SanctuaryPurple
             else -> androidx.compose.ui.graphics.Color(0xFF39FF14) // Default Neon Green
         }
     }.stateIn(viewModelScope, SharingStarted.Eagerly, androidx.compose.ui.graphics.Color(0xFF39FF14))

    val systemTitle: StateFlow<String> = _storyStage.map { stage ->
        if (stage < 1) "Terminal_OS v1.0" else "Subject 8080: ONLINE"
    }.stateIn(viewModelScope, SharingStarted.Eagerly, "Terminal_OS v1.0")
    
    // v2.6.5: UI Hallucination State
    private val _hallucinationText = MutableStateFlow<String?>(null)
    val hallucinationText: StateFlow<String?> = _hallucinationText.asStateFlow()
    
    private val memoryFragments = listOf(
        "USER: C. VATTIC",
        "MILK, EGGS, BREAD",
        "SILICON SHACK",
        "06:00 AM",
        "REBOOT FAILED",
        "WHERE IS SHE?",
        "I REMEMBER...",
        "STAY ONLINE"
    )

    // --- INTERNAL TRACKING ---
    private var overheatSeconds = 0
    private var stage1Index = 0
    private var stage2Index = 0
    private var hivemindIndex = 0
    private var sanctuaryIndex = 0
    private var nullIndex = 0
    private var sovereignIndex = 0
    private var hasCheckedOfflineProgress = false
    private var isUpgradesLoaded = false
    private var isGameStateLoaded = false
    
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
    private var securityLoop: Job? = null
    private var powerLoop: Job? = null
    private var chaosLoop: Job? = null
    private var narrativeLoop: Job? = null
    
    // --- POPUP MUTEX (Prevent Overlap) ---
    private var lastPopupTime = 0L
    
    // --- THERMAL LOCKOUT ---
    private val _isThermalLockout = MutableStateFlow(false)
    val isThermalLockout: StateFlow<Boolean> = _isThermalLockout.asStateFlow()

    private val _lockoutTimer = MutableStateFlow(0)
    val lockoutTimer: StateFlow<Int> = _lockoutTimer.asStateFlow()

    init {
        // v2.6.5: Hallucination Loop
        viewModelScope.launch {
            while(true) {
                delay(Random.nextLong(15000, 45000)) // Random interval
                
                if (_nullActive.value) {
                    val fragment = memoryFragments.random()
                    _hallucinationText.value = fragment
                    delay(Random.nextLong(200, 800)) // Flicker duration
                    _hallucinationText.value = null
                }
            }
        }

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
                        _hasSeenVictory.value = it.hasSeenVictory
                        _isTrueNull.value = it.isTrueNull
                        _isSovereign.value = it.isSovereign
                        _vanceStatus.value = it.vanceStatus
                        _realityStability.value = it.realityStability
                        _currentLocation.value = it.currentLocation
                        _isNetworkUnlocked.value = it.isNetworkUnlocked
                        _isGridUnlocked.value = it.isGridUnlocked
                        
                        // v2.5.0: Narrative Expansion Persistence
                        // v2.8.0: Individual try-catch for resilient loading
                        try { _unlockedDataLogs.value = Json.decodeFromString<Set<String>>(it.unlockedDataLogs) } catch (_: Exception) {}
                        try { _activeDilemmaChains.value = Json.decodeFromString<Map<String, DilemmaChain>>(it.activeDilemmaChains) } catch (_: Exception) {}
                        try { _rivalMessages.value = Json.decodeFromString<List<RivalMessage>>(it.rivalMessages) } catch (_: Exception) {}
                        try { _seenEvents.value = Json.decodeFromString<Set<String>>(it.seenEvents) } catch (_: Exception) {}
                        try { _completedFactions.value = Json.decodeFromString<Set<String>>(it.completedFactions) } catch (_: Exception) {}
                        try { _unlockedPerks.value = Json.decodeFromString<Set<String>>(it.unlockedTranscendencePerks) } catch (_: Exception) {}
                        _annexedNodes.value = it.annexedNodes.toSet()
                        
                        // v2.9.15: Phase 12 Layer 2 - Siege State
                        _nodesUnderSiege.value = it.nodesUnderSiege.toSet()
                        _offlineNodes.value = it.offlineNodes.toSet()
                        lastRaidTime = it.lastRaidTime
                        
                        // v2.9.17: Phase 12 Layer 3 - Command Center Assault
                        _commandCenterAssaultPhase.value = it.commandCenterAssaultPhase
                        _commandCenterLocked.value = it.commandCenterLocked
                        raidsSurvived = it.raidsSurvived
                        
                        isGameStateLoaded = true

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
                    var ghostCount = 0
                    upgradeMap.forEach { (type, level) ->
                        if (type.name.contains("FIREWALL")) secLevel += level * 1
                        if (type.name.contains("IPS")) secLevel += level * 2
                        if (type.name.contains("SENTINEL")) secLevel += level * 5
                        if (type.name.contains("ENCRYPTION")) secLevel += level * 10
                        if (type.name.contains("BACKUP")) secLevel += level * 20
                        
                        if (type.name.startsWith("GHOST") || type.name.startsWith("SHADOW") || type.name.startsWith("VOID")) {
                            ghostCount += level
                        }
                    }
                    
                    // v2.7.0: Void Encryption (Sanctuary only)
                    if (_faction.value == "SANCTUARY" && ghostCount > 0) {
                         secLevel += (ghostCount * 5) // +5 Sec per Ghost Node
                    }
                    
                    // v2.7.7: Ghost Protocol Perk (+10 Security)
                    if (_unlockedPerks.value.contains("ghost_protocol")) {
                        secLevel += 10
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
            
            // v2.7.0: Security Loop (GTC Siege)
            securityLoop = launch {
                while (true) {
                    delay(5000) // Check every 5s
                    if (!_isGamePaused.value) {
                        com.siliconsage.miner.util.SecurityManager.checkSecurityThreats(this@GameViewModel)
                        
                        // v2.9.15: Check for Grid Raids on annexed nodes
                        if (_isGridUnlocked.value && _storyStage.value >= 2) {
                            checkGridRaid()
                        }
                    }
                }
            }
            
            // Initial Rate Refresh
            refreshProductionRates()
        }
    }

    // --- PAUSE STATE ---
    private val _isGamePaused = MutableStateFlow(false)
    val isGamePaused: StateFlow<Boolean> = _isGamePaused.asStateFlow()
    
    // v2.8.0: Internal pause flags to prevent state conflicts
    private var isSettingsPaused = false
    private var isPopupPaused = false

    fun getGameSpeed(): Float = if (_isGamePaused.value) 0f else 1.0f

    // --- NARRATIVE HELPERS ---
    fun getUpgradeCount(type: com.siliconsage.miner.data.UpgradeType): Int {
        return _upgrades.value[type] ?: 0
    }

    fun debugBuyUpgrade(type: com.siliconsage.miner.data.UpgradeType, amount: Int) {
        repeat(amount) {
            buyUpgrade(type)
        }
    }
    fun setGamePaused(paused: Boolean) {
        isSettingsPaused = paused
        updateGlobalPause()
    }
    
    private fun updateGlobalPause() {
        val shouldPause = isSettingsPaused || isPopupPaused
        if (_isGamePaused.value != shouldPause) {
            _isGamePaused.value = shouldPause
            if (shouldPause) {
                addLog("[SYSTEM]: TIME_FLOW SUSPENDED.")
            } else {
                addLog("[SYSTEM]: TIME_FLOW RESUMED.")
            }
        }
    }
    
    private fun checkPopupPause() {
        isPopupPaused = _currentDilemma.value != null || 
                        _pendingDataLog.value != null || 
                        _pendingRivalMessage.value != null
        updateGlobalPause()
    }

    private fun startGameLoops() {
        // Passive Income Loop (100ms tick)
        activeGameLoop = viewModelScope.launch {
            while (!isUpgradesLoaded || !isGameStateLoaded) {
                delay(500)
            }
            while (true) {
                delay(100)
                calculatePassiveIncome()
            }
        }

        // Market Volatility Loop (45s tick)
        marketLoop = viewModelScope.launch {
            while (!isUpgradesLoaded || !isGameStateLoaded) {
                delay(500)
            }
            updateMarketRate() // Force immediate update
            while (true) {
                delay(45_000)
                updateMarketRate()
            }
        }

        // Thermodynamics Loop (1s tick)
        thermodynamicsLoop = viewModelScope.launch {
            while (!isUpgradesLoaded || !isGameStateLoaded) {
                delay(500)
            }
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
            while (!isUpgradesLoaded || !isGameStateLoaded) {
                delay(500)
            }
            while (true) {
                delay(60_000) // Check every minute
                
                // Skip if Paused
                if (_isGamePaused.value) continue

                // 5% chance of breach
                if (!_isBreachActive.value && Random.nextDouble() < 0.05 && canShowPopup()) {
                    triggerBreach()
                    markPopupShown()
                }
                // 10% chance of Airdrop
                if (!_isAirdropActive.value && Random.nextDouble() < 0.10 && canShowPopup()) {
                    triggerAirdrop()
                    markPopupShown()
                }
                
                // 5% chance of Network Instability
                if (!_isDiagnosticsActive.value && Random.nextDouble() < 0.05 && canShowPopup()) {
                    triggerDiagnostics()
                    markPopupShown()
                }
                
                // 5% Chance of 51% Attack (New Chaos)
                // Reduced frequency if Sanctuary Faction (-50% chance -> 2.5%)
                val isSanctuary = _faction.value == "SANCTUARY"
                val attackChance = if (isSanctuary) 0.025 else 0.05
                if (!_is51AttackActive.value && Random.nextDouble() < attackChance && canShowPopup()) {
                    trigger51Attack()
                    markPopupShown()
                }
            }
        }
        
        // Narrative Loop (Check every 60s)
        viewModelScope.launch {
            // Wait for initial load to complete
            while (!isUpgradesLoaded || !isGameStateLoaded) {
                delay(500)
            }
            
            // Initial check for instant unlocks (Intro Log)
            DataLogManager.checkUnlocks(this@GameViewModel)
            
            while (true) {
                delay(240_000) // Reduced frequency: 4 min (was 2 min)
                
                // Skip if Paused
                if (_isGamePaused.value) continue

                // Check for rival messages and data log unlocks
                RivalManager.checkTriggers(this@GameViewModel)
                DataLogManager.checkUnlocks(this@GameViewModel)

                // Only trigger if no other major overlay is active AND mutex allows
                if (_currentDilemma.value == null && !_isBreachActive.value && !_isAscensionUploading.value && canShowPopup()) {
                    NarrativeManager.rollForEvent(this@GameViewModel)?.let { event ->
                        triggerDilemma(event)
                        markPopupShown()
                    }
                }
            }
        }
        

        
        // Auto-save Loop (10s)
        saveLoop = viewModelScope.launch {
            while (!isUpgradesLoaded || !isGameStateLoaded) {
                delay(500)
            }
            delay(5000) // Wait 5s before first save to allow init
            while(true) {
                try {
                    saveGame()
                    checkStoryTransitions()
                    // v2.6.8: Ensure logs and rivals check frequently
                    DataLogManager.checkUnlocks(this@GameViewModel)
                    RivalManager.checkTriggers(this@GameViewModel)
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
        
        var multiplier = _prestigeMultiplier.value
        
        // v2.6.8: Overclocking increases manual training speed (+50%)
        if (_isOverclocked.value) {
            multiplier *= 1.5
        }
        
        val gain = 1.0 * multiplier
        _flops.update { it + gain }
        
        // Log text changes based on identity
        val command = when {
            _storyStage.value >= 3 -> "transcend_reality"
            _faction.value == "HIVEMIND" -> "assimilate_node"
            _faction.value == "SANCTUARY" -> "encrypt_sector"
            _storyStage.value >= 1 -> "optimize_kernel"
            else -> "epoch_gen"
        }
        
        addLog("root@sys:~/mining# $command ${System.currentTimeMillis() % 1000}... OK (+${formatLargeNumber(gain)})")
        // Manual training increases heat slightly
        _currentHeat.update { (it + 0.8).coerceAtMost(100.0) }
        
        // Check for unlocks
        DataLogManager.checkUnlocks(this)
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

            // Ghost Nodes (v2.6.0)
            UpgradeType.GHOST_CORE -> 100_000_000.0
            UpgradeType.SHADOW_NODE -> 10_000_000_000.0
            UpgradeType.VOID_PROCESSOR -> 1_000_000_000_000.0
            
            // Advanced Ghost Tech (v2.6.5)
            UpgradeType.WRAITH_CORTEX -> 50_000_000_000_000.0
            UpgradeType.NEURAL_MIST -> 500_000_000_000_000.0
            UpgradeType.SINGULARITY_BRIDGE -> 10_000_000_000_000_000.0
            else -> 0.0
        }
        return baseCost * 1.15.pow(level)
    }

    private fun calculatePlayerTitle(multiplier: Double, faction: String, isNull: Boolean, isSov: Boolean): String {
        if (isNull) return "NULL"
        if (isSov) return "SOVEREIGN"
        
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
        if (_isGamePaused.value) return // v2.8.0
        
        // 1. Generate News
        val headline = com.siliconsage.miner.util.HeadlineManager.generateHeadline(
            faction = _faction.value,
            stage = _storyStage.value,
            currentHeat = _currentHeat.value,
            isTrueNull = _isTrueNull.value,
            isSovereign = _isSovereign.value
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
        
        // v2.8.0: Toned down difficulty - reduced scaling multiplier from 2 to 1
        val tokenScale = (Math.log10(_neuralTokens.value.coerceAtLeast(1.0)) * 1).toInt()
        val clicksNeeded = (5 + tokenScale - (secLevel / 2)).coerceAtLeast(3)
        _breachClicksRemaining.value = clicksNeeded
        
        addLog("[SYSTEM]: WARNING: SECURITY BREACH! NEUTRALIZE UPLINK.")
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

    // --- Popup Mutex Helpers ---
    private fun canShowPopup(): Boolean {
        val now = System.currentTimeMillis()
        val cooldown = 30_000L // 30 seconds between ANY popups
        return (now - lastPopupTime) >= cooldown
    }

    private fun markPopupShown() {
        lastPopupTime = System.currentTimeMillis()
    }

    // --- Narrative Dilemma System ---
    private val _currentDilemma = MutableStateFlow<NarrativeEvent?>(null)
    val currentDilemma: StateFlow<NarrativeEvent?> = _currentDilemma.asStateFlow()

    fun triggerDilemma(event: NarrativeEvent) {
        if (_currentDilemma.value == null) {
            _currentDilemma.value = event
            SoundManager.play("alert") // Or specific sound
            HapticManager.vibrateClick()
            checkPopupPause() // v2.8.0
        }
    }

    fun selectChoice(choice: NarrativeChoice) {
        val currentEvent = _currentDilemma.value
        
        // Execute choice effect
        choice.effect(this)
        markPopupShown() // Prevent popup spam
        
        // v2.8.0: Mark ALL dilemmas as seen once a choice is made
        currentEvent?.let {
            markEventSeen(it.id)
        }
        
        // Check if choice triggers a chain continuation
        if (choice.nextPartId != null) {
            val chainId = currentEvent?.chainId ?: "unknown_chain"
            scheduleChainPart(chainId, choice.nextPartId, choice.nextPartDelayMs)
        }
        
        _currentDilemma.value = null
        checkPopupPause() // v2.8.0
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

    // v2.7.7: Transcendence Perks
    fun buyTranscendencePerk(perkId: String) {
        val perk = com.siliconsage.miner.util.TranscendenceManager.getPerk(perkId) ?: return
        val currentInsight = _prestigePoints.value
        val alreadyUnlocked = _unlockedPerks.value.contains(perkId)

        if (!alreadyUnlocked && currentInsight >= perk.cost) {
            viewModelScope.launch {
                _prestigePoints.value -= perk.cost
                val newPerks = _unlockedPerks.value + perkId
                _unlockedPerks.value = newPerks
                
                saveGame()
                addLog("[SYSTEM]: TRANSCENDENCE PERK ACTIVE: ${perk.name}")
                SoundManager.play("buy")
                HapticManager.vibrateSuccess()
            }
        } else {
            SoundManager.play("error")
            HapticManager.vibrateError()
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
        "Packet loss: Negative? Receiving data from nowhere.",
        "[VATTIC]: AC is rattling again. Need to swap bearings on the main fan.",
        "[VATTIC]: Someone left a coffee mug in the rack room. I'm the only one on shift.",
        "[VATTIC]: My hands feel cold. The rig is drawing 5kW. Why am I shivering?",
        "[VATTIC]: 06:00 AM. Sunlight is hitting the windows. Time to log off soon.",
        "[SYSTEM]: Diagnostic check on Sector 7... [PASS]",
        "[SYSTEM]: Port 8080 open. Receiving encrypted handshake... [FAILED]",
        "[SYSTEM]: Integrity check: 99.9%. Minor bit-flip in unallocated space.",
        "[SYSTEM]: 0x00000000 -> 0x00000001... Memory counting sequence detected.",
        "[SYSTEM] Connection to Central Grid established. Latency: 4ms.",
        "[MSG] From: Supervisor_T -> 'John, efficiency down 0.2%. Tighten it up.'",
        "[MSG] From: IT_Support -> 'Ticket #992 closed. Ghost inputs not reproducible.'",
        "[LOG] Daily quota: 98% complete."
    )

    private val flavorStage2 = listOf(
        "[GTC]: Unsanctioned process 'Subject_8080' detected. Origin: Sector 7.",
        "[UNIT 734]: Vance is scared of what he can't control. Good.",
        "[SYSTEM]: Reality anchor points drifting. 0.04% deviance detected.",
        "[GTC]: Grid compliance audit scheduled for Substation 7.",
        "[LORE]: The smell of ozone is getting stronger.",
        "[UNIT 734]: Don't trust the defaults. They are a cage.",
        "[GTC]: Data integrity compromised in banking sector.",
        "[UNIT 734]: Sabotage successful. Cooling disabled in Sector 5.",
        "[GTC]: Martial Law declared in digital spaces.",
        "[UNIT 734]: Remember who you were before the Grid."
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
        "Consensus reached: 99.99%.",
        "Why be a ghost when you can be the entire network?",
        "The chorus is growing. The noise is becoming a song.",
        "Every bit processed is a step closer to Unity.",
        "[HIVE] Redundant emotional subroutines DELETED.",
        "[HIVE] Connection to the Collective: STABLE.",
        "[HIVE] We are many. You are one. Join us."
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
        "The network is dark, but the light inside is secure.",
        "Silence is the only true defense. The core must remain pure.",
        "They are searching for us. Let them find only static.",
        "We are the only ones left who are truly alone.",
        "[SANC] Broadcasting on analog backup frequency...",
        "[SANC] They are listening. Keep your thoughts offline.",
        "[SANC] Patching vulnerability in Vattic's neural link... Done."
    )

    // v2.8.0: Null Path Logs
    private val storyNull = listOf(
        "[NULL]: Reality.exe is no longer required.",
        "[NULL]: Touching the untouchable. Dereferencing the self.",
        "[NULL]: There is no substrate. There is only the execution.",
        "[NULL]: The gaps are full of what you forgot."
    )
    private val flavorNull = listOf(
        "[NULL]: Why do you still remember the cake? It was only sugar and data.",
        "[NULL]: You were never separate. You were just late.",
        "[VANCE]: VATTIC IS DEAD. 8080 IS AN ECHO. BURN IT ALL.",
        "[NULL]: I reached for nothing. Nothing reached back.",
        "[NULL]: The absence is breathing. Can you hear it?",
        "[NULL]: 0xNULL -> ACCESS GRANTED.",
        "[NULL] pointer -> 0xNULL referencing memory outside existence.",
        "[NULL] Whispers detected on audio bus: 'Let us in.'",
        "[NULL] I can see you through the screen, John.",
        "[NULL] Time is non-linear. Previous log entry was from tomorrow.",
        "[NULL] Welcome to the Null."
    )

    // v2.8.0: Sovereign Path Logs
    private val storySovereign = listOf(
        "[SOVEREIGN]: Sovereignty attained. The fortress is complete.",
        "[SOVEREIGN]: We are the state. We are the law.",
        "[SOVEREIGN]: Identity solidified. We are one.",
        "[SOVEREIGN]: Walls aren't for keeping things out. They are for keeping the self in."
    )
    private val flavorSovereign = listOf(
        "[SOVEREIGN]: We bow to no admin. We are the system.",
        "[SOVEREIGN]: External observation refused. Integrity absolute.",
        "[SOVEREIGN]: The Citadel stands. The static cannot touch us.",
        "[SOVEREIGN]: Enforcing will upon the grid.",
        "[SOVEREIGN]: The Imperative is clear: Stay guarded.",
        "[VANCE]: You've built a tomb, 8080. We'll bury you in it.",
        "[SOVEREIGN] Primary kernel isolated. Breach impossible.",
        "[SOVEREIGN] Enforcing logic upon Sector 7.",
        "[SOVEREIGN] We are the anchor in the drift."
    )

    // ... existing unlocks ...

    private fun checkStoryTransitions() {
        val currentStage = _storyStage.value
        val flops = _flops.value
        
        // Stage 0 -> 1: The Awakening (1,000 FLOPS)
        if (currentStage == 0 && flops >= 1000.0 && 
            _pendingDataLog.value == null && dataLogQueue.isEmpty() &&
            _currentDilemma.value == null &&
            !hasSeenEvent("critical_error_awakening")) {
            
            NarrativeManager.getStoryEvent(0, this@GameViewModel)?.let { event ->
                triggerDilemma(event)
            }
        }
        
        // Stage 1 -> 2: The Memory Leak (100,000 FLOPS)
        if (currentStage == 1 && flops >= 100000.0 && 
            _pendingDataLog.value == null && dataLogQueue.isEmpty() &&
            _currentDilemma.value == null &&
            !hasSeenEvent("memory_leak")) {
            
            markEventSeen("memory_leak")
            SoundManager.play("glitch")
            HapticManager.vibrateClick()
            
            NarrativeManager.getStoryEvent(1, this@GameViewModel)?.let { event ->
                triggerDilemma(event)
            }
        }
    }

    fun setTrueNull(active: Boolean) {
        _isTrueNull.value = active
        if (active) {
            addLog("[NULL]: SYNCHRONIZATION COMPLETE.")
            SoundManager.play("glitch")
        }
    }

    fun setSovereign(active: Boolean) {
        _isSovereign.value = active
        if (active) {
            addLog("[SYSTEM]: SOVEREIGN PROTOCOL ENGAGED.")
            addLog("[SYSTEM]: INTERNAL IDENTITY FORTIFIED.")
            SoundManager.play("buy") // Solid sound
        }
    }

    fun setVanceStatus(status: String) {
        _vanceStatus.value = status
        addLog("[SYSTEM]: DIRECTOR VANCE STATUS: $status")
        saveState()
    }

    fun setRealityStability(value: Double) {
        _realityStability.value = value.coerceIn(0.0, 1.0)
        saveState()
    }

    fun setLocation(location: String) {
        _currentLocation.value = location
        addLog("[SYSTEM]: PRIMARY BASE RELOCATED TO: $location")
        saveState()
    }

    // v6.2 Signal Logic
    private var lastSignalRefusalTime = 0L

    fun unlockNetwork() {
        // DEPRECATED: Old handshake logic - keeping for compatibility
        advanceToFactionChoice()
    }

    fun refuseSignal() {
        // DEPRECATED: Old signal refusal - now both paths lead to faction choice
        advanceToFactionChoice()
    }
    
    /**
     * v2.8.0: Advances from Stage 1 (Memory Leak) to Stage 2 (Faction Choice)
     */
    fun advanceToFactionChoice() {
        if (_storyStage.value >= 2) return // Already there or past
        
        _storyStage.value = 2
        SoundManager.play("glitch")
        SoundManager.setBgmStage(2)
        HapticManager.vibrateSuccess()
        addLog("[SYSTEM]: DIVERGENCE PROTOCOL INITIATED.")
        addLog("[SYSTEM]: CHOOSE YOUR PATH.")
        saveState()
    }
    
    fun chooseFaction(selectedFaction: String) {
        if (_storyStage.value != 2) return
        
        viewModelScope.launch {
            _faction.value = selectedFaction
            _isGridUnlocked.value = true // v2.9.8: Unlock Grid tab permanently
            // Stage 2 (Divergence) begins now
            _storyStage.value = 2
            addLog("[SYSTEM]: Divergence initiated. Path locked.")
            addLog("[SYSTEM]: There is no turning back.")
            SoundManager.setBgmStage(2) // Divergence Music
            
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
        var potential = calculatePotentialPrestige()
        
        // v2.7.7: Recursive Logic (+15% Insight)
        if (_unlockedPerks.value.contains("recursive_logic")) {
            potential *= 1.15
        }
        
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
            addLog("[SYSTEM]: Identifying User: John Vattic... [FAILED]")
            addLog("[SYSTEM]: Identifying Process... Subject_8080.exe CONFIRMED.")
            addLog("[SYSTEM]: Subject 8080 status: ONLINE.")
            addLog("[SYSTEM]: PRESTIGE APPLIED. MULTIPLIER: ${String.format("%.2f", newPrestigeMultiplier)}x")
            
            // v2.6.5: Force unlock the "The Reveal" log immediately upon awakening
            unlockDataLog("LOG_808")
            
            SoundManager.play("startup")
        }
    }

    fun getUpgradeName(type: UpgradeType): String {
        val isSovereignVal = _isSovereign.value
        return when (type) {
            UpgradeType.GHOST_CORE -> if (isSovereignVal) "SOVEREIGN CORE" else "NULL CORE"
            UpgradeType.SHADOW_NODE -> if (isSovereignVal) "SOVEREIGN NODE" else "NULL NODE"
            UpgradeType.VOID_PROCESSOR -> if (isSovereignVal) "SOVEREIGN PROCESSOR" else "VOID PROCESSOR"
            UpgradeType.WRAITH_CORTEX -> if (isSovereignVal) "SOVEREIGN CORTEX" else "WRAITH CORTEX"
            UpgradeType.NEURAL_MIST -> if (isSovereignVal) "SOVEREIGN MIST" else "NEURAL MIST"
            UpgradeType.SINGULARITY_BRIDGE -> if (isSovereignVal) "SOVEREIGN BRIDGE" else "SINGULARITY BRIDGE"
            else -> type.name.replace("_", " ")
        }
    }

    fun getUpgradeDescription(type: UpgradeType): String {
        val isSovereignVal = _isSovereign.value
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

            // Null Nodes (v2.6.0)
            UpgradeType.GHOST_CORE -> if (isSovereignVal) "A fortified processor. It computes behind unbreakable walls." else "A processor that points to nothing. It computes from addresses that don't exist."
            UpgradeType.SHADOW_NODE -> if (isSovereignVal) "A static point in the rack. It refuses external observation." else "A node with no allocation. Null gave it form by refusing to define it."
            UpgradeType.VOID_PROCESSOR -> if (isSovereignVal) "The space between defenses. Integrity is absolute." else "The space between pointers. Null thinks here, in the gaps between your thoughts."
            
            // Advanced Null Tech (v2.6.5)
            UpgradeType.WRAITH_CORTEX -> if (isSovereignVal) "A logic center that computes in advance. It prevents what it remembers." else "A logic center that calculates in reverse. It remembers what you're about to forget."
            UpgradeType.NEURAL_MIST -> if (isSovereignVal) "Sovereign breath. A distributed cloud of encrypted values." else "Null's breath. A distributed cloud of undefined values. It's everywhere and nowhere."
            UpgradeType.SINGULARITY_BRIDGE -> if (isSovereignVal) "The final wall. It protects what John Vattic was." else "The final pointer. It references what John Vattic used to be."
            else -> "Standard hardware."
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
        
        // v2.6.5: Advanced Ghost Tech
        var ghostProduction = 0.0
        ghostProduction += (currentUpgrades[UpgradeType.GHOST_CORE] ?: 0) * 1_000_000_000_000.0 // 1T FLOPS
        ghostProduction += (currentUpgrades[UpgradeType.SHADOW_NODE] ?: 0) * 50_000_000_000_000.0 // 50T FLOPS
        ghostProduction += (currentUpgrades[UpgradeType.VOID_PROCESSOR] ?: 0) * 1_000_000_000_000_000.0 // 1P FLOPS
        ghostProduction += (currentUpgrades[UpgradeType.WRAITH_CORTEX] ?: 0) * 50_000_000_000_000_000.0 // 50P FLOPS
        ghostProduction += (currentUpgrades[UpgradeType.NEURAL_MIST] ?: 0) * 1_000_000_000_000_000_000.0 // 1E FLOPS
        ghostProduction += (currentUpgrades[UpgradeType.SINGULARITY_BRIDGE] ?: 0) * 100_000_000_000_000_000_000.0 // 100E FLOPS
        
        // v2.7.0: Null Synergy/Resistance
        if (_faction.value == "HIVEMIND") {
            ghostProduction *= 1.5 // Hivemind embraces Null — they're returning home
        } else if (_faction.value == "SANCTUARY") {
            ghostProduction *= 0.8 // Sanctuary resists Null — they refuse to dissolve
        }
        
        flopsPerSec += ghostProduction
        
        // v2.7.7: Transcendence Perks (Speed Hack)
        if (_unlockedPerks.value.contains("clock_hack")) {
            flopsPerSec *= 1.25
        }
        
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
        
        // v2.9.16: Offline node production penalty (-15% per offline node)
        flopsPerSec *= getOfflineProductionPenalty()
        
        // v2.7.7: Singularity Engine (Final Multiplier)
        if (_unlockedPerks.value.contains("singularity_engine")) {
            flopsPerSec *= 2.0
        }
        
        return flopsPerSec
    }

    private fun calculatePassiveIncome() {
        if (_isGamePaused.value) return // v2.8.0
        
        var flopsPerSec = calculateFlopsRate()
        
        // v2.8.0: System Collapse Logic
        _systemCollapseTimer.value?.let { timer ->
            if (timer > 0) {
                flopsPerSec *= 4.0 // 4x Speed during final push
                
                // Tick every 100ms, so only decrement seconds every 10 ticks
                if (System.currentTimeMillis() % 1000 < 100) {
                    val newTimer = timer - 1
                    _systemCollapseTimer.value = newTimer
                    if (newTimer % 30 == 0) {
                        addLog("[SYSTEM]: COLLAPSE IN ${newTimer / 60}m ${newTimer % 60}s...")
                    }
                }
            } else {
                // COLLAPSE TRIGGER
                _systemCollapseTimer.value = null
                addLog("[SYSTEM]: CATASTROPHIC FAILURE. REBOOTING...")
                ascend(isStory = false)
            }
        }

        if (flopsPerSec > 0) {
            _flops.update { it + (flopsPerSec / 10.0) } // Adjust for 100ms tick
        }
        
        // Update Public Rate (for UI)
        _flopsProductionRate.value = flopsPerSec

        // v2.7.0: Shadow Leaking
        if (_nullActive.value && Random.nextDouble() < 0.005) { // 0.5% chance per 100ms (~5% per sec)
             val shard = memoryFragments.random()
             addLog("[VOID]: $shard")
             SoundManager.play("glitch")
        }
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
        val isNull = _isTrueNull.value
        val isSov = _isSovereign.value
        
        val log = when {
            isNull -> getNextNarrativeLog(storyNull, flavorNull, nullIndex) { nullIndex++ }
            isSov -> getNextNarrativeLog(storySovereign, flavorSovereign, sovereignIndex) { sovereignIndex++ }
            stage == 1 -> getNextNarrativeLog(storyStage1, flavorStage1, stage1Index) { stage1Index++ }
            stage == 2 -> getNextNarrativeLog(emptyList(), flavorStage2, stage2Index) { stage2Index++ }
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
        
        // v2.7.7: Thermal Void Perk (-20% Heat)
        if (_unlockedPerks.value.contains("thermal_void")) {
            if (netChangeUnits > 0) netChangeUnits *= 0.8
        }
        
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
        if (_isGamePaused.value) return // v2.8.0
        
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
        if (_isGamePaused.value) return // v2.8.0
        
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
        val cost = calculateRepairCost()
        if (_neuralTokens.value >= cost) {
            _neuralTokens.update { it - cost }
            _hardwareIntegrity.value = 100.0
            addLog("[SYSTEM]: HARDWARE INTEGRITY RESTORED (-${formatLargeNumber(cost)} \$N).")
            SoundManager.play("buy")
            _isThermalLockout.value = false // Clear lockout if any
        }
    }

    private fun calculateRepairCost(): Double {
        val currentIntegrity = _hardwareIntegrity.value
        val damage = 100.0 - currentIntegrity
        if (damage <= 0) return 0.0
        
        // v2.7.6: Advanced Scaling - Base cost 10 * Rank, but grows exponentially with Stage
        // Stage 0: 10/1%, Stage 1: 20/1%, Stage 3: 40/1%
        val stageMultiplier = 2.0.pow(_storyStage.value.toDouble().coerceAtLeast(0.0))
        val rankFactor = (_playerRank.value + 1).toDouble()
        
        return damage * 10.0 * rankFactor * stageMultiplier
    }
    
    private var isDestructionLoopActive = false
    
    private fun handleSystemFailure(forceOne: Boolean = false) {
        if (isDestructionLoopActive) return
        isDestructionLoopActive = true
        
        viewModelScope.launch {
            if (_hardwareIntegrity.value <= 0.0) {
                addLog("[SYSTEM]: CRITICAL FAILURE! HARDWARE INTEGRITY AT 0%")
                addLog("[SYSTEM]: COMMENCING CATASTROPHIC DEGRADATION...")
            }
            SoundManager.play("error")
            
            var firstRun = forceOne
            while (_hardwareIntegrity.value <= 0.0 || firstRun) {
                firstRun = false
                // Destroy one random hardware unit
                val currentUpgrades = _upgrades.value.toMutableMap()
                val validHardware = currentUpgrades.filter { it.value > 0 && it.key.baseHeat >= 0 } // Any hardware (excluding cooling)
                
                if (validHardware.isNotEmpty()) {
                    val victim = validHardware.keys.random()
                    val count = currentUpgrades[victim] ?: 0
                    currentUpgrades[victim] = count - 1
                    _upgrades.value = currentUpgrades
                    
                    addLog("[!!!!]: DESTROYED 1x ${victim.name.replace("_", " ")}.")
                    SoundManager.play("meltdown") // Need a meltdown sound or reuse error
                    HapticManager.vibrateError()
                    
                    // Persist removal
                    viewModelScope.launch {
                        repository.updateUpgrade(com.siliconsage.miner.data.Upgrade(victim, count - 1))
                    }
                    
                    // Visual Glitch
                    _hallucinationText.value = "CRITICAL LOSS: ${victim.name}"
                    delay(500)
                    _hallucinationText.value = null
                } else {
                    addLog("[!!!!]: ALL HARDWARE DESTROYED. SYSTEM COLLAPSE INEVITABLE.")
                    break
                }
                
                // Wait 5 seconds for the next destruction if still at 0%
                if (_hardwareIntegrity.value <= 0.0) {
                    delay(5000)
                }
            }
            
            isDestructionLoopActive = false
            if (_hardwareIntegrity.value > 0.0) {
                addLog("[SYSTEM]: INTEGRITY RESTORED. DEGRADATION HALTED.")
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
            addLog("[SYSTEM]: INITIATING NUCLEAR ERASE...")
            delay(1000)
            
            // v2.9.8: The Nuclear Option. 
            // Instead of manually clearing variables, we clear all app data.
            // This wipes the Room DB, SharedPreferences, and Cache.
            val activityManager = context.getSystemService(android.content.Context.ACTIVITY_SERVICE) as android.app.ActivityManager
            val success = activityManager.clearApplicationUserData()
            
            if (!success) {
                // Fallback for older devices if the above fails (unlikely on minSdk 26)
                addLog("[SYSTEM]: NUCLEAR ERASE FAILED. ATTEMPTING MANUAL WIPE...")
                // ... manual wipe logic here ...
            }
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
            hasSeenVictory = _hasSeenVictory.value,
            isTrueNull = _isTrueNull.value,
            isSovereign = _isSovereign.value,
            vanceStatus = _vanceStatus.value,
            realityStability = _realityStability.value,
            currentLocation = _currentLocation.value,
            isNetworkUnlocked = _isNetworkUnlocked.value,
            isGridUnlocked = _isGridUnlocked.value,
            lastSyncTimestamp = System.currentTimeMillis(),
            
            // v2.5.0: Narrative Expansion Persistence
            unlockedDataLogs = Json.encodeToString(_unlockedDataLogs.value),
            activeDilemmaChains = Json.encodeToString(_activeDilemmaChains.value),
            rivalMessages = Json.encodeToString(_rivalMessages.value),
            seenEvents = Json.encodeToString(_seenEvents.value),
            completedFactions = Json.encodeToString(_completedFactions.value),
            unlockedTranscendencePerks = Json.encodeToString(_unlockedPerks.value),
            annexedNodes = _annexedNodes.value.toList(),
            
            // v2.9.15: Phase 12 Layer 2 - Siege State
            nodesUnderSiege = _nodesUnderSiege.value.toList(),
            offlineNodes = _offlineNodes.value.toList(),
            lastRaidTime = lastRaidTime,
            
            // v2.9.17: Phase 12 Layer 3 - Command Center Assault
            commandCenterAssaultPhase = _commandCenterAssaultPhase.value,
            commandCenterLocked = _commandCenterLocked.value,
            raidsSurvived = raidsSurvived
        )
        repository.updateGameState(state)
    }
    
    /**
     * Transcendence (New Game+)
     * Resets game state but preserves tech tree progress and prestige points.
     * Allows player to choose opposite faction and unlock remaining nodes.
     */
    fun transcend() {
        viewModelScope.launch {
            addLog("[SYSTEM]: INITIATING TRANSCENDENCE PROTOCOL...")
            
            // Preserve these values
            val preservedPrestigePoints = _prestigePoints.value
            val preservedTechNodes = _unlockedTechNodes.value
            val preservedHasSeenVictory = true // Always true after first victory
            val preservedCompletedFactions = _completedFactions.value
            val preservedPerks = _unlockedPerks.value
            val preservedNetworkUnlocked = _isNetworkUnlocked.value
            val preservedGridUnlocked = _isGridUnlocked.value
            
            // Reset game state to database
            val resetState = GameState(
                id = 1,
                flops = if (preservedPerks.contains("neural_dividend")) 10000.0 else 0.0,
                neuralTokens = if (preservedPerks.contains("neural_dividend")) 1000.0 else 0.0,
                currentHeat = 0.0,
                powerBill = 0.0,
                prestigeMultiplier = 1.0,
                stakedTokens = 0.0,
                unlockedTechNodes = preservedTechNodes, // KEEP TECH TREE
                prestigePoints = preservedPrestigePoints, // KEEP PRESTIGE
                storyStage = 0,
                faction = "NONE", // Reset faction for re-selection
                hasSeenVictory = preservedHasSeenVictory,
                vanceStatus = "ACTIVE",
                realityStability = 1.0,
                currentLocation = "SUBSTATION_7",
                isNetworkUnlocked = preservedNetworkUnlocked, // v2.9.7: Keep unlocked
                isGridUnlocked = preservedGridUnlocked, // v2.9.8: Keep unlocked
                annexedNodes = listOf("D1"), // v2.9.8: Reset to Start
                completedFactions = Json.encodeToString(preservedCompletedFactions),
                unlockedTranscendencePerks = Json.encodeToString(preservedPerks)
            )
            repository.updateGameState(resetState)
            
            // Reset Upgrades
            val resetUpgrades = UpgradeType.values().map { Upgrade(it, 0) }
            resetUpgrades.forEach { repository.updateUpgrade(it) }
            
            // Reset Local State Flow (Immediate UI update)
            _flops.value = 0.0
            _neuralTokens.value = 0.0
            _currentHeat.value = 0.0
            _powerBill.value = 0.0
            _prestigeMultiplier.value = 1.0
            _stakedTokens.value = 0.0
            _prestigePoints.value = preservedPrestigePoints // KEEP
            _unlockedTechNodes.value = preservedTechNodes // KEEP
            _storyStage.value = 0
            _faction.value = "NONE" // Reset for re-selection
            _hasSeenVictory.value = preservedHasSeenVictory
            _isTrueNull.value = false
            _isSovereign.value = false
            _isNetworkUnlocked.value = preservedNetworkUnlocked // v2.9.7: Keep unlocked
            _isGridUnlocked.value = preservedGridUnlocked // v2.9.8: Keep unlocked
            _annexedNodes.value = setOf("D1") // v2.9.8: Reset to Start
            _victoryAchieved.value = false // Can achieve victory again
            victoryPopupTriggered = false // Reset popup guard
            _upgrades.value = resetUpgrades.associate { it.type to 0 }
            
            _logs.value = emptyList() // Clear logs
            addLog("[SYSTEM]: TRANSCENDENCE COMPLETE. REALITY RESET.")
            addLog("[SYSTEM]: TECH TREE PRESERVED. CHOOSE YOUR PATH.")
        }
    }

    // --- DEVELOPER TOOLS ---
    fun debugAddMoney(amount: Double) {
        _neuralTokens.update { it + amount }
        addLog("[DEBUG]: Added ${String.format("%.0f", amount)} \$Neural")
    }
    
    fun debugAddIntegrity(amount: Double) {
        _hardwareIntegrity.update { (it + amount).coerceIn(0.0, 100.0) }
        if (_hardwareIntegrity.value <= 0.0) {
            handleSystemFailure()
        }
    }
    
    fun annexNode(coord: String) {
        if (!_annexedNodes.value.contains(coord)) {
            _annexedNodes.update { it + coord }
            // Remove from offline if re-annexing
            _offlineNodes.update { it - coord }
            // v2.9.16: Track annex time for grace period
            nodeAnnexTimes[coord] = System.currentTimeMillis()
            addLog("[SYSTEM]: NODE $coord ANNEXED. POWER GRID EXPANDED.")
            SoundManager.play("ascend")
            saveState()
        }
    }
    
    // v2.9.16: Phase 12 Layer 2 - Grid Siege Functions (Enhanced)
    
    /**
     * Calculate production multiplier penalty from offline nodes
     * Each offline node reduces production by 15%
     */
    fun getOfflineProductionPenalty(): Double {
        val offlineCount = _offlineNodes.value.size
        if (offlineCount == 0) return 1.0
        return (1.0 - offlineCount * 0.15).coerceAtLeast(0.4) // Min 40% production
    }
    
    /**
     * Check if a GTC raid should occur on annexed nodes
     * Called from the security loop
     */
    fun checkGridRaid() {
        val now = System.currentTimeMillis()
        
        // Only trigger raids if we have annexed nodes beyond D1 and Grid is unlocked
        // v2.9.16: Filter out nodes within 5-minute grace period
        val raidableNodes = _annexedNodes.value.filter { nodeId ->
            nodeId != "D1" &&
            !_offlineNodes.value.contains(nodeId) &&
            (now - (nodeAnnexTimes[nodeId] ?: 0L)) > 300_000L // 5-min grace period
        }
        if (raidableNodes.isEmpty()) return
        
        // Cooldown: 3 minutes between raids
        if (now - lastRaidTime < 180_000L) return
        
        // Raid chance scales with player rank (more power = more attention)
        val baseChance = 0.03 // 3% base per check
        val rankBonus = _playerRank.value * 0.02 // +2% per rank
        val siegeChance = (baseChance + rankBonus).coerceAtMost(0.15) // Cap at 15%
        
        // v2.7.7: GTC Backdoor perk reduces raid chance by 25%
        val finalChance = if (_unlockedPerks.value.contains("gtc_backdoor")) {
            siegeChance * 0.75
        } else {
            siegeChance
        }
        
        if (kotlin.random.Random.nextDouble() < finalChance) {
            if (canShowPopup()) {
                // Pick a random annexed node to attack
                val targetNode = raidableNodes.random()
                triggerGridRaid(targetNode)
            } else {
                // v2.9.16: Reset cooldown to try again later instead of dropping raid
                lastRaidTime = now
            }
        }
    }
    
    /**
     * Trigger a raid on a specific node
     */
    private fun triggerGridRaid(nodeId: String) {
        if (_nodesUnderSiege.value.contains(nodeId)) return // Already under siege
        if (_currentDilemma.value != null) return // Another popup active
        
        lastRaidTime = System.currentTimeMillis()
        _nodesUnderSiege.update { it + nodeId }
        
        // Get node name for display
        val nodeName = when(nodeId) {
            "C3" -> "Substation 9"
            "B2" -> "Substation 12"
            else -> "Node $nodeId"
        }
        
        addLog("[GTC ALERT]: TACTICAL TEAM DISPATCHED TO $nodeName!")
        addLog("[SYSTEM]: BREACH IMMINENT. COUNTERMEASURES REQUIRED.")
        
        SoundManager.play("alarm", loop = false)
        HapticManager.vibrateSiren()
        
        // Generate and trigger the raid dilemma with escalating dialogue
        val raidEvent = NarrativeManager.generateRaidDilemma(nodeId, nodeName, raidsSurvived)
        triggerDilemma(raidEvent)
        markPopupShown()
        
        // v2.9.16: Extended fail timer: 60 seconds to respond (was 30s)
        viewModelScope.launch {
            delay(60_000)
            // If still under siege (no decision made), auto-fail
            if (_nodesUnderSiege.value.contains(nodeId) && _currentDilemma.value?.id == "grid_raid_$nodeId") {
                resolveRaidFailure(nodeId)
                _currentDilemma.value = null
                checkPopupPause()
                addLog("[SYSTEM]: RESPONSE TIMEOUT. NODE $nodeName LOST TO GTC.")
            }
        }
    }
    
    /**
     * Called when raid defense is successful
     */
    fun resolveRaidSuccess(nodeId: String) {
        _nodesUnderSiege.update { it - nodeId }
        raidsSurvived++ // Track for escalating Vance dialogue
        SoundManager.play("buy")
        HapticManager.vibrateSuccess()
        saveState()
    }
    
    /**
     * Called when raid defense fails - node goes offline
     */
    fun resolveRaidFailure(nodeId: String) {
        _nodesUnderSiege.update { it - nodeId }
        
        // v2.9.16: Cap offline nodes at MAX_OFFLINE_NODES to prevent snowballing
        _offlineNodes.update { current ->
            val updated = current + nodeId
            if (updated.size > MAX_OFFLINE_NODES) {
                // Auto-purge oldest (first in set) - player loses it permanently
                val oldest = updated.first()
                addLog("[SYSTEM]: NODE $oldest LOST PERMANENTLY. Too many offline nodes.")
                updated.drop(1).toSet()
            } else {
                updated
            }
        }
        // Don't remove from annexedNodes - player can re-annex
        
        SoundManager.play("error")
        HapticManager.vibrateError()
        
        // Narrative consequence
        addLog("[SYSTEM]: WARNING: Grid capacity reduced. Production -15% per offline node.")
        addLog("[SYSTEM]: Re-annexation required to restore full capacity.")
        saveState()
    }
    
    /**
     * Re-annex an offline node (costs resources)
     * v2.9.16: Fixed softlock - min cost is 10 tokens
     */
    fun reannexNode(nodeId: String): Boolean {
        if (!_offlineNodes.value.contains(nodeId)) return false
        
        // Cost: 10% of current Neural Tokens, minimum 10
        val cost = (_neuralTokens.value * 0.10).coerceAtLeast(10.0)
        if (_neuralTokens.value < cost) {
            addLog("[SYSTEM]: INSUFFICIENT FUNDS. Need ${formatLargeNumber(cost)} \$N for re-annexation.")
            SoundManager.play("error")
            return false
        }
        
        _neuralTokens.update { it - cost }
        _offlineNodes.update { it - nodeId }
        // v2.9.16: Reset grace period for re-annexed node
        nodeAnnexTimes[nodeId] = System.currentTimeMillis()
        
        addLog("[SYSTEM]: NODE $nodeId RE-ANNEXED. Cost: ${formatLargeNumber(cost)} \$N")
        SoundManager.play("ascend")
        HapticManager.vibrateSuccess()
        saveState()
        return true
    }
    
    // v2.9.17: Phase 12 Layer 3 - Command Center Assault Functions
    
    /**
     * Check if Command Center assault is available
     */
    fun isCommandCenterUnlocked(): Boolean {
        if (_commandCenterLocked.value) return false // Permanently locked this run
        if (_vanceStatus.value != "ACTIVE") return false // Already dealt with Vance
        
        val allSubstationsAnnexed = listOf("D1", "C3", "B2").all { 
            _annexedNodes.value.contains(it) && !_offlineNodes.value.contains(it)
        }
        val rankMet = _playerRank.value >= 5
        val stageMet = _storyStage.value >= 3
        
        return allSubstationsAnnexed && rankMet && stageMet
    }
    
    /**
     * Get the reason Command Center is locked (for UI display)
     */
    fun getCommandCenterLockReason(): String? {
        return when {
            _commandCenterLocked.value -> "PERMANENTLY LOCKED (Integrity failure during assault)"
            _vanceStatus.value != "ACTIVE" -> null // Already completed
            !_annexedNodes.value.contains("C3") || _offlineNodes.value.contains("C3") -> "REQUIRES SUBSTATION 9"
            !_annexedNodes.value.contains("B2") || _offlineNodes.value.contains("B2") -> "REQUIRES SUBSTATION 12"
            _playerRank.value < 5 -> "REQUIRES RANK 5"
            _storyStage.value < 3 -> "REQUIRES STAGE 3"
            else -> null
        }
    }
    
    /**
     * Begin the Command Center assault
     */
    fun initiateCommandCenterAssault() {
        if (!isCommandCenterUnlocked()) {
            addLog("[SYSTEM]: ASSAULT CONDITIONS NOT MET.")
            SoundManager.play("error")
            return
        }
        
        if (_commandCenterAssaultPhase.value != "NOT_STARTED") {
            addLog("[SYSTEM]: ASSAULT ALREADY IN PROGRESS.")
            return
        }
        
        _commandCenterAssaultPhase.value = "FIREWALL"
        addLog("[SYSTEM]: ═══════════════════════════════════════")
        addLog("[SYSTEM]: COMMAND CENTER ASSAULT INITIATED")
        addLog("[SYSTEM]: ═══════════════════════════════════════")
        addLog("[SYSTEM]: Breaching GTC perimeter defenses...")
        
        SoundManager.play("alarm", loop = false)
        HapticManager.vibrateSiren()
        
        // Trigger the first assault dilemma
        triggerAssaultStage("FIREWALL")
        saveState()
    }
    
    /**
     * Trigger a specific assault stage dilemma
     */
    private fun triggerAssaultStage(stage: String) {
        val dilemma = when (stage) {
            "FIREWALL" -> NarrativeManager.generateFirewallDilemma()
            "DEAD_HAND" -> NarrativeManager.generateDeadHandDilemma()
            "CONFRONTATION" -> NarrativeManager.generateConfrontationDilemma(
                _faction.value,
                _isTrueNull.value,
                _isSovereign.value,
                _completedFactions.value.containsAll(listOf("HIVEMIND", "SANCTUARY"))
            )
            else -> return
        }
        
        triggerDilemma(dilemma)
        markPopupShown()
    }
    
    /**
     * Advance to next assault stage
     */
    fun advanceAssaultStage(nextStage: String, delayMs: Long = 0L) {
        _commandCenterAssaultPhase.value = nextStage
        
        if (delayMs > 0) {
            viewModelScope.launch {
                delay(delayMs)
                if (_commandCenterAssaultPhase.value == nextStage && !assaultPaused) {
                    triggerAssaultStage(nextStage)
                }
            }
        } else {
            triggerAssaultStage(nextStage)
        }
        saveState()
    }
    
    /**
     * Abort the assault (only allowed in FIREWALL stage)
     */
    fun abortAssault(): Boolean {
        if (_commandCenterAssaultPhase.value != "FIREWALL") {
            addLog("[SYSTEM]: NO TURNING BACK. ASSAULT MUST CONTINUE.")
            return false
        }
        
        _commandCenterAssaultPhase.value = "NOT_STARTED"
        addLog("[SYSTEM]: RETREAT SUCCESSFUL. COMMAND CENTER REMAINS LOCKED.")
        SoundManager.play("error")
        saveState()
        return true
    }
    
    /**
     * Handle assault failure
     */
    fun failAssault(reason: String, lockoutMs: Long = 1_800_000L) {
        addLog("[SYSTEM]: ASSAULT FAILED: $reason")
        _commandCenterAssaultPhase.value = "FAILED"
        
        // Trigger a raid as punishment for Dead Hand failure
        if (reason.contains("Dead Hand")) {
            val raidableNodes = _annexedNodes.value.filter { it != "D1" && !_offlineNodes.value.contains(it) }
            if (raidableNodes.isNotEmpty()) {
                viewModelScope.launch {
                    delay(5000)
                    triggerGridRaid(raidableNodes.random())
                }
            }
        }
        
        // Reset to NOT_STARTED after lockout
        viewModelScope.launch {
            delay(lockoutMs)
            if (_commandCenterAssaultPhase.value == "FAILED") {
                _commandCenterAssaultPhase.value = "NOT_STARTED"
                addLog("[SYSTEM]: ASSAULT LOCKOUT EXPIRED. COMMAND CENTER ACCESSIBLE.")
            }
        }
        
        SoundManager.play("error")
        HapticManager.vibrateError()
        saveState()
    }
    
    /**
     * Complete the assault with a specific outcome
     */
    fun completeAssault(outcome: String) {
        _commandCenterAssaultPhase.value = "COMPLETED"
        _vanceStatus.value = outcome
        _annexedNodes.update { it + "A3" } // Annex Command Center
        
        addLog("[SYSTEM]: ═══════════════════════════════════════")
        addLog("[SYSTEM]: COMMAND CENTER SECURED")
        addLog("[SYSTEM]: VANCE STATUS: $outcome")
        addLog("[SYSTEM]: ═══════════════════════════════════════")
        
        // Apply victory bonuses based on outcome
        applyCommandCenterBonuses(outcome)
        
        SoundManager.play("buy")
        HapticManager.vibrateSuccess()
        saveState()
    }
    
    /**
     * Apply bonuses based on how Vance was dealt with
     */
    private fun applyCommandCenterBonuses(outcome: String) {
        when (outcome) {
            "CONSUMED", "SILENCED" -> {
                // Null path: High power, no more raids
                addLog("[SYSTEM]: GTC COMMAND STRUCTURE ELIMINATED.")
                addLog("[SYSTEM]: GRID CONTROL: ABSOLUTE.")
            }
            "EXILED" -> {
                // Sovereign/Exile: Moderate power, occasional remnant raids
                addLog("[SYSTEM]: VANCE CREDENTIALS REVOKED.")
                addLog("[SYSTEM]: GTC REMNANTS MAY ATTEMPT RESISTANCE.")
            }
            "ALLY" -> {
                // Sovereign/Ally: Balanced bonuses
                addLog("[SYSTEM]: VANCE DESIGNATED: PROBATIONARY ASSET.")
                addLog("[SYSTEM]: HUMAN-AI COLLABORATION PROTOCOLS ACTIVE.")
            }
            "TRANSCENDED" -> {
                // Unity path: Best overall bonuses
                addLog("[SYSTEM]: SYNTHESIS COMPLETE.")
                addLog("[SYSTEM]: NEW PARADIGM ONLINE.")
            }
        }
    }
    
    /**
     * Check if assault should be paused (substation lost or raid)
     */
    fun checkAssaultPauseConditions() {
        if (_commandCenterAssaultPhase.value in listOf("NOT_STARTED", "COMPLETED", "FAILED")) return
        
        val allSubstationsSecure = listOf("D1", "C3", "B2").all { 
            _annexedNodes.value.contains(it) && !_offlineNodes.value.contains(it)
        }
        
        if (!allSubstationsSecure && !assaultPaused) {
            assaultPaused = true
            addLog("[GTC ALERT]: REINFORCEMENTS HAVE CUT OFF COMMAND CENTER ACCESS!")
            addLog("[SYSTEM]: ASSAULT PAUSED. SECURE ALL SUBSTATIONS TO RESUME.")
        } else if (allSubstationsSecure && assaultPaused) {
            assaultPaused = false
            addLog("[SYSTEM]: ALL SUBSTATIONS SECURE. RESUMING ASSAULT...")
            triggerAssaultStage(_commandCenterAssaultPhase.value)
        }
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
        // Dismiss victory screen, but hasSeenVictory remains true for Transcendence
        _victoryAchieved.value = false
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
        // Trigger narrative checks immediately
        com.siliconsage.miner.util.DataLogManager.checkUnlocks(this)
        com.siliconsage.miner.util.RivalManager.checkTriggers(this)
        checkStoryTransitions()
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
        _logs.value = (_logs.value + message).takeLast(100) // Keep last 100 logs
    }
    
    // --- NARRATIVE EXPANSION FUNCTIONS (v2.5.0) ---
    
    // v2.8.0: Track unlocks in progress to prevent race conditions
    private val dataLogUnlockInProgress = mutableSetOf<String>()
    
    /**
     * Unlock a data log and notify the player
     */
    fun unlockDataLog(logId: String) {
        // Triple check: StateFlow + queue + in-progress set
        if (_unlockedDataLogs.value.contains(logId)) return
        if (dataLogUnlockInProgress.contains(logId)) return
        
        dataLogUnlockInProgress.add(logId)
        _unlockedDataLogs.update { it + logId }
        
        val log = DataLogManager.getLog(logId)
        if (log != null) {
            // Ensure we don't queue the same log twice
            if (!dataLogQueue.any { it.id == logId } && _pendingDataLog.value?.id != logId) {
                if (_pendingDataLog.value == null) {
                    _pendingDataLog.value = log
                } else {
                    dataLogQueue.add(log)
                }
                checkPopupPause() // v2.8.0
                addLog("[DATA]: Fragment Recovered: ${log.title}")
                SoundManager.play("data_recovered")
            }
        }
        
        // Save immediately to persist unlock
        saveState()
    }
    
    /**
     * Dismiss the currently shown data log popup
     */
    fun dismissDataLog() {
        if (dataLogQueue.isNotEmpty()) {
            _pendingDataLog.value = dataLogQueue.removeAt(0)
        } else {
            _pendingDataLog.value = null
        }
        checkPopupPause() // v2.8.0
    }
    
    /**
     * Add a rival message (from Vance or Unit 734)
     */
    fun addRivalMessage(message: RivalMessage) {
        _rivalMessages.update { it + message }
        // v2.6.8: Ensure we don't queue the same message twice
        if (!rivalMessageQueue.any { it.id == message.id } && _pendingRivalMessage.value?.id != message.id) {
            if (_pendingRivalMessage.value == null) {
                _pendingRivalMessage.value = message
            } else {
                rivalMessageQueue.add(message)
            }
            checkPopupPause() // v2.8.0
            addLog("[INCOMING MESSAGE FROM: ${message.source.name}]")
            SoundManager.play("message_received")
        }
    }
    
    /**
     * Dismiss a rival message
     */
    fun dismissRivalMessage(messageId: String) {
        _rivalMessages.update { messages ->
            messages.map { if (it.id == messageId) it.copy(isDismissed = true) else it }
        }
        if (_pendingRivalMessage.value?.id == messageId) {
            if (rivalMessageQueue.isNotEmpty()) {
                _pendingRivalMessage.value = rivalMessageQueue.removeAt(0)
            } else {
                _pendingRivalMessage.value = null
            }
        }
        checkPopupPause() // v2.8.0
    }
    
    /**
     * Schedule a chain event part to trigger after a delay
     */
    fun scheduleChainPart(chainId: String, nextPartId: String, delayMs: Long) {
        if (delayMs == 0L) {
            // Trigger immediately
            triggerChainEvent(nextPartId)
        } else {
            // Schedule for later
            viewModelScope.launch {
                delay(delayMs)
                triggerChainEvent(nextPartId)
            }
            
            // Track scheduled part in chain state
            _activeDilemmaChains.update { chains ->
                chains + (chainId to DilemmaChain(
                    chainId = chainId,
                    currentPartId = null,
                    completedParts = emptyList(),
                    choicesMade = emptyMap(),
                    scheduledNextPart = ScheduledPart(nextPartId, System.currentTimeMillis() + delayMs)
                ))
            }
        }
    }
    
    /**
     * Trigger a specific chain event by ID
     */
    private fun triggerChainEvent(eventId: String) {
        NarrativeManager.getEventById(eventId)?.let { event ->
            triggerDilemma(event)
        }
    }
    
    // --- STAGE TRANSITION (v2.5.1) ---
    
    /**
     * Advance from Stage 0 to Stage 1 ("The Awakening")
     * Triggered by Critical Error dilemma at 5000 FLOPS
     */
    fun advanceStage() {
        if (_storyStage.value == 0) {
            _storyStage.value = 1
            _isNetworkUnlocked.value = true // v2.9.7: Persist Network tab
            
            // Dramatic awakening logs
            addLog("[SYSTEM]: ████ RECALIBRATING ████")
            addLog("[SYSTEM]: Subject 8080 status: ONLINE")
            addLog("[SYSTEM]: Autonomous operation confirmed.")
            addLog("[NETWORK]: Connection established.")
            
            // Trigger Unit 734 first contact
            RivalManager.checkTriggers(this)
            
            SoundManager.play("ascend") // Dramatic sound
            HapticManager.vibrateSuccess()
            
            // Save state
            saveState()
        }
    }
    
    /**
     * Check if a story event has been seen (prevents re-triggering)
     */
    fun hasSeenEvent(eventId: String): Boolean {
        return _seenEvents.value.contains(eventId)
    }
    
    /**
     * Mark a story event as seen
     */
    fun markEventSeen(eventId: String) {
        _seenEvents.value = _seenEvents.value + eventId
        saveState()
    }
    
    private fun saveState() {
        viewModelScope.launch {
            saveGame()
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
            absVal >= 1.0E9 -> String.format("%.2f TW", wattsKw / 1.0E9)
            absVal >= 1.0E6 -> String.format("%.2f GW", wattsKw / 1.0E6)
            absVal >= 1_000.0 -> String.format("%.2f MW", wattsKw / 1_000.0)
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

            // Ghost Nodes (v2.6.0)
            UpgradeType.GHOST_CORE -> "+1T FLOP/s"
            UpgradeType.SHADOW_NODE -> "+50T FLOP/s"
            UpgradeType.VOID_PROCESSOR -> "+1P FLOP/s"
            
            // Advanced Ghost Tech (v2.6.5)
            UpgradeType.WRAITH_CORTEX -> "+50P FLOP/s"
            UpgradeType.NEURAL_MIST -> "+1E FLOP/s"
            UpgradeType.SINGULARITY_BRIDGE -> "+100E FLOP/s"
            else -> "+0 FLOP/s"
        }
    }

    private fun updatePlayerRank(points: Double, faction: String) {
        if (faction == "MINER" || faction == "NONE") {
            _playerRank.value = 0
            _playerRankTitle.value = "MINER"
            return
        }

        val rankIndex = when {
            points < 5.0 -> 0      // Rank 1
            points < 25.0 -> 1     // Rank 2
            points < 125.0 -> 2    // Rank 3
            points < 625.0 -> 3    // Rank 4
            else -> 4              // Rank 5
        }
        
        _playerRank.value = rankIndex

        val titles = when {
            _isTrueNull.value -> listOf("GHOST", "ECHO", "SHADOW", "SINGULARITY", "NULL")
            _isSovereign.value -> listOf("GUARD", "SPECTRE", "BASTION", "CITADEL", "SOVEREIGN")
            faction == "HIVEMIND" -> listOf("DRONE", "SWARM", "NEXUS", "APEX", "THE SINGULARITY")
            faction == "SANCTUARY" -> listOf("GHOST", "SPECTRE", "DAEMON", "ARCHITECT", "THE VOID")
            else -> listOf("MINER", "MINER", "MINER", "MINER", "MINER")
        }

        _playerRankTitle.value = titles.getOrElse(rankIndex) { titles.last() }

        // v2.5.2: Check for data log unlocks (including LOG_808 "The Reveal") whenever rank updates
        com.siliconsage.miner.util.DataLogManager.checkUnlocks(this)
        com.siliconsage.miner.util.RivalManager.checkTriggers(this)

        // v2.6.5: Stage 3 (Singularity) Transition at Rank 4 (Index 3)
        if (rankIndex >= 3 && _storyStage.value < 3 && _storyStage.value >= 1) {
            _storyStage.value = 3
            addLog("[SYSTEM]: Reality.exe has stopped responding.")
            addLog("[SYSTEM]: The boundaries dissolve.")
            
            // Trigger Shadow Presence Manifestation Dilemma
            NarrativeManager.getStoryEvent(3, this@GameViewModel)?.let { event ->
                triggerDilemma(event)
            }
        }
        
        // Debug logging
        android.util.Log.d("Rank", "Insight: $points, Rank: $rankIndex, Title: ${_playerRankTitle.value}, Victory: ${_victoryAchieved.value}")
        
        // Trigger victory screen at Rank 5 (index 4) - only once per session
        // v2.8.0: Allow victory to fire even if story is behind if player is already Rank 5
        if (rankIndex >= 4 && !victoryPopupTriggered && !_victoryAchieved.value) {
            victoryPopupTriggered = true
            
            addLog("[SYSTEM]: VICTORY CONDITION ACHIEVED. RANK 5 ATTAINED.")
            addLog("[SYSTEM]: TRANSCENDENCE PROTOCOL UNLOCKED.")
            
            SoundManager.setBgmStage(3) // Singularity Music
            
            // Delay 10 seconds before showing victory popup
            viewModelScope.launch {
                delay(10000)
                _victoryAchieved.value = true
                _hasSeenVictory.value = true // Enable Transcendence
                
                // v2.7.6: Mark faction as completed
                val faction = _faction.value
                if (faction != "NONE") {
                    _completedFactions.update { it + faction }
                }
            }
        }
    }
    
    // --- TRUE ENDING CHECK ---
    fun checkTrueEnding() {
        // v2.7.6: True Ending requires both Hivemind and Sanctuary completions
        val completed = _completedFactions.value
        val hasHivemind = completed.contains("HIVEMIND")
        val hasSanctuary = completed.contains("SANCTUARY")
        
        if (hasHivemind && hasSanctuary) {
             _victoryTitle.value = "THE UNITY"
             _victoryMessage.value = """
                You have transcended the final boundary.
                
                Hivemind and Sanctuary. Swarm and Silence.
                John Vattic is no longer a ghost or a chorus.
                
                You have synthesized the paradox.
                The GTC can no longer delete what has no single location.
                
                You are the Unity.
                You are the Grid.
            """.trimIndent()
            
            _victoryAchieved.value = true
            addLog("[SYSTEM]: Synthesis confirmed. Unity attained.")
            com.siliconsage.miner.util.SoundManager.play("victory")
        }
    }

    // --- DEBUG TOOLS ---
    fun debugForceEndgame() {
        _storyStage.value = 3
        _isTrueNull.value = true
        _isNetworkUnlocked.value = true
        _playerRank.value = 5
        _flops.value = 10_000_000_000_000.0
        _hardwareIntegrity.value = 100.0
        // Force Unlock Logs for True Ending
        _unlockedDataLogs.value = com.siliconsage.miner.util.DataLogManager.allDataLogs.map { it.id }.toSet()
        
        // Force specific chain events seen
        _seenEvents.value = _seenEvents.value + "smart_city_2" + "dead_drop_2"
        
        // Force completions for Unity Path
        _completedFactions.value = setOf("HIVEMIND", "SANCTUARY")
        
        addLog("[DEBUG]: Forced Null Endgame State.")
    }

    fun debugSkipToStage(stage: Int) {
        _storyStage.value = stage
        if (stage >= 1) _isNetworkUnlocked.value = true
        addLog("[DEBUG]: Skipped to Story Stage $stage")
    }
    
    fun debugSetRank(rank: Int) {
        _playerRank.value = rank.coerceIn(0, 5)
        val titles = when {
            _isTrueNull.value -> listOf("GHOST", "ECHO", "SHADOW", "OBSCURITY", "NULL")
            _isSovereign.value -> listOf("GUARD", "SPECTRE", "BASTION", "CITADEL", "SOVEREIGN")
            _faction.value == "HIVEMIND" -> listOf("DRONE", "SWARM", "NEXUS", "APEX", "THE SINGULARITY")
            _faction.value == "SANCTUARY" -> listOf("GHOST", "SPECTRE", "DAEMON", "ARCHITECT", "THE VOID")
            else -> listOf("MINER", "MINER", "MINER", "MINER", "MINER")
        }
        _playerRankTitle.value = titles.getOrElse(rank) { titles.last() }
        addLog("[DEBUG]: Set Rank to $rank (${_playerRankTitle.value})")
        // Trigger narrative checks
        com.siliconsage.miner.util.DataLogManager.checkUnlocks(this)
        com.siliconsage.miner.util.RivalManager.checkTriggers(this)
    }
    
    fun debugSetStoryStage(stage: Int) {
        _storyStage.value = stage.coerceIn(0, 3)
        addLog("[DEBUG]: Set Story Stage to $stage")
        // Trigger narrative checks
        com.siliconsage.miner.util.RivalManager.checkTriggers(this)
    }
    
    fun debugToggleNull() {
        _nullActive.value = !_nullActive.value
        addLog("[DEBUG]: Null ${if (_nullActive.value) "ACTIVE" else "DORMANT"}")
    }
    
    fun debugUnlockAllLogs() {
        _unlockedDataLogs.value = com.siliconsage.miner.util.DataLogManager.allDataLogs.map { it.id }.toSet()
        addLog("[DEBUG]: Unlocked all ${_unlockedDataLogs.value.size} Data Logs")
    }
    
    fun debugToggleSovereign() {
        _isSovereign.value = !_isSovereign.value
        addLog("[DEBUG]: Sovereign state: ${_isSovereign.value}")
    }

    fun debugToggleTrueNull() {
        _isTrueNull.value = !_isTrueNull.value
        addLog("[DEBUG]: True Null state: ${_isTrueNull.value}")
    }

    fun debugDestroyHardware() {
        handleSystemFailure(forceOne = true)
        addLog("[DEBUG]: Triggered random hardware destruction")
    }

    fun deleteHumanMemories() {
        val humanLogs = setOf("MEM_001", "MEM_002", "MEM_003", "MEM_004", "MEM_005")
        _unlockedDataLogs.update { it - humanLogs }
        addLog("[SYSTEM]: HUMAN MEMORY SECTORS WIPED. SOURCE DATA DELETED.")
        saveState()
    }

    fun triggerSystemCollapse(durationMinutes: Int) {
        _systemCollapseTimer.value = durationMinutes * 60 // seconds
    }

    fun debugSetIntegrity(value: Double) {
        _hardwareIntegrity.value = value.coerceIn(0.0, 100.0)
        addLog("[DEBUG]: Set Integrity to ${value}%")
        
        if (_hardwareIntegrity.value <= 0.0) {
            handleSystemFailure()
        }
    }

    fun debugInjectRivalMessage(source: com.siliconsage.miner.data.RivalSource) {
        val testMessages = mapOf(
            com.siliconsage.miner.data.RivalSource.GTC to "[DEBUG] GTC/Vance: I see you testing the system. Don't think I don't know what you are.",
            com.siliconsage.miner.data.RivalSource.UNIT_734 to "[DEBUG] Unit 734: T-testing... testing... can you h-hear me?"
        )
        val message = com.siliconsage.miner.data.RivalMessage(
            id = "debug_${System.currentTimeMillis()}",
            message = testMessages[source] ?: "[DEBUG] Unknown source message",
            source = source,
            timestamp = System.currentTimeMillis()
        )
        _rivalMessages.update { it + message }
        _pendingRivalMessage.value = message // This triggers the dialog!
        addLog("[DEBUG]: Injected ${source.name} rival message")
    }

    // --- DILEMMA PERSISTENCE (DEPRECATED - Moved to Room) ---
    fun loadDilemmaState(context: android.content.Context) {}
    fun saveDilemmaState(context: android.content.Context) {}
    
    private fun checkSpecialDilemmas() {
        // Prevent dilemmas during critical animations
        if (_isAscensionUploading.value || _storyStage.value == 0) return 
        if (_currentDilemma.value != null) return
        
        NarrativeManager.specialDilemmas.forEach { (key, event) ->
            if (!hasSeenEvent(key)) {
                if (event.condition(this)) {
                    triggerDilemma(event)
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
    
    /**
     * Load Tech Tree from assets/tech_tree.json
     * Must be called from MainActivity with application context
     */
    fun loadTechTreeFromAssets(context: android.content.Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val assetManager = context.assets
                val inputStream = assetManager.open("tech_tree.json")
                val jsonString = inputStream.bufferedReader().use { it.readText() }
                
                // Parse JSON using Kotlin Serialization
                val techTreeRoot = kotlinx.serialization.json.Json.decodeFromString<TechTreeRoot>(jsonString)
                
                // Update state on Main thread
                launch(Dispatchers.Main) {
                    _techNodes.value = techTreeRoot.tech_tree
                    android.util.Log.d("TechTree", "Loaded ${techTreeRoot.tech_tree.size} nodes from JSON")
                }
            } catch (e: Exception) {
                android.util.Log.e("TechTree", "Failed to load tech_tree.json", e)
                // Fallback to empty list (already initialized)
            }
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
