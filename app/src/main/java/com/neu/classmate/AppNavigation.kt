package com.neu.classmate

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.neu.classmate.screens.AuthScreen
import com.neu.classmate.screens.Login
import com.neu.classmate.screens.Routes
import com.neu.classmate.screens.Signup

@Composable
fun AppNavigation(modifier: Modifier = Modifier){
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.AuthScreen) {
        composable(Routes.AuthScreen) {
            AuthScreen(modifier,navController)
        }
        composable(Routes.Login) {
            Login(modifier,navController)
        }
        composable(Routes.Signup) {
            Signup(modifier,navController)
        }
    }
}