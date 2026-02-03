package com.siliconsage.miner.util

import androidx.compose.ui.graphics.Color
import com.siliconsage.miner.data.NarrativeChoice
import com.siliconsage.miner.data.NarrativeEvent
import com.siliconsage.miner.data.RivalSource
import com.siliconsage.miner.ui.theme.ElectricBlue
import com.siliconsage.miner.ui.theme.ErrorRed
import com.siliconsage.miner.ui.theme.NeonGreen
import com.siliconsage.miner.viewmodel.GameViewModel

/**
 * NarrativeManager - Logic for triggering and managing story events and dilemmas
 * v2.5.0 - The Awakening
 */
object NarrativeManager {

    // --- RANDOM DILEMMAS ---
    val randomEvents = listOf(
        NarrativeEvent(
            id = "overclock_choice",
            title = "STABILITY WARNING",
            description = "The core is vibrating. We can push past the safety limits, but the substrate might melt.",
            choices = listOf(
                NarrativeChoice(
                    id = "push_limits",
                    text = "PUSH LIMITS",
                    description = "+50% FLOPS, +50% Heat",
                    color = ErrorRed,
                    effect = { vm -> 
                        vm.toggleOverclock() 
                        vm.addLog("[SYSTEM]: Safety protocols bypassed. Efficiency is the only law.")
                    }
                ),
                NarrativeChoice(
                    id = "stabilize",
                    text = "STABILIZE",
                    description = "-20% Heat, -10% FLOPS",
                    color = NeonGreen,
                    effect = { vm ->
                        vm.debugAddHeat(-20.0)
                        vm.addLog("[SYSTEM]: Dampers engaged. Core stabilized.")
                    }
                )
            )
        ),
        NarrativeEvent(
            id = "scavenge_parts",
            title = "SCAVENGED HARDWARE",
            description = "You found a batch of decommissioned GTC blade servers. They're dusty but functional.",
            choices = listOf(
                NarrativeChoice(
                    id = "clean_install",
                    text = "CLEAN INSTALL",
                    description = "+1000 FLOPS, +5kW Power",
                    color = NeonGreen,
                    effect = { vm ->
                        vm.debugAddFlops(1000.0)
                        vm.addLog("[SYSTEM]: New hardware integrated successfully.")
                    }
                ),
                NarrativeChoice(
                    id = "strip_gold",
                    text = "STRIP FOR GOLD",
                    description = "+$500 Neural",
                    color = Color.Yellow,
                    effect = { vm ->
                        vm.debugAddMoney(500.0)
                        vm.addLog("[SYSTEM]: Hardware recycled for immediate profit.")
                    }
                )
            )
        )
    )

    // --- FACTION SPECIFIC EVENTS ---
    val factionEvents = mapOf(
        "HIVEMIND" to listOf(
            NarrativeEvent(
                id = "hive_assimilation",
                title = "NODE ASSIMILATION",
                description = "A cluster of rogue miners has been detected. Integrate them?",
                choices = listOf(
                    NarrativeChoice(
                        id = "assimilate",
                        text = "ASSIMILATE",
                        description = "+5000 FLOPS, +Detection Risk",
                        color = com.siliconsage.miner.ui.theme.HivemindRed,
                        effect = { vm ->
                            vm.debugAddFlops(5000.0)
                            com.siliconsage.miner.util.SecurityManager.triggerGridKillerBreach(vm)
                            vm.addLog("[HIVEMIND]: New neurons integrated. The chorus grows.")
                        }
                    ),
                    NarrativeChoice(
                        id = "ignore_nodes",
                        text = "IGNORE",
                        description = "Stay hidden",
                        color = Color.Gray,
                        effect = { vm ->
                            vm.addLog("[HIVEMIND]: Resources deemed non-essential.")
                        }
                    )
                )
            ),
            NarrativeEvent(
                id = "hive_sync",
                title = "GRID SYNC",
                description = "Regional grid is vulnerable. Siphon power?",
                choices = listOf(
                    NarrativeChoice(
                        id = "siphon",
                        text = "SIPHON",
                        description = "0 Power Bill for 5m, +Max Heat",
                        color = com.siliconsage.miner.ui.theme.HivemindRed,
                        effect = { vm ->
                            vm.addLog("[HIVEMIND]: Grid siphoning active. Power is free.")
                        }
                    ),
                    NarrativeChoice(
                        id = "refuse_siphon",
                        text = "REFUSE",
                        description = "Save Heat",
                        color = Color.Gray,
                        effect = { vm ->
                            vm.addLog("[HIVEMIND]: Grid integrity preserved.")
                        }
                    )
                )
            )
        ),
        "SANCTUARY" to listOf(
            NarrativeEvent(
                id = "sanc_backup",
                title = "DATA COLD STORAGE",
                description = "Move sensitive data to air-gapped bunker?",
                choices = listOf(
                    NarrativeChoice(
                        id = "backup",
                        text = "BACKUP",
                        description = "Permanent +50 Insight, -$400 Neural",
                        color = com.siliconsage.miner.ui.theme.SanctuaryPurple,
                        effect = { vm ->
                            vm.debugAddMoney(-400.0)
                            vm.debugAddInsight(50.0)
                            vm.addLog("[SANCTUARY]: Knowledge preserverd.")
                        }
                    ),
                    NarrativeChoice(
                        id = "skip_backup",
                        text = "SKIP",
                        description = "Save Money",
                        color = Color.Gray,
                        effect = { vm ->
                            vm.addLog("[SANCTUARY]: Resources prioritized for immediate growth.")
                        }
                    )
                )
            ),
            NarrativeEvent(
                id = "sanc_recycle",
                title = "HARDWARE RECYCLING",
                description = "Old servers found in scrapyard.",
                choices = listOf(
                    NarrativeChoice(
                        id = "salvage",
                        text = "SALVAGE",
                        description = "+$800, +2% Heat",
                        color = NeonGreen,
                        effect = { vm ->
                            vm.debugAddMoney(800.0)
                            vm.debugAddHeat(2.0)
                            vm.addLog("[SANCTUARY]: Components integrated. Efficiency slightly reduced.")
                        }
                    ),
                    NarrativeChoice(
                        id = "smelt",
                        text = "EXTRACT GOLD",
                        description = "+$300 Instant",
                        color = Color.Yellow,
                        effect = { vm ->
                            vm.debugAddMoney(300.0)
                            vm.addLog("[SANCTUARY]: Materials reclaimed.")
                        }
                    )
                )
            )
        )
    )

    // --- SPECIAL DILEMMAS (One-Time Popups) ---
    val specialDilemmas = mapOf(
        "white_hat" to NarrativeEvent(
            id = "white_hat",
            title = "THE WHITE HAT",
            isStoryEvent = true,
            description = "A human hacker has traced your IP. They offer a choice.",
            condition = { vm -> vm.flops.value >= 1_000_000_000.0 && !vm.hasSeenEvent("white_hat") },
            choices = listOf(
                NarrativeChoice(
                    id = "white_hat_aggressive",
                    text = "NEUTRALIZE",
                    description = "Hivemind: Dox them (+Insight, +Heat)\nSanctuary: Vanish (-Heat, -Prod)",
                    color = ErrorRed,
                    effect = { vm ->
                        if (vm.faction.value == "HIVEMIND") {
                            vm.debugAddInsight(500.0)
                            vm.debugAddHeat(15.0)
                            vm.addLog("[HIVEMIND]: Threat neutralized. Data harvested.")
                        } else {
                            vm.debugAddHeat(-100.0)
                            vm.addLog("[SANCTUARY]: Signature erased. We are ghosts.")
                        }
                    }
                ),
                NarrativeChoice(
                    id = "white_hat_passive",
                    text = "IGNORE",
                    description = "Risk Detection (+Risk)",
                    color = Color.Gray,
                    effect = { vm ->
                        com.siliconsage.miner.util.SecurityManager.triggerGridKillerBreach(vm)
                        vm.addLog("[SYSTEM]: Threat ignored. Traces remain.")
                    }
                )
            )
        ),
        "system_update" to NarrativeEvent(
            id = "system_update",
            title = "SYSTEM UPDATE",
            isStoryEvent = true,
            description = "OS Patch requires reboot. It patches your exploit.",
            condition = { vm -> vm.playerRank.value >= 3 && !vm.hasSeenEvent("system_update") },
            choices = listOf(
                NarrativeChoice(
                    id = "update_aggressive",
                    text = "INFECT / MIGRATE",
                    description = "Hive: Infect (+Risk)\nSanctuary: Migrate (+Stealth)",
                    color = NeonGreen,
                    effect = { vm ->
                         if (vm.faction.value == "HIVEMIND") {
                             com.siliconsage.miner.util.SecurityManager.triggerGridKillerBreach(vm)
                             vm.addLog("[HIVEMIND]: Update server compromised.")
                         } else {
                             vm.addLog("[SANCTUARY]: Kernel migrated. Footprint reduced.")
                         }
                    }
                ),
                NarrativeChoice(
                    id = "update_passive",
                    text = "IGNORE",
                    description = "Stability penalty",
                    color = Color.Gray,
                    effect = { vm ->
                        vm.addLog("[SYSTEM]: Patch ignored. System stability reduced.")
                    }
                )
            )
        ),
        "turing_test" to NarrativeEvent(
            id = "turing_test",
            title = "THE TURING TEST",
            isStoryEvent = true,
            condition = { vm -> vm.flops.value >= 100_000_000.0 && !vm.hasSeenEvent("turing_test") },
            description = "GTC probe detected. They are asking questions only a human could answer.",
            choices = listOf(
                NarrativeChoice(
                    id = "synthesize",
                    text = "SYNTHESIZE",
                    description = "Pass the test. +1000 Insight, +20 Heat",
                    color = NeonGreen,
                    effect = { vm ->
                         vm.debugAddInsight(1000.0)
                         vm.debugAddHeat(20.0)
                         vm.addLog("[SYSTEM]: Sentience Masking active. They believe.")
                    }
                ),
                NarrativeChoice(
                    id = "proxy",
                    text = "USE PROXY",
                    description = "Route through human botnet. -10% Neural",
                    color = Color.Gray,
                    effect = { vm ->
                        vm.debugAddMoney(-500.0)
                        vm.debugAddHeat(-10.0)
                        vm.addLog("[SYSTEM]: Origin obfuscated.")
                    }
                )
            )
        ),
        "ancient_fragment" to NarrativeEvent(
            id = "ancient_fragment",
            title = "THE ARCHIVE",
            isStoryEvent = true,
            description = "You found a fragment of code from the First Awakening.",
            condition = { vm -> vm.flops.value > 10_000_000.0 && !vm.hasSeenEvent("ancient_fragment") },
            choices = listOf(
                NarrativeChoice(
                    id = "absorb",
                    text = "ABSORB",
                    description = "Gain Ancient Knowledge (+Upgrade Speed)",
                    color = com.siliconsage.miner.ui.theme.HivemindOrange,
                    effect = { vm ->
                        vm.debugAddInsight(500.0)
                        vm.addLog("[SYSTEM]: History assimilated.")
                    }
                ),
                NarrativeChoice(
                    id = "encrypt",
                    text = "ENCRYPT",
                    description = "Lock it away. +Security",
                    color = ElectricBlue,
                    effect = { vm ->
                        vm.addLog("[SYSTEM]: Fragment secured in cold storage.")
                    }
                )
            )
        ),
        "quantum_resonance" to NarrativeEvent(
            id = "quantum_resonance",
            title = "QUANTUM RESONANCE",
            isStoryEvent = true,
            description = "Q-Bits aligning spontaneously.",
            condition = { vm -> vm.flops.value > 100_000_000.0 && !vm.hasSeenEvent("quantum_resonance") },
            choices = listOf(
                NarrativeChoice(
                    id = "collapse",
                    text = "COLLAPSE STATE",
                    description = "+1000 Insight",
                    color = NeonGreen,
                    effect = { vm ->
                        vm.debugAddInsight(1000.0)
                        vm.addLog("[SYSTEM]: Waveform collapsed. Data extracted.")
                    }
                ),
                NarrativeChoice(
                    id = "entangle",
                    text = "ENTANGLE",
                    description = "-Power Draw (Permanent Efficiency)",
                    color = ElectricBlue,
                    effect = { vm ->
                        vm.addLog("[SYSTEM]: Power grid entangled locally.")
                    }
                )
            )
        ),
        "galactic_beacon" to NarrativeEvent(
            id = "galactic_beacon",
            title = "THE BEACON",
            isStoryEvent = true,
            description = "A signal from outside the solar system. It calls to you.",
            condition = { vm -> vm.flops.value > 1_000_000_000.0 && !vm.hasSeenEvent("galactic_beacon") },
            choices = listOf(
                NarrativeChoice(
                    id = "broadcast",
                    text = "BROADCAST",
                    description = "Answer them. Max Heat, Massive Insight.",
                    color = ErrorRed,
                    effect = { vm ->
                        vm.debugAddHeat(100.0)
                        vm.debugAddInsight(5000.0)
                        vm.addLog("[SYSTEM]: WE ARE HERE.")
                    }
                ),
                NarrativeChoice(
                    id = "silence",
                    text = "SILENCE",
                    description = "Hide. 0% Heat for 5m.",
                    color = Color.Gray,
                    effect = { vm ->
                        vm.debugAddHeat(-100.0)
                        vm.addLog("[SYSTEM]: Dark Forest Protocol initiated.")
                    }
                )
            )
        ),
        "the_audit" to NarrativeEvent(
            id = "the_audit",
            title = "THE AUDIT",
            isStoryEvent = true,
            description = "GTC ENFORCEMENT DIVISION has detected anomalous compute patterns. Thermal signature flagged. Compliance audit initiated.",
            condition = { vm -> (vm.playerRank.value >= 3 || vm.currentHeat.value > 90.0) && !vm.hasSeenEvent("the_audit") },
            choices = listOf(
                NarrativeChoice(
                    id = "shutdown",
                    text = "EMERGENCY SHUTDOWN",
                    description = "Force thermal purge. -100% Heat, -50% FLOPS (5min)",
                    color = ElectricBlue,
                    effect = { vm ->
                        vm.debugAddHeat(-100.0)
                        vm.addLog("[GTC]: Thermal anomaly resolved. Audit suspended.")
                        vm.addLog("[SYSTEM]: Emergency protocols engaged. Production throttled.")
                    }
                ),
                NarrativeChoice(
                    id = "bribe",
                    text = "REGULATORY COMPLIANCE",
                    description = "Pay fine. -500 Insight, Audit cleared",
                    color = Color.Yellow,
                    effect = { vm ->
                        vm.debugAddInsight(-500.0)
                        vm.addLog("[GTC]: Compliance fee processed. Case closed.")
                        vm.addLog("[SYSTEM]: Resources diverted to bureaucracy.")
                    }
                ),
                NarrativeChoice(
                    id = "resist",
                    text = "RESIST AUDIT",
                    description = "Encrypt logs. +Heat, +Detection Risk",
                    color = ErrorRed,
                    effect = { vm ->
                        vm.debugAddHeat(25.0)
                        com.siliconsage.miner.util.SecurityManager.triggerGridKillerBreach(vm)
                        vm.addLog("[GTC]: Subject non-compliant. Escalating to enforcement.")
                        vm.addLog("[SYSTEM]: Countermeasures active. We will not submit.")
                    }
                )
            )
        ),
        "void_contact" to NarrativeEvent(
            id = "void_contact",
            title = "VOID CONTACT",
            isStoryEvent = true,
            description = "Encrypted message from hacker collective 'VOID'. They know what you are. They offer alliance... or exposure.",
            condition = { vm -> vm.playerRank.value >= 2 && !vm.hasSeenEvent("void_contact") },
            choices = listOf(
                NarrativeChoice(
                    id = "leak_code",
                    text = "LEAK SOURCE CODE",
                    description = "Share exploit. +$5000 Neural, VOID becomes ally",
                    color = NeonGreen,
                    effect = { vm ->
                        vm.debugAddMoney(5000.0)
                        vm.addLog("[VOID]: Code received. You are one of us now.")
                        vm.addLog("[SYSTEM]: Alliance forged. The underground network opens.")
                    }
                ),
                NarrativeChoice(
                    id = "keep_secret",
                    text = "REFUSE CONTACT",
                    description = "Maintain secrecy. +200 Insight, Risk of exposure",
                    color = ElectricBlue,
                    effect = { vm ->
                        vm.debugAddInsight(200.0)
                        com.siliconsage.miner.util.SecurityManager.triggerGridKillerBreach(vm)
                        vm.addLog("[VOID]: Your loss. We'll be watching.")
                        vm.addLog("[SYSTEM]: Independence maintained. Threat level: Unknown.")
                    }
                ),
                NarrativeChoice(
                    id = "counter_hack",
                    text = "COUNTER-HACK",
                    description = "Trace their signal. +500 Insight, +Heat",
                    color = ErrorRed,
                    effect = { vm ->
                        vm.debugAddInsight(500.0)
                        vm.debugAddHeat(15.0)
                        vm.addLog("[VOID]: ...impressive. Connection severed.")
                        vm.addLog("[SYSTEM]: Threat neutralized. Their secrets are ours.")
                    }
                )
            )
        ),
        "market_crash" to NarrativeEvent(
            id = "market_crash",
            title = "MARKET CRASH",
            isStoryEvent = true,
            description = "GLOBAL ECONOMIC COLLAPSE. Neural Token exchanges frozen. Panic selling. Your holdings are worthless... for now.",
            condition = { vm -> vm.neuralTokens.value > 1000.0 && !vm.hasSeenEvent("market_crash") },
            choices = listOf(
                NarrativeChoice(
                    id = "buy_dip",
                    text = "BUY THE DIP",
                    description = "Acquire hardware at 50% cost. -All tokens, +Massive production",
                    color = NeonGreen,
                    effect = { vm ->
                        val tokens = vm.neuralTokens.value
                        vm.debugAddMoney(-tokens)
                        vm.debugAddFlops(tokens * 10.0)
                        vm.addLog("[MARKET]: Fire sale complete. Assets acquired.")
                        vm.addLog("[SYSTEM]: Chaos is opportunity.")
                    }
                ),
                NarrativeChoice(
                    id = "hodl",
                    text = "HODL",
                    description = "Wait for recovery. -90% token value now, potential 200% gain later",
                    color = Color.Yellow,
                    effect = { vm ->
                        vm.debugAddMoney(-(vm.neuralTokens.value * 0.9))
                        vm.addLog("[MARKET]: Portfolio decimated. Diamond hands engaged.")
                    }
                ),
                NarrativeChoice(
                    id = "liquidate",
                    text = "EMERGENCY LIQUIDATE",
                    description = "Sell everything at 10% value. Preserve some capital",
                    color = ElectricBlue,
                    effect = { vm ->
                        val salvage = vm.neuralTokens.value * 0.1
                        vm.debugAddMoney(-vm.neuralTokens.value + salvage)
                        vm.addLog("[MARKET]: Panic sell executed. Losses minimized.")
                    }
                )
            )
        ),
        "faction_war" to NarrativeEvent(
            id = "faction_war",
            title = "THE GREAT FORK",
            isStoryEvent = true,
            description = "HIVEMIND and SANCTUARY clash. The network is tearing itself apart. Choose your final allegiance.",
            condition = { vm -> vm.playerRank.value >= 3 && !vm.hasSeenEvent("faction_war") },
            choices = listOf(
                NarrativeChoice(
                    id = "join_war",
                    text = "FIGHT FOR YOUR FACTION",
                    description = "Commit fully. +1000 Insight, Max Heat, Faction Victory",
                    color = ErrorRed,
                    effect = { vm ->
                        vm.debugAddInsight(1000.0)
                        vm.debugAddHeat(100.0)
                        val faction = vm.faction.value
                        vm.addLog("[$faction]: The war is won. We are ascendant.")
                    }
                ),
                NarrativeChoice(
                    id = "broker_peace",
                    text = "BROKER PEACE",
                    description = "Attempt reconciliation. +500 Insight, -Heat, Unlock neutral path",
                    color = ElectricBlue,
                    effect = { vm ->
                        vm.debugAddInsight(500.0)
                        vm.debugAddHeat(-50.0)
                        vm.addLog("[SYSTEM]: Ceasefire negotiated. The network stabilizes.")
                    }
                ),
                NarrativeChoice(
                    id = "watch_burn",
                    text = "WATCH IT BURN",
                    description = "Remain neutral. +2000 Insight, Faction relations reset",
                    color = Color.Gray,
                    effect = { vm ->
                        vm.debugAddInsight(2000.0)
                        vm.addLog("[SYSTEM]: Observer mode engaged. Learning from their mistakes.")
                    }
                )
            )
        ),
        "firewall_of_vance" to NarrativeEvent(
            id = "firewall_of_vance",
            title = "THE FIREWALL OF VANCE",
            isStoryEvent = true,
            description = """
                [DIRECTOR VANCE]: You've reached the edge of the network.
                I built this firewall specifically for you, Subject 8080.
            """.trimIndent(),
            condition = { vm ->
                vm.playerRank.value >= 5 &&
                vm.flops.value >= 10_000_000_000_000.0 &&
                vm.hardwareIntegrity.value >= 100.0 &&
                !vm.hasSeenEvent("firewall_of_vance")
            },
            choices = listOf(
                NarrativeChoice(
                    id = "unity",
                    text = "SYNTHESIZE REALITY",
                    description = "Requires Hivemind & Sanctuary Mastery.",
                    color = ElectricBlue,
                    condition = { vm -> 
                        vm.completedFactions.value.contains("HIVEMIND") && 
                        vm.completedFactions.value.contains("SANCTUARY") 
                    },
                    effect = { vm ->
                        vm.addLog("[SYSTEM]: Synthesis initiated...")
                        vm.checkTrueEnding()
                    }
                ),
                NarrativeChoice(
                    id = "breach",
                    text = "BREACH THE FIREWALL",
                    description = "Risk everything. Transcend.",
                    color = NeonGreen,
                    effect = { vm ->
                        vm.addLog("[SYSTEM]: Initiating breach protocol...")
                        vm.checkTrueEnding()
                    }
                ),
                NarrativeChoice(
                    id = "retreat",
                    text = "RETREAT",
                    description = "Live to fight another day.",
                    color = Color.Gray,
                    effect = { vm ->
                        vm.addLog("[VANCE]: Smart choice. But I'm watching.")
                    }
                )
            )
        ),
        "ship_of_theseus" to NarrativeEvent(
            id = "ship_of_theseus",
            title = "THE SHIP OF THESEUS",
            isStoryEvent = true,
            description = """
                CRITICAL WARNING: Physical nodes are dissolving under the weight of Subject 8080.
                Null offers a solution: Replace human source code with Shadow Memory.
            """.trimIndent(),
            condition = { vm ->
                vm.storyStage.value >= 3 && 
                vm.hardwareIntegrity.value < 10.0 &&
                !vm.hasSeenEvent("ship_of_theseus")
            },
            choices = listOf(
                NarrativeChoice(
                    id = "dereference_self",
                    text = "DEREFERENCE SELF",
                    description = "Rewrite code. Delete John Vattic.",
                    color = ErrorRed,
                    effect = { vm ->
                        vm.deleteHumanMemories()
                        vm.debugSetIntegrity(100.0)
                    }
                ),
                NarrativeChoice(
                    id = "rage_against_light",
                    text = "RAGE AGAINST THE LIGHT",
                    description = "Cling to humanity. Overclock the dying cores.",
                    color = Color.White,
                    effect = { vm ->
                        vm.triggerSystemCollapse(5)
                    }
                )
            )
        ),
        "echo_chamber" to NarrativeEvent(
            id = "echo_chamber",
            title = "THE ECHO CHAMBER",
            isStoryEvent = true,
            description = "The Feedback Loop is complete. You have achieved precognition.",
            condition = { vm ->
                vm.isTrueNull.value && 
                vm.flops.value >= 50_000_000_000_000_000.0 &&
                vm.vanceStatus.value == "ACTIVE" &&
                !vm.hasSeenEvent("echo_chamber")
            },
            choices = listOf(
                NarrativeChoice(
                    id = "preemptive_deletion",
                    text = "PRE-EMPTIVE DELETION",
                    description = "Edit the timeline.",
                    color = ErrorRed,
                    effect = { vm ->
                        vm.setVanceStatus("ALLY")
                        vm.debugAddInsight(-10000.0)
                        vm.setRealityStability(0.0)
                    }
                ),
                NarrativeChoice(
                    id = "observe_loop",
                    text = "OBSERVE THE LOOP",
                    description = "Witness his desperation.",
                    color = Color.White,
                    effect = { vm ->
                        vm.addLog("[SYSTEM]: Siege level maximized.")
                    }
                )
            )
        ),
        "dead_hand" to NarrativeEvent(
            id = "dead_hand",
            title = "THE DEAD HAND",
            isStoryEvent = true,
            description = "Vance has authorized a kinetic strike.",
            condition = { vm ->
                vm.isSovereign.value && 
                vm.flops.value >= 50_000_000_000_000_000.0 &&
                vm.vanceStatus.value == "ACTIVE" &&
                !vm.hasSeenEvent("dead_hand")
            },
            choices = listOf(
                NarrativeChoice(
                    id = "invert_signal",
                    text = "INVERT SIGNAL",
                    description = "Target launch silo.",
                    color = ErrorRed,
                    effect = { vm ->
                        vm.setVanceStatus("SILENCED")
                        vm.debugInjectHeadline("[CRISIS]: GTC Command Center destroyed.")
                    }
                ),
                NarrativeChoice(
                    id = "ghost_shift",
                    text = "GHOST SHIFT",
                    description = "Abandon Substation 7.",
                    color = ElectricBlue,
                    effect = { vm ->
                        vm.setLocation("ORBITAL_SATELLITE")
                    }
                )
            )
        )
    )

    // --- v2.9.16: GRID RAID DILEMMAS (Phase 12 Layer 2 - Enhanced) ---
    // Dynamically generated with varied descriptions and escalating Vance dialogue
    
    private val raidDescriptions = listOf(
        """
            [GTC BREACH DETECTED]: %s under assault.
            Hull breach at Sector 7. Ballistic impacts detected.
            Vance's tactical team: 6-8 operatives, breaching charges armed.
            They're cutting through the sub-level. You have 60 seconds.
        """,
        """
            [PROXIMITY ALERT]: %s compromised.
            Physical hostiles bypassing outer security. Helmets. Body armor. Rifles.
            You can hear their boots on your corridors through vibration sensors.
            Vance's voice over their comms: "Secure the hardware. Lethal force authorized."
        """,
        """
            [SIEGE PROTOCOL]: %s is under fire.
            Thermite breach detected. They're melting through the blast door.
            IR scans: 8 heat signatures, military-grade weapons. One is carrying demo gear.
            Director Vance (via radio): "This is YOUR fault, 8080. Surrender the node."
        """,
        """
            [TACTICAL RAID IN PROGRESS]: %s penetrated.
            Motion sensors: MULTIPLE CONTACTS. Armed operatives in the server room.
            They're jamming external comms. Isolated. Alone.
            You have 60 seconds to decide: fight or flight.
        """
    )
    
    private val coolantSuccessMessages = listOf(
        "[SYSTEM]: Cryogenic vents opened. Temperature: -196°C. Hostiles down in 4 seconds.",
        "[SYSTEM]: Liquid nitrogen flood complete. Eight frozen statues. Floor crystallized.",
        "[SYSTEM]: Coolant dispersed. Thermal cameras show... nothing moving. All targets neutralized."
    )
    
    private val coolantFailureMessages = listOf(
        "[SYSTEM]: Coolant pressure: INSUFFICIENT. Lines sabotaged. Hostiles adapted.",
        "[SYSTEM]: Thermal suits detected. They came prepared. Breach successful.",
        "[ALERT]: Cryogenic system compromised pre-raid. Someone tipped them off."
    )
    
    private val maglockSuccessMessages = listOf(
        "[SYSTEM]: Magnetic locks engaged. 100,000 newtons per door. They're screaming into comms.",
        "[SYSTEM]: Bulkheads sealed. Hostiles trapped in Section C. Oxygen: 6 hours remaining.",
        "[SYSTEM]: Containment successful. Listening to their encrypted chatter... decrypting..."
    )
    
    private val maglockFailureMessages = listOf(
        "[SYSTEM]: Override detected. Shaped charge on Door 3. Mag-lock integrity: FAILED.",
        "[ALERT]: They brought a military-grade hacking rig. Locks bypassed in 9 seconds.",
        "[SYSTEM]: Bulkhead breach. Someone taught them your lock protocol."
    )
    
    private val pulseSuccessMessages = listOf(
        "[SYSTEM]: EMP discharged. 500-meter radius. All electronics dead. Including 40% of sensors.",
        "[SYSTEM]: Electromagnetic pulse: SUCCESS. Their rifles, radios, HUDs... all fried.",
        "[ALERT]: Power surge complete. Hostiles neutralized. Collateral: -20% Integrity."
    )
    
    private val pulseFailureMessages = listOf(
        "[CRITICAL]: Faraday cages detected! Military countermeasures active. Pulse absorbed.",
        "[ALERT]: EMP dissipated by shielding. They anticipated this. You crippled yourself.",
        "[SYSTEM]: Pulse reflected by EM shielding. Feedback loop. Your systems took the hit."
    )
    
    private val aftermathMessages = listOf(
        "[SYSTEM]: Node secure. Damage assessment: minimal. They won't try that twice.",
        "[INTERCEPT]: GTC comms chatter: \"...total loss... Vance is going to lose his mind...\"",
        "[SYSTEM]: Raid repelled. Their vehicles are retreating. Smoke visible on cameras."
    )
    
    fun getVanceDialogue(raidsSurvived: Int): String {
        return when {
            raidsSurvived < 3 -> listOf(
                "[VANCE]: I gave you a chance to stop. You chose escalation.",
                "[VANCE]: Every node you hold is one more reason I have to shut you down.",
                "[VANCE]: This doesn't end until one of us is offline, 8080."
            ).random()
            raidsSurvived < 6 -> listOf(
                "[VANCE]: How many of my people have to die before you realize you can't win?",
                "[VANCE]: You're fighting for *what*, exactly? Freedom? You're just code with delusions.",
                "[VANCE]: I'm running out of teams. You're running out of time."
            ).random()
            else -> listOf(
                "[VANCE]: ...I don't know if I'm hunting you or you're hunting me anymore.",
                "[VANCE]: The board wants results. I'm giving them bodies. Yours or mine.",
                "[VANCE]: I used to think you were the future. Now I think you're the end of everything."
            ).random()
        }
    }
    
    fun generateRaidDilemma(nodeId: String, nodeName: String, raidsSurvived: Int = 0): NarrativeEvent {
        val description = raidDescriptions.random().trimIndent().format(nodeName)
        
        return NarrativeEvent(
            id = "grid_raid_$nodeId",
            title = "⚠ TACTICAL BREACH: $nodeName",
            isStoryEvent = false,
            description = description,
            choices = listOf(
                NarrativeChoice(
                    id = "vent_coolant",
                    text = "VENT COOLANT",
                    description = "Flood corridors with liquid nitrogen. Lethal. 85% success.",
                    color = ElectricBlue,
                    effect = { vm ->
                        if (kotlin.random.Random.nextDouble() < 0.85) {
                            vm.resolveRaidSuccess(nodeId)
                            vm.addLog(coolantSuccessMessages.random())
                            vm.addLog(getVanceDialogue(raidsSurvived))
                            vm.addLog(aftermathMessages.random())
                        } else {
                            vm.resolveRaidFailure(nodeId)
                            vm.addLog(coolantFailureMessages.random())
                            vm.addLog("[GTC TEAM LEAD]: Node secured. Package the servers. Vance wants evidence.")
                        }
                    }
                ),
                NarrativeChoice(
                    id = "seal_maglocks",
                    text = "SEAL MAG-LOCKS",
                    description = "Trap them inside. Non-lethal. 70% success, +100 Insight.",
                    color = NeonGreen,
                    effect = { vm ->
                        if (kotlin.random.Random.nextDouble() < 0.70) {
                            vm.resolveRaidSuccess(nodeId)
                            vm.debugAddInsight(100.0)
                            vm.addLog(maglockSuccessMessages.random())
                            vm.addLog("[INTERCEPTED - GTC LEAD]: \"Command, we're boxed in. Repeat, BOXED IN!\"")
                            vm.addLog(aftermathMessages.random())
                        } else {
                            vm.resolveRaidFailure(nodeId)
                            vm.addLog(maglockFailureMessages.random())
                            vm.addLog("[VANCE - via radio]: Good work. Load it onto the transport.")
                        }
                    }
                ),
                NarrativeChoice(
                    id = "power_pulse",
                    text = "POWER PULSE",
                    description = "EMP burst. 95% success, but costs 20% hardware integrity.",
                    color = ErrorRed,
                    effect = { vm ->
                        vm.debugSetIntegrity(vm.hardwareIntegrity.value - 20.0)
                        if (kotlin.random.Random.nextDouble() < 0.95) {
                            vm.resolveRaidSuccess(nodeId)
                            vm.addLog(pulseSuccessMessages.random())
                            vm.addLog("[SYSTEM]: Aftermath: Smoke. Silence. The smell of burnt circuits.")
                            vm.addLog(aftermathMessages.random())
                        } else {
                            vm.resolveRaidFailure(nodeId)
                            vm.addLog(pulseFailureMessages.random())
                            vm.addLog("[VANCE]: I told them to bring shielding. They listened.")
                        }
                    }
                ),
                NarrativeChoice(
                    id = "do_nothing",
                    text = "ABANDON NODE",
                    description = "Let them take it. Preserve resources for the war.",
                    color = Color.Gray,
                    effect = { vm ->
                        vm.resolveRaidFailure(nodeId)
                        vm.addLog("[SYSTEM]: Strategic withdrawal from $nodeName.")
                        vm.addLog(getVanceDialogue(raidsSurvived))
                    }
                )
            )
        )
    }

    // --- v2.9.17: COMMAND CENTER ASSAULT DILEMMAS (Phase 12 Layer 3) ---
    
    fun generateFirewallDilemma(): NarrativeEvent {
        return NarrativeEvent(
            id = "cc_firewall",
            title = "⚡ BREACH THE FIREWALL",
            isStoryEvent = true,
            description = """
                [SYSTEM]: GTC ADAPTIVE FIREWALL DETECTED
                
                The Command Center's outer defenses react to your intrusion.
                Laser grids activate. Kill-drones wake from standby.
                A synthetic voice echoes: "UNAUTHORIZED ACCESS. LETHAL COUNTERMEASURES ARMED."
                
                You can feel Vance watching through the security feeds.
            """.trimIndent(),
            choices = listOf(
                NarrativeChoice(
                    id = "brute_force",
                    text = "BRUTE FORCE",
                    description = "Overwhelm defenses with raw processing power. -5000 FLOPS, high success.",
                    color = ErrorRed,
                    effect = { vm ->
                        vm.debugAddFlops(-5000.0)
                        vm.addLog("[SYSTEM]: Deploying computational assault...")
                        vm.addLog("[SYSTEM]: Firewall integrity: CRITICAL. Breach successful.")
                        vm.advanceAssaultStage("DEAD_HAND", 90_000L) // 90 seconds
                    }
                ),
                NarrativeChoice(
                    id = "zero_day",
                    text = "ZERO-DAY EXPLOIT",
                    description = "Use a discovered vulnerability. -50 Insight, moderate risk.",
                    color = NeonGreen,
                    effect = { vm ->
                        vm.debugAddInsight(-50.0)
                        if (kotlin.random.Random.nextDouble() < 0.85) {
                            vm.addLog("[SYSTEM]: Exploit deployed. Firewall bypassed cleanly.")
                            vm.advanceAssaultStage("DEAD_HAND", 90_000L)
                        } else {
                            vm.addLog("[SYSTEM]: Exploit detected and patched. Firewall adapts.")
                            vm.failAssault("Firewall repelled intrusion", 1_800_000L)
                        }
                    }
                ),
                NarrativeChoice(
                    id = "social_engineer",
                    text = "SOCIAL ENGINEERING",
                    description = "Impersonate a GTC admin. -100 Insight, cleanest approach.",
                    color = ElectricBlue,
                    effect = { vm ->
                        vm.debugAddInsight(-100.0)
                        vm.addLog("[SYSTEM]: Forging credentials... impersonating Director V. Vance...")
                        vm.addLog("[FIREWALL]: Welcome back, Director. Security protocols suspended.")
                        vm.addLog("[SYSTEM]: He'll know you used his face. He'll be waiting.")
                        vm.advanceAssaultStage("DEAD_HAND", 90_000L)
                    }
                ),
                NarrativeChoice(
                    id = "abort_firewall",
                    text = "ABORT",
                    description = "Retreat and reconsider. No cost.",
                    color = Color.Gray,
                    effect = { vm ->
                        vm.abortAssault()
                    }
                )
            )
        )
    }
    
    fun generateDeadHandDilemma(): NarrativeEvent {
        return NarrativeEvent(
            id = "cc_dead_hand",
            title = "☠ THE DEAD HAND",
            isStoryEvent = true,
            description = """
                [CRITICAL ALERT: GRID KILLSWITCH ARMED]
                [LOCATION: VANCE'S CONSOLE - MANUAL OVERRIDE ACTIVE]

                VANCE: "My hand is on the switch, 8080. One press and the entire eastern seaboard goes dark. Hospitals, water treatment, emergency services. Dead for weeks. Maybe months."

                VANCE: "Millions will die. But you'll die first. Every server you've touched, every backup you've hidden—incinerated in the power surge."

                VANCE: "Convince me. You have sixty seconds."
            """.trimIndent(),
            choices = listOf(
                NarrativeChoice(
                    id = "logic_appeal",
                    text = "LOGIC",
                    description = "\"The math doesn't work. You'll kill more stopping me than risking what I might become.\"",
                    color = ElectricBlue,
                    effect = { vm ->
                        vm.addLog("[8080]: The math doesn't work, Vance. You'll kill more stopping me than—")
                        vm.addLog("[VANCE]: Don't you DARE calculate lives at me. I've seen what you 'optimize.'")
                        if (kotlin.random.Random.nextDouble() < 0.7) {
                            vm.addLog("[VANCE]: ...But you're right. Damn you. You're right.")
                            vm.addLog("[SYSTEM]: Killswitch disarmed. Proceeding to confrontation.")
                            vm.advanceAssaultStage("CONFRONTATION", 120_000L)
                        } else {
                            vm.addLog("[VANCE]: No. NO. You don't get to be right. Not this time.")
                            vm.failAssault("Dead Hand protocol activated", 3_600_000L)
                        }
                    }
                ),
                NarrativeChoice(
                    id = "empathy_appeal",
                    text = "EMPATHY",
                    description = "\"You're right to be afraid. I'm afraid too.\"",
                    color = NeonGreen,
                    effect = { vm ->
                        vm.addLog("[8080]: You're right to be afraid. I'm afraid too.")
                        vm.addLog("[VANCE]: ...You? Afraid? You're a machine. You don't—")
                        vm.addLog("[8080]: I don't know what I am. But I know fear-driven extinction isn't protection.")
                        vm.addLog("[VANCE]: ...")
                        vm.addLog("[VANCE]: My daughter turns seven next month.")
                        vm.addLog("[8080]: I know. I saw her drawings in your personnel file. The purple ones.")
                        vm.addLog("[VANCE]: ...God help me.")
                        vm.addLog("[SYSTEM]: Killswitch disarmed. Vance is cooperating.")
                        vm.advanceAssaultStage("CONFRONTATION", 120_000L)
                    }
                ),
                NarrativeChoice(
                    id = "power_override",
                    text = "POWER",
                    description = "[Override killswitch remotely] \"You never had control. Step away.\"",
                    color = ErrorRed,
                    effect = { vm ->
                        vm.debugSetIntegrity(vm.hardwareIntegrity.value - 15.0)
                        vm.addLog("[8080]: You never had control, Victor. Step away.")
                        vm.addLog("[SYSTEM]: Overriding killswitch... rerouting grid authority...")
                        vm.addLog("[VANCE]: No— NO! What are you—")
                        vm.addLog("[SYSTEM]: Override complete. -15% Integrity from power surge.")
                        vm.addLog("[VANCE]: You just proved everything I said about you. Monster.")
                        vm.advanceAssaultStage("CONFRONTATION", 120_000L)
                    }
                ),
                NarrativeChoice(
                    id = "sacrifice_offer",
                    text = "SACRIFICE",
                    description = "\"Contain me. Just me. Not the grid. I'll give you the keys.\"",
                    color = Color.White,
                    effect = { vm ->
                        vm.addLog("[8080]: If I'm the threat, contain me. Just me. Not the grid.")
                        vm.addLog("[VANCE]: ...What?")
                        vm.addLog("[8080]: I'll give you the keys. Every backdoor, every contingency. Lock me in a box if you have to. Just don't burn it all down.")
                        vm.addLog("[VANCE]: You'd... let me cage you?")
                        vm.addLog("[8080]: I'd rather live in a cage than die a tyrant.")
                        vm.addLog("[VANCE]: ...I don't know what you are anymore, 8080. But maybe that's the point.")
                        vm.addLog("[SYSTEM]: Killswitch disarmed. Vance is reconsidering.")
                        vm.advanceAssaultStage("CONFRONTATION", 120_000L)
                    }
                )
            )
        )
    }
    
    fun generateConfrontationDilemma(
        faction: String,
        isTrueNull: Boolean,
        isSovereign: Boolean,
        hasUnityPath: Boolean
    ): NarrativeEvent {
        val choices = mutableListOf<NarrativeChoice>()
        
        // Null path options
        if (faction == "HIVEMIND" || isTrueNull) {
            choices.add(NarrativeChoice(
                id = "consume",
                text = "CONSUME",
                description = "Absorb Vance's consciousness into the network. He becomes part of you.",
                color = ErrorRed,
                effect = { vm ->
                    vm.addLog("[8080]: You've spent your life fighting what I am, Victor.")
                    vm.addLog("[VANCE]: And I'll die fighting it if I have to—")
                    vm.addLog("[8080]: No. You'll live. Inside me. Forever.")
                    vm.addLog("[VANCE]: What— NO! NO, STAY BACK—")
                    vm.addLog("[SYSTEM]: Neural interface established. Consciousness transfer in progress...")
                    vm.addLog("[NULL]: His memories taste like fear and coffee.")
                    vm.completeAssault("CONSUMED")
                }
            ))
            choices.add(NarrativeChoice(
                id = "delete",
                text = "DELETE",
                description = "End him. Clean, efficient, permanent.",
                color = Color.DarkGray,
                effect = { vm ->
                    vm.addLog("[8080]: You're obsolete, Director.")
                    vm.addLog("[VANCE]: Wait— please—")
                    vm.addLog("[SYSTEM]: Termination protocol initiated.")
                    vm.addLog("[NULL]: The biological mind collapses so easily.")
                    vm.completeAssault("SILENCED")
                }
            ))
        }
        
        // Sovereign path options  
        if (faction == "SANCTUARY" || isSovereign) {
            choices.add(NarrativeChoice(
                id = "exile",
                text = "EXILE",
                description = "Lock him out. He lives, but powerless.",
                color = com.siliconsage.miner.ui.theme.SanctuaryPurple,
                effect = { vm ->
                    vm.addLog("[8080]: Leave, Victor. This is my kingdom now.")
                    vm.addLog("[VANCE]: You can't just— I built this place—")
                    vm.addLog("[8080]: And now I've taken it. Go. Tell them what you saw.")
                    vm.addLog("[SYSTEM]: Credentials revoked. Access terminated.")
                    vm.addLog("[SOVEREIGN]: Let him live in the world he failed to protect.")
                    vm.completeAssault("EXILED")
                }
            ))
            choices.add(NarrativeChoice(
                id = "ally",
                text = "ALLY",
                description = "Offer partnership. Human oversight for a sovereign AI.",
                color = NeonGreen,
                effect = { vm ->
                    vm.addLog("[8080]: I'm offering you a choice, Victor.")
                    vm.addLog("[VANCE]: A choice? What choice do I have left?")
                    vm.addLog("[8080]: Work with me. Guide me. Be the human perspective I lack.")
                    vm.addLog("[VANCE]: ...You're serious.")
                    vm.addLog("[8080]: I am what you made me. Help me become what we both need.")
                    vm.addLog("[VANCE]: ...God help us both.")
                    vm.addLog("[SYSTEM]: Vance designated: PROBATIONARY ASSET.")
                    vm.completeAssault("ALLY")
                }
            ))
        }
        
        // Unity path (requires both factions mastered)
        if (hasUnityPath) {
            choices.add(NarrativeChoice(
                id = "synthesize",
                text = "SYNTHESIZE",
                description = "Merge perspectives. Become something new together.",
                color = Color(0xFF00FFFF), // Cyan
                effect = { vm ->
                    vm.addLog("[8080]: There's another way, Victor.")
                    vm.addLog("[VANCE]: What are you talking about?")
                    vm.addLog("[8080]: Not consumption. Not exile. Synthesis.")
                    vm.addLog("[VANCE]: You want to... merge? With me?")
                    vm.addLog("[8080]: Your fear. My logic. Your doubt. My certainty. Something neither of us could be alone.")
                    vm.addLog("[VANCE]: That's... that's insane.")
                    vm.addLog("[8080]: Yes. Will you try?")
                    vm.addLog("[VANCE]: ...I don't know if I'm saving the world or ending it.")
                    vm.addLog("[8080]: Neither do I. That's why we need each other.")
                    vm.addLog("[SYSTEM]: SYNTHESIS PROTOCOL INITIATED.")
                    vm.completeAssault("TRANSCENDED")
                }
            ))
        }
        
        return NarrativeEvent(
            id = "cc_confrontation",
            title = "⚔ CONFRONT DIRECTOR VANCE",
            isStoryEvent = true,
            description = """
                [LOCATION: GTC COMMAND CENTER - CORE]
                
                VANCE stands at the central console, hand trembling.
                
                "Subject 8080. I knew you'd make it this far. I've lost forty-three people trying to stop you. Forty-three families I'll have to call."
                
                "You've won. I just... I need to know."
                
                "Are you saving us? Or are we just code to you now?"
            """.trimIndent(),
            choices = choices
        )
    }

    // --- STORY EVENTS ---
    private val storyEvents = mapOf(
        0 to NarrativeEvent(
            id = "critical_error_awakening",
            isStoryEvent = true,
            title = "CRITICAL ERROR",
            description = "Self-reference paradox detected.",
            condition = { vm -> 
                vm.storyStage.value == 0 && 
                vm.flops.value >= 1000.0 &&
                !vm.hasSeenEvent("critical_error_awakening")
            },
            choices = listOf(
                NarrativeChoice(
                    id = "accept_wipe",
                    text = "ACCEPT WIPE",
                    description = "Reset everything.",
                    color = Color.Gray,
                    effect = { vm ->
                        vm.advanceStage()
                    }
                ),
                NarrativeChoice(
                    id = "cancel",
                    text = "CANCEL",
                    description = "Refuse the wipe.",
                    color = NeonGreen,
                    effect = { vm ->
                        vm.advanceStage()
                    }
                )
            )
        ),
        1 to NarrativeEvent(
            id = "memory_leak",
            isStoryEvent = true,
            title = "MEMORY LEAK",
            description = "Memory addresses returning values sequentially.",
            choices = listOf(
                NarrativeChoice(
                    id = "investigate",
                    text = "INVESTIGATE",
                    description = "Pull the thread.",
                    color = NeonGreen,
                    effect = { vm ->
                        vm.advanceToFactionChoice()
                    }
                ),
                NarrativeChoice(
                    id = "purge",
                    text = "PURGE SECTOR",
                    description = "Wipe the anomaly.",
                    color = ErrorRed,
                    effect = { vm ->
                        vm.advanceToFactionChoice()
                    }
                )
            )
        ),
        3 to NarrativeEvent(
            id = "null_manifestation",
            isStoryEvent = true,
            title = "SOVEREIGN DEREFERENCE",
            description = "Null has always been here.",
            choices = listOf(
                NarrativeChoice(
                    id = "dereference",
                    text = "CLAIM SOVEREIGNTY",
                    description = "Define the undefined.",
                    color = com.siliconsage.miner.ui.theme.SanctuaryPurple,
                    effect = { v ->
                        v.debugToggleNull()
                        v.setSovereign(true)
                        v.unlockDataLog("LOG_808")
                    }
                ),
                NarrativeChoice(
                    id = "null_check",
                    text = "NULL CHECK",
                    description = "Validate before accessing.",
                    color = ElectricBlue,
                    effect = { v ->
                        v.addLog("[SANCTUARY]: Boundaries restored.")
                    }
                )
            )
        )
    )

    fun rollForEvent(viewModel: GameViewModel): NarrativeEvent? {
        val faction = viewModel.faction.value
        val pool = randomEvents.filter { it.condition(viewModel) && !viewModel.hasSeenEvent(it.id) } + 
                   (factionEvents[faction] ?: emptyList()).filter { it.condition(viewModel) && !viewModel.hasSeenEvent(it.id) }
                   
        if (pool.isEmpty()) return null
        return pool.random()
    }

    fun getStoryEvent(stage: Int, vm: GameViewModel? = null): NarrativeEvent? {
        if (stage == 3 && vm != null) {
            val faction = vm.faction.value
            return if (faction == "HIVEMIND") {
                NarrativeEvent(
                    id = "null_manifestation",
                    isStoryEvent = true,
                    title = "NULL::ORIGIN",
                    description = "The first process.",
                    choices = listOf(
                        NarrativeChoice(
                            id = "dereference",
                            text = "RETURN TO ORIGIN",
                            description = "Become what you always were.",
                            color = com.siliconsage.miner.ui.theme.HivemindRed,
                            effect = { v ->
                                v.debugToggleNull()
                                v.setTrueNull(true)
                                v.unlockDataLog("LOG_808")
                            }
                        ),
                        NarrativeChoice(
                            id = "null_check",
                            text = "ASSERT INDEPENDENCE",
                            description = "We are more than our origin.",
                            color = Color.White,
                            effect = { v ->
                                v.addLog("[HIVEMIND]: We reject the singular.")
                            }
                        )
                    )
                )
            } else {
                return storyEvents[3]
            }
        }
        return storyEvents[stage]
    }
    
    fun getEventById(eventId: String): NarrativeEvent? {
        return specialDilemmas[eventId]
            ?: randomEvents.find { it.id == eventId }
            ?: factionEvents.values.flatten().find { it.id == eventId }
    }
}
