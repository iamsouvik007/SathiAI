package com.example.sathiai.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import dev.jeziellago.compose.markdowntext.MarkdownText

@Composable
fun SathiMarkdownText(
    markdown: String,
    modifier: Modifier = Modifier,
    color: Color = Color.White
) {
    MarkdownText(
        markdown = markdown,
        modifier = modifier,
        style = MaterialTheme.typography.bodyMedium.copy(color = color)
    )
}