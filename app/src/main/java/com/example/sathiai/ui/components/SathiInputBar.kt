package com.example.sathiai.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sathiai.ai.AiTone
import com.example.sathiai.ui.theme.AccentPrimary
import com.example.sathiai.ui.theme.TextMuted
import com.example.sathiai.ui.theme.TextPrimary

@Composable
fun SathiInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    selectedTone: AiTone,
    onToneSelected: (AiTone) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ToneSelector(
            selectedTone = selectedTone,
            onToneSelected = onToneSelected,
            modifier = Modifier
                .width(200.dp)
                .padding(bottom = 6.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .premiumGlass(RoundedCornerShape(20.dp))
                .background(Color.White.copy(alpha = 0.03f))
                .padding(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { /* TODO: Speech to text */ },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Default.Mic, contentDescription = "Voice", tint = AccentPrimary, modifier = Modifier.size(16.dp))
            }

            TextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Ask...", color = TextMuted, fontSize = 13.sp) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = TextPrimary,
                    cursorColor = AccentPrimary
                ),
                maxLines = 2,
                textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp)
            )

            FloatingActionButton(
                onClick = onSend,
                containerColor = AccentPrimary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .size(30.dp)
                    .padding(end = 2.dp),
                elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
