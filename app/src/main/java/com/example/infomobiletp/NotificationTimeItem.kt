package com.example.infomobiletp

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
fun BatteryLevelPicker(value: Int, range: IntRange, onValueChange: (Int) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Button(onClick = { if (value < range.last) onValueChange(value + 1) }) {
            Text("▲")
        }
        Text("$value", fontSize = 20.sp)
        Button(onClick = { if (value > range.first) onValueChange(value - 1) }) {
            Text("▼")
        }
    }
}

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

@SuppressLint("DefaultLocale")
@Composable
fun NotificationTimeItem(
    initialTime: String,                // Expected in format "HH:mm"
    initialMessage: String,             // Used for TIME and BATTERY triggers.
    initialTrigger: TriggerType,
    initialBatteryLevel: Int? = null,     // For BATTERY trigger.
    initialLocation: String = "",         // For LOCATION trigger.
    initialLocationRadius: Int? = null,     // For LOCATION trigger.
    initiallyOpen: Boolean = false,
    onUpdate: (newTime: String, newMessage: String, newTrigger: TriggerType, newBatteryLevel: Int?, newLocation: String, newLocationRadius: Int?) -> Unit,
    onDelete: () -> Unit,
    onDialogDismiss: () -> Unit = {}
) {
    var showDialog by remember { mutableStateOf(initiallyOpen) }
    var showMapDialog by remember { mutableStateOf(false) }

    // Display summary based on the trigger type:
    val displayText = when (initialTrigger) {
        TriggerType.TIME -> initialTime
        TriggerType.BATTERY -> "Batterie: ${initialBatteryLevel?.toString() ?: "N/A"}%"
        TriggerType.LOCATION -> "Localisation: ${if (initialLocation.isNotEmpty()) initialLocation else "Choisir Lieu"} (Rayon: ${initialLocationRadius ?: "N/A"} m)"
    }

    Button(
        onClick = { showDialog = true },
        modifier = Modifier.padding(4.dp)
    ) {
        Text(text = displayText)
    }

    if (showDialog) {
        // Define mutable states for updated values.
        var selectedTrigger by remember { mutableStateOf(initialTrigger) }
        var selectedHour by remember { mutableStateOf(initialTime.substringBefore(":").toIntOrNull() ?: 0) }
        var selectedMinute by remember { mutableStateOf(initialTime.substringAfter(":").toIntOrNull() ?: 0) }
        var batteryLevel by remember { mutableStateOf(initialBatteryLevel ?: 50) }
        var locationText by remember { mutableStateOf(initialLocation) }
        var locationRadius by remember { mutableStateOf(initialLocationRadius ?: 100) }
        var updatedMessage by remember { mutableStateOf(initialMessage) }

        AlertDialog(
            onDismissRequest = {
                showDialog = false
                onDialogDismiss()
            },
            title = { Text("Modifier Notification") },
            text = {
                Column {
                    // Row to select trigger type.
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        RadioButton(
                            selected = selectedTrigger == TriggerType.TIME,
                            onClick = { selectedTrigger = TriggerType.TIME }
                        )
                        Text("Heure")
                        RadioButton(
                            selected = selectedTrigger == TriggerType.BATTERY,
                            onClick = { selectedTrigger = TriggerType.BATTERY }
                        )
                        Text("Batterie")
                        RadioButton(
                            selected = selectedTrigger == TriggerType.LOCATION,
                            onClick = { selectedTrigger = TriggerType.LOCATION }
                        )
                        Text("Localisation")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    // Show UI based on the selected trigger.
                    when (selectedTrigger) {
                        TriggerType.TIME -> {
                            TimeWheelPickerManual(
                                initialHour = selectedHour,
                                initialMinute = selectedMinute,
                                onTimeSelected = { hour, minute ->
                                    selectedHour = hour
                                    selectedMinute = minute
                                }
                            )
                        }
                        TriggerType.BATTERY -> {
                            Text("Niveau de batterie déclenchement (%)", fontSize = 16.sp)
                            BatteryLevelPicker(
                                value = batteryLevel,
                                range = 1..100,
                                onValueChange = { batteryLevel = it }
                            )
                        }
                        TriggerType.LOCATION -> {
                            Column {
                                Button(onClick = { showMapDialog = true }) {
                                    Text("Choisir un lieu")
                                }
                                if (locationText.isNotEmpty()) {
                                    Text("Lieu sélectionné: $locationText", fontSize = 14.sp)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Rayon de déclenchement (m)", fontSize = 16.sp)
                                BatteryLevelPicker(
                                    value = locationRadius,
                                    range = 10..1000,
                                    onValueChange = { locationRadius = it }
                                )
                            }
                            if (showMapDialog) {
                                AlertDialog(
                                    onDismissRequest = { showMapDialog = false },
                                    title = { Text("Sélectionnez une localisation") },
                                    text = {
                                        // Integrate MapLibreView for selecting location.
                                        // Assurez-vous que votre composable MapLibreView est défini dans le projet.
                                        OSMMapView(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(300.dp)
                                        ) { lat, lng ->
                                            locationText = "$lat,$lng"
                                            showMapDialog = false
                                        }
                                    },
                                    confirmButton = {
                                        Button(onClick = { showMapDialog = false }) {
                                            Text("Fermer")
                                        }
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = updatedMessage,
                        onValueChange = { updatedMessage = it },
                        label = { Text("Message de Notification") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val newTime = if (selectedTrigger == TriggerType.TIME)
                        String.format("%02d:%02d", selectedHour, selectedMinute)
                    else ""
                    onUpdate(
                        newTime,
                        updatedMessage,
                        selectedTrigger,
                        if (selectedTrigger == TriggerType.BATTERY) batteryLevel else null,
                        if (selectedTrigger == TriggerType.LOCATION) locationText else "",
                        if (selectedTrigger == TriggerType.LOCATION) locationRadius else null
                    )
                    showDialog = false
                    onDialogDismiss()
                }) {
                    Text("Sauvegarder", fontSize = 14.sp)
                }
            },
            dismissButton = {
                Button(onClick = {
                    onDelete()
                    showDialog = false
                    onDialogDismiss()
                }) {
                    Text("Supprimer", fontSize = 14.sp)
                }
            }
        )
    }
}