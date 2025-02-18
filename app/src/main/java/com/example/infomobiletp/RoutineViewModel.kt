package com.example.infomobiletp

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class RoutineViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application) //Use singleton

    private val _routines = mutableStateListOf<Routine>()
    val routines: List<Routine> get() = _routines

    fun loadRoutines() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val routineList = db.routineDao().getAll()

                viewModelScope.launch(Dispatchers.Main) {
                    _routines.clear()
                    _routines.addAll(routineList) // This ensures UI recomposition
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun addRoutine(name: String, description: String, time: String, daysOfWeek: List<Int>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val routine = Routine(name = name, description = description, time = time, daysOfWeek = daysOfWeek)
                db.routineDao().insert(routine)

                viewModelScope.launch(Dispatchers.Main) {
                    loadRoutines()  // Ensure UI updates after adding a routine
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    fun deleteRoutine(routine: Routine) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                db.routineDao().delete(routine)
                loadRoutines()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateRoutine(routine: Routine) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                db.routineDao().update(routine)
                loadRoutines()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun scheduleNotifications(routine: Routine) {
        val alarmManager = getApplication<Application>().getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val timeParts = routine.time.split(" ")
        val (hourMinute, period) = timeParts
        val (hour, minute) = hourMinute.split(":").map { it.toInt() }
        val isAM = period == "AM"
        val adjustedHour = if (isAM) hour else hour + 12

        for (day in routine.daysOfWeek) {
            val notificationIntent = Intent(getApplication(), NotificationReceiver::class.java).apply {
                putExtra("routineName", routine.name)
            }

            val alarmIntent = PendingIntent.getBroadcast(getApplication(), routine.id, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            val calendar = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_WEEK, day)
                set(Calendar.HOUR_OF_DAY, adjustedHour - 1) // 1 hour before
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
            }

            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, alarmIntent)
        }
    }
}