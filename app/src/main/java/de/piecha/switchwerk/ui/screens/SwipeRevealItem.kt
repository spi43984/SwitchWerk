package de.piecha.switchwerk.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

private enum class SwipeRevealValue {
    Closed,
    OpenStart,
    OpenEnd
}

@Composable
fun SwipeRevealItem(
    isOpen: Boolean,
    isAnyItemOpen: Boolean,
    onOpen: () -> Unit,
    onClose: () -> Unit,
    onContentClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val revealWidth = 112.dp
    val revealWidthPx = with(LocalDensity.current) { revealWidth.toPx() }
    val anchors = remember(revealWidthPx) {
        DraggableAnchors {
            SwipeRevealValue.Closed at 0f
            SwipeRevealValue.OpenStart at revealWidthPx
            SwipeRevealValue.OpenEnd at -revealWidthPx
        }
    }
    val state = remember(anchors) {
        AnchoredDraggableState(
            initialValue = SwipeRevealValue.Closed,
            anchors = anchors
        )
    }
    val coroutineScope = rememberCoroutineScope()
    val isRevealed = isOpen ||
        state.currentValue != SwipeRevealValue.Closed ||
        state.targetValue != SwipeRevealValue.Closed
    val openDirection = when {
        state.targetValue != SwipeRevealValue.Closed -> state.targetValue
        else -> state.currentValue
    }

    LaunchedEffect(isOpen) {
        if (!isOpen && state.currentValue != SwipeRevealValue.Closed) {
            state.animateTo(SwipeRevealValue.Closed)
        }
    }

    LaunchedEffect(state.targetValue) {
        if (state.targetValue != SwipeRevealValue.Closed) {
            onOpen()
        }
    }

    LaunchedEffect(state.currentValue) {
        if (state.currentValue == SwipeRevealValue.Closed) {
            if (isOpen) {
                onClose()
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .anchoredDraggable(
                state = state,
                orientation = Orientation.Horizontal
            )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    if (isAnyItemOpen) {
                        coroutineScope.launch {
                            state.animateTo(SwipeRevealValue.Closed)
                            onClose()
                        }
                    } else {
                        onContentClick()
                    }
                },
            color = if (isRevealed) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ) {
            Box(
                modifier = if (isRevealed) {
                    when (openDirection) {
                        SwipeRevealValue.OpenStart -> Modifier.padding(start = revealWidth)
                        SwipeRevealValue.OpenEnd -> Modifier.padding(end = revealWidth)
                        SwipeRevealValue.Closed -> Modifier
                    }
                } else {
                    Modifier
                },
                content = content
            )
        }

        if (isRevealed) {
            Row(
                modifier = Modifier
                    .matchParentSize()
                    .clickable(onClick = onClose)
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (openDirection == SwipeRevealValue.OpenStart) {
                    SwipeRevealActions(
                        onEditClick = {
                            coroutineScope.launch {
                                state.animateTo(SwipeRevealValue.Closed)
                                onClose()
                                onEditClick()
                            }
                        },
                        onDeleteClick = {
                            coroutineScope.launch {
                                state.animateTo(SwipeRevealValue.Closed)
                                onClose()
                                onDeleteClick()
                            }
                        }
                    )
                } else {
                    Box(modifier = Modifier.width(revealWidth))
                }

                if (openDirection == SwipeRevealValue.OpenEnd) {
                    SwipeRevealActions(
                        onEditClick = {
                            coroutineScope.launch {
                                state.animateTo(SwipeRevealValue.Closed)
                                onClose()
                                onEditClick()
                            }
                        },
                        onDeleteClick = {
                            coroutineScope.launch {
                                state.animateTo(SwipeRevealValue.Closed)
                                onClose()
                                onDeleteClick()
                            }
                        }
                    )
                } else {
                    Box(modifier = Modifier.width(revealWidth))
                }
            }
        }
    }
}

@Composable
private fun SwipeRevealActions(
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Row {
        SwipeRevealAction(
            onClick = onEditClick
        ) {
            Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = "Bearbeiten",
                tint = MaterialTheme.colorScheme.primary
            )
        }

        SwipeRevealAction(
            onClick = onDeleteClick
        ) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = "Löschen",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun SwipeRevealAction(
    onClick: () -> Unit,
    icon: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .width(54.dp)
            .padding(horizontal = 1.dp),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(48.dp)
        ) {
            icon()
        }
    }
}
