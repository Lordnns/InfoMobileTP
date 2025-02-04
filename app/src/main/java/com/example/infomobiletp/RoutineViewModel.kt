package com.example.infomobiletp

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RoutineViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application) //Use singleton

    val routines = mutableStateListOf<Routine>()

    fun loadRoutines() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val routineList = db.routineDao().getAll()
                routines.clear()
                routines.addAll(routineList)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun addRoutine(name: String, description: String, time: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val routine = Routine(name = name, description = description, time = time)
                db.routineDao().insert(routine)
                loadRoutines()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteRoutine(routine: Routine) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                db.routineDao().delete(routine)
                loadRoutines()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateRoutine(routine: Routine) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                db.routineDao().update(routine)
                loadRoutines()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}