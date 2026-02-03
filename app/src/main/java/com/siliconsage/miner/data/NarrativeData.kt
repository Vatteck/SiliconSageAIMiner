package com.siliconsage.miner.data

import androidx.compose.ui.graphics.Color
import com.siliconsage.miner.viewmodel.GameViewModel

// Represents a specific choice within a dilemma
data class NarrativeChoice(
    val id: String,
    val text: String,
    val description: String = "",
    val color: Color,
    val effect: (GameViewModel) -> Unit,
    val nextPartId: String? = null, // If choice leads to next part of chain
    val nextPartDelayMs: Long = 0, // Delay before next part triggers (0 = immediate)
    val condition: (GameViewModel) -> Boolean = { true }
)

// Represents the Dilemma event itself
data class NarrativeEvent(
    val id: String,
    val title: String,
    val description: String,
    val choices: List<NarrativeChoice>,
    val condition: (GameViewModel) -> Boolean = { true },
    val isStoryEvent: Boolean = false, // Added for tracking major story events
    val isOneTime: Boolean = true, // Default: trigger only once per playthrough
    val chainId: String? = null, // If part of a chain, the chain identifier
    val isChainStart: Boolean = false, // True if this is the first event in a chain
    val expiresAt: Long? = null // Optional implementation for timed events later
)
