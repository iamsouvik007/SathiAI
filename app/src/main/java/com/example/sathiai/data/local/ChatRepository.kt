package com.example.sathiai.data.local

import com.example.sathiai.ai.AiTone
import com.example.sathiai.ai.PersonalityManager
import com.example.sathiai.network.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class ChatRepository(private val dao: ChatDao) {

    // Conversations
    fun getAllConversations(): Flow<List<ConversationEntity>> = dao.getAllConversations()

    suspend fun createConversation(title: String): Long {
        return dao.insertConversation(ConversationEntity(title = title))
    }

    suspend fun deleteConversation(id: Long) {
        dao.deleteConversation(id)
        dao.deleteMessagesForConversation(id)
    }

    suspend fun updateConversationTitle(id: Long, title: String) {
        val conv = ConversationEntity(id = id, title = title, lastTimestamp = System.currentTimeMillis())
        dao.updateConversation(conv)
    }

    suspend fun togglePinConversation(id: Long, isPinned: Boolean) {
        dao.updatePinnedStatus(id, isPinned)
    }

    // Messages
    fun getMessages(conversationId: Long): Flow<List<MessageEntity>> = dao.getMessagesForConversation(conversationId)

    suspend fun sendMessage(
        conversationId: Long,
        text: String,
        tone: AiTone,
        imageBase64: String? = null
    ): String {
        // 1. Save user message locally
        val userMessage = MessageEntity(
            conversationId = conversationId,
            text = text,
            isUser = true,
            imageUrl = imageBase64,
        )
        dao.insertMessage(userMessage)

        // 2. Prepare AI Request
        val systemPrompt = PersonalityManager.getSystemPrompt(tone)
        val apiMessages = mutableListOf<Message>()
        apiMessages.add(Message(role = "system", content = systemPrompt))

        // Optional: Add some history
        val history = dao.getMessagesForConversation(conversationId).firstOrNull()?.takeLast(5)
        history?.filter { it.imageUrl == null }?.forEach { 
             apiMessages.add(Message(role = if (it.isUser) "user" else "assistant", content = it.text))
        }

        // Current message with optional vision
        val currentContent = if (imageBase64 != null) {
            listOf(
                ContentItem(type = "text", text = text),
                ContentItem(type = "image_url", imageUrl = ImageUrl(imageBase64))
            )
        } else {
            text
        }

        apiMessages.add(Message(role = "user", content = currentContent))

        val model = if (imageBase64 != null) "llama-3.2-11b-vision-preview" else "llama-3.3-70b-versatile"

        return try {
            val response = RetrofitInstance.api.sendMessage(
                token = "Bearer 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",
                request = ChatRequest(model = model, messages = apiMessages)
            )
            val aiReply = response.choices.firstOrNull()?.message?.content?.toString() ?: "No response"

            // 3. Save AI reply locally
            dao.insertMessage(
                MessageEntity(
                    conversationId = conversationId,
                    text = aiReply,
                    isUser = false
                )
            )

            // Update conversation timestamp
            updateConversationTitle(conversationId, text.take(30) + "...")

            aiReply
        } catch (e: Exception) {
            val errorMsg = "Error: ${e.message}"
            dao.insertMessage(
                MessageEntity(
                    conversationId = conversationId,
                    text = errorMsg,
                    isUser = false
                )
            )
            errorMsg
        }
    }
}