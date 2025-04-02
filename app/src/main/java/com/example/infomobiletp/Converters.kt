package com.example.infomobiletp

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromRecurrenceType(value: RecurrenceType): String = value.name

    @TypeConverter
    fun toRecurrenceType(value: String): RecurrenceType = RecurrenceType.valueOf(value)

    @TypeConverter
    fun fromPriority(value: Priority): String = value.name

    @TypeConverter
    fun toPriority(value: String): Priority = Priority.valueOf(value)

    @TypeConverter
    fun fromNotificationTimeList(times: List<NotificationTime>?): String {
        return if (times.isNullOrEmpty()) "" else com.google.gson.Gson().toJson(times)
    }

    @TypeConverter
    fun toNotificationTimeList(data: String?): List<NotificationTime> {
        if (data.isNullOrEmpty()) return emptyList()
        val listType = object : com.google.gson.reflect.TypeToken<List<NotificationTime>>() {}.type
        return com.google.gson.Gson().fromJson(data, listType)
    }

    @TypeConverter
    fun fromList(days: List<Int>?): String {
        return days?.joinToString(",") ?: ""
    }

    @TypeConverter
    fun toList(data: String?): List<Int> {
        return data?.split(",")?.mapNotNull { it.toIntOrNull() } ?: emptyList()
    }
}
