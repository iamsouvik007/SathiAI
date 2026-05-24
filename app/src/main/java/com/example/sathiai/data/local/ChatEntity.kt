package com.example.sathiai.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val isPinned: Boolean = false,
    val lastTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val conversationId: Long,
    val text: String,
    val isUser: Boolean,
    val imageUrl: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)