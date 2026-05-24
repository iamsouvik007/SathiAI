package com.example.sathiai.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sathiai.ai.AiTone
import com.example.sathiai.ui.theme.AccentPrimary
import com.example.sathiai.ui.theme.SurfaceGlass
import com.example.sathiai.ui.theme.TextPrimary
import com.example.sathiai.ui.theme.TextSecondary

@Composable
fun ToneSelector(
    selectedTone: AiTone,
    onToneSelected: (AiTone) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(SurfaceGlass)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        AiTone.entries.forEach { tone ->
            val isSelected = selectedTone == tone
            val contentColor by animateColorAsState(
                if (isSelected) Color.White else TextSecondary,
                label = "color"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isSelected) AccentPrimary else Color.Transparent)
                    .clickable { onToneSelected(tone) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tone.displayName,
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = contentColor,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 12.sp
                    )
                )
            }
        }
    }
}