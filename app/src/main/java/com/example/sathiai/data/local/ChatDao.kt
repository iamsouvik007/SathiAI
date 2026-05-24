package com.example.sathiai.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ChatDao {

    @Insert(
        onConflict =
            OnConflictStrategy.REPLACE
    )
    suspend fun insertMessage(
        chat: ChatEntity
    )

    @Query(
        "SELECT * FROM chat_messages ORDER BY timestamp ASC"
    )
    suspend fun getAllMessages():
            List<ChatEntity>

    @Query(
        "DELETE FROM chat_messages"
    )
    suspend fun clearChat()
}