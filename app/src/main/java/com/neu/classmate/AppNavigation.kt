package com.neu.classmate

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.neu.classmate.components.TaskView
import com.neu.classmate.screens.AuthScreen
import com.neu.classmate.screens.HomeScreen
import com.neu.classmate.screens.Login
import com.neu.classmate.screens.Routes
import com.neu.classmate.screens.Signup

@Composable
fun AppNavigation(modifier: Modifier = Modifier){
    val navController = rememberNavController()

    val isLoggedIn = Firebase.auth.currentUser != null

    val firstPage = if(isLoggedIn) Routes.HomeScreen else Routes.AuthScreen

    NavHost(navController = navController, startDestination = firstPage) {
        composable(Routes.AuthScreen) {
            AuthScreen(modifier,navController)
        }
        composable(Routes.Login) {
            Login(modifier,navController)
        }
        composable(Routes.Signup) {
            Signup(modifier,navController)
        }
        composable(Routes.HomeScreen) {
            HomeScreen(modifier,navController)
        }
        composable("task_view/{taskTitle}") { backStackEntry ->
            val taskTitle = backStackEntry.arguments?.getString("taskTitle") ?: ""
            TaskView(taskTitle)
        }
    }
}