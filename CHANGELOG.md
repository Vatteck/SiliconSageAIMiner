# Changelog

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
