package com.example.infomobiletp

import androidx.room.*

@Dao
interface RoutineDao {
    @Query("SELECT * FROM routine")
    fun getAll(): List<Routine>

    @Insert
    fun insert(routine: Routine): Long

    @Delete
    fun delete(routine: Routine)

    @Update
    fun update(routine: Routine)
}