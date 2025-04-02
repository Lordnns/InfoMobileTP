package com.example.infomobiletp

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

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
        Text(text = "Catégorie: ${routine.category}", fontSize = 14.sp, color = Color.DarkGray)
        Text(text = "Récurrence: ${routine.recurrenceType.toFrench()}", fontSize = 14.sp, color = Color.DarkGray)
        /*Text(text = "Début: ${dateFormat.format(Date(routine.startDate))}", fontSize = 14.sp)
        Text(
            text = "Fin: ${routine.endDate?.let { dateFormat.format(Date(it)) } ?: "Indéfini"}",
            fontSize = 14.sp
        )*/
        Text(text = "Priorité: ${routine.priority.toFrench()}", fontSize = 14.sp)
        fun getFrenchDays(days: List<Int>): String {
            if (days.isEmpty()) return "Aucun jour sélectionné"

            val sortedDays = days.sorted()
            val mapping = mapOf(
                1 to "Dimanche",
                2 to "Lundi",
                3 to "Mardi",
                4 to "Mercredi",
                5 to "Jeudi",
                6 to "Vendredi",
                7 to "Samedi"
            )
            return when (sortedDays) {
                listOf(1, 2, 3, 4, 5, 6, 7) -> "Toute la semaine"
                listOf(2, 3, 4, 5, 6) -> "Du lundi au vendredi"
                listOf(1, 7) -> "Fin de Semaine"
                else -> sortedDays.joinToString(", ") { mapping[it] ?: it.toString() }
            }
        }

        Text(
            text = "Jours: ${getFrenchDays(routine.daysOfWeek)}",
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Temps de Notification:", fontSize = 14.sp, color = Color.Gray)
        if (routine.notificationTimes.isNotEmpty()) {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(routine.notificationTimes) { nt ->
                    Text(
                        text = "${nt.time} - ${nt.message}",
                        fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        } else {
            Text(text = "Aucun Temp de Notification", fontSize = 14.sp, color = Color.Gray)
        }
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
    // Pre-fill fields with the existing routine values.
    var name by remember { mutableStateOf(routine.name) }
    var description by remember { mutableStateOf(routine.description) }
    var category by remember { mutableStateOf(routine.category) }
    var selectedDays by remember { mutableStateOf(routine.daysOfWeek) }
    var recurrenceType by remember { mutableStateOf(routine.recurrenceType) }
    var startDate by remember { mutableStateOf(routine.startDate) }
    var endDate by remember { mutableStateOf(routine.endDate) }
    var priority by remember { mutableStateOf(routine.priority) }
    // Use mutableStateListOf for notificationTimes so updates trigger recomposition.
    val notificationTimes = remember { mutableStateListOf<NotificationTime>().apply { addAll(routine.notificationTimes) } }
    // Manage a list of categories. In a full implementation, these might be loaded from a persistent store.

    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

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
        // Category dropdown for selecting category.
        CategoryDropdown(
            selectedCategory = category,
            onCategorySelected = { category = it },
        )

        RecurrenceDropdown(
            selectedRecurrence = recurrenceType,
            onRecurrenceSelected = { recurrenceType = it }
        )

        // Day selection remains as before.
        Column {
            Text("Sélection de journée(s):")
            DaySelector(selectedDays = selectedDays, onSelectionChange = { selectedDays = it })
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Start and End Dates (for simplicity, displayed as text; a DatePicker can be integrated)
        //Text("Début: ${dateFormat.format(Date(startDate))}")
        //Text("Fin: ${endDate?.let { dateFormat.format(Date(it)) } ?: "Indéfini"}")
        // Priority selection (radio buttons)
        Text("Priorité:")
        Row {
            Priority.values().forEach { prio ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(4.dp)) {
                    RadioButton(
                        selected = (priority == prio),
                        onClick = { priority = prio }
                    )
                    Text(text = prio.toFrench())
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        // Notification Times editing section
        Column(modifier = Modifier.fillMaxWidth()) {
            Text("Modifié Temps de Notification:", fontSize = 16.sp)
            notificationTimes.forEachIndexed { index, nt ->
                NotificationTimeItem(
                    initialTime = nt.time,
                    initialMessage = nt.message,
                    onUpdate = { newTime, newMessage ->
                        notificationTimes[index] = notificationTimes[index].copy(time = newTime, message = newMessage)
                    },
                    onDelete = {
                        notificationTimes.removeAt(index)
                    }
                )
            }
            Button(
                onClick = {
                    notificationTimes.add(NotificationTime("07:00", enabled = true, message = ""))
                }
            ) {
                Text("Ajouter Notification")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        // Save and Cancel buttons.
        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
            Button(onClick = {
                val updatedRoutine = routine.copy(
                    name = name,
                    description = description,
                    category = category,
                    daysOfWeek = selectedDays,
                    recurrenceType = recurrenceType,
                    startDate = startDate,
                    endDate = endDate,
                    priority = priority,
                    notificationTimes = notificationTimes.toList()
                )
                viewModel.updateRoutine(updatedRoutine)
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
