package com.example.sathiai.chat

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sathiai.network.ChatRequest
import com.example.sathiai.network.Message
import com.example.sathiai.network.RetrofitInstance
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {

    val messages = mutableStateListOf(

        ChatMessage(
            text =
                "Hello 👋\nI am SathiAI.\nAsk me anything.",
            isUser = false
        )
    )

    var isTyping by mutableStateOf(false)

    fun sendMessage(text: String) {

        if (text.isBlank()) return

        messages.add(

            ChatMessage(
                text = text,
                isUser = true
            )
        )

        isTyping = true

        viewModelScope.launch {

            try {

                val request = ChatRequest(

                    model =
                        "llama-3.3-70b-versatile",

                    messages = listOf(
                        Message(
                            role = "user",
                            content = text
                        )
                    )
                )

                delay(500)

                val response =
                    RetrofitInstance.api.sendMessage(

                        token =
                            "Bearer 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",

                        request = request
                    )

                val aiReply =
                    response.choices.firstOrNull()
                        ?.message
                        ?.content
                        ?: "No response"

                messages.add(

                    ChatMessage(
                        text = aiReply,
                        isUser = false
                    )
                )

            } catch (e: Exception) {

                messages.add(

                    ChatMessage(
                        text =
                            "Error:\n${e.message}",
                        isUser = false
                    )
                )
            }

            isTyping = false
        }
    }
}