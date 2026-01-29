package com.siliconsage.miner.data

data class TechTreeRoot(
    val tech_tree: List<TechNode>
)

data class TechNode(
    val id: String,
    val name: String,
    val description: String,
    val cost: Double,
    val multiplier: Double, // Global multiplier bonus (e.g., 0.1 for +10%)
    val requires: List<String>
)
