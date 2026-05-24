package com.example.sathiai.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sathiai.ai.AiTone
import com.example.sathiai.network.Message
import com.example.sathiai.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumOverlayUI(
    messages: List<Message>,
    isThinking: Boolean,
    inputText: String,
    onInputChanged: (String) -> Unit,
    onAnalyzeScreen: () -> Unit,
    onClear: () -> Unit,
    onSend: () -> Unit,
    onClose: () -> Unit
) {
    val listState = rememberLazyListState()
    var selectedTone by remember { mutableStateOf(AiTone.DEFAULT) }
    
    // Mini expansion animation state
    var expanded by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { expanded = true }
    
    val scale by animateFloatAsState(
        targetValue = if (expanded) 1f else 0.8f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
        label = "scale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (expanded) 1f else 0f,
        animationSpec = tween(300),
        label = "alpha"
    )

    LaunchedEffect(messages.size, isThinking) {
        if (messages.isNotEmpty() || isThinking) {
            listState.animateScrollToItem(if (isThinking) messages.size else messages.size - 1)
        }
    }

    SathiTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(scaleX = scale, scaleY = scale, alpha = alpha)
                .imePadding()
        ) {
            Scaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .premiumGlass(RoundedCornerShape(24.dp)),
                containerColor = MidnightBlue.copy(alpha = 0.96f),
                topBar = {
                    Column {
                        // Header also acts as a visual drag handle
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(AccentPrimary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                            }
                            Spacer(Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Sathi AI", 
                                    style = SathiTypography.titleLarge.copy(fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                )
                                Text(
                                    if (isThinking) "Thinking..." else "Active Copilot",
                                    style = SathiTypography.labelSmall.copy(fontSize = 9.sp, color = TextMuted)
                                )
                            }
                            
                            IconButton(onClick = onClear, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.Refresh, contentDescription = "Clear", tint = TextMuted, modifier = Modifier.size(16.dp))
                            }
                            IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted, modifier = Modifier.size(16.dp))
                            }
                        }
                        // Visual drag handle indicator
                        Box(
                            modifier = Modifier
                                .width(30.dp)
                                .height(3.dp)
                                .align(Alignment.CenterHorizontally)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.1f))
                        )
                        Spacer(Modifier.height(4.dp))
                        HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                    }
                },
                bottomBar = {
                    SathiInputBar(
                        value = inputText,
                        onValueChange = onInputChanged,
                        onSend = onSend,
                        selectedTone = selectedTone,
                        onToneSelected = { selectedTone = it },
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (messages.isEmpty() && !isThinking) {
                        EmptyState(onAnalyzeScreen)
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(messages) { message ->
                                SathiMessageBubble(
                                    content = message.content.toString(),
                                    isUser = message.role == "user"
                                )
                            }
                            if (isThinking) {
                                item {
                                    TypingIndicator()
                                }
                            }
                            item { Spacer(Modifier.height(24.dp)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(onAnalyzeScreen: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(12.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.AutoAwesome, 
            contentDescription = null, 
            tint = AccentPrimary.copy(alpha = 0.15f), 
            modifier = Modifier.size(40.dp)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Floating Copilot", 
            style = SathiTypography.titleLarge.copy(fontSize = 15.sp),
            color = TextPrimary.copy(alpha = 0.7f)
        )
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = onAnalyzeScreen,
            colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary.copy(alpha = 0.1f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.height(34.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
        ) {
            Icon(Icons.Default.ScreenSearchDesktop, contentDescription = null, tint = AccentPrimary, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(6.dp))
            Text("Analyze Screen", color = AccentPrimary, fontSize = 12.sp)
        }
    }
}

@Composable
private fun TypingIndicator() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceGlass)
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                repeat(3) {
                    Dot(it * 150)
                }
            }
        }
    }
}

@Composable
private fun Dot(delay: Int) {
    val infiniteTransition = rememberInfiniteTransition()
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, delayMillis = delay),
            repeatMode = RepeatMode.Reverse
        )
    )
    Box(
        modifier = Modifier
            .size(4.dp)
            .background(AccentPrimary.copy(alpha = alpha), CircleShape)
    )
}
