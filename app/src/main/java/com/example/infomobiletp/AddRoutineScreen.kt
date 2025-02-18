package com.example.infomobiletp

import android.annotation.SuppressLint
import android.widget.ToggleButton
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@SuppressLint("DefaultLocale")
@Composable
fun AddRoutineScreen(viewModel: RoutineViewModel) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedHour by remember { mutableStateOf(12) }
    var selectedMinute by remember { mutableStateOf(0) }
    var isAM by remember { mutableStateOf(true) }
    var selectedDays by remember { mutableStateOf(listOf<Int>()) }
    var isAdding by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = Modifier.padding(16.dp)) {
        SnackbarHost(hostState = snackbarHostState)

        BasicTextField(
            value = name,
            onValueChange = { name = it },
            modifier = Modifier.fillMaxWidth().padding(4.dp),
            decorationBox = { innerTextField ->
                if (name.isEmpty()) Text("Nom de la routine")
                innerTextField()
            }
        )

        BasicTextField(
            value = description,
            onValueChange = { description = it },
            modifier = Modifier.fillMaxWidth().padding(4.dp),
            decorationBox = { innerTextField ->
                if (description.isEmpty()) Text("Description")
                innerTextField()
            }
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            NumberPicker(value = selectedHour, range = 1..12, onValueChange = { selectedHour = it })
            Text(":")
            NumberPicker(value = selectedMinute, range = 0..59, onValueChange = { selectedMinute = it })
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(if (isAM) "AM" else "PM")
                Switch(
                    checked = isAM,
                    onCheckedChange = { isAM = !isAM }
                )
            }
        }

        Column {
            Text("Select Days:")
            DaySelector(selectedDays = selectedDays, onSelectionChange = { selectedDays = it })
        }

        Button(
            onClick = {
                isAdding = true
                val formattedTime = String.format("%02d:%02d %s", selectedHour, selectedMinute, if (isAM) "AM" else "PM")
                viewModel.addRoutine(name, description, formattedTime, selectedDays)

                name = ""
                description = ""
                selectedHour = 12
                selectedMinute = 0
                isAM = true
                selectedDays = emptyList()

                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Routine ajoutée avec succès")
                }

                isAdding = false
            },
            enabled = !isAdding
        ) {
            Text(if (isAdding) "Ajout..." else "Ajouter Routine")
        }
    }
}

@Composable
fun NumberPicker(value: Int, range: IntRange, onValueChange: (Int) -> Unit) {
    // Simple vertical number picker using buttons
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
fun DaySelector(selectedDays: List<Int>, onSelectionChange: (List<Int>) -> Unit) {
    val days = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    val dayValues = listOf(1, 2, 3, 4, 5, 6, 7)

    Column {
        // First Row: Sunday to Wednesday
        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
            for (i in 0..3) {
                DayButton(day = days[i], dayValue = dayValues[i], selectedDays, onSelectionChange)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Second Row: Thursday to Saturday
        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
            for (i in 4..6) {
                DayButton(day = days[i], dayValue = dayValues[i], selectedDays, onSelectionChange)
            }
        }
    }
}

// Helper function to create the day selection button
@Composable
fun DayButton(day: String, dayValue: Int, selectedDays: List<Int>, onSelectionChange: (List<Int>) -> Unit) {
    val isSelected = dayValue in selectedDays

    Button(
        onClick = {
            onSelectionChange(if (isSelected) selectedDays - dayValue else selectedDays + dayValue)
        },
        colors = ButtonDefaults.buttonColors(if (isSelected) Color.Blue else Color.Gray),
        modifier = Modifier.padding(4.dp)
    ) {
        Text(day)
    }
}
