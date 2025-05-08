package com.neu.classmate.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskView(taskId: String, title: String, dueDate: String, navController: NavHostController) {
    val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

    var showMenu by remember { mutableStateOf(false) }
    var showSubtaskInput by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    var newSubtask by remember { mutableStateOf("") }
    var subtasks by remember { mutableStateOf(listOf<String>()) }

    // Load subtasks from Firestore
    LaunchedEffect(Unit) {
        db.collection("users")
            .document(userId)
            .collection("tasks")
            .document(taskId)
            .get()
            .addOnSuccessListener { document ->
                val list = document.get("subtasks") as? List<*>
                subtasks = list?.mapNotNull { it as? String } ?: emptyList()
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Task View")
                },
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More Options"
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Add Subtask") },
                            onClick = {
                                showMenu = false
                                showSubtaskInput = true
                            },
                            leadingIcon = {
                                Icon(imageVector = Icons.Filled.Add, contentDescription = "Add")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete Task") },
                            onClick = {
                                showMenu = false
                                showDeleteDialog = true // Show confirmation dialog
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = "Trash"
                                )
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

                Spacer(modifier = Modifier.height(16.dp))

                if (showSubtaskInput) {
                    Text("Subtasks", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = newSubtask,
                        onValueChange = { newSubtask = it },
                        label = { Text("New Subtask") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            if (newSubtask.isNotBlank()) {
                                val updatedSubtasks = subtasks + newSubtask

                                db.collection("users")
                                    .document(userId)
                                    .collection("tasks")
                                    .document(taskId)
                                    .update("subtasks", updatedSubtasks)
                                    .addOnSuccessListener {
                                        subtasks = updatedSubtasks
                                        newSubtask = ""
                                    }
                            }
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Add Subtask")
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Display subtasks
                subtasks.forEach { subtask ->
                    OutlinedTextField(
                        value = subtask,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        label = { Text("Subtask") }
                    )
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
                                        navController.popBackStack() // navigate back
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
