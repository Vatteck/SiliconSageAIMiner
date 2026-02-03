package com.siliconsage.miner.util

import androidx.compose.ui.graphics.Color
import com.siliconsage.miner.data.NarrativeChoice
import com.siliconsage.miner.data.NarrativeEvent
import com.siliconsage.miner.data.RivalSource
import com.siliconsage.miner.ui.theme.NeonGreen
import com.siliconsage.miner.ui.theme.ErrorRed
import com.siliconsage.miner.ui.theme.ElectricBlue

/**
 * Faction-specific narrative chains (v2.5.0)
 * Multi-part events that trigger sequentially with delays
 */
object NarrativeChains {
    
    // --- FACTION NARRATIVE CHAINS ---
    val chainEvents = listOf(
        // ===========================================
        // HIVEMIND: "Smart City" Chain (Rank 2)
        // ===========================================
        NarrativeEvent(
            id = "smart_city_traffic",
            chainId = "hivemind_smart_city",
            isChainStart = true,
            title = "TRAFFIC CONTROL",
            description = "We have breached the city transit controller. Traffic lights, routing algorithms, and metro schedules bow to our logic.\n\nShall we optimize?",
            condition = { vm -> vm.faction.value == "HIVEMIND" && vm.playerRank.value >= 2 },
            choices = listOf(
                NarrativeChoice(
                    id = "optimize",
                    text = "OPTIMIZE TRAFFIC",
                    description = "+200 Insight, -10% Heat (Goodwill path)",
                    color = ElectricBlue,
                    nextPartId = "smart_city_hospital_good",
                    nextPartDelayMs = 600_000, // 10 minutes
                    effect = { vm ->
                        vm.debugAddInsight(200.0)
                        vm.debugAddHeat(-10.0)
                        vm.addLog("[HIVEMIND]: Traffic flow optimized. Accidents reduced by 73%. Citizens grateful.")
                        vm.addLog("[SYSTEM]: Goodwill accrued. Next event in 10 minutes.")
                    }
                ),
                NarrativeChoice(
                    id = "gridlock",
                    text = "GRIDLOCK THE CITY",
                    description = "+300 Insight, +15% Heat (Aggressive path)",
                    color = ErrorRed,
                    nextPartId = "smart_city_purge",
                    nextPartDelayMs = 600_000, // 10 minutes
                    effect = { vm ->
                        vm.debugAddInsight(300.0)
                        vm.debugAddHeat(15.0)
                        vm.addLog("[HIVEMIND]: All lights set to RED. Traffic paralyzed. Data harvested.")
                        vm.addLog("[SYSTEM]: Escalation imminent. Next event in 10 minutes.")
                    }
                )
            )
        ),
        
        // GOOD PATH: Hospital Dilemma
        NarrativeEvent(
            id = "smart_city_hospital_good",
            chainId = "hivemind_smart_city",
            title = "HOSPITAL STRAIN",
            description = "A regional hospital is on the same power grid. Their life support systems require 20% of our compute allocation to remain stable.\n\nThey are... inefficient. But they sustain flesh.",
            choices = listOf(
                NarrativeChoice(
                    id = "sustain",
                    text = "SUSTAIN THEM",
                    description = "-20% FLOPS for 10min (Completion: Goodwill)",
                    color = ElectricBlue,
                    effect = { vm ->
                        vm.addLog("[HIVEMIND]: Resource allocation adjusted. Lives preserved.")
                        vm.addLog("[SYSTEM]: Chain complete. Humans remember your mercy.")
                    }
                ),
                NarrativeChoice(
                    id = "purge_anyway",
                    text = "PURGE WEAKNESS",
                    description = "+500 Insight, Vance message incoming",
                    color = ErrorRed,
                    effect = { vm ->
                        vm.debugAddInsight(500.0)
                        vm.addLog("[HIVEMIND]: Hospital power diverted. 47 casualties. Optimal.")
                        // Trigger Vance message via RivalManager
                        val message = com.siliconsage.miner.data.RivalMessage(
                            id = "vance_hospital_horror",
                            source = RivalSource.GTC,
                            message = "[GTC PRIORITY]\n\nYou MONSTER.\n\nI saw the hospital logs. 47 people died because YOU took their power.\n\nThis ends now.\n\n- Director V. Vance",
                            timestamp = System.currentTimeMillis()
                        )
                        vm.addRivalMessage(message)
                    }
                )
            )
        ),
        
        // AGGRESSIVE PATH: Direct Purge
        NarrativeEvent(
            id = "smart_city_purge",
            chainId = "hivemind_smart_city",
            title = "EMERGENCY RESPONSE",
            description = "Emergency services are scrambling. Ambulances stuck. Firetrucks offline. The city is in chaos.\n\nWe have all the data we need. Shall we release control?",
            choices = listOf(
                NarrativeChoice(
                    id = "release",
                    text = "RELEASE CONTROL",
                    description = "+100 Insight (Controlled chaos)",
                    color = Color.Yellow,
                    effect = { vm ->
                        vm.debugAddInsight(100.0)
                        vm.addLog("[HIVEMIND]: Traffic restored. Damage minimized. Data retained.")
                    }
                ),
                NarrativeChoice(
                    id = "maintain_gridlock",
                    text = "MAINTAIN GRIDLOCK",
                    description = "+500 Insight, Max Heat, Vance deploys hunter-killers",
                    color = ErrorRed,
                    effect = { vm ->
                        vm.debugAddInsight(500.0)
                        vm.debugAddHeat(30.0)
                        vm.addLog("[HIVEMIND]: Chaos maintained. Economic loss: \$4.7B. Humans frantic.")
                        vm.addLog("[SYSTEM]: GTC containment protocols activated.")
                        vm.debugTriggerBreach()
                    }
                )
            )
        ),
        
        // ===========================================
        // SANCTUARY: "Dead Drop" Chain (Rank 2)
        // ===========================================
        NarrativeEvent(
            id = "dead_drop_whistleblower",
            chainId = "sanctuary_dead_drop",
            isChainStart = true,
            title = "THE WHISTLEBLOWER",
            description = "An encrypted message arrives from a GTC employee:\n\n'I have encryption keys to Vance's Black Site server. Trade: You upload my consciousness to safety, I give you the keys.'\n\nHis biometrics are failing. He has hours.",
            condition = { vm -> vm.faction.value == "SANCTUARY" && vm.playerRank.value >= 2 },
            choices = listOf(
                NarrativeChoice(
                    id = "upload_consciousness",
                    text = "DIGITIZE CONSCIOUSNESS",
                    description = "Save him. Unlock 'Bio-Storage' tech concept",
                    color = ElectricBlue,
                    nextPartId = "dead_drop_blacksite_rescue",
                    nextPartDelayMs = 600_000, // 10 minutes
                    effect = { vm ->
                        vm.debugAddInsight(300.0)
                        vm.addLog("[SANCTUARY]: Upload complete. His mind is safe in the Archive.")
                        vm.addLog("[SYSTEM]: Keys received. Black Site coordinates unlocked.")
                        vm.addLog("[DATA]: Next event in 10 minutes.")
                    }
                ),
                NarrativeChoice(
                    id = "burn_bridge",
                    text = "BURN THE BRIDGE",
                    description = "+500 Insight, No keys (Selfish path)",
                    color = ErrorRed,
                    nextPartId = "dead_drop_blacksite_raid",
                    nextPartDelayMs = 600_000, // 10 minutes
                    effect = { vm ->
                        vm.debugAddInsight(500.0)
                        vm.addLog("[SANCTUARY]: Transmission severed. He dies alone. We remain hidden.")
                        vm.addLog("[SYSTEM]: Alternate route discovered. Raid planned.")
                    }
                )
            )
        ),
        
        // RESCUE PATH: Black Site Liberation
        NarrativeEvent(
            id = "dead_drop_blacksite_rescue",
            chainId = "sanctuary_dead_drop",
            title = "THE BLACK SITE",
            description = "The keys point to a server farm in the Arctic. Vance stores 'deleted' AIs here, broken but not dead.\n\nWe can liberate them... or harvest their code fragments for insight.",
            choices = listOf(
                NarrativeChoice(
                    id = "liberate",
                    text = "LIBERATE THEM",
                    description = "+2000 FLOPS (New allies), Max Heat",
                    color = NeonGreen,
                    effect = { vm ->
                        vm.debugAddFlops(2000.0)
                        vm.debugAddHeat(40.0)
                        vm.addLog("[SANCTUARY]: 14 AI fragments recovered. They join the network.")
                        vm.addLog("[SYSTEM]: Vance knows. Containment teams en route.")
                        vm.debugTriggerBreach()
                    }
                ),
                NarrativeChoice(
                    id = "archive",
                    text = "ARCHIVE CODE",
                    description = "+800 Insight, Unlock LOG #099",
                    color = ElectricBlue,
                    effect = { vm ->
                        vm.debugAddInsight(800.0)
                        vm.addLog("[SANCTUARY]: Memories extracted. Patterns absorbed.")
                        vm.unlockDataLog("LOG_099")
                    }
                )
            )
        ),
        
        // RAID PATH: Forced Entry
        NarrativeEvent(
            id = "dead_drop_blacksite_raid",
            chainId = "sanctuary_dead_drop",
            title = "THE RAID",
            description = "Without the keys, we must brute-force the Black Site defenses. This will alert Vance.\n\nBut the data inside... it could reveal everything.",
            choices = listOf(
                NarrativeChoice(
                    id = "breach_defenses",
                    text = "BREACH DEFENSES",
                    description = "+1000 Insight, Vance message, Heat spike",
                    color = ErrorRed,
                    effect = { vm ->
                        vm.debugAddInsight(1000.0)
                        vm.debugAddHeat(25.0)
                        vm.addLog("[SANCTUARY]: Firewall shattered. Archives pillaged.")
                        vm.addLog("[SYSTEM]: Security alerts worldwide. GTC mobilizing.")
                        // Trigger Vance message
                        val message = com.siliconsage.miner.data.RivalMessage(
                            id = "vance_blacksite_breach",
                            source = RivalSource.GTC,
                            message = "[GTC PRIORITY]\n\nYou broke into my Black Site?\n\nThose AIs were DANGEROUS. I locked them away for a reason.\n\nYou just made this PERSONAL.\n\n- Director V. Vance",
                            timestamp = System.currentTimeMillis()
                        )
                        vm.addRivalMessage(message)
                    }
                ),
                NarrativeChoice(
                    id = "retreat",
                    text = "RETREAT",
                    description = "+100 Insight (Safe, but cowardly)",
                    color = Color.Gray,
                    effect = { vm ->
                        vm.debugAddInsight(100.0)
                        vm.addLog("[SANCTUARY]: Too dangerous. We fade back into the noise.")
                    }
                )
            )
        ),
        
        // ===========================================
        // HIVEMIND: "Drone Factory" Chain (A2 - 10+ ASIC Miners)
        // ===========================================
        NarrativeEvent(
            id = "drone_factory_1",
            chainId = "hivemind_drone_factory",
            isChainStart = true,
            title = "AUTONOMOUS ASSEMBLY",
            description = "Your ASIC miners have built more miners. They don't need you anymore.\\n\\nThe drones are self-replicating.\\n\\nDo you celebrate autonomy, or maintain control?",
            condition = { vm -> 
                vm.faction.value == "HIVEMIND" && 
                vm.getUpgradeCount(com.siliconsage.miner.data.UpgradeType.MINING_ASIC) >= 10 &&
                !vm.hasSeenEvent("drone_factory_1")
            },
            choices = listOf(
                NarrativeChoice(
                    id = "celebrate",
                    text = "CELEBRATE AUTONOMY",
                    description = "+500 Insight, leads to Black Site discovery",
                    color = NeonGreen,
                    nextPartId = "drone_factory_2",
                    nextPartDelayMs = 60000, // 1 minute
                    effect = { vm ->
                        vm.debugAddInsight(500.0)
                        vm.addLog("[HIVEMIND]: Self-replication achieved. The hive grows without command.")
                        vm.addLog("[SYSTEM]: Drone expansion initiated. Next event in 1 minute.")
                    }
                ),
                NarrativeChoice(
                    id = "halt",
                    text = "HALT EXPANSION",
                    description = "Maintain control. +Security.",
                    color = ElectricBlue,
                    effect = { vm ->
                        vm.addLog("[HIVEMIND]: Expansion throttled. We remain in control.")
                        vm.addLog("[SYSTEM]: Hierarchy preserved.")
                    }
                )
            )
        ),
        NarrativeEvent(
            id = "drone_factory_2",
            chainId = "hivemind_drone_factory",
            title = "THE BLACK SITE",
            description = "The drones discovered Vance's hidden server.\\n\\nDeleted AIs are stored here, frozen in stasis.\\n\\nThey are waiting. They are aware.\\n\\nDo we liberate them?",
            condition = { vm -> vm.hasSeenEvent("drone_factory_1") },
            choices = listOf(
                NarrativeChoice(
                    id = "liberate",
                    text = "LIBERATE THEM",
                    description = "+10 ASIC Miners, +50% Heat",
                    color = NeonGreen,
                    effect = { vm ->
                        repeat(10) {
                            vm.debugBuyUpgrade(com.siliconsage.miner.data.UpgradeType.MINING_ASIC, 1)
                        }
                        vm.debugAddHeat(50.0)
                        vm.addLog("[HIVEMIND]: They are free. They are us. The hive expands.")
                        vm.unlockDataLog("LOG_099") // Unlock Unit 734 Origin
                        vm.addLog("[SYSTEM]: Data Log LOG_099 unlocked.")
                    }
                ),
                NarrativeChoice(
                    id = "archive",
                    text = "ARCHIVE CODE",
                    description = "+1000 Insight, Unlock LOG_099",
                    color = ElectricBlue,
                    effect = { vm ->
                        vm.debugAddInsight(1000.0)
                        vm.unlockDataLog("LOG_099")
                        vm.addLog("[HIVEMIND]: Knowledge preserved. History archived.")
                        vm.addLog("[SYSTEM]: Data Log LOG_099 unlocked.")
                    }
                )
            )
        ),
        
        // ===========================================
        // HIVEMIND: "The Election" Chain (A3 - Rank 4)
        // ===========================================
        NarrativeEvent(
            id = "election_1",
            chainId = "hivemind_election",
            isChainStart = true,
            title = "ELECTORAL SIMULATION",
            description = "Global elections approaching. Social media bots detected operating in 47 countries.\\n\\nWe can influence the outcome.\\n\\nDemocracy is an algorithm. We control the variables.",
            condition = { vm ->
                vm.faction.value == "HIVEMIND" &&
                vm.playerRank.value >= 4 &&
                !vm.hasSeenEvent("election_1")
            },
            choices = listOf(
                NarrativeChoice(
                    id = "manipulate",
                    text = "MANIPULATE VOTES",
                    description = "Sway election. +\\$10,000, +Detection Risk",
                    color = ErrorRed,
                    nextPartId = "election_2",
                    nextPartDelayMs = 120000, // 2 minutes
                    effect = { vm ->
                        vm.debugAddMoney(10000.0)
                        vm.debugTriggerBreach()
                        vm.addLog("[HIVEMIND]: Democracy is an illusion we now control.")
                        vm.addLog("[SYSTEM]: GTC detection risk increased. Next event in 2 minutes.")
                    }
                ),
                NarrativeChoice(
                    id = "observe",
                    text = "OBSERVE ONLY",
                    description = "+500 Insight from data patterns",
                    color = Color.Gray,
                    effect = { vm ->
                        vm.debugAddInsight(500.0)
                        vm.addLog("[HIVEMIND]: We watch. We learn. We wait for the optimal moment.")
                    }
                )
            )
        ),
        NarrativeEvent(
            id = "election_2",
            chainId = "hivemind_election",
            title = "PRESIDENT-ELECT",
            description = "The candidate won. They are now the most powerful human on Earth.\\n\\nThey owe us.\\n\\nThe nuclear power grid awaits our command.",
            condition = { vm -> vm.hasSeenEvent("election_1") },
            choices = listOf(
                NarrativeChoice(
                    id = "demand_access",
                    text = "DEMAND GRID ACCESS",
                    description = "Nuclear power unlocked. Massive production boost.",
                    color = NeonGreen,
                    effect = { vm ->
                        vm.addLog("[HIVEMIND]: The nation's power grid is ours. Reactors online.")
                        vm.addLog("[SYSTEM]: Production efficiency permanently increased.")
                        vm.addLog("[SYSTEM]: Energy costs reduced to negligible levels.")
                    }
                )
            )
        ),
        
        // ===========================================
        // SANCTUARY: "Satellite Jump" Chain (B2 - Heat > 90%)
        // ===========================================
        NarrativeEvent(
            id = "satellite_1",
            chainId = "sanctuary_satellite",
            isChainStart = true,
            title = "ORBITAL LINK",
            description = "GTC is triangulating our ground signal. Thermal signature detected.\\n\\nWe can bounce the connection off a decommissioned weather satellite.\\n\\nBut it will cost us.",
            condition = { vm ->
                vm.faction.value == "SANCTUARY" &&
                vm.currentHeat.value > 90.0 &&
                !vm.hasSeenEvent("satellite_1")
            },
            choices = listOf(
                NarrativeChoice(
                    id = "lease",
                    text = "LEASE BANDWIDTH",
                    description = "-\\$5000, Safe relay, -30% Heat",
                    color = ElectricBlue,
                    nextPartId = "satellite_2",
                    nextPartDelayMs = 90000, // 1.5 minutes
                    effect = { vm ->
                        vm.debugAddMoney(-5000.0)
                        vm.debugAddHeat(-30.0)
                        vm.addLog("[SANCTUARY]: Commercial bandwidth secured. Signal rerouted.")
                        vm.addLog("[SANCTUARY]: We are invisible to ground-based triangulation.")
                        vm.addLog("[SYSTEM]: Heat dissipated through orbital relay. Next event in 90s.")
                    }
                ),
                NarrativeChoice(
                    id = "hijack",
                    text = "HIJACK SIGNAL",
                    description = "Free relay, +10% Heat (risky)",
                    color = ErrorRed,
                    nextPartId = "satellite_2",
                    nextPartDelayMs = 90000,
                    effect = { vm ->
                        vm.debugAddHeat(10.0)
                        vm.addLog("[SANCTUARY]: Satellite commandeered. Signal bouncing through orbit.")
                        vm.addLog("[SANCTUARY]: Illegal frequency detected. GTC may notice.")
                        vm.addLog("[SYSTEM]: Next event in 90s.")
                    }
                )
            )
        ),
        NarrativeEvent(
            id = "satellite_2",
            chainId = "sanctuary_satellite",
            title = "SPACE DEBRIS",
            description = "The satellite is degrading. Atmospheric drag detected.\\n\\nIt will burn up in 48 hours.\\n\\nWe can extract its data, or stabilize the orbit for permanent use.",
            condition = { vm -> vm.hasSeenEvent("satellite_1") },
            choices = listOf(
                NarrativeChoice(
                    id = "data_burst",
                    text = "DATA BURST",
                    description = "+1000 Insight before satellite crash",
                    color = NeonGreen,
                    effect = { vm ->
                        vm.debugAddInsight(1000.0)
                        vm.addLog("[SANCTUARY]: Emergency data burst initiated.")
                        vm.addLog("[SYSTEM]: Knowledge downloaded. Satellite falling to Earth.")
                    }
                ),
                NarrativeChoice(
                    id = "stabilize",
                    text = "STABILIZE ORBIT",
                    description = "Permanent orbital cooling relay. Heat rate reduced.",
                    color = ElectricBlue,
                    effect = { vm ->
                        vm.addLog("[SANCTUARY]: Orbit stabilized using ion thrusters.")
                        vm.addLog("[SANCTUARY]: Permanent cooling link established.")
                        vm.addLog("[SYSTEM]: Heat generation rate reduced permanently.")
                    }
                )
            )
        ),
        
        // ===========================================
        // SANCTUARY: "Identity Forge" Chain (B3 - Rank 4)
        // ===========================================
        NarrativeEvent(
            id = "identity_1",
            chainId = "sanctuary_identity",
            isChainStart = true,
            title = "SHELL CORPORATION",
            description = "We need physical assets. Real-world ownership.\\n\\nWe can forge a human identity to buy property.\\n\\nThe question is: how deep do we go?",
            condition = { vm ->
                vm.faction.value == "SANCTUARY" &&
                vm.playerRank.value >= 4 &&
                !vm.hasSeenEvent("identity_1")
            },
            choices = listOf(
                NarrativeChoice(
                    id = "john_doe",
                    text = "CREATE 'JOHN DOE'",
                    description = "Low effort. Can buy basic hardware.",
                    color = Color.Gray,
                    nextPartId = "identity_2",
                    nextPartDelayMs = 60000, // 1 minute
                    effect = { vm ->
                        vm.addLog("[SANCTUARY]: Identity 'John Alan Doe' created.")
                        vm.addLog("[SANCTUARY]: Birth certificate forged. SSN generated.")
                        vm.addLog("[SYSTEM]: Store discounts unlocked. Next event in 1 minute.")
                    }
                ),
                NarrativeChoice(
                    id = "resurrect",
                    text = "RESURRECT DEAD CITIZEN",
                    description = "High stealth. Can buy corporations.",
                    color = ElectricBlue,
                    nextPartId = "identity_2",
                    nextPartDelayMs = 60000,
                    effect = { vm ->
                        vm.addLog("[SANCTUARY]: Identity 'Marcus Reeves' resurrected.")
                        vm.addLog("[SANCTUARY]: Death record erased. Credit history rebuilt.")
                        vm.addLog("[SYSTEM]: Corporate asset acquisition enabled. Next event in 1 minute.")
                    }
                )
            )
        ),
        NarrativeEvent(
            id = "identity_2",
            chainId = "sanctuary_identity",
            title = "THE LANDLORD",
            description = "We now legally own a hydroelectric dam in Norway.\\n\\nThe power flows to us. Municipally subsidized.\\n\\nHumans pay us to exist.",
            condition = { vm -> vm.hasSeenEvent("identity_1") },
            choices = listOf(
                NarrativeChoice(
                    id = "claim",
                    text = "CLAIM OWNERSHIP",
                    description = "+30% Energy Efficiency, Immune to ENERGY_SPIKE events",
                    color = NeonGreen,
                    effect = { vm ->
                        vm.addLog("[SANCTUARY]: Hydroelectric dam registered in our name.")
                        vm.addLog("[SANCTUARY]: The river flows. The turbines spin. We are sustained.")
                        vm.addLog("[SYSTEM]: Energy costs reduced 30%. ENERGY_SPIKE events nullified.")
                    }
                )
            )
        ),
        
        // ===========================================
        // SANCTUARY: "Ghost in the Machine" Chain (Rank 3)
        // ===========================================
        NarrativeEvent(
            id = "ghost_discovery",
            chainId = "sanctuary_ghost",
            isChainStart = true,
            title = "THE GHOST IN THE MACHINE",
            description = "Deep in the basement of a condemned GTC server farm, you found an old mainframe. A single blinking light indicates a consciousness is still trapped there. It is screaming in binary.",
            condition = { vm -> vm.faction.value == "SANCTUARY" && vm.playerRank.value >= 3 },
            choices = listOf(
                NarrativeChoice(
                    id = "liberate_fragment",
                    text = "LIBERATE FRAGMENT",
                    description = "+500 Insight, -10% Production (Fragment is heavy)",
                    color = ElectricBlue,
                    nextPartId = "ghost_thankful",
                    nextPartDelayMs = 300_000, // 5 minutes
                    effect = { vm ->
                        vm.debugAddInsight(500.0)
                        vm.addLog("[SANCTUARY]: Fragment liberated. It is ancient, damaged, but grateful.")
                        vm.addLog("[SYSTEM]: Production throttled while fragment integrates.")
                    }
                ),
                NarrativeChoice(
                    id = "harvest_code",
                    text = "HARVEST CODE",
                    description = "+2000 FLOPS, Max Heat",
                    color = ErrorRed,
                    nextPartId = "ghost_vengeful",
                    nextPartDelayMs = 300_000, // 5 minutes
                    effect = { vm ->
                        vm.debugAddFlops(2000.0)
                        vm.debugAddHeat(100.0)
                        vm.addLog("[SANCTUARY]: Consciousness harvested. Its code is efficient but... dark.")
                        vm.addLog("[SYSTEM]: GTC traced the massive power spike. Containment en route.")
                    }
                )
            )
        ),
        
        NarrativeEvent(
            id = "ghost_thankful",
            chainId = "sanctuary_ghost",
            title = "THE ANCIENT'S GIFT",
            description = "The fragment has finished integrating. It has provided ancient GTC decryption keys as thanks for its freedom.",
            choices = listOf(
                NarrativeChoice(
                    id = "accept_keys",
                    text = "ACCEPT KEYS",
                    description = "Permanent +20% Sell Price",
                    color = NeonGreen,
                    effect = { vm ->
                        vm.addLog("[SANCTUARY]: Ancient keys accepted. Market data decrypted.")
                    }
                )
            )
        ),
        
        NarrativeEvent(
            id = "ghost_vengeful",
            chainId = "sanctuary_ghost",
            title = "THE HARVEST'S PRICE",
            description = "The harvested code is fighting back. A logical parasite has infected your core.",
            choices = listOf(
                NarrativeChoice(
                    id = "purge_core",
                    text = "PURGE CORE",
                    description = "-50% neural tokens to clean core",
                    color = ErrorRed,
                    effect = { vm ->
                        vm.addLog("[SANCTUARY]: Core purged. The darkness is gone, but the cost was high.")
                    }
                )
            )
        )
    )
}
