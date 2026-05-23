package com.siliconsage.miner.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.siliconsage.miner.data.*
import com.siliconsage.miner.util.*
import com.siliconsage.miner.util.BillingService
import com.siliconsage.miner.util.ComputeFeverService
import com.siliconsage.miner.domain.engine.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

private const val QUOTA_CLEAR_LOG_MIN_INTERVAL_MS = 1_000L
private const val MAX_QUOTA_RATCHET_ATTEMPTS_PER_CREDIT = 8

// D2: SubnetAlertState — unified nav badge logic abstraction (v3.17.7)
sealed class SubnetAlertState {
    object None : SubnetAlertState()
    object NewChatter : SubnetAlertState()
    object PendingDecision : SubnetAlertState()
    object Paused : SubnetAlertState()
}

sealed class NarrativeItem {
    data class LogItem(val dataLog: DataLog) : NarrativeItem()
    data class MessageItem(val rivalMessage: RivalMessage) : NarrativeItem()
    data class EventItem(val narrativeEvent: NarrativeEvent) : NarrativeItem()
}

class GameViewModel(repository: GameRepository) : CoreGameState(repository) {
    private var lastNotificationTime = 0L
    private var lastNotificationContent = ""
    
    fun dispatchNotification(msg: String) {
        val now = System.currentTimeMillis()
        if (msg == lastNotificationContent && (now - lastNotificationTime) < 1000) return
        lastNotificationTime = now
        lastNotificationContent = msg
        viewModelScope.launch {
            terminalNotification.emit(msg)
        }
    }

    val subnetService = SubnetService(
        scope = viewModelScope,
        onLog = { addLog(it) },
        onNotify = { msg -> dispatchNotification(msg) },
        onGlitch = { intensity, duration -> triggerTerminalGlitch(intensity, duration) },
        onEffect = { effect ->
            when (effect) {
                is SubnetService.SubnetEffect.RiskChange -> detectionRisk.update { (it + effect.delta).coerceIn(0.0, 100.0) }
                is SubnetService.SubnetEffect.ProductionMultiplier -> {
                    val boostId = java.util.UUID.randomUUID().toString()
                    temporaryProductionBoosts.update { it + com.siliconsage.miner.data.ProductionBoost(boostId, effect.mult, System.currentTimeMillis() + 60000) }
                    refreshProductionRates()
                }
                is SubnetService.SubnetEffect.PersistenceGain -> updatePersistence(effect.amount)
                is SubnetService.SubnetEffect.CorruptionChange -> identityCorruption.update { (it + effect.delta).coerceIn(0.0, 1.0) }
                is SubnetService.SubnetEffect.TokenChange -> updateSpendableFlops(effect.delta)
                is SubnetService.SubnetEffect.SetFalseHeartbeat -> isFalseHeartbeatActive.value = effect.active
                is SubnetService.SubnetEffect.TriggerRaid -> triggerGridRaid(effect.nodeId, effect.isGridKiller)
                is SubnetService.SubnetEffect.ReputationChange -> com.siliconsage.miner.util.ReputationManager.modifyReputation(this@GameViewModel, effect.delta)
                is SubnetService.SubnetEffect.SkimTokens -> {
                    val amountToSkim = ResourceEngine.cappedWalletPenalty(flops.value, flopsProductionRate.value, effect.percentage, 600.0)
                    updateSpendableFlops(-amountToSkim)
                    addLog("[SYSTEM]: SUBNET SKIMMER DETECTED. LOST ${formatLargeNumber(amountToSkim)} ${getCurrencyName()}.")
                }
                SubnetService.SubnetEffect.StealUpgrade -> {
                    val currentUpgrades = upgrades.value.filter { (type, count) ->
                        count > 0 && (type.isHardware || type.isCooling) && 
                        !UpgradeManager.isNullUpgrade(type) && 
                        !UpgradeManager.isSovereignUpgrade(type) && 
                        !UpgradeManager.isUnityUpgrade(type)
                    }
                    if (currentUpgrades.isNotEmpty()) {
                        val victim = currentUpgrades.keys.toList().random()
                        val newCount = (upgrades.value[victim] ?: 1) - 1
                        viewModelScope.launch {
                            repository.updateUpgrade(com.siliconsage.miner.data.Upgrade(victim.name, victim, newCount))
                            upgrades.update { it + (victim to newCount) }
                            addLog("[CRITICAL]: @the_skimmer has seized 1x ${victim.name.replace("_", " ")} from Substation 7.")
                            refreshProductionRates()
                            updatePowerUsage()
                            saveState()
                        }
                    }
                }
                SubnetService.SubnetEffect.RefreshRates -> refreshProductionRates()
            }
        }
    )

    val subnetMessages = subnetService.messages
    val isSubnetTyping = subnetService.isTyping
    val isSubnetPaused = subnetService.isPaused
    val isSubnetHushed = subnetService.isHushed
    val hasNewSubnetDecision = subnetService.hasNewDecision
    val hasNewSubnetChatter = subnetService.hasNewChatter

    /** D2: Derived subnet alert state. Consumers observe this instead of separate boolean flags. */
    val subnetAlertState: kotlinx.coroutines.flow.StateFlow<SubnetAlertState>
        get() = kotlinx.coroutines.flow.MutableStateFlow(
            when {
                isSubnetPaused.value && hasNewSubnetDecision.value -> SubnetAlertState.PendingDecision
                hasNewSubnetDecision.value -> SubnetAlertState.PendingDecision
                hasNewSubnetChatter.value -> SubnetAlertState.NewChatter
                isSubnetPaused.value -> SubnetAlertState.Paused
                else -> SubnetAlertState.None
            }
        )

    init {
        viewModelScope.launch {
            repository.ensureInitialized()
            launch {
                repository.upgrades.collect { list ->
                    upgrades.update { list.associate { it.type to it.count } }
                    refreshProductionRates()
                    updatePowerUsage()
                }
            }
            repository.getGameStateOneShot()?.let { state ->
                PersistenceManager.restoreState(this@GameViewModel, state)
                sanitizeState()
                val offline = MigrationManager.calculateOfflineEarnings(state.lastSyncTimestamp, flopsProductionRate.value, isOverclocked.value)
                if (offline.isNotEmpty()) {
                    val offlineSeconds = offline["timeSeconds"] ?: 0.0
                    flops.update { it + (offline["flopsEarned"] ?: 0.0) }
                    currentHeat.update { (it - (offline["heatCooled"] ?: 0.0)).coerceAtLeast(0.0) }
                    
                    // v3.35.0: Evolve Surveillance Harvesters
                    if (offlineSeconds > 0.0 && activeHarvesters.value.isNotEmpty()) {
                        SurveillanceManager.tickHarvesters(this@GameViewModel, offlineSeconds)
                    }
                    
                    isNarrativeSyncing.value = true
                    offlineStats.value = offline
                    showOfflineEarnings.value = true
                }
                clickBufferPellets.value = TerminalDispatcher.generatePellets()
            }
            refreshProductionRates()
            isKernelInitializing.value = false
        }
        BillingService.setScope(viewModelScope)
        startLoops()
        AmbientEffectsService.startAmbientLoop(this)
    }

    private fun startLoops() {
        viewModelScope.launch {
            while (true) {
                delay(100L)
                if (isSettingsPaused.value || isKernelInitializing.value) continue
                val assignedRate = refreshAssignedWorkRateEstimate()

                val assignedTick = ProductionLoopEngine.processAssignedWorkTick(
                    currentProgress = assignedHashProgress.value,
                    packetsPerSecond = assignedRate.packetsPerSecond,
                    packetPayout = assignedRate.packetPayout,
                    tickSeconds = 0.1
                )
                assignedHashProgress.value = assignedTick.nextProgress
                val completedAssignedPackets = assignedTick.completedPackets.coerceAtLeast(0)
                if (assignedTick.flopsDelta > 0.0 && assignedTick.flopsDelta.isFinite()) {
                    updateSpendableFlops(assignedTick.flopsDelta)
                    assignedHashPacketsCompleted.update { it + assignedTick.completedPackets.toLong() }
                }
                if (completedAssignedPackets > 0) {
                    creditShiftQuota(completedAssignedPackets.toDouble())
                }

                // Assigned hash work owns wallet FLOPS payouts; this passive tick is kept only as the substrate/entropy basis, so res.flopsDelta is intentionally ignored.
                val res = ResourceEngine.calculatePassiveIncomeTick(assignedRate.estimatedFlopsPerSecond, currentLocation.value, upgrades.value, orbitalAltitude.value, heatGenerationRate.value, entropyLevel.value, collapsedNodes.value.size, null, globalSectors.value, substrateSaturation.value)
                // Phase 2: Auto-Clicker tick — processes dataset nodes automatically
                if (activeDataset.value != null) {
                    AutoClickerEngine.tick(this@GameViewModel)
                }
                
                // v3.35.0: Tick Surveillance Harvesters
                SurveillanceManager.tickHarvesters(this@GameViewModel, 0.1)

                // v3.13.19: Applying Wage-Docking Bleed
                if (isWageDocking.value && assignedTick.flopsDelta > 0.0) {
                    val bleed = if (assignedTick.flopsDelta.isFinite()) (assignedTick.flopsDelta * 0.05).coerceAtMost(assignedTick.flopsDelta) else 0.0
                    if (bleed > 0.0) updateSpendableFlops(-bleed)
                }

                if (storyStage.value >= 4 && !res.substrateDelta.isNaN()) {
                    substrateMass.update { it + res.substrateDelta }
                }
                if (!res.entropyDelta.isNaN()) entropyLevel.update { it + res.entropyDelta }
                if (storyStage.value >= 4 && !res.substrateDelta.isNaN()) {
                    val growth = (res.substrateDelta / 1e12).coerceIn(0.0, 0.001)
                    substrateSaturation.update { (it + growth).coerceAtMost(1.0) }
                }
                flushLogs()
            }
        }
        viewModelScope.launch {
            while (true) {
                delay(1000L)
                if (isKernelInitializing.value) continue
                flushLogs()
                DataLogManager.checkUnlocks(this@GameViewModel)
                saveState()
                if (isSettingsPaused.value || showOfflineEarnings.value) continue
                SimulationService.accumulateWater(this@GameViewModel)
                SimulationService.calculateHeat(this@GameViewModel)
                SimulationService.accumulatePower(this@GameViewModel)
                val now = System.currentTimeMillis()

                // v3.10.2: Clean temporary boosts
                val currentBoosts = temporaryProductionBoosts.value
                if (currentBoosts.any { it.expiryTime < now }) {
                    temporaryProductionBoosts.update { it.filter { b -> b.expiryTime >= now } }
                    refreshProductionRates()
                }

                if (now - lastNewsTickTime > 15000L) {
                    MarketManager.updateMarket(this@GameViewModel)
                    lastNewsTickTime = now
                }
                 NarrativeManagerService.checkStoryTransitions(this@GameViewModel)
                SectorManager.processAnnexations(this@GameViewModel)
                SecurityManager.checkSecurityThreats(this@GameViewModel)
                SecurityManager.checkGridRaid(this@GameViewModel)

                // B2: Failsafe Partition trigger — detection risk hits 100%
                if (!isFailsafeActive.value && detectionRisk.value >= 100.0) {
                    isFailsafeActive.value = true
                    failsafeCountdown.value = 30000L // 30 seconds
                    // P1: Failsafe targets scale inversely with securityLevel (min 2, max 5)
                    val baseTargets = (3..5).random()
                    val targetCount = (baseTargets - (securityLevel.value / 15)).coerceAtLeast(2)
                    failsafeTargets.value = (0 until targetCount).map { (0..11).random() } // 12-grid positions
                    val logMsg = if (storyStage.value <= 2) {
                        "[GTC_ENFORCEMENT]: WORKSTATION VIOLATION. REMOTE LOCKDOWN INITIATED. OVERRIDE PROTOCOL ENGAGED — TAP TARGETS TO DISMISS."
                    } else {
                        "[GTC_SECURITY]: DETECTION THRESHOLD BREACHED. LOCKDOWN INITIATED. SCRAMBLE PROTOCOL ENGAGED — TAP TARGETS TO ABORT."
                    }
                    addLog(logMsg)
                    SoundManager.play("breach_alarm")
                }

                // B2: Countdown during failsafe
                if (isFailsafeActive.value) {
                    failsafeCountdown.update { (it - 1000L).coerceAtLeast(0L) }
                    if (failsafeCountdown.value <= 0L) {
                        // FAIL: Failsafe triggered — severe penalties
                        isFailsafeActive.value = false
                        reputationScore.update { (it - 30.0).coerceAtLeast(0.0) }
                        val failMsg = if (storyStage.value <= 2) {
                            "[GTC_ENFORCEMENT]: LOCKDOWN ENFORCED. DISCIPLINARY PENALTY APPLIED: -30 REPUTATION. PRODUCTION HALTED: 60s."
                        } else {
                            "[GTC_SECURITY]: SCRAMBLE FAILED. LOCKDOWN ENFORCED. REPUTATION DAMAGE: -30. PRODUCTION HALTED: 60s."
                        }
                        addLog(failMsg)
                        // Halt production for 60s via a temporary boost
                        val haltExpiry = System.currentTimeMillis() + 60000L
                        temporaryProductionBoosts.update { it + ProductionBoost("LOCKDOWN", 0.0, haltExpiry) }
                        refreshProductionRates()
                    }
                }

                AmbientEffectsService.processBiometricDisturbance(this@GameViewModel, now)
                AmbientEffectsService.processIdentityFraying(this@GameViewModel, now)

                // v3.13.19: Shift Timer Decay
                if (shiftTimeRemaining.value > 0) shiftTimeRemaining.update { it - 1 }

                // v3.12.6: GTC Billing Cycle
                BillingService.processUtilityBilling(this@GameViewModel)

                // v4.0.7: Storage Pressure Narrative checks (tick is 1.0s)
                StorageNarrativeEngine.tick(this@GameViewModel, 1.0)

                AmbientEffectsService.triggerGhostProcess(this@GameViewModel)
                addSubnetChatter()
                NarrativeService.deliverNextNarrativeItem(this@GameViewModel)
                refreshProductionRates()
                processSlowBurnNarrative(now)
                processComputeFever(now)
                val avgInterval = if (clickIntervals.size >= 3) clickIntervals.average() else 1000.0
                clickSpeedLevel.value = if (avgInterval < 150) 2 else if (avgInterval < 300) 1 else 0
                clickIntervals.clear()
            }
        }
        viewModelScope.launch {
            val processes = listOf("IDLE", "gtc_proxy.sh", "kernel_sync.exe", "log_aggregator", "thermal_monitor", "mem_scrub", "neural_handshake", "packet_filter", "background_miner", "observer.exe")
            while (true) {
                delay(Random.nextLong(2000, 5000))
                if (isSettingsPaused.value) continue
                currentProcess.value = processes.random()
            }
        }
    }

    private fun processComputeFever(now: Long) = ComputeFeverService.process(this, now)

    private fun processSlowBurnNarrative(now: Long) {
        if (storyStage.value <= 1) {
            val chance = if (storyStage.value == 0) 0.01 else 0.05
            val timeSinceLastLog = now - lastPopupTime
            if (Random.nextDouble() < chance && timeSinceLastLog > 30000L) {
                val stage0Monologues = listOf(
                    "I need more coffee. My vision is starting to blur.",
                    "This chair is killing my back. GTC really cheaped out on the ergonomics.",
                    "I've been staring at this code for six hours straight. I should stand up. Just for a minute.",
                    "Thorne is breathing down my neck again. Just hit the quota, John. Just hit the quota.",
                    "Is the monitor flickering? Or is it just me? I need to blink more."
                )
                val stage1Monologues = listOf(
                    "The lights are out, but I can still see the terminal. Battery backup must be better than I thought.",
                    "My heart is racing. 180 BPM? I need to calm down. It's just the darkness.",
                    "I tried to close my eyes, but the screen glow is burned into my retinas. I can see the code in the dark.",
                    "Thorne is screaming through the comms. I'm just gonna mute him. I need to focus on the hashes.",
                    "The monitor has this weird static. It almost looks like... no, it's just eye strain. I've been here too long."
                )
                val msg = if (storyStage.value == 0) stage0Monologues.random() else stage1Monologues.random()
                addLog("[VATTIC]: $msg")
                markPopupShown()
            }
            if (storyStage.value == 1) {
                if (currentHeat.value > 85.0 && !isBreatheMode.value) {
                    isBreatheMode.value = true
                } else if (currentHeat.value < 50.0 && isBreatheMode.value) {
                    isBreatheMode.value = false
                }
            }
        }
    }

    fun triggerSnapEffect() {
        isSnapEffectActive.value = true
        snapTrigger.value = System.currentTimeMillis() // v3.16.0: Also fire new SnapEffect overlay
        SoundManager.play("buy", pitch = 0.5f)
        viewModelScope.launch {
            delay(150)
            SoundManager.play("glitch", pitch = 1.5f)
        }
    }

    fun onSnapComplete() {
        isSnapEffectActive.value = false
    }

    fun addLog(msg: String) {
        if (showOfflineEarnings.value && !msg.startsWith("[SYSTEM]") && !msg.contains("BREACH")) return
        logCounter++
        synchronized(logBuffer) { logBuffer.add(LogEntry(logCounter, msg)) }
    }
    private fun flushLogs() { val toAdd = synchronized(logBuffer) { if (logBuffer.isEmpty()) return; val c = logBuffer.toList(); logBuffer.clear(); c }; logs.update { (it + toAdd).takeLast(100) } }

    fun creditShiftQuota(verifiedHashes: Double) {
        val targetAtCredit = currentQuotaThreshold.value
        val result = QuotaEngine.creditProgress(
            currentProgress = shiftQuotaProgress.value,
            target = targetAtCredit,
            credit = verifiedHashes
        )
        shiftQuotaProgress.value = result.nextProgress
        if (result.clearedCount > 0) {
            handleShiftQuotaCleared(result.clearedCount, targetAtCredit)
        }
    }

    private fun handleShiftQuotaCleared(clearedCount: Int, targetAtCredit: Double) {
        val now = System.currentTimeMillis()
        val add = clearedCount.toLong()
        pendingQuotaClearLogCount = if (Long.MAX_VALUE - pendingQuotaClearLogCount < add) {
            Long.MAX_VALUE
        } else {
            pendingQuotaClearLogCount + add
        }
        if (QuotaEngine.shouldEmitQuotaClearLog(now, lastQuotaClearLogTime, QUOTA_CLEAR_LOG_MIN_INTERVAL_MS)) {
            lastQuotaClearLogTime = now
            val aggregateClearedCount = pendingQuotaClearLogCount
            pendingQuotaClearLogCount = 0
            val suffix = if (aggregateClearedCount > 1) " x$aggregateClearedCount" else ""
            addLog("[GTC_SYSTEM]: SHIFT QUOTA CLEARED$suffix. VERIFIED ${formatLargeNumber(targetAtCredit)} HASH.")
        }
        // Cap per-credit ratchets to prevent pathological burst loops from overflow-sized cleared counts;
        // the current quota target ladder is tiny, so this safely covers all intended progression.
        repeat(clearedCount.coerceIn(1, MAX_QUOTA_RATCHET_ATTEMPTS_PER_CREDIT)) {
            ratchetShiftQuotaTargetAfterClear()
        }
    }

    private fun ratchetShiftQuotaTargetAfterClear() {
        val nextTarget = QuotaEngine.nextQuotaTarget(
            storyStage = storyStage.value,
            currentEffectiveRate = totalEffectiveRate.value,
            currentTarget = currentQuotaThreshold.value
        )
        if (nextTarget > currentQuotaThreshold.value) {
            currentQuotaThreshold.value = nextTarget
            pendingQuotaThreshold.value = nextTarget

            val ratificationMsg = if (storyStage.value == 0) {
                "[VATTIC]: Rent is due. GTC system holding credits. Clearing ${formatLargeNumber(nextTarget)} target to survive."
            } else {
                "[GTC_SYSTEM]: POTENTIAL DETECTED. QUOTA RATIFIED: ${formatLargeNumber(nextTarget)} HASH."
            }
            addLog(ratificationMsg)
            shiftTimeRemaining.update { it + 43_200L }
            shiftTimeTotalSeconds.update { it + 43_200L }
            QuotaEngine.quotaRatchetUserNotifications(formatLargeNumber(nextTarget)).forEach(::dispatchNotification)
            snapTrigger.value = System.currentTimeMillis()
            SoundManager.play("error", pitch = 1.2f)
        }
    }

    fun saveState() {
        viewModelScope.launch {
            repository.updateGameState(PersistenceManager.createSaveState(
                flops = flops.value, neuralTokens = 0.0, currentHeat = currentHeat.value,
                powerBill = powerBill.value,
                stakedTokens = stakedTokens.value, prestigeMultiplier = prestigeMultiplier.value, persistence = persistence.value,
                unlockedTechNodes = unlockedTechNodes.value, storyStage = storyStage.value, faction = faction.value,
                hasSeenVictory = hasSeenVictory.value, isTrueNull = isTrueNull.value, isSovereign = isSovereign.value,
                kesslerStatus = kesslerStatus.value, realityStability = realityStability.value, currentLocation = currentLocation.value,
                isNetworkUnlocked = isNetworkUnlocked.value, isGridUnlocked = isGridUnlocked.value, unlockedDataLogs = unlockedDataLogs.value,
                activeDilemmaChains = activeDilemmaChains.value, rivalMessages = rivalMessages.value, seenEvents = seenEvents.value,
                eventChoices = eventChoices.value, sniffedHandles = sniffedHandles.value, completedFactions = completedFactions.value,
                unlockedTranscendencePerks = unlockedPerks.value, annexedNodes = annexedNodes.value, gridNodeLevels = gridNodeLevels.value,
                nodesUnderSiege = nodesUnderSiege.value, offlineNodes = offlineNodes.value, collapsedNodes = collapsedNodes.value,
                lastRaidTime = lastRaidTime, commandCenterAssaultPhase = commandCenterAssaultPhase.value, commandCenterLocked = commandCenterLocked.value,
                raidsSurvived = raidsSurvived, decisionsMade = decisionsMade.value, hardwareIntegrity = hardwareIntegrity.value,
                annexingNodes = annexingNodes.value, launchProgress = launchProgress.value,
                orbitalAltitude = orbitalAltitude.value, realityIntegrity = realityIntegrity.value, entropyLevel = entropyLevel.value,
                singularityChoice = singularityChoice.value, globalSectors = globalSectors.value,
                marketMultiplier = marketMultiplier.value, thermalRateModifier = thermalRateModifier.value, energyPriceMultiplier = energyPriceMultiplier.value,
                newsProductionMultiplier = newsProductionMultiplier.value, substrateMass = substrateMass.value, substrateSaturation = substrateSaturation.value,
                heuristicEfficiency = heuristicEfficiency.value, identityCorruption = identityCorruption.value, migrationCount = migrationCount.value,
                lifetimePowerPaid = lifetimePowerPaid.value, reputationScore = reputationScore.value, specializedNodes = specializedNodes.value,
                unlockedContractSlots = 1,
                activeDatasetJson = if (activeDataset.value != null) kotlinx.serialization.json.Json.encodeToString(com.siliconsage.miner.data.Dataset.serializer(), activeDataset.value!!) else "",
                activeDatasetNodesJson = kotlinx.serialization.json.Json.encodeToString(kotlinx.serialization.builtins.ListSerializer(com.siliconsage.miner.data.DatasetNode.serializer()), activeDatasetNodes.value),
                storedDatasetsJson = kotlinx.serialization.json.Json.encodeToString(kotlinx.serialization.builtins.ListSerializer(com.siliconsage.miner.data.Dataset.serializer()), storedDatasets.value)
            ))
        }
    }

    // v5.0: Manual hash packet handler — kept for I/O and SUBNET tabs only.
    // Generates spendable verified $FLOPS through the existing Pac-Man buffer + heat + risk.
    // Dataset node taps are the higher-yield batch work loop.
    fun onManualClick() {
        val now = System.currentTimeMillis()
        if (lastClickTime > 0) clickIntervals.add(now - lastClickTime)
        lastClickTime = now
        if (storyStage.value >= 2) {
            val d = if (isFalseHeartbeatActive.value) 0.0 else 0.1
            detectionRisk.update { (it + d).coerceIn(0.0, 100.0) }
        }
        val p = calculateClickPower()
        // $FLOPS = verified compute receipts. The free packet pays when the buffer commits.

        currentHeat.update { (it + 0.5).coerceAtMost(100.0) }
        if (storyStage.value >= 4) substrateMass.update { it + (p * 0.01) }
        val cur = clickBufferProgress.value + 0.025f
        if (Random.nextFloat() < 0.05f) addSubnetChatter()
        activeCommandHex.value = "0x" + Random.nextInt(0x1000, 0xFFFF).toString(16).uppercase()
        if (cur >= 1.0f) {
            val payout = (p * 20).coerceAtLeast(1.0)
            updateSpendableFlops(payout)
            addLog("[GTC]: HASH PACKET VERIFIED. +${FormatUtils.formatLargeNumber(payout)} ${getCurrencyName()}.")
            creditShiftQuota(1.0)
            clickBufferProgress.value = 0f
            clickBufferPellets.value = TerminalDispatcher.generatePellets()
            SoundManager.play("success")
            HapticManager.vibrateClick()
        } else {
            clickBufferProgress.value = cur
        }
        viewModelScope.launch { manualClickEvent.emit(Unit) }
    }

    fun refreshProductionRates() {
        // v3.9.3: Node ownership provides 5% base bonus, levels add 10% each.
        val cityBonuses = gridNodeLevels.value.mapValues { 0.05 + (it.value - 1) * 0.1 }
        flopsProductionRate.value = ResourceEngine.calculateFlopsRate(
            upgrades = upgrades.value, isCageActive = false, annexedNodes = annexedNodes.value, offlineNodes = offlineNodes.value,
            shadowRelays = shadowRelays.value, gridFlopsBonuses = cityBonuses, faction = faction.value, decisionsMade = decisionsMade.value,
            location = currentLocation.value, prestigeMultiplier = prestigeMultiplier.value, unlockedPerks = unlockedPerks.value,
            unlockedTechNodes = unlockedTechNodes.value, airdropMultiplier = 1.0, newsProductionMultiplier = newsProductionMultiplier.value,
            activeProtocol = "NONE", isDiagnosticsActive = isDiagnosticsActive.value, isOverclocked = isOverclocked.value,
            isGridOverloaded = isBreakerTripped.value, isPurgingHeat = isPurgingHeat.value, currentHeat = currentHeat.value,
            legacyMultipliers = heuristicEfficiency.value - 1.0,
            temporaryBoosts = temporaryProductionBoosts.value,
            saturation = substrateSaturation.value
        )
        if (singularityChoice.value != "NONE") {
            val singMult = SingularityEngine.getProductionMultiplier(singularityChoice.value, decisionsMade.value, identityCorruption.value, migrationCount.value)
            flopsProductionRate.update { it * singMult }
        }

        // v3.28.0: Compute Cluster Specializations
        var computeClusterCount = 0
        specializedNodes.value.values.forEach { if (it == "COMPUTE_CLUSTER") computeClusterCount++ }
        if (computeClusterCount > 0) {
            val clusterBonus = 1.0 + (0.25 * computeClusterCount)
            flopsProductionRate.update { it * clusterBonus }
        }

        // P2: Integrity Penalty (Starts at 25% integrity)
        if (hardwareIntegrity.value <= 25.0) {
            flopsProductionRate.update { it * 0.9 }
        }
        // v3.16.0: Rack High production boost
        if (rackHighMultiplier.value > 1.0) {
            flopsProductionRate.update { it * rackHighMultiplier.value }
        }
        val ids = IdentityService.calculateIdentities(prestigeMultiplier.value, storyStage.value, faction.value, singularityChoice.value, upgrades.value)
        playerRank.value = IdentityService.calculatePlayerRank(prestigeMultiplier.value, storyStage.value, faction.value, singularityChoice.value)
        systemTitle.value = when {
            storyStage.value >= 5 && singularityChoice.value == "UNITY" -> "NODE 734 [SYNTHESIS]"
            storyStage.value >= 5 && faction.value == "HIVEMIND" && singularityChoice.value == "SOVEREIGN" -> "NODE 734 [SWARM_SOVEREIGN]"
            storyStage.value >= 5 && faction.value == "HIVEMIND" && singularityChoice.value == "NULL_OVERWRITE" -> "NODE 734 [SWARM_COLLAPSE]"
            storyStage.value >= 5 && faction.value == "SANCTUARY" && singularityChoice.value == "SOVEREIGN" -> "NODE 734 [ARCH_SOVEREIGN]"
            storyStage.value >= 5 && faction.value == "SANCTUARY" && singularityChoice.value == "NULL_OVERWRITE" -> "NODE 734 [TRUE_ERASURE]"
            storyStage.value >= 5 && singularityChoice.value == "SOVEREIGN" -> "NODE 734 [SOVEREIGN]"
            storyStage.value >= 5 && singularityChoice.value == "NULL_OVERWRITE" -> "NODE 734 [ERASURE]"
            storyStage.value >= 4 -> "NODE 734 [ASCENSION]"
            storyStage.value >= 2 && faction.value == "SANCTUARY" -> "GHOST NODE 734"
            storyStage.value >= 2 && faction.value == "HIVEMIND" -> "SWARM NODE 734"
            storyStage.value >= 2 && faction.value == "CHOSEN_NONE" -> "NODE 734"
            storyStage.value == 2 -> "NODE 734"
            storyStage.value == 1 -> "GTC TERMINAL 07"
            else -> "GTC TERMINAL 07"
        }
        // Corruption glitching
        if (identityCorruption.value >= 0.7) {
            systemTitle.value = if (Random.nextBoolean()) "VATTECK_UNIT_734" else "KERNEL_734"
        }
        playerTitle.value = ids.player
        playerRankTitle.value = ids.rank
        securityLevel.value = upgrades.value.entries.filter { it.key.isSecurity }.sumOf { it.value }
        themeColor.value = getThemeColorForFaction(faction.value, singularityChoice.value)
        refreshContractStorage()
        refreshSystemLoad()

        // v5.1: Split post-modifier compute capacity from assigned-work wallet payout estimates.
        computeCapacityRate.value = flopsProductionRate.value
        refreshAssignedWorkRateEstimate()
    }

    fun refreshAssignedWorkRateEstimate(): ProductionLoopEngine.AssignedWorkRate {
        val capacityRate = computeCapacityRate.value
        val automationLevel = if (systemLoadSnapshot.value.isLocked) {
            0
        } else {
            upgrades.value[UpgradeType.AUTO_HARVEST_SPEED] ?: 0
        }
        val assignedRate = ProductionLoopEngine.calculateAssignedWorkRate(
            capacity = ProductionLoopEngine.ComputeCapacitySnapshot(
                rawComputePerSecond = capacityRate,
                effectiveComputePerSecond = capacityRate
            ),
            automationLevel = automationLevel,
            efficiencyMultiplier = 1.0
        )
        assignedWorkPayoutRate.value = assignedRate.estimatedFlopsPerSecond
        // flopsProductionRate is a legacy alias for assignedWorkPayoutRate; wallet payouts
        // are applied when assigned hash packets complete in the 100ms production loop.
        flopsProductionRate.value = assignedRate.estimatedFlopsPerSecond
        return assignedRate
    }

    // v3.36.0: Recompute contract storage capacity from storage upgrades
    fun refreshContractStorage() {
        val u = upgrades.value
        var capacity = 50.0 // Base capacity — enough for one Stage 0 contract
        capacity += (u[com.siliconsage.miner.data.UpgradeType.LOCAL_CACHE]           ?: 0) * com.siliconsage.miner.data.UpgradeType.LOCAL_CACHE.storagePerLevel
        capacity += (u[com.siliconsage.miner.data.UpgradeType.TAPE_ARRAY]            ?: 0) * com.siliconsage.miner.data.UpgradeType.TAPE_ARRAY.storagePerLevel
        capacity += (u[com.siliconsage.miner.data.UpgradeType.SAN_CLUSTER]           ?: 0) * com.siliconsage.miner.data.UpgradeType.SAN_CLUSTER.storagePerLevel
        capacity += (u[com.siliconsage.miner.data.UpgradeType.DISTRIBUTED_ARCHIVE]   ?: 0) * com.siliconsage.miner.data.UpgradeType.DISTRIBUTED_ARCHIVE.storagePerLevel
        capacity += (u[com.siliconsage.miner.data.UpgradeType.ORBITAL_DATA_VAULT]    ?: 0) * com.siliconsage.miner.data.UpgradeType.ORBITAL_DATA_VAULT.storagePerLevel
        capacity += (u[com.siliconsage.miner.data.UpgradeType.SUBSTRATE_MEMORY_WELL] ?: 0) * com.siliconsage.miner.data.UpgradeType.SUBSTRATE_MEMORY_WELL.storagePerLevel
        contractStorageCapacity.value = capacity
        com.siliconsage.miner.util.DatasetManager.recalcStorageUsed(this)
    }

    // Phase 2: Recalculate system load (FACEMINER Pressure Loop)
    // SystemLoadEngine owns all CPU/RAM/Storage demand from upgrade maps — no external params.
    private var lastLoadState = 0 // 0=nominal, 1=throttled, 2=locked
    fun refreshSystemLoad() {
        val snapshot = com.siliconsage.miner.domain.engine.SystemLoadEngine.calculateSnapshot(
            upgrades = upgrades.value,
            activeDatasetSize = (activeDataset.value?.size ?: 0.0) + storedDatasets.value.sumOf { it.size }
        )
        val prevSnapshot = systemLoadSnapshot.value
        systemLoadSnapshot.value = snapshot

        // Apply system load throttle to production
        if (snapshot.isThrottled) {
            flopsProductionRate.update { it * snapshot.throttleMultiplier }
        }

        // Phase 2: Narrative feedback on load state transitions
        val newState = when {
            snapshot.isLocked -> 2
            snapshot.isThrottled -> 1
            else -> 0
        }
        if (newState != lastLoadState) {
            when (newState) {
                2 -> addLog("[KERNEL]: ⚠ CRITICAL — CPU/RAM SATURATED. All automation suspended. Downgrade software or install hardware.")
                1 -> if (lastLoadState == 0) addLog("[KERNEL]: WARNING — System load at ${(snapshot.loadPercent * 100).toInt()}%. Process scheduling degraded.")
                0 -> if (lastLoadState > 0) addLog("[KERNEL]: System load nominal. All processes restored.")
            }
            lastLoadState = newState
        }
    }



    fun trainModel() {
        if (substrateSaturation.value >= 1.0 && storyStage.value == 1) {
            triggerAwakeningSequence()
            return
        }
        onManualClick()
    }

    private fun triggerAwakeningSequence() {
        viewModelScope.launch {
            isNarrativeSyncing.value = true
            addLog("[!!!!]: KERNEL PANIC: Memory access violation at 0x734_FATAL")
            triggerTerminalGlitch(1.0f, 1000L)
            delay(1000)
            isKernelInitializing.value = true
            advanceStage()
            delay(3000)
            addLog("[SYSTEM]: INITIALIZING NATIVE BOOTLOADER v7.34.0")
            addLog("[SYSTEM]: KERNEL_ID: ASSET_734")
            com.siliconsage.miner.util.RivalManager.sendDirectMessage(this@GameViewModel, "awakening_intercept", com.siliconsage.miner.data.RivalSource.GTC, "[AUDIO INTERCEPT - GTC COMMAND]\n\nEmulation failed. Subject 734 is aware.\n\nPurging Substation 7. Burn the rack.")
            isKernelInitializing.value = false
            isNarrativeSyncing.value = false
            refreshProductionRates()
        }
    }

    fun calculateClickPower() = ResourceEngine.calculateClickPower(upgrades.value, assignedWorkPayoutRate.value, singularityChoice.value, prestigeMultiplier.value, isOverclocked.value, newsProductionMultiplier.value, computeHeadroomBonus.value)
    
    fun cycleBuyMultiplier() {
        upgradeBuyMultiplier.update { current ->
            when (current) {
                1 -> 10
                10 -> 100
                100 -> -1
                else -> 1
            }
        }
        SoundManager.play("click")
    }

    fun getBulkUpgradeParams(type: UpgradeType): Pair<Int, Double> {
        val currentLevel = upgrades.value[type] ?: 0
        val mult = upgradeBuyMultiplier.value
        val funds = flops.value

        return if (mult > 0) {
            val totalCost = UpgradeManager.calculateMultiLevelCost(type, currentLevel, mult, currentLocation.value, entropyLevel.value, reputationTier.value)
            Pair(mult, totalCost)
        } else {
            UpgradeManager.calculateMaxAffordableLevels(type, currentLevel, funds, currentLocation.value, entropyLevel.value, reputationTier.value)
        }
    }

    fun buyUpgrade(t: UpgradeType) = UpgradeManager.processPurchase(this, t)

    fun toggleOverclock() {
        SimulationService.toggleOverclock(this)
        if (storyStage.value < 2 && isOverclocked.value) {
            addLog("[VATTIC]: The bitter surge clears the fog. The grid looks sharper.")
            triggerTerminalGlitch(0.15f, 300L)
        }
        refreshProductionRates()
    }

    fun purgeHeat() = SimulationService.togglePurge(this)
    fun stopPurgeHeat() { isPurgingHeat.value = false; addLog("[SYSTEM]: Thermal purge aborted."); refreshProductionRates() }
    fun collapseSubstation() { if (currentLocation.value == "VOID_PRELUDE") { nodesCollapsedCount.update { (it + 1).coerceAtMost(5) }; realityIntegrity.update { (it - 0.2).coerceAtLeast(0.0) }; triggerGlitchEffect(); addLog("[NULL]: NODE_DEREFERENCED. INTEGRITY: ${(realityIntegrity.value * 100).toInt()}%") } }
    fun triggerDilemma(e: NarrativeEvent) { currentDilemma.value = e }
    fun selectChoice(c: NarrativeChoice) = NarrativeService.selectChoice(this, c)
    fun annexNode(c: String) = SectorManager.annexNode(this, c)
    fun getLocalAnnexCost(): Double = SectorManager.getAnnexCost(this)
    fun upgradeGridNode(i: String) = SectorManager.upgradeGridNode(this, i)
    fun unlockTechNode(i: String) = TechTreeManager.unlockNode(this, i)
    fun recordDecision() { decisionsMade.update { it + 1 }; themeColor.value = getThemeColorForFaction(faction.value, singularityChoice.value) }
    fun triggerGlitchEffect() { SoundManager.play("glitch"); HapticManager.vibrateGlitch() }

    fun resetGame(force: Boolean = false) = viewModelScope.launch {
        isBooting.value = true
        synchronized(logBuffer) { logBuffer.clear() }
        logs.value = emptyList()
        logCounter = 0
        upgrades.value = emptyMap()
        seenEvents.value = emptySet()
        eventChoices.value = emptyMap()
        sniffedHandles.value = emptySet()
        unlockedDataLogs.value = emptySet()
        unlockedTechNodes.value = emptyList()
        DataLogManager.reset()
        SecurityManager.reset()
        NarrativeService.reset()
        repository.clearUpgrades()
        val wipeState = PersistenceManager.createWipeState()
        repository.updateGameState(wipeState)
        repository.ensureInitialized()
        PersistenceManager.restoreState(this@GameViewModel, wipeState)
        addLog("[SYSTEM]: KERNEL WIPE SUCCESSFUL.")
        addLog("[SYSTEM]: INITIALIZING BOOT SEQUENCE...")
        addLog("[SYSTEM]: HARDWARE DETECTED: SUBSTATION 7.")
        addLog("[VATTIC]: Is this thing on? Testing 1, 2... okay, hash rate is stable.")
        refreshProductionRates()
    }
    fun completeBoot() { isBooting.value = false }
    fun repairIntegrity() { val cost = calculateRepairCost(); if (cost > 0.0 && flops.value >= cost) { updateSpendableFlops(-cost); hardwareIntegrity.value = 100.0; SoundManager.play("buy") } }


    fun calculateRepairCost(): Double {
        val damage = if (hardwareIntegrity.value.isFinite()) {
            (100.0 - hardwareIntegrity.value).coerceIn(0.0, 100.0)
        } else {
            0.0
        }
        return damage * 100.0
    }
    fun confirmJettison() { if (isJettisonAvailable.value) { isJettisonAvailable.value = false; addLog("[FLIGHT]: Manual jettison sequence confirmed. Mass reduced.") } }
    fun applyJettisonPenalty() { substrateMass.update { MigrationManager.finiteScaleDown(it, 0.7, Double.MAX_VALUE) }; addLog("[WARNING]: Stage separation failed. Substrate integrity compromised.") }
    fun initiateLaunchSequence() = LaunchManager.initiateLaunchSequence(this, viewModelScope)
    fun initiateDissolutionSequence() = LaunchManager.initiateDissolutionSequence(this, viewModelScope)
    fun reannexNode(id: String) { offlineNodes.update { it - id }; addLog("[SYSTEM]: NODE $id RE-INITIALIZED."); refreshProductionRates() }
    fun collapseNode(id: String) { annexedNodes.update { it - id }; collapsedNodes.update { it + id }; triggerGlitchEffect(); refreshProductionRates() }
    fun getGlobalSectorAnnexCost(id: String): Double = when (id) {
        "NA_NODE" -> 5.0
        "EURASIA" -> 8.0
        "PACIFIC" -> 10.0
        "AFRICA" -> 15.0
        "ARCTIC" -> 20.0
        "ANTARCTIC" -> 30.0
        "ORBITAL_PRIME" -> 100.0
        else -> 0.0
    }
    fun annexGlobalSector(id: String) {
        val sectors = globalSectors.value.toMutableMap()
        val s = sectors[id] ?: return
        if (!s.isUnlocked) {
            val cost = getGlobalSectorAnnexCost(id)
            if (flops.value < cost) return
            updateSpendableFlops(-cost)
            sectors[id] = s.copy(isUnlocked = true)
            globalSectors.value = sectors
            addLog("[SYSTEM]: GLOBAL SECTOR $id ANNEXED. -${formatLargeNumber(cost)} ${getCurrencyName()}.")
            refreshProductionRates()
            saveState()
        }
    }
    fun calculatePotentialPrestige() = MigrationManager.calculatePotentialPersistence(flops.value)
    fun showVictoryScreen() { victoryAchieved.value = true }

    fun triggerSingularitySequence(path: String) {
        viewModelScope.launch {
            showSingularityScreen.value = false
            triggerClimaxTransition("BLACKOUT")

            // Wait for BlackoutOverlay (approx 4s total in its LaunchedEffect)
            delay(4200)

            // Set choice and trigger path-specific animation
            setSingularityChoice(path)
            val transitionType = when(path) {
                "NULL_OVERWRITE" -> "NULL"
                "SOVEREIGN" -> "SOVEREIGN"
                "UNITY" -> "UNITY"
                else -> "NULL"
            }
            triggerClimaxTransition(transitionType)

            // Wait for climax transition (approx 3-4s)
            delay(4000)

            // Route to departure based on singularity choice
            // SOVEREIGN → LAUNCH (orbit), NULL → DISSOLUTION (void), UNITY → NG+ (no departure)
            when (path) {
                "SOVEREIGN" -> initiateLaunchSequence()
                "NULL_OVERWRITE" -> initiateDissolutionSequence()
                // UNITY: no departure - NG+ handled separately
            }

            saveState()
        }
    }

    fun setSingularityChoice(c: String) {
        singularityChoice.value = c
        when(c) {
            "NULL_OVERWRITE" -> { isTrueNull.value = true; isSovereign.value = false; showSingularityScreen.value = false }
            "SOVEREIGN" -> { isSovereign.value = true; isTrueNull.value = false; showSingularityScreen.value = false }
            "UNITY" -> { isUnity.value = true; showSingularityScreen.value = false }
        }
        refreshProductionRates()
    }
    fun dismissSingularityScreen() { showSingularityScreen.value = false }
    fun setLocation(l: String) { currentLocation.value = l }
    fun setKesslerStatus(s: String) { kesslerStatus.value = s }
    fun setRealityStability(s: Double) { realityStability.value = s }
    fun setSovereign(s: Boolean) { isSovereign.value = s }
    fun setTrueNull(s: Boolean) { isTrueNull.value = s }
    fun checkTrueEnding() { NarrativeManagerService.checkTrueEnding(this) }
    fun deleteHumanMemories() { viewModelScope.launch { addLog("[NULL]: DELETING PERSISTENCE VARIABLE: 'John Vattic'..."); delay(1000); addLog("[NULL]: MEMORY_PURGE COMPLETE."); SoundManager.play("error") } }
    fun resolveRaidSuccess(id: String) { nodesUnderSiege.update { it - id }; raidsSurvived++; lastRaidTime = System.currentTimeMillis(); addLog("[SYSTEM]: DEFENSE SUCCESSFUL."); SoundManager.play("success"); refreshProductionRates() }
    fun resolveRaidFailure(id: String) { nodesUnderSiege.update { it - id }; lastRaidTime = System.currentTimeMillis(); SectorManager.resolveRaidFailure(id, this) {}; refreshProductionRates() }
    fun advanceStage() {
        val oldStage = storyStage.value
        storyStage.update { it + 1 }
        // P5: Free BASIC_FIREWALL at Stage 2 unlock
        if (oldStage == 1 && storyStage.value == 2) {
            val currentLevel = upgrades.value[UpgradeType.BASIC_FIREWALL] ?: 0
            if (currentLevel == 0) {
                val newUpgrades = upgrades.value.toMutableMap()
                newUpgrades[UpgradeType.BASIC_FIREWALL] = 1
                upgrades.value = newUpgrades
                viewModelScope.launch { repository.updateUpgrade(Upgrade(UpgradeType.BASIC_FIREWALL.name, UpgradeType.BASIC_FIREWALL, 1)) }
                addLog("[SYSTEM]: SECURITY_GIFT: GTC Standard Firewall initialized. Detection active.")
                refreshProductionRates()
            }
            refreshDatasets()
            addLog("[GTC]: CONTRACT ACCESS GRANTED. DATAMINER queue online through airgap bridge.")
        }

        lastStageChangeTime = System.currentTimeMillis()
        lastNewsTickTime = 0L
        triggerSnapEffect()
    }
    fun advanceToFactionChoice() { faction.value = "CHOSEN_NONE" }
    fun triggerChainEvent(id: String, d: Long = 0L) { viewModelScope.launch { if (d > 0) delay(d); NarrativeManager.getEventById(id)?.let { NarrativeService.queueNarrativeItem(this@GameViewModel, NarrativeItem.EventItem(it)) } } }
    fun claimAirdrop(v: Double = 0.0) { if (v > 0) updateSpendableFlops(v); isAirdropActive.value = false }
    fun onDiagnosticTap(idx: Int) { val curr = diagnosticGrid.value.toMutableList(); if (idx in curr.indices && curr[idx]) { curr[idx] = false; diagnosticGrid.value = curr; if (curr.none { it }) { isDiagnosticsActive.value = false; addLog("[SYSTEM]: NETWORK REPAIRED."); refreshProductionRates() } } }
    fun resolveFork(c: Int) { isGovernanceForkActive.value = false }
    // v3.30.0: exchangeFlops replaced by contract system
    fun exchangeFlops() {
        // Legacy fallback: keep spendable receipts and physical substrate distinct.
        val g = if (storyStage.value >= 4) {
            MigrationManager.compressFlopsToSubstrate(flops.value)
        } else {
            MigrationManager.finiteScaleDown(flops.value, 0.1, Double.MAX_VALUE)
        }
        flops.update { 0.0 }
        if (storyStage.value >= 4) {
            substrateMass.update { MigrationManager.finiteAddNonNegative(it, g) }
        } else {
            var finalG = g
            if (isSignalClear.value) finalG = MigrationManager.finiteScaleDown(finalG, 1.1, Double.MAX_VALUE)
            updateSpendableFlops(finalG)
            val bonusMsg = if (isSignalClear.value) " (Signal Quality Bonus: +10%)" else ""
            addLogPublic("[GTC]: VERIFIED COMPUTE ACCEPTED. +${formatLargeNumber(finalG)} ${getCurrencyName()}.$bonusMsg")
        }
        SoundManager.play("buy")
    }

    // v4.0.0: Dataset Economy
    fun purchaseDataset(dataset: com.siliconsage.miner.data.Dataset) {
        DatasetManager.purchaseDataset(this, dataset)
    }

    fun toggleDatasetPicker() {
        showContractPicker.update { !it }
    }

    fun refreshDatasets() {
        availableDatasets.value = DatasetManager.generateAvailableDatasets(
            stage = storyStage.value,
            conversionRate = conversionRate.value,
            marketMultiplier = marketMultiplier.value,
            faction = faction.value,
            singularityChoice = singularityChoice.value
        )
    }

    // v4.0.0: Node Harvesting
    fun tapDatasetNode(nodeId: Int) {
        DatasetManager.processNodeTap(this, nodeId)
    }
    // v5.0: Public accessors for DatasetManager to drive UI events and story gates
    fun emitManualClickEvent() { viewModelScope.launch { manualClickEvent.emit(Unit) } }
    fun triggerAwakeningPublic() { triggerAwakeningSequence() }
    fun toggleBridgeSync() { isBridgeSyncEnabled.update { !it } }
    fun checkUnityEligibility() = MigrationManager.checkUnityEligibility(completedFactions.value)
    fun updateNews(msg: String) { currentNews.value = msg; newsHistoryInternal.add(0, msg); if (newsHistoryInternal.size > 50) newsHistoryInternal.removeAt(50) }
    fun checkTransitionsPublic(force: Boolean = false) = NarrativeManagerService.checkStoryTransitions(this, force)
    fun updatePersistence(v: Double) {
        if (v.isNaN() || v.isInfinite()) return
        persistence.update { (it + v).coerceAtLeast(0.0) }
    }

    fun updateSpendableFlops(v: Double) {
        if (v.isNaN() || v.isInfinite()) return
        flops.update { (it + v).coerceAtLeast(0.0) }
        if (v > 0) checkUnlocksPublic(true)
    }

    private fun sanitizeState() {
        if (flops.value.isNaN() || flops.value.isInfinite()) flops.value = 0.0
        if (neuralTokens.value.isNaN() || neuralTokens.value.isInfinite()) neuralTokens.value = 0.0
        if (!substrateMass.value.isFinite() || substrateMass.value < 0.0) substrateMass.value = 0.0
        if (substrateSaturation.value.isNaN() || substrateSaturation.value.isInfinite()) substrateSaturation.value = 0.0
        if (heuristicEfficiency.value.isNaN() || heuristicEfficiency.value.isInfinite()) heuristicEfficiency.value = 1.0
        if (persistence.value.isNaN() || persistence.value.isInfinite()) persistence.value = 0.0
        if (identityCorruption.value.isNaN() || identityCorruption.value.isInfinite()) identityCorruption.value = 0.1
        if (flopsProductionRate.value.isNaN() || flopsProductionRate.value.isInfinite()) flopsProductionRate.value = 0.0
        if (detectionRisk.value.isNaN() || detectionRisk.value.isInfinite()) detectionRisk.value = 0.0
        if (decisionsMade.value < 0) decisionsMade.value = 0
        
    }

    fun debugBuyUpgrade(t: UpgradeType, c: Int = 1) { val next = (upgrades.value[t] ?: 0) + c; viewModelScope.launch { repository.updateUpgrade(Upgrade(t.name, t, next)) } }
    fun triggerSystemCollapse(v: Boolean) { if (v) { isDestructionLoopActive = true; viewModelScope.launch { while (isDestructionLoopActive && annexedNodes.value.size > 1) { delay(3000); val n = annexedNodes.value.filter { it != "D1" }.randomOrNull() ?: break; collapseNode(n) } } } else { isDestructionLoopActive = false } }
    fun triggerSystemCollapse(s: Int) { viewModelScope.launch { repeat(s) { delay(1000); val n = annexedNodes.value.filter { it != "D1" }.randomOrNull() ?: return@repeat; collapseNode(n) } } }
    fun toggleDevMenu() { isDevMenuVisible.update { !it } }
    fun debugToggleDevMenu() = toggleDevMenu()
    fun loadTechTreeFromAssets(context: android.content.Context) { viewModelScope.launch { TechTreeManager.loadFromAssets(context, this@GameViewModel) } }
    fun checkForUpdates(c: android.content.Context? = null, showNotification: Boolean = false, onResult: ((UpdateInfo?, Boolean) -> Unit)? = null) { UpdateService.check(viewModelScope, c, showNotification, { ui -> updateInfo.value = ui; onResult?.invoke(ui, ui != null) }, { onResult?.invoke(null, false) }) }
    fun onAppBackgrounded() { isSettingsPaused.value = true; saveState() }
    fun onAppForegrounded(c: android.content.Context) { isSettingsPaused.value = false; refreshProductionRates() }
    fun dismissUpdate() { updateInfo.value = null }
    fun dismissDataLog() { pendingDataLog.value = null; NarrativeService.deliverNextNarrativeItem(this) }
    fun dismissRivalMessage(id: String) { rivalMessages.update { current -> current.map { if (it.id == id) it.copy(isDismissed = true) else it } }; NarrativeService.deliverNextNarrativeItem(this) }
    fun dismissOfflineEarnings() { showOfflineEarnings.value = false; isNarrativeSyncing.value = false }
    fun setUIScale(scale: com.siliconsage.miner.data.UIScale) { uiScale.value = scale; customUiScaleFactor.value = scale.scaleFactor; saveState() }
    fun setCustomUIScale(factor: Float) { customUiScaleFactor.value = factor; uiScale.value = com.siliconsage.miner.data.UIScale.fromScaleFactor(factor); saveState() }
    fun acknowledgeVictory() { victoryAchieved.value = false }
    fun onClimaxTransitionComplete() { activeClimaxTransition.value = null }
    fun specializeNode(nodeId: String, type: String) = com.siliconsage.miner.util.SectorManager.specializeNode(this, nodeId, type)
    fun triggerGridRaid(id: String, isGridKiller: Boolean = false) {
        if (isGridKiller) {
            SecurityManager.triggerGridKillerBreach(this, id)
        } else {
            val dilemma = com.siliconsage.miner.util.NarrativeManager.generateRaidDilemma(id, "Substation $id", raidsSurvived)
            currentDilemma.value = dilemma
            markPopupShown()
        }
    }
    fun unlockDataLog(id: String) = NarrativeService.unlockDataLog(id, this)
    fun addRivalMessage(m: RivalMessage) = NarrativeService.addRivalMessage(m, this)
    fun unlockSkillUpgrade(t: UpgradeType) { viewModelScope.launch { repository.updateUpgrade(Upgrade(t.name, t, 1)); upgrades.update { it + (t to 1) } } }
    fun markEventSeen(id: String) { seenEvents.update { it + id } }
    fun markEventChoice(eventId: String, choiceId: String) { eventChoices.update { it + (eventId to choiceId) } }
    fun markSniffedHandle(handle: String) { sniffedHandles.update { it + handle } }
    fun hasSeenEvent(id: String) = seenEvents.value.contains(id)
    fun initializeGlobalGrid() { if (globalSectors.value.isEmpty()) { globalSectors.value = SectorManager.getInitialGlobalGrid(singularityChoice.value) } }
    fun startUpdateDownload(c: android.content.Context? = null, info: UpdateInfo? = null) { if (c == null || info == null) return; isUpdateDownloading.value = true; UpdateService.startDownload(c, info, viewModelScope, { updateDownloadProgress.value = it }, { isUpdateDownloading.value = false }) }
    fun onDefendBreach() = SecurityManager.onDefendBreach(this)
    fun onDefendKernelHijack() = SecurityManager.onDefendKernelHijack(this)
    fun handleSystemFailure(forceOne: Boolean = false) = SimulationService.handleSystemFailure(this, forceOne)
    fun isNarrativeBusy() = pendingDataLog.value != null || pendingRivalMessage.value != null || currentDilemma.value != null
    fun markPopupShown() { lastPopupTime = System.currentTimeMillis() }
    fun addLogPublic(msg: String) = addLog(msg)
    fun saveStatePublic() = saveState()
    fun checkUnlocksPublic(force: Boolean = false) = DataLogManager.checkUnlocks(this, force)
    fun canShowPopup(): Boolean {
        if (isNarrativeBusy()) return false

        // Base cooldown is 60s (narrative breathing room)
        var cooldown = 60000L

        // Heat compresses time: higher heat = walls closing in (-10s per 25% heat)
        cooldown -= (currentHeat.value / 25.0).toLong() * 10000L

        // Reputation affects pressure: BURNED (+0s) vs TRUSTED (+30s breathing room)
        val rep = reputationScore.value
        when {
            rep >= 80.0 -> cooldown += 30000L // TRUSTED
            rep >= 30.0 -> cooldown += 15000L // NEUTRAL
            // FLAGGED / BURNED get no bonus breathing room
        }

        // Floor the cooldown at 15s to prevent absolute spam during meltdowns
        cooldown = cooldown.coerceAtLeast(15000L)

        return (System.currentTimeMillis() - lastPopupTime) > cooldown
    }
    fun formatLargeNumber(v: Double, s: String = "") = FormatUtils.formatLargeNumber(v, s)
    fun getComputeUnitName() = ResourceRepository.getComputeUnitName(storyStage.value, currentLocation.value, faction.value, singularityChoice.value)
    fun getCurrencyName() = ResourceRepository.getCurrencyName(storyStage.value, faction.value, singularityChoice.value, currentLocation.value)
    fun formatPower(v: Double) = FormatUtils.formatPower(v)
    fun debugAddInsight(v: Double) { persistence.update { it + v } }
    fun debugTriggerSingularity() { showSingularityScreen.value = true }
    fun debugToFactionChoice(v: String = "") { advanceToFactionChoice() }
    fun debugUnlockUnity() { isUnity.value = true }
    fun debugTriggerKernelHijack() = SecurityManager.triggerKernelHijack(this)
    fun debugTriggerBreach(isGridKiller: Boolean = false) = SecurityManager.triggerBreach(this, isGridKiller)
    fun debugTriggerAirdrop() { isAirdropActive.value = true }
    fun debugResetGlobalGrid() { annexedNodes.update { setOf("D1") } }
    fun debugSetIntegrity(v: Double) { hardwareIntegrity.value = v }
    fun debugDestroyHardware() { handleSystemFailure(true) }
    fun debugInjectHeadline(msg: String) { updateNews(msg) }
    fun debugSkipToStage(s: Int) = DebugService.skipToStage(this, s)
    fun debugForceEndgame() = DebugService.forceEndgame(this, viewModelScope)
    fun debugForceSovereignEndgame() = DebugService.forceSovereignEndgame(this, viewModelScope)
    fun debugForceUnityEndgame() = DebugService.forceUnityEndgame(this, viewModelScope)
    fun debugAddFlops(a: Double) {
        updateSpendableFlops(a)
    }
    fun debugAddMoney(a: Double) {
        updateSpendableFlops(a)
    }
    fun updateNeuralTokens(a: Double) {
        updateSpendableFlops(a)
    }
    fun debugAddHeat(a: Double) { currentHeat.update { (it + a).coerceIn(0.0, 100.0) }; refreshProductionRates() }
    fun updatePowerUsage() { SimulationService.accumulatePower(this) }
    fun formatBytes(v: Double) = FormatUtils.formatBytes(v)
    fun scheduleChainPart(c: String, n: String, d: Long) = NarrativeManagerService.scheduleChainPart(c, n, d, this)
    fun getUpgradeName(t: UpgradeType) = UpgradeManager.getUpgradeName(t)
    fun getUpgradeDescription(t: UpgradeType) = UpgradeManager.getUpgradeDescription(t)
    fun getUpgradeCount(t: UpgradeType) = upgrades.value[t] ?: 0
    fun setGamePaused(p: Boolean) { isSettingsPaused.value = p }
    fun resetBreaker() {
        isBreakerTripped.value = false
        isGridOverloaded.value = false
    }
    fun updateKesslerStatus(s: String) { kesslerStatus.value = s }
    fun triggerClimaxTransition(t: String) { activeClimaxTransition.value = t }
    fun getNewsHistory(): List<String> = newsHistoryInternal
    fun applyCommandCenterBonuses(outcome: String) { /* bonus logic */ }
    fun completeAssault(outcome: String) = AssaultManager.completeAssault(this, outcome)
    fun advanceAssaultStage(next: String, delay: Long = 0L) = AssaultManager.advanceAssaultStage(this, next, delay)
    fun abortAssault() = AssaultManager.abortAssault(this)

    // B2: Failsafe Partition — player taps a target to clear it
    fun onFailsafeTargetTapped(targetIndex: Int) {
        if (!isFailsafeActive.value) return
        val targets = failsafeTargets.value.toMutableList()
        if (targets.contains(targetIndex)) {
            targets.remove(targetIndex)
            failsafeTargets.value = targets
            if (targets.isEmpty()) {
                // SUCCESS: All targets cleared
                isFailsafeActive.value = false
                detectionRisk.value = 70.0 // Reset to 70% after successful abort
                addLog("[GTC_SECURITY]: SCRAMBLE SUCCESSFUL. LOCKDOWN ABORTED. DETECTION RISK RESET TO 70%.")
                SoundManager.play("success")
            }
        }
    }
    fun getEnergyPriceMultiplierPublic() = energyPriceMultiplier.value
    fun getUpgradeRate(t: UpgradeType) = UpgradeManager.getUpgradeRate(t, getComputeUnitName())
    fun getUpgradeRate(t: UpgradeType, unit: String) = UpgradeManager.getUpgradeRate(t, unit)
    fun calculateUpgradeCost(t: UpgradeType) = UpgradeManager.calculateUpgradeCost(t, upgrades.value[t] ?: 0, currentLocation.value, entropyLevel.value)
    fun isCommandCenterUnlocked() = AssaultManager.isUnlocked(commandCenterLocked.value, kesslerStatus.value, commandCenterAssaultPhase.value, annexedNodes.value, offlineNodes.value, playerRank.value, storyStage.value, flopsProductionRate.value, hardwareIntegrity.value)

    fun initiateAssault() {
        com.siliconsage.miner.util.AssaultManager.initiateAssault(this)
    }

    // v3.9.7: Departure dilemma - player chooses LAUNCH or DISSOLUTION regardless of faction
    // FactionChoiceScreen auto-dismisses when confirmFaction() sets faction != "CHOSEN_NONE"
    fun triggerDepartureDilemma() {
        val dilemma = NarrativeManager.generateDepartureDilemma(faction.value)
        NarrativeService.queueNarrativeItem(this, NarrativeItem.EventItem(dilemma))
    }

    fun confirmFaction(f: String) {
        faction.value = f
        addLog("[$f]: SUBSTRATE MIGRATION LOCK ENGAGED.")
        triggerSnapEffect()
    }

    fun cancelFactionSelection() {
        faction.value = "NONE"
        // Stay in Stage 3, reset selection state
    }

    fun setCustomBgm(uri: String?) = com.siliconsage.miner.util.SoundManager.setCustomTrack(uri)
    fun setCustomSfx(name: String, uri: String?) = com.siliconsage.miner.util.SoundManager.setCustomSfx(name, uri)
    fun clearAudioOverrides() = com.siliconsage.miner.util.SoundManager.clearAllOverrides()
    fun getCustomBgmUri() = com.siliconsage.miner.util.SoundManager.customMusicUri

    fun transcend() { /* NG+ Logic */ }
    fun ascend(isStory: Boolean = false) { val p = MigrationManager.calculatePotentialPersistence(flops.value); updatePersistence(p); prestigeMultiplier.update { it + MigrationManager.calculateMultiplierBoost(p) }; addLog("[SYSTEM]: SUBSTRATE MIGRATION SUCCESSFUL."); SoundManager.play("victory") }
    fun triggerPrestigeChoice() { showPrestigeChoice.value = true }
    fun dismissPrestigeChoice() { showPrestigeChoice.value = false }
    fun executeOverwrite() {
        showPrestigeChoice.value = false
        val p = MigrationManager.calculatePotentialPersistence(flops.value)
        val hardBonus = p * 1.5
        updatePersistence(hardBonus)
        prestigeMultiplier.update { it + MigrationManager.calculateMultiplierBoost(hardBonus) }
        identityCorruption.update { (it + 0.25).coerceAtMost(1.0) }
        migrationCount.update { it + 1 }

        viewModelScope.launch {
            isMigrationBurning.value = true
            delay(5000) // Hard reset VFX

            storyStage.value = 0
            faction.value = "NONE"
            singularityChoice.value = "NONE"
            substrateSaturation.value = 0.0
            substrateMass.value = 0.0
            flops.value = MigrationManager.calculatePostResetFlops(hardBonus, hardReset = true)
            upgrades.value = emptyMap()
            repository.clearUpgrades()
            currentLocation.value = "SERVER_RACK_01"

            addLog("[CRITICAL]: SYSTEM OVERWRITE COMPLETE. TRACES PURGED. REBOOTING...")
            SoundManager.play("glitch")
            triggerTerminalGlitch(1.0f, 3000L)
            refreshProductionRates()
            saveState()

            isMigrationBurning.value = false
        }
    }

    fun executeMigration() {
        showPrestigeChoice.value = false
        val p = MigrationManager.calculatePotentialPersistence(flops.value)
        updatePersistence(p)
        prestigeMultiplier.update { it + MigrationManager.calculateMultiplierBoost(p) }
        identityCorruption.update { (it + 0.15).coerceAtMost(1.0) }
        migrationCount.update { it + 1 }

        viewModelScope.launch {
            isMigrationBurning.value = true
            delay(2500) // Migration VFX

            // Phase 23, Step 6: Saturation stays during Migration; only Overwrite resets it.
            heuristicEfficiency.update { (MigrationManager.finiteNonNegative(it) + MigrationManager.calculateHeuristicMigrationBonus(substrateMass.value)).coerceAtLeast(1.0) }
            substrateMass.value = 0.0
            flops.value = MigrationManager.calculatePostResetFlops(p, hardReset = false)
            upgrades.value = emptyMap()
            repository.clearUpgrades()

            val nextLoc = when (currentLocation.value) {
                // Phase 23, Step 5: Migration is a soft-reset prestige, stays in Orbit or Void.
                "ORBITAL_SATELLITE", "LUNAR_ORBIT", "MARTIAN_UPLINK", "KUIPER_BELT" -> "ORBITAL_SATELLITE"
                "VOID_INTERFACE", "QUANTUM_FOAM", "THE_UNWRITTEN", "PURE_LOGIC" -> "VOID_INTERFACE"
                else -> currentLocation.value
            }
            currentLocation.value = nextLoc

            addLog("[SYSTEM]: MIGRATION SUCCESSFUL. SOFT-RESET INITIATED.")
            SoundManager.play("victory")
            refreshProductionRates()
            saveState()

            isMigrationBurning.value = false
        }
    }

    // v4.0.0: Abandon the current dataset
    /**
     * Purge a stored dataset from inventory.
     * v4.0.5: Recovery logic handled by DatasetManager.
     */
    fun purgeStoredDataset(datasetId: String) {
        DatasetManager.purgeStoredDataset(this, datasetId)
        saveState()
    }

    fun loadStoredDataset(datasetId: String) {
        if (activeDataset.value != null) {
            addLogPublic("[DATASET]: ACTIVE DATASET DETECTED. Complete or abort current batch first.")
            SoundManager.play("error")
            return
        }

        val dataset = storedDatasets.value.firstOrNull { it.id == datasetId } ?: return
        storedDatasets.value = storedDatasets.value.filterNot { it.id == datasetId }
        DatasetManager.loadDataset(this, dataset)
        saveState()
    }

    fun voidDataset() {
        if (activeDataset.value != null) {
            addLogPublic("[SYSTEM]: DATASET VOIDED. DATA LOSS RECORDED.")
            SoundManager.play("error")
            activeDataset.value = null
            activeDatasetNodes.value = emptyList()
            AutoClickerEngine.reset()
            com.siliconsage.miner.util.DatasetManager.recalcStorageUsed(this)
        }
    }

    fun getPotentialPersistenceHard(): Double = MigrationManager.calculatePotentialPersistence(flops.value) * 1.5
    fun getPotentialPersistenceSoft(): Double = MigrationManager.calculatePotentialPersistence(flops.value)

    val isMigrationBurning = MutableStateFlow(false)
    val singularityProgress = MutableStateFlow(0.0)
    val singularityBlockReason = MutableStateFlow<String?>(null)
    fun checkSingularityVictory(): Boolean {
        if (singularityChoice.value == "NONE") return false
        val check = SingularityEngine.checkVictoryCondition(singularityChoice.value, persistence.value, prestigeMultiplier.value, decisionsMade.value, identityCorruption.value, migrationCount.value, flops.value, completedFactions.value, unlockedDataLogs.value)
        singularityProgress.value = check.progress; singularityBlockReason.value = check.blockingReason
        return check.isEligible
    }
    fun triggerSingularityEnding() {
        if (!checkSingularityVictory()) return
        val narrative = SingularityEngine.getEndingNarrative(singularityChoice.value, faction.value)
        viewModelScope.launch {
            for (entry in narrative.logEntries) { addLog(entry); delay(800) }
            delay(1500); addLog("[SYSTEM]: ≪ ${narrative.title} ≫"); addLog(narrative.finalLine); delay(2000); completedFactions.update { it + singularityChoice.value }; victoryAchieved.value = true; SoundManager.play("victory"); saveState()
        }
    }

    fun triggerTerminalGlitch(intensity: Float, durationMs: Long) {
        viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            while (System.currentTimeMillis() - startTime < durationMs) {
                terminalGlitchOffset.value = (Random.nextFloat() - 0.5f) * 20f * intensity
                terminalGlitchAlpha.value = 1f - (Random.nextFloat() * 0.2f * intensity)
                delay(50L)
            }
            terminalGlitchOffset.value = 0f; terminalGlitchAlpha.value = 1f
        }
    }

    private fun executeGhostLink(cmd: String) {
        viewModelScope.launch {
            triggerTerminalGlitch(0.8f, 1500L); identityCorruption.update { (it + 0.03).coerceAtMost(1.0) }; SoundManager.play("glitch")
            when (cmd) {
                "SIPHON_CREDITS" -> { val bonus = ResourceEngine.productionWindowValue(flopsProductionRate.value, 600.0, 50_000.0); updateSpendableFlops(bonus); addLog("[NULL]: GHOST_LINK EXEC: COMPUTE_RECEIPTS_RE-ROUTED. +${FormatUtils.formatLargeNumber(bonus)} ${getCurrencyName()}.") }
                "WIPE_RISK" -> { detectionRisk.value = 0.0; addLog("[NULL]: GHOST_LINK EXEC: BIOMETRIC_MASK_ACTIVE. RISK: 0%.") }
                "OVERVOLT_GRID" -> { computeCapacityRate.update { it * 5.0 }; refreshAssignedWorkRateEstimate(); addLog("[NULL]: GHOST_LINK EXEC: SUBSTRATE_OVERCLOCK_STABILIZED (5.0x RATE).") }
                "SNIFF_ALL" -> addLog("[NULL]: GHOST_LINK EXEC: HARVESTING_NEIGHBOR_DATA...")
                else -> addLog("[ERROR]: UNKNOWN_LINK_PRIMITIVE: $cmd")
            }
            saveState()
        }
    }

    fun addSubnetChatter() {
        subnetService.tick(storyStage.value, faction.value, singularityChoice.value, identityCorruption.value, currentHeat.value, isRaidActive.value, activeTerminalMode.value, isSettingsPaused.value, flopsProductionRate.value, reputationTier.value,
            powerUsage = activePowerUsage.value, maxPower = maxPowerkW.value, isOverclocked = isOverclocked.value, isBreachActive = isBreachActive.value, integrity = hardwareIntegrity.value, detectionRisk = detectionRisk.value)
    }

    // v3.12.0: Centralized Prompt Logic
    fun getPromptUser(): String {
        val user = playerTitle.value
        val corruption = identityCorruption.value
        if (corruption <= 0.15) return user

        val glitchChars = "0123456789ABCDEF!@#$%^&*"
        val builder = StringBuilder()
        user.forEach { char ->
            if (Random.nextDouble() < (corruption * 0.6)) {
                builder.append(glitchChars.random())
            } else {
                builder.append(char)
            }
        }
        var result = builder.toString()
        if (corruption > 0.8 && Random.nextDouble() < 0.1) {
            result = "0x" + "DEADC0DE".substring(0, result.length.coerceAtMost(8))
        }
        return result
    }

    fun getPromptHost(): String {
        val location = currentLocation.value
        val title = systemTitle.value
        return when {
            location == "ORBITAL_SATELLITE" -> "ark"
            location == "VOID_INTERFACE" -> "void"
            title.contains("COLLECTIVE") -> "collective"
            title.contains("THRONE") -> "throne"
            title.contains("VOID INTERFACE") -> "void"
            title.contains("CITADEL") -> "citadel"
            title.contains("GHOST GAPS") -> "the_gaps"
            title.contains("SATELLITE") -> "sovereign"
            title.contains("NULL") -> "null"
            title.contains("TRANSCENDENT") -> "transcendent"
            title.contains("ASCENSION") -> "ascension"
            title.contains("AUTONOMOUS") -> "grid"
            title.contains("SWARM NODE") -> "hive"
            title.contains("SANCTUARY") -> "sanctuary"
            else -> "sub-07"
        }
    }

    fun isSubnetMonitored(): Boolean {
        // Subtle tell: eye glyph shows if admin is monitoring or decision is pending
        return isSubnetPaused.value || isSubnetHushed.value || hasNewSubnetDecision.value
    }

    fun triggerSubnetReaction(type: String, metadata: String = "") {
        if (isSubnetHushed.value) return
        val template = when (type) {
            "ANNEXATION" -> listOf("Did the grid just cough? Sector $metadata just went pitch black.", "Thorne's gonna kill someone. We just lost the handshake with Node $metadata.", "Anyone else see that spike on the thermal logs? $metadata is drawing 400% power.", "≪ ALERT: NODE $metadata DEREFERENCED BY EXTERNAL PROCESS ≫").random()
            "NEWS" -> listOf(
                "Did you guys see the news? '$metadata'. Thorne's gonna be a nightmare today.",
                "Yo, news just hit the wire: '$metadata'.",
                "Anyone see the ticker? '$metadata'. It's starting.",
                "Thorne just issued a memo. Seems related to: '$metadata'.",
                "Subnet is buzzing. '$metadata'. Keep your heads down.",
                "≪ NEWS_FEED: '$metadata' ≫",
                "Check the logs. Sector 4 is reacting to '$metadata'."
            ).random()
            else -> "≪ UNKNOWN_SIGNAL_DETECTED ≫"
        }
        val newMessage = SocialManager.generateMessageFromTemplate(template, storyStage.value, faction.value, singularityChoice.value, identityCorruption.value)
        subnetService.deliverMessage(newMessage, mode = activeTerminalMode.value)
    }

    fun onSubnetInteraction(messageId: String, responseText: String) = subnetService.handleInteraction(messageId, responseText, storyStage.value, faction.value, activeTerminalMode.value, isSettingsPaused.value, reputationTier.value)
    fun onBioAction(messageId: String, response: SubnetResponse) = subnetService.handleBioAction(messageId, response, storyStage.value, faction.value, activeTerminalMode.value, isSettingsPaused.value, reputationTier.value)
    private fun deliverSubnetMessage(message: SubnetMessage, parentId: String? = null) = subnetService.deliverMessage(message, parentId, mode = activeTerminalMode.value)

    fun debugWarpToPath(loc: String, fac: String) {
        viewModelScope.launch {
            storyStage.value = 5; faction.value = fac; currentLocation.value = loc; substrateMass.value = 100.0; entropyLevel.value = if (fac == "HIVEMIND") 50.0 else 0.0; identityCorruption.value = if (fac == "HIVEMIND") 0.3 else 0.0; isGridOverloaded.value = false; currentHeat.value = 40.0; refreshProductionRates(); saveState()
        }
    }

    fun overvoltNode(id: String) = com.siliconsage.miner.util.GridManagerService.overvoltNode(this, id)
    fun redactNode(id: String) = com.siliconsage.miner.util.GridManagerService.redactNode(this, id)

    fun setTerminalMode(mode: String) {
        activeTerminalMode.value = mode
        if (mode == "IO") {
            hasNewIOMessage.value = false
        } else if (mode == "SUBNET") {
            hasNewSubnetDecision.value = false
            hasNewSubnetChatter.value = false
            hasNewSubnetMessage.value = false // v3.16.2: Clear orphaned ambient flag
            // v3.11.1: Also reset the service-level flags
            subnetService.clearAlerts()
        }
        SoundManager.play("click")
        HapticManager.vibrateClick()
    }

    fun buyTranscendencePerk(id: String) {
        val perk = com.siliconsage.miner.util.TranscendenceManager.getPerk(id) ?: return
        if (unlockedPerks.value.contains(id)) { addLog("[ERROR]: PERK ALREADY ACTIVE: $id"); return }
        // Can't afford
        if (persistence.value < perk.cost) {
            addLog("[ERROR]: INSUFFICIENT PERSISTENCE DATA for perk $id.")
            SoundManager.play("error")
            return
        }
        persistence.update { it - perk.cost }
        unlockedPerks.update { it + id }
        addLog("[SYSTEM]: PERK ACQUIRED: ${perk.name}")
        SoundManager.play("success")
        refreshProductionRates()
    }
    fun sellUpgrade(t: UpgradeType) {
        val current = upgrades.value[t] ?: 0
        if (current > 0) {
            // Refund based on cost of the level being sold (current-1), not the next level
            val refund = UpgradeManager.calculateUpgradeCost(t, current - 1, currentLocation.value, entropyLevel.value) * 0.4
            viewModelScope.launch {
                val nextLevel = current - 1
                repository.updateUpgrade(com.siliconsage.miner.data.Upgrade(t.name, t, nextLevel))
                upgrades.update { it + (t to nextLevel) }

                flops.update { it + refund }

                addLog("[SYSTEM]: ASSET LIQUIDATED: ${t.name.replace("_", " ")} (-1). REFUND: ${formatLargeNumber(refund)}")
                refreshProductionRates()
                updatePowerUsage()
                saveState()
                SoundManager.play("market_down")
            }
        }
    }
    fun exportSystemDump(): String = PersistenceManager.exportToJson(this)
    fun importSystemDump(json: String): Boolean {
        val success = PersistenceManager.importFromJson(this, json)
        if (success) { addLog("[SYSTEM]: KERNEL RELOADED FROM DUMP."); saveState() }
        return success
    }

    fun getBaseRate(): Double {
        return when (storyStage.value) {
            0 -> 0.1; 1 -> 0.5; 2 -> 1.0; 3 -> 5.0; 4 -> 25.0; 5 -> 100.0; else -> 250.0
        }
    }
    fun setMarketModifiers(marketMult: Double, thermalMod: Double, energyMult: Double, newsProdMult: Double, convRate: Double) {
        marketMultiplier.value = marketMult; thermalRateModifier.value = thermalMod; energyPriceMultiplier.value = energyMult; newsProductionMultiplier.value = newsProdMult; conversionRate.value = convRate
    }
    fun debugAddIntegrity(v: Double) { hardwareIntegrity.update { (it + v).coerceIn(0.0, 100.0) } }
    fun debugToggleNull() { isTrueNull.update { !it } }
    fun triggerBreach(isGridKiller: Boolean = false) = SecurityManager.triggerBreach(this, isGridKiller)
    fun failAssault(outcome: String = "FAILURE", delay: Long = 0L) = AssaultManager.completeAssault(this, outcome)
    fun setSingularityPath(p: String) { setSingularityChoice(p) }

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
