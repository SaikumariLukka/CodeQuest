

package com.example.codequest

import android.content.Intent
import android.os.Bundle
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.example.codequest.ui.theme.CodeQuestTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.codequest.LoginActivity
import com.example.codequest.R

import androidx.compose.foundation.background
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CodeQuestTheme {
                SplashScreen { // After splash screen, navigate based on login status
                    val isLoggedIn = checkIfUserIsLoggedIn()
                    if (isLoggedIn) {
                        // Navigate to Main Content
                        setContent {
                            MainContent()
                        }
                    } else {
                        // Navigate to LoginActivity
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish() // Close MainActivity
                    }
                }
            }
        }
    }

    private fun checkIfUserIsLoggedIn(): Boolean {
        return false // Replace with actual login check
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
        // Background Image with a translucent overlay to reduce contrast
        Image(
            painter = painterResource(id = R.drawable.background),
            contentDescription = "Background Image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop // Scales and crops to fill the screen
        )



        // Logo Image
        Image(
            painter = painterResource(id = R.drawable.logo), // Replace with your logo's resource ID
            contentDescription = "Logo",
            modifier = Modifier
                .size(400.dp) // Set desired size for the logo
                .padding(bottom = 15.dp),
            contentScale = ContentScale.Fit // Scale the logo proportionally
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
                    Text(text = "Welcome to CodeQuest!", color = Color.White)
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
