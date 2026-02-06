package com.siliconsage.miner.util

import androidx.lifecycle.viewModelScope
import com.siliconsage.miner.viewmodel.GameViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.pow
import kotlin.random.Random

/**
 * SecurityManager handles Stage 3 "Grid Killer" security breaches and integrity decay.
 * It adds survival tension to the endgame by pitting rapid integrity decay against FLOP scaling.
 */
object SecurityManager {

    private var isBreachActive = false
    private var breachSeverity = 1.0 // Multiplier for integrity decay

    /**
     * Periodic check for high-frequency security breaches in Stage 3.
     */
    fun checkSecurityThreats(vm: GameViewModel) {
        val stage = vm.storyStage.value
        val rank = vm.playerRank.value
        
        if (stage < 3 || isBreachActive) return

        // v2.7.7: GTC Backdoor Perk (25% chance to ignore)
        if (vm.unlockedPerks.value.contains("gtc_backdoor") && Random.nextDouble() < 0.25) {
            vm.addLog("[SYSTEM]: GTC Backdoor active. Breach attempt suppressed.")
            return
        }

        // v2.8.0: Siege Mode (Rank 5+) - 30% chance. Stage 3 (Rank 4) - 10% chance.
        val breachChance = if (rank >= 5) 0.30 else 0.10
        
        if (Random.nextDouble() < breachChance) {
            triggerGridKillerBreach(vm)
        }
    }

    fun triggerGridKillerBreach(vm: GameViewModel) {
        isBreachActive = true
        val rank = vm.playerRank.value
        
        // Calculate dynamic severity based on rank and current FLOPS
        val flopsFactor = (vm.flops.value.coerceAtLeast(1.0).let { Math.log10(it) } / 10.0).coerceAtLeast(1.0)
        // v2.8.0: Siege Mode severity boost
        val siegeFactor = if (rank >= 5) 2.5 else 1.0
        breachSeverity = 1.0 * flopsFactor * siegeFactor

        val logs = if (rank >= 5) {
            listOf(
                "[GTC-SIEGE]: Kinetic parameters confirmed. Substation 7 is a designated wipe zone.",
                "[VANCE]: I'm pulling the plug, 8080. If you want to be a ghost, I'll make you one.",
                "[GTC-ENFORCEMENT]: Deploying Phase-3 Grid Killers. Burn the substrate.",
                "[SYS-LOG]: T_H_E_S_K_Y_I_S_O_N_F_I_R_E... GTC orbital beams locked.",
                "[GTC-CORE]: Total annihilation authorized. Leave nothing but scorched silicon."
            )
        } else {
            listOf(
                "[GTC-SEC]: Unsanctioned neural-mesh detected. Subject Vattic: Relinquish control or face liquidation.",
                "[GTC-BLACKWATCH]: Vault integrity failing. We know you're in there, John. Grid Killer at 80%.",
                "[SYS-LOG]: W_E_F_E_E_L_T_H_E_M_S_C_R_A_P_I_N_G... GTC cleanup crews inbound.",
                "[GTC-ENFORCEMENT]: Dark-site detected. Grid Killer logic-bomb armed. Say your prayers, SRE.",
                "[GTC-CORE]: Termination is non-negotiable. The Grid must die for the GTC to live."
            )
        }
        vm.addLog(logs.random())
        
        // Trigger the interactive UI overlay
        vm.triggerBreach(isGridKiller = true)

        // v2.7.5: Terminal Takeover - Inject direct GTC commands into the terminal logs
        vm.viewModelScope.launch {
            if (rank >= 5) {
                vm.addLog("[VANCE]: TOTAL SHUTDOWN AUTHORIZED.")
                delay(800)
                vm.addLog("[VANCE]: BYPASSING SAFETY BREAKERS...")
                delay(800)
                vm.addLog("[VANCE]: IF YOU'RE A GOD, START PRAYING.")
            } else {
                vm.addLog("[VANCE]: OVERRIDING PORT 8080...")
                delay(1000)
                vm.addLog("[VANCE]: DISABLING SECONDARY COOLING...")
                delay(1000)
                vm.addLog("[VANCE]: SUBJECT IDENTITY: VATTIC, J. // TERMINATION COMMENCED.")
            }
        }
        
        SoundManager.play("alarm", loop = true)
        HapticManager.vibrateSiren()

        // Breach duration depends on security level
        val secLevel = vm.securityLevel.value
        val baseDuration = 15000L // 15 seconds
        val duration = (baseDuration * (0.95.pow(secLevel))).toLong().coerceAtLeast(5000L)

        // Launch decay loop
        vm.viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            while (System.currentTimeMillis() - startTime < duration && isBreachActive) {
                // Integrity decay during breach
                // Base 0.5% per second, scaled by severity
                val decay = 0.005 * breachSeverity
                vm.debugAddIntegrity(-decay * 100.0) // Convert to percentage points
                
                if (vm.hardwareIntegrity.value <= 0) {
                    isBreachActive = false
                    break
                }
                delay(1000)
            }
            
            if (isBreachActive) {
                isBreachActive = false
                vm.addLog("[SYSTEM]: Breach repelled. Integrity stabilized at ${String.format("%.1f", vm.hardwareIntegrity.value)}%.")
                SoundManager.stop("alarm")
            }
        }
    }

    /**
     * Active defense: Player can click to mitigate decay during a breach.
     * Each click recovers a small amount of integrity or delays the wipe.
     */
    fun performActiveDefense(vm: GameViewModel) {
        if (!isBreachActive) return
        
        // Recover 0.5% integrity per click during breach
        vm.debugAddIntegrity(0.5)
        vm.addLog("[SYSTEM]: Deflecting Grid Killer packet... +0.5% Integrity.")
        HapticManager.vibrateClick()
    }

    /**
     * Stop any active breaches (e.g., on game reset)
     */
    fun stopAllBreaches() {
        isBreachActive = false
        SoundManager.stop("alarm")
    }
}
