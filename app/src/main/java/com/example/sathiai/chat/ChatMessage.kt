package com.example.sathiai.chat

data class ChatMessage(

    val text: String,

    val isUser: Boolean,

    val timestamp: String =
        System.currentTimeMillis().toString()
)