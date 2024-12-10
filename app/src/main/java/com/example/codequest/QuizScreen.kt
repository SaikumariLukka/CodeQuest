package com.example.codequest

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import com.example.codequest.api.models.QuizQuestion
import com.example.codequest.api.models.QuizResponse

@SuppressLint("MutableCollectionMutableState")
@Composable
fun QuizScreen(subject: String) {
    val context = LocalContext.current
    var quizQuestions by remember { mutableStateOf<List<QuizQuestion>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showInstructions by remember { mutableStateOf(true) }
    var timer by remember { mutableIntStateOf(60) }
    var isTimerRunning by remember { mutableStateOf(false) }
    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    var selectedAnswers by remember { mutableStateOf(mutableMapOf<Int, String>()) }
    var showResults by remember { mutableStateOf(false) }

    // Mock data for the quiz questions
    LaunchedEffect(subject) {
        quizQuestions = listOf(
            QuizQuestion(
                question = "What is the capital of France?",
                options = listOf("Paris", "London", "Rome", "Berlin"),
                category = "Geography", // Provide the category
                correctAnswer = "Paris" // Provide the correct answer
            ),
            QuizQuestion(
                question = "What is 2 + 2?",
                options = listOf("3", "4", "5", "6"),
                category = "Math", // Provide the category
                correctAnswer = "4" // Provide the correct answer
            ),
            QuizQuestion(
                question = "Which planet is known as the Red Planet?",
                options = listOf("Earth", "Mars", "Jupiter", "Venus"),
                category = "Science", // Provide the category
                correctAnswer = "Mars" // Provide the correct answer
            )
        )
        isLoading = false
    }

    // Timer logic
    LaunchedEffect(isTimerRunning) {
        if (isTimerRunning) {
            while (timer > 0) {
                delay(1000L)
                timer -= 1
            }
            if (timer == 0) {
                showResults = true
                isTimerRunning = false
            }
        }
    }

    // Show loading indicator while fetching questions
    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        // Show instructions screen before the quiz starts
        if (showInstructions) {
            InstructionsScreen(
                subject = subject,
                onStartQuiz = {
                    showInstructions = false
                    isTimerRunning = true
                }
            )
        }
        // Show the quiz questions and options while quiz is ongoing
        else {
            // Get the selected answer for the current question (if any)
            val selectedAnswer = selectedAnswers[currentQuestionIndex]

            QuizContent(
                question = quizQuestions[currentQuestionIndex],
                questionIndex = currentQuestionIndex,
                totalQuestions = quizQuestions.size,
                selectedAnswer = selectedAnswer,
                onAnswerSelected = { answer ->
                    selectedAnswers[currentQuestionIndex] = answer
                },
                onNextClicked = {
                    // Move to next question or show results if it's the last question
                    if (currentQuestionIndex < quizQuestions.size - 1) {
                        currentQuestionIndex++
                    } else {
                        showResults = true
                    }
                },
                timer = timer
            )
        }
    }
}


@Composable
fun QuizContent(
    question: QuizQuestion,
    questionIndex: Int,
    totalQuestions: Int,
    selectedAnswer: String?,
    onAnswerSelected: (String) -> Unit,
    onNextClicked: () -> Unit,
    timer: Int
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        // Top section with Question Indicator and Timer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Question ${questionIndex + 1}/$totalQuestions",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Row {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = "Timer",
                    modifier = Modifier.align(Alignment.CenterVertically),
                    tint = Color.Red
                )
                Text(
                    text = "$timer s",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.Red,
                    modifier = Modifier.align(Alignment.CenterVertically).padding(start = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Linear progress indicator
        LinearProgressIndicator(
            progress = (questionIndex + 1).toFloat() / totalQuestions.toFloat(),
            modifier = Modifier.fillMaxWidth().height(8.dp),
            color = Color.Blue,
            trackColor = Color.Gray
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Question Text - Elliptical shape with orange background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .background(Color(0xFFFF9800), shape = RoundedCornerShape(50.dp)) // Orange with elliptical shape
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = question.question,
                fontSize = 18.sp,
                color = Color.White, // White text color to contrast with the orange background
                modifier = Modifier.align(Alignment.CenterStart)
            )
        }

        // Options for the question
        question.options.forEach { option ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                RadioButton(
                    selected = selectedAnswer == option,
                    onClick = { onAnswerSelected(option) }
                )
                Text(
                    text = option,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // "Next" Button to navigate between questions
        Button(
            onClick = onNextClicked,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            enabled = selectedAnswer != null
        ) {
            Text(text = if (questionIndex == totalQuestions - 1) "Submit" else "Next")
        }
    }
}


@Composable
fun InstructionsScreen(subject: String, onStartQuiz: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Welcome to the $subject Quiz",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(
            text = "You will have 60 seconds to answer all questions. Good luck!",
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Button(onClick = onStartQuiz, modifier = Modifier.fillMaxWidth()) {
            Text(text = "Start Quiz")
        }
    }
}

@Preview
@Composable
fun PreviewQuizScreen() {
    QuizScreen(subject = "Sample Subject")
}
