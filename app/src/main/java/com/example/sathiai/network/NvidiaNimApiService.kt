package com.example.sathiai.network

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface NvidiaNimApiService {
    @POST("v1/chat/completions")
    suspend fun analyzeImage(
        @Header("Authorization") token: String,
        @Body request: ChatRequest
    ): ChatResponse
}
