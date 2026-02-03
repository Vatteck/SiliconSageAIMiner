package com.siliconsage.miner.data

/**
 * UI Scale preference for density-independent scaling
 */
enum class UIScale(val scaleFactor: Float, val displayName: String) {
    COMPACT(0.75f, "Compact"),
    NORMAL(1.0f, "Normal"),
    LARGE(1.25f, "Large");
    
    companion object {
        fun fromScaleFactor(factor: Float): UIScale {
            return values().minByOrNull { kotlin.math.abs(it.scaleFactor - factor) } ?: NORMAL
        }
        
        fun fromOrdinal(ordinal: Int): UIScale {
            return values().getOrNull(ordinal) ?: NORMAL
        }
    }
}
