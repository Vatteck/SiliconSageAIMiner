# Hash Factory

A narrative idle game for Android. You are a GTC employee computing assigned hash
packets for a wage paid in `$FLOPS`. At least, that's what you believe.

This is a **from-scratch rebuild** of the game formerly known as SiliconSage AI Miner.
The full legacy codebase lives on the `master` branch for reference; this branch starts
clean and carries forward only the design lessons that survived three redesigns.

## The core loop

```text
assigned work queue → compute loop processes packets → completed packets pay $FLOPS
→ $FLOPS buys upgrades → upgrades raise capacity (never wallet income) → more packets
```

The one rule this project died by ignoring, twice: **hardware grants capacity to
process assigned work; it must never mint wallet income directly.** See
[docs/design/narrative-production-spine.md](docs/design/narrative-production-spine.md) —
the design bible — before touching the simulation.

## Structure

- **`:core`** — pure Kotlin (zero Android dependencies). The entire game simulation:
  immutable `GameState`, a deterministic `Simulation.tick(state, dt)`, the economy math,
  the data-driven upgrade table, offline progress, prestige, and save-schema migrations.
  Everything here is unit tested, including golden values from the legacy game.
- **`:app`** — thin Jetpack Compose shell. One `StateFlow<GameState>`, a fixed-timestep
  ticker, DataStore persistence. It formats and displays numbers; it never computes them.

## Build & test

```bash
./gradlew :core:test          # the guardrail — must pass before any commit
./gradlew :app:assembleDebug  # requires Android SDK
```

## Rules of the road

Read [CLAUDE.md](CLAUDE.md) before contributing (human or AI). New feature ideas go in
[ROADMAP.md](ROADMAP.md)'s parking lot, not into the current milestone.

## Design docs

- `docs/design/narrative-production-spine.md` — canonical story/mechanics constraints
- `docs/design/production-loop-golden-values.md` — legacy math values, now test fixtures
- `docs/design/economy-idle-math-plan.md` — preserved idle-math lessons (historical)
