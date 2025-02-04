package com.example.infomobiletp

import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.*

@Composable
fun AddRoutineScreen(viewModel: RoutineViewModel) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp)) {
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

        BasicTextField(
            value = time,
            onValueChange = { time = it },
            modifier = Modifier.fillMaxWidth().padding(4.dp),
            decorationBox = { innerTextField ->
                if (time.isEmpty()) Text("Heure (HH:mm)")
                innerTextField()
            }
        )

        Button(onClick = { viewModel.addRoutine(name, description, time) }) {
            Text("Ajouter Routine")
        }
    }
}
