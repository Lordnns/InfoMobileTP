package com.example.infomobiletp

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun RoutineListScreen(viewModel: RoutineViewModel = viewModel()) {
    val routines = viewModel.routines
    val navController = rememberNavController()

    LaunchedEffect(Unit) {
        viewModel.loadRoutines()
    }

    NavHost(navController = navController, startDestination = "routine_list") {
        composable("routine_list") {
            Column(modifier = Modifier.padding(16.dp)) {
                routines.forEach { routine ->
                    RoutineItem(routine, onClick = {
                        navController.navigate("routine_detail/${routine.id}")
                    })
                }

                AddRoutineScreen(viewModel)
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
