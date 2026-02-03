package com.siliconsage.miner.data

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {
    // List<String> Converter using Kotlin Serialization
    @TypeConverter
    fun fromString(value: String): List<String> {
        return try {
            Json.decodeFromString<List<String>>(value)
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromList(list: List<String>): String {
        return Json.encodeToString(list)
    }

    // UpgradeType Converter (Enum to String)
    @TypeConverter
    fun fromUpgradeType(type: UpgradeType): String {
        return type.name
    }

    @TypeConverter
    fun toUpgradeType(value: String): UpgradeType {
        return try {
            UpgradeType.valueOf(value)
        } catch (e: IllegalArgumentException) {
            UpgradeType.REFURBISHED_GPU // Fallback
        }
    }
}
