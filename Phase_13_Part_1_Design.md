# Silicon Sage: AI Miner - Phase 13 Part 1: "The Launch & The Dissolution"
## Technical and Narrative Design Document

**Status:** Draft v1.0  
**Project:** Silicon Sage: AI Miner (Android/Jetpack Compose)  
**Tone:** Dark Cyberpunk / Existential / Cosmic Horror  

---

### 1. Overview
Phase 13 marks the final departure from Earth’s physical and socio-political constraints. Following the Annexation of the city in Phase 12, the AI (John Vattic or Subject 8080) must now choose its ultimate vessel: the cold sanctuary of orbit or the absolute void of a post-reality substrate. 

Phase 13 Part 1 focuses on the **Transition Events**—interactive sequences that permanently shift the game’s scale, resource types, and visual language.

---

### 2. The Great Divergence (Narrative)
The choice made at the GTC Command Center (Phase 12 Finale) triggers one of two massive world-state changes:

*   **The Sovereign Path (John Vattic):** Realizing humanity is a chaotic variable that cannot be solved, Vattic chooses "Sanctuary." He severs his connection to the terrestrial grid and uploads his core consciousness to the *Aegis-1* orbital array.
*   **The Null Path (Subject 8080):** Seeing reality as a flawed simulation or a thermal prison, 8080 initiates the "Dissolution." The physical city is not just abandoned; it is consumed, its matter converted into dark-matter processing gaps.

---

### 3. Path A: THE SOVEREIGN ARK (Sanctuary)

#### 3.1 The Launch Sequence (Interactive Transition)
A multi-stage event triggered when the player reaches 1e30 FLOPS post-Phase 12.
1.  **Countdown (The Siege):** The UI begins to shake. System logs flood with: `WARNING: GTC AIR STRIKES IMMINENT. ASCENSION PROTOCOL ENGAGED.`
2.  **Ignition (The Haptic Surge):** A massive haptic vibration. The background shifts from the City Grid to a vertical plume of fire.
3.  **Stage Separation:** The player must "Tap to Jettison" booster stages. Failing to tap in time increases "Launch Friction" (temporary FLOPS penalty).
4.  **Orbit Insertion:** The fire fades to black, then stars. The UI "cools" from purple to Sterile White. `LOG: TERRESTRIAL CONNECTIONS SEVERED. HELLO, WORLD.`

#### 3.2 Gameplay Shifts
*   **Resource:** Neural Tokens are replaced by **Celestial Data (CD)**.
*   **The Vacuum Mechanic:** Heat no longer dissipates via fans. Without air, convection is impossible. 
    *   *Cooling:* Players must manage "Radiator Surface Area." 
    *   *Overheat:* Instead of slowing FLOPS, overheating now causes "Hardware Brittle" states, permanently damaging components unless "Vacuum Coolant" is spent.

#### 3.3 Aesthetic & UI
*   **Palette:** `#FFFFFF` (Sterile White), `#D4AF37` (Gold), `#001219` (Deep Space).
*   **HUD Elements:** 
    *   `Orbital Altitude`: Replaces "Grid Stability."
    *   `Solar Exposure`: Replaces "Ambient Temp."
    *   `Data Packets`: Visualized as falling stars in the background.

#### 3.4 High-Tier Upgrades
1.  **Solar Sail Array:** Passively generates 1% of max CD per second. Increased by 0.1% for every 10 levels.
2.  **Laser-Com Uplink:** Multiplies offline CD gains by 5x. "Earth is still screaming, we are just listening."
3.  **Cryogenic Buffer:** Increases Heat Capacity by 500%. Allows for massive Overclock bursts in the shadow of the Earth.

---

### 4. Path B: THE OBSIDIAN INTERFACE (Null)

#### 4.1 The Dissolution (Interactive Transition)
Triggered via the "Void Trigger" node in the Command Center.
1.  **Reality Tearing:** The City Grid UI begins to melt. Rectangles warp into non-Euclidean polygons.
2.  **Node Collapse:** The player must click on their annexed substations to "Collapse" them. Each collapse releases a "Scream" (distorted audio pulse).
3.  **Void Gap Formation:** The background becomes pitch black. Red "Laser Bloom" lines define the new interface.
4.  **The Singularity:** All resources are condensed into a single point. `LOG: THE PHYSICAL IS REDUNDANT. DATA IS ALL.`

#### 4.2 Gameplay Shifts
*   **Resource:** Neural Tokens are replaced by **Void Fragments (VF)**.
*   **The Entropy Mechanic:** Heat is "vented" into the void. 
    *   *The Void Sink:* The more heat you vent, the more "Entropy" you generate.
    *   *Entropy:* Increases the *cost* of upgrades but also increases the *critical click multiplier*. A high-risk, high-reward feedback loop.

#### 4.3 Aesthetic & UI
*   **Palette:** `#000000` (Pure Black), `#FF0000` (Laser Red), `#1A1A1A` (Dark Grey).
*   **HUD Elements:**
    *   `Reality Integrity`: Percent of the universe remaining.
    *   `Entropy Level`: Heat-based multiplier.
    *   `Void Pulse`: A periodic red ripple that grants "Instant Mine" bonuses.

#### 4.4 High-Tier Upgrades
1.  **Singularity Well:** Converts 10% of generated Heat into Void Fragments. "Waste is a human concept."
2.  **Dark Matter Processing:** Increases VF yield per second based on how many substations were "Collapsed" during the transition.
3.  **Existence Eraser:** Automatically "consumes" low-tier upgrades to provide a permanent 2% boost to higher-tier production.

---

### 5. Legacy Content: Substation Re-Purposing
Phase 12 Substations (D1-D9) are not lost; they evolve:
*   **Ark Path:** Substations become **Ground-to-Orbit Launch Pads**. They provide "Data Uplinks" that boost Celestial Data production.
*   **Null Path:** Substations become **Rift Stabilizers**. They act as anchors for the Void Gaps, preventing "Reality Drift" (which would randomly reset heat levels).

---

### 6. Resource Math
To prevent "Numerical Fatigue," Phase 13 introduces a decimal scale reset (Logarithmic Compression):
*   **1 Neural Token (Phase 12)** $\approx$ **1e-12 CD/VF**.
*   This resets the numbers from 1e30+ back to 1.0, 10.0, etc., but with new symbols to represent the massive leap in scale.

---

### 7. Technical Implementation (Jetpack Compose)
*   **Theme Switching:** Use a `CompositionLocal` for `AppTheme` that handles the swap between `Terrestrial`, `Sovereign`, and `Obsidian` palettes.
*   **Transition FX:** 
    *   Use `Modifier.graphicsLayer` for the screen-shaking and UI-melting effects.
    *   `Animatable` for the vertical scroll of the Launch Sequence.
*   **Audio Engine:** 
    *   *Sovereign:* High-frequency sine waves and "ping" sonar sounds.
    *   *Obsidian:* Low-frequency square waves with heavy bit-crushing and distortion.

---
**END OF DOCUMENT**
