package com.example.codequest

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codequest.ui.theme.CodeQuestTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import androidx.compose.ui.platform.LocalContext

class SignUpActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Firebase Authentication setup
        auth = FirebaseAuth.getInstance()

        // One Tap Google Sign-Up initialization
        oneTapClient = Identity.getSignInClient(this)
        signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(getString(R.string.default_web_client_id)) // Web client ID from Firebase
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .build()

        setContent {
            CodeQuestTheme {
                SignUpScreen(
                    onSignUpSuccess = {
                        val homeIntent = Intent(this, LoginActivity::class.java)
                        startActivity(homeIntent)
                        finish() // Close the sign-up activity
                    },
                    onGoogleSignUp = { handleGoogleSignUp() },
                    handleEmailSignUp = { email, password, onSignUpSuccess ->
                        handleEmailSignUp(email, password, onSignUpSuccess)
                    }
                )
            }
        }
    }

    private fun handleEmailSignUp(email: String, password: String, onSignUpSuccess: () -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Sign-Up Successful", Toast.LENGTH_SHORT).show()
                    onSignUpSuccess()
                } else {
                    Toast.makeText(this, "Sign-Up Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun handleGoogleSignUp() {
        val oneTapSignUp = oneTapClient.beginSignIn(signInRequest)
        oneTapSignUp.addOnSuccessListener { result ->
            val signUpIntent = result.pendingIntent.intentSender
            // googleSignUpLauncher.launch(signUpIntent)
        }.addOnFailureListener { exception ->
            Toast.makeText(this, "Google Sign-Up Failed: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private val googleSignUpLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val credential = oneTapClient.getSignInCredentialFromIntent(result.data)
                val idToken = credential.googleIdToken
                if (idToken != null) {
                    val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                    auth.signInWithCredential(firebaseCredential)
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(this, "Google Sign-Up Successful", Toast.LENGTH_SHORT).show()
                                val homeIntent = Intent(this, HomeActivity::class.java)
                                startActivity(homeIntent)
                                finish() // Close the sign-up activity
                            } else {
                                Toast.makeText(this, "Google Sign-Up Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            }
        }
}

@Composable
fun SignUpScreen(
    onSignUpSuccess: () -> Unit,
    onGoogleSignUp: () -> Unit,
    handleEmailSignUp: (String, String, () -> Unit) -> Unit
) {
    var email by remember { mutableStateOf(TextFieldValue("")) }
    var password by remember { mutableStateOf(TextFieldValue("")) }
    var confirmPassword by remember { mutableStateOf(TextFieldValue("")) }
    var passwordVisible by remember { mutableStateOf(false) }
    var signUpError by remember { mutableStateOf<String?>(null) } // Error message

    val context = LocalContext.current // Correct context usage inside composable

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.background),
            contentDescription = "Background Image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(250.dp)
                    .padding(bottom = 15.dp),
                contentScale = ContentScale.Fit
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password"
                        )
                    }
                }
            )

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password") },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password"
                        )
                    }
                }
            )

            if (signUpError != null) {
                Text(
                    text = signUpError!!,
                    color = Color.Red,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Button(
                onClick = {
                    // Validation checks
                    when {
                        email.text.isBlank() -> signUpError = "Email cannot be empty."
                        !Patterns.EMAIL_ADDRESS.matcher(email.text).matches() -> signUpError = "Enter a valid email address."
                        password.text.isBlank() -> signUpError = "Password cannot be empty."
                        password.text != confirmPassword.text -> signUpError = "Passwords do not match."
                        else -> {
                            signUpError = null
                            handleEmailSignUp(email.text, password.text, onSignUpSuccess)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(text = "Sign Up")
            }

            Text(
                text = "Or sign up with Google",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .clickable { onGoogleSignUp() },
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp)
            )

            Row(
                modifier = Modifier.padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Already have an account? ", color = Color.Blue)
                Text(
                    text = "Login",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable {
                        // Correctly use context to navigate in composable
                        val loginIntent = Intent(context, LoginActivity::class.java)
                        context.startActivity(loginIntent)
                    }
                )
            }
        }
    }
}
