package com.neu.classmate.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

// Data model
data class Subtask(val title: String, val done: Boolean = false)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskView(taskId: String, title: String, dueDate: String, navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

    var showMenu by remember { mutableStateOf(false) }
    var showSubtaskInput by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var newSubtask by remember { mutableStateOf("") }
    var taskTitle by remember { mutableStateOf(title) }
    var isTitleEditing by remember { mutableStateOf(false) }
    val subtasks = remember { mutableStateListOf<Subtask>() }

    var listenerRegistration: ListenerRegistration? = remember { null }

    LaunchedEffect(Unit) {
        listenerRegistration?.remove()
        listenerRegistration = db.collection("users")
            .document(userId)
            .collection("tasks")
            .document(taskId)
            .addSnapshotListener { snapshot, _ ->
                val list = snapshot?.get("subtasks") as? List<*> ?: return@addSnapshotListener
                subtasks.clear()
                list.mapNotNullTo(subtasks) { item ->
                    val map = item as? Map<*, *> ?: return@mapNotNullTo null
                    val title = map["title"] as? String ?: return@mapNotNullTo null
                    val done = map["done"] as? Boolean ?: false
                    Subtask(title, done)
                }
            }
    }

    DisposableEffect(Unit) {
        onDispose { listenerRegistration?.remove() }
    }

    val progress by remember(subtasks) {
        derivedStateOf {
            if (subtasks.isNotEmpty()) subtasks.count { it.done } / subtasks.size.toFloat() else 0f
        }
    }

    fun updateSubtasks(newList: List<Subtask>) {
        val data = newList.map { mapOf("title" to it.title, "done" to it.done) }
        val progressMap = mapOf("completed" to newList.count { it.done }, "total" to newList.size)

        db.collection("users")
            .document(userId)
            .collection("tasks")
            .document(taskId)
            .update(mapOf(
                "title" to taskTitle,
                "subtasks" to data,
                "progress" to progressMap
            ))

        if (newList.isNotEmpty() && newList.all { it.done }) {
            val taskData = hashMapOf(
                "title" to taskTitle,
                "dueDate" to dueDate,
                "subtasks" to data,
                "progress" to progressMap
            )

            db.collection("users")
                .document(userId)
                .collection("completedTasks")
                .document(taskId)
                .set(taskData)
                .addOnSuccessListener {
                    db.collection("users")
                        .document(userId)
                        .collection("tasks")
                        .document(taskId)
                        .delete()
                        .addOnSuccessListener {
                            navController.popBackStack()
                        }
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Task View") },
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More Options")
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Add Subtask") },
                            onClick = {
                                showMenu = false
                                showSubtaskInput = true
                            },
                            leadingIcon = { Icon(Icons.Filled.Add, contentDescription = "Add") }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete Task") },
                            onClick = {
                                showMenu = false
                                showDeleteDialog = true
                            },
                            leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = "Delete") }
                        )
                    }
                }
            )
        },
        content = { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = taskTitle,
                        onValueChange = {
                            taskTitle = it
                            isTitleEditing = true
                        },
                        label = { Text("Task Title") },
                        trailingIcon = {
                            if (isTitleEditing && taskTitle.isNotBlank()) {
                                IconButton(onClick = {
                                    db.collection("users")
                                        .document(userId)
                                        .collection("tasks")
                                        .document(taskId)
                                        .update("title", taskTitle)
                                    isTitleEditing = false
                                }) {
                                    Icon(Icons.Default.Check, contentDescription = "Save Task Title")
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = dueDate,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Due Date") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }

                if (showSubtaskInput) {
                    item {
                        OutlinedTextField(
                            value = newSubtask,
                            onValueChange = { newSubtask = it },
                            label = { Text("New Subtask") },
                            singleLine = true,
                            trailingIcon = {
                                if (newSubtask.isNotBlank()) {
                                    IconButton(onClick = {
                                        val updated = subtasks.toList() + Subtask(newSubtask, false)
                                        updateSubtasks(updated)
                                        newSubtask = ""
                                    }) {
                                        Icon(Icons.Default.Check, contentDescription = "Add Subtask")
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                if (subtasks.isNotEmpty()) {
                    item {
                        val progressPercent = (progress * 100).toInt()
                        val progressLabel = if (progressPercent < 100) "In-progress" else "Complete"

                        Text("$progressPercent% - $progressLabel", style = MaterialTheme.typography.labelLarge)
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                itemsIndexed(subtasks) { index, subtask ->
                    var isEditing by remember { mutableStateOf(false) }
                    var editedText by remember { mutableStateOf(subtask.title) }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Checkbox(
                                checked = subtask.done,
                                onCheckedChange = { isChecked ->
                                    val updated = subtasks.toMutableList()
                                    updated[index] = updated[index].copy(done = isChecked)
                                    updateSubtasks(updated)
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))

                            if (isEditing) {
                                OutlinedTextField(
                                    value = editedText,
                                    onValueChange = { editedText = it },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = {
                                    if (editedText.isNotBlank()) {
                                        val updated = subtasks.toMutableList()
                                        updated[index] = updated[index].copy(title = editedText)
                                        updateSubtasks(updated)
                                        isEditing = false
                                    }
                                }) {
                                    Icon(Icons.Default.Check, contentDescription = "Save")
                                }
                            } else {
                                Text(
                                    text = subtask.title,
                                    style = TextStyle(
                                        textDecoration = if (subtask.done) TextDecoration.LineThrough else TextDecoration.None
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { isEditing = true }
                                )
                            }

                            IconButton(onClick = {
                                val updated = subtasks.toMutableList().apply { removeAt(index) }
                                updateSubtasks(updated)
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Subtask")
                            }
                        }
                    }
                }
                if (showDeleteDialog) {
                    item {
                        AlertDialog(
                            onDismissRequest = { showDeleteDialog = false },
                            title = { Text("Delete Task?") },
                            text = { Text("Are you sure you want to permanently delete this task?") },
                            confirmButton = {
                                TextButton(onClick = {
                                    showDeleteDialog = false
                                    db.collection("users")
                                        .document(userId)
                                        .collection("tasks")
                                        .document(taskId)
                                        .delete()
                                        .addOnSuccessListener {
                                            navController.popBackStack()
                                        }
                                }) {
                                    Text("Delete")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDeleteDialog = false }) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }
                }
            }
        }
    )
}
