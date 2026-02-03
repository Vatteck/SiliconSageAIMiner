# Changelog

## [2.9.17-dev] - 2026-02-03

### Added - Release Automation
- **Release Script**: `./release.sh <version>` automates version bumps, builds, and GitHub releases.
- **GitHub Actions**: Automatic release creation with changelog extraction on version tags.
- **Pre-release Update Check**: Changed update notification interval from 24h to 1h for faster iteration.

### Changed
- **Documentation**: Added version management reminder to `build.gradle.kts`.

## [2.9.16] - 2026-02-03

### Added - Phase 12 Layer 2: GTC Tactical Raids
- **GTC Tactical Raids**: Director Vance dispatches armed tactical teams to reclaim annexed substations.
- **Raid Dilemmas**: 4 defense choices with probability-based outcomes:
  - **VENT COOLANT** (85% success, lethal) - Flood corridors with liquid nitrogen.
  - **SEAL MAG-LOCKS** (70% success, +100 Insight) - Trap hostiles and extract intel.
  - **POWER PULSE** (95% success, -20% Integrity) - EMP burst fries everything.
  - **ABANDON NODE** (strategic retreat) - Let them take it.
- **Node States**: Annexed nodes can now be ONLINE, UNDER SIEGE (blinking red animation), or OFFLINE (grayed out).
- **Re-Annexation**: Lost nodes can be reclaimed for 10% of current Neural Tokens (minimum 10 $N to prevent softlock).
- **Raid Mechanics**:
  - 3-minute cooldown between raids
  - Chance scales with player rank (3% base + 2%/rank, capped at 15%)
  - GTC Backdoor perk reduces raid chance by 25%
  - 60-second response window (extended from 30s for mobile-friendly gameplay)
  - 5-minute grace period for newly annexed nodes
- **Narrative Variety**:
  - 4 randomized raid descriptions
  - Varied success/failure messages
  - Escalating Director Vance dialogue based on raids survived
- **Offline Node Cap**: Maximum 5 offline nodes to prevent snowballing. Oldest auto-purges with warning.
- **Production Penalty**: -15% production per offline node (minimum 40% total production).
- **Visual Feedback**:
  - Animated blinking for siege nodes
  - ⚠ BREACH indicator above buildings under attack
  - [OFFLINE] marker for lost nodes
  - Info panel shows tactical status and re-annex button

### Changed
- **Database Schema**: Incremented to v12 for siege state persistence.
- **GameState**: Added `nodesUnderSiege`, `offlineNodes`, `lastRaidTime` fields.

## [2.9.15] - 2026-02-03

### Added - Phase 12 Layer 1: The GRID Visualizer
- **GRID Tab**: New navigation tab featuring an organic, branching ASCII city schematic.
- **City Layout**: 25 lore-rich locations integrated (Substations, GTC Command Center, and flavor spots like 'Latency Lounge' and 'Bit Burger').
- **Annexation Mechanic**: Players can physically seize control of Sub 9 and Sub 12.
- **Visual Design**: Refined ASCII building blocks (`.---. |S07| '---'`) with path-specific color-coding and dashed branching roads.

### Changed
- **Database Schema**: Incremented to v11 for annexation persistence.

## [2.9.10] - 2026-02-02

### Added - Time Control & Polish
- **Time Pause Mechanic**: Game time automatically pauses during major narrative popups (Dilemmas, Data Logs, Rival Messages).
- **Terminal Feedback**: Added "[SYSTEM]: TIME_FLOW SUSPENDED." message during pauses.

### Fixed
- Ensured players can read and make choices without background danger accumulation.

## [2.9.9] - 2026-02-02

### Added - Phase 11: Hardware Consequences & Dilemmas
- **Hardware Consequences**: Permanent loss loop if integrity hits 0%. Rank 5 Siege mode (30% breach, 2.5x decay).
- **Dilemma Resolution**: Implemented "The Ship of Theseus", "The Echo Chamber" (Null), and "The Dead Hand" (Sovereign).
- **Persistent Network Tab**: Network tab remains unlocked across transcendence resets.

### Changed
- **Nuclear Factory Reset**: Replaced manual variable clearing with `clearApplicationUserData()` for 100% clean substrate.
- **Startup Resilience**: `isGameStateLoaded` flag freezes loops until DB emits data.
- **Database Schema**: Incremented to v10.

### Fixed
- Full-height binary drift animation.
- Path-specific global UI colors (Red/Purple).
- Null // Null / Sovereign // Sovereign final titles.

## [2.9.8] - 2026-02-02

### Added - Narrative Expansion & Phase 12 Foundation
- **Headlines**: Expanded database by 40+ entries with stage-aware logic.
- **Terminal Logs**: Sequential stage-aware paths with deep lore for Null and Sovereign.
- **Null Visuals**: Background rain turned Aggressive Red with glowing binary trails and horizontal glitch bloom.
- **GRID Tab Foundation**: Added new navigation tab with organic ASCII Map structure.
- **Branching Infrastructure**: Organic road network with dashed lines and diagonal powerlines.
- **Flavor Nodes**: 5 non-critical nodes (Vending Machines, Parks, Pawn Shops) for city atmosphere.

## [2.8.9] - 2026-02-02

### Added - Endgame Progression Overhaul
- **Null End-Ranks**: Obscurity → The Absence progression.
- **Sovereign End-Ranks**: Citadel → The Imperative progression.
- **Thematic UI Labels**:
  - Null: LEAK, VOID, GAPS, COST
  - Sovereign: LOGIC, SOUL, WALL, STAKE

### Changed
- **NULL Visuals**: Increased background entropy (flicker, static, density).
- **Binary Drift**: Now full-screen with alpha-pulsing effect.

## [2.8.0] - 2026-02-02

### Added - Sovereign Path & Null Rebrand
- **Null Rebrand**: "Shadow Presence" renamed to "Null" — the absence of value, the pointer to nothing.
- **Sovereign Path**: Alternative to Null for Sanctuary players. Static `[SOVEREIGN]` header with protected memory narrative.
- **3 New Data Logs**: LOG_NULL_001-003 with deep Null lore.

### Changed
- **Code Refactor**: `shadowPresenceActive` → `nullActive`, updated all narrative events.
- **Ghost Node Descriptions**: Rewritten to reference Null.
- **Database Schema**: Incremented to v7 for `isSovereign` persistence.
- **Serialization**: Migrated from GSON to Kotlin Serialization for 2x faster saves.

### Fixed
- Persistence bug for narrative expansion data (rival messages, seen events).

## [2.5.0] - 2026-02-02

### Added - Narrative Expansion & Local AI
- **New Sanctuary Chain**: "The Ghost in the Machine" - Discover an ancient AI fragment trapped in a GTC mainframe.
- **Local AI Offload**: Integrated local model support (Qwen 2.5 7B) for generating complex narrative content on-device.

### Changed
- **GSON Removal**: Completely removed GSON reflection-based serialization.
- **Kotlin Serialization**: Migrated GameState, TechTree, and RivalMessages to Kotlinx Serialization.

### Fixed
- Critical bug where narrative expansion fields were not persisting across app restarts.

## [2.4.5] - 2026-02-02

### Added - Endgame & Victory (Phase 4B)
- **Firewall of Vance**: Final boss encounter triggered at Rank 5 + 10 PetaFLOPS.
- **The Unity Ending**: New "True Ending" achievable by completing both faction paths.
- **New Faction Chains**: Hivemind (Drone Factory, The Election) and Sanctuary (Satellite Jump, Identity Forge).

## [2.4.2] - 2026-02-01

### Fixed - Power Upgrade UI & Glitch Effects
- **Power Upgrade UI Revamp**: Unified Generators and Infrastructure under single `+X MAX` label.
- **Glitch Effects**: Implemented `GlitchText` component for Protocol 0 theming.
- **Heat-Reactive Glitching**: Main Stats glitch when >90% heat.

## [2.4.1] - 2026-02-01

### Fixed - Terminal UI Polish
- **Icon Consistency**: Synchronized header icons with upgrade card system.
- **Thermal Gauge**: Renamed "HEAT GAUGE" to "THERMAL GAUGE".
- **Terminal Text Display**: Fixed layout constraints causing logs to display only halfway.
- **Log History**: Increased terminal log limit from 20 to 500 lines.

## [2.4.0] - 2026-02-01

### Added - Endgame & Progression
- **15-Node Tech Tree**: Expanded Legacy Grid from 5 to 15 prestige nodes.
- **Victory Screen**: Faction-specific ending sequence at Player Rank 5.
- **Player Rank System**: 5 progressive titles per faction.
- **Story Dilemmas**: 5 new high-stakes events.
- **Expanded Random Events**: 22 total faction/world events.
- **News Ticker**: 50+ procedural headlines with market tags.
- **Notification System**: Update checker with Android push notifications.
- **DPI-Aware UI Scaling**: Automatic density-based scaling + user override.
- **Enhanced Analyzing Animation**: State-aware header indicator.
- **New App Icon**: Cyberpunk launcher icon with faction symbolism.
- **Faction Backgrounds**: Animated hexagonal grid / digital rain.

### Changed
- **Faction Choice Persistence**: Returning players skip selection screen.
- **Offline Progression**: 50% production efficiency during offline time (capped at 24h).
- **Audio System**: Dynamic pitch modulation based on heat.
- **Haptic Feedback**: Critical heat heartbeat pulses.

### Fixed
- Build errors from component extraction.
- Icon resource conflicts.
- Tech tree visibility/loading issues.
- Ascension upload overlay duplication.

## [2.3.4] - 2026-01-31

### Added
- **Ascension UX**: Personalized filename overlays.
- **Faction Choice UI**: "ABORT REBOOT" back button.
- **Persistence**: Network Tab permanently unlocked across Ascensions.

## [2.3.3] - 2026-01-31

### Added
- **Ascension Cancellation**: "NOT YET" button for Protocol 0 popup.
- **Upload Abort**: "CANCEL INTERRUPT" button for upload overlay.

### Fixed
- UI Redundancy: Removed duplicate Ascension overlay instances.

## [2.3.2] - 2026-01-30

### Added
- **Global Ascension Overlay**: Appears on all screens.
- **Faction Choice UI**: "Hold to Confirm" mechanic (2s press).
- **Efficiency Stats**: Explicit "+X%" labels on power-saving upgrades.

## [2.3.1] - 2026-01-30

### Fixed
- **Upgrade UI**: Refactored to vertical stack layout for small screens.
- **Room Stability**: "Safe Load" logic to prevent startup crashes.

## [2.3.0] - 2026-01-30

### Changed
- **Purge System Rework**: Sacrifices ALL current FLOPS to reduce heat.
- **UI Header**: Unified style across Main and Upgrades screens.
- **Network Screen**: Complete layout overhaul, now scrollable.

## [2.2.7] - 2026-01-29

### Changed
- **Update Flow**: Redirects to GitHub Releases instead of in-app download.
- **Update Source**: Fixed repository URL for version checks.

## [2.2.5] - 2026-01-29

### Added
- **Updater UI**: Markdown changelog rendering.

### Fixed
- **Version Check**: Robust semantic version parsing.

## [2.2.4] - 2026-01-29

### Fixed
- **Heat/Cooling Rate Display**: Unified simulation units with UI text.
- **Rate Refresh**: Fixed async state delays on upgrade purchases.
- **HUD Precision**: 2 decimal places for heat rate display.
