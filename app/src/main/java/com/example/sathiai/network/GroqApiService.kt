package com.example.sathiai.network

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface GroqApiService {

    @POST("openai/v1/chat/completions")
    suspend fun sendMessage(

        @Header("Authorization")
        token: String,

        @Body
        request: ChatRequest

    ): ChatResponse
}