package de.piecha.switchwerk.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import de.piecha.switchwerk.R

@Composable
fun InfoHint(
    @StringRes titleResourceId: Int,
    @StringRes messageResourceId: Int,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }

    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
        IconButton(
            onClick = { isVisible = true },
            modifier = modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = stringResource(R.string.show_information)
            )
        }
    }

    if (isVisible) {
        StandardConfigurationDialog(
            title = stringResource(titleResourceId),
            onDismissRequest = { isVisible = false },
            actionText = null,
            onAction = null,
            cancelText = stringResource(R.string.close)
        ) {
            BulletListText(stringResource(messageResourceId))
        }
    }
}

@Composable
fun BulletListText(message: String, modifier: Modifier = Modifier) {
    val lines = message.lines()
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        lines.forEach { line ->
            if (line.startsWith(BULLET_PREFIX)) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(BULLET, modifier = Modifier.padding(end = 8.dp))
                    Text(
                        text = line.removePrefix(BULLET_PREFIX),
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                Text(line)
            }
        }
    }
}

private const val BULLET = "•"
private const val BULLET_PREFIX = "$BULLET "
