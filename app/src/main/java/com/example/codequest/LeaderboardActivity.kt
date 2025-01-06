package com.example.codequest

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

class LeaderboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                LeaderboardScreen()
            }
        }
    }
}

@Composable
fun LeaderboardScreen() {
    val db = FirebaseFirestore.getInstance()

    // States for subjects and leaderboard data
    val subjects = remember { mutableStateOf(listOf<String>()) }
    val leaderboardData = remember { mutableStateOf(mapOf<String, List<Map<String, Any>>>()) }
    val showResults = remember { mutableStateOf(false) } // State for controlling the visibility of results

    // Fetch subjects and leaderboard data
    LaunchedEffect(Unit) {
        // Fetch the subjects from the leaderboard collection
        db.collection("leaderboard").get()
            .addOnSuccessListener { result ->
                val subjectList = result.documents.map { it.id }
                subjects.value = subjectList
                Log.d("Leaderboard", "Fetched subjects: $subjectList")  // Debug log

                // Check if subjects are fetched correctly
                Log.d("Leaderboard", "Subjects fetched: ${subjectList.size}")  // Debug the number of subjects

                // Fetch scores for each subject
                subjectList.forEach { subject ->
                    db.collection("leaderboard")
                        .document(subject)
                        .collection("scores")
                        .orderBy("score", Query.Direction.DESCENDING)
                        .get()
                        .addOnSuccessListener { scoresResult ->
                            val data = scoresResult.documents.map {
                                it.data ?: emptyMap<String, Any>()
                            }
                            leaderboardData.value = leaderboardData.value.toMutableMap().apply {
                                put(subject, data)
                            }
                            Log.d("Leaderboard", "Fetched data for $subject: $data") // Debug log
                            data.forEach {
                                Log.d("Leaderboard Data", "Entry: $it") // Log each entry in the subject
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e("Leaderboard", "Error fetching scores for $subject: ${e.message}")
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("Leaderboard", "Error fetching subjects: ${e.message}")
            }

    }

    // UI for the leaderboard screen
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Leaderboard Title
        Text(
            text = "Leaderboard",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF42A5F5),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Button to toggle results visibility
        Button(
            onClick = {
                showResults.value = !showResults.value // Toggle the visibility of results
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF42A5F5)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text("View Results", color = Color.White)
        }

        // Conditionally show leaderboard results if `showResults` is true
        if (showResults.value) {
            // Display leaderboard for each subject
            if (subjects.value.isNotEmpty()) {
                subjects.value.forEach { subject ->
                    Text(
                        text = subject,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    leaderboardData.value[subject]?.forEach { scoreEntry ->
                        // Format timestamp if present
                        val timestamp = scoreEntry["timestamp"] as? Timestamp
                        val formattedDate = timestamp?.toDate()?.let { formatTimestamp(it) } ?: "N/A"

                        // Display each leaderboard entry
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    // User and score info
                                    Text(
                                        text = "${scoreEntry["username"]} - ${scoreEntry["score"]} Points",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    // Display the formatted date and time
                                    Text(
                                        text = formattedDate,
                                        fontSize = 14.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // Display loading or no results if subjects are empty
                Text("No subjects available", color = Color.Gray)
            }
        }
    }
}

// Function to format Firestore Timestamp to a human-readable string
fun formatTimestamp(date: Date): String {
    val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return format.format(date)
}
