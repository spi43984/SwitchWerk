package de.piecha.switchwerk.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import de.piecha.switchwerk.R
import kotlin.math.max

@Composable
fun StandardConfigurationDialog(
    title: String,
    onDismissRequest: () -> Unit,
    actionText: String?,
    onAction: (() -> Unit)?,
    modifier: Modifier = Modifier,
    actionEnabled: Boolean = true,
    scrollToBottom: Boolean = false,
    cancelText: String? = null,
    @StringRes infoTitleResourceId: Int? = null,
    @StringRes infoMessageResourceId: Int? = null,
    content: @Composable () -> Unit
) {
    val scrollState = rememberScrollState()
    var scrollViewportHeight by remember { mutableIntStateOf(0) }
    LaunchedEffect(scrollToBottom) {
        if (scrollToBottom) {
            withFrameNanos { }
            withFrameNanos { }
            scrollState.scrollTo(scrollState.maxValue)
        }
    }
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .imePadding(),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = modifier
                    .padding(horizontal = 16.dp)
                    .widthIn(max = 560.dp)
                    .fillMaxWidth()
                    .heightIn(max = maxHeight * MAX_DIALOG_HEIGHT_FRACTION)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = title, style = MaterialTheme.typography.titleLarge)
                        if (infoTitleResourceId != null && infoMessageResourceId != null) {
                            InfoHint(infoTitleResourceId, infoMessageResourceId)
                        }
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f, fill = false)
                                .onSizeChanged { scrollViewportHeight = it.height }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(scrollState)
                                    .padding(end = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                content()
                            }
                            VerticalScrollIndicator(
                                scrollState = scrollState,
                                viewportHeight = scrollViewportHeight,
                                modifier = Modifier.align(Alignment.CenterEnd)
                            )
                        }
                    }
                    StandardDialogButtons(
                        actionText = actionText,
                        onAction = onAction,
                        cancelText = cancelText ?: stringResource(R.string.cancel),
                        onCancel = onDismissRequest,
                        actionEnabled = actionEnabled
                    )
                }
            }
        }
    }
}

private const val MAX_DIALOG_HEIGHT_FRACTION = 0.85f
private const val SCROLL_INDICATOR_MIN_THUMB_HEIGHT_PX = 24f

@Composable
fun VerticalScrollIndicator(
    scrollState: ScrollState,
    viewportHeight: Int,
    modifier: Modifier = Modifier
) {
    if (scrollState.maxValue <= 0 || viewportHeight <= 0) {
        return
    }
    val contentHeight = viewportHeight + scrollState.maxValue
    val thumbFraction = viewportHeight.toFloat() / contentHeight
    val scrollFraction = scrollState.value.toFloat() / scrollState.maxValue
    ScrollIndicator(
        thumbFraction = thumbFraction,
        scrollFraction = scrollFraction,
        modifier = modifier
    )
}

@Composable
fun LazyListScrollIndicator(
    listState: LazyListState,
    modifier: Modifier = Modifier
) {
    val layoutInfo = listState.layoutInfo
    val visibleItems = layoutInfo.visibleItemsInfo
    val totalItemsCount = layoutInfo.totalItemsCount
    if (totalItemsCount <= 0 || visibleItems.isEmpty()) {
        return
    }
    if (!listState.canScrollBackward && !listState.canScrollForward) {
        return
    }
    val visibleItemsCount = visibleItems.size
    val scrollSteps = max(1, totalItemsCount - visibleItemsCount)
    ScrollIndicator(
        thumbFraction = visibleItemsCount.toFloat() / totalItemsCount,
        scrollFraction = listState.firstVisibleItemIndex.toFloat() / scrollSteps,
        modifier = modifier
    )
}

@Composable
fun LazyGridScrollIndicator(
    gridState: LazyGridState,
    modifier: Modifier = Modifier
) {
    val layoutInfo = gridState.layoutInfo
    val visibleItems = layoutInfo.visibleItemsInfo
    val totalItemsCount = layoutInfo.totalItemsCount
    if (totalItemsCount <= 0 || visibleItems.isEmpty()) {
        return
    }
    if (!gridState.canScrollBackward && !gridState.canScrollForward) {
        return
    }
    val visibleItemsCount = visibleItems.size
    val scrollSteps = max(1, totalItemsCount - visibleItemsCount)
    ScrollIndicator(
        thumbFraction = visibleItemsCount.toFloat() / totalItemsCount,
        scrollFraction = gridState.firstVisibleItemIndex.toFloat() / scrollSteps,
        modifier = modifier
    )
}

@Composable
private fun ScrollIndicator(
    thumbFraction: Float,
    scrollFraction: Float,
    modifier: Modifier = Modifier
) {
    val thumbColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
    Canvas(
        modifier = modifier
            .fillMaxHeight()
            .width(3.dp)
    ) {
        val thumbHeight = max(
            SCROLL_INDICATOR_MIN_THUMB_HEIGHT_PX,
            size.height * thumbFraction
        ).coerceAtMost(size.height)
        val scrollRange = size.height - thumbHeight
        val thumbTop = scrollRange * scrollFraction.coerceIn(0f, 1f)
        drawRoundRect(
            color = thumbColor,
            topLeft = Offset(x = 0f, y = thumbTop),
            size = Size(width = size.width, height = thumbHeight),
            cornerRadius = CornerRadius(size.width / 2f)
        )
    }
}

@Composable
fun StandardDialogButtons(
    actionText: String?,
    onAction: (() -> Unit)?,
    cancelText: String,
    onCancel: () -> Unit,
    actionEnabled: Boolean = true,
    actionWeight: Float = 1f,
    cancelUsesWeight: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (actionText != null && onAction != null) {
            StandardActionButton(
                text = actionText,
                onClick = onAction,
                enabled = actionEnabled,
                modifier = Modifier
                    .weight(actionWeight)
                    .fillMaxHeight()
            )
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }
        val hasAction = actionText != null && onAction != null
        val cancelModifier = if (hasAction && cancelUsesWeight) {
            Modifier
                .weight(1f)
                .fillMaxHeight()
        } else {
            Modifier.fillMaxHeight()
        }
        StandardActionButton(
            text = cancelText,
            onClick = onCancel,
            modifier = cancelModifier
        )
    }
}

@Composable
fun StandardActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.heightIn(min = 48.dp)
    ) {
        Text(
            text = text,
            textAlign = TextAlign.Center
        )
    }
}
