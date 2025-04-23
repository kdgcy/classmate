package com.neu.classmate.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.neu.classmate.R

@Composable
fun AuthScreen(modifier: Modifier = Modifier, navController: NavHostController){
    Column(modifier = modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            painter = painterResource(id = R.drawable.img_placeholder),
            contentDescription = "Logo"
        )

        Text(
            text = "ClassMate",
            style = TextStyle(
                fontSize = 32.sp,
                fontWeight = FontWeight.W700,
            )
        )

        Text(
            text = "Your Academic Sidekick for Tackling Tasks, Deadlines, and Everything in Between!",
            style = TextStyle(
                textAlign = TextAlign.Center
            )
        )

        Spacer(modifier = Modifier.height(72.dp))

        Button(onClick = {
            navController.navigate(Routes.Login)
        },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)) {
            Text(text = "Login")
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(onClick = {
            navController.navigate(Routes.Signup)
        },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)) {
            Text(text = "Sign up")
        }
    }
}