# Production Loop Golden Values

Status: Baseline audit for production-loop conversion Task 1  
Created: 2026-05-02  
Scope: current direct production behavior only; no Kotlin code changed.

## Purpose

This document records the current direct hardware production outputs before the production-loop refactor. Later tasks can use these values to prove behavior parity before any intentional balance changes.

## Current direct hardware formula

`ProductionEngine.calculateFlopsRate()` currently accumulates hardware production with:

```text
level * baseProduction * 2^(level / 25)
```

Important Kotlin behavior: `level / 25` is integer division because `level` is an `Int`. The milestone exponent therefore advances only at whole 25-level thresholds:

- levels `1..24` use exponent `0` and multiplier `1x`
- levels `25..49` use exponent `1` and multiplier `2x`
- levels `50..74` use exponent `2` and multiplier `4x`

## Golden direct production values

These values are for one upgrade type at the listed level/count, before grid, faction, prestige, heat, offline, event, system-load, or other external multipliers.

| Upgrade type | Upgrade level/count | Base production | Milestone multiplier | Current direct `$FLOPS/s` output |
| --- | ---: | ---: | ---: | ---: |
| `REFURBISHED_GPU` | 1 | 2.0 | 1x | 2.0 |
| `REFURBISHED_GPU` | 24 | 2.0 | 1x | 48.0 |
| `REFURBISHED_GPU` | 25 | 2.0 | 2x | 100.0 |
| `DUAL_GPU_RIG` | 1 | 8.0 | 1x | 8.0 |
| `MINING_ASIC` | 1 | 35.0 | 1x | 35.0 |
| `SERVER_RACK` | 1 | 25_000.0 | 1x | 25_000.0 |
| `QUANTUM_CORE` | 1 | 10_000_000.0 | 1x | 10_000_000.0 |
| `BIO_NEURAL_NET` | 1 | 800_000_000.0 | 1x | 800_000_000.0 |

## Current 100ms wallet tick behavior

As of this baseline:

1. `GameViewModel.startLoops()` runs the production loop every 100ms.
2. That loop calls `ResourceEngine.calculatePassiveIncomeTick(flopsProductionRate, ...)`.
3. `ResourceEngine.calculatePassiveIncomeTick()` returns `flopsDelta = flopsPerSec / 10.0`, with a `4x` multiplier while `systemCollapseTimer` is active. `GameViewModel.startLoops()` currently passes `null`, so that `4x` path is not active in the main 100ms loop.
4. `GameViewModel` adds `flopsDelta` directly to the wallet via `flops.update { it + res.flopsDelta }`.

This is the direct passive wallet-faucet behavior that later production-loop tasks are intended to wrap and then replace with assigned hash packet completion payouts while preserving numeric parity first.

## Source audit references

- `app/src/main/java/com/siliconsage/miner/domain/engine/ProductionEngine.kt`
  - `calculateFlopsRate()` accumulates hardware production and multipliers.
  - `calculateHardwareProduction()` contains the current milestone formula.
- `app/src/main/java/com/siliconsage/miner/domain/engine/ResourceEngine.kt`
  - `calculatePassiveIncomeTick()` currently converts `$FLOPS/s` into a 100ms wallet delta by dividing by `10.0`.
- `app/src/main/java/com/siliconsage/miner/viewmodel/GameViewModel.kt`
  - `startLoops()` currently invokes `calculatePassiveIncomeTick(...)` every 100ms and adds `res.flopsDelta` directly to `flops`.

## Code-change note

No Kotlin code was changed for this baseline audit. This file is documentation only.
