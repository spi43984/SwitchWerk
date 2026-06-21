package de.piecha.switchwerk.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import de.piecha.switchwerk.R
import kotlinx.coroutines.launch

private enum class SwipeRevealValue {
    Closed,
    OpenStart,
    OpenEnd
}

@Composable
fun SwipeToDeleteListItem(
    isOpen: Boolean,
    isAnyItemOpen: Boolean,
    onOpen: () -> Unit,
    onClose: () -> Unit,
    onContentClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val revealWidth = 56.dp
    val swipeDistance = 128.dp
    val swipeDistancePx = with(LocalDensity.current) { swipeDistance.toPx() }
    val anchors = remember(swipeDistancePx) {
        DraggableAnchors {
            SwipeRevealValue.Closed at 0f
            SwipeRevealValue.OpenStart at swipeDistancePx
            SwipeRevealValue.OpenEnd at -swipeDistancePx
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
        if (state.currentValue == SwipeRevealValue.Closed && isOpen) {
            onClose()
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
                MaterialTheme.colorScheme.errorContainer
            } else {
                Color.Transparent
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
                    .clickable(onClick = onClose),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (openDirection == SwipeRevealValue.OpenStart) {
                    DeleteAction(
                        onClick = {
                            onClose()
                            onDeleteClick()
                        }
                    )
                } else {
                    Box(modifier = Modifier.width(revealWidth))
                }

                Box(modifier = Modifier.weight(1f))

                if (openDirection == SwipeRevealValue.OpenEnd) {
                    DeleteAction(
                        onClick = {
                            onClose()
                            onDeleteClick()
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
private fun DeleteAction(onClick: () -> Unit) {
    Box(
        modifier = Modifier.width(56.dp),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = stringResource(R.string.delete),
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}
