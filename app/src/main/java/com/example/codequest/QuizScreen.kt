package com.example.codequest

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Warning
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
    var score by remember { mutableStateOf(0) }

    // Mock Python-related quiz questions
    LaunchedEffect(subject) {
        quizQuestions = listOf(
            QuizQuestion(
                question = "What is Python?",
                options = listOf("Programming language", "Snake", "IDE", "Game"),
                category = "Python Basics",
                correctAnswer = "Programming language"
            ),
            QuizQuestion(
                question = "Which of the following is a Python framework?",
                options = listOf("Django", "Angular", "React", "Vue"),
                category = "Python Frameworks",
                correctAnswer = "Django"
            ),
            QuizQuestion(
                question = "What is the output of 'print(2**3)'?",
                options = listOf("6", "8", "4", "2"),
                category = "Python Arithmetic",
                correctAnswer = "8"
            ),
            QuizQuestion(
                question = "Which keyword is used to define a function in Python?",
                options = listOf("function", "def", "lambda", "define"),
                category = "Python Functions",
                correctAnswer = "def"
            ),
            QuizQuestion(
                question = "What is the use of 'break' in Python?",
                options = listOf("Exits a loop", "Stops the program", "Continues a loop", "Starts a loop"),
                category = "Python Loops",
                correctAnswer = "Exits a loop"
            ),
            QuizQuestion(
                question = "What is the correct file extension for Python files?",
                options = listOf(".java", ".py", ".txt", ".cpp"),
                category = "Python Basics",
                correctAnswer = ".py"
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

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        if (showResults) {
            // Show Results Screen
            ResultsScreen(score = score, totalQuestions = quizQuestions.size)
        } else if (showInstructions) {
            InstructionsScreen(
                subject = subject,
                onStartQuiz = {
                    showInstructions = false
                    isTimerRunning = true
                }
            )
        } else {
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
                    // Calculate score for correct answers
                    if (quizQuestions[currentQuestionIndex].correctAnswer == selectedAnswers[currentQuestionIndex]) {
                        score++
                    }
                    // Move to the next question or show results if it's the last question
                    if (currentQuestionIndex < quizQuestions.size - 1) {
                        currentQuestionIndex++
                    } else {
                        showResults = true
                        isTimerRunning = false
                    }
                },
                timer = timer
            )
        }
    }
}

@Composable
fun ResultsScreen(score: Int, totalQuestions: Int) {
    val passPercentage = 50
    val highScorePercentage = 80
    val percentage = (score.toDouble() / totalQuestions.toDouble()) * 100
    val resultMessage = when {
        percentage >= highScorePercentage -> "Congratulations! You passed with flying colors!"
        percentage >= passPercentage -> "Great job! You passed!"
        else -> "Oops! You failed. Try again."
    }
    val resultColor = when {
        percentage >= highScorePercentage -> Color.Green
        percentage >= passPercentage -> Color.Yellow
        else -> Color.Red
    }
    val resultIcon = when {
        percentage >= highScorePercentage -> Icons.Default.ThumbUp
        percentage >= passPercentage -> Icons.Default.ThumbUp
        else -> Icons.Default.Warning
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = resultIcon,
            contentDescription = "Result Icon",
            tint = resultColor,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = resultMessage,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = resultColor
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Score: $score / $totalQuestions",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = { /* Add restart logic */ }) {
            Text(text = "Try Again")
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


@Preview
@Composable
fun PreviewQuizScreen() {
    QuizScreen(subject = "Python")
}
