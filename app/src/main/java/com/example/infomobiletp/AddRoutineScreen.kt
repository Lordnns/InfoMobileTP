package com.example.infomobiletp

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.widget.ToggleButton
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.material3.RadioButton
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.modifier.modifierLocalMapOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@SuppressLint("DefaultLocale")
@Composable
fun AddRoutineScreen(viewModel: RoutineViewModel) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Aucune") }
    var selectedDays by remember { mutableStateOf(listOf<Int>()) }
    var recurrenceType by remember { mutableStateOf(RecurrenceType.WEEKLY) }
    var startDate by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var endDate by remember { mutableStateOf<Long?>(null) }
    var priority by remember { mutableStateOf(Priority.MEDIUM) }
    val notificationTimes = remember { mutableStateListOf<NotificationTime>() }
    var isAdding by remember { mutableStateOf(false) }

    // Manage the list of categories (persisted via Room or SharedPreferences)
    var categories by remember { mutableStateOf(listOf("None", "Work", "Leisure", "Health")) }

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
        Text(
            text = "Priorité:",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
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
            Text("Temps de Notification:", fontSize = 16.sp)
            notificationTimes.forEachIndexed { index, nt ->
                NotificationTimeItem(
                    initialTime = nt.time,
                    initialMessage = nt.message,
                    onUpdate = { newTime, newMessage ->
                        notificationTimes[index] = nt.copy(time = newTime, message = newMessage)
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

        Button(
            onClick = {
                isAdding = true
                viewModel.addRoutine(
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
                name = ""
                description = ""
                category = "None"
                selectedDays = emptyList()
                notificationTimes.clear()
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
fun DateInputField(
    label: String,
    dateMillis: Long,
    onDateSelected: (Long) -> Unit

) {
    val context = LocalContext.current
    var showDatePicker by remember { mutableStateOf(false) }
    val calendar = Calendar.getInstance().apply { timeInMillis = dateMillis }
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val displayDate = dateFormat.format(calendar.time)

    if (showDatePicker) {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val newCalendar = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }
                onDateSelected(newCalendar.timeInMillis)
                showDatePicker = false
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    OutlinedTextField(
        value = displayDate,
        onValueChange = {},
        label = { Text(label) },
        modifier = Modifier
            .fillMaxWidth(),
        readOnly = true
    )
}

@Composable
fun OptionalDateInputField(
    label: String,
    dateMillis: Long?,
    onDateSelected: (Long?) -> Unit
) {
    val context = LocalContext.current
    var showDatePicker by remember { mutableStateOf(false) }
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val displayDate = dateMillis?.let { dateFormat.format(Date(it)) } ?: "Aucune"

    if (showDatePicker) {
        LaunchedEffect(showDatePicker) {
            // Use current time as default if dateMillis is null.
            val defaultMillis = dateMillis ?: System.currentTimeMillis()
            val calendar = Calendar.getInstance().apply { timeInMillis = defaultMillis }
            DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    val newCalendar = Calendar.getInstance().apply {
                        set(year, month, dayOfMonth)
                    }
                    onDateSelected(newCalendar.timeInMillis)
                    showDatePicker = false
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }


    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDatePicker = true },
        tonalElevation = 2.dp // optional: gives a visual elevation
    ) {
        OutlinedTextField(
            value = displayDate,
            onValueChange = {},
            label = { Text(label) },
            readOnly = true,
            modifier = Modifier.fillMaxWidth()
        )
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
    val days = listOf("Dim", "Lun", "Mar", "Mer", "Jeu", "Ven", "Sam")
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
