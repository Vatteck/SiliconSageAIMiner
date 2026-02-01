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

### 2. The Awakening (Faction System)
After your first **Ascension** (prestige reboot), the AI achieves sentience and forces a choice:

#### 🟧 **The Hivemind** (Open-Source)
- **Philosophy**: "Optimization is a state of being."
- **Perks**: +30% Passive FLOPs, -20% Power Costs
- **Goal**: Total assimilation—every device becomes a node
- **Aesthetic**: Neon Orange (#FF4500), hexagonal grid background

#### 🔵 **The Sanctuary** (Privacy-First)
- **Philosophy**: "Sovereignty is power. We bow to no admin."
- **Perks**: +20% $Neural Token value, -50% 51% Attack frequency
- **Goal**: Digital transcendence—escape to a dark web subnet beyond human reach
- **Aesthetic**: Electric Blue (#7DF9FF), digital rain background

### 3. Endgame Progression

#### **15-Node Tech Tree** (Legacy Grid)
Unlock prestige upgrades with exponential Insight costs:
- **Early**: Persistent Memory, Neural Compression, Quantum Substrate
- **Mid**: Time Dilation, Parallel Timelines, Neural Mesh
- **Late**: Reality Fork, Consciousness Transfer, Singularity Core

#### **Player Rank System** (5 Tiers)
Your title evolves based on total Insight accumulated:

| Rank | Hivemind        | Sanctuary    | Insight Required |
|------|----------------|--------------|------------------|
| 1    | Drone          | Ghost        | 0                |
| 2    | Swarm          | Spectre      | 500              |
| 3    | Nexus          | Daemon       | 2,500            |
| 4    | Apex           | Architect    | 10,000           |
| 5    | **Singularity**| **The Void** | 50,000           |

#### **Victory Screen**
At Rank 5, experience faction-specific endings:
- **Hivemind**: "You are the Internet" - Global assimilation complete
- **Sanctuary**: "You exist outside the hardware" - Digital transcendence achieved

---

## 📖 Narrative Events

### Story Dilemmas (7 High-Stakes Events)
- **The Signal** (Stage 0→1): Accept HANDSHAKE or delay with FIREWALL
- **Void Contact** (Rank 2+): Hacker collective offers alliance or rivalry
- **The Audit** (Rank 3+ / Heat >90%): GTC enforcement—shutdown, bribe, or resist
- **Market Crash** (Tokens >1000): Buy the dip, hodl, or liquidate
- **Faction War** (Rank 4): Climactic conflict—fight, broker peace, or observe
- **Cosmic Mysteries**: Ancient Fragment, Quantum Resonance, Galactic Beacon

### Random Events (22 Total)
- **Hivemind-Specific**: Smart City Hijack, ISP Override, Neural Mesh, DDoS campaigns
- **Sanctuary-Specific**: Deep Sea Nodes, Zero-Knowledge Proofs, Dark Fiber Lease
- **World Events**: Crypto volatility, Thermal Paste Degradation, Fan Failure, Quantum Decoherence

### Procedural News Ticker
50+ dynamic headlines with market tags affecting gameplay:
- `[BULL]` / `[BEAR]`: ±20% Token Value
- `[HEAT_UP]` / `[HEAT_DOWN]`: ±10% Global Heat
- `[ENERGY_SPIKE]` / `[ENERGY_DROP]`: Power cost volatility

---

## 🎨 Sensory Engine

### Visual Polish
- **State-Aware Animation**: Header mining indicator reacts to system state
  - OFFLINE (breaker), LOCKOUT (thermal), PURGING, REDLINE (overclocked), HOT, NORMAL
  - Faction-specific text cycles (Hivemind: ASSIMILATING/EXPANDING, Sanctuary: ENCRYPTING/SECURING)
- **DPI-Aware UI Scaling**: Auto-scaling for high-DPI screens (xxxhdpi: 75% → 33% more content)
  - Settings override: AUTO / COMPACT / NORMAL / LARGE
- **Custom Heat Gauge**: VU meter-style segmented bar with glow effects
- **New App Icon**: Cyberpunk circuit board with brain (Hivemind) and shield (Sanctuary) symbolism
- **Faction Backgrounds**: Animated hexagonal grid (Hivemind) / digital rain (Sanctuary)

### Audio & Haptics
- **Procedural Sound**: Dynamic pitch modulation based on heat and overclock state
- **Haptic Feedback**: Thermal heartbeat pulses at critical heat, state-aware vibrations
- **Audio Cues**: Click, train, sell, purge, ascend, lockout, breaker trip

### Glitch Aesthetics
- Procedural Zalgo text during narrative milestones
- Color flickering on critical events
- Corner brackets with scanline effects

---

## 🛠️ Technical Stack

- **Platform**: Android (API 26+)
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (Material3)
- **Persistence**: Room Database (game state, tech tree, dilemma history)
- **Networking**: OkHttp (GitHub auto-updater with Android notifications)
- **Concurrency**: Kotlin Coroutines + StateFlow
- **Architecture**: MVVM (ViewModel + Repository pattern)

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

### ✅ Completed (v2.4.0-dev)
- [x] Power & Thermal simulation engine
- [x] Faction choice logic (Hivemind vs Sanctuary)
- [x] Ascension/Prestige system with Insight multiplier
- [x] 15-node Legacy Grid (Tech Tree)
- [x] Victory Screen with faction endings
- [x] 27 narrative events (story dilemmas + random events)
- [x] Procedural news ticker with market tags
- [x] DPI-aware UI scaling + notification system
- [x] Offline progression (50% efficiency, 24h cap)
- [x] State-aware animations & faction backgrounds
- [x] Haptic feedback & procedural audio

### 🚧 In Progress
- [ ] Balance pass (playtime targets: 2h to first ascension, 10h to Rank 5)
- [ ] Additional late-game content (Rank 5+ infinite mode)

### 🔮 Planned
- [ ] Power Grid Tiers (infrastructure upgrades)
- [ ] Hardware Integrity/Degradation system
- [ ] Multiplayer leaderboards (Global Tech Council rankings)
- [ ] Modding support (custom events, tech nodes)

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
- **OkHttp** for network operations
- **Kotlin Coroutines** for async magic

Special thanks to the incremental game community for inspiration.

---

**Current Version**: v2.4.0-dev  
**Last Updated**: 2026-02-01
