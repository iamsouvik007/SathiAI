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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.sathiai.ai.AiTone
import com.example.sathiai.vision.VisionUtils
import com.example.sathiai.voice.VoiceState
import kotlinx.coroutines.launch

// ─── Colour Palette ────────────────────────────────────────────────────────────
private val BgDeep        = Color(0xFF080B14)
private val BgCard        = Color(0xFF0F1320)
private val BgSurface     = Color(0xFF151929)
private val BgInput       = Color(0xFF1A1F30)
private val AccentPrimary = Color(0xFF7B61FF)
private val AccentSecond  = Color(0xFF4E9FFF)
private val AccentGlow    = Color(0x337B61FF)
private val TextPrimary   = Color(0xFFF0F2FF)
private val TextSecondary = Color(0xFF8891B0)
private val TextMuted     = Color(0xFF4A5070)
private val Divider       = Color(0xFF1E2438)
private val ChipBorder    = Color(0xFF252B3F)

// ─── Glow elevation helper ─────────────────────────────────────────────────────
fun Modifier.coloredShadow(
    color: Color,
    borderRadius: Dp = 0.dp,
    blurRadius: Dp = 20.dp,
    offsetX: Dp = 0.dp,
    offsetY: Dp = 4.dp
) = this.drawBehind {
    drawIntoCanvas { canvas ->
        val paint = Paint().apply {
            asFrameworkPaint().apply {
                isAntiAlias = true
                this.color = android.graphics.Color.TRANSPARENT
                setShadowLayer(
                    blurRadius.toPx(),
                    offsetX.toPx(),
                    offsetY.toPx(),
                    color.copy(alpha = 0.45f).toArgb()
                )
            }
        }
        canvas.drawRoundRect(
            left = 0f, top = 0f,
            right = size.width, bottom = size.height,
            radiusX = borderRadius.toPx(), radiusY = borderRadius.toPx(),
            paint = paint
        )
    }
}

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

    // Sync STT text to input field
    LaunchedEffect(viewModel.sttText) {
        if (viewModel.sttText.isNotEmpty()) {
            inputText = viewModel.sttText
        }
    }

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.selectedImageBase64 = VisionUtils.uriToBase64(context, it) }
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startListening()
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        scrimColor = Color(0x99000000),
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = BgCard,
                drawerContentColor = TextPrimary,
                modifier = Modifier.width(300.dp)
            ) {
                // Drawer Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(AccentPrimary.copy(0.15f), Color.Transparent)
                            )
                        )
                        .padding(horizontal = 20.dp, vertical = 24.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        Brush.radialGradient(
                                            listOf(AccentPrimary, AccentSecond)
                                        ),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("S", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
                            }
                            Spacer(Modifier.width(10.dp))
                            Text(
                                "SathiAI",
                                color = TextPrimary,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp,
                                letterSpacing = 1.sp
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Your conversations",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                HorizontalDivider(color = Divider, thickness = 0.5.dp)

                // Conversation list
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    items(conversations) { conv ->
                        val isSelected = conv.id == viewModel.currentConversationId
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .then(
                                    if (isSelected) Modifier.background(
                                        Brush.horizontalGradient(
                                            listOf(AccentPrimary.copy(0.2f), Color.Transparent)
                                        ),
                                        RoundedCornerShape(12.dp)
                                    ).border(
                                        0.5.dp,
                                        AccentPrimary.copy(0.4f),
                                        RoundedCornerShape(12.dp)
                                    )
                                    else Modifier
                                )
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    viewModel.selectConversation(conv.id)
                                    scope.launch { drawerState.close() }
                                }
                                .padding(horizontal = 14.dp, vertical = 12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    if (conv.isPinned) Icons.Default.PushPin else Icons.Default.ChatBubbleOutline,
                                    contentDescription = null,
                                    tint = if (isSelected) AccentPrimary else if (conv.isPinned) AccentPrimary.copy(0.7f) else TextMuted,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    conv.title,
                                    maxLines = 1,
                                    color = if (isSelected) TextPrimary else TextSecondary,
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    modifier = Modifier.weight(1f)
                                )
                                
                                // Pin/Delete actions
                                IconButton(onClick = { viewModel.togglePinConversation(conv.id, conv.isPinned) }, modifier = Modifier.size(20.dp)) {
                                    Icon(
                                        Icons.Default.PushPin,
                                        contentDescription = "Pin",
                                        tint = if (conv.isPinned) AccentPrimary else TextMuted.copy(0.4f),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                Spacer(Modifier.width(4.dp))
                                IconButton(onClick = { viewModel.deleteConversation(conv.id) }, modifier = Modifier.size(20.dp)) {
                                    Icon(
                                        Icons.Default.DeleteOutline,
                                        contentDescription = "Delete",
                                        tint = TextMuted.copy(0.4f),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = Divider, thickness = 0.5.dp)

                // New Chat Button
                Box(modifier = Modifier.padding(16.dp)) {
                    Button(
                        onClick = { viewModel.createNewConversation() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .coloredShadow(AccentPrimary, 24.dp, 16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(0.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(AccentPrimary, AccentSecond)
                                    ),
                                    RoundedCornerShape(14.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "New Conversation",
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(
                                        Brush.radialGradient(
                                            listOf(AccentPrimary, AccentSecond)
                                        ),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("S", color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "SathiAI",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                letterSpacing = 1.5.sp,
                                color = TextPrimary
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = TextSecondary
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.createNewConversation() }) {
                            Icon(
                                Icons.Default.EditNote,
                                contentDescription = "New chat",
                                tint = TextSecondary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = BgDeep,
                        titleContentColor = TextPrimary,
                        navigationIconContentColor = TextSecondary
                    )
                )
            },
            containerColor = BgDeep
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                // Subtle separator line under top bar
                HorizontalDivider(color = Divider, thickness = 0.5.dp)

                // Messages Area
                Box(modifier = Modifier.weight(1f)) {
                    if (messages.isEmpty()) {
                        WelcomeSection()
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(top = 8.dp, bottom = 20.dp)
                        ) {
                            items(messages) { msg ->
                                MessageBubble(msg)
                            }
                            if (isTyping) {
                                item { TypingIndicator() }
                            }
                        }
                    }
                }

                // Input Area
                ChatInputBar(
                    text = inputText,
                    onTextChange = { inputText = it },
                    selectedTone = viewModel.selectedTone,
                    onToneChange = { viewModel.selectedTone = it },
                    selectedImage = viewModel.selectedImageBase64,
                    onPickImage = { imagePicker.launch("image/*") },
                    onClearImage = { viewModel.selectedImageBase64 = null },
                    voiceState = voiceState,
                    onMicClick = {
                        if (voiceState is VoiceState.Listening || voiceState is VoiceState.PartialResult) {
                            viewModel.stopListening()
                        } else {
                            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    },
                    onSend = {
                        if (inputText.isNotBlank() || viewModel.selectedImageBase64 != null) {
                            viewModel.sendMessage(inputText)
                            inputText = ""
                        }
                    }
                )
            }
        }
    }
}

// ─── Welcome ──────────────────────────────────────────────────────────────────
@Composable
fun WelcomeSection() {
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Background glow blob
        Box(
            modifier = Modifier
                .size(260.dp)
                .background(
                    Brush.radialGradient(
                        listOf(
                            AccentPrimary.copy(alpha = glowAlpha * 0.25f),
                            Color.Transparent
                        )
                    ),
                    CircleShape
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(
                        Brush.radialGradient(
                            listOf(AccentPrimary.copy(0.3f), AccentSecond.copy(0.15f))
                        ),
                        CircleShape
                    )
                    .border(1.dp, AccentPrimary.copy(0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("S", color = AccentPrimary, fontWeight = FontWeight.Black, fontSize = 32.sp)
            }

            Spacer(Modifier.height(20.dp))

            Text(
                "Hello, I'm Sathi 👋",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Your personal AI companion.\nAsk me anything.",
                color = TextSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(Modifier.height(32.dp))

            // Suggestion chips
            val suggestions = listOf("Write a poem", "Explain AI", "Help me plan")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                suggestions.forEach { suggestion ->
                    SuggestionChip(
                        label = suggestion,
                        onClick = { /* could pre-fill the input */ }
                    )
                }
            }
        }
    }
}

@Composable
fun SuggestionChip(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .border(0.5.dp, ChipBorder, RoundedCornerShape(20.dp))
            .background(BgSurface)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(label, color = TextSecondary, fontSize = 12.sp)
    }
}

// ─── Typing Indicator ─────────────────────────────────────────────────────────
@Composable
fun TypingIndicator() {
    val dotCount = 3
    val infiniteTransition = rememberInfiniteTransition(label = "typing")
    val dots = (0 until dotCount).map { i ->
        infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(500, delayMillis = i * 150, easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse
            ),
            label = "dot$i"
        )
    }

    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .padding(start = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        // Small avatar dot
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(BgSurface, CircleShape)
                .border(0.5.dp, ChipBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("S", color = AccentPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.width(10.dp))

        Box(
            modifier = Modifier
                .background(BgSurface, RoundedCornerShape(topStart = 4.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 18.dp))
                .border(0.5.dp, ChipBorder, RoundedCornerShape(topStart = 4.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 18.dp))
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                dots.forEach { dot ->
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .scale(dot.value)
                            .background(AccentPrimary.copy(alpha = dot.value), CircleShape)
                    )
                }
            }
        }
    }
}

// ─── Input Bar ────────────────────────────────────────────────────────────────
@Composable
fun ChatInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    selectedTone: AiTone,
    onToneChange: (AiTone) -> Unit,
    selectedImage: String?,
    onPickImage: () -> Unit,
    onClearImage: () -> Unit,
    voiceState: VoiceState,
    onMicClick: () -> Unit,
    onSend: () -> Unit
) {
    val canSend = text.isNotBlank() || selectedImage != null
    val isListening = voiceState is VoiceState.Listening || voiceState is VoiceState.PartialResult

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val micScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "micScale"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgDeep)
    ) {
        HorizontalDivider(color = Divider, thickness = 0.5.dp)

        // ── Tone Chips ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Tone ",
                color = TextMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.width(4.dp))
            AiTone.entries.forEach { tone ->
                val selected = selectedTone == tone
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .then(
                            if (selected) Modifier.background(
                                Brush.horizontalGradient(listOf(AccentPrimary, AccentSecond)),
                                RoundedCornerShape(20.dp)
                            ).coloredShadow(AccentPrimary, 20.dp, 10.dp)
                            else Modifier
                                .background(BgSurface, RoundedCornerShape(20.dp))
                                .border(0.5.dp, ChipBorder, RoundedCornerShape(20.dp))
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onToneChange(tone) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        tone.displayName,
                        fontSize = 11.sp,
                        color = if (selected) Color.White else TextSecondary,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }

        // ── Image Preview ──
        AnimatedVisibility(
            visible = selectedImage != null,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, AccentPrimary.copy(0.3f), RoundedCornerShape(12.dp))
                ) {
                    AsyncImage(
                        model = selectedImage,
                        contentDescription = "Attached image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = 6.dp, y = (-6).dp)
                        .background(BgSurface, CircleShape)
                        .border(0.5.dp, ChipBorder, CircleShape)
                        .clickable(onClick = onClearImage),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Remove image",
                        tint = TextSecondary,
                        modifier = Modifier.size(11.dp)
                    )
                }
            }
        }

        // ── Text Field Row ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            // Attach button
            IconButton(
                onClick = onPickImage,
                modifier = Modifier
                    .size(42.dp)
                    .align(Alignment.CenterVertically)
            ) {
                Icon(
                    Icons.Default.AttachFile,
                    contentDescription = "Attach image",
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Mic Button with pulse
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(42.dp).align(Alignment.CenterVertically)) {
                if (isListening) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .scale(micScale)
                            .background(AccentPrimary.copy(alpha = 0.2f), CircleShape)
                    )
                }
                IconButton(onClick = onMicClick) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = "Voice Input",
                        tint = if (isListening) Color.Red else TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Text field
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(BgInput)
                    .border(
                        0.5.dp,
                        if (text.isNotBlank()) AccentPrimary.copy(0.3f) else ChipBorder,
                        RoundedCornerShape(20.dp)
                    )
            ) {
                TextField(
                    value = text,
                    onValueChange = onTextChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            if (isListening) "Listening..." else "Ask Sathi anything…",
                            color = TextMuted,
                            fontSize = 14.sp
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = AccentPrimary
                    ),
                    textStyle = LocalTextStyle.current.copy(fontSize = 14.sp, lineHeight = 20.sp),
                    maxLines = 5
                )
            }

            Spacer(Modifier.width(4.dp))

            // Send button
            val sendScale by animateFloatAsState(
                targetValue = if (canSend) 1f else 0.85f,
                animationSpec = spring(dampingRatio = 0.6f),
                label = "sendScale"
            )
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .scale(sendScale)
                    .then(
                        if (canSend) Modifier.coloredShadow(AccentPrimary, 22.dp, 12.dp)
                        else Modifier
                    )
                    .background(
                        brush = if (canSend)
                            Brush.radialGradient(listOf(AccentPrimary, AccentSecond.copy(0.8f)))
                        else
                            Brush.radialGradient(listOf(BgSurface, BgSurface)),
                        shape = CircleShape
                    )
                    .clip(CircleShape)
                    .clickable(enabled = canSend, onClick = onSend),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = if (canSend) Color.White else TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Bottom safe area padding
        Spacer(Modifier.height(4.dp))
    }
}