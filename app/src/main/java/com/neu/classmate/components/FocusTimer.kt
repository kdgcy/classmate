package com.neu.classmate.components


import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.neu.classmate.viewmodel.FocusTimerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusTimer(
    modifier: Modifier,
    navController: NavController,
    focusTimerViewModel: FocusTimerViewModel
) {
    val viewModel = focusTimerViewModel
    val timeLeft by viewModel.timeLeft.collectAsState()
    val isRunning by viewModel.isRunning.collectAsState()
    val isBreak by viewModel.isBreak.collectAsState()
    val cycleCount by viewModel.cycleCount.collectAsState()

    val minutes = timeLeft / 60
    val seconds = timeLeft % 60
    val formattedTime = String.format("%02d:%02d", minutes, seconds)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Focus Timer", fontWeight = FontWeight.Bold) },
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
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (isBreak) "Break Time" else "Focus Time",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = formattedTime,
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Cycle $cycleCount of 4",
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(32.dp))

                Row(horizontalArrangement = Arrangement.SpaceEvenly) {
                    Button(
                        onClick = { viewModel.startTimer() },
                        enabled = !isRunning,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Text("Start")
                    }

                    Button(
                        onClick = { viewModel.pauseTimer() },
                        enabled = isRunning,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Text("Pause")
                    }

                    Button(
                        onClick = { viewModel.resetTimer() },
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Text("Reset")
                    }
                }
            }
        }
    )
}
