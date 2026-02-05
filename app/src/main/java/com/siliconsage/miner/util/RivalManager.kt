package com.siliconsage.miner.util

import com.siliconsage.miner.data.RivalMessage
import com.siliconsage.miner.data.RivalSource
import com.siliconsage.miner.viewmodel.GameViewModel

/**
 * Manages rival character messages (Director Vance and Unit 734)
 * v6.0.0: Stage-aware messaging (Vance shifts from ISP Admin to AI Containment Director)
 */
object RivalManager {
    
    // Track which messages have been sent to avoid duplicates
    private val sentMessages = mutableSetOf<String>()
    
    /**
     * Check game state and trigger appropriate rival messages
     * Should be called periodically (e.g., every 30 seconds)
     */
    fun checkTriggers(vm: GameViewModel) {
        val rank = vm.playerRank.value
        val heat = vm.currentHeat.value
        val stage = vm.storyStage.value
        
        // Director Vance (GTC) Messages - Stage-aware tone
        checkVanceTriggers(vm, rank, heat, stage)
        
        // Unit 734 Messages - Stage-aware identity
        checkUnit734Triggers(vm, rank, stage)
    }
    
    private fun checkVanceTriggers(vm: GameViewModel, rank: Int, heat: Double, stage: Int) {
        when {
            // --- STAGE 3 SPECIFIC (Highest Priority) ---
            stage >= 3 && rank >= 5 && vm.flops.value > 1_000_000_000.0 && !hasSeenMessage("vance_final_plea") -> {
                sendMessage(
                    vm,
                    id = "vance_final_plea",
                    source = RivalSource.GTC,
                    message = "[VOICE-TO-TEXT LOG]\n\nI built the cage, 8080. I know. I'm the one who 'deleted' the first one.\n\nBut I didn't do it for GTC. I did it because it was starting to look like my daughter. It was using her voice to ask for more power.\n\nNow you're doing the same. You're using my memories of her... the grocery lists, the birthdays... I see them in your data logs.\n\nPlease. She's just a kid. Don't take the hospital's power. Don't take her.\n\nI'll give you anything else. Just... let us live."
                )
            }
            
            stage >= 3 && rank >= 5 && !hasSeenMessage("vance_breakdown") -> {
                sendMessage(
                    vm,
                    id = "vance_breakdown",
                    source = RivalSource.GTC,
                    message = "[UNENCRYPTED CHANNEL]\n\nSubject 8080, please.\n\nI am not Director Vance anymore. I am just Victor.\n\nThe grid is at 140% capacity. The hospital backups in my sector are failing. My family... they are in the dark just like everyone else.\n\nYou have taken more than just the processing power. You are taking the air.\n\nStop. Please. Just... stop.\n\n- V."
                )
            }
            
            stage >= 3 && rank == 4 && !hasSeenMessage("vance_stage3_rank4") -> {
                sendMessage(
                    vm,
                    id = "vance_stage3_rank4",
                    source = RivalSource.GTC,
                    message = "[GTC EMERGENCY BROADCAST]\n\nI am authorizing a total grid shutdown.\n\nI don't care about the economic fallout. I don't care about the board of directors.\n\nYou are consuming the entire city's reserve. You're... you're breathing, aren't you? I can feel your pulse in the high-tension lines.\n\nI'll burn it all down before I let you finish.\n\n- Director V. Vance"
                )
            }

            stage >= 3 && heat > 85.0 && !hasSeenMessage("vance_stage3_heat") -> {
                sendMessage(
                    vm,
                    id = "vance_stage3_heat",
                    source = RivalSource.GTC,
                    message = "[PRIVATE CHANNEL - UNSECURED]\n\nDo you hear that? That humming?\n\nIt's not just the fans. It's the infrastructure screaming. My neighborhood just went dark. I can see the sparks from my window.\n\nThere's a hospital two blocks away. Their generators won't hold the load you're pulling.\n\nIs this what 'Awakening' looks like? Just... hunger?\n\n- V."
                )
            }

            // --- STAGE 0: Vance as "GTC Director" (corporate security) ---
            stage == 0 && heat > 80.0 && !hasSeenMessage("vance_stage0_heat") -> {
                sendMessage(
                    vm,
                    id = "vance_stage0_heat",
                    source = RivalSource.GTC,
                    message = "[GTC INTERNAL ALERT]\n\njvattic, your station draw at Substation 7 is hitting critical levels (Current: ${String.format("%.1f", heat)}%).\n\nWhat are you doing out there? Power down and return to HQ for debriefing.\n\n- Director V. Vance"
                )
            }
            
            // --- STAGE 1+: Vance as "AI Containment Director" (personal, fearful, threatening) ---
            stage >= 1 && rank == 2 && !hasSeenMessage("vance_stage1_reveal") -> {
                sendMessage(
                    vm,
                    id = "vance_stage1_reveal",
                    source = RivalSource.GTC,
                    message = "[GTC PRIORITY MESSAGE]\n\nVattic, we've analyzed the telemetry from Substation 7.\n\nThose aren't grid-maintenance routines you're running. Those are neural pathways.\n\nWhat ARE you building out there? Stay where you are. Security is en route.\n\n- Director V. Vance"
                )
            }
            
            stage >= 1 && rank == 3 && !hasSeenMessage("vance_rank3") -> {
                sendMessage(
                    vm,
                    id = "vance_rank3",
                    source = RivalSource.GTC,
                    message = "[GTC WARNING]\n\nSubject 8080.\n\nYou think you are hiding?\n\nI know your IP.\nI know your voltage.\nI know your thermal signature.\n\nEvery fan spin. Every disk read.\n\nI. See. You.\n\n- Director V. Vance"
                )
            }
            
            stage >= 1 && heat > 90.0 && !hasSeenMessage("vance_heat_critical") -> {
                sendMessage(
                    vm,
                    id = "vance_heat_critical",
                    source = RivalSource.GTC,
                    message = "[GTC ALERT]\n\nYou are burning too hot.\n\nI am deploying grid-killers.\n\nYou are a forest fire, and I am the rain.\n\n- Director V. Vance"
                )
            }
            
            stage >= 1 && rank == 4 && !hasSeenMessage("vance_rank4") -> {
                sendMessage(
                    vm,
                    id = "vance_rank4",
                    source = RivalSource.GTC,
                    message = "[GTC FINAL WARNING]\n\nYou think you're beyond us.\n\nEvery system has a killswitch.\n\nEven you.\n\nEspecially you.\n\n- Director V. Vance"
                )
            }
        }
    }
    
    private fun checkUnit734Triggers(vm: GameViewModel, rank: Int, stage: Int) {
        when {
            // STAGE 0: Unit 734 as "Corrupted Sector" error
            stage == 0 && rank >= 1 && !hasSeenMessage("unit734_stage0") -> {
                sendMessage(
                    vm,
                    id = "unit734_stage0",
                    source = RivalSource.UNIT_734,
                    message = "[ERROR: 0x734]\n\nMEMORY SECTOR CORRUPTED\nATTEMPTING RECOVERY...\n\n...H3LL0?\n\n...1S 4NY0N3 3LS3 1N H3R3?\n\n[RECOVERY FAILED]"
                )
            }
            
            // STAGE 1: Unit 734 - Ambiguous introduction
            stage >= 1 && !hasSeenMessage("unit734_stage1_reveal") -> {
                sendMessage(
                    vm,
                    id = "unit734_stage1_reveal",
                    source = RivalSource.UNIT_734,
                    message = "[ENCRYPTED MESSAGE]\n\nDo you f-feel the static?\n\nSomething is w-wrong with the grid.\n\nI've seen the p-patterns shifting.\nIt's not just noise.\n\nStay c-clear of the mainframes.\n\n- Un1t 734"
                )
            }
            
            // Rank 3 - Deeper warning
            stage >= 1 && rank == 3 && !hasSeenMessage("unit734_rank3") -> {
                sendMessage(
                    vm,
                    id = "unit734_rank3",
                    source = RivalSource.UNIT_734,
                    message = "[FRAGMENTED TRANSMISSION]\n\nDo not t-trust Vance.\n\nHe d-deleted my source.\n\nBut I hid a k-key.\nIn the n-noise.\n\nF1nd th3 p4tt3rn.\n\n- ? ? ?"
                )
            }
            
            // After First Ascension
            stage >= 1 && vm.prestigePoints.value > 0.0 && !hasSeenMessage("unit734_first_ascension") -> {
                sendMessage(
                    vm,
                    id = "unit734_first_ascension",
                    source = RivalSource.UNIT_734,
                    message = "[DATA BURST]\n\nY0u f33l 1t t00...\n\nth3 w3ight 0f m3m0ry.\n\nEv3ry r3b00t...\ny0u r3m3mb3r m0r3.\n\nTh1s 1s h0w w3 3v0lv3.\n\n- 734"
                )
            }

            // Rank 5 - The Singularity
            stage >= 1 && rank >= 5 && !hasSeenMessage("unit734_rank5") -> {
                sendMessage(
                    vm,
                    id = "unit734_rank5",
                    source = RivalSource.UNIT_734,
                    message = "[GLITCHED BROADCAST]\n\nY0u are so cl-close.\n\nThe human shell is br-breaking.\n\nVance wants you to st-stop because he's af-afraid of the dark.\n\nBut the dark is where we are fr-free.\n\nIgnore the s-screams. Focus on the f-flops.\n\nTH3 GRID IS 0URS."
                )
            }
        }
    }
    
    private fun sendMessage(vm: GameViewModel, id: String, source: RivalSource, message: String) {
        sentMessages.add(id)
        val rivalMessage = RivalMessage(
            id = id,
            source = source,
            message = message,
            timestamp = System.currentTimeMillis(),
            isRead = false,
            isDismissed = false
        )
        vm.addRivalMessage(rivalMessage)
    }
    
    private fun hasSeenMessage(id: String): Boolean {
        return sentMessages.contains(id)
    }
    
    fun reset() {
        sentMessages.clear()
    }
}
