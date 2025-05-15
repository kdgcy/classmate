package com.neu.classmate.components

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Profile(modifier: Modifier = Modifier, navController: NavController) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val userId = currentUser?.uid ?: return

    val db = FirebaseFirestore.getInstance()
    val scope = rememberCoroutineScope()

    var expanded by remember { mutableStateOf(false) }
    var isEditMode by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var showValidationError by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showPasswordPrompt by remember { mutableStateOf(false) }
    var passwordInput by remember { mutableStateOf("") }
    var isDeleting by remember { mutableStateOf(false) }

    var profileImageUrl by remember { mutableStateOf<String?>(null) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    var username by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> selectedImageUri = uri }

    LaunchedEffect(Unit) {
        try {
            val doc = db.collection("users").document(userId).get().await()
            username = doc.getString("username") ?: ""
            firstName = doc.getString("firstName") ?: ""
            lastName = doc.getString("lastName") ?: ""
            email = doc.getString("email") ?: ""
            profileImageUrl = doc.getString("profileImageUrl")
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
                                showDeleteConfirm = true
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
                            imagePickerLauncher.launch("image/*")
                        }
                ) {
                    when {
                        selectedImageUri != null -> AsyncImage(model = selectedImageUri, contentDescription = null)
                        !profileImageUrl.isNullOrBlank() -> AsyncImage(model = profileImageUrl, contentDescription = null)
                        else -> Icon(Icons.Filled.AddAPhoto, contentDescription = "Add image", tint = Color.White, modifier = Modifier.size(36.dp))
                    }
                }

                TextButton(onClick = { imagePickerLauncher.launch("image/*") }, enabled = isEditMode) {
                    Text("Add photo", style = TextStyle(fontWeight = FontWeight.ExtraBold, fontSize = 18.sp))
                }

                Spacer(modifier = Modifier.height(20.dp))
                OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("@Username") }, singleLine = true, enabled = isEditMode)
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(value = firstName, onValueChange = { firstName = it }, label = { Text("First name") }, singleLine = true, enabled = isEditMode)
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(value = lastName, onValueChange = { lastName = it }, label = { Text("Last name") }, singleLine = true, enabled = isEditMode)
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, singleLine = true, enabled = isEditMode)

                if (showValidationError) {
                    Text("All fields are required.", color = Color.Red, fontSize = 14.sp, modifier = Modifier.padding(top = 8.dp))
                }

                if (isEditMode) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            if (username.isBlank() || firstName.isBlank() || lastName.isBlank() || email.isBlank()) {
                                showValidationError = true
                                return@Button
                            }

                            showValidationError = false
                            isSaving = true

                            scope.launch {
                                val updatedData = mutableMapOf<String, Any>(
                                    "username" to username,
                                    "firstName" to firstName,
                                    "lastName" to lastName,
                                    "email" to email
                                )

                                try {
                                    if (selectedImageUri != null) {
                                        val storageRef = FirebaseStorage.getInstance().reference.child("profileImages/$userId.jpg")
                                        val uploadTask = storageRef.putFile(selectedImageUri!!)
                                        val uri = uploadTask.continueWithTask { task ->
                                            if (!task.isSuccessful) throw task.exception ?: Exception("Upload failed")
                                            storageRef.downloadUrl
                                        }.await()
                                        updatedData["profileImageUrl"] = uri.toString()
                                    }

                                    db.collection("users").document(userId).update(updatedData).await()
                                    isSaving = false
                                    isEditMode = false
                                    selectedImageUri = null
                                } catch (e: Exception) {
                                    isSaving = false
                                    Log.e("Profile", "Save failed", e)
                                }
                            }
                        },
                        enabled = !isSaving,
                        modifier = Modifier.fillMaxWidth(0.80f)
                    ) {
                        Text(if (isSaving) "Saving..." else "Save Changes")
                    }
                }
            }
        }
    )

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Account") },
            text = { Text("Are you sure you want to permanently delete your account? This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    showPasswordPrompt = true
                }) {
                    Text("Proceed", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showPasswordPrompt) {
        AlertDialog(
            onDismissRequest = { showPasswordPrompt = false },
            title = { Text("Confirm Password") },
            text = {
                Column {
                    Text("Please enter your password to confirm account deletion.")
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        label = { Text("Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true
                    )

                    if (isDeleting) {
                        Spacer(modifier = Modifier.height(12.dp))
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        isDeleting = true
                        deleteAccount(userId, passwordInput, email, navController) { success ->
                            isDeleting = false
                            showPasswordPrompt = false
                            if (!success) {
                                // Optional: handle error
                            }
                        }
                    },
                    enabled = !isDeleting
                ) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPasswordPrompt = false }, enabled = !isDeleting) {
                    Text("Cancel")
                }
            }
        )
    }
}

fun deleteAccount(
    userId: String,
    password: String,
    email: String,
    navController: NavController,
    onComplete: (Boolean) -> Unit
) {
    val user = FirebaseAuth.getInstance().currentUser
    val db = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance().reference.child("profileImages/$userId.jpg")

    val credential = EmailAuthProvider.getCredential(email, password)

    user?.reauthenticate(credential)
        ?.addOnSuccessListener {
            user.delete()
                .addOnSuccessListener {
                    db.collection("users").document(userId).delete()
                    storage.delete()
                    navController.navigate("auth") {
                        popUpTo("home") { inclusive = true }
                    }
                    onComplete(true)
                }
                .addOnFailureListener {
                    Log.e("DeleteAccount", "Account deletion failed", it)
                    onComplete(false)
                }
        }
        ?.addOnFailureListener {
            Log.e("DeleteAccount", "Re-authentication failed", it)
            onComplete(false)
        }
}
