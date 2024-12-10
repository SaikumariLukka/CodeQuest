package com.example.codequest.api.models

data class QuizQuestion(
    val question: String,
    val options: List<String>,
    val category: String,  // Add the 'category' field
    val correctAnswer: String // Add the 'correctAnswer' field
)

