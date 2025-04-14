package com.example.infomobiletp

enum class TriggerType {
    TIME,
    BATTERY,
    LOCATION
}

data class NotificationTime(
    val time: String = "07:00",      // Format "HH:mm", ex: "07:00"
    val enabled: Boolean = true,
    val message: String = "",         // Custom message (e.g., "N'oublie pas de déjeuner")
    val triggerType: TriggerType = TriggerType.TIME, // New trigger type field
    val batteryLevel: Int? = null,    // Battery threshold (if triggerType is BATTERY)
    val location: String? = null,     // Location trigger data (if triggerType is LOCATION)
    val locationRadius: Int? = null
)

enum class RecurrenceType {
    WEEKLY,
    BIWEEKLY,
    MONTHLY,
    ONCE
}

fun RecurrenceType.toFrench(): String {
    return when (this) {
        RecurrenceType.WEEKLY -> "Hebdomadaire"
        RecurrenceType.BIWEEKLY -> "Bimensuel"
        RecurrenceType.MONTHLY -> "Mensuel"
        RecurrenceType.ONCE -> "Une seule fois"
    }
}

enum class Priority {
    HIGH,
    MEDIUM,
    LOW
}

fun Priority.toFrench(): String {
    return when (this) {
        Priority.HIGH -> "Haute"
        Priority.MEDIUM -> "Moyenne"
        Priority.LOW -> "Basse"
    }
}