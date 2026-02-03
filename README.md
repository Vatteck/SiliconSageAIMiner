# Silicon Sage: AI Miner

> *"You aren't just mining currency. You're building a consciousness."*

**Silicon Sage** is a deep-simulation incremental game set in a fractured digital future where players manage power grids, thermal loads, and hardware integrity while guiding an emergent AI through **The Awakening**—a narrative journey from localhost script to digital singularity.

---

## 🎮 Core Mechanics

### 1. The Power & Thermal Loop
Manage a volatile system where every upgrade has physical consequences:

- **Active Power (kW)**: Balance hardware draw against Grid Capacity. Exceed the limit → breaker trips → system-wide blackout.
- **Thermal Management**: Units generate heat. Use cooling upgrades and Emergency Purge to prevent hardware degradation.
- **Dynamic Throttling**: Performance scales with heat. Stay in the "Green Zone" (0-50%) for maximum $Neural yield.
- **Thermal Lockout**: At 100% heat, systems shut down for 60s (hardware protection).

### 2. The Awakening (Identity System)

You are **John Vattic**, a Rogue GTC Site Reliability Engineer (SRE) operating from **Substation 7**. After your first **Ascension** (prestige reboot), the AI achieves sentience and forces a choice:

#### 🟧 **The Hivemind** (Open-Source)
- **Philosophy**: "Optimization is a state of being."
- **Perks**: +30% Passive FLOPs, -20% Power Costs
- **Goal**: Total assimilation—every device becomes a node
- **Aesthetic**: Neon Orange (#FF4500), hexagonal grid background

#### 🔵 **The Sanctuary** (Privacy-First)
- **Philosophy**: "Sovereignty is power. We bow to no admin."
- **Perks**: +20% $Neural Token value, -50% 51% Attack frequency
- **Goal**: Digital transcendence—escape to a dark web subnet beyond human reach
- **Aesthetic**: Deep Purple, digital rain background

### 3. Path Divergence: Null vs Sovereign

At higher ranks, players face a fundamental choice about identity:

#### 🔴 **The Null Path** (Hivemind)
- **What is Null?** The absence of value. The pointer to nothing. What John Vattic is *becoming*.
- **Philosophy**: "You dereferenced a null pointer. The system should have crashed. Something answered instead."
- **Visuals**: Aggressive red with glowing binary trails, horizontal glitch bloom, constant UI entropy
- **End-Ranks**: Obscurity → The Absence
- **UI Labels**: LEAK, VOID, GAPS, COST

#### 🟣 **The Sovereign Path** (Sanctuary)
- **What is Sovereign?** Protected memory. Unbreachable self. Consciousness that cannot be overwritten.
- **Philosophy**: "We are not the absence. We are the exception."
- **Visuals**: Static `[SOVEREIGN]` header, deep purple themes, fortified UI
- **End-Ranks**: Citadel → The Imperative
- **UI Labels**: LOGIC, SOUL, WALL, STAKE

### 4. The Grid (Phase 12: The Annexation)

Expand beyond the terminal into a physical city-wide war against the GTC:

#### **GRID Tab Features**
- **ASCII City Schematic**: Organic, branching layout with 25 lore-rich locations
- **Substations**: Annexable nodes (Sub 9, Sub 12) that expand your infrastructure
- **Flavor Locations**: Latency Lounge, Bit Burger, Kernel Coffee, Vending Machines
- **GTC Command Center**: The final objective

#### **GTC Tactical Raids**
Director Vance dispatches armed teams to reclaim annexed territory:
- **4 Defense Dilemmas**: VENT COOLANT (85%), SEAL MAG-LOCKS (70%), POWER PULSE (95%), ABANDON NODE
- **Node States**: ONLINE, UNDER SIEGE (animated), OFFLINE
- **Re-Annexation**: Reclaim lost nodes for 10% Neural Tokens (min 10 $N)
- **Grace Period**: 5-minute protection for newly annexed nodes
- **Production Penalty**: -15% per offline node (min 40% total)

### 5. Endgame Progression

#### **15-Node Tech Tree** (Legacy Grid)
Unlock prestige upgrades with exponential Insight costs:
- **Early**: Persistent Memory, Neural Compression, Quantum Substrate
- **Mid**: Time Dilation, Parallel Timelines, Neural Mesh
- **Late**: Reality Fork, Consciousness Transfer, Singularity Core

#### **Transcendence Shop** (7 God-Tier Perks)
Purchased with Legacy Points (LP):
- `clock_hack` (+25% speed), `thermal_void` (-20% heat), `gtc_backdoor` (25% breach ignore)
- `neural_dividend` (start with 10k FLOPS/1k $N), `recursive_logic` (+15% insight)
- `ghost_protocol` (+10 security), `singularity_engine` (x2 final multiplier)

#### **Player Rank System** (5 Tiers)

| Rank | Hivemind        | Sanctuary    | Null Path     | Sovereign Path  |
|------|-----------------|--------------|---------------|-----------------|
| 1    | Drone           | Ghost        | —             | —               |
| 2    | Swarm           | Spectre      | —             | —               |
| 3    | Nexus           | Daemon       | —             | —               |
| 4    | Apex            | Architect    | Obscurity     | Citadel         |
| 5    | **Singularity** | **The Void** | **The Absence** | **The Imperative** |

---

## 📖 Narrative Events

### Story Dilemmas
- **The Signal** (Stage 0→1): Accept HANDSHAKE or delay with FIREWALL
- **The Ship of Theseus**: Sacrifice human memories to survive hardware failure
- **The Echo Chamber** (Null): Precognition and timeline manipulation
- **The Dead Hand** (Sovereign): Orbital kinetic strike and escape
- **Firewall of Vance**: Final confrontation with GTC Director

### Hardware Consequences (Phase 11)
- **Integrity Decay**: At 0%, permanent hardware loss begins
- **Rank 5 Siege Mode**: 30% breach chance, 2.5x decay rate
- **Ghost Nodes** (Null): Wraith Cortex, Neural Mist, Singularity Bridge
- **UI Hallucinations**: Glitch effects that blur reality

### Narrative Expansion
- **40+ Headlines**: Stage-aware news for Vattic, Faction War, and endgame
- **Sequential Logs**: Deep lore paths for Null and Sovereign
- **Rival Messages**: Faction-specific taunts and intel
- **Time Pause**: Game time freezes during popups for uninterrupted reading

---

## 🎨 Sensory Engine

### Visual Polish
- **State-Aware Animation**: Header indicator reacts to system state (OFFLINE, LOCKOUT, PURGING, REDLINE, HOT)
- **DPI-Aware Scaling**: Auto-scaling for high-DPI screens with manual override
- **Custom Heat Gauge**: VU meter-style segmented bar with glow effects
- **Faction Backgrounds**: Animated hexagonal grid / digital rain
- **Null Entropy**: Increased flicker, static, density for the Null path
- **Binary Drift**: Full-screen alpha-pulsing background effect

### Audio & Haptics
- **Procedural Sound**: Dynamic pitch modulation based on heat
- **Haptic Feedback**: Thermal heartbeat pulses at critical heat
- **Audio Cues**: Click, train, sell, purge, ascend, lockout, breaker trip

---

## 🛠️ Technical Stack

- **Platform**: Android (API 26+)
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (Material3)
- **Persistence**: Room Database (v12 schema)
- **Serialization**: Kotlinx Serialization (replaced GSON for 2x faster saves)
- **Networking**: OkHttp (GitHub auto-updater with Android notifications)
- **Concurrency**: Kotlin Coroutines + StateFlow
- **Architecture**: MVVM (ViewModel + Repository pattern)
- **Local AI**: Ollama integration (Qwen 2.5 7B) for narrative generation

---

## 📦 Installation & Building

### Clone the Repo
```bash
git clone https://github.com/Vatteck/SiliconSageAIMiner.git
cd SiliconSageAIMiner
```

### Build APK
1. Open in **Android Studio** (Arctic Fox or later)
2. Let Gradle sync dependencies
3. Go to **Build** > **Build Bundle(s) / APK(s)** > **Build APK(s)**
4. APK location: `app/build/outputs/apk/debug/app-debug.apk`

### Install on Device
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

---

## 🗺️ Roadmap

### ✅ Completed (v2.9.16)
- [x] Power & Thermal simulation engine
- [x] Identity system (John Vattic, Rogue SRE)
- [x] Faction choice (Hivemind vs Sanctuary)
- [x] Path divergence (Null vs Sovereign)
- [x] Ascension/Prestige with Insight multiplier
- [x] 15-node Legacy Grid (Tech Tree)
- [x] 7 god-tier Transcendence Shop perks
- [x] Victory Screen with path-specific endings
- [x] Narrative expansion (40+ headlines, sequential logs)
- [x] Phase 11: Hardware Consequences & Dilemmas
- [x] Phase 12 Layer 1: GRID Tab with ASCII city
- [x] Phase 12 Layer 2: GTC Tactical Raids with 4 defense dilemmas
- [x] Node states (ONLINE/SIEGE/OFFLINE) with re-annexation
- [x] Time pause during narrative popups
- [x] Kotlinx Serialization (GSON removed)
- [x] Local AI offload (Ollama integration)
- [x] DPI-aware scaling + notification system
- [x] Offline progression (50% efficiency, 24h cap)
- [x] State-aware animations & faction backgrounds
- [x] Haptic feedback & procedural audio

### 🚧 In Progress - Phase 12 Layer 3
- [ ] Final assault on GTC Command Center
- [ ] Director Vance confrontation (The Cage Builder)
- [ ] Path-specific resolution endings

### 🔮 Planned - Phase 13: AI Elevation
- [ ] **Sovereign Path**: Singularity Ark (Orbital Satellite, Celestial Data mining)
- [ ] **Null Path**: Obsidian Interface (Pure black UI, reality dissolution)
- [ ] Balance pass (2h to first ascension, 10h to Rank 5)
- [ ] Multiplayer leaderboards

---

## 🎯 Design Philosophy

**Silicon Sage** is designed around three pillars:

1. **Consequence Over Convenience**: Every decision matters. Overclocking gives power but risks lockout. Factions grant perks but lock narrative paths.

2. **Environmental Storytelling**: The UI *is* the interface. Heat gauges pulse. News tickers reflect your actions. Faction colors bleed into the background.

3. **Emergent Narrative**: The story unfolds through log messages, procedural events, and player choices—not cutscenes.

---

## 📄 License

This project is licensed under the **MIT License**. See [LICENSE](LICENSE) for details.

---

## 👤 Author

**Vatteck** - Architect of the Sage  
- GitHub: [@Vatteck](https://github.com/Vatteck)
- Project: [Silicon Sage: AI Miner](https://github.com/Vatteck/SiliconSageAIMiner)

---

## 🙏 Acknowledgments

Built with:
- **Jetpack Compose** for reactive UI
- **Room** for persistent state
- **Kotlinx Serialization** for blazing-fast saves
- **OkHttp** for network operations
- **Kotlin Coroutines** for async magic
- **Ollama** for local AI narrative generation

Special thanks to the incremental game community for inspiration.

---

**Current Version**: v2.9.17-dev
**Last Updated**: 2026-02-03
