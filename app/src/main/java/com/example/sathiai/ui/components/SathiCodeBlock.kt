package com.example.sathiai.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sathiai.ui.theme.AccentPrimary
import com.example.sathiai.ui.theme.TextMuted

@Composable
fun SathiCodeBlock(
    code: String,
    modifier: Modifier = Modifier,
    language: String = ""
) {
    val clipboardManager = LocalClipboardManager.current
    val codeBackground = Color(0xFF0D1117) // GitHub Dark style
    val borderColor = Color.White.copy(alpha = 0.08f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(codeBackground)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
    ) {
        // Header with language and copy button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.03f))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = language.ifBlank { "code" }.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    color = AccentPrimary.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            )
            
            IconButton(
                onClick = { clipboardManager.setText(AnnotatedString(code)) },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy code",
                    tint = TextMuted,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        // Code content with horizontal scroll
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = highlightCode(code),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    color = Color(0xFFE6EDF3)
                )
            )
        }
    }
}

private fun highlightCode(code: String): AnnotatedString {
    return buildAnnotatedString {
        val keywords = listOf(
            "fun", "val", "var", "import", "package", "class", "interface", "object",
            "if", "else", "when", "for", "while", "return", "override", "private", "public",
            "def", "from", "as", "elif", "try", "except", "with", "async", "await",
            "function", "const", "let", "type", "enum", "true", "false", "null"
        )
        
        val types = listOf(
            "String", "Int", "Boolean", "Float", "Double", "Long", "Unit", "Any", "List", "Map", "Set",
            "number", "boolean", "void", "Promise"
        )

        // Split by words, but preserve delimiters
        val tokens = code.split(Regex("(?<=\\W)|(?=\\W)"))
        
        var inString = false
        var inComment = false
        
        tokens.forEach { token ->
            when {
                inComment -> {
                    withStyle(SpanStyle(color = Color(0xFF8B949E))) { append(token) }
                    if (token.contains("\n")) inComment = false
                }
                inString -> {
                    withStyle(SpanStyle(color = Color(0xFFA5D6FF))) { append(token) }
                    if (token == "\"" || token == "\'") inString = false
                }
                token == "//" || token == "#" -> {
                    inComment = true
                    withStyle(SpanStyle(color = Color(0xFF8B949E))) { append(token) }
                }
                token == "\"" || token == "\'" -> {
                    inString = true
                    withStyle(SpanStyle(color = Color(0xFFA5D6FF))) { append(token) }
                }
                token in keywords -> {
                    withStyle(SpanStyle(color = Color(0xFFFF7B72))) { append(token) }
                }
                token in types -> {
                    withStyle(SpanStyle(color = Color(0xFF79C0FF))) { append(token) }
                }
                token.toIntOrNull() != null -> {
                    withStyle(SpanStyle(color = Color(0xFFD2A8FF))) { append(token) }
                }
                else -> {
                    append(token)
                }
            }
        }
    }
}
