package com.example.infomobiletp

import androidx.compose.foundation.layout.*
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
    var name by remember { mutableStateOf(routine.name) }
    var description by remember { mutableStateOf(routine.description) }
    var time by remember { mutableStateOf(routine.time) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = { navController.popBackStack() }) { //Back button
            Text("Retour")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isEditing) {
            TextField(value = name, onValueChange = { name = it }, label = { Text("Nom") })
            TextField(value = description, onValueChange = { description = it }, label = { Text("Description") })
            TextField(value = time, onValueChange = { time = it }, label = { Text("Heure") })
        } else {
            Text(text = name, fontSize = 24.sp)
            Text(text = description, fontSize = 16.sp)
            Text(text = time, fontSize = 14.sp, color = Color.Gray)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(onClick = {
                if (isEditing) {
                    viewModel.updateRoutine(routine.copy(name = name, description = description, time = time))
                    isEditing = false
                } else {
                    isEditing = true
                }
            }) {
                Text(if (isEditing) "Sauvegarder" else "Modifier")
            }

            Button(onClick = {
                if (isEditing) {
                    isEditing = false
                } else {
                    viewModel.deleteRoutine(routine)
                    navController.popBackStack()
                }
            }) {
                Text(if (isEditing) "Annuler" else "Supprimer")
            }
        }
    }
}
