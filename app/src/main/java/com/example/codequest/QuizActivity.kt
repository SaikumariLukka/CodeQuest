package com.example.codequest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codequest.ui.theme.CodeQuestTheme

class QuizActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CodeQuestTheme {
                QuizScreen(onQuizEnd = {
                    finish() // End the activity when the quiz is completed
                })
            }
        }
    }
}

@Composable
fun QuizScreen(onQuizEnd: () -> Unit) {
    // Sample question and options
    val question = "What is the output of 2 + 2 in Kotlin?"
    val options = listOf("3", "4", "5", "22")
    val correctAnswer = "4"

    // State to track selected answer and feedback
    var selectedOption by remember { mutableStateOf<String?>(null) }
    var isAnswered by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = question,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Options List
        options.forEach { option ->
            Button(
                onClick = {
                    selectedOption = option
                    isAnswered = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isAnswered && option == correctAnswer) Color(0xFF66BB6A) else Color(0xFF42A5F5),
                    contentColor = Color.White
                )
            ) {
                Text(text = option, fontSize = 16.sp)
            }
        }

        // Feedback Text
        if (isAnswered) {
            Text(
                text = if (selectedOption == correctAnswer) "Correct!" else "Wrong Answer!",
                fontSize = 18.sp,
                color = if (selectedOption == correctAnswer) Color(0xFF66BB6A) else Color(0xFFD32F2F),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        // Next Button
        Button(
            onClick = { onQuizEnd() },
            modifier = Modifier
                .padding(top = 32.dp)
                .fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFAB47BC))
        ) {
            Text(text = "Finish Quiz", color = Color.White)
        }
    }
}
