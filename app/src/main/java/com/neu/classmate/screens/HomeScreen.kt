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

@Composable
fun HomeScreen(modifier: Modifier = Modifier, navController: NavHostController) {
    var taskList by remember { mutableStateOf(listOf<String>()) }
    var showDialog by remember { mutableStateOf(false) }
    var taskText by remember { mutableStateOf("") }

    Column{
        HeaderView(modifier,navController)
        Button(onClick = {
            //Dito i-implement ko yung AlertDialog through State
            showDialog = true
        },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(text = "Add new task")
        }

        //LazyColumn for taskList here....
        LazyColumn(
            modifier = Modifier.padding(top = 32.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(taskList) { task ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = task,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        style = TextStyle(fontSize = 16.sp)
                    )
                }
            }
        }//LazyColumn END

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
                            taskList = taskList + taskText
                            showDialog = false
                            taskText = ""
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
    }//Column End
}//HomeScreen END



