package com.example.codequest.api.models

data class QuizQuestion(
    val question: String,
    val options: List<String>,
    val correctAnswer: String,
    val category: String
)
