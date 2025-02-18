package com.example.infomobiletp

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun RoutineDetailScreen(routine: Routine, viewModel: RoutineViewModel, navController: NavController) {
    var isEditing by remember { mutableStateOf(false) }

    if (isEditing) {
        RoutineEditScreen(routine, viewModel, onEditComplete = { isEditing = false }, navController)
    } else {
        RoutineDetailView(routine, onEditClick = { isEditing = true }, onDeleteClick = {
            viewModel.deleteRoutine(routine)
            navController.popBackStack()
        }, navController)
    }
}

@Composable
fun RoutineDetailView(routine: Routine, onEditClick: () -> Unit, onDeleteClick: () -> Unit, navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Back Button
        Button(onClick = { navController.popBackStack() }) {
            Text("Retour")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = routine.name, fontSize = 24.sp)
        Text(text = routine.description, fontSize = 16.sp)
        Text(text = routine.time, fontSize = 14.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(onClick = onEditClick) {
                Text("Modifier")
            }
            Button(onClick = onDeleteClick) {
                Text("Supprimer")
            }
        }
    }
}

@Composable
fun RoutineEditScreen(routine: Routine, viewModel: RoutineViewModel, onEditComplete: () -> Unit, navController: NavController) {
    var name by remember { mutableStateOf(routine.name) }
    var description by remember { mutableStateOf(routine.description) }
    var selectedHour by remember { mutableStateOf(parseHour(routine.time)) }
    var selectedMinute by remember { mutableStateOf(parseMinute(routine.time)) }
    var isAM by remember { mutableStateOf(parseAMPM(routine.time)) }
    var selectedDays by remember { mutableStateOf(routine.daysOfWeek) }

    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        // Back Button
        Button(onClick = { onEditComplete() }) {
            Text("Retour")
        }

        Spacer(modifier = Modifier.height(16.dp))

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
                Switch(checked = isAM, onCheckedChange = { isAM = !isAM })
            }
        }

        Column {
            Text("Select Days:")
            DaySelector(selectedDays = selectedDays, onSelectionChange = { selectedDays = it })
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(onClick = {
                val formattedTime = String.format("%02d:%02d %s", selectedHour, selectedMinute, if (isAM) "AM" else "PM")
                viewModel.updateRoutine(routine.copy(name = name, description = description, time = formattedTime, daysOfWeek = selectedDays))
                onEditComplete()
            }) {
                Text("Sauvegarder")
            }

            Button(onClick = onEditComplete) {
                Text("Annuler")
            }
        }
    }
}

fun parseHour(time: String): Int {
    val parts = time.split(" ")
    val (hour, _) = parts[0].split(":").map { it.toInt() }
    return if (hour == 0 || hour == 12) 12 else hour % 12
}

fun parseMinute(time: String): Int {
    return time.split(" ")[0].split(":")[1].toInt()
}

fun parseAMPM(time: String): Boolean {
    return time.contains("AM")
}
