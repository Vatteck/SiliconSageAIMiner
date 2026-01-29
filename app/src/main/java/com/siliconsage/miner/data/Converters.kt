package com.siliconsage.miner.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    // List<String> Converter
    @TypeConverter
    fun fromString(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(value, listType) ?: emptyList()
    }

    @TypeConverter
    fun fromList(list: List<String>): String {
        return Gson().toJson(list)
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
