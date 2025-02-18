package com.example.infomobiletp

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Routine::class], version = 5)
@TypeConverters(Converters::class)
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
                    .addCallback(DatabaseCallback(context)) // Add database callback
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(private val context: Context) : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            // Insert initial data when the database is created
            CoroutineScope(Dispatchers.IO).launch {

                INSTANCE?.routineDao()?.apply {
                    insert(Routine(name = "Morning Workout", description = "30 mins of cardio", time = "06:30 AM", daysOfWeek = listOf(2,3,4,5,6)))
                    insert(Routine(name = "Team Meeting", description = "Daily stand-up", time = "09:00 AM", daysOfWeek = listOf(2,3,4,5,6)))
                    insert(Routine(name = "Lunch Break", description = "Healthy meal", time = "12:00 PM", daysOfWeek = listOf(1,2,3,4,5,6,7)))
                }
            }
        }
    }
}