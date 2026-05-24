package com.example.sathiai.network

data class ChatRequest(
    val model: String,
    val messages: List<Message>
)