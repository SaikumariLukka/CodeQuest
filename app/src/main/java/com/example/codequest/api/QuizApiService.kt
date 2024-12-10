package com.example.codequest.api

import com.example.codequest.api.models.QuizResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface QuizApiService {
    @GET("api.php")
    fun getQuizQuestions(
        @Query("amount") amount: Int,
        @Query("category") category: Int,
        @Query("type") type: String
    ): Call<QuizResponse>
}

