## [2.9.89-dev] - 2026-02-06

### Added
- **HUD Polish**: 
  - Increased font sizes across the header for better legibility on high-DPI displays.
  - High-contrast white iconography for FLOPS and Credits.
  - More dramatic waveform scaling—the HUD now visually "jitters" more intensely during high-load and overclocking.
  - Added a dedicated "Used / Total" power indication.

### Fixed
- **UI Stability**: Resolved overlapping issues with the "Syncing" status indicator.
- **Click Responsiveness**: Fine-tuned the click-scramble intensity for better readability during rapid input.

## [2.9.88-dev] - 2026-02-06

### Added
- **HUD Polish & Stability**:
  - Restored Terminal OS branding (systemTitle) in the metadata ribbon.
  - Fixed value wrapping by increasing column widths to 120dp.
  - Low-profile "Syncing" indicator integrated into the bottom gauge row to prevent vertical header expansion.
  - Deeper load-aware kinetic waveforms with frequency doubling during Overclocking.
  - Restored interactive 51% Attack overlay.

## [2.9.83-dev] - 2026-02-06

### Added
- **Kinetic HUD Overhaul**: Complete redesign of the header for higher density and better visual feedback.
  - **Metadata Ribbon**: Consolidated Status, Rank, Sec, and Loc into a single ultra-compact line at the top.
  - **Reactive Waveform Borders**: Added procedural sine-wave borders that vibrate and spike based on click frequency.
  - **Click Jolt FX**: Every manual compute hash triggers a high-tension jolt effect in the HUD background and border waveforms.
  - **Faction Substrates**: Implemented procedural background scrims for Hivemind (Synapse lines), Sovereign (Monoliths), and Null (Binary noise).
  - **Unified Gauge**: Merged Heat and Integrity into a single split-progress bar at the bottom of the header.

### Fixed
- **UI Performance**: Migrated header drawing to native Canvas commands for GPU-accelerated rendering.
- **Narrative Sync**: Isolated the "Syncing" indicator to its own dedicated status line to avoid clobbering the player's Rank title.

## [2.9.82-dev] - 2026-02-06

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
