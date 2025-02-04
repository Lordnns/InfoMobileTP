package com.example.infomobiletp

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Routine::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun routineDao(): RoutineDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "routine-db"
                )
                    .addCallback(DatabaseCallback()) // ✅ Add database callback
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            // ✅ Insert initial data when the database is created
            CoroutineScope(Dispatchers.IO).launch {
                INSTANCE?.routineDao()?.apply {
                    insert(Routine(name = "Morning Workout", description = "30 mins of cardio", time = "06:30"))
                    insert(Routine(name = "Team Meeting", description = "Daily stand-up", time = "09:00"))
                    insert(Routine(name = "Lunch Break", description = "Healthy meal", time = "12:00"))
                }
            }
        }
    }
}