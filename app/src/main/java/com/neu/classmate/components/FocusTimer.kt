package com.neu.classmate.components


import FocusTimerViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusTimer(
    modifier: Modifier,
    navController: NavController,
    focusTimerViewModel: FocusTimerViewModel
) {
    val context = LocalContext.current

    val timeLeft by focusTimerViewModel.timeLeft.collectAsStateWithLifecycle()
    val isRunning by focusTimerViewModel.isRunning.collectAsStateWithLifecycle()
    val isBreak by focusTimerViewModel.isBreak.collectAsStateWithLifecycle()
    val cycleCount by focusTimerViewModel.cycleCount.collectAsStateWithLifecycle()

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
                        onClick = { focusTimerViewModel.startTimer(context) },
                        enabled = !isRunning,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Text("Start")
                    }

                    Button(
                        onClick = { focusTimerViewModel.pauseTimer() },
                        enabled = isRunning,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Text("Pause")
                    }

                    Button(
                        onClick = { focusTimerViewModel.resetTimer() },
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Text("Reset")
                    }
                }
            }
        }
    )
}
