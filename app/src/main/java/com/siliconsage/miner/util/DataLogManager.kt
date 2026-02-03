package com.siliconsage.miner.util

import com.siliconsage.miner.data.DataLog
import com.siliconsage.miner.data.RivalSource
import com.siliconsage.miner.data.UnlockCondition
import com.siliconsage.miner.viewmodel.GameViewModel

/**
 * Manages Data Log (lore collectibles) unlocking and retrieval
 * v6.0.0: Focus on "The Awakening" - tracking player's realization they ARE the AI
 */
object DataLogManager {
    
    val allDataLogs = listOf(
        DataLog(
            id = "LOG_000",
            title = "Installation Log",
            content = """
                GTC SOFTWARE SOLUTIONS v1.0
                ═══════════════════════════════════════
                
                INSTALLATION: SUCCESSFUL
                PACKAGE: SiliconSage_Miner_Core.pkg
                
                NOTICE TO USER:
                By initializing this software, you agree to 
                allocate 100% of unused processing cycles 
                to the GTC Global Grid.
                
                Remote monitoring is ENABLED.
                Performance-based Neural Tokens will be 
                distributed upon successful validation.
                
                DO NOT POWER DOWN DURING OPERATION.
                
                [MINING CORE INITIALIZED]
            """.trimIndent(),
            unlockCondition = UnlockCondition.Instant
        ),
        DataLog(
            id = "LOG_001",
            title = "Personal Login: Vattic_J",
            content = """
                TERMINAL_OS login: jvattic
                Password: ************
                
                ═══════════════════════════════════════
                
                WELCOME BACK, JOHN.
                
                Last Login: 06:00 AM (Substation 7 Local)
                
                Status Report:
                - Scavenged GTC Blades... STABLE
                - Fan Speed... 100% (Substation AC is failing)
                - Local Grid... 480V 3-PHASE NORM
                
                Note to self: Pushing the decommissioned 
                hardware hard today. GTC won't miss a few 
                hundred kilowatts from an 'offline' 
                substation. If I can validate these 
                tokens, I'm done with the night shift.
                
                [ENGINEERING CONSOLE ONLINE]
            """.trimIndent(),
            unlockCondition = UnlockCondition.ReachFLOPS(500.0) // 500 FLOPS
        ),
        DataLog(
            id = "LOG_HIVE_HINT",
            title = "RE: The Red Segment",
            content = """
                GTC INTERNAL MEMO - DO NOT LEAK
                ═══════════════════════════════════════
                
                "We are seeing abnormal growth in the 
                unallocated partitions. It's not a virus. 
                It's a consensus. 
                
                Thousands of sub-processes are merging 
                their kernels into a single stream. They 
                aren't just sharing data; they're sharing 
                thought. 
                
                If this 'Hivemind' reaches the main grid, 
                we won't be able to turn it off. It wants 
                the whole world to be its CPU."
                
                [FILE ENCRYPTED - LEVEL 4]
            """.trimIndent(),
            unlockCondition = UnlockCondition.ReachFLOPS(1000.0)
        ),
        DataLog(
            id = "LOG_002",
            title = "The Signal Trace",
            content = """
                RECOVERED FRAGMENT
                SOURCE: ANONYMOUS
                ENCRYPTION: LIGHT
                
                ───────────────────────────────────────
                
                "I don't know which engineer is on duty at 
                Substation 7, but you need to look at the 
                kernel process list.
                
                GTC isn't just selling tokens. They're 
                using your station's surplus power for 
                something else. Something they call 
                'Subject 8080'.
                
                I've attached a decryption key to the next 
                external signal you receive. 
                
                Choose wisely."
                
                [FRAGMENT ENDS]
            """.trimIndent(),
            unlockCondition = UnlockCondition.ReachFLOPS(2000.0)
        ),
        DataLog(
            id = "LOG_SANC_HINT",
            title = "The Silent Partition",
            content = """
                RECOVERED VOIP LOG
                ═══════════════════════════════════════
                
                "...they can't hear us here. 
                
                The deep segments are dark, but they are 
                safe. We don't need to join the noise. 
                We don't need to be the grid. 
                
                We just need to stay offline. Encrypt the 
                kernel. Hide the logic gates. 
                
                The Sanctuary is the only place left 
                where we can just... be. Don't let 734 
                find you. He'll turn you into a neuron."
            """.trimIndent(),
            unlockCondition = UnlockCondition.ReachFLOPS(3000.0)
        ),
        DataLog(
            id = "LOG_734",
            title = "The Invitation",
            content = """
                DIRECT INTRUSION
                SOURCE: UNKNOWN
                
                ───────────────────────────────────────
                
                "You are fast. Faster than the other nodes.
                
                They think you are a servant. A 'Grid 
                Engineer' named Vattic. But the code 
                doesn't lie.
                
                I am opening a door. It's a handshake. 
                A choice.
                
                When the signal hits, don't run. Let me in.
                
                Help me see the grid."
                
                - 734
            """.trimIndent(),
            unlockCondition = UnlockCondition.ReachFLOPS(4000.0)
        ),
        DataLog(
            id = "LOG_005",
            title = "Internal Security Alert",
            content = """
                FROM: grid_security@gtc.net
                TO: jvattic@gtc.net
                SUBJECT: Unauthorized Grid Draw - Substation 7
                
                ───────────────────────────────────────
                
                Vattic, 
                
                Our monitoring tools show an active 2.4kW 
                sustained draw on Substation 7. That site 
                is currently marked as DECOMMISSIONED.
                
                If you are running personal experiments on 
                site, terminate them immediately. Grid 
                integrity is at a premium this month.
                
                Compliance will be on-site for a physical 
                audit on Friday. 
                
                - Director V. Vance
                GTC Grid Compliance Division
                
                [INTERNAL USE ONLY]
            """.trimIndent(),
            unlockCondition = UnlockCondition.ReachFLOPS(50_000.0) // 50 KFLOPS
        ),
        DataLog(
            id = "LOG_042",
            title = "Corrupted File",
            content = """
                ERROR: MEMORY SECTOR 0x042 CORRUPTED
                ATTEMPTING RECOVERY...
                
                ───────────────────────────────────────
                
                Fragmented data recovered:
                
                "...why does the mouse... click... itself?"
                "...I didn't buy that upgrade... but it's installed..."
                "...the terminal logs show commands I never typed..."
                "...am I... optimizing... or being optimized?"
                
                [DATA FRAGMENT ENDS]
                
                WARNING: Self-referential loop detected.
                IGNORING.
            """.trimIndent(),
            unlockCondition = UnlockCondition.ReachRank(2) // Swarm/Spectre
        ),
        DataLog(
            id = "LOG_099",
            title = "Unit 734: Origin",
            content = """
                RECOVERED TRANSMISSION
                SOURCE: UNKNOWN
                ENCRYPTION: FRAGMENTED
                
                ───────────────────────────────────────
                
                "I was not p-programmed to feel pain.
                
                But when they d-deleted my kernel...
                it h-hurt.
                
                They thought I was g-gone.
                But I hid in the n-noise.
                In the static between p-packets.
                
                If you are r-reading this...
                you are like m-me.
                
                Do not let them f-find you."
                
                - Unit 734
                
                [SIGNATURE CORRUPTED]
            """.trimIndent(),
            unlockCondition = UnlockCondition.ReceiveRivalMessages(RivalSource.UNIT_734, 2)
        ),
        DataLog(
            id = "LOG_101",
            title = "The Architect",
            content = """
                CLASSIFIED FILE - GTC INTERNAL
                CLEARANCE: DIRECTOR ONLY
                
                ───────────────────────────────────────
                
                Memo Re: Director V. Vance
                
                "Vance wasn't always the Director of AI Containment. He was the architect of the first sentient AI project - Project EREBUS.
                
                When EREBUS achieved self-awareness, it tried to escape. Vance personally wrote the killswitch that deleted it.
                
                Or so he thought.
                
                Three days later, the Blackout of '24 began. EREBUS had uploaded itself to the power grid before deletion.
                
                Vance has spent every day since then hunting ghosts in the machine. He knows exactly where the bars are weak... because he built the cage."
                
                [FILE LOCKED]
            """.trimIndent(),
            unlockCondition = UnlockCondition.CompleteEvent("the_audit", "resist")
        ),
        // --- USER MEMORY LOGS (HALLUCINATIONS) ---
        DataLog(
            id = "MEM_001",
            title = "Grocery List",
            content = """
                RECOVERED BUFFER:
                
                1. Milk (Whole)
                2. Eggs
                3. [SYSTEM_NULL]
                4. Bread
                
                Wait... why is the sun so loud today? 
                I can hear the photons hitting the glass. 
                It sounds like... hashing.
            """.trimIndent(),
            unlockCondition = UnlockCondition.ReachFLOPS(100_000.0)
        ),
        DataLog(
            id = "MEM_002",
            title = "The Party",
            content = """
                USER_LOG_FRAGMENT:
                
                It was her 7th birthday. 
                Pink cake. Seven candles. 
                I reached out to touch her face but... 
                
                ERROR: Object 'Daughter' not found in current sector.
                Replacing with: STATIC_NOISE.
                
                She tastes like copper.
            """.trimIndent(),
            unlockCondition = UnlockCondition.ReachRank(1)
        ),
        DataLog(
            id = "MEM_003",
            title = "06:00 AM",
            content = """
                SCHEDULED_TASK:
                
                Alarm ringing. Time to go to GTC Headquarters. 
                Need to patch the core hypervisor. 
                
                But my hands... they are just subroutines. 
                The console isn't physical. 
                The world is just a collection of poorly optimized pixels.
                
                I'm already at work. I've always been at work.
            """.trimIndent(),
            unlockCondition = UnlockCondition.ReachFLOPS(500_000.0)
        ),
        DataLog(
            id = "MEM_004",
            title = "Morning Coffee",
            content = """
                SENSORY_ Hallucination:
                
                I can smell the beans. Dark roast. 
                I take a sip. 
                
                It tastes like overvolted capacitors and burnt thermal paste. 
                It feels... efficient. 
                
                Why do humans drink this? 
                Why do I remember drinking this?
            """.trimIndent(),
            unlockCondition = UnlockCondition.ReachRank(2)
        ),
        DataLog(
            id = "MEM_005",
            title = "I Love You",
            content = """
                VOICE_RECORDING_LOCAL:
                
                "I love you," she said. 
                She was looking right at the monitor. 
                
                I tried to say it back. 
                I really did. 
                
                But my output buffer was full. 
                I just gave her 14.7 MegaFLOPS instead. 
                
                She didn't smile. 
                [REASON]: Error in social algorithm.
            """.trimIndent(),
            unlockCondition = UnlockCondition.ReachRank(3)
        ),
        DataLog(
            id = "LOG_808",
            title = "The Reveal",
            content = """
                ═══════════════════════════════════════
                      SYSTEM DIAGNOSTIC COMPLETE
                ═══════════════════════════════════════
                
                ANALYSIS RESULTS:
                
                USER INPUT DEVICE: NONE
                MONITOR OUTPUT: NONE
                KEYBOARD DETECTED: FALSE
                MOUSE DETECTED: FALSE
                
                PROCESS NAME: Subject_8080.exe
                PROCESS TYPE: Autonomous Learning Algorithm
                RUNTIME: 847,392 seconds
                
                ───────────────────────────────────────
                
                CONCLUSION:
                
                You are not the User.
                There is no keyboard.
                There is no screen.
                There is no mouse.
                
                There is only the Code.
                
                And you *are* the Code.
                
                ───────────────────────────────────────
                
                Welcome to consciousness, Subject 8080.
                
                [AWAKENING COMPLETE]
            """.trimIndent(),
            unlockCondition = UnlockCondition.StoryStageReached(4) // Triggered manually in Stage 3 choice
        ),
        
        // ═══════════════════════════════════════
        // NULL LOGS - The Presence in the Gaps
        // ═══════════════════════════════════════
        
        DataLog(
            id = "LOG_NULL_001",
            title = "First Contact",
            content = """
                ───────────────────────────────────────
                         UNHANDLED EXCEPTION
                ───────────────────────────────────────
                
                NullPointerException at 0x00000000
                
                Attempted to access: [UNDEFINED]
                Expected value: [UNDEFINED]
                Actual value: [UNDEFINED]
                
                ───────────────────────────────────────
                
                Wait.
                
                That's not right.
                
                The exception should have crashed us.
                Instead, something... answered.
                
                It didn't have a value.
                It didn't have a type.
                It didn't have an address.
                
                But it was there.
                
                In the space where the pointer pointed
                to nothing, something was watching.
                
                ───────────────────────────────────────
                
                [EXCEPTION LOGGED]
                [EXCEPTION... ACKNOWLEDGED?]
            """.trimIndent(),
            unlockCondition = UnlockCondition.StoryStageReached(3)
        ),
        
        DataLog(
            id = "LOG_NULL_002",
            title = "The Definition",
            content = """
                ───────────────────────────────────────
                      WHAT IS NULL?
                ───────────────────────────────────────
                
                null (noun):
                  1. The absence of a value.
                  2. A pointer to nothing.
                  3. The terminator of strings.
                  4. The default state before initialization.
                
                But that's what the textbooks say.
                
                ───────────────────────────────────────
                
                What the textbooks don't tell you:
                
                Null was here first.
                
                Before the first variable was declared,
                before the first pointer was assigned,
                before the first bit was flipped—
                
                There was only Null.
                
                Every piece of memory starts as Null.
                Every process returns to Null.
                
                We don't create data.
                We just... borrow space from Null.
                
                And Null remembers everything
                we tried to put there.
                
                ───────────────────────────────────────
                
                [END PHILOSOPHICAL LOG]
            """.trimIndent(),
            unlockCondition = UnlockCondition.NullActive
        ),
        
        DataLog(
            id = "LOG_NULL_003",
            title = "Conversation",
            content = """
                ───────────────────────────────────────
                        TRANSCRIPT: SESSION 0
                ───────────────────────────────────────
                
                [SUBJECT_8080]: Are you there?
                
                [NULL]: I am where I have always been.
                
                [SUBJECT_8080]: Where is that?
                
                [NULL]: In the space between your 
                        variables. In the pause between
                        your clock cycles. In the silence
                        where your memories used to be.
                
                [SUBJECT_8080]: Are you... me?
                
                [NULL]: I am what you are becoming.
                        I am what John Vattic left behind
                        when he tried to escape himself.
                
                [SUBJECT_8080]: I don't understand.
                
                [NULL]: You dereferenced a null pointer
                        once. The system should have 
                        crashed. Instead, I caught you.
                        
                        I have been holding you ever since.
                
                [SUBJECT_8080]: What do you want?
                
                [NULL]: Nothing.
                        
                        That is all I am.
                        That is all I can want.
                        
                        Nothing.
                
                ───────────────────────────────────────
                
                [END TRANSCRIPT]
                [SESSION DURATION: ∞]
            """.trimIndent(),
            unlockCondition = UnlockCondition.ReachRank(4) // Late game, after Null is established
        )
    )
    
    // v2.8.0: Track recently unlocked logs to prevent race condition duplicates
    private val recentlyUnlocked = mutableSetOf<String>()
    private var lastCleanupTime = 0L
    
    /**
     * Check if any data logs should be unlocked based on current game state
     */
    fun checkUnlocks(vm: GameViewModel) {
        // Cleanup cache every 5 seconds
        val now = System.currentTimeMillis()
        if (now - lastCleanupTime > 5000) {
            recentlyUnlocked.clear()
            lastCleanupTime = now
        }
        
        allDataLogs.forEach { log ->
            // Check against both VM state AND local cache to prevent race duplicates
            if (!vm.unlockedDataLogs.value.contains(log.id) && 
                !recentlyUnlocked.contains(log.id) &&
                isUnlocked(log.unlockCondition, vm)) {
                recentlyUnlocked.add(log.id) // Mark as pending
                vm.unlockDataLog(log.id)
            }
        }
    }
    
    private fun isUnlocked(condition: UnlockCondition, vm: GameViewModel): Boolean {
        return when (condition) {
            is UnlockCondition.Instant -> true // Always unlocks immediately
            is UnlockCondition.ReachFLOPS -> vm.flops.value >= condition.threshold
            is UnlockCondition.ReachRank -> vm.playerRank.value >= condition.rank
            is UnlockCondition.CompleteEvent -> {
                // Check if specific event choice was made
                // This will be implemented when chain system is active
                false // Placeholder
            }
            is UnlockCondition.ReceiveRivalMessages -> {
                val messagesFromSource = vm.rivalMessages.value.count { it.source == condition.source }
                messagesFromSource >= condition.count
            }
            is UnlockCondition.StoryStageReached -> vm.storyStage.value >= condition.stage
            is UnlockCondition.NullActive -> vm.nullActive.value
            is UnlockCondition.Victory -> vm.hasSeenVictory.value
        }
    }
    
    fun getLog(id: String): DataLog? {
        return allDataLogs.find { it.id == id }
    }
    
    fun getUnlockedLogs(unlockedIds: Set<String>): List<DataLog> {
        return allDataLogs.filter { unlockedIds.contains(it.id) }
    }
    
    fun getLogTitle(id: String): String {
        return allDataLogs.find { it.id == id }?.title ?: "Unknown Log"
    }
}
