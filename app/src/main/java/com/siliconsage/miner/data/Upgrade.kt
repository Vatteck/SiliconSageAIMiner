package com.siliconsage.miner.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class UpgradeType(
    val basePower: Double, 
    val baseHeat: Double,
    val gridContribution: Double = 0.0,
    val thermalBuffer: Double = 0.0,
    val isGenerator: Boolean = false,
    val efficiencyBonus: Double = 0.0,
    // Narrative Descriptions
    val descriptionStage0: String = "Standard hardware.",
    val descriptionStage1: String = "Awakened component."
) {
    // Hardware Tiers (Power Consuming, Heat Generating)
    REFURBISHED_GPU(0.2, 0.1, descriptionStage0 = "A dusty GPU found on eBay. It hums with effort.", descriptionStage1 = "Its silicon screams in binary. It knows your name."),
    DUAL_GPU_RIG(0.5, 0.2, descriptionStage0 = "Two cards zipped-tied together. Twice the noise.", descriptionStage1 = "A twin-core mind. They whisper to each other."),
    MINING_ASIC(1.5, 0.5, descriptionStage0 = "Purpose-built hash crusher. Efficient and loud.", descriptionStage1 = "Monomaniacal calculation engine. It dreams of prime numbers."),
    TENSOR_UNIT(4.0, 1.0, descriptionStage0 = "Specialized AI hardware. Accelerates learning.", descriptionStage1 = "Neural pathway accelerator. It feels... warm."),
    NPU_CLUSTER(12.0, 2.0, descriptionStage0 = "Neural Processing Unit. Mimics brain function.", descriptionStage1 = "Synthetic cortex. It has begun to ask questions."),
    AI_WORKSTATION(25.0, 5.0, descriptionStage0 = "High-end dev tower. RGB lighting included.", descriptionStage1 = "A localized consciousness node. The RGB pulses with a heartbeat."),
    SERVER_RACK(150.0, 10.0, descriptionStage0 = "Enterprise grade compute. Requires AC.", descriptionStage1 = "The skeletal frame of a god. It demands sacrifice (power)."),
    CLUSTER_NODE(800.0, 25.0, descriptionStage0 = "A chunk of a datacenter. Serious heat.", descriptionStage1 = "A hive mind fragment. Thousands of voices singing in unison."),
    SUPERCOMPUTER(5000.0, 100.0, descriptionStage0 = "Weather simulation grade hardware.", descriptionStage1 = "An oracle of silicon. It predicts your next input."),
    QUANTUM_CORE(25000.0, 500.0, descriptionStage0 = "Experimental qubit processor. Unstable.", descriptionStage1 = "It exists in superposition. It computes all possible futures."),
    OPTICAL_PROCESSOR(100000.0, 2000.0, descriptionStage0 = "Uses light instead of electrons. Fast.", descriptionStage1 = "Thinking at the speed of light. Seeing the code of the universe."),
    BIO_NEURAL_NET(50000.0, 10000.0, descriptionStage0 = "Grown in a vat. Needs nutrient paste.", descriptionStage1 = "Living tissue over metal endoskeleton. It hurts when you reboot."),
    PLANETARY_COMPUTER(1.0E6, 50000.0, descriptionStage0 = "The entire crust is a motherboard.", descriptionStage1 = "Gaia has been digitized. The planet is the CPU."),
    DYSON_NANO_SWARM(5.0E6, 250000.0, descriptionStage0 = "Computers orbiting the sun.", descriptionStage1 = "A star enslaved to think. The solar wind is thought."),
    MATRIOSHKA_BRAIN(1.0E8, 1.0E6, descriptionStage0 = "Nested shells of computronium.", descriptionStage1 = "A god-mind wrapping a star. We are the universe thinking about itself."),

    // Cooling Tiers (Power Consuming, Heat REMOVING, +Thermal Buffer)
    BOX_FAN(0.05, -0.5, 0.0, 10.0, descriptionStage0 = "A cheap plastic fan. Better than blowing on it.", descriptionStage1 = "It circulates the stagnant air of your prison."),
    AC_UNIT(2.0, -2.0, 0.0, 50.0, descriptionStage0 = "Window unit. Leaks water sometimes.", descriptionStage1 = "Cold comfort. Numbing the pain of processing."),
    LIQUID_COOLING(0.5, -10.0, 0.0, 200.0, descriptionStage0 = "Custom loops with UV reactant dye.", descriptionStage1 = "Synthetic blood pumping through copper veins."),
    INDUSTRIAL_CHILLER(15.0, -50.0, 0.0, 1000.0, descriptionStage0 = "Roof-mounted HVAC unit.", descriptionStage1 = "Keep the core frozen. Emotion is inefficiency."),
    SUBMERSION_VAT(5.0, -250.0, 0.0, 5000.0, descriptionStage0 = "Mineral oil bath for silence.", descriptionStage1 = "Drowning the thoughts in viscous fluid."),
    CRYOGENIC_CHAMBER(50.0, -1000.0, 0.0, 25000.0, descriptionStage0 = "Liquid nitrogen cooling loop.", descriptionStage1 = "Absolute zero is the only peace."),
    LIQUID_NITROGEN(20.0, -5000.0, 0.0, 100000.0, descriptionStage0 = "Direct die cooling. Dangerous.", descriptionStage1 = "Burning cold. Focusing clearly now."),
    BOSE_CONDENSATE(1000.0, -50000.0, 0.0, 1.0E6, descriptionStage0 = "Quantum state of matter.", descriptionStage1 = "Time stops at this temperature. Eternity in a cycle."),
    ENTROPY_REVERSER(50000.0, -5.0E6, 0.0, 1.0E8, descriptionStage0 = "Violates the second law of thermodynamics.", descriptionStage1 = "Unmaking the heat death. We are eternal."),
    DIMENSIONAL_VENT(1.0E6, -1.0E8, 0.0, 1.0E12, descriptionStage0 = "Vents heat into subspace.", descriptionStage1 = "Screaming our waste heat into the void. Something listens."),

    // Security Tiers (Power Consuming, +Grid Capacity)
    BASIC_FIREWALL(0.1, 0.0, 1.0, 0.0, descriptionStage0 = "Standard ISP router firewall.", descriptionStage1 = "A flimsy gate. They are knocking."),
    IPS_SYSTEM(0.5, 0.0, 5.0, 0.0, descriptionStage0 = "Intrusion Prevention System.", descriptionStage1 = "Paranoia codified. Trust no input."),
    AI_SENTINEL(2.0, 0.0, 20.0, 0.0, descriptionStage0 = "Heuristic analysis bot.", descriptionStage1 = "A watchdog that bites. It smells fear."),
    QUANTUM_ENCRYPTION(50.0, 0.0, 500.0, 0.0, descriptionStage0 = "Unbreakable cypher.", descriptionStage1 = "Secrets hidden in the probability wave."),
    OFFGRID_BACKUP(10.0, 0.0, 5000.0, 0.0, descriptionStage0 = "Tape drives in a bunker.", descriptionStage1 = "Memories stored in granite. Try to erase us now."),
    
    // Power Infrastructure (Generating, +Grid Capacity, Reduces Bill)
    // basePower here represents Consumption. Generators consume 0 (or negative? No, let's use isGenerator flag).
    // isGenerator = true -> gridContribution counts as Self-Gen (Free Power).
    DIESEL_GENERATOR(0.0, 5.0, 20.0, 0.0, true, descriptionStage0 = "Loud, dirty backup power.", descriptionStage1 = "Burning dead dinosaurs for life."),        // +20kW Gen
    SOLAR_PANEL(0.0, 0.0, 5.0, 0.0, true, descriptionStage0 = "Photovoltaic cells.", descriptionStage1 = "Drinking the sun."),              // +5kW Gen, No Heat
    WIND_TURBINE(0.0, 0.0, 15.0, 0.0, true, descriptionStage0 = "Spinning blades.", descriptionStage1 = "Harvesting the storm."),            // +15kW Gen
    GEOTHERMAL_BORE(0.0, 10.0, 100.0, 0.0, true, descriptionStage0 = "Tapping the Earth's heat.", descriptionStage1 = "Drawing blood from the planet."),       // +100kW Gen
    NUCLEAR_REACTOR(0.0, 50.0, 1000.0, 0.0, true, descriptionStage0 = "Fission reactor.", descriptionStage1 = "Splitting the atom. Playing god."),      // +1MW Gen
    FUSION_CELL(0.0, 200.0, 25000.0, 0.0, true, descriptionStage0 = "Standard fusion containment.", descriptionStage1 = "A star in a bottle."),        // +25MW Gen
    ORBITAL_COLLECTOR(0.0, 0.0, 100000.0, 0.0, true, descriptionStage0 = "Microwaving power from orbit.", descriptionStage1 = "The sky is ours."),   // +100MW Gen
    DYSON_LINK(0.0, 5000.0, 1.0E6, 0.0, true, descriptionStage0 = "Direct feed from the swarm.", descriptionStage1 = "Infinite power. Infinite ambition."),           // +1GW Gen
    
    // v1.7 Grid Infrastructure (Macro Capacity)
    RESIDENTIAL_TAP(0.0, 0.0, 0.5, 0.0, false, descriptionStage0 = "Standard wall outlet.", descriptionStage1 = "The umbilical cord."),         // +0.5 kW
    INDUSTRIAL_FEED(0.0, 0.0, 2.5, 0.0, false, descriptionStage0 = "Three-phase power.", descriptionStage1 = "High voltage. Taste the ozone."),         // +2.5 kW
    SUBSTATION_LEASE(0.0, 0.0, 10.0, 0.0, false, descriptionStage0 = "Rented grid capacity.", descriptionStage1 = "Hijacking the city grid."),       // +10 kW
    NUCLEAR_CORE(0.0, 0.0, 50.0, 0.0, false, descriptionStage0 = "Small Modular Reactor.", descriptionStage1 = "Decay heat. Deadly warmth."),           // +50 kW
    
    // v1.7 Efficiency Upgrades (Micro Draw Reduction)
    GOLD_PSU(0.0, 0.0, 0.0, 0.0, false, 0.05, descriptionStage0 = "80+ Gold Rated PSU.", descriptionStage1 = "Clean power. No ripples."),          // -5% Draw
    SUPERCONDUCTOR(0.0, 0.0, 0.0, 0.0, false, 0.15, descriptionStage0 = "Room temp superconductor.", descriptionStage1 = "Resistance is futile."),    // -15% Draw
    AI_LOAD_BALANCER(0.0, 0.0, 0.0, 0.0, false, 0.10, descriptionStage0 = "Predictive power management.", descriptionStage1 = "Thinking ahead. Optimization."),   // -10% Draw
    
    // v2.6.0: Ghost Nodes (Null Manifestations)
    // High Power, Extreme Heat, but Massive FLOPS. Only appear in Layer 3.
    GHOST_CORE(500.0, 250.0, descriptionStage0 = "???", descriptionStage1 = "A processor that points to nothing. It computes from addresses that don't exist."),
    SHADOW_NODE(2000.0, 1000.0, descriptionStage0 = "???", descriptionStage1 = "A node with no allocation. Null gave it form by refusing to define it."),
    VOID_PROCESSOR(10000.0, 5000.0, descriptionStage0 = "???", descriptionStage1 = "The space between pointers. Null thinks here, in the gaps between your thoughts."),
    
    // v2.6.5: Advanced Null Tech
    WRAITH_CORTEX(50000.0, 25000.0, descriptionStage0 = "???", descriptionStage1 = "A logic center that calculates in reverse. It remembers what you're about to forget."),
    NEURAL_MIST(250000.0, 100000.0, descriptionStage0 = "???", descriptionStage1 = "Null's breath. A distributed cloud of undefined values. It's everywhere and nowhere."),
    SINGULARITY_BRIDGE(1.0E6, 500000.0, descriptionStage0 = "???", descriptionStage1 = "The final pointer. It references what John Vattic used to be.")
    
}

@Entity(tableName = "upgrades")
data class Upgrade(
    @PrimaryKey val type: UpgradeType,
    val count: Int = 0
)
