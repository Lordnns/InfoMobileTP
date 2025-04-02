package com.example.infomobiletp

import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun TimeWheelPickerManual(
    initialHour: Int,
    initialMinute: Int,
    onTimeSelected: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val hours = (0..23).toList()
    val minutes = (0..59).toList()

    // Define the height for each item and the container height
    val itemHeight = 40.dp
    val containerHeight = 150.dp
    // Compute vertical padding so that one item is centered
    val verticalPadding = (containerHeight - itemHeight) / 2

    val density = LocalDensity.current
    val itemHeightPx = with(density) { itemHeight.toPx() }

    val hourState = rememberLazyListState(initialFirstVisibleItemIndex = initialHour)
    val minuteState = rememberLazyListState(initialFirstVisibleItemIndex = initialMinute)
    val scope = rememberCoroutineScope()

    // Manual snapping for hours
    LaunchedEffect(hourState.isScrollInProgress) {
        if (!hourState.isScrollInProgress) {
            val index = hourState.firstVisibleItemIndex
            val offset = hourState.firstVisibleItemScrollOffset.toFloat()
            Log.d("TimePicker", "Hour index=$index offset=$offset")
            val targetIndex = if (offset >= itemHeightPx / 2) (index + 1).coerceAtMost(hours.size - 1) else index
            if (targetIndex != index) {
                scope.launch { hourState.animateScrollToItem(targetIndex) }
            }
            if (!minuteState.isScrollInProgress) {
                onTimeSelected(hours[targetIndex], minutes[minuteState.firstVisibleItemIndex])
            }
        }
    }

    // Manual snapping for minutes
    LaunchedEffect(minuteState.isScrollInProgress) {
        if (!minuteState.isScrollInProgress) {
            val index = minuteState.firstVisibleItemIndex
            val offset = minuteState.firstVisibleItemScrollOffset.toFloat()
            Log.d("TimePicker", "Minute index=$index offset=$offset")
            val targetIndex = if (offset >= itemHeightPx / 2) (index + 1).coerceAtMost(minutes.size - 1) else index
            if (targetIndex != index) {
                scope.launch { minuteState.animateScrollToItem(targetIndex) }
            }
            if (!hourState.isScrollInProgress) {
                onTimeSelected(hours[hourState.firstVisibleItemIndex], minutes[targetIndex])
            }
        }
    }

    Row(modifier = modifier) {
        // Hours column + indicator
        Box(
            modifier = Modifier
                .weight(1f)
                .height(containerHeight)  // or .fillMaxHeight() if desired
        ) {
            LazyColumn(
                state = hourState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = verticalPadding)
            ) {
                items(hours) { hour ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(itemHeight),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = hour.toString().padStart(2, '0'), fontSize = 20.sp)
                    }
                }
            }
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .padding(vertical = verticalPadding)
                    .height(itemHeight)
                    .fillMaxWidth()
                    .border(width = 2.dp, color = Color.Gray)
            )
        }

        // Minutes column + indicator
        Box(
            modifier = Modifier
                .weight(1f)
                .height(containerHeight)
        ) {
            LazyColumn(
                state = minuteState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = verticalPadding)
            ) {
                items(minutes) { minute ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(itemHeight),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = minute.toString().padStart(2, '0'), fontSize = 20.sp)
                    }
                }
            }
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .padding(vertical = verticalPadding)
                    .height(itemHeight)
                    .fillMaxWidth()
                    .border(width = 2.dp, color = Color.Gray)
            )
        }
    }
}

@Composable
fun NotificationTimeItem(
    initialTime: String,
    initialMessage: String,
    onUpdate: (newTime: String, newMessage: String) -> Unit,
    onDelete: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    // Parse initial time "HH:mm" into hour and minute, defaulting to 0 if parsing fails.
    var selectedHour by remember { mutableStateOf(initialTime.substringBefore(":").toIntOrNull() ?: 0) }
    var selectedMinute by remember { mutableStateOf(initialTime.substringAfter(":").toIntOrNull() ?: 0) }
    var message by remember { mutableStateOf(initialMessage) }

    // Button that displays the current time value
    Button(
        onClick = { showDialog = true },
        modifier = Modifier.padding(4.dp)
    ) {
        Text(text = String.format("%02d:%02d", selectedHour, selectedMinute))
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Modifié Temp de Notification") },
            text = {
                Column {
                    // The custom wheel picker for time selection
                    TimeWheelPickerManual(
                        initialHour = selectedHour,
                        initialMinute = selectedMinute,
                        onTimeSelected = { hour, minute ->
                            selectedHour = hour
                            selectedMinute = minute
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it },
                        label = { Text("Message de Notification") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    // When confirmed, update the notification time using formatted time string "HH:mm"
                    onUpdate(String.format("%02d:%02d", selectedHour, selectedMinute), message)
                    showDialog = false
                }) {
                    Text("Sauvegarder", fontSize = 14.sp)
                }
            },
            dismissButton = {
                Button(onClick = {
                    onDelete()
                    showDialog = false
                }) {
                    Text("Supprimer", fontSize = 14.sp)
                }
            }
        )
    }
}