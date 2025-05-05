package com.neu.classmate.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.neu.classmate.screens.Routes

@Composable
fun HeaderView(
    modifier: Modifier = Modifier,
    navController: NavHostController
) {
    // Get user's name from Firestore
    var name by remember { mutableStateOf("") }

    // For showing/hiding the dropdown
    var expanded by remember { mutableStateOf(false) }

    // Fetch name when composable launches
    LaunchedEffect(Unit) {
        Firebase.firestore.collection("users")
            .document(FirebaseAuth.getInstance().currentUser?.uid!!)
            .get()
            .addOnSuccessListener { document ->
                name = document.getString("name") ?: ""
            }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "Welcome back,",
                style = TextStyle(fontSize = 14.sp)
            )

            Text(
                text = name,
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        Box {
            IconButton(onClick = {
                expanded = true // Open dropdown when icon clicked
            }) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Expandable Setting",
                    modifier = Modifier.size(32.dp)
                )
            }

            // Dropdown menu
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    leadingIcon = {
                        Icon(Icons.Rounded.AccountCircle,
                            contentDescription = null)
                    },
                    text = { Text("Profile") },
                    onClick = {
                        expanded = false
                        //implement code here...
                    }
                )
                DropdownMenuItem(
                    leadingIcon = {
                        Icon(Icons.Rounded.Done,
                            contentDescription = null)
                    },
                    text = { Text("Logout") },
                    onClick = {
                        expanded = false
                        Firebase.auth.signOut()
                        navController.navigate(Routes.AuthScreen){
                            popUpTo(Routes.HomeScreen){inclusive=true}
                        }
                    }
                )
            }//Dropdown END
        }//Box END
    }//Row END
}//Fun END