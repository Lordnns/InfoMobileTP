package com.example.infomobiletp

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

    LaunchedEffect(Unit) {
        viewModel.loadRoutines()
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        NavHost(navController = navController, startDestination = "routine_list") {
            composable("routine_list") {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(routines) { routine ->
                        RoutineItem(routine, onClick = {
                            navController.navigate("routine_detail/${routine.id}")
                        })
                    }

                    // Add Routine UI at the bottom, scrolling together
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
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
