package com.neu.classmate.screens
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.neu.classmate.components.HeaderView

@Composable
fun HomeScreen(modifier: Modifier = Modifier, navController: NavHostController) {
    Column(
    ) {
        HeaderView(modifier,navController)
    }//Column End
}//HomeScreen END

