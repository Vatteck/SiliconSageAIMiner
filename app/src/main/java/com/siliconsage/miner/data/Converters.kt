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

    // Map<String, Float> Converter (v2.9.29)
    @TypeConverter
    fun fromMapStringFloat(value: Map<String, Float>): String {
        return Json.encodeToString(value)
    }

    @TypeConverter
    fun toMapStringFloat(value: String): Map<String, Float> {
        return try {
            Json.decodeFromString<Map<String, Float>>(value)
        } catch (e: Exception) {
            emptyMap()
        }
    }

    // Set<String> Converter (v2.9.69)
    @TypeConverter
    fun fromSet(value: Set<String>): String {
        return Json.encodeToString(value)
    }

    @TypeConverter
    fun toSet(value: String): Set<String> {
        return try {
            Json.decodeFromString<Set<String>>(value)
        } catch (e: Exception) {
            emptySet()
        }
    }
}
