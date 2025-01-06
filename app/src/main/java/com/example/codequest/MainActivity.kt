package com.example.codequest

import android.content.Intent
import android.os.Bundle
import android.Manifest
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codequest.ui.theme.CodeQuestTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.activity.result.contract.ActivityResultContracts

class MainActivity : ComponentActivity() {
    private val locationPermissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val locationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
            val cameraGranted = permissions[Manifest.permission.CAMERA] == true

            if (locationGranted && cameraGranted) {
                // Navigate to the consent screen after permissions are granted
                startActivity(Intent(this, ConsentActivity::class.java))
                finish()
            } else {
                // Handle permission denial, show a message or ask again
                Toast.makeText(this, "Permissions are required to proceed.", Toast.LENGTH_LONG).show()
                finish()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CodeQuestTheme {
                SplashScreen { // Navigate after splash screen
                    // Request permissions
                    locationPermissionRequest.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.CAMERA
                        )
                    )
                }
            }
        }
    }

    private fun checkIfUserIsLoggedIn(): Boolean {
        // Replace this with actual logic to check login status
        return false
    }
}

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            delay(2000) // 2-second delay for splash screen
            onTimeout()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Background Image with a translucent overlay
        Image(
            painter = painterResource(id = R.drawable.background),
            contentDescription = "Background Image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Logo Image
        Image(
            painter = painterResource(id = R.drawable.logo), // Replace with your logo's resource ID
            contentDescription = "Logo",
            modifier = Modifier
                .size(400.dp)
                .padding(bottom = 15.dp),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
fun MainContent() {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.background),
            contentDescription = "Background Image",
            modifier = Modifier.fillMaxSize()
        )
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            content = { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Welcome to CodeQuest!",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    CodeQuestTheme {
        SplashScreen(onTimeout = {})
    }
}
