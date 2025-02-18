package com.example.infomobiletp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Routine(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val time: String,
    val daysOfWeek: List<Int>
)