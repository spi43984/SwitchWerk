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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.piecha.switchwerk.domain.model.Device
import de.piecha.switchwerk.viewmodel.DeviceActionUiState
import de.piecha.switchwerk.viewmodel.MainViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun StartScreen(
    onNavigateToSettings: () -> Unit,
    viewModel: MainViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val devices = uiState.devices.sortedBy { it.sortOrder }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(24.dp)
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

            OutlinedButton(
                onClick = onNavigateToSettings
            ) {
                Text("Einstellungen")
            }
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

        if (devices.isEmpty()) {
            EmptyDeviceList()
        } else {
            DeviceList(
                devices = devices,
                actionStates = uiState.deviceActionStates,
                onDeviceActionClick = viewModel::executeDeviceAction
            )
        }
    }
}

@Composable
private fun EmptyDeviceList() {
    Column(
        modifier = Modifier.fillMaxWidth(),
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
    onDeviceActionClick: (Device) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = devices,
            key = { device -> device.id }
        ) { device ->
            DeviceCard(
                device = device,
                actionState = actionStates[device.id],
                onActionClick = { onDeviceActionClick(device) }
            )
        }
    }
}

@Composable
private fun DeviceCard(
    device: Device,
    actionState: DeviceActionUiState?,
    onActionClick: () -> Unit
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
                enabled = actionState !is DeviceActionUiState.Loading
            ) {
                Text(
                    if (actionState is DeviceActionUiState.Loading) {
                        "Wird ausgeführt..."
                    } else {
                        device.actionLabel
                    }
                )
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
