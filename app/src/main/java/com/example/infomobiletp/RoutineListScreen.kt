package com.example.infomobiletp

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*

@Composable
fun RoutineListScreen(viewModel: RoutineViewModel = viewModel()) {
    val routines = viewModel.routines
    val navController = rememberNavController()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        NavHost(navController = navController, startDestination = "routine_list") {
            composable("routine_list") {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    item {
                        Text(
                            text = "Routines Créées",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    items(routines) { routine ->
                        RoutineItem(
                            routine = routine,
                            onClick = { navController.navigate("routine_detail/${routine.id}") },
                            onToggleNotifications = { updatedRoutine ->
                                // Toggle notificationsEnabled and update the routine
                                val toggled = updatedRoutine.copy(
                                    notificationsEnabled = !updatedRoutine.notificationsEnabled
                                )
                                viewModel.updateRoutine(toggled)
                            }
                        )
                    }
                    // Add Routine UI at the bottom
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Nouvelle routine",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        AddRoutineScreen(viewModel)
                    }
                }
            }
            composable("routine_detail/{id}") { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id")?.toIntOrNull()
                val routine = routines.find { it.id == id }
                routine?.let {
                    RoutineDetailScreen(it, viewModel, navController)
                }
            }
        }
    }
}
