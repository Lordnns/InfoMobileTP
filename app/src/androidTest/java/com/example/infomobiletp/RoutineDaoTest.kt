package com.example.infomobiletp

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.runner.RunWith
import com.example.infomobiletp.RecurrenceType


@RunWith(AndroidJUnit4::class)
class RoutineDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: RoutineDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries() // pour les tests uniquement
            .build()
        dao = database.routineDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAndGetRoutine() = runBlocking {
        val routine = Routine(
            name = "Test Routine",
            description = "Test Description",
            category = "Test Category",
            daysOfWeek = listOf(2, 4),
            recurrenceType = RecurrenceType.WEEKLY,
            startDate = System.currentTimeMillis(),
            endDate = null,
            priority = Priority.HIGH,
            notificationTimes = listOf(
                NotificationTime("08:00", true, "Test Message")
            )
        )

        val id = dao.insert(routine)
        val routines = dao.getAll()

        Assert.assertEquals(1, routines.size)
        Assert.assertEquals("Test Routine", routines[0].name)
        Assert.assertEquals(id.toInt(), routines[0].id)
    }

    @Test
    fun deleteRoutine() = runBlocking {
        val routine = Routine(
            name = "To be deleted",
            description = "Delete me",
            category = "Test",
            daysOfWeek = listOf(1),
            recurrenceType = RecurrenceType.WEEKLY,
            startDate = System.currentTimeMillis(),
            endDate = null,
            priority = Priority.LOW,
            notificationTimes = emptyList()
        )

        val id = dao.insert(routine)
        val inserted = dao.getAll().first()
        dao.delete(inserted)

        val afterDelete = dao.getAll()
        Assert.assertTrue(afterDelete.isEmpty())
    }

    @Test
    fun updateRoutine() = runBlocking {
        val routine = Routine(
            name = "Original",
            description = "Desc",
            category = "Cat",
            daysOfWeek = listOf(3),
            recurrenceType = RecurrenceType.WEEKLY,
            startDate = System.currentTimeMillis(),
            endDate = null,
            priority = Priority.MEDIUM,
            notificationTimes = emptyList()
        )

        val id = dao.insert(routine)
        val updated = routine.copy(id = id.toInt(), name = "Updated")
        dao.update(updated)

        val routines = dao.getAll()
        Assert.assertEquals("Updated", routines.first().name)
    }
}
