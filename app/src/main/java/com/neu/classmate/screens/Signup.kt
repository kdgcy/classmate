package com.neu.classmate.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.neu.classmate.AppUtil
import com.neu.classmate.viewmodel.AuthViewModel

@Composable
fun Signup(modifier: Modifier = Modifier, navController: NavHostController,authViewModel: AuthViewModel = viewModel()) {

    var email by remember {
        mutableStateOf("")
    }

    var name by remember {
        mutableStateOf("")
    }

    var password by remember {
        mutableStateOf("")
    }

    var isLoading by remember {
        mutableStateOf(false)
    }

    var context = LocalContext.current

    Column(
        modifier = modifier.fillMaxSize().padding(31.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Hi there,",
            modifier = Modifier.fillMaxWidth(),
            style = TextStyle(
                fontSize = 32.sp,
                fontWeight = FontWeight.W700
            )
        )

        Text(
            text = "Register your account",
            modifier = Modifier.fillMaxWidth(),
            style = TextStyle(
                fontSize = 18.sp,
                fontWeight = FontWeight.W700
            )
        )

        Spacer(modifier= Modifier.height(20.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
            },
            label = {
                Text(text = "Full name")
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier= Modifier.height(10.dp))

        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
            },
            label = {
                Text(text = "Email")
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier= Modifier.height(10.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
            },
            label = {
                Text(text = "Password")
            },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(10.dp))

        Button(onClick = {
            isLoading = true
            authViewModel.signup(name, email, password){success,errorMessage->
                if(success){
                    isLoading = false
                    navController.navigate(Routes.HomeScreen){
                        popUpTo(Routes.AuthScreen){inclusive=true}
                    }
                }else{
                    isLoading = false
                    AppUtil.showToast(context,errorMessage?:"Something went wrong")
                }
            }
        },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)) {
            Text(text = if(isLoading)"Creating Accouont" else "Sign up")
        }
    }
}