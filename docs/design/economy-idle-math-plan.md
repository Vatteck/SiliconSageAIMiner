# SUBSTRATE:Miner Economy Rail — Idle Math Application Plan

Status: Draft rail, created 2026-04-24 22:22 EDT  
Repo: `/home/vatteck/Projects/SiliconSageAIMiner`  
Branch observed: `refactor/gameplay-loop-wip-2026-03-01`  
Compile gate at plan creation: `./gradlew :app:compileDebugKotlin` passed before this doc was written.

## Why this exists

The Faceminer full gameplay-loop overhaul is scrapped. It made datasets the spine of the game and fought the current Hash Factory story.

The current direction is different:

- the player begins as a supposed GTC employee computing assigned hash packets
- the truth is that the player is an AI in a corporate work sandbox
- manual compute and automation process assigned work
- hardware expands compute capacity/substrate, not direct wallet faucets
- DATAMINER/datasets unlock only after the airgap as a sidecar of unauthorized data access

Canonical narrative/economy spine: `docs/narrative-production-spine.md`.

This plan preserves useful idle-game math lessons, but its old direct-hardware-production assumptions are now legacy reference material. Do not treat this document as permission to deepen direct passive `$FLOPS/s` upgrade faucets.

The rail is deliberately boring:

1. Read `docs/narrative-production-spine.md` at the start of economy work.
2. Read this doc only for historical math/context.
3. Pick the next checked/unchecked item from `gametasks.md` under the current v5.x section.
4. Make only that change.
5. Compile.
6. Update docs + `gametasks.md`.
7. Put new ideas in the parking lot unless Cory explicitly swaps scope.

## Hard non-goals

- Do not resurrect the full Faceminer loop redesign from `PLAN.md`.
- Do not make Neural Tokens buy every upgrade as a surprise structural rewrite.
- Do not replace the core `$FLOPS` spendable wallet without an explicit migration plan.
- Do not make datasets/DATAMINER the main progression spine unless Cory explicitly pivots the whole game.
- Do not add direct upgrade-granted passive wallet production in new work; future balance should move hardware toward production-loop capacity.
- Do not add dataset modifiers, decay nodes, corruption spread, or a new active-loop system in this pass.
- Do not touch release/signing flow unless explicitly asked.
- Do not force-push, reset hard, or delete branches.

## Current economy shape

### Production

Relevant files:

- `app/src/main/java/com/siliconsage/miner/domain/engine/ProductionEngine.kt`
- `app/src/main/java/com/siliconsage/miner/domain/engine/ResourceEngine.kt`
- `app/src/main/java/com/siliconsage/miner/viewmodel/GameViewModel.kt`

Current implementation is still direct-idle legacy:

```kotlin
baseFlops += ownedCount * tierProduction
```

Then it is multiplied by grid, faction, skill, prestige, temporary, thermal, and saturation modifiers via `ResourceEngine.calculateFlopsRate()` and eventually feeds the wallet tick.

Target direction is different:

```text
upgrade count → compute capacity / lanes / automation / efficiency
assigned work queue → packet processing loop → completed packet payout → $FLOPS wallet
```

Treat direct hardware `$FLOPS/s` as legacy behavior to replace deliberately, not as the desired long-term model.

### Upgrade costs

Relevant file: `app/src/main/java/com/siliconsage/miner/util/UpgradeManager.kt`

Single-level cost is exponential:

```kotlin
base * 1.15.pow(level.toDouble()) * entropyMultiplier * repModifier
```

Bulk cost currently exists via looped summation. Earlier v4.0.6 backlog says geometric projection was added, but current inspected code still loops in `calculateMultiLevelCost()` and `calculateMaxAffordableLevels()`. Verify before changing; do not assume docs are current.

### Prestige / migration

Relevant file: `app/src/main/java/com/siliconsage/miner/util/MigrationManager.kt`

Persistence gain:

```kotlin
100 * log10(flops / 1000)
```

Multiplier boost:

```kotlin
potentialPersistence * 0.1
```

This is safe but heavily compressed. Do not alter without a simulation table.

### Saturation

Relevant files:

- `GameViewModel.kt`
- `ProductionEngine.kt`
- `ResourceEngine.kt`

Saturation stalls production with:

```kotlin
stallMultiplier = (1.0 - saturation).coerceIn(0.0, 1.0)
```

Migration preserves saturation. Overwrite resets saturation. This already provides reset pressure and should be left alone unless we are explicitly tuning prestige.

### Dataset/storage sidecar

Relevant files:

- `DatasetManager.kt`
- `AutoClickerEngine.kt`
- `StorageNarrativeEngine.kt`
- `HeaderSection.kt`
- `DatasetPickerOverlay.kt`

Storage pressure and dataset inventory are worth preserving. Treat datasets as a post-airgap sidecar pressure loop, not the new spine of the whole game.

Before the airgap, the player is just a supposed GTC employee processing assigned hash packets. DATAMINER unlocks after the airgap because the AI has crossed the containment boundary and can perceive unauthorized external data flows. Datasets should provide burst opportunities, lore, risk, faction hooks, and special rewards; they should not simply become “better hashes.”

## Article lessons worth applying

### 1. Exponential costs are fine, but buy-max should use geometric math

For a cost curve:

```text
cost(n) = baseCost * growthRate^owned
```

Cost to buy `k` levels from current owned count `n`:

```text
totalCost = baseCost * growthRate^n * (growthRate^k - 1) / (growthRate - 1)
```

Max affordable levels:

```text
k = floor(log(available * (growthRate - 1) / currentCost + 1) / log(growthRate))
```

In this project, `growthRate = 1.15`, but effective base cost includes location entropy and reputation modifier. Use the same modifiers as `calculateUpgradeCost()` so output stays behavior-compatible.

### 2. Milestones create the bumpy idle-game feel — legacy note

A flat exponential cost curve with additive direct production trends toward “buy latest tier forever.” Milestones made old direct hardware production spike back into relevance.

This is already implemented in the legacy direct-production model, but it should not be deepened until the production-loop conversion is designed. In the target model, milestone-style bumps should apply to capacity, lanes, throughput, efficiency, or automation behavior — not direct wallet income.

Candidate thresholds:

```text
25 owned  -> x2 tier output
50 owned  -> x2 again
100 owned -> x2 again
200 owned -> x2 again, optional late-game
```

Keep this as a helper function, not scattered inline conditionals.

Candidate helper:

```kotlin
fun calculateMilestoneMultiplier(count: Int): Double = when {
    count >= 200 -> 16.0
    count >= 100 -> 8.0
    count >= 50 -> 4.0
    count >= 25 -> 2.0
    else -> 1.0
}
```

Then each hardware tier becomes:

```kotlin
owned * baseRate * calculateMilestoneMultiplier(owned)
```

This changes balance, so it belongs after buy-max math and after a compile gate.

### 3. Derivative chains fit the fiction, but not this pass

The Part II chain model maps beautifully to SUBSTRATE:

```text
assigned hash queue -> automation workers -> completed packets -> $FLOPS
storage -> post-airgap datasets -> bursts / lore / special rewards
```

But implementing that now would become Faceminer 2.0. Park it.

### 4. Prestige formula defines behavior

Lifetime-based prestige rewards deeper runs. Since-reset prestige can reward shallow reset spam.

Current SUBSTRATE formula is based on current `flops`, not clearly lifetime. Before changing it, we need a small simulation across representative FLOPS values and migration counts.

Do not tune prestige from vibes.

## Implementation phases

### Phase 0 — Rail setup

Goal: establish docs and task tracking.

Files:

- `docs/economy-idle-math-plan.md`
- `gametasks.md`

Gate:

- Documentation exists.
- `gametasks.md` has a `v4.1.x — Economy Rail` section.
- No gameplay code changes unless already present before this plan.

### Phase 1 — Safe math cleanup: geometric bulk-buy

Goal: make bulk buy mathematically correct and fast without changing single-level cost behavior.

Files:

- `UpgradeManager.kt`

Work:

- Add constants/helper for growth rate.
- Rewrite `calculateMultiLevelCost()` using geometric series.
- Rewrite `calculateMaxAffordableLevels()` using closed-form max-affordable math.
- Preserve return behavior for unaffordable case if UI expects `Pair(1, nextCost)`.
- Clamp max levels if needed for safety.

Verification:

- Add or run the smallest available test if test harness exists.
- Always run `./gradlew :app:compileDebugKotlin`.
- Manually inspect buy multiplier UI still compiles in `UpgradesScreen.kt` / `UpgradeItem` call path.

### Phase 2 — Naming and visibility cleanup

Goal: improve player mental model without changing economy.

Files likely involved:

- `FormatUtils.kt`
- `HeaderSection.kt`
- `TerminalLogLine.kt`
- `UpgradeManager.kt` descriptions
- other UI/log files found by grep

Work:

- Historical note: this phase already concluded that player-facing labels should stay `$FLOPS`.
- Do not revive `FLOPS-CREDS` unless Cory explicitly asks for another naming pass.
- Add/keep storage visibility only where it supports DATAMINER sidecar clarity.
- Reconsider/remove noisy buffer display only if it competes with clearer economy state.

Verification:

- Grep for remaining visible `FLOPS` strings.
- Compile.
- If screenshots/device run is available, inspect header density.

### Phase 3 — Milestone multipliers

Goal: add idle-game “bumps” without adding new systems.

Files:

- `ProductionEngine.kt`
- possibly `UpgradeManager.kt` descriptions/UI if milestone info is displayed

Work:

- Add milestone helper.
- Apply to hardware tier output only at first.
- Do not apply to cooling, power, security, storage, or special ghost/endgame tech unless explicitly chosen later.
- Add small UI copy if needed: milestone thresholds in upgrade item detail.

Verification:

- Compile.
- Create a quick script/table or Kotlin scratch output comparing production at counts `1, 10, 25, 50, 100` for first several hardware tiers.
- Confirm progression is boosted but not instantly stupid.

### Phase 4 — Prestige simulation only

Goal: understand whether migration rewards are stingy before changing them.

Files:

- likely no app code initially
- optional script under `scripts/` or doc table under `docs/`

Work:

- Generate table for `calculatePotentialPersistence()` over FLOPS values.
- Include hard bonus x1.5 and multiplier boost.
- Compare current/current-run behavior vs possible lifetime behavior.

Verification:

- No app compile required if docs/script only, but run compile if app code changes.

Decision gate:

- Cory must explicitly approve any prestige formula change.

## Parking lot

These are good ideas, but not in the first implementation pass:

- Production-loop conversion: hardware grants capacity/lanes/throughput, completed assigned work pays `$FLOPS`.
- Assigned hash queue automation that preserves idle progression without direct hardware wallet faucets.
- System load cleanup: compute/scheduler pressure, not storage and not duplicate heat.
- Derivative daemon/process/thread generator chain.
- Dataset modifiers: encrypted, volatile, compressed.
- Decay nodes and corruption spread.
- Auto-clicker graduation fantasy pass.
- Neural Tokens buying every upgrade — currently rejected unless the whole economy pivots.
- Full market-price rewrite.
- New game built from Faceminer ideas.

## Operating rule for ADHD-safe follow-through

When a new idea appears mid-task:

1. Add it to Parking lot.
2. Finish current phase or stop deliberately.
3. Ask: “swap scope or shelve?”
4. Never silently expand the phase.

The files are the memory. The plan is the rail. No vibes-based rewrites.
