package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SubtitleItem
import com.example.data.model.SubtitlePosition
import com.example.data.model.SubtitleStyle
import kotlinx.coroutines.delay

@Composable
fun SubtitlePreviewCanvas(
    subtitles: List<SubtitleItem>,
    style: SubtitleStyle,
    modifier: Modifier = Modifier
) {
    var isPlaying by remember { mutableStateOf(false) }
    var currentProgressMs by remember { mutableFloatStateOf(0f) }
    val totalDurationMs = remember(subtitles) {
        (subtitles.maxOfOrNull { it.endMs } ?: 15000L).coerceAtLeast(10000L).toFloat()
    }

    // Auto-advance playhead when playing
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            delay(100)
            currentProgressMs += 100f
            if (currentProgressMs >= totalDurationMs) {
                currentProgressMs = 0f
                isPlaying = false
            }
        }
    }

    // Find active subtitle for current playhead
    val currentSub = remember(subtitles, currentProgressMs) {
        subtitles.firstOrNull {
            currentProgressMs.toLong() in it.startMs..it.endMs
        } ?: subtitles.firstOrNull()
    }

    val textColor = remember(style.textColorHex) {
        try {
            Color(android.graphics.Color.parseColor(style.textColorHex))
        } catch (e: Exception) {
            Color.Yellow
        }
    }

    val bgColor = remember(style.bgColorHex) {
        try {
            Color(android.graphics.Color.parseColor(style.bgColorHex))
        } catch (e: Exception) {
            Color(0x99000000)
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Video Preview Container (16:9)
        Box(
            modifier = Modifier
                .testTag("subtitle_canvas_preview")
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0F2027),
                            Color(0xFF203A43),
                            Color(0xFF2C5364)
                        )
                    )
                )
                .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(16.dp))
        ) {
            // Simulated video content backdrop
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                // Resolution & Aspect Badge
                Text(
                    text = "1080p • 60fps • Hendy Vietsub",
                    color = Color(0x80FFFFFF),
                    fontSize = 10.sp,
                    modifier = Modifier.align(Alignment.TopStart)
                )

                // Subtitle Overlay
                val alignment = when (style.position) {
                    SubtitlePosition.TOP -> Alignment.TopCenter
                    SubtitlePosition.CENTER -> Alignment.Center
                    SubtitlePosition.BOTTOM -> Alignment.BottomCenter
                }

                currentSub?.let { sub ->
                    Box(
                        modifier = Modifier
                            .align(alignment)
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .shadow(if (style.hasOutline) 4.dp else 0.dp, RoundedCornerShape(8.dp))
                            .background(bgColor, RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = sub.vietnameseText.ifBlank { "Chưa có nội dung phụ đề" },
                            color = textColor,
                            fontSize = style.fontSizeSp.sp,
                            fontWeight = if (style.isBold) FontWeight.Bold else FontWeight.Normal,
                            textAlign = TextAlign.Center,
                            lineHeight = (style.fontSizeSp * 1.3f).sp
                        )
                    }
                }
            }
        }

        // Playback Timeline Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = { isPlaying = !isPlaying },
                modifier = Modifier.testTag("play_pause_button")
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Tạm dừng" else "Phát",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Slider(
                value = currentProgressMs,
                onValueChange = { currentProgressMs = it },
                valueRange = 0f..totalDurationMs,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
                    .testTag("timeline_slider"),
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary
                )
            )

            val currentSec = (currentProgressMs / 1000).toInt()
            val totalSec = (totalDurationMs / 1000).toInt()
            Text(
                text = "%02d:%02d / %02d:%02d".format(
                    currentSec / 60, currentSec % 60,
                    totalSec / 60, totalSec % 60
                ),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
