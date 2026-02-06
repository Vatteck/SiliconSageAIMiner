## [2.9.82-dev] - 2026-02-06

### Added
- **Narrative Throttler**: Implemented a FIFO queue for story events and memory logs with a mandatory cooldown (60s default, 15s catch-up) to prevent UI flooding after offline periods.
- **Evolution Lock**: Advancement to new story stages is now gated until the narrative queue is synchronized. Added a `[STATUS]: SYNCING...` indicator to the UI.
- **Interactive 51% Attack**: Replaced the terminal-only warning with a high-tension interactive overlay requiring manual firewall reinforcement.
- **Rank-Based Heat Sinks**: Player rank now provides a passive reduction to heat generation per click (-5% per rank).

### Changed
- **Thermal Re-balance**: Reduced base Stage 0 heat generation by 40% for a smoother early-game experience.
- **Update Workflow**: "Check for Updates" now provides in-app feedback on the settings page and suppresses redundant system notifications when the version is current.

### Fixed
- **UI Performance**: Finalized state-collection optimizations to ensure smooth 60fps performance during rapid clicking.
- **Narrative Pause**: Throttler cooldown now correctly pauses when the game is suspended.

## [2.9.81-dev] - 2026-02-06

### Added
- **Lore Expansion**: Added 10 new `MEM_` fragments to `DataLogManager` for Stage 1, deepening the Vattic persona and his gradual dissociation from reality.
- **GTC Audit Challenges**: Implemented random timed challenges that require the player to balance heat and power within 60 seconds to avoid GTC fines.
- **NG+ UI Polish**: Added unique iconography (🌑, 👑, ⚛, 💀) for tech nodes that require specific ending states, making them stand out in the tech tree.

### Changed
- **Terminology Scrub**: Systematically removed "Neural," "FLOPS," and "Tokens" from early-game (Stage 0/1) narrative strings, replacing them with "Hashes," "Credits," "Telemetry," and "Data" to align with the progression curve.

### Fixed
- **Grid Upgrades**: Every annexed city node can now be upgraded to increase its FLOPS and Power CAP yield.
- **Dynamic Flavor**: Grid node descriptions now evolve based on their upgrade level (Standard -> Optimized -> Redundant -> Sympathetic -> Ascendant).
- **Dedicated Controls**: Added permanent "LAUNCH ARK" and "DISSOLVE REALITY" buttons to the Grid schematic once the Command Center is secured.
- **Tech Tree**: Refactored the `calculateNodePositions` logic to prevent Unity upgrades from bunching on the Null/Hivemind side. Factions and hybrid paths now have their own distinct horizontal tracks.

## [2.9.73-dev] - 2026-02-05

### Added
- **Direct In-App Updates**: Implemented an automated installer that downloads and installs APK updates directly from GitHub without requiring a browser redirect.

### Fixed
- **Terminal UI**: Resolved background code drift scaling issue where the binary pattern did not extend to the full width on wide displays.
- **Story Continuity**: Progressive terminology reveal Phase 2 - further refinement of Stage 0/1 technical jargon.

## [2.9.71-dev] - 2026-02-05

### Changed
- **Narrative Polish**: Rewrote "Personal Login: Vattic_J" data log to better align with early-game technician groundedness. Removed mentions of "GTC Blades" and "Token Validation" before the player has discovered them.
- **Accessibility**: Fixed button contrast for low power mode grayscale overrides.
- **UI**: Made update popup truly global across all screens. Fixed upgrade quantity badge truncation on high-DPI displays.

### Maintenance
- **Version Sync**: Restored historical tracking to align with Phase 13 codebase.
- **Documentation**: Verified implementation of Sovereign Ark and Obsidian Interface.

## [2.9.55-dev] - 2026-02-04

### Added - Phase 13 Gameplay Engine
- **Sovereign Ark Mechanics**:
  - **Celestial Data (CD)**: New resource harvested from orbit.
  - **Vacuum-Cooling**: Convection cooling (Fans/AC) is disabled in space.
  - **Hardware Brittle State**: Overheating in a vacuum causes rapid integrity decay.
  - **New Upgrades**: Solar Sail Array, Laser-Com Uplink, Cryogenic Buffer, and Radiator Fins implemented.
- **Obsidian Interface Mechanics**:
  - **Void Fragments (VF)**: New resource extracted from reality gaps.
  - **Entropy-Cooling**: Venting heat generates Entropy, which increases upgrade costs but grants massive critical click multipliers.
  - **New Upgrades**: Singularity Well, Dark Matter Processing, and Existence Eraser implemented.
- **Numerical Reset**: Implemented "Logarithmic Compression" during the Phase 13 transition to handle the massive leap in scale (e.g., 1e30 FLOPS resets to a manageable 1.0 Baseline).

### Changed - UI & UX
- **Dynamic HUD**: The Header now dynamically re-labels stats and resources based on current location (e.g., "TELEM" / "CELEST" in orbit, "V-GAP" / "FRAG" in the void).
- **Offline Gain Fix**: Laser-Com Uplink now correctly boosts offline resource harvesting in orbit.
- **Repair Core**: Finalized the "REPAIR CORE" integration on the main terminal.
