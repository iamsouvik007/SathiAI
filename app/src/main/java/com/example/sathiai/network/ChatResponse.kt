package com.example.sathiai.network

data class ChatResponse(
    val choices: List<Choice>
)

data class Choice(
    val message: Message
)