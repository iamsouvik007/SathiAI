package com.example.sathiai.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun MessageBubble(
    message: ChatMessage
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),

        horizontalArrangement =
            if (message.isUser)
                Arrangement.End
            else
                Arrangement.Start
    ) {

        Column(
            horizontalAlignment =
                if (message.isUser)
                    Alignment.End
                else
                    Alignment.Start
        ) {

            Box(

                modifier = Modifier
                    .widthIn(max = 320.dp)

                    .background(

                        brush =
                            if (message.isUser)

                                Brush.linearGradient(
                                    listOf(
                                        Color(0xFF6750A4),
                                        Color(0xFF7F67BE)
                                    )
                                )

                            else

                                Brush.linearGradient(
                                    listOf(
                                        Color(0xFF2A2A2A),
                                        Color(0xFF1F1F1F)
                                    )
                                ),

                        shape = RoundedCornerShape(
                            topStart = 24.dp,
                            topEnd = 24.dp,
                            bottomStart =
                                if (message.isUser)
                                    24.dp
                                else
                                    6.dp,

                            bottomEnd =
                                if (message.isUser)
                                    6.dp
                                else
                                    24.dp
                        )
                    )

                    .padding(
                        horizontal = 16.dp,
                        vertical = 12.dp
                    )
            ) {

                Text(

                    text = message.text,

                    color = Color.White,

                    style =
                        MaterialTheme.typography.bodyLarge,

                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}