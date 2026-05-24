package com.example.sathiai.chat

import android.app.Application
import android.content.Context
import androidx.compose.runtime.*
import androidx.lifecycle.*
import com.example.sathiai.ai.AiTone
import com.example.sathiai.data.local.*
import com.example.sathiai.voice.VoiceRecognizerManager
import com.example.sathiai.voice.VoiceState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ChatViewModel(private val repository: ChatRepository, private val voiceManager: VoiceRecognizerManager) : ViewModel() {

    private val _conversations = repository.getAllConversations()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val conversations: StateFlow<List<ConversationEntity>> = _conversations

    var currentConversationId by mutableStateOf<Long?>(null)
        private set

    private val _messages = MutableStateFlow<List<MessageEntity>>(emptyList())
    val messages: StateFlow<List<MessageEntity>> = _messages

    var isTyping by mutableStateOf(false)
        private set

    var selectedTone by mutableStateOf(AiTone.DEFAULT)
    
    var selectedImageBase64 by mutableStateOf<String?>(null)

    // Voice State
    private val _voiceState = voiceManager.voiceState
    val voiceState: StateFlow<VoiceState> = _voiceState

    var sttText by mutableStateOf("")
        private set

    init {
        viewModelScope.launch {
            voiceState.collect { state ->
                when (state) {
                    is VoiceState.PartialResult -> sttText = state.text
                    is VoiceState.FinalResult -> sttText = state.text
                    is VoiceState.Error -> {
                        // Could handle error toast here via a SharedFlow
                        sttText = ""
                    }
                    else -> {}
                }
            }
        }
    }

    fun startListening() {
        sttText = ""
        voiceManager.startListening()
    }

    fun stopListening() {
        voiceManager.stopListening()
    }

    fun togglePinConversation(id: Long, currentPinned: Boolean) {
        viewModelScope.launch {
            repository.togglePinConversation(id, !currentPinned)
        }
    }

    fun createNewConversation() {
        viewModelScope.launch {
            val id = repository.createConversation("New Chat")
            selectConversation(id)
        }
    }

    fun selectConversation(id: Long) {
        currentConversationId = id
        viewModelScope.launch {
            repository.getMessages(id).collect {
                _messages.value = it
            }
        }
    }

    fun deleteConversation(id: Long) {
        viewModelScope.launch {
            repository.deleteConversation(id)
            if (currentConversationId == id) {
                currentConversationId = null
                _messages.value = emptyList()
            }
        }
    }

    fun sendMessage(text: String) {
        val convId = currentConversationId ?: run {
             // Create one if it doesn't exist
             viewModelScope.launch {
                 val id = repository.createConversation("New Chat")
                 selectConversation(id)
                 performSendMessage(id, text)
             }
             return
        }
        performSendMessage(convId, text)
    }

    private fun performSendMessage(convId: Long, text: String) {
        if (text.isBlank() && selectedImageBase64 == null) return
        
        viewModelScope.launch {
            isTyping = true
            repository.sendMessage(convId, text, selectedTone, selectedImageBase64)
            selectedImageBase64 = null
            isTyping = false
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val database = ChatDatabase.getDatabase(context)
            val repository = ChatRepository(database.chatDao())
            val voiceManager = VoiceRecognizerManager(context)
            return ChatViewModel(repository, voiceManager) as T
        }
    }
}