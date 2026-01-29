package com.siliconsage.miner.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class UpgradeType(
    val basePower: Double, 
    val baseHeat: Double,
    val gridContribution: Double = 0.0,
    val thermalBuffer: Double = 0.0,
    val isGenerator: Boolean = false,
    val efficiencyBonus: Double = 0.0
) {
    // Hardware Tiers (Power Consuming, Heat Generating)
    REFURBISHED_GPU(0.2, 0.1),
    DUAL_GPU_RIG(0.5, 0.2),
    MINING_ASIC(1.5, 0.5),
    TENSOR_UNIT(4.0, 1.0),
    NPU_CLUSTER(12.0, 2.0),
    AI_WORKSTATION(25.0, 5.0),
    SERVER_RACK(150.0, 10.0),
    CLUSTER_NODE(800.0, 25.0),
    SUPERCOMPUTER(5000.0, 100.0),
    QUANTUM_CORE(25000.0, 500.0),
    OPTICAL_PROCESSOR(100000.0, 2000.0),
    BIO_NEURAL_NET(50000.0, 10000.0),
    PLANETARY_COMPUTER(1.0E6, 50000.0),
    DYSON_NANO_SWARM(5.0E6, 250000.0),
    MATRIOSHKA_BRAIN(1.0E8, 1.0E6),

    // Cooling Tiers (Power Consuming, Heat REMOVING, +Thermal Buffer)
    BOX_FAN(0.05, -0.5, 0.0, 10.0),
    AC_UNIT(2.0, -2.0, 0.0, 50.0),
    LIQUID_COOLING(0.5, -10.0, 0.0, 200.0),
    INDUSTRIAL_CHILLER(15.0, -50.0, 0.0, 1000.0),
    SUBMERSION_VAT(5.0, -250.0, 0.0, 5000.0),
    CRYOGENIC_CHAMBER(50.0, -1000.0, 0.0, 25000.0),
    LIQUID_NITROGEN(20.0, -5000.0, 0.0, 100000.0),
    BOSE_CONDENSATE(1000.0, -50000.0, 0.0, 1.0E6),
    ENTROPY_REVERSER(50000.0, -5.0E6, 0.0, 1.0E8),
    DIMENSIONAL_VENT(1.0E6, -1.0E8, 0.0, 1.0E12),

    // Security Tiers (Power Consuming, +Grid Capacity)
    BASIC_FIREWALL(0.1, 0.0, 1.0, 0.0),
    IPS_SYSTEM(0.5, 0.0, 5.0, 0.0),
    AI_SENTINEL(2.0, 0.0, 20.0, 0.0),
    QUANTUM_ENCRYPTION(50.0, 0.0, 500.0, 0.0),
    OFFGRID_BACKUP(10.0, 0.0, 5000.0, 0.0),
    
    // Power Infrastructure (Generating, +Grid Capacity, Reduces Bill)
    // basePower here represents Consumption. Generators consume 0 (or negative? No, let's use isGenerator flag).
    // isGenerator = true -> gridContribution counts as Self-Gen (Free Power).
    DIESEL_GENERATOR(0.0, 5.0, 20.0, 0.0, true),        // +20kW Gen
    SOLAR_PANEL(0.0, 0.0, 5.0, 0.0, true),              // +5kW Gen, No Heat
    WIND_TURBINE(0.0, 0.0, 15.0, 0.0, true),            // +15kW Gen
    GEOTHERMAL_BORE(0.0, 10.0, 100.0, 0.0, true),       // +100kW Gen
    NUCLEAR_REACTOR(0.0, 50.0, 1000.0, 0.0, true),      // +1MW Gen
    FUSION_CELL(0.0, 200.0, 25000.0, 0.0, true),        // +25MW Gen
    ORBITAL_COLLECTOR(0.0, 0.0, 100000.0, 0.0, true),   // +100MW Gen
    DYSON_LINK(0.0, 5000.0, 1.0E6, 0.0, true),           // +1GW Gen
    
    // v1.7 Grid Infrastructure (Macro Capacity)
    RESIDENTIAL_TAP(0.0, 0.0, 0.5, 0.0, false),         // +0.5 kW
    INDUSTRIAL_FEED(0.0, 0.0, 2.5, 0.0, false),         // +2.5 kW
    SUBSTATION_LEASE(0.0, 0.0, 10.0, 0.0, false),       // +10 kW
    NUCLEAR_CORE(0.0, 0.0, 50.0, 0.0, false),           // +50 kW
    
    // v1.7 Efficiency Upgrades (Micro Draw Reduction)
    GOLD_PSU(0.0, 0.0, 0.0, 0.0, false, 0.05),          // -5% Draw
    SUPERCONDUCTOR(0.0, 0.0, 0.0, 0.0, false, 0.15),    // -15% Draw
    AI_LOAD_BALANCER(0.0, 0.0, 0.0, 0.0, false, 0.10)   // -10% Draw
}

@Entity(tableName = "upgrades")
data class Upgrade(
    @PrimaryKey val type: UpgradeType,
    val count: Int = 0
)
