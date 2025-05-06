package com.neu.classmate.screens

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.neu.classmate.components.HeaderView
import kotlinx.coroutines.tasks.await

data class Task(
    val id: String,
    val title: String,
    val dueDate: String? = null
)

@Composable
fun HomeScreen(modifier: Modifier = Modifier, navController: NavHostController) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val db = FirebaseFirestore.getInstance()

    var taskList by remember { mutableStateOf(listOf<Task>()) }
    var showDialog by remember { mutableStateOf(false) }
    var taskText by remember { mutableStateOf("") }
    var refreshTrigger by remember { mutableStateOf(false) }

    suspend fun loadTasks() {
        try {
            val snapshot = db.collection("users")
                .document(userId)
                .collection("tasks")
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .get()
                .await()

            taskList = snapshot.documents.mapNotNull { doc ->
                val title = doc.getString("title")
                val dueDate = doc.getString("dueDate")
                val id = doc.id
                if (title != null) Task(id = id, title = title, dueDate = dueDate) else null
            }
        } catch (e: Exception) {
            Log.e("Firestore", "Error fetching tasks", e)
        }
    }

    LaunchedEffect(refreshTrigger) {
        loadTasks()
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        HeaderView(modifier,navController)

        Button(
            onClick = { showDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Add new task")
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(8.dp)
        ) {
            items(taskList) { task ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            navController.navigate("task_view/${Uri.encode(task.title)}")
                        }
                        .padding(horizontal = 4.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = task.title,
                            style = TextStyle(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                                )
                        )
                        if (!task.dueDate.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Due: ${task.dueDate}",
                                style = TextStyle(fontSize = 12.sp)
                            )
                        }
                    }
                }
            }
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = {
                    showDialog = false
                    taskText = ""
                },
                title = { Text("Add New Task") },
                text = {
                    OutlinedTextField(
                        value = taskText,
                        onValueChange = { taskText = it },
                        label = { Text("Enter task") },
                        singleLine = true
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (taskText.isNotBlank()) {
                            val newTask = hashMapOf(
                                "title" to taskText,
                                "createdAt" to FieldValue.serverTimestamp()
                            )
                            db.collection("users")
                                .document(userId)
                                .collection("tasks")
                                .add(newTask)
                                .addOnSuccessListener {
                                    showDialog = false
                                    taskText = ""
                                    refreshTrigger = !refreshTrigger
                                }
                                .addOnFailureListener { e ->
                                    Log.e("Firestore", "Error adding task", e)
                                }
                        }
                    }) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showDialog = false
                        taskText = ""
                    }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
