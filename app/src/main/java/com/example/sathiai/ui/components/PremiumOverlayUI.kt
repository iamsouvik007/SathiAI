package com.example.sathiai.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sathiai.ui.theme.*

@Composable
fun PremiumOverlayUI(
    responseText: String,
    isThinking: Boolean,
    inputText: String,
    onInputChanged: (String) -> Unit,
    onAnalyzeScreen: () -> Unit,
    onClear: () -> Unit,
    onSend: () -> Unit,
    onClose: () -> Unit
) {
    val listState = rememberLazyListState()
    
    LaunchedEffect(responseText) {
        if (responseText.isNotEmpty()) {
            listState.animateScrollToItem(Int.MAX_VALUE)
        }
    }

    SathiTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .premiumGlass(RoundedCornerShape(32.dp))
                .background(MidnightBlue.copy(alpha = 0.8f))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // ── Header ──────────────────────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(AccentPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Sathi AI", style = SathiTypography.titleLarge.copy(fontSize = 16.sp))
                        Text(
                            if (isThinking) "Thinking..." else "Online",
                            style = SathiTypography.labelSmall.copy(color = if (isThinking) AccentPrimary else Color.Green.copy(0.7f))
                        )
                    }
                    IconButton(onClick = onClose, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = TextMuted)
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

                // ── Content ─────────────────────────────────────────────────
                Box(modifier = Modifier.weight(1f)) {
                    if (responseText.isEmpty() && !isThinking) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.CenterFocusWeak, contentDescription = null, tint = TextMuted.copy(alpha = 0.3f), modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(12.dp))
                            Text("Ready to analyze screen", style = SathiTypography.bodyMedium, color = TextMuted)
                        }
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(20.dp)
                        ) {
                            item {
                                Text(
                                    text = responseText,
                                    style = SathiTypography.bodyLarge.copy(lineHeight = 22.sp),
                                    color = TextPrimary
                                )
                            }
                            if (isThinking) {
                                item {
                                    LinearProgressIndicator(
                                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp).height(2.dp),
                                        color = AccentPrimary,
                                        trackColor = Color.Transparent
                                    )
                                }
                            }
                        }
                    }
                }

                // ── Bottom Bar ──────────────────────────────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DeepBlack.copy(alpha = 0.5f))
                        .padding(12.dp)
                ) {
                    // Action Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ActionIconCard(Icons.Default.ScreenSearchDesktop, "Analyze", AccentSecond) { onAnalyzeScreen() }
                        ActionIconCard(Icons.Default.DeleteSweep, "Clear", TextMuted) { onClear() }
                    }

                    // Input Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextField(
                            value = inputText,
                            onValueChange = onInputChanged,
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Ask anything...", color = TextMuted, fontSize = 14.sp) },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = Color.White
                            ),
                            maxLines = 3
                        )
                        FloatingActionButton(
                            onClick = onSend,
                            containerColor = AccentPrimary,
                            contentColor = Color.White,
                            shape = CircleShape,
                            modifier = Modifier.size(40.dp),
                            elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActionIconCard(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, color: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.03f))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(label, style = SathiTypography.labelSmall.copy(color = color))
    }
}