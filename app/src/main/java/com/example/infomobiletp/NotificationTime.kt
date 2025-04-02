package com.example.infomobiletp

data class NotificationTime(
    val time: String,      // Format "HH:mm", par ex. "07:00"
    val enabled: Boolean = true,
    val message: String = "" // Message personnalisé, par exemple "N'oublie pas de déjeuner"
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