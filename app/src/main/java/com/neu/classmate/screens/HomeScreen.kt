package com.neu.classmate.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import java.util.*

data class TaskItem(
    val id: String,
    val title: String,
    val dueDate: String,
    val percentComplete: Float = 0f
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavHostController) {
    val db = Firebase.firestore
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

    var name by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var taskText by remember { mutableStateOf("") }
    var dueDateText by remember { mutableStateOf("") }
    var dueTimeText by remember { mutableStateOf("") }
    var taskList by remember { mutableStateOf(listOf<TaskItem>()) }

    val calendar = Calendar.getInstance()
    val timeCalendar = Calendar.getInstance()
    val dateFormatter = remember { SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()) }
    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    fun listenToTasks() {
        db.collection("users")
            .document(userId)
            .collection("tasks")
            .orderBy("dueDateTimestamp")
            .addSnapshotListener { result, _ ->
                if (result != null) {
                    taskList = result.documents.mapNotNull { doc ->
                        val title = doc.getString("title") ?: return@mapNotNull null
                        val dueDate = doc.getString("dueDate") ?: "No due date"
                        val subtasks = doc.get("subtasks") as? List<Map<String, Any>> ?: emptyList()
                        val total = subtasks.size
                        val completed = subtasks.count { it["done"] == true }
                        val percentComplete = if (total > 0) completed.toFloat() / total else 0f
                        TaskItem(doc.id, title, dueDate, percentComplete)
                    }
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
        listenToTasks()
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.fillMaxWidth(0.7f)) {
                Box(modifier = Modifier.fillMaxHeight()) {
                    Column(modifier = Modifier.align(Alignment.TopStart)) {
                        Text(name, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(16.dp))
                        HorizontalDivider()
                        listOf(
                            "Profile" to Icons.Filled.AccountCircle,
                            "Calendar" to Icons.Filled.CalendarMonth,
                            "Focus Timer" to Icons.Filled.Timer,
                            "Task Completed" to Icons.Filled.CheckCircle
                        ).forEach { (label, icon) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        when (label) {
                                            "Profile" -> navController.navigate(Routes.Profile)
                                            "Focus Timer" -> navController.navigate(Routes.FocusTimer)
                                            "Task Completed" -> navController.navigate(Routes.TaskCompleted)
                                        }
                                    }
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
                            Icon(Icons.Filled.Menu, contentDescription = "Menu")
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
                                    Spacer(modifier = Modifier.height(4.dp))

                                    val progressPercent = (task.percentComplete * 100).toInt()
                                    val showProgress = task.percentComplete > 0f

                                    if (showProgress) {
                                        val progressLabel = if (progressPercent < 100) "In-progress" else "Complete"
                                        Text("$progressPercent% - $progressLabel", style = MaterialTheme.typography.labelSmall)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        LinearProgressIndicator(
                                            progress = task.percentComplete,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(6.dp)
                                        )
                                    } else {
                                        Spacer(modifier = Modifier.height(20.dp))
                                    }
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
                                dueTimeText = ""
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

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable(
                                                indication = null,
                                                interactionSource = interactionSource
                                            ) {
                                                TimePickerDialog(
                                                    context,
                                                    { _, hour, minute ->
                                                        timeCalendar.set(Calendar.HOUR_OF_DAY, hour)
                                                        timeCalendar.set(Calendar.MINUTE, minute)
                                                        dueTimeText = String.format("%02d:%02d", hour, minute)
                                                    },
                                                    timeCalendar.get(Calendar.HOUR_OF_DAY),
                                                    timeCalendar.get(Calendar.MINUTE),
                                                    false
                                                ).show()
                                            }
                                    ) {
                                        OutlinedTextField(
                                            value = dueTimeText,
                                            onValueChange = {},
                                            label = { Text("Select due time") },
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
                                    if (taskText.isNotBlank() && dueDateText.isNotBlank() && dueTimeText.isNotBlank()) {
                                        val newTask = hashMapOf(
                                            "title" to taskText,
                                            "dueDate" to "$dueDateText $dueTimeText",
                                            "dueDateTimestamp" to calendar.time,
                                            "createdAt" to com.google.firebase.Timestamp.now()
                                        )
                                        db.collection("users")
                                            .document(userId)
                                            .collection("tasks")
                                            .add(newTask)
                                        taskText = ""
                                        dueDateText = ""
                                        dueTimeText = ""
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
                                    dueTimeText = ""
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
