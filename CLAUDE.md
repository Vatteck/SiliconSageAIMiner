# Hash Factory — Rules for AI and human contributors

This repo is a rebuild of a project that collapsed under scope creep and parallel
systems. These rules exist to prevent a repeat. They are short. Follow them.

1. **One economy spine.** All `$FLOPS` enter the wallet through packet completion in
   `Simulation.tick` (or an explicit, event-logged grant). Never add a second income
   tick. If a feature "also produces flops", make it produce *packets or capacity*
   instead. Hardware never mints wallet income directly — see
   `docs/design/narrative-production-spine.md`.

2. **`:core` is Android-free.** No `android.*` or `androidx.*` imports in `:core`.
   Its only runtime dependency is `kotlinx-serialization-json`. Do not add
   dependencies to `:core` without updating this file.

3. **State is one immutable data class.** A new feature means new fields on
   `GameState` + tick logic + tests. Never a parallel store, never a
   `MutableStateFlow` per field in the ViewModel.

4. **No state field without a save story.** Every `GameState` change either has a
   serialization default or a `SaveCodec.migrate` step plus a frozen-fixture test.
   Destructive save fallback (wiping player saves on schema change) is forbidden.

5. **Features are data.** New upgrades are rows in `UpgradeDefs`. If you are writing
   `when (upgradeId)`, stop and add a numeric column instead.

6. **Numbers live in `GameConfig` and `UpgradeDefs`,** never as inline literals in
   sim code. Balance changes are evaluated with the `BalanceSmokeTest` /
   `PrestigeTableTest` output tables, not vibes.

7. **Scope rail.** New ideas go to the parking lot in `ROADMAP.md`. Never silently
   expand the current milestone. The three meters — heat (physical stress), system
   load (scheduler pressure), storage (data capacity) — stay mechanically distinct,
   and only exist once their mechanic exists.

8. **Gate:** `./gradlew :core:test` must pass before any commit.
   `:app:assembleDebug` must pass before any push (when an Android SDK is available).
