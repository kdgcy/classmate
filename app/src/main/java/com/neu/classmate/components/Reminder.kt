package com.neu.classmate.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Reminder(navController: NavController) {
    var showDialog by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf("") }

    val calendar = Calendar.getInstance()
    val context = LocalContext.current
    val dateFormatter = remember { SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()) }
    val timeFormatter = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reminder", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBackIosNew, contentDescription = "Arrow back")
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
                    // items(reminderList) { Card() }
                }

                if (showDialog) {
                    AlertDialog(
                        onDismissRequest = {
                            showDialog = false
                            selectedDate = ""
                            selectedTime = ""
                        },
                        title = { Text("Set Reminder") },
                        text = {
                            Column {
                                OutlinedTextField(
                                    value = "",
                                    onValueChange = {},
                                    label = { Text("Title")}
                                )
                                // Date Picker
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

                                // Time Picker
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
                                showDialog = false
                                // You can add your Firestore saving logic here
                            }) {
                                Text("Save")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = {
                                showDialog = false
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
