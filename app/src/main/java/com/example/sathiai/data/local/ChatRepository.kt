package com.example.sathiai.data.local

class ChatRepository(

    private val dao: ChatDao
) {

    suspend fun insertMessage(
        chat: ChatEntity
    ) {

        dao.insertMessage(chat)
    }

    suspend fun getMessages():
            List<ChatEntity> {

        return dao.getAllMessages()
    }

    suspend fun clearMessages() {

        dao.clearChat()
    }
}