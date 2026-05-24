package com.example.sathiai.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.Icons

import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ChatScreen() {

    val viewModel: ChatViewModel = viewModel()

    var inputText by remember {
        mutableStateOf("")
    }

    val listState =
        rememberLazyListState()

    LaunchedEffect(
        viewModel.messages.size
    ) {

        if (viewModel.messages.isNotEmpty()) {

            listState.animateScrollToItem(
                viewModel.messages.lastIndex
            )
        }
    }

    Box(

        modifier = Modifier

            .fillMaxSize()

            .background(

                brush = Brush.verticalGradient(

                    listOf(

                        Color(0xFF121212),

                        Color(0xFF1B1B1B),

                        Color(0xFF202020)
                    )
                )
            )
    ) {

        Column(

            modifier = Modifier
                .fillMaxSize()
        ) {

            Text(

                text = "SathiAI",

                color = Color.White,

                style =
                    MaterialTheme.typography.headlineMedium,

                fontWeight = FontWeight.Bold,

                modifier = Modifier.padding(
                    20.dp
                )
            )

            LazyColumn(

                state = listState,

                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),

                contentPadding = PaddingValues(
                    horizontal = 12.dp,
                    vertical = 8.dp
                )
            ) {

                items(viewModel.messages) {

                    MessageBubble(it)
                }

                if (viewModel.isTyping) {

                    item {

                        Text(

                            text = "AI is typing...",

                            color = Color.LightGray,

                            modifier = Modifier.padding(
                                16.dp
                            )
                        )
                    }
                }
            }

            Row(

                modifier = Modifier

                    .fillMaxWidth()

                    .padding(
                        horizontal = 12.dp,
                        vertical = 10.dp
                    ),

                verticalAlignment =
                    Alignment.CenterVertically,

                horizontalArrangement =
                    Arrangement.Center
            ) {

                TextField(

                    value = inputText,

                    onValueChange = {
                        inputText = it
                    },

                    modifier = Modifier.weight(1f),

                    shape =
                        RoundedCornerShape(28.dp),

                    placeholder = {

                        Text(
                            "Ask anything..."
                        )
                    }
                )

                Spacer(
                    modifier = Modifier.width(10.dp)
                )

                FloatingActionButton(

                    onClick = {

                        viewModel.sendMessage(
                            inputText
                        )

                        inputText = ""
                    },

                    containerColor =
                        Color(0xFF6750A4)
                ) {

                    Icon(

                        imageVector =
                            Icons.AutoMirrored.Filled.Send,

                        contentDescription =
                            "Send"
                    )
                }
            }
        }
    }
}