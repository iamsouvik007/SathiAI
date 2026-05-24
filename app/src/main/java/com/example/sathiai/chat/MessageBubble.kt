package com.example.sathiai.chat

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.sathiai.data.local.MessageEntity
import com.example.sathiai.ui.components.SathiMarkdownText

// ─── Palette (mirrors ChatScreen palette for consistency) ─────────────────────
private val BgDeep        = Color(0xFF080B14)
private val BgCard        = Color(0xFF0F1320)
private val BgSurface     = Color(0xFF151929)
private val AccentPrimary = Color(0xFF7B61FF)
private val AccentSecond  = Color(0xFF4E9FFF)
private val TextPrimary   = Color(0xFFF0F2FF)
private val TextSecondary = Color(0xFF8891B0)
private val TextMuted     = Color(0xFF4A5070)
private val ChipBorder    = Color(0xFF1E2438)

@Composable
fun MessageBubble(message: MessageEntity) {
    val isUser = message.isUser

    AnimatedVisibility(
        visible = true,
        enter = if (isUser)
            slideInHorizontally(initialOffsetX = { it / 3 }) + fadeIn(tween(220))
        else
            slideInHorizontally(initialOffsetX = { -it / 3 }) + fadeIn(tween(220))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 5.dp),
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
            verticalAlignment = Alignment.Bottom
        ) {
            // ── AI avatar (left side) ──────────────────────────────────────────
            if (!isUser) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .background(
                            Brush.radialGradient(
                                listOf(AccentPrimary.copy(0.25f), AccentSecond.copy(0.1f))
                            ),
                            CircleShape
                        )
                        .border(0.5.dp, AccentPrimary.copy(0.35f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "S",
                        color = AccentPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Spacer(Modifier.width(8.dp))
            }

            // ── Bubble column ─────────────────────────────────────────────────
            Column(
                modifier = Modifier.widthIn(max = 285.dp),
                horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
            ) {

                // ── Attached image ────────────────────────────────────────────
                message.imageUrl?.let { url ->
                    AsyncImage(
                        model = url,
                        contentDescription = "Attached image",
                        modifier = Modifier
                            .padding(bottom = 6.dp)
                            .clip(
                                RoundedCornerShape(
                                    topStart = 16.dp, topEnd = 16.dp,
                                    bottomStart = if (isUser) 16.dp else 4.dp,
                                    bottomEnd = if (isUser) 4.dp else 16.dp
                                )
                            )
                            .fillMaxWidth()
                            .heightIn(max = 220.dp)
                            .border(
                                0.5.dp,
                                ChipBorder,
                                RoundedCornerShape(
                                    topStart = 16.dp, topEnd = 16.dp,
                                    bottomStart = if (isUser) 16.dp else 4.dp,
                                    bottomEnd = if (isUser) 4.dp else 16.dp
                                )
                            ),
                        contentScale = ContentScale.Crop
                    )
                }

                // ── Text bubble ───────────────────────────────────────────────
                val bubbleShape = RoundedCornerShape(
                    topStart = 18.dp,
                    topEnd = 18.dp,
                    bottomStart = if (isUser) 18.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 18.dp
                )

                Box(
                    modifier = Modifier
                        .then(
                            if (isUser) Modifier.background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        AccentPrimary,
                                        Color(0xFF5B4FD4)
                                    )
                                ),
                                bubbleShape
                            )
                            else Modifier
                                .background(BgSurface, bubbleShape)
                                .border(0.5.dp, ChipBorder, bubbleShape)
                        )
                        .padding(
                            horizontal = if (isUser) 14.dp else 13.dp,
                            vertical = 11.dp
                        )
                ) {
                    SelectionContainer {
                        if (isUser) {
                            Text(
                                text = message.text,
                                color = Color.White,
                                fontSize = 14.sp,
                                lineHeight = 21.sp
                            )
                        } else {
                            SathiMarkdownText(
                                markdown = message.text,
                                color = TextPrimary
                            )
                        }
                    }
                }

                // ── Copy button (AI messages only) ────────────────────────────
                if (!isUser && message.text.isNotBlank()) {
                    var copied by remember { mutableStateOf(false) }

                    LaunchedEffect(copied) {
                        if (copied) {
                            kotlinx.coroutines.delay(1500)
                            copied = false
                        }
                    }

                    Row(
                        modifier = Modifier
                            .padding(top = 4.dp, start = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "just now",
                            color = TextMuted,
                            fontSize = 10.sp
                        )

                        Spacer(Modifier.width(8.dp))

                        IconButton(
                            onClick = {
                                // Copy to clipboard functionality would go here
                                copied = true
                            },
                            modifier = Modifier.size(22.dp)
                        ) {
                            Icon(
                                if (copied) Icons.Default.Check else Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                tint = if (copied) AccentPrimary else TextMuted,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
            }

            // ── User avatar (right side) ───────────────────────────────────────
            if (isUser) {
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .background(
                            Brush.radialGradient(listOf(Color(0xFF2A2440), Color(0xFF1A1A30))),
                            CircleShape
                        )
                        .border(0.5.dp, AccentPrimary.copy(0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}