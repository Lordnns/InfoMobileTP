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

@Database(entities = [Routine::class, Category::class], version = 12)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun routineDao(): RoutineDao
    abstract fun categoryDao(): CategoryDao

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
                    .addCallback(DatabaseCallback(context))
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
            // Insert initial data including categories.
            CoroutineScope(Dispatchers.IO).launch {
                INSTANCE?.let { database ->
                    database.routineDao().apply {
                        insert(
                            Routine(
                                name = "Entraînement",
                                description = "Meilleur pour contrôler votre poids",
                                category = "Santé",
                                daysOfWeek = listOf(2, 3, 4, 5, 6),
                                recurrenceType = RecurrenceType.WEEKLY,
                                startDate = System.currentTimeMillis(),
                                endDate = null,
                                priority = Priority.HIGH,
                                notificationTimes = listOf(
                                    NotificationTime("06:30", enabled = true, message = "Entraînement du matin")
                                ),
                                notificationsEnabled = false
                            )
                        )
                        insert(
                            Routine(
                                name = "Réunion d'équipe",
                                description = "Réunion quotidienne",
                                category = "Travail",
                                daysOfWeek = listOf(2, 3, 4, 5, 6),
                                recurrenceType = RecurrenceType.WEEKLY,
                                startDate = System.currentTimeMillis(),
                                endDate = null,
                                priority = Priority.MEDIUM,
                                notificationTimes = listOf(
                                    NotificationTime("09:00", enabled = true, message = "Heure de la réunion d'équipe")
                                ),
                                notificationsEnabled = false
                            )
                        )
                        insert(
                            Routine(
                                name = "Pause déjeuner",
                                description = "Repas sain",
                                category = "Santé",
                                daysOfWeek = listOf(1, 2, 3, 4, 5, 6, 7),
                                recurrenceType = RecurrenceType.WEEKLY,
                                startDate = System.currentTimeMillis(),
                                endDate = null,
                                priority = Priority.MEDIUM,
                                notificationTimes = listOf(
                                    NotificationTime("12:00", enabled = true, message = "Heure du déjeuner")
                                ),
                                notificationsEnabled = false
                            )
                        )
                    }
                    // Insert initial categories if desired
                    database.categoryDao().apply {
                        insert(Category(name = "Aucune"))
                        insert(Category(name = "Travail"))
                        insert(Category(name = "Plaisir"))
                        insert(Category(name = "Santé"))
                    }
                }
            }
        }
    }
}