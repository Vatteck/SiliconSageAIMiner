# Changelog
 
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
