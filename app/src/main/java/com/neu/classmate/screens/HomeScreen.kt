package com.neu.classmate.screens


import android.app.DatePickerDialog
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class TaskItem(val id: String, val title: String, val dueDate: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(modifier: Modifier = Modifier, navController: NavHostController) {
    val db = Firebase.firestore
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

    var name by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var taskText by remember { mutableStateOf("") }
    var dueDateText by remember { mutableStateOf("") }
    var taskList by remember { mutableStateOf(listOf<TaskItem>()) }

    val calendar = Calendar.getInstance()
    val dateFormatter = remember { SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()) }
    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    fun loadTasks() {
        db.collection("users")
            .document(userId)
            .collection("tasks")
            .orderBy("dueDateTimestamp")
            .get()
            .addOnSuccessListener { result ->
                taskList = result.documents.mapNotNull { doc ->
                    val title = doc.getString("title") ?: return@mapNotNull null
                    val dueDate = doc.getString("dueDate") ?: "No due date"
                    TaskItem(doc.id, title, dueDate)
                }
            }
    }

    LaunchedEffect(Unit) {
        db.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                name = document.getString("name") ?: ""
            }
        loadTasks()
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.fillMaxWidth(0.7F)) {
                Box(modifier = Modifier.fillMaxHeight()) {
                    Column(modifier = Modifier.align(Alignment.TopStart)) {
                        Text(name, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(16.dp))
                        HorizontalDivider()

                        listOf("Profile" to Icons.Filled.AccountCircle,
                            "Calendar" to Icons.Filled.CalendarMonth,
                            "Focus Timer" to Icons.Filled.Timer).forEach { (label, icon) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { if (label == "Focus Timer") navController.navigate(Routes.FocusTimer) }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(icon, contentDescription = label)
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(label)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .clickable {
                                Firebase.auth.signOut()
                                navController.navigate(Routes.AuthScreen) {
                                    popUpTo(Routes.HomeScreen) { inclusive = true }
                                }
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout")
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Logout")
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Dashboard", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Hamburger Menu")
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
                        Text("Add new task")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    LazyColumn {
                        items(taskList) { task ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val encodedTitle = Uri.encode(task.title)
                                        val encodedDue = Uri.encode(task.dueDate)
                                        navController.navigate("task_view/${task.id}/$encodedTitle/$encodedDue")
                                    }
                                    .padding(horizontal = 4.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(24.dp)) {
                                    Text(task.title, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Due: ${task.dueDate}", style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }

                    if (showDialog) {
                        AlertDialog(
                            onDismissRequest = {
                                showDialog = false
                                taskText = ""
                                dueDateText = ""
                            },
                            title = { Text("Add New Task") },
                            text = {
                                Column {
                                    OutlinedTextField(
                                        value = taskText,
                                        onValueChange = { taskText = it },
                                        label = { Text("Enter new task") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    val interactionSource = remember { MutableInteractionSource() }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable(
                                                indication = null,
                                                interactionSource = interactionSource
                                            ) {
                                                DatePickerDialog(
                                                    context,
                                                    { _, year, month, dayOfMonth ->
                                                        calendar.set(year, month, dayOfMonth)
                                                        dueDateText = dateFormatter.format(calendar.time)
                                                    },
                                                    calendar.get(Calendar.YEAR),
                                                    calendar.get(Calendar.MONTH),
                                                    calendar.get(Calendar.DAY_OF_MONTH)
                                                ).show()
                                            }
                                    ) {
                                        OutlinedTextField(
                                            value = dueDateText,
                                            onValueChange = {},
                                            label = { Text("Select due date") },
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
                                    if (taskText.isNotBlank()) {
                                        val newTask = hashMapOf(
                                            "title" to taskText,
                                            "dueDate" to dueDateText,
                                            "dueDateTimestamp" to calendar.time,
                                            "createdAt" to com.google.firebase.Timestamp.now()
                                        )
                                        db.collection("users")
                                            .document(userId)
                                            .collection("tasks")
                                            .add(newTask)
                                            .addOnSuccessListener {
                                                loadTasks()
                                            }
                                        taskText = ""
                                        dueDateText = ""
                                    }
                                }) {
                                    Text("Add")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = {
                                    showDialog = false
                                    taskText = ""
                                    dueDateText = ""
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
}
