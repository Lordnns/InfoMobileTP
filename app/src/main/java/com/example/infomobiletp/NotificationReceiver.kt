package com.example.infomobiletp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import java.util.Calendar

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val customMessage = intent?.getStringExtra("notificationMessage") ?: "Routine Reminder"

        // Déterminer le message contextuel selon l'heure actuelle
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val contextualMessage = when (currentHour) {
            in 5..11 -> "Bon matin ! C'est l'heure de"
            in 12..17 -> "Bon après-midi ! N'oubliez pas"
            in 18..22 -> "Bonsoir ! Pensez à"
            else -> "Il est temps de"
        }

        // Pour intégrer la localisation, ajouter ici le code pour récupérer la position et ajuster le message

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Création du canal de notification pour Android 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "routine_notifications",
                "Routine Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, "routine_notifications")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Routine Reminder")
            .setContentText("$contextualMessage $customMessage")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}