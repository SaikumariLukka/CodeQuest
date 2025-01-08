package com.example.codequest

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

    val subjects = remember { mutableStateOf(listOf<String>()) }
    val leaderboardData = remember { mutableStateOf(emptyMap<String, List<Map<String, Any>>>()) }
    val isLoading = remember { mutableStateOf(true) }
    val expandedSubjects = remember { mutableStateOf(setOf<String>()) }

    LaunchedEffect(Unit) {
        db.collection("leaderboard")
            .addSnapshotListener { result, error ->
                if (error != null) {
                    Log.e("FirestoreError", "Error fetching subjects: ${error.message}")
                    isLoading.value = false
                    return@addSnapshotListener
                }

                val subjectList = result?.documents?.map { it.id } ?: emptyList()
                subjects.value = subjectList

                subjectList.forEach { subject ->
                    db.collection("leaderboard")
                        .document(subject)
                        .collection("scores")
                        .orderBy("score", Query.Direction.DESCENDING)
                        .addSnapshotListener { scoresResult, scoresError ->
                            if (scoresError != null) {
                                Log.e("FirestoreError", "Error fetching scores for $subject: ${scoresError.message}")
                                return@addSnapshotListener
                            }

                            val data = scoresResult?.documents?.map { it.data ?: emptyMap() } ?: emptyList()
                            leaderboardData.value = leaderboardData.value.toMutableMap().apply {
                                put(subject, data)
                            }.toMap()
                            isLoading.value = false
                        }
                }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Leaderboard",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF42A5F5),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (isLoading.value) {
            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
        } else {
            if (subjects.value.isNotEmpty()) {
                subjects.value.forEach { subject ->
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = subject,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clickable {
                                    expandedSubjects.value = if (expandedSubjects.value.contains(subject)) {
                                        expandedSubjects.value - subject
                                    } else {
                                        expandedSubjects.value + subject
                                    }
                                }
                        )

                        if (expandedSubjects.value.contains(subject)) {
                            leaderboardData.value[subject]?.forEach { scoreEntry ->
                                val timestamp = scoreEntry["timestamp"] as? Timestamp
                                val formattedDate = timestamp?.toDate()?.let { formatTimestamp(it) } ?: "N/A"

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
                                            Text(
                                                text = "${scoreEntry["username"]} - ${scoreEntry["score"]} Points",
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
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
                    }
                }
            } else {
                Text("No subjects available", color = Color.Gray)
            }
        }
    }
}

fun formatTimestamp(date: Date): String {
    val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return format.format(date)
}
