package com.example.codequest.api.models

data class QuizResponse(
    val title: String,
    val questions: List<QuizQuestion>
)
