package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FaceRetouchingNatural
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MovieFilter
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.ViewQuilt
import androidx.compose.material.icons.filled.WbIncandescent
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ToolItem

@Composable
fun ToolGridItem(
    tool: ToolItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .testTag("tool_card_${tool.id}")
            .height(96.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 6.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = getToolIcon(tool.iconName),
                    contentDescription = tool.title,
                    modifier = Modifier.size(26.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = tool.title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 14.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Optional tiny badge (AI / HOT / PRO)
            tool.badge?.let { badge ->
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 5.dp, end = 5.dp)
                        .background(
                            color = when (badge) {
                                "HOT" -> Color(0xFFFF4D4F)
                                "PRO" -> Color(0xFF722ED1)
                                "LITE" -> Color(0xFF13C2C2)
                                "MUSIC" -> Color(0xFFFA8C16)
                                else -> Color(0xFF1890FF)
                            },
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = badge,
                        color = Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun getToolIcon(name: String): ImageVector {
    return when (name) {
        "face_retouching_natural" -> Icons.Default.FaceRetouchingNatural
        "subtitles" -> Icons.Default.Subtitles
        "desktop_windows" -> Icons.Default.DesktopWindows
        "photo_camera" -> Icons.Default.PhotoCamera
        "auto_fix_high" -> Icons.Default.AutoFixHigh
        "speed" -> Icons.Default.Speed
        "mic" -> Icons.Default.Mic
        "view_quilt" -> Icons.Default.ViewQuilt
        "crop" -> Icons.Default.Crop
        "laptop" -> Icons.Default.Laptop
        "movie_filter" -> Icons.Default.MovieFilter
        "account_box" -> Icons.Default.AccountBox
        "translate" -> Icons.Default.Translate
        "forum" -> Icons.Default.Forum
        "image" -> Icons.Default.Image
        "auto_awesome" -> Icons.Default.AutoAwesome
        "video_call" -> Icons.Default.VideoCall
        "content_cut" -> Icons.Default.ContentCut
        "trending_up" -> Icons.Default.TrendingUp
        "video_library" -> Icons.Default.VideoLibrary
        "psychology" -> Icons.Default.Psychology
        "hearing" -> Icons.Default.Hearing
        "smart_display" -> Icons.Default.SmartDisplay
        "bolt" -> Icons.Default.Bolt
        "record_voice_over" -> Icons.Default.RecordVoiceOver
        "music_note" -> Icons.Default.MusicNote
        "edit" -> Icons.Default.Edit
        "person_remove" -> Icons.Default.PersonRemove
        "wb_incandescent" -> Icons.Default.WbIncandescent
        "aspect_ratio" -> Icons.Default.AspectRatio
        else -> Icons.Default.Widgets
    }
}
