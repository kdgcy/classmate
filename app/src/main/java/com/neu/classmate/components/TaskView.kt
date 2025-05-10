package com.neu.classmate.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
    var subtasks by remember { mutableStateOf(listOf<Subtask>()) }

    var listenerRegistration: ListenerRegistration? = remember { null }

    LaunchedEffect(Unit) {
        listenerRegistration?.remove()
        listenerRegistration = db.collection("users")
            .document(userId)
            .collection("tasks")
            .document(taskId)
            .addSnapshotListener { snapshot, _ ->
                snapshot?.let { document ->
                    val list = document.get("subtasks") as? List<Map<String, Any>>
                    subtasks = list?.mapNotNull {
                        val title = it["title"] as? String ?: return@mapNotNull null
                        val done = it["done"] as? Boolean ?: false
                        Subtask(title, done)
                    } ?: emptyList()
                }
            }
    }

    DisposableEffect(Unit) {
        onDispose {
            listenerRegistration?.remove()
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
                            leadingIcon = {
                                Icon(Icons.Filled.Add, contentDescription = "Add")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete Task") },
                            onClick = {
                                showMenu = false
                                showDeleteDialog = true
                            },
                            leadingIcon = {
                                Icon(Icons.Filled.Delete, contentDescription = "Trash")
                            }
                        )
                    }
                }
            )
        },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Task Title") },
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

                if (showSubtaskInput) {
                    Text("Subtasks", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = newSubtask,
                        onValueChange = { newSubtask = it },
                        label = { Text("New Subtask") },
                        singleLine = true,
                        trailingIcon = {
                            IconButton(onClick = {
                                if (newSubtask.isNotBlank()) {
                                    val updatedSubtasks = subtasks + Subtask(newSubtask, false)
                                    val data = updatedSubtasks.map { mapOf("title" to it.title, "done" to it.done) }

                                    db.collection("users")
                                        .document(userId)
                                        .collection("tasks")
                                        .document(taskId)
                                        .update("subtasks", data)
                                        .addOnSuccessListener { newSubtask = "" }
                                }
                            }) {
                                Icon(Icons.Default.Add, contentDescription = "Add")
                            }
                        },
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                }

                subtasks.forEachIndexed { index, subtask ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Checkbox(
                                checked = subtask.done,
                                onCheckedChange = { isChecked ->
                                    val updated = subtasks.toMutableList()
                                    updated[index] = updated[index].copy(done = isChecked)
                                    val data = updated.map { mapOf("title" to it.title, "done" to it.done) }
                                    db.collection("users")
                                        .document(userId)
                                        .collection("tasks")
                                        .document(taskId)
                                        .update("subtasks", data)
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = subtask.title,
                                style = TextStyle(
                                    textDecoration = if (subtask.done) TextDecoration.LineThrough else TextDecoration.None
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = {
                                val updated = subtasks.toMutableList().apply { removeAt(index) }
                                val data = updated.map { mapOf("title" to it.title, "done" to it.done) }
                                db.collection("users")
                                    .document(userId)
                                    .collection("tasks")
                                    .document(taskId)
                                    .update("subtasks", data)
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Subtask")
                            }
                        }
                    }
                }

                if (showDeleteDialog) {
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
    )
}
