package com.example.sathiai.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.example.sathiai.ui.theme.TextPrimary
import dev.jeziellago.compose.markdowntext.MarkdownText

@Composable
fun SathiMarkdownText(
    markdown: String,
    modifier: Modifier = Modifier,
    color: Color = TextPrimary
) {
    val segments = parseMarkdownSegments(markdown)

    Column(modifier = modifier) {
        segments.forEach { segment ->
            when (segment) {
                is MarkdownSegment.Text -> {
                    if (segment.content.isNotBlank()) {
                        MarkdownText(
                            markdown = segment.content,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = color,
                                lineHeight = 22.sp
                            ),
                            isTextSelectable = true
                        )
                    }
                }
                is MarkdownSegment.Code -> {
                    SathiCodeBlock(
                        code = segment.code,
                        language = segment.language
                    )
                }
            }
        }
    }
}

sealed class MarkdownSegment {
    data class Text(val content: String) : MarkdownSegment()
    data class Code(val code: String, val language: String) : MarkdownSegment()
}

private fun parseMarkdownSegments(markdown: String): List<MarkdownSegment> {
    val segments = mutableListOf<MarkdownSegment>()
    val regex = Regex("(?s)```(\\w*)\\n?(.*?)\\n?```")
    var lastIndex = 0
    
    regex.findAll(markdown).forEach { match ->
        // Add text before code block
        if (match.range.first > lastIndex) {
            segments.add(MarkdownSegment.Text(markdown.substring(lastIndex, match.range.first)))
        }
        
        // Add code block
        val language = match.groupValues[1].trim()
        val code = match.groupValues[2].trim()
        segments.add(MarkdownSegment.Code(code, language))
        
        lastIndex = match.range.last + 1
    }
    
    // Add remaining text
    if (lastIndex < markdown.length) {
        segments.add(MarkdownSegment.Text(markdown.substring(lastIndex)))
    }
    
    return if (segments.isEmpty()) listOf(MarkdownSegment.Text(markdown)) else segments
}
