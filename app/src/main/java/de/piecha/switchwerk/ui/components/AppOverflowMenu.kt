package de.piecha.switchwerk.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun AppMenuLayout(
    onOpenSettings: () -> Unit,
    onOpenHelp: () -> Unit,
    modifier: Modifier = Modifier,
    rightEdgeExtension: Dp = 0.dp,
    content: @Composable (openMenu: () -> Unit) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        content { isExpanded = true }

        if (isExpanded) {
            Surface(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .offset(x = rightEdgeExtension)
                    .width(220.dp + rightEdgeExtension)
                    .fillMaxHeight(),
                shape = MaterialTheme.shapes.medium,
                tonalElevation = 6.dp,
                shadowElevation = 8.dp
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    VerticalDivider()
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(end = rightEdgeExtension),
                        horizontalAlignment = Alignment.End
                    ) {
                        IconButton(onClick = { isExpanded = false }) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Menü schließen"
                            )
                        }
                        MenuItem(
                            text = "Einstellungen",
                            onClick = {
                                isExpanded = false
                                onOpenSettings()
                            }
                        )
                        MenuItem(
                            text = "Hilfe",
                            onClick = {
                                isExpanded = false
                                onOpenHelp()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppOverflowMenu(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = Icons.Filled.Menu,
            contentDescription = "Menü öffnen"
        )
    }
}

@Composable
private fun MenuItem(text: String, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = text,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.End
        )
    }
}
