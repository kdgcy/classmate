package com.neu.classmate.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

data class ReminderItem(
    val id: String,
    val title: String,
    val date: String,
    val time: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Reminder(navController: NavController) {
    var showDialog by remember { mutableStateOf(false) }
    var reminderTitle by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf("") }
    var reminderList by remember { mutableStateOf(listOf<ReminderItem>()) }

    val calendar = Calendar.getInstance()
    val context = LocalContext.current
    val dateFormatter = remember { SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()) }
    val timeFormatter = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

    val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    LaunchedEffect(Unit) {
        if (userId != null) {
            db.collection("users")
                .document(userId)
                .collection("reminders")
                .orderBy("createdAt")
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        reminderList = snapshot.documents.mapNotNull { doc ->
                            val title = doc.getString("title") ?: ""
                            val date = doc.getString("date") ?: ""
                            val time = doc.getString("time") ?: ""
                            ReminderItem(doc.id, title, date, time)
                        }
                    }
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reminder", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBackIosNew, contentDescription = "Arrow back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        //I implement this later
                    }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
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
                Button(
                    onClick = { showDialog = true },
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .fillMaxWidth(0.75f)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add reminder")
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn {
                    items(reminderList) { reminder ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = reminder.title.ifBlank { "Untitled Reminder" },
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Date: ${reminder.date}", fontSize = 12.sp)
                                Text("Time: ${reminder.time}", fontSize = 12.sp)
                            }
                        }
                    }
                }

                if (showDialog) {
                    AlertDialog(
                        onDismissRequest = {
                            showDialog = false
                            reminderTitle = ""
                            selectedDate = ""
                            selectedTime = ""
                        },
                        title = { Text("Set Reminder") },
                        text = {
                            Column {
                                OutlinedTextField(
                                    value = reminderTitle,
                                    onValueChange = { reminderTitle = it },
                                    label = { Text("Title")},
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            DatePickerDialog(
                                                context,
                                                { _, year, month, dayOfMonth ->
                                                    calendar.set(year, month, dayOfMonth)
                                                    selectedDate = dateFormatter.format(calendar.time)
                                                },
                                                calendar.get(Calendar.YEAR),
                                                calendar.get(Calendar.MONTH),
                                                calendar.get(Calendar.DAY_OF_MONTH)
                                            ).show()
                                        }
                                ) {
                                    OutlinedTextField(
                                        value = selectedDate,
                                        onValueChange = {},
                                        label = { Text("Select date") },
                                        readOnly = true,
                                        enabled = false,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            TimePickerDialog(
                                                context,
                                                { _, hourOfDay, minute ->
                                                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                                                    calendar.set(Calendar.MINUTE, minute)
                                                    selectedTime = timeFormatter.format(calendar.time)
                                                },
                                                calendar.get(Calendar.HOUR_OF_DAY),
                                                calendar.get(Calendar.MINUTE),
                                                false
                                            ).show()
                                        }
                                ) {
                                    OutlinedTextField(
                                        value = selectedTime,
                                        onValueChange = {},
                                        label = { Text("Select time") },
                                        readOnly = true,
                                        enabled = false,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                if (userId != null && selectedDate.isNotBlank() && selectedTime.isNotBlank()) {
                                    val reminderData = hashMapOf(
                                        "title" to reminderTitle,
                                        "date" to selectedDate,
                                        "time" to selectedTime,
                                        "createdAt" to com.google.firebase.Timestamp.now()
                                    )

                                    db.collection("users")
                                        .document(userId)
                                        .collection("reminders")
                                        .add(reminderData)
                                }

                                showDialog = false
                                reminderTitle = ""
                                selectedDate = ""
                                selectedTime = ""
                            }) {
                                Text("Save")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = {
                                showDialog = false
                                reminderTitle = ""
                                selectedDate = ""
                                selectedTime = ""
                            }) {
                                Text("Cancel")
                            }
                        }
                    )
                }
            }
        }
    )
}
