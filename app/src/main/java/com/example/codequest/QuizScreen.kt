package com.example.codequest

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codequest.api.RetrofitInstance
import com.example.codequest.api.models.QuizQuestion
import com.example.codequest.api.models.QuizResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Composable
fun QuizScreen(subjectIndex: Int) {
    val context = LocalContext.current
    var quizQuestions by remember { mutableStateOf<List<QuizQuestion>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Fetch quiz questions for the selected subject
    LaunchedEffect(subjectIndex) {
        val subject = when (subjectIndex) {
            0 -> "Python" // Python category
            1 -> "OOP" // OOP category
            2 -> "Machine Learning"
            3 -> "Data Science"
            4 -> "Power BI"
            5 -> "SQL"
            else -> "Python" // Default to Python
        }

        // Make the API call with Retrofit (using `Call`)
        RetrofitInstance.quizApiService.getQuizQuestions(subject).enqueue(object : Callback<QuizResponse> {
            override fun onResponse(call: Call<QuizResponse>, response: Response<QuizResponse>) {
                if (response.isSuccessful) {
                    quizQuestions = response.body()?.questions ?: emptyList()
                } else {
                    Toast.makeText(context, "Failed to load quiz", Toast.LENGTH_SHORT).show()
                }
                isLoading = false
            }

            override fun onFailure(call: Call<QuizResponse>, t: Throwable) {
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                isLoading = false
            }
        })
    }

    if (isLoading) {
        // Show a loading indicator while fetching quiz data
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        // Display the quiz content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            if (quizQuestions.isEmpty()) {
                Text(
                    text = "No quiz questions available.",
                    color = Color.Red,
                    fontSize = 18.sp
                )
            } else {
                // Display the quiz title
                Text(
                    text = "Quiz: ${quizQuestions.firstOrNull()?.category ?: "Unknown"}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF42A5F5),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Instructions
                Text(
                    text = "Follow the instructions below and answer carefully:",
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Displaying quiz questions
                quizQuestions.forEachIndexed { index, question ->
                    QuizQuestion(index = index + 1, questionText = question.question)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        // Submit quiz or navigate to results
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF42A5F5),
                        contentColor = Color.White
                    )
                ) {
                    Text(text = "Submit Quiz", fontSize = 18.sp)
                }
            }
        }
    }
}

@Composable
fun QuizQuestion(index: Int, questionText: String) {
    var userAnswer by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        Text(
            text = "$index. $questionText",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Placeholder for user input (can be multiple-choice buttons or text input)
        BasicTextField(
            value = userAnswer,
            onValueChange = { userAnswer = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .height(48.dp)
                .background(Color(0xFFF5F5F5), shape = MaterialTheme.shapes.medium),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done,
                keyboardType = KeyboardType.Text
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    // Handle the answer submission or move to the next question
                }
            )
        )
    }
}
