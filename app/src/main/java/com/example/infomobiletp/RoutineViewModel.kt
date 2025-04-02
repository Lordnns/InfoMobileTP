package com.example.infomobiletp

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.net.Uri
import android.provider.Settings
import java.util.Calendar
import androidx.core.net.toUri

class RoutineViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)

    private val _routines = mutableStateListOf<Routine>()
    val routines: List<Routine> get() = _routines

    // Sort order: "name" or "category"
    var sortOrder: String = "name"

    init {
        loadRoutines()
        viewModelScope.launch {
            // Delay to allow the database's onCreate callback to finish its insertions.
            kotlinx.coroutines.delay(1000)
            loadRoutines()
        }
    }

    fun loadRoutines() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val routineList = db.routineDao().getAll()
                viewModelScope.launch(Dispatchers.Main) {
                    _routines.clear()
                    _routines.addAll(routineList)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun addRoutine(
        name: String,
        description: String,
        category: String,
        daysOfWeek: List<Int>,
        recurrenceType: RecurrenceType,
        startDate: Long,
        endDate: Long?,
        priority: Priority,
        notificationTimes: List<NotificationTime>
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Build the routine with its list of notification times
                val routine = Routine(
                    name = name,
                    description = description,
                    category = category,
                    daysOfWeek = daysOfWeek,
                    recurrenceType = recurrenceType,
                    startDate = startDate,
                    endDate = endDate,
                    priority = priority,
                    notificationTimes = notificationTimes
                )
                // Insert routine (make sure your DAO returns the generated id)
                val newId = db.routineDao().insert(routine)
                val newRoutine = routine.copy(id = newId.toInt())
                try {
                    scheduleNotifications(newRoutine)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                viewModelScope.launch(Dispatchers.Main) {
                    loadRoutines()
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
                scheduleNotifications(routine)
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
                scheduleNotifications(routine)
                viewModelScope.launch(Dispatchers.Main) {
                    loadRoutines()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @SuppressLint("ScheduleExactAlarm", "NewApi")
    private fun scheduleNotifications(routine: Routine) {
        val context = getApplication<Application>()
        val alarmManager = getApplication<Application>().getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (!alarmManager.canScheduleExactAlarms()) {
            // Rediriger l'utilisateur vers les paramètres pour activer l'autorisation
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = ("package:" + context.packageName).toUri()
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }

        routine.notificationTimes.filter { it.enabled }.forEach { nt ->
            // Expecting nt.time to be in "HH:mm" format
            val parts = nt.time.split(":")
            if (parts.size != 2) return@forEach
            val hour = parts[0].toIntOrNull() ?: return@forEach
            val minute = parts[1].toIntOrNull() ?: return@forEach

            for (day in routine.daysOfWeek) {
                // Build the notification intent with the custom message
                val notificationIntent = Intent(context, NotificationReceiver::class.java).apply {
                    putExtra("routineName", routine.name)
                    putExtra("routineId", routine.id)
                    putExtra("notificationMessage", nt.message)
                }
                // Create a unique request code—for example, combining routine.id, day, hour, and minute
                val requestCode = routine.id * 10000 + day * 100 + hour * 10 + minute
                val alarmIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    notificationIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val calendar = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_WEEK, day)
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    // If the time is already past, move to the next occurrence (e.g., add 7 days)
                    if (timeInMillis < System.currentTimeMillis()) {
                        add(Calendar.DAY_OF_YEAR, 7)
                    }
                }

                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, alarmIntent)
            }
        }
    }
}