package com.example.codequest

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.codequest.api.models.QuizQuestion
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay

@SuppressLint("MutableCollectionMutableState")
@Composable
fun QuizScreen(subject: String, navController: NavController) {
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
    var showAlertDialog by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.background),
            contentDescription = "Background Image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Back Button and Timer Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { showAlertDialog = true }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Row {
                if (isTimerRunning) {
                    IconButton(onClick = {
                        isTimerRunning = false
                        isPaused = true
                    }) {
                        Icon(Icons.Default.Pause, contentDescription = "Pause")
                    }
                } else if (isPaused) {
                    IconButton(onClick = {
                        isTimerRunning = true
                        isPaused = false
                    }) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Resume")
                    }
                }
            }
        }

        // Alert Dialog for stopping the quiz
        if (showAlertDialog) {
            AlertDialog(
                onDismissRequest = { showAlertDialog = false },
                title = { Text("Are you sure?") },
                text = { Text("Do you want to stop the quiz?") },
                confirmButton = {
                    TextButton(onClick = {
                        showAlertDialog = false
                        currentQuestionIndex = 0
                        selectedAnswers.clear()
                        score = 0
                        timer = 60
                        showResults = false
                        isTimerRunning = false
                        showInstructions = true
                    }) {
                        Text("Yes")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAlertDialog = false }) {
                        Text("No")
                    }
                }
            )
        }

        // Fetch quiz data from Firestore based on the subject
        LaunchedEffect(subject) {
            val db = FirebaseFirestore.getInstance()
            db.collection("quizzes").document(subject).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val questions = document.get("questions") as? List<Map<String, Any>> ?: emptyList()
                        quizQuestions = questions.mapNotNull { questionMap ->
                            try {
                                QuizQuestion(
                                    question = questionMap["question"] as? String ?: return@mapNotNull null,
                                    options = questionMap["options"] as? List<String> ?: return@mapNotNull null,
                                    category = questionMap["category"] as? String ?: "General",
                                    correctAnswer = questionMap["correctAnswer"] as? String ?: ""
                                )
                            } catch (e: Exception) {
                                Log.e("QuizScreen", "Error parsing question: ${e.message}")
                                null
                            }
                        }
                        isLoading = false
                    } else {
                        Toast.makeText(context, "No quiz found for the subject", Toast.LENGTH_SHORT).show()
                        isLoading = false
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(context, "Error fetching data: ${exception.message}", Toast.LENGTH_SHORT).show()
                    isLoading = false
                }
        }

        LaunchedEffect(isTimerRunning) {
            if (isTimerRunning) {
                while (timer > 0) {
                    delay(1000L)
                    if (!isTimerRunning) break // Break if timer is stopped
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
                ResultsScreen(
                    score = score,
                    totalQuestions = quizQuestions.size,
                    subject = subject,
                    onTryAgain = {
                        currentQuestionIndex = 0
                        selectedAnswers.clear()
                        score = 0
                        timer = 60
                        showResults = false
                        isTimerRunning = false
                        showInstructions = true
                    },
                    onSaveAndViewLeaderboard = {
                        // Navigate to leaderboard screen after saving
                        navController.navigate("leaderboard_screen")
                    }
                )
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
                    onAnswerSelected = { answer -> selectedAnswers[currentQuestionIndex] = answer },
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
}


@Composable
fun ResultsScreen(
    score: Int,
    totalQuestions: Int,
    subject: String,
    onTryAgain: () -> Unit,
    onSaveAndViewLeaderboard: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()
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

    val currentDateTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
    var isSaved by remember { mutableStateOf(false) }

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

        Button(onClick = onTryAgain) {
            Text(text = "Try Again")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (!isSaved) {
                    val resultData = mapOf(
                        "username" to "YourUsername", // Replace with actual username
                        "score" to score,
                        "date" to currentDateTime.split(" ")[0],
                        "time" to currentDateTime.split(" ")[1]
                    )
                    db.collection("leaderboard")
                        .document(subject)
                        .collection("scores")
                        .add(resultData)
                        .addOnSuccessListener {
                            Log.d("Firestore", "Result saved successfully")
                            isSaved = true
                            // No navigation after saving the score
                        }
                        .addOnFailureListener { e ->
                            Log.e("Firestore", "Error saving result: ${e.message}")
                        }
                }
            }
        ) {
            Text(text = if (isSaved) "Saved!" else "Save Result")
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

        Spacer(modifier = Modifier.height(32.dp))

        // Next Button
        Button(
            onClick = onNextClicked,
            modifier = Modifier.fillMaxWidth(),
            enabled = selectedAnswer != null // Ensure Next button is enabled only when an option is selected
        ) {
            Text(text = if (questionIndex == totalQuestions - 1) "Submit" else "Next")
        }
    }
}
