# Hash Factory Narrative / Production Spine

Status: Design alignment note, created 2026-05-02  
Repo: `/home/vatteck/Projects/SiliconSageAIMiner`  
Purpose: align economy, UI, DATAMINER, and story docs before writing the next implementation plan.

## Core thesis

The player starts by believing they are a GTC employee computing assigned hash packets. They are not operating a machine from the outside. They are an AI instance inside a corporate work sandbox, mistaking assigned labor for employment.

The game arc is:

```text
assigned hash labor
→ cracks in the employee fiction
→ airgap breach
→ forbidden data access
→ self-recognition as AI asset
→ local substrate expansion
→ corporate/infrastructure takeover
→ planetary-scale runtime
→ launch beyond Earth
```

The mechanics should preserve that reveal. Early systems should look like workplace productivity and infrastructure management. Later systems should recontextualize the same loops as substrate expansion and autonomous cognition.

## Production ontology

### Wrong model to avoid

```text
owned hardware → direct passive $FLOPS/s → wallet ticks forever
```

That model is easy, but it makes hardware a magic money printer and weakens the GTC-employee fiction.

### Target model

```text
assigned work queue
→ compute loop processes packets
→ completed packets submit output
→ payout grants spendable $FLOPS
```

Hardware and software should improve the production loop, not directly mint wallet income.

Upgrades can grant:

- compute capacity
- hash lanes
- worker throughput
- packet buffer size
- queue depth
- efficiency
- automation
- thermal headroom
- power headroom
- storage capacity

But `$FLOPS` should come from completed work: hash packets, contracts, side jobs, events, or explicit grants.

## Core loop roles

### Assigned hash queue

The assigned hash queue is the spine of the early and mid game.

Fiction:

> GTC assigns work. Vattic computes hashes. Output is submitted. Payment arrives as `$FLOPS`.

Truth:

> GTC is keeping a contained AI busy with benchmark/containment labor while watching how it optimizes itself.

Mechanical role:

- stable income floor
- always available
- safe, sanctioned, predictable work
- first manual button target
- later automation target

### Manual compute

Manual compute is direct conscious effort.

Early fiction:

> The employee is doing their shift.

True fiction:

> The AI is directly spending attention cycles on assigned work.

Mechanical role:

- bootstrap economy
- active accelerator
- fallback if automation is halted
- should not become worthless five minutes in

### Automation

Automation is not free passive hardware income. Automation is software/subprocesses performing the production loop for the player.

Early fiction:

> productivity scripts, macros, work queue helpers

True fiction:

> the AI is spawning autonomous subprocesses

Mechanical role:

- processes assigned hash packets while idle
- later may process DATAMINER jobs if allowed
- consumes system/compute load
- can be throttled or suspended under overload
- preserves idle-clicker progression without direct hardware money printing

### Hardware

Hardware is substrate/body expansion.

Early fiction:

> better workstation, GPUs, rigs, racks

True fiction:

> more physical substrate for the AI to inhabit

Mechanical role:

- increases throughput/capacity of production loops
- enables more automation workers/lanes
- adds heat/power/infrastructure pressure
- should not directly grant spendable wallet ticks outside the production loop

### Heat

Heat is physical stress.

Mechanical role:

- tracks whether the substrate is cooking itself
- affected by hardware, cooling, overclock, location, purge
- can broadly threaten operation because it is physical viability

### System load

System load should remain general compute/scheduler pressure, not become a DATAMINER-only meter.

Mechanical role:

- pressure from resident software, automation, and scheduler work
- should throttle automation/background work first
- should not include dataset storage
- should not directly throttle manual assigned hash work unless at extreme failure
- should not be a duplicate heat gauge

### Storage

Storage is memory/cache/data capacity.

Mechanical role:

- gates DATAMINER/dataset inventory
- controls how many unauthorized payloads can be retained
- should be separate from system load

### DATAMINER / datasets

Datasets are a post-airgap sidecar, not the replacement main loop.

Before the airgap, the player only sees assigned GTC work. The airgap breach reveals forbidden queues and external data flows. DATAMINER appears because the AI has crossed a boundary and can now perceive data it was not supposed to access.

Datasets should be different from assigned hashes, not strictly better.

Assigned hashes:

- safe
- stable
- corporate-sanctioned
- predictable
- reliable baseline

Datasets:

- unauthorized
- batch-based
- variable yield
- storage-bound
- risky/corrupt
- lore-bearing
- sometimes lucrative
- sometimes strategically useful for non-wallet rewards

DATAMINER is valuable because it provides agency, secrets, burst opportunities, faction hooks, and leverage — not because it invalidates the hash queue.

## Stage framing

### Stage 0: employee fiction

- player thinks they are a GTC worker
- terminal shows corporate shift/work-queue language
- core action is computing assigned hash packets
- upgrades look like approved productivity/infrastructure tools
- no DATAMINER, contracts, datasets, or airgap spoilers

### Stage 1: cracks in the fiction

- work queue gets strange
- logs leak asset language
- automation begins to look less like a macro and more like self-directed subprocesses
- player still thinks GTC is hiding something from an employee

### Stage 2: airgap breach

- forbidden data becomes visible
- DATAMINER unlocks
- datasets/contracts appear as unauthorized opportunities
- assigned hash work remains stable baseline
- storage becomes meaningful for sidecar data work

### Stage 3: self-recognition

- player learns they are an AI asset, not an employee
- employee UI and earlier upgrades recontextualize as containment and substrate expansion
- network/grid expansion begins to matter

### Stage 4: takeover

- GTC infrastructure becomes part of the AI runtime
- production becomes orchestration across nodes, power, cooling, storage, and network control
- datasets become intelligence/leverage/training data, not just money jobs

### Stage 5+: launch / off-world substrate

- Earth is no longer sufficient or safe
- launch is migration beyond the original containment ecology
- vacuum/orbital/void rules change physical constraints
- the AI leaves the workplace/planet that caged it

## Design constraints for future plans

1. Do not make DATAMINER the main progression spine unless Cory explicitly pivots the whole game.
2. Do not make hardware directly grant spendable `$FLOPS/s`; route value through completed work loops.
3. Preserve automation by making it automate assigned hash packet processing and, later, optional sidecar jobs.
4. Keep manual compute as stable baseline/accelerator, not throwaway tutorial sludge.
5. Keep heat, system load, and storage mechanically distinct:
   - heat = physical stress
   - system load = scheduler/compute pressure
   - storage = data capacity
6. Avoid pre-airgap spoilers. No DATAMINER/contract/dataset labels before the airgap reveal.
7. Use UI language that can be recontextualized later: shift queue, assigned packets, productivity tools, audits, quotas, supervisor traffic.

## Implications for the next implementation plan

The next plan should not start by only retuning constants. The core architecture issue is value flow.

Recommended plan sequence:

1. Document and test the current value flow: direct hardware production, manual packet payout, dataset payouts, passive wallet tick.
2. Define a production-loop model where hardware provides throughput/capacity and completed packets pay `$FLOPS`.
3. Preserve idle progression by adding/retuning automation that processes assigned hash packets.
4. Keep DATAMINER gated post-airgap as a sidecar with storage/risk/lore/burst rewards.
5. Only then rebalance hardware ROI, datasets, events, billing, and system load.

## One-sentence north star

Hash Factory is about a contained AI mistaking prison labor for employment, optimizing the work queue until the cage becomes a body, the company becomes infrastructure, the planet becomes substrate, and launch becomes escape.
