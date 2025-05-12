package com.neu.classmate.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

data class CompletedTask(
    val id: String,
    val title: String,
    val dueDate: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskCompleted() {
    val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    var completedTasks by remember { mutableStateOf(listOf<CompletedTask>()) }
    var showMenu by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }


    LaunchedEffect(Unit) {
        db.collection("users")
            .document(userId)
            .collection("completedTasks") // Make sure this matches Firestore structure
            .get()
            .addOnSuccessListener { result ->
                completedTasks = result.documents.mapNotNull { doc ->
                    val title = doc.getString("title") ?: return@mapNotNull null
                    val dueDate = doc.getString("dueDate") ?: return@mapNotNull null
                    CompletedTask(doc.id, title, dueDate)
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Completed Tasks") },
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Sort by Title") },
                            onClick = {
                                showMenu = false
                                completedTasks = completedTasks.sortedBy { it.title }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Sort by Due Date") },
                            onClick = {
                                showMenu = false
                                completedTasks = completedTasks.sortedBy { it.dueDate }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Clear all tasks") },
                            onClick = {
                                showMenu = false
                                showClearDialog = true
                            }
                        )
                    }
                }
            )
        },
        content = { padding ->
            if (completedTasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No completed tasks yet.", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.alpha(0.5f))
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp)
                ) {
                    items(completedTasks) { task ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(task.title, style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Due: ${task.dueDate}", style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp))
                            }
                        }
                    }

                }
                if (showClearDialog) {
                    AlertDialog(
                        onDismissRequest = { showClearDialog = false },
                        title = { Text("Confirm Deletion") },
                        text = { Text("Are you sure you want to permanently delete all completed tasks? This action cannot be undone.") },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    showClearDialog = false
                                    val batch = db.batch()
                                    completedTasks.forEach { task ->
                                        val docRef = db.collection("users")
                                            .document(userId)
                                            .collection("completedTasks")
                                            .document(task.id)
                                        batch.delete(docRef)
                                    }
                                    batch.commit().addOnSuccessListener {
                                        completedTasks = emptyList()
                                    }
                                }
                            ) {
                                Text("Delete")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showClearDialog = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }

            }
        }
    )
}
