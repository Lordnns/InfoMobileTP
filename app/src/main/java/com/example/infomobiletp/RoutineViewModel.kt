package com.example.infomobiletp

import android.Manifest
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
import android.content.ContentValues
import android.content.pm.PackageManager
import android.provider.CalendarContract
import androidx.core.app.ActivityCompat
import java.util.TimeZone
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import android.content.ContentUris



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
                    addRoutineToCalendar(newRoutine)
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
            val context = getApplication<Application>()
            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val geofencingClient = LocationServices.getGeofencingClient(context)

            // Cancel TIME alarms
            routine.notificationTimes
                .filter { it.enabled && it.triggerType == TriggerType.TIME }
                .forEach { nt ->
                    val (hour, minute) = nt.time.split(":")
                        .mapNotNull { it.toIntOrNull() }
                        .let { if (it.size == 2) it[0] to it[1] else return@forEach }
                    routine.daysOfWeek.forEach { day ->
                        val code = routine.id * 10000 + day * 100 + hour * 10 + minute
                        val pi = PendingIntent.getBroadcast(
                            context,
                            code,
                            Intent(context, NotificationReceiver::class.java),
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                        am.cancel(pi)
                    }
                }

            // 🗺 Remove geofences (LOCATION triggers)
            geofencingClient.removeGeofences(listOf(routine.id.toString()))

            // SharedPreferences for cleaning up BATTERY & LOCATION messages
            val prefs = context.getSharedPreferences("notification_prefs", Context.MODE_PRIVATE)

            // Clear BATTERY prefs
            prefs.edit()
                .remove("battery_threshold_${routine.id}")
                .remove("battery_message_${routine.id}")
                .apply()

            // Clear LOCATION message prefs
            prefs.edit()
                .remove("location_message_${routine.id}")
                .apply()

            // Remove Calendar event
            val eventId = prefs.getLong("calendar_event_${routine.id}", -1L)
            if (eventId != -1L) {
                context.contentResolver.delete(
                    ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId),
                    null, null
                )
                prefs.edit().remove("calendar_event_${routine.id}").apply()
            }

            // 🗄 Finally delete from Room & reload list
            db.routineDao().delete(routine)
            loadRoutines()
        }
    }

    fun updateRoutine(routine: Routine) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                db.routineDao().update(routine)
                scheduleNotifications(routine)
                addRoutineToCalendar(routine)
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
        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val prefs = context.getSharedPreferences("notification_prefs", Context.MODE_PRIVATE)
        val geofencingClient = LocationServices.getGeofencingClient(context)

        // TIME triggers → exact alarms
        routine.notificationTimes
            .filter { it.enabled && it.triggerType == TriggerType.TIME }
            .forEach { nt ->
                val (hour, minute) = nt.time.split(":")
                    .mapNotNull { it.toIntOrNull() }
                    .let { if (it.size == 2) it[0] to it[1] else return@forEach }

                routine.daysOfWeek.forEach { day ->
                    val intent = Intent(context, NotificationReceiver::class.java).apply {
                        putExtra("routineId", routine.id)
                        putExtra("notificationMessage", nt.message)
                    }
                    val requestCode = routine.id * 10000 + day * 100 + hour * 10 + minute
                    val pi = PendingIntent.getBroadcast(
                        context, requestCode, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    val cal = Calendar.getInstance().apply {
                        set(Calendar.DAY_OF_WEEK, day)
                        set(Calendar.HOUR_OF_DAY, hour)
                        set(Calendar.MINUTE, minute)
                        set(Calendar.SECOND, 0)
                        if (timeInMillis < System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 7)
                    }
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pi)
                }
            }

        // BATTERY triggers → store threshold & message in prefs
        routine.notificationTimes
            .filter { it.triggerType == TriggerType.BATTERY }
            .forEach { nt ->
                prefs.edit()
                    .putInt("battery_threshold_${routine.id}", nt.batteryLevel ?: 0)
                    .putString("battery_message_${routine.id}", nt.message)
                    .apply()
            }

        // LOCATION triggers → register geofences
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val pi = PendingIntent.getBroadcast(
                context,
                routine.id,
                Intent(context, GeofenceBroadcastReceiver::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            routine.notificationTimes
                .filter { it.triggerType == TriggerType.LOCATION }
                .forEach { nt ->
                    val (lat, lng) = nt.location!!.split(",").map(String::toDouble)
                    val fence = Geofence.Builder()
                        .setRequestId(routine.id.toString())
                        .setCircularRegion(lat, lng, (nt.locationRadius ?: 100).toFloat())
                        .setExpirationDuration(Geofence.NEVER_EXPIRE)
                        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                        .build()
                    val request = GeofencingRequest.Builder()
                        .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                        .addGeofence(fence)
                        .build()
                    geofencingClient.addGeofences(request, pi)
                }
        }
    }

    private fun addRoutineToCalendar(routine: Routine) {
        val cr = getApplication<Application>().contentResolver
        val context = getApplication<Application>()
        val prefs = context
            .getSharedPreferences("notification_prefs", Context.MODE_PRIVATE)
        // pick first writable calendar:
        val calId = cr.query(
            CalendarContract.Calendars.CONTENT_URI,
            arrayOf(CalendarContract.Calendars._ID), null, null, null
        )?.use { if (it.moveToFirst()) it.getLong(0) else return } ?: return

        // build event values
        val ev = ContentValues().apply {
            put(CalendarContract.Events.CALENDAR_ID, calId)
            put(CalendarContract.Events.TITLE, routine.name)
            put(CalendarContract.Events.DESCRIPTION, routine.description)
            put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
            put(CalendarContract.Events.DTSTART, routine.startDate)
            routine.endDate?.let { put(CalendarContract.Events.DTEND, it) }
            // weekly RRULE on the selected days
            if (routine.recurrenceType == RecurrenceType.WEEKLY) {
                val days = routine.daysOfWeek
                    .joinToString(",") { listOf("SU","MO","TU","WE","TH","FR","SA")[it-1] }
                put(CalendarContract.Events.RRULE, "FREQ=WEEKLY;BYDAY=$days")
            }
        }
        val uri = cr.insert(CalendarContract.Events.CONTENT_URI, ev) ?: return
        val eventId = uri.lastPathSegment!!.toLong()
        prefs.edit()
            .putLong("calendar_event_${routine.id}", eventId)
            .apply()

        // add a 10‑min reminder
        ContentValues().apply {
            put(CalendarContract.Reminders.EVENT_ID, uri.lastPathSegment!!.toLong())
            put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT)
            put(CalendarContract.Reminders.MINUTES, 10)
        }.let { cr.insert(CalendarContract.Reminders.CONTENT_URI, it) }
    }
}
