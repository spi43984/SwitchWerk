package de.piecha.switchwerk.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.piecha.switchwerk.domain.model.Device
import de.piecha.switchwerk.ui.components.AppMenuLayout
import de.piecha.switchwerk.ui.components.AppOverflowMenu
import de.piecha.switchwerk.viewmodel.DeviceActionUiState
import de.piecha.switchwerk.viewmodel.DiagnosticListItem
import de.piecha.switchwerk.viewmodel.MainViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun StartScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToHelp: () -> Unit,
    viewModel: MainViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val devices = uiState.devices.sortedBy { it.sortOrder }

    AppMenuLayout(
        onOpenSettings = onNavigateToSettings,
        onOpenHelp = onNavigateToHelp,
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(24.dp),
        rightEdgeExtension = 24.dp
    ) { openMenu ->
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SwitchWerk",
                    style = MaterialTheme.typography.headlineLarge
                )

                AppOverflowMenu(onClick = openMenu)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "${devices.size} Geräte gefunden",
                style = MaterialTheme.typography.bodyLarge
            )

            uiState.errorMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(modifier = Modifier.weight(1f)) {
                val detailHeight = uiState.appSettings.detailPanelHeight.fraction
                val deviceAreaWeight = if (uiState.appSettings.showActionDetails) {
                    1f - detailHeight
                } else {
                    1f
                }

                if (devices.isEmpty()) {
                    EmptyDeviceList(modifier = Modifier.weight(deviceAreaWeight))
                } else {
                    DeviceList(
                        devices = devices,
                        actionStates = uiState.deviceActionStates,
                        onDeviceActionClick = viewModel::executeDeviceAction,
                        onMoveUpClick = viewModel::moveDeviceUp,
                        onMoveDownClick = viewModel::moveDeviceDown,
                        modifier = Modifier.weight(deviceAreaWeight)
                    )
                }

                if (uiState.appSettings.showActionDetails) {
                    Spacer(modifier = Modifier.height(12.dp))
                    DiagnosticPanel(
                        items = uiState.diagnosticItems,
                        newestFirst = uiState.appSettings.diagnosticsNewestFirst,
                        onClear = viewModel::clearDiagnosticMessages,
                        onToggleSortOrder = viewModel::toggleDiagnosticSortOrder,
                        modifier = Modifier.weight(detailHeight)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyDeviceList(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Keine Geräte konfiguriert",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun DeviceList(
    devices: List<Device>,
    actionStates: Map<String, DeviceActionUiState>,
    onDeviceActionClick: (Device) -> Unit,
    onMoveUpClick: (String) -> Unit,
    onMoveDownClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = devices,
            key = { device -> device.id }
        ) { device ->
            DeviceCard(
                device = device,
                actionState = actionStates[device.id],
                canMoveUp = devices.indexOf(device) > 0,
                canMoveDown = devices.indexOf(device) < devices.lastIndex,
                onActionClick = { onDeviceActionClick(device) },
                onMoveUpClick = { onMoveUpClick(device.id) },
                onMoveDownClick = { onMoveDownClick(device.id) }
            )
        }
    }
}

@Composable
private fun DiagnosticPanel(
    items: List<DiagnosticListItem>,
    newestFirst: Boolean,
    onClear: () -> Unit,
    onToggleSortOrder: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    val displayedItems = if (newestFirst) items.asReversed() else items

    LaunchedEffect(items.size, newestFirst) {
        if (displayedItems.isNotEmpty()) {
            listState.scrollToItem(if (newestFirst) 0 else displayedItems.lastIndex)
        }
    }

    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Aktionsdetails",
                    style = MaterialTheme.typography.titleSmall
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onClear,
                        enabled = items.isNotEmpty()
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Aktionsprotokoll löschen"
                        )
                    }
                    OutlinedButton(onClick = onToggleSortOrder) {
                        Text(if (newestFirst) "Neueste oben" else "Neueste unten")
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(
                modifier = Modifier.weight(1f),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (displayedItems.isEmpty()) {
                    item {
                        Text(
                            text = "Noch keine Geräteaktion ausgeführt",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                } else {
                    items(displayedItems) { item ->
                        when (item) {
                            is DiagnosticListItem.Message -> Text(
                                text = item.text,
                                style = MaterialTheme.typography.bodySmall
                            )
                            DiagnosticListItem.Separator -> HorizontalDivider(
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DeviceCard(
    device: Device,
    actionState: DeviceActionUiState?,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onActionClick: () -> Unit,
    onMoveUpClick: () -> Unit,
    onMoveDownClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = device.name,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onActionClick,
                enabled = actionState !is DeviceActionUiState.Loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (actionState is DeviceActionUiState.Loading) {
                        "Wird ausgeführt..."
                    } else {
                        device.actionLabel
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onMoveUpClick,
                    enabled = canMoveUp,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowUp,
                        contentDescription = "Gerät nach oben verschieben"
                    )
                }

                IconButton(
                    onClick = onMoveDownClick,
                    enabled = canMoveDown,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = "Gerät nach unten verschieben"
                    )
                }
            }

            when (actionState) {
                is DeviceActionUiState.Success -> {
                    Text(
                        text = actionState.message,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                is DeviceActionUiState.Error -> {
                    Text(
                        text = actionState.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                DeviceActionUiState.Loading,
                null -> Unit
            }
        }
    }
}
