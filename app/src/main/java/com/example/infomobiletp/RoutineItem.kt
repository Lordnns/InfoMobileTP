package com.example.infomobiletp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.compose.material3.Button

@Composable
fun RoutineItem(
    routine: Routine,
    onClick: () -> Unit,
    onToggleNotifications: (Routine) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(Color.LightGray, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Column for routine details occupies most of the horizontal space.
            Column(modifier = Modifier.weight(1f)) {
                Text(text = routine.name, fontSize = 20.sp)
                Text(text = "Categorie: ${routine.category}", fontSize = 14.sp, color = Color.DarkGray)
                if (routine.notificationTimes.isNotEmpty()) {
                    Text(
                        text = routine.notificationTimes.joinToString(separator = ", ") { nt ->
                            "${nt.time} ${if (nt.message.isNotEmpty()) "(${nt.message})" else ""}"
                        },
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                } else {
                    Text(text = "Aucun Temp de Notification", fontSize = 14.sp, color = Color.Gray)
                }
            }
            // Toggle button to enable/disable notifications
            Button(
                onClick = { onToggleNotifications(routine) },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(if (routine.notificationsEnabled) "ON" else "OFF")
            }
        }
    }
}
