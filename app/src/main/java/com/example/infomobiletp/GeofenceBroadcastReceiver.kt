// GeofenceBroadcastReceiver.kt
package com.example.infomobiletp

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, intent: Intent) {
        // Permission guard
        if (ContextCompat.checkSelfPermission(
                ctx, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        // Unwrap geofencing event safely
        val event = GeofencingEvent.fromIntent(intent) ?: return
        if (event.hasError()) return

        // Handle enter transition
        if (event.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            val prefs = ctx.getSharedPreferences("notification_prefs", Context.MODE_PRIVATE)
            event.triggeringGeofences.orEmpty().forEach { gf ->
                val id = gf.requestId.toInt()
                val customMsg = prefs.getString("location_message_$id", "") ?: ""
                val contentText = "Localisation : $customMsg"

                // Fire the notification
                NotificationManagerCompat.from(ctx).notify(
                    id,
                    NotificationCompat.Builder(ctx, "routine_notifications")
                        .setContentTitle("Arrivée détectée")
                        .setContentText(contentText)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .build()
                )
            }
        }
    }
}
