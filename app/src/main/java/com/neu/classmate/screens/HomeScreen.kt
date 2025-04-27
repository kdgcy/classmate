package com.neu.classmate.screens
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.neu.classmate.components.HeaderView

@Composable
fun HomeScreen(modifier: Modifier = Modifier, navController: NavHostController) {


    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
    ) {
        HeaderView()
        // Logout Button
        Button(
            onClick = {
                Firebase.auth.signOut()
                navController.navigate(Routes.AuthScreen) {
                    popUpTo(Routes.HomeScreen) { inclusive = true }
                }
            },
            modifier = Modifier.fillMaxWidth().height(65.dp)
        ) {
            Text("Logout")
        }

    }//Column End
}//HomeScreen END

