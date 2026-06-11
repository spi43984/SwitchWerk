package de.piecha.switchwerk.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import de.piecha.switchwerk.domain.model.ApiMethod
import de.piecha.switchwerk.domain.model.Device
import de.piecha.switchwerk.domain.model.WifiProfile
import de.piecha.switchwerk.viewmodel.DeviceConnectionFormState
import de.piecha.switchwerk.viewmodel.SettingsViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    BackHandler(enabled = uiState.isEditingWifiProfile) {
        viewModel.cancelWifiProfileEdit()
    }

    BackHandler(enabled = uiState.isEditingDevice) {
        viewModel.cancelDeviceEdit()
    }

    BackHandler(enabled = !uiState.isEditingWifiProfile && !uiState.isEditingDevice) {
        onNavigateBack()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Einstellungen",
                style = MaterialTheme.typography.headlineLarge
            )
        }

        uiState.errorMessage?.let { message ->
            item {
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        item {
            WifiProfileManagementSection(
                profiles = uiState.wifiProfiles,
                isEditing = uiState.isEditingWifiProfile,
                ssid = uiState.form.ssid,
                password = uiState.form.password,
                isPasswordVisible = uiState.form.isPasswordVisible,
                onAddClick = viewModel::startNewWifiProfile,
                onEditClick = viewModel::startEditWifiProfile,
                onDeleteClick = viewModel::deleteWifiProfile,
                onSsidChange = viewModel::updateWifiProfileSsid,
                onPasswordChange = viewModel::updateWifiProfilePassword,
                onClearPasswordClick = viewModel::clearWifiProfilePassword,
                onTogglePasswordVisibility = viewModel::toggleWifiPasswordVisibility,
                onSaveClick = viewModel::saveWifiProfile,
                onCancelClick = viewModel::cancelWifiProfileEdit
            )
        }

        item {
            DeviceManagementSection(
                devices = uiState.devices,
                wifiProfiles = uiState.wifiProfiles,
                isEditing = uiState.isEditingDevice,
                name = uiState.deviceForm.name,
                actionLabel = uiState.deviceForm.actionLabel,
                apiMethod = uiState.deviceForm.apiMethod,
                apiPath = uiState.deviceForm.apiPath,
                sortOrder = uiState.deviceForm.sortOrder,
                connections = uiState.deviceForm.connections,
                onAddClick = viewModel::startNewDevice,
                onEditClick = viewModel::startEditDevice,
                onDeleteClick = viewModel::deleteDevice,
                onNameChange = viewModel::updateDeviceName,
                onActionLabelChange = viewModel::updateDeviceActionLabel,
                onApiMethodChange = viewModel::updateDeviceApiMethod,
                onApiPathChange = viewModel::updateDeviceApiPath,
                onSortOrderChange = viewModel::updateDeviceSortOrder,
                onConnectionHostChange = viewModel::updateDeviceConnectionHost,
                onSaveClick = viewModel::saveDevice,
                onCancelClick = viewModel::cancelDeviceEdit
            )
        }

        item {
            SettingsSection(
                title = "Import / Export",
                description = "Konfigurationen können später ohne WLAN-Passwörter exportiert und importiert werden."
            )
        }

        item {
            Button(
                onClick = onNavigateBack
            ) {
                Text("Zurück zum Dashboard")
            }
        }
    }
}

@Composable
private fun WifiProfileManagementSection(
    profiles: List<WifiProfile>,
    isEditing: Boolean,
    ssid: String,
    password: String,
    isPasswordVisible: Boolean,
    onAddClick: () -> Unit,
    onEditClick: (WifiProfile) -> Unit,
    onDeleteClick: (String) -> Unit,
    onSsidChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onClearPasswordClick: () -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SectionHeader(
                title = "WLAN-Profile",
                showAddButton = !isEditing,
                onAddClick = onAddClick,
                addDescription = "WLAN-Profil hinzufügen"
            )

            if (isEditing) {
                WifiProfileForm(
                    ssid = ssid,
                    password = password,
                    isPasswordVisible = isPasswordVisible,
                    onSsidChange = onSsidChange,
                    onPasswordChange = onPasswordChange,
                    onClearPasswordClick = onClearPasswordClick,
                    onTogglePasswordVisibility = onTogglePasswordVisibility,
                    onSaveClick = onSaveClick,
                    onCancelClick = onCancelClick
                )
            } else if (profiles.isEmpty()) {
                Text("Keine WLAN-Profile konfiguriert.")
            } else {
                profiles.forEach { profile ->
                    WifiProfileRow(
                        profile = profile,
                        onEditClick = { onEditClick(profile) },
                        onDeleteClick = { onDeleteClick(profile.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DeviceManagementSection(
    devices: List<Device>,
    wifiProfiles: List<WifiProfile>,
    isEditing: Boolean,
    name: String,
    actionLabel: String,
    apiMethod: String,
    apiPath: String,
    sortOrder: String,
    connections: List<DeviceConnectionFormState>,
    onAddClick: () -> Unit,
    onEditClick: (Device) -> Unit,
    onDeleteClick: (String) -> Unit,
    onNameChange: (String) -> Unit,
    onActionLabelChange: (String) -> Unit,
    onApiMethodChange: (String) -> Unit,
    onApiPathChange: (String) -> Unit,
    onSortOrderChange: (String) -> Unit,
    onConnectionHostChange: (String, String) -> Unit,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SectionHeader(
                title = "Geräte",
                showAddButton = !isEditing,
                onAddClick = onAddClick,
                addDescription = "Gerät hinzufügen"
            )

            if (isEditing) {
                DeviceForm(
                    wifiProfiles = wifiProfiles,
                    name = name,
                    actionLabel = actionLabel,
                    apiMethod = apiMethod,
                    apiPath = apiPath,
                    sortOrder = sortOrder,
                    connections = connections,
                    onNameChange = onNameChange,
                    onActionLabelChange = onActionLabelChange,
                    onApiMethodChange = onApiMethodChange,
                    onApiPathChange = onApiPathChange,
                    onSortOrderChange = onSortOrderChange,
                    onConnectionHostChange = onConnectionHostChange,
                    onSaveClick = onSaveClick,
                    onCancelClick = onCancelClick
                )
            } else if (devices.isEmpty()) {
                Text("Keine Geräte konfiguriert.")
            } else {
                devices.forEach { device ->
                    DeviceRow(
                        device = device,
                        onEditClick = { onEditClick(device) },
                        onDeleteClick = { onDeleteClick(device.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    showAddButton: Boolean,
    onAddClick: () -> Unit,
    addDescription: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium
        )

        if (showAddButton) {
            IconButton(
                onClick = onAddClick,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = addDescription
                )
            }
        }
    }
}

@Composable
private fun WifiProfileRow(
    profile: WifiProfile,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var pendingDelete by remember { mutableStateOf(false) }

    if (pendingDelete) {
        ConfirmDeleteDialog(
            title = "WLAN-Profil löschen",
            text = "SSID ${profile.ssid} wirklich löschen?",
            onConfirm = {
                pendingDelete = false
                onDeleteClick()
            },
            onDismiss = {
                pendingDelete = false
            }
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = profile.ssid,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium
        )

        Row {
            IconButton(onClick = onEditClick) {
                Icon(Icons.Filled.Edit, contentDescription = "WLAN-Profil bearbeiten")
            }

            IconButton(onClick = { pendingDelete = true }) {
                Icon(Icons.Filled.Delete, contentDescription = "WLAN-Profil löschen")
            }
        }
    }
}

@Composable
private fun DeviceRow(
    device: Device,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var pendingDelete by remember { mutableStateOf(false) }

    if (pendingDelete) {
        ConfirmDeleteDialog(
            title = "Gerät löschen",
            text = "Gerät ${device.name} wirklich löschen?",
            onConfirm = {
                pendingDelete = false
                onDeleteClick()
            },
            onDismiss = {
                pendingDelete = false
            }
        )
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = device.name,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "${device.apiCall.method} ${device.apiCall.path}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "${device.connections.size} WLAN-Zuordnung(en)",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Row {
                IconButton(onClick = onEditClick) {
                    Icon(Icons.Filled.Edit, contentDescription = "Gerät bearbeiten")
                }

                IconButton(onClick = { pendingDelete = true }) {
                    Icon(Icons.Filled.Delete, contentDescription = "Gerät löschen")
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun WifiProfileForm(
    ssid: String,
    password: String,
    isPasswordVisible: Boolean,
    onSsidChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onClearPasswordClick: () -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = ssid,
            onValueChange = onSsidChange,
            label = { Text("SSID") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text("Passwort") },
            singleLine = true,
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = onTogglePasswordVisibility) {
                    Icon(
                        imageVector = if (isPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = "Passwort anzeigen oder verbergen"
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = "Leeres Passwort ist erlaubt. Vorhandenes Passwort bleibt nur erhalten, wenn die Sternchen unverändert bleiben.",
            style = MaterialTheme.typography.bodySmall
        )

        FormButtons(
            onSaveClick = onSaveClick,
            onCancelClick = onCancelClick
        )

        OutlinedButton(onClick = onClearPasswordClick) {
            Text("Passwort leeren")
        }
    }
}

@Composable
private fun DeviceForm(
    wifiProfiles: List<WifiProfile>,
    name: String,
    actionLabel: String,
    apiMethod: String,
    apiPath: String,
    sortOrder: String,
    connections: List<DeviceConnectionFormState>,
    onNameChange: (String) -> Unit,
    onActionLabelChange: (String) -> Unit,
    onApiMethodChange: (String) -> Unit,
    onApiPathChange: (String) -> Unit,
    onSortOrderChange: (String) -> Unit,
    onConnectionHostChange: (String, String) -> Unit,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = actionLabel,
            onValueChange = onActionLabelChange,
            label = { Text("Button-Beschriftung") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ApiMethod.entries.forEach { method ->
                OutlinedButton(
                    onClick = { onApiMethodChange(method.name) }
                ) {
                    Text(if (apiMethod == method.name) "✓ ${method.name}" else method.name)
                }
            }
        }

        OutlinedTextField(
            value = apiPath,
            onValueChange = onApiPathChange,
            label = { Text("API-Aufruf") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = sortOrder,
            onValueChange = onSortOrderChange,
            label = { Text("Sortierreihenfolge") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = "WLAN-Zuordnungen",
            style = MaterialTheme.typography.titleSmall
        )

        if (wifiProfiles.isEmpty()) {
            Text(
                text = "Lege zuerst mindestens ein WLAN-Profil an.",
                style = MaterialTheme.typography.bodySmall
            )
        } else {
            connections.forEach { connection ->
                OutlinedTextField(
                    value = connection.host,
                    onValueChange = { host ->
                        onConnectionHostChange(connection.wifiProfileId, host)
                    },
                    label = { Text("${connection.ssid}: Hostname/IP") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Text(
                text = "Leere Host-Felder bedeuten: Dieses WLAN ist dem Gerät nicht zugeordnet.",
                style = MaterialTheme.typography.bodySmall
            )
        }

        FormButtons(
            onSaveClick = onSaveClick,
            onCancelClick = onCancelClick
        )
    }
}

@Composable
private fun FormButtons(
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = onSaveClick) {
            Text("Speichern")
        }

        OutlinedButton(onClick = onCancelClick) {
            Text("Abbrechen")
        }
    }
}

@Composable
private fun ConfirmDeleteDialog(
    title: String,
    text: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Nein")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onConfirm) {
                Text("Ja")
            }
        }
    )
}

@Composable
private fun SettingsSection(
    title: String,
    description: String
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
