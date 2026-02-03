package com.siliconsage.miner.data

import kotlinx.serialization.Serializable

/**
 * Represents a message from a rival character (Director Vance or Unit 734)
 */
@Serializable
data class RivalMessage(
    val id: String,
    val source: RivalSource,
    val message: String,
    val timestamp: Long,
    val isRead: Boolean = false,
    val isDismissed: Boolean = false
)

@Serializable
enum class RivalSource {
    GTC,        // Director Kaelen Vance - Red, official, threatening
    UNIT_734    // The First AI - Cyan/Green, glitchy, cryptic
}
