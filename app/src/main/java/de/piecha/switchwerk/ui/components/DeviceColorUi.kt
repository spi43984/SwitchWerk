package de.piecha.switchwerk.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import de.piecha.switchwerk.R
import de.piecha.switchwerk.domain.model.DeviceColor

@Composable
fun DeviceColorIndicator(
    color: DeviceColor,
    modifier: Modifier = Modifier,
    selected: Boolean = false
) {
    val fill = color.toComposeColor() ?: MaterialTheme.colorScheme.surface
    val borderColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline
    }
    Box(
        modifier = modifier
            .size(if (selected) 36.dp else 20.dp)
            .border(if (selected) 3.dp else 1.dp, borderColor, CircleShape)
            .background(fill, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Icon(
                imageVector = if (color == DeviceColor.NONE) Icons.Filled.Close else Icons.Filled.Check,
                contentDescription = null,
                tint = color.contentColor(),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun DeviceColorPicker(
    selectedColor: DeviceColor,
    onColorChange: (DeviceColor) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        DeviceColor.entries.chunked(6).forEach { rowColors ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                rowColors.forEach { color ->
                    val colorDescription = stringResource(color.labelResource())
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .semantics { contentDescription = colorDescription }
                            .selectable(
                                selected = color == selectedColor,
                                onClick = { onColorChange(color) }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        DeviceColorIndicator(
                            color = color,
                            selected = color == selectedColor
                        )
                    }
                }
            }
        }
    }
}

fun DeviceColor.toComposeColor(): Color? = argb?.let { value -> Color(value) }

@StringRes
fun DeviceColor.labelResource(): Int = when (this) {
    DeviceColor.NONE -> R.string.device_color_none
    DeviceColor.RED -> R.string.device_color_red
    DeviceColor.ORANGE -> R.string.device_color_orange
    DeviceColor.YELLOW -> R.string.device_color_yellow
    DeviceColor.GREEN -> R.string.device_color_green
    DeviceColor.TEAL -> R.string.device_color_teal
    DeviceColor.BLUE -> R.string.device_color_blue
    DeviceColor.PURPLE -> R.string.device_color_purple
    DeviceColor.BROWN -> R.string.device_color_brown
    DeviceColor.SLATE -> R.string.device_color_slate
    DeviceColor.PINK -> R.string.device_color_pink
    DeviceColor.LIME -> R.string.device_color_lime
}

@Composable
fun DeviceColor.contentColor(): Color = when (this) {
    DeviceColor.NONE -> MaterialTheme.colorScheme.onSurface
    DeviceColor.RED,
    DeviceColor.ORANGE,
    DeviceColor.YELLOW,
    DeviceColor.GREEN,
    DeviceColor.TEAL,
    DeviceColor.LIME,
    DeviceColor.PINK -> Color.Black
    else -> Color.White
}
