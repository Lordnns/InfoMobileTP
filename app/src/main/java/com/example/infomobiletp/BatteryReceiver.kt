package com.example.infomobiletp

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.BatteryManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

class BatteryReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, intent: Intent) {
        // Permission guard
        if (ContextCompat.checkSelfPermission(
                ctx, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        // Read current battery level
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)

        // Load saved thresholds & messages
        val prefs = ctx.getSharedPreferences("notification_prefs", Context.MODE_PRIVATE)
        prefs.all.forEach { (key, value) ->
            if (key.startsWith("battery_threshold_")) {
                val id = key.removePrefix("battery_threshold_").toInt()
                val threshold = (value as Int)
                if (level <= threshold) {
                    // Build your content string
                    val customMsg = prefs.getString("battery_message_$id", "") ?: ""
                    val contentText = "Batterie : $level% $customMsg"

                    // Fire the notification
                    NotificationManagerCompat.from(ctx).notify(
                        id,
                        NotificationCompat.Builder(ctx, "routine_notifications")
                            .setContentTitle("Batterie faible")
                            .setContentText(contentText)
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .build()
                    )
                }
            }
        }
    }
}
