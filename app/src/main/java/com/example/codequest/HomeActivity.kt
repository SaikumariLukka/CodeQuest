package com.example.codequest

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.codequest.api.models.QuizResponse
import com.example.codequest.ui.theme.CodeQuestTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.google.firebase.firestore.FirebaseFirestore

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CodeQuestTheme {
                MainScreen { subject, onResult ->
                    fetchQuizQuestions(subject, onResult) // Fetch quiz questions based on subject
                }
            }
        }
    }

    private fun fetchQuizQuestions(subject: String, onResult: (QuizResponse?) -> Unit) {
        val db = FirebaseFirestore.getInstance()

        // Firestore path assuming each subject has a document
        val quizRef = db.collection("quizzes").document(subject)

        quizRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Assuming you have a QuizResponse object in Firestore with questions
                    val quizResponse = document.toObject(QuizResponse::class.java)
                    onResult(quizResponse)
                } else {
                    showToast("No quiz found for this subject.")
                    onResult(null)
                }
            }
            .addOnFailureListener { exception ->
                showToast("Error loading quiz: ${exception.message}")
                onResult(null)
            }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    @Composable
    fun MainScreen(fetchQuestions: (String, (QuizResponse?) -> Unit) -> Unit) {
        val navController = rememberNavController()

        Scaffold(
            bottomBar = { BottomNavigationBar(navController) }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = "home",
                Modifier.padding(innerPadding)
            ) {
                composable("home") {
                    HomeScreen(navController)
                }
                composable("leaderboard") {
                    LeaderboardScreen()
                }
                composable("profile") {
                    ProfileScreen()
                }
                composable(
                    route = "quiz/{subject}",
                    arguments = listOf(navArgument("subject") { type = NavType.StringType })
                ) { backStackEntry ->
                    val subject = backStackEntry.arguments?.getString("subject") ?: "Unknown"
                    QuizScreen(subject = subject) // Pass subject name to QuizScreen
                }
            }
        }

    }

    @Composable
    fun BottomNavigationBar(navController: NavHostController) {
        NavigationBar {
            NavigationBarItem(
                icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                label = { Text("Home") },
                selected = navController.currentDestination?.route == "home",
                onClick = { navController.navigate("home") }
            )
            NavigationBarItem(
                icon = { Icon(Icons.Filled.Star, contentDescription = "Leaderboard") },
                label = { Text("Leaderboard") },
                selected = navController.currentDestination?.route == "leaderboard",
                onClick = { navController.navigate("leaderboard") }
            )
            NavigationBarItem(
                icon = { Icon(Icons.Filled.AccountCircle, contentDescription = "Profile") },
                label = { Text("Profile") },
                selected = navController.currentDestination?.route == "profile",
                onClick = { navController.navigate("profile") }
            )
        }
    }

    @Composable
    fun HomeScreen(navController: NavHostController) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Image(
                painter = painterResource(id = R.drawable.background),
                contentDescription = "Background Image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Making the Column scrollable by wrapping it with Modifier.verticalScroll
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()), // Enable vertical scrolling
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Choose a Subject",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // List of subjects with corresponding images
                val subjects = listOf(
                    Pair("Python", R.drawable.python),
                    Pair("OOP", R.drawable.oop),
                    Pair("Machine Learning", R.drawable.machinelearning)
                )

                subjects.forEach { (subject, imageRes) ->
                    Button(
                        onClick = {
                            // Pass the subject name to the quiz screen
                            navController.navigate("quiz/$subject")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()  // This makes the image 100% of the screen width
                                .height(200.dp)  // Adjusted height to make the image larger
                                .clip(RoundedCornerShape(16.dp))  // Apply rounded corners to the image
                        ) {
                            Image(
                                painter = painterResource(id = imageRes),
                                contentDescription = subject,
                                modifier = Modifier
                                    .fillMaxSize()  // Image takes up the full Box size
                                    .padding(8.dp),  // Padding around the image
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }
        }
    }
}
