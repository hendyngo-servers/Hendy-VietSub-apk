package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.PaletteBlack
import com.example.ui.theme.PaletteCoral
import com.example.ui.theme.PaletteEmerald
import com.example.ui.theme.PaletteGray
import com.example.ui.theme.PaletteOrange
import com.example.ui.theme.PalettePurple
import com.example.ui.theme.PaletteSky

val SUBTITLE_PALETTE_COLORS = listOf(
    PaletteBlack to "#1E1E1E",
    PaletteCoral to "#FF5252",
    PaletteOrange to "#FF9800",
    PaletteEmerald to "#10B981",
    PaletteSky to "#00B4D8",
    PalettePurple to "#A855F7",
    PaletteGray to "#FFFFFF"
)

@Composable
fun ColorPalettePicker(
    selectedHex: String,
    onColorSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SUBTITLE_PALETTE_COLORS.forEach { (color, hex) ->
            val isSelected = selectedHex.equals(hex, ignoreCase = true)
            Box(
                modifier = Modifier
                    .testTag("color_picker_$hex")
                    .size(if (isSelected) 34.dp else 28.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(
                        width = if (isSelected) 3.dp else 1.dp,
                        color = if (isSelected) Color(0xFF00C2FF) else Color(0x33000000),
                        shape = CircleShape
                    )
                    .clickable { onColorSelected(hex) }
            )
        }
    }
}
