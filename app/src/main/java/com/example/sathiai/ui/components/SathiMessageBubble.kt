package com.example.sathiai.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.sathiai.network.ContentItem
import com.example.sathiai.ui.theme.AccentPrimary
import com.example.sathiai.ui.theme.SurfaceGlass
import com.example.sathiai.ui.theme.TextPrimary

@Composable
fun SathiMessageBubble(
    content: Any,
    isUser: Boolean,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.98f) // Increased width for better code block display
                .clip(
                    RoundedCornerShape(
                        topStart = 14.dp,
                        topEnd = 14.dp,
                        bottomStart = if (isUser) 14.dp else 3.dp,
                        bottomEnd = if (isUser) 3.dp else 14.dp
                    )
                )
                .background(if (isUser) AccentPrimary.copy(alpha = 0.8f) else SurfaceGlass.copy(alpha = 0.6f))
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Column {
                when (content) {
                    is String -> {
                        SelectionContainer {
                            SathiMarkdownText(
                                markdown = content,
                                color = TextPrimary
                            )
                        }
                    }
                    is List<*> -> {
                        content.forEach { item ->
                            if (item is ContentItem) {
                                when (item.type) {
                                    "text" -> {
                                        item.text?.let {
                                            SathiMarkdownText(markdown = it, color = TextPrimary)
                                        }
                                    }
                                    "image_url" -> {
                                        item.imageUrl?.url?.let {
                                            AsyncImage(
                                                model = it,
                                                contentDescription = "User image",
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .heightIn(max = 240.dp)
                                                    .padding(vertical = 4.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                if (!isUser && content is String && content.length > 15) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(
                            onClick = { clipboardManager.setText(AnnotatedString(content)) },
                            modifier = Modifier.size(18.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                tint = TextPrimary.copy(alpha = 0.3f),
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
