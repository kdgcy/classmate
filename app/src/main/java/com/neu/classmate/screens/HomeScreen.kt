package com.neu.classmate.screens

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.neu.classmate.components.HeaderView
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale

data class Task(val title: String, val createdAt: String)

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
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            taskList = snapshot.documents.mapNotNull { doc ->
                val title = doc.getString("title")
                val timestamp = doc.getTimestamp("createdAt")?.toDate()
                if (title != null && timestamp != null) {
                    Task(title, formatter.format(timestamp))
                } else null
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
                        .padding(horizontal = 4.dp)
                        .clickable {
                            navController.navigate("task_view/${Uri.encode(task.title)}")
                        },
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = task.title,
                            style = TextStyle(fontSize = 16.sp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = task.createdAt,
                            style = TextStyle(fontSize = 12.sp, color = Color.Gray)
                        )
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