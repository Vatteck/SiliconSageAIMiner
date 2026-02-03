package com.siliconsage.miner.data

import kotlinx.serialization.Serializable

/**
 * Represents a multi-part narrative dilemma chain
 * Tracks progression through faction-specific storylines
 */
@Serializable
data class DilemmaChain(
    val chainId: String,
    val currentPartId: String?,
    val completedParts: List<String>,
    val choicesMade: Map<String, String>, // partId to choiceId
    val scheduledNextPart: ScheduledPart? = null
)

@Serializable
data class ScheduledPart(
    val partId: String,
    val triggerTime: Long // Unix timestamp when this part should trigger
)
