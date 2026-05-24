package com.example.sathiai.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.sathiai.data.local.MessageEntity
import com.example.sathiai.ui.components.SathiMarkdownText
import com.example.sathiai.ui.theme.*

@Composable
fun MessageBubble(message: MessageEntity) {
    val isUser = message.isUser

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier.widthIn(max = 280.dp),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            // Attached image
            message.imageUrl?.let { url ->
                AsyncImage(
                    model = url,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Crop
                )
            }

            // Text bubble
            Box(
                modifier = Modifier
                    .background(
                        brush = if (isUser) {
                            Brush.linearGradient(listOf(AccentPrimary, Color(0xFF5B4FD4)))
                        } else {
                            Brush.verticalGradient(listOf(Color(0xFF1E2330), Color(0xFF161A24)))
                        },
                        shape = RoundedCornerShape(
                            topStart = 20.dp,
                            topEnd = 20.dp,
                            bottomStart = if (isUser) 20.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 20.dp
                        )
                    )
                    .padding(14.dp)
            ) {
                SelectionContainer {
                    if (isUser) {
                        Text(
                            text = message.text,
                            style = SathiTypography.bodyLarge.copy(color = Color.White)
                        )
                    } else {
                        SathiMarkdownText(
                            markdown = message.text,
                            color = TextPrimary
                        )
                    }
                }
            }
        }
    }
}