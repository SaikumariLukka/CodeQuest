package com.example.codequest

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun ProfileScreen() {
    // Fetch user data from Firebase Authentication
    val user = FirebaseAuth.getInstance().currentUser
    val email = user?.email ?: "john.doe@example.com"  // Default to a sample email if not available

    var fullName by remember { mutableStateOf("Loading...") }
    var showDialog by remember { mutableStateOf(false) }

    // Fetch the full name from Firestore
    user?.let {
        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .document(user.uid)
            .get()
            .addOnSuccessListener { document ->
                fullName = document.getString("fullName") ?: "No Name Provided"
            }
    }

    // Handle the display of the privacy policy dialog
    if (showDialog) {
        showPrivacyPolicyDialog(onDismiss = { showDialog = false })
    }

    val context = LocalContext.current

    // Sign Out logic
    fun signOut() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(context, LoginActivity::class.java)
        context.startActivity(intent)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Profile",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF42A5F5),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Display Full Name and Email
        Text(
            text = "Name: $fullName",  // Display full name from Firestore
            fontSize = 18.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Email: $email",  // Display email
            fontSize = 18.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Privacy Policy Section
        Text(
            text = "Privacy Policy",
            fontSize = 18.sp,
            color = Color(0xFF42A5F5),
            modifier = Modifier
                .clickable {
                    // Show the privacy policy dialog
                    showDialog = true
                }
                .padding(bottom = 16.dp)
        )

        // Sign Out Button
        Button(
            onClick = { signOut() },
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF42A5F5))
        ) {
            Text("Sign Out", color = Color.White, fontSize = 18.sp)
        }
    }
}

// Function to show the Privacy Policy as an in-app dialog
@Composable
fun showPrivacyPolicyDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = "Privacy Policy") },
        text = {
            Text(
                text = """
                Your privacy is important to us. We do not collect or share your personal data 
                without your consent. This app uses Firebase Authentication to manage user accounts. 
                Your data is stored securely and only used for the purpose of providing quiz functionalities. 
                By using this app, you agree to the terms and conditions stated in this privacy policy.
                """
            )
        },
        confirmButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("OK")
            }
        }
    )
}
