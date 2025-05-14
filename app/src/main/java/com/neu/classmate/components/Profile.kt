package com.neu.classmate.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Profile(modifier: Modifier = Modifier, navController: NavController) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val userId = currentUser?.uid

    if (userId == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Loading user...", style = MaterialTheme.typography.bodyMedium)
        }
        return
    }

    val db = FirebaseFirestore.getInstance()

    var expanded by remember { mutableStateOf(false) }
    var isEditMode by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var showValidationError by remember { mutableStateOf(false) }

    // Profile fields
    var username by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    // Fetch Firestore data once
    LaunchedEffect(Unit) {
        try {
            val doc = db.collection("users").document(userId).get().await()
            username = doc.getString("username") ?: ""
            firstName = doc.getString("firstName") ?: ""
            lastName = doc.getString("lastName") ?: ""
            email = doc.getString("email") ?: ""
        } catch (e: Exception) {
            Log.e("Profile", "Failed to fetch user data", e)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { expanded = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Menu")
                    }

                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        DropdownMenuItem(
                            text = { Text(if (isEditMode) "Cancel Edit" else "Edit Profile") },
                            onClick = {
                                expanded = false
                                isEditMode = !isEditMode
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete Account") },
                            onClick = {
                                expanded = false
                                // Optional: delete logic
                            }
                        )
                    }
                }
            )
        },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF424242))
                        .clickable(enabled = isEditMode) {
                            // TODO: Implement photo selection/upload
                        }
                ) {
                    Icon(
                        imageVector = Icons.Filled.AddAPhoto,
                        contentDescription = "Add image",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(5.dp))

                TextButton(onClick = {
                    // Future: Add photo logic
                }) {
                    Text("Add photo", style = TextStyle(fontWeight = FontWeight.ExtraBold, fontSize = 18.sp))
                }

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("@Username") },
                    singleLine = true,
                    enabled = isEditMode
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("First name") },
                    singleLine = true,
                    enabled = isEditMode
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Last name") },
                    singleLine = true,
                    enabled = isEditMode
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true,
                    enabled = isEditMode
                )

                if (showValidationError) {
                    Text(
                        text = "All fields are required.",
                        color = Color.Red,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                if (isEditMode) {
                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (username.isBlank() || firstName.isBlank() || lastName.isBlank() || email.isBlank()) {
                                showValidationError = true
                                return@Button
                            }

                            isSaving = true
                            showValidationError = false

                            val updatedData = mapOf(
                                "username" to username,
                                "firstName" to firstName,
                                "lastName" to lastName,
                                "email" to email
                            )

                            FirebaseFirestore.getInstance()
                                .collection("users")
                                .document(userId)
                                .update(updatedData)
                                .addOnSuccessListener {
                                    isSaving = false
                                    isEditMode = false
                                }
                                .addOnFailureListener {
                                    isSaving = false
                                    Log.e("Profile", "Failed to save profile", it)
                                }
                        },
                        enabled = !isSaving,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (isSaving) "Saving..." else "Save Changes")
                    }
                }
            }
        }
    )
}
