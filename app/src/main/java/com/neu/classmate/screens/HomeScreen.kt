package com.neu.classmate.screens
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.neu.classmate.components.HeaderView

@Composable
fun HomeScreen(modifier: Modifier = Modifier, navController: NavHostController) {
    Column(
    ) {
        HeaderView(modifier,navController)

        LazyColumn(
            modifier = Modifier.padding(top = 32.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(16.dp)
        ) {

        }//LazyColumn END
    }//Column End
}//HomeScreen END

