package com.example.codequest.api

import com.example.codequest.api.models.QuizResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface QuizApiService {
    @GET("getQuizQuestions")  // Adjust the endpoint to match your API
    fun getQuizQuestions(
        @Query("subject") subject: String  // Pass subject as a query parameter
    ): Call<QuizResponse>
}
