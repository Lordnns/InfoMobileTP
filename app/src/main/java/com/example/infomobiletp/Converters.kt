package com.example.infomobiletp

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromList(days: List<Int>?): String {
        return days?.joinToString(",") ?: ""
    }

    @TypeConverter
    fun toList(data: String?): List<Int> {
        return data?.split(",")?.mapNotNull { it.toIntOrNull() } ?: emptyList()
    }
}
