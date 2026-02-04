# Changelog

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
