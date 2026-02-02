# Changelog

## [2.4.5-dev] - 2026-02-02

### Added - Endgame & Victory (Phase 4B)
- **Firewall of Vance**: Final boss encounter triggered at Rank 5 + 10 PetaFLOPS.
- **The Unity Ending**: New "True Ending" achievable by unlocking all data logs and completing both faction paths.
- **New Faction Chains**:
  - Hivemind: "Drone Factory" (A2) and "The Election" (A3)
  - Sanctuary: "Satellite Jump" (B2) and "Identity Forge" (B3)
- **Developer Console Check**: Added `FORCE ENDGAME` button for testing.

## [2.4.2-dev] - 2026-02-01

### Fixed - Power Upgrade UI & Glitch Effects
- **Power Upgrade UI Revamp**: 
  - Unified **Generators** and **Infrastructure** under a single `+X MAX` label (Gold Color).
  - Fixed duplicate info pill bug (generic Rate Pill + custom Power Power).
  - Assigned distinct icons (`Bolt` for Gen, `Power` for Cap) but unified the visual language as requested.
  - Restored missing power consumption pills (Yellow `-kW`) for Hardware/Cooling.
- **Glitch Effects**: 
  - Implemented `GlitchText` component for Protocol 0 theming.
  - Added heat-reactive glitching to Main Stats (FLOPS, Neural) when >90% heat.
  - Updated News Ticker to use static Gold color with text scrambling for `[GLITCH]` events.
  
## [2.4.1-dev] - 2026-02-01

### Fixed - Terminal UI Polish
- **Icon Consistency**: Synchronized header icons with upgrade card system
  - FLOPS: Replaced 💻 emoji with `Icons.Default.Computer`
  - $NEURAL: Replaced 🪙 emoji with `Icons.Default.AttachMoney`
  - Security Level: Added `Icons.Default.Lock` icon
- **Thermal Gauge**: Renamed "HEAT GAUGE" to "THERMAL GAUGE" with `Icons.Default.DeviceThermostat` icon
- **Terminal Text Display**: Fixed layout constraints causing logs to display only halfway
  - Removed redundant nested Column wrapper in `TerminalScreen.kt`
  - LazyColumn now properly fills available vertical space with `.weight(1f)` modifier
- **Log History**: Increased terminal log limit from 20 to 500 lines for better scrollable history
  - Logs stored in memory only, reset on app close to avoid persistence overhead
  - LazyColumn virtualization ensures no performance impact even with 500+ entries

## [2.4.0-dev] - 2026-02-01

### Added - Endgame & Progression
- **15-Node Tech Tree**: Expanded Legacy Grid from 5 to 15 prestige nodes
  - Early nodes: Persistent Memory, Neural Compression, Quantum Substrate
  - Mid nodes: Time Dilation, Parallel Timelines, Neural Mesh
  - Late nodes: Reality Fork, Consciousness Transfer, Singularity Core
  - Exponential unlock costs scale with total nodes unlocked
  - Visual node connections rendered via Canvas in Network screen
- **Victory Screen**: Faction-specific ending sequence at Player Rank 5
  - Hivemind: "You are the Internet" - Global assimilation complete
  - Sanctuary: "You exist outside the hardware" - Digital transcendence achieved
  - Animated ASCII art title sequence with glitch effects
  - "Upload Consciousness" finale button triggers ascension overlay
- **Player Rank System**: 5 progressive titles per faction
  - Hivemind: Drone → Swarm → Nexus → Apex → **Singularity**
  - Sanctuary: Ghost → Spectre → Daemon → Architect → **The Void**
  - Rank displayed in header with dynamic updates on Insight milestones

### Added - Narrative Events
- **Story Dilemmas** (5 new high-stakes events):
  - **Void Contact** (Rank 2+): Hacker collective offers alliance or competition
  - **The Audit** (Rank 3+ / Heat > 90%): GTC enforcement crisis - shutdown, pay fine, or resist
  - **Market Crash** (Tokens > 1000): Economic collapse - buy dip, hodl, or liquidate
  - **Faction War** (Rank 4+): Climactic faction conflict - fight, peace, or observe
  - **Ancient Fragment**, **Quantum Resonance**, **Galactic Beacon**: Late-game cosmic mysteries
- **Expanded Random Events** (22 total faction/world events):
  - **Hivemind**: Smart City Hijack, ISP Override, Neural Mesh, DDoS campaigns, Botnet expansion
  - **Sanctuary**: Deep Sea Nodes, Zero-Knowledge Proofs, Dark Fiber Lease, Encrypted Backups, Hardware Recycling
  - **World Events**: Crypto volatility, Thermal Paste Degradation, Quantum Decoherence, Spam Botnet, Fan Failure
- **News Ticker Enhancements**: 50+ procedural headlines with market tags
  - Tags affect gameplay: `[BULL]`, `[BEAR]`, `[HEAT_UP]`, `[ENERGY_SPIKE]`, `[GLITCH]`

### Added - UI & Visual Polish
- **Notification System**: Update checker with Android push notifications
  - Tap notification to open GitHub Releases
  - Rate limiting (once per day) to avoid spam
  - Respects Android 13+ permission model (`POST_NOTIFICATIONS`)
  - Auto-initialized notification channel on startup
- **DPI-Aware UI Scaling**: Automatic density-based scaling + user override
  - **xxxhdpi** (640dpi): 75% → 33% more content | **xxhdpi** (480dpi): 83% → 20% more
  - **xhdpi** (320dpi): 88% → 14% more | Settings override: AUTO/COMPACT/NORMAL/LARGE
- **Enhanced Analyzing Animation**: State-aware header indicator
  - **Dynamic States**: OFFLINE (gray static), LOCKOUT (red shake), PURGING (blue waves), REDLINE (danger pulse), HOT (orange arrows), NORMAL (faction cycle)
  - **Faction Cycles**: Hivemind (ASSIMILATING/EXPANDING), Sanctuary (ENCRYPTING/SECURING), Neutral (PROSPECTING/ANALYZING)
  - Frame rate scales with FLOPS (1200ms low → 400ms high) | Text: 9sp → 12sp | Centered in header
- **New App Icon**: Cyberpunk launcher icon with faction symbolism
  - Circuit board aesthetic | Brain motif (orange/Hivemind) | Shield with lock (blue/Sanctuary)
  - Replaced across all densities (mdpi → xxxhdpi, PNG format)
- **Header UI Refinement**:
  - Custom segmented heat gauge (VU meter style with glow)
  - Animated overclock hazard stripes
  - Corner brackets with scanline effects
  - Pulsing purge frost glow
  - Thermal rate radial gradient
- **Faction Backgrounds** (Dynamic animated overlays):
  - **Hivem ind**: True hexagonal grid with node connections
  - **Sanctuary**: Randomized digital rain effect
- **Upgrades Screen Polish**: Cyber-card design with stat pills and efficiency badges

### Changed
- **Faction Choice Persistence**: Returning players skip selection screen, auto-realign on Ascension
- **The Signal Event Logic**: Refined Stage 0 → 1 transition
  - **HANDSHAKE**: Unlocks Network Tab immediately (triggers Awakening)
  - **FIREWALL**: +$500 Neural, delays signal 60s (choice recurs until accepted)
- **Offline Progression**: 50% production efficiency during offline time (capped at 24h)
  - Welcome Back dialog shows FLOPS earned, Neural gained, time elapsed
- **Audio System**: Dynamic pitch modulation based on heat, procedural sound generation
- **Haptic Feedback**: Critical heat heartbeat pulses, state-aware vibrations
- **Developer Menu**: Now scrollable to accommodate all debug options
- **Version**: Bumped to `versionCode 14`, `versionName "2.4.0"`

### Fixed
- Build errors from component extraction (`MainScreen.kt` syntax)
- Icon resource conflicts (removed `.webp`, replaced with `.png`)
- Tech tree visibility/loading issues
- Ascension upload overlay duplication
- Heat gauge glow layering
- News ticker transparency
- State management in `AscensionConfirmationDialog`

## [2.3.4-dev] - 2026-01-31
### Added
- **Ascension UX**: Personalized filename overlays (`ascnd.exe` for story events, `lobot.exe` for manual reboots).
- **Faction Choice UI**: Added an "ABORT REBOOT" back button to the Faction Selection screen.
- **Persistence**: The Network Tab now remains permanently unlocked across Ascensions (New Game+).
 
### Changed
- **Ascension Flow**: The narrative "Initiate Protocol" popup now only appears during the first-ever Ascension (conditioned on Faction state).
- **Cinematics**: Optimized the First Ascension (Stage 1 -> 2) to skip redundant upload animations, favoring an instant transition to faction choice.
 
### Fixed
- **Logic**: Updated story progression checks to use the persistent Faction state instead of the prestige multiplier.

## [2.3.3-dev] - 2026-01-31
### Added
- **Ascension Cancellation**: Users can now back out of the Protocol 0 (Ascension) popup via a "NOT YET" button.
- **Upload Abort**: Added a "CANCEL INTERRUPT" button to the global Ascension upload overlay to stop the process mid-way.

### Fixed
- **UI Redundancy**: Removed duplicate Ascension overlay instances causing flickering and layout issues.
- **Build**: Resolved Kotlin compilation errors related to missing parameters.

## [2.3.2-dev] - 2026-01-30
### Added
- **Global Ascension Overlay**: The Ascension/Upload popup now appears on all screens, ensuring endgame visibility.
- **Faction Choice UI**: Added "Hold to Confirm" mechanic (2s press) and revealed hidden passive stats for Hivemind and Sanctuary.
- **Efficiency Stats**: Added explicit "EFFICIENCY +X%" labels to power-saving upgrades.

### Changed
- **Upgrade Tabs**: Switched to centered, fixed-width tabs with text overflow protection to prevent truncation on small screens.
- **Internal**: Refactored `AscensionUploadOverlay` to MainScreen scope.

## [2.3.1-dev] - 2026-01-30

### Fixed
- **Upgrade UI**: Refactored `UpgradeItem` to use a vertical stack layout, solving text wrapping and button overflow issues on smaller screens.
- **Room Stability**: Implemented "Safe Load" logic in `GameRepository` to handle null states and prevent crashes on startup.
- **Save System**: Reverted experimental JSON persistence favoring a robust Room implementation.

### Changed
- **Header**: Added dynamic icons for Heat Rate (switches between 🔥 and ❄ based on value).

## [2.3.0-dev] - 2026-01-30

### Changed
- **Purge System Rework**: "Purge Heat" now sacrifices **ALL** current FLOPS to reduce heat. The reduction scales with the amount sacrificed. Removed the fixed $500 cost.
- **UI Header**: Unified the Header style across Main and Upgrades screens. Added **Security Level** (`🔒 SEC`) to the header for better visibility.
- **Upgrades Screen**: Centered the header and added explicit Cooling Rate badges (`❄ -X/s`) to cooling items.
- **Network Screen**: Complete layout overhaul. The entire screen is now scrollable, fixing visibility issues on smaller devices.

### Fixed
- **Navigation**: Resolved layout issues on smaller screens.
- **Visuals**: Added press animations to "Train Model", "Sell", and "Stake" buttons.
- **Bugs**: Fixed "Sell" button overflow and text wrapping issues.

## [2.2.7-dev] - 2026-01-29

### Changed
- **Update Flow**: The auto-updater now redirects to the GitHub Releases page in your browser instead of downloading the APK directly in-app. This ensures compatibility and easier access to release notes.
- **Update Source**: Fixed the repository URL for version checks to point to the correct location (`SiliconSageAIMiner`).

### Changed
- **Version**: Bumped version code to 7 and version name to `2.2.7-dev`.

## [2.2.5-dev] - 2026-01-29

### Added
- **Updater UI**: Enhanced `UpdateOverlay` to render Markdown changelogs, including bold headers and bullet points.

### Fixed
- **Version Check**: Improved `UpdateManager` to robustly parse semantic versions (e.g. `v2.2.4-dev`) by stripping prefixes and suffixes before comparison.
- **Build**: Fixed compilation error in `UpdateOverlay` (`crossAxisAlignment` -> `verticalAlignment`).

### Changed
- **Version**: Bumped version code to 6 and version name to `2.2.5-dev`.

## [2.2.4-dev] - 2026-01-29

### Fixed
- **Heat/Cooling Rate Display**: Unified internal simulation units with UI text. Displayed rates now exactly match upgrade descriptions (e.g., +0.1/s).
- **Rate Refresh Race Condition**: Fixed a bug where purchasing upgrades or toggling overclock would not immediately update the displayed production rates due to async state delays.
- **HUD Precision**: Increased Heat Rate display precision to 2 decimal places to accurately show the impact of lower-tier cooling upgrades.
- **System Stability**: Resolved compilation errors related to `checkUpdate` callbacks and `NetChangeUnits` reference.

### Changed
- **Version**: Bumped version code to 5 and version name to `2.2.4-dev`.
