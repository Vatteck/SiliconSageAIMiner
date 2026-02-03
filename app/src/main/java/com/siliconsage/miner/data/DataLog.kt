package com.siliconsage.miner.data

/**
 * Represents a collectible lore fragment that unlocks at specific milestones
 */
data class DataLog(
    val id: String,
    val title: String,
    val content: String,
    val unlockCondition: UnlockCondition,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long? = null // Timestamp when unlocked
)

sealed class UnlockCondition {
    object Instant : UnlockCondition() // Unlocks immediately on game start
    data class ReachFLOPS(val threshold: Double) : UnlockCondition()
    data class ReachRank(val rank: Int) : UnlockCondition()
    data class CompleteEvent(val eventId: String, val choiceId: String) : UnlockCondition()
    data class ReceiveRivalMessages(val source: RivalSource, val count: Int) : UnlockCondition()
    data class StoryStageReached(val stage: Int) : UnlockCondition()
    object NullActive : UnlockCondition() // Requires Null to be active
    object Victory : UnlockCondition()
}
