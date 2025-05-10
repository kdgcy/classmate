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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch

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
    val subtasks = remember { mutableStateListOf<Subtask>() }
    val snackbarHostState = remember { SnackbarHostState() }
    val view = LocalView.current
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    val imeBottomPadding = with(density) {
        val insets = ViewCompat.getRootWindowInsets(view)?.getInsets(WindowInsetsCompat.Type.ime())
        insets?.bottom?.toDp() ?: 0.dp
    }

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
                "subtasks" to data,
                "progress" to progressMap
            ))
    }

    Scaffold(
        snackbarHost = {
            Box(modifier = Modifier.fillMaxSize()) {
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = imeBottomPadding + 8.dp)
                )
            }
        },
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
                            leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = "Trash") }
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
                    OutlinedTextField(
                        value = newSubtask,
                        onValueChange = { newSubtask = it },
                        label = { Text("New Subtask") },
                        singleLine = true,
                        trailingIcon = {
                            IconButton(onClick = {
                                if (newSubtask.isNotBlank()) {
                                    val updated = subtasks.toList() + Subtask(newSubtask, false)
                                    updateSubtasks(updated)
                                    newSubtask = ""
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Subtask added successfully")
                                    }
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

                if (subtasks.isNotEmpty()) {
                    Text("${(progress * 100).toInt()}% Complete", style = MaterialTheme.typography.labelLarge)
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                } else {
                    Text(
                        text = "No subtasks yet.",
                        modifier = Modifier.alpha(0.6f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                subtasks.forEachIndexed { index, subtask ->
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
                            Text(
                                text = subtask.title,
                                style = TextStyle(
                                    textDecoration = if (subtask.done) TextDecoration.LineThrough else TextDecoration.None
                                ),
                                modifier = Modifier.weight(1f)
                            )
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
