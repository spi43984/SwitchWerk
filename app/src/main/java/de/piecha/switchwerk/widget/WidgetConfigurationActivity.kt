package de.piecha.switchwerk.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import de.piecha.switchwerk.R
import de.piecha.switchwerk.ui.theme.SwitchWerkTheme
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

class WidgetConfigurationActivity : ComponentActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID
        setResult(Activity.RESULT_CANCELED, resultIntent())
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }
        setContent {
            val viewModel: WidgetConfigurationViewModel = koinViewModel()
            val uiState by viewModel.uiState.collectAsState()
            LaunchedEffect(appWidgetId) {
                viewModel.load(appWidgetId, getString(R.string.widget_title))
            }
            SwitchWerkTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WidgetConfigurationScreen(
                        uiState = uiState,
                        onTitleChange = viewModel::updateTitle,
                        onColumnModeChange = viewModel::updateColumnMode,
                        onToggle = viewModel::toggle,
                        onSave = {
                            lifecycleScope.launch {
                                if (viewModel.save()) {
                                    closeConfiguration(Activity.RESULT_OK)
                                }
                            }
                        },
                        onCancel = { closeConfiguration(Activity.RESULT_CANCELED) }
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        window.setLayout(
            (resources.displayMetrics.widthPixels * DIALOG_WIDTH_FRACTION).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    private fun resultIntent(): Intent {
        return Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
    }

    private fun closeConfiguration(resultCode: Int) {
        setResult(resultCode, resultIntent())
        finish()
    }
}

private const val DIALOG_WIDTH_FRACTION = 0.92f

@Composable
private fun WidgetConfigurationScreen(
    uiState: WidgetConfigurationUiState,
    onTitleChange: (String) -> Unit,
    onColumnModeChange: (WidgetColumnMode) -> Unit,
    onToggle: (WidgetActionTarget) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val defaultWidgetTitle = stringResource(R.string.widget_title)
        Text(
            text = stringResource(R.string.widget_configuration_title),
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = stringResource(R.string.widget_configuration_description),
            style = MaterialTheme.typography.bodyMedium
        )
        OutlinedTextField(
            value = uiState.title,
            onValueChange = onTitleChange,
            label = { Text(stringResource(R.string.widget_custom_title)) },
            placeholder = { Text(stringResource(R.string.widget_no_title)) },
            trailingIcon = {
                IconButton(onClick = { onTitleChange(defaultWidgetTitle) }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = stringResource(R.string.widget_restore_default_title)
                    )
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = stringResource(R.string.widget_layout),
                style = MaterialTheme.typography.titleSmall
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WidgetColumnMode.entries.forEach { mode ->
                    WidgetColumnModeOption(
                        text = stringResource(
                            when (mode) {
                                WidgetColumnMode.AUTO -> R.string.widget_layout_auto
                                WidgetColumnMode.ONE -> R.string.widget_layout_one_column
                                WidgetColumnMode.TWO -> R.string.widget_layout_two_columns
                            }
                        ),
                        selected = uiState.columnMode == mode,
                        onClick = { onColumnModeChange(mode) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        when {
            uiState.isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            uiState.availableActions.isEmpty() -> {
                Text(
                    text = stringResource(R.string.widget_configuration_empty),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(
                        items = uiState.availableActions,
                        key = { it.target.type.name + ":" + it.target.id }
                    ) { action ->
                        val selectedIndex = uiState.selectedTargets.indexOf(action.target)
                        WidgetActionSelectionRow(
                            action = action,
                            selectionOrder = selectedIndex.takeIf { it >= 0 }?.plus(1),
                            onToggle = { onToggle(action.target) }
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
        ) {
            OutlinedButton(onClick = onCancel) {
                Text(stringResource(R.string.cancel))
            }
            Button(
                onClick = onSave,
                enabled = uiState.selectedTargets.isNotEmpty()
            ) {
                Text(stringResource(R.string.save))
            }
        }
    }
}

@Composable
private fun WidgetColumnModeOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(52.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outlineVariant
            }
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun WidgetActionSelectionRow(
    action: AvailableWidgetAction,
    selectionOrder: Int?,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            modifier = Modifier.size(32.dp),
            shape = CircleShape,
            color = if (selectionOrder != null) {
                MaterialTheme.colorScheme.primary
            } else {
                Color.Transparent
            },
            border = BorderStroke(
                width = 2.dp,
                color = if (selectionOrder != null) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outline
                }
            )
        ) {
            Box(contentAlignment = Alignment.Center) {
                selectionOrder?.let { order ->
                    Text(
                        text = order.toString(),
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = action.title, style = MaterialTheme.typography.bodyLarge)
            Text(text = action.subtitle, style = MaterialTheme.typography.bodySmall)
        }
        Text(
            text = when (action.target.type) {
                WidgetActionTargetType.DEVICE -> stringResource(R.string.widget_target_device)
                WidgetActionTargetType.SWITCH_GROUP -> stringResource(R.string.widget_target_group)
            },
            style = MaterialTheme.typography.labelSmall
        )
    }
}
