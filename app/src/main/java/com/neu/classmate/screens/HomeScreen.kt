package com.neu.classmate.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.neu.classmate.components.HeaderView

//
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await

@Composable
fun HomeScreen(modifier: Modifier = Modifier, navController: NavHostController) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val db = FirebaseFirestore.getInstance()

    var taskList by remember { mutableStateOf(listOf<String>()) }
    var showDialog by remember { mutableStateOf(false) }
    var taskText by remember { mutableStateOf("") }

    // 🔄 Load tasks once
    LaunchedEffect(Unit) {
        val snapshot = db.collection("users")
            .document(userId)
            .collection("tasks")
            .get()
            .await()

        taskList = snapshot.documents.mapNotNull { it.getString("title") }
    }

    Column {
        HeaderView(modifier, navController)

        Button(
            onClick = { showDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(text = "Add new task")
        }

        LazyColumn(
            modifier = Modifier
                .padding(top = 32.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(taskList) { task ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = task,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        style = TextStyle(fontSize = 16.sp)
                    )
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
                            val newTask = hashMapOf("title" to taskText)
                            db.collection("users")
                                .document(userId)
                                .collection("tasks")
                                .add(newTask)
                                .addOnSuccessListener {
                                    taskList = taskList + taskText
                                    showDialog = false
                                    taskText = ""
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



