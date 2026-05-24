package com.example.sathiai.chat

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sathiai.ai.AiTone
import com.example.sathiai.ui.components.*
import com.example.sathiai.ui.theme.*
import com.example.sathiai.voice.VoiceState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen() {
    val context = LocalContext.current
    val viewModel: ChatViewModel = viewModel(factory = ChatViewModel.Factory(context))
    val messages       by viewModel.messages.collectAsState()
    val conversations  by viewModel.conversations.collectAsState()
    val isTyping       = viewModel.isTyping
    val voiceState     by viewModel.voiceState.collectAsState()
    
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope       = rememberCoroutineScope()
    val listState   = rememberLazyListState()

    var inputText by remember { mutableStateOf("") }

    LaunchedEffect(viewModel.sttText) {
        if (viewModel.sttText.isNotEmpty()) inputText = viewModel.sttText
    }
    
    val micPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) viewModel.startListening()
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
    }

    SathiTheme {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(
                    drawerContainerColor = MidnightBlue,
                    drawerContentColor = TextPrimary,
                    modifier = Modifier.width(300.dp)
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "History",
                        modifier = Modifier.padding(24.dp),
                        style = SathiTypography.titleLarge
                    )
                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                    
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(conversations) { conv ->
                            NavigationDrawerItem(
                                label = { Text(conv.title, maxLines = 1) },
                                selected = conv.id == viewModel.currentConversationId,
                                onClick = {
                                    viewModel.selectConversation(conv.id)
                                    scope.launch { drawerState.close() }
                                },
                                badge = {
                                    Row {
                                        IconButton(onClick = { viewModel.togglePinConversation(conv.id, conv.isPinned) }) {
                                            Icon(
                                                Icons.Default.PushPin,
                                                contentDescription = null,
                                                tint = if (conv.isPinned) AccentPrimary else TextMuted,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        IconButton(onClick = { viewModel.deleteConversation(conv.id) }) {
                                            Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                },
                                colors = NavigationDrawerItemDefaults.colors(
                                    unselectedContainerColor = Color.Transparent,
                                    selectedContainerColor = Color.White.copy(alpha = 0.05f)
                                ),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                            )
                        }
                    }
                    
                    Box(modifier = Modifier.padding(20.dp)) {
                        Button(
                            onClick = { viewModel.createNewConversation() },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("New Chat", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        ) {
            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                "SathiAI",
                                style = SathiTypography.titleLarge,
                                letterSpacing = 2.sp
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = null, tint = TextSecondary)
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = DeepBlack,
                            titleContentColor = TextPrimary
                        )
                    )
                },
                containerColor = DeepBlack
            ) { padding ->
                Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                    if (messages.isEmpty()) {
                        HeroSection()
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 140.dp, top = 16.dp)
                        ) {
                            items(messages) { msg ->
                                MessageBubble(msg)
                            }
                            if (isTyping) {
                                item { TypingIndicator() }
                            }
                        }
                    }

                    // Floating Input Area
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(horizontal = 16.dp, vertical = 24.dp)
                    ) {
                        PremiumInputBar(
                            text = inputText,
                            onTextChange = { inputText = it },
                            selectedTone = viewModel.selectedTone,
                            onToneChange = { viewModel.selectedTone = it },
                            voiceState = voiceState,
                            onMicClick = {
                                if (voiceState is VoiceState.Listening) viewModel.stopListening()
                                else micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            },
                            onSend = {
                                if (inputText.isNotBlank()) {
                                    viewModel.sendMessage(inputText)
                                    inputText = ""
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HeroSection() {
    Column(
        modifier = Modifier.fillMaxSize().padding(bottom = 80.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AICenterpiece()
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            "Hello, I'm Sathi 👋",
            style = SathiTypography.headlineLarge
        )
        Text(
            "Your futuristic AI companion.\nHow can I help you today?",
            style = SathiTypography.bodyLarge,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Suggestion Grid
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SuggestionGlassCard("Explain\nQuantum", Modifier.weight(1f))
            SuggestionGlassCard("Plan a\nTrip", Modifier.weight(1f))
            SuggestionGlassCard("Write a\nStory", Modifier.weight(1f))
        }
    }
}

@Composable
fun SuggestionGlassCard(label: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .premiumGlass(RoundedCornerShape(20.dp))
            .clickable { }
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            style = SathiTypography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )
    }
}

@Composable
fun TypingIndicator() {
    Row(
        modifier = Modifier.padding(24.dp, 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(AccentPrimary, CircleShape)
        )
        Spacer(Modifier.width(12.dp))
        Text("Sathi is thinking...", style = SathiTypography.labelSmall)
    }
}

@Composable
fun PremiumInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    selectedTone: AiTone,
    onToneChange: (AiTone) -> Unit,
    voiceState: VoiceState,
    onMicClick: () -> Unit,
    onSend: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .premiumGlass(RoundedCornerShape(32.dp))
            .padding(8.dp)
    ) {
        // Tone Segmented Control
        ToneSelector(
            selectedTone = selectedTone,
            onToneSelected = onToneChange,
            modifier = Modifier.padding(bottom = 8.dp).fillMaxWidth(0.9f).align(Alignment.CenterHorizontally)
        )
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            IconButton(onClick = onMicClick) {
                val isListening = voiceState is VoiceState.Listening
                Icon(
                    if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                    contentDescription = null,
                    tint = if (isListening) Color.Red else TextSecondary
                )
            }

            TextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Ask Sathi...", color = TextMuted) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                maxLines = 4
            )

            FloatingActionButton(
                onClick = onSend,
                containerColor = AccentPrimary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(44.dp),
                elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(20.dp))
            }
        }
    }
}
