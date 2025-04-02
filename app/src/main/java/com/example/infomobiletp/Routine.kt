package com.example.infomobiletp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Routine(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,                // Routine title (ex: "Sport du matin")
    val description: String,         // Optional details
    val category: String,            // e.g., "travail", "loisir", "santé"
    val daysOfWeek: List<Int>,       // Repeating days (existing system)
    val recurrenceType: RecurrenceType, // Using enum: WEEKLY, BIWEEKLY, MONTHLY, ONCE
    val startDate: Long,             // Start date in milliseconds (prefilled with today's date)
    val endDate: Long?,              // End date (if null, run indefinitely)
    val priority: Priority,          // Priority: HIGH, MEDIUM, LOW
    val notificationTimes: List<NotificationTime>,  // Notification settings
    val notificationsEnabled: Boolean = true        // Global flag
)