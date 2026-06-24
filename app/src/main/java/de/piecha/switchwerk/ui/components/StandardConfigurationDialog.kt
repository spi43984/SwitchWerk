package de.piecha.switchwerk.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.res.stringResource
import de.piecha.switchwerk.R

@Composable
fun StandardConfigurationDialog(
    title: String,
    onDismissRequest: () -> Unit,
    actionText: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
    actionEnabled: Boolean = true,
    secondaryActionText: String? = null,
    onSecondaryAction: (() -> Unit)? = null,
    verticalActions: Boolean = false,
    cancelText: String? = null,
    @StringRes infoTitleResourceId: Int? = null,
    @StringRes infoMessageResourceId: Int? = null,
    content: @Composable () -> Unit
) {
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
                            .weight(1f, fill = false)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        content()
                    }
                    StandardDialogButtons(
                        actionText = actionText,
                        onAction = onAction,
                        cancelText = cancelText ?: stringResource(R.string.cancel),
                        onCancel = onDismissRequest,
                        actionEnabled = actionEnabled,
                        secondaryActionText = secondaryActionText,
                        onSecondaryAction = onSecondaryAction,
                        verticalActions = verticalActions
                    )
                }
            }
        }
    }
}

private const val MAX_DIALOG_HEIGHT_FRACTION = 0.85f

@Composable
fun StandardDialogButtons(
    actionText: String,
    onAction: () -> Unit,
    cancelText: String,
    onCancel: () -> Unit,
    actionEnabled: Boolean = true,
    secondaryActionText: String? = null,
    onSecondaryAction: (() -> Unit)? = null,
    verticalActions: Boolean = false
) {
    if (verticalActions) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StandardActionButton(
                text = actionText,
                onClick = onAction,
                enabled = actionEnabled,
                modifier = Modifier.fillMaxWidth()
            )
            if (secondaryActionText != null && onSecondaryAction != null) {
                StandardActionButton(
                    text = secondaryActionText,
                    onClick = onSecondaryAction,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            StandardActionButton(
                text = cancelText,
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth()
            )
        }
        return
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StandardActionButton(
            text = actionText,
            onClick = onAction,
            enabled = actionEnabled,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        )
        if (secondaryActionText != null && onSecondaryAction != null) {
            StandardActionButton(
                text = secondaryActionText,
                onClick = onSecondaryAction,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }
        StandardActionButton(
            text = cancelText,
            onClick = onCancel,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
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
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
