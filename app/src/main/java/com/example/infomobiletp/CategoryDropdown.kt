package com.example.infomobiletp

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDropdown(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    val context = LocalContext.current
    // A key to force reload the list when a new category is added.
    var reloadKey by remember { mutableIntStateOf(0) }
    // Load categories from Room using produceState.
    val categories by produceState(initialValue = emptyList<String>(), key1 = reloadKey) {
        val db = AppDatabase.getDatabase(context)
        value = withContext(Dispatchers.IO) {
            db.categoryDao().getAll().map { it.name }
        }
    }

    var expanded by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var newCategoryText by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedCategory,
            onValueChange = {},
            readOnly = true,
            label = { Text("Catégorie") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
                .clickable { expanded = true }
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            // Display the categories loaded from Room.
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category) },
                    onClick = {
                        onCategorySelected(category)
                        expanded = false
                    }
                )
            }
            // Option to add a new category.
            DropdownMenuItem(
                text = { Text("Add new category") },
                onClick = {
                    expanded = false
                    showAddDialog = true
                }
            )
        }
    }
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add New Category") },
            text = {
                OutlinedTextField(
                    value = newCategoryText,
                    onValueChange = { newCategoryText = it },
                    label = { Text("Category Name") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    coroutineScope.launch {
                        val db = AppDatabase.getDatabase(context)
                        // Check for duplicate (ignoring case)
                        val duplicate = categories.find { it.equals(newCategoryText, ignoreCase = true) }
                        if (duplicate != null) {
                            onCategorySelected(duplicate)
                        } else {
                            withContext(Dispatchers.IO) {
                                db.categoryDao().insert(Category(name = newCategoryText))
                            }
                            // Update reloadKey to trigger reloading categories.
                            reloadKey++
                            onCategorySelected(newCategoryText)
                        }
                    }
                    newCategoryText = ""
                    showAddDialog = false
                }) {
                    Text("Add")
                }
            },
            dismissButton = {
                Button(onClick = {
                    newCategoryText = ""
                    showAddDialog = false
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurrenceDropdown(
    selectedRecurrence: RecurrenceType,
    onRecurrenceSelected: (RecurrenceType) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedRecurrence.toFrench(),
            onValueChange = {},
            readOnly = true,
            label = { Text("Récurrence") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            RecurrenceType.values().forEach { recurrence ->
                DropdownMenuItem(
                    text = { Text(recurrence.toFrench()) },
                    onClick = {
                        onRecurrenceSelected(recurrence)
                        expanded = false
                    }
                )
            }
        }
    }
}