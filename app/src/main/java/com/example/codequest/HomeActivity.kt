package com.example.codequest

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.codequest.api.RetrofitInstance
import com.example.codequest.api.models.QuizResponse
import com.example.codequest.ui.theme.CodeQuestTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CodeQuestTheme {
                MainScreen { subject, onResult ->
                    fetchQuizQuestions(subject, onResult)
                }
            }
        }
    }

    private fun fetchQuizQuestions(subject: String, onResult: (QuizResponse?) -> Unit) {
        val call = RetrofitInstance.quizApiService.getQuizQuestions(subject)

        call.enqueue(object : Callback<QuizResponse> {
            override fun onResponse(call: Call<QuizResponse>, response: Response<QuizResponse>) {
                if (response.isSuccessful) {
                    onResult(response.body())
                } else {
                    showToast("Failed to load quiz questions.")
                    onResult(null)
                }
            }

            override fun onFailure(call: Call<QuizResponse>, t: Throwable) {
                showToast("Network failure: ${t.message}")
                onResult(null)
            }
        })
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
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
            composable("home") { HomeScreen(navController) }
            composable("leaderboard") { LeaderboardScreen() }
            composable("profile") { ProfileScreen() }
            composable(
                route = "quiz/{subjectIndex}",
                arguments = listOf(navArgument("subjectIndex") { type = NavType.IntType })
            ) { backStackEntry ->
                val subjectIndex = backStackEntry.arguments?.getInt("subjectIndex") ?: 0
                QuizScreen(subjectIndex)
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
    val subjects = listOf(
        "Python",
        "Object Oriented Programming",
        "Machine Learning",
        "Data Science",
        "Power BI",
        "SQL"
    )

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
            Text(
                text = "Choose a Subject",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            subjects.forEachIndexed { index, subject ->
                Button(
                    onClick = {
                        // Pass the subject index instead of the subject name
                        navController.navigate("quiz/$index")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF42A5F5),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(text = subject, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
