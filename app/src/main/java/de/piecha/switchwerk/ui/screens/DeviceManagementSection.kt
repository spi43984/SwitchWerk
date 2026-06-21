package de.piecha.switchwerk.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.piecha.switchwerk.domain.model.ApiMethod
import de.piecha.switchwerk.domain.model.Device
import de.piecha.switchwerk.domain.model.WifiProfile
import de.piecha.switchwerk.ui.components.StandardConfigurationDialog
import de.piecha.switchwerk.viewmodel.DeviceConnectionFormState
import de.piecha.switchwerk.viewmodel.DeviceFormState

@Composable
fun DeviceManagementSection(
    devices: List<Device>,
    wifiProfiles: List<WifiProfile>,
    isEditing: Boolean,
    form: DeviceFormState,
    openSwipeItemId: String?,
    onOpenSwipeItem: (String) -> Unit,
    onCloseSwipeItem: () -> Unit,
    onAddClick: () -> Unit,
    onEditClick: (Device) -> Unit,
    onDeleteClick: (String) -> Unit,
    onNameChange: (String) -> Unit,
    onActionLabelChange: (String) -> Unit,
    onApiMethodChange: (String) -> Unit,
    onApiPathChange: (String) -> Unit,
    onAddConnection: (String, String) -> Unit,
    onUpdateConnection: (String, String, String) -> Unit,
    onDeleteConnection: (String) -> Unit,
    onMoveConnection: (String, Int) -> Unit,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isEditing) {
        DeviceEditDialog(
            form = form,
            wifiProfiles = wifiProfiles,
            onNameChange = onNameChange,
            onActionLabelChange = onActionLabelChange,
            onApiMethodChange = onApiMethodChange,
            onApiPathChange = onApiPathChange,
            onAddConnection = onAddConnection,
            onUpdateConnection = onUpdateConnection,
            onDeleteConnection = onDeleteConnection,
            onMoveConnection = onMoveConnection,
            onSaveClick = onSaveClick,
            onCancelClick = onCancelClick
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Name",
                style = MaterialTheme.typography.titleSmall
            )

            IconButton(
                onClick = onAddClick,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Gerät hinzufügen"
                )
            }
        }

        DeviceList(
            devices = devices,
            openSwipeItemId = openSwipeItemId,
            onOpenSwipeItem = onOpenSwipeItem,
            onCloseSwipeItem = onCloseSwipeItem,
            onEditClick = onEditClick,
            onDeleteClick = onDeleteClick,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun DeviceEditDialog(
    form: DeviceFormState,
    wifiProfiles: List<WifiProfile>,
    onNameChange: (String) -> Unit,
    onActionLabelChange: (String) -> Unit,
    onApiMethodChange: (String) -> Unit,
    onApiPathChange: (String) -> Unit,
    onAddConnection: (String, String) -> Unit,
    onUpdateConnection: (String, String, String) -> Unit,
    onDeleteConnection: (String) -> Unit,
    onMoveConnection: (String, Int) -> Unit,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    StandardConfigurationDialog(
        title = if (form.id == null) "Gerät hinzufügen" else "Gerät bearbeiten",
        onDismissRequest = onCancelClick,
        actionText = "Speichern",
        onAction = onSaveClick
    ) {
        DeviceForm(
            form = form,
            wifiProfiles = wifiProfiles,
            onNameChange = onNameChange,
            onActionLabelChange = onActionLabelChange,
            onApiMethodChange = onApiMethodChange,
            onApiPathChange = onApiPathChange,
            onAddConnection = onAddConnection,
            onUpdateConnection = onUpdateConnection,
            onDeleteConnection = onDeleteConnection,
            onMoveConnection = onMoveConnection
        )
    }
}

@Composable
private fun DeviceList(
    devices: List<Device>,
    openSwipeItemId: String?,
    onOpenSwipeItem: (String) -> Unit,
    onCloseSwipeItem: () -> Unit,
    onEditClick: (Device) -> Unit,
    onDeleteClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (devices.isEmpty()) {
        EmptyListArea(text = "Keine Geräte konfiguriert.", modifier = modifier)
        return
    }

    val listState = rememberLazyListState()
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp),
            contentAlignment = Alignment.Center
        ) {
            if (listState.canScrollBackward) {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowUp,
                    contentDescription = "Weitere Geräte oberhalb",
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        LazyColumn(
            state = listState,
            verticalArrangement = Arrangement.spacedBy(0.dp),
            modifier = Modifier
                .weight(1f)
                .clickable(
                    enabled = openSwipeItemId != null,
                    onClick = onCloseSwipeItem
                )
        ) {
            items(
                items = devices,
                key = { device -> device.id }
            ) { device ->
                val swipeItemId = "device:${device.id}"
                DeviceRow(
                    device = device,
                    isOpen = openSwipeItemId == swipeItemId,
                    isAnyItemOpen = openSwipeItemId != null,
                    onOpen = {
                        onOpenSwipeItem(swipeItemId)
                    },
                    onClose = onCloseSwipeItem,
                    onContentClick = {
                        if (openSwipeItemId == null) {
                            onEditClick(device)
                        } else {
                            onCloseSwipeItem()
                        }
                    },
                    onEditClick = { onEditClick(device) },
                    onDeleteClick = { onDeleteClick(device.id) }
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp),
            contentAlignment = Alignment.Center
        ) {
            if (listState.canScrollForward) {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = "Weitere Geräte unterhalb",
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptyListArea(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth(),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun DeviceRow(
    device: Device,
    isOpen: Boolean,
    isAnyItemOpen: Boolean,
    onOpen: () -> Unit,
    onClose: () -> Unit,
    onContentClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var pendingDeleteDevice by remember { mutableStateOf<Device?>(null) }

    pendingDeleteDevice?.let { deviceToDelete ->
        ConfirmDeviceDeleteDialog(
            device = deviceToDelete,
            onConfirm = {
                pendingDeleteDevice = null
                onDeleteClick()
            },
            onDismiss = {
                pendingDeleteDevice = null
            }
        )
    }

    SwipeRevealItem(
        isOpen = isOpen,
        isAnyItemOpen = isAnyItemOpen,
        onOpen = onOpen,
        onClose = onClose,
        onContentClick = onContentClick,
        onEditClick = onEditClick,
        onDeleteClick = {
            pendingDeleteDevice = device
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 0.dp, end = 0.dp, top = 0.dp, bottom = 0.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 4.dp, bottom = 4.dp)
            ) {
                Text(
                    text = device.name,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun ConfirmDeviceDeleteDialog(
    device: Device,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    StandardConfigurationDialog(
        title = "Gerät löschen",
        onDismissRequest = onDismiss,
        actionText = "Ja",
        onAction = onConfirm,
        cancelText = "Nein"
    ) {
        Text("Gerät ${device.name} wirklich löschen?")
    }
}

@Composable
private fun DeviceForm(
    form: DeviceFormState,
    wifiProfiles: List<WifiProfile>,
    onNameChange: (String) -> Unit,
    onActionLabelChange: (String) -> Unit,
    onApiMethodChange: (String) -> Unit,
    onApiPathChange: (String) -> Unit,
    onAddConnection: (String, String) -> Unit,
    onUpdateConnection: (String, String, String) -> Unit,
    onDeleteConnection: (String) -> Unit,
    onMoveConnection: (String, Int) -> Unit
) {
    OutlinedTextField(
        value = form.name,
        onValueChange = onNameChange,
        label = { Text("Name") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )

    OutlinedTextField(
        value = form.actionLabel,
        onValueChange = onActionLabelChange,
        label = { Text("Button-Beschriftung") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ApiMethod.entries.forEach { method ->
            OutlinedButton(
                onClick = {
                    onApiMethodChange(method.name)
                }
            ) {
                Text(
                    text = if (form.apiMethod == method.name) "✓ ${method.name}" else method.name
                )
            }
        }
    }

    OutlinedTextField(
        value = form.apiPath,
        onValueChange = onApiPathChange,
        label = { Text("API-Aufruf") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )

    DeviceConnectionList(
        wifiProfiles = wifiProfiles,
        connections = form.connections,
        onAddConnection = onAddConnection,
        onUpdateConnection = onUpdateConnection,
        onDeleteConnection = onDeleteConnection,
        onMoveConnection = onMoveConnection
    )
}

@Composable
private fun DeviceConnectionList(
    wifiProfiles: List<WifiProfile>,
    connections: List<DeviceConnectionFormState>,
    onAddConnection: (String, String) -> Unit,
    onUpdateConnection: (String, String, String) -> Unit,
    onDeleteConnection: (String) -> Unit,
    onMoveConnection: (String, Int) -> Unit
) {
    var isAddingConnection by remember { mutableStateOf(false) }
    var editingConnection by remember { mutableStateOf<DeviceConnectionFormState?>(null) }

    if (isAddingConnection) {
        ConnectionEditDialog(
            title = "WLAN-Zuordnung hinzufügen",
            wifiProfiles = wifiProfiles,
            usedWifiProfileIds = connections.map { it.wifiProfileId },
            initialConnection = null,
            onSave = { wifiProfileId, host ->
                onAddConnection(wifiProfileId, host)
                isAddingConnection = false
            },
            onCancel = {
                isAddingConnection = false
            }
        )
    }

    editingConnection?.let { connection ->
        ConnectionEditDialog(
            title = "WLAN-Zuordnung bearbeiten",
            wifiProfiles = wifiProfiles,
            usedWifiProfileIds = connections
                .filterNot { it.wifiProfileId == connection.wifiProfileId }
                .map { it.wifiProfileId },
            initialConnection = connection,
            onSave = { wifiProfileId, host ->
                onUpdateConnection(connection.wifiProfileId, wifiProfileId, host)
                editingConnection = null
            },
            onCancel = {
                editingConnection = null
            }
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "WLAN-Verbindungsreihenfolge",
            style = MaterialTheme.typography.titleSmall
        )

        IconButton(
            onClick = {
                isAddingConnection = true
            },
            enabled = wifiProfiles.any { profile ->
                connections.none { it.wifiProfileId == profile.id }
            },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "WLAN-Zuordnung hinzufügen"
            )
        }
    }

    if (connections.isEmpty()) {
        Text(
            text = "Noch keine WLAN-Zuordnung. Über + kannst du ein WLAN und Hostname/IP hinzufügen.",
            style = MaterialTheme.typography.bodySmall
        )
        return
    }

    Text(
        text = "Mit den Pfeilen sortieren. Beim Schalten gilt die Reihenfolge von oben nach unten.",
        style = MaterialTheme.typography.bodySmall
    )

    val listState = rememberLazyListState()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(
                items = connections,
                key = { _, connection -> connection.wifiProfileId }
            ) { index, connection ->
                DeviceConnectionRow(
                    connection = connection,
                    canMoveUp = index > 0,
                    canMoveDown = index < connections.lastIndex,
                    onMoveUpClick = {
                        onMoveConnection(connection.wifiProfileId, index - 1)
                    },
                    onMoveDownClick = {
                        onMoveConnection(connection.wifiProfileId, index + 1)
                    },
                    onEditClick = {
                        editingConnection = connection
                    },
                    onDeleteClick = {
                        onDeleteConnection(connection.wifiProfileId)
                    }
                )
            }
        }

        if (listState.canScrollBackward) {
            Icon(
                imageVector = Icons.Filled.KeyboardArrowUp,
                contentDescription = "Weitere WLAN-Zuordnungen oberhalb",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .size(16.dp)
            )
        }

        if (listState.canScrollForward) {
            Icon(
                imageVector = Icons.Filled.KeyboardArrowDown,
                contentDescription = "Weitere WLAN-Zuordnungen unterhalb",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .size(16.dp)
            )
        }
    }
}

@Composable
private fun DeviceConnectionRow(
    connection: DeviceConnectionFormState,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onMoveUpClick: () -> Unit,
    onMoveDownClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 0.dp, end = 0.dp, top = 0.dp, bottom = 0.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(top = 4.dp, bottom = 4.dp)
        ) {
            Text(
                text = connection.wifiProfileName,
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "SSID: ${connection.ssid} | ${connection.host}",
                style = MaterialTheme.typography.bodySmall
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onMoveUpClick,
                enabled = canMoveUp,
                modifier = Modifier.size(26.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowUp,
                    contentDescription = "WLAN-Zuordnung nach oben verschieben"
                )
            }

            IconButton(
                onClick = onMoveDownClick,
                enabled = canMoveDown,
                modifier = Modifier.size(26.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = "WLAN-Zuordnung nach unten verschieben"
                )
            }

            IconButton(
                onClick = onEditClick,
                modifier = Modifier.size(26.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = "WLAN-Zuordnung bearbeiten"
                )
            }

            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier.size(26.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "WLAN-Zuordnung löschen"
                )
            }
        }
    }
}

@Composable
private fun ConnectionEditDialog(
    title: String,
    wifiProfiles: List<WifiProfile>,
    usedWifiProfileIds: List<String>,
    initialConnection: DeviceConnectionFormState?,
    onSave: (String, String) -> Unit,
    onCancel: () -> Unit
) {
    val selectableProfiles = wifiProfiles.filterNot { profile ->
        usedWifiProfileIds.contains(profile.id)
    }

    var selectedWifiProfileId by remember(initialConnection, selectableProfiles) {
        mutableStateOf(
            initialConnection?.wifiProfileId ?: selectableProfiles.firstOrNull()?.id.orEmpty()
        )
    }

    var host by remember(initialConnection) {
        mutableStateOf(initialConnection?.host.orEmpty())
    }

    StandardConfigurationDialog(
        title = title,
        onDismissRequest = onCancel,
        actionText = "Speichern",
        onAction = { onSave(selectedWifiProfileId, host) },
        actionEnabled = selectedWifiProfileId.isNotBlank() && host.isNotBlank()
    ) {
        if (selectableProfiles.isEmpty()) {
            Text("Es sind keine weiteren WLAN-Profile verfügbar.")
        } else {
            selectableProfiles.forEach { profile ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedWifiProfileId == profile.id,
                        onClick = { selectedWifiProfileId = profile.id }
                    )

                    Column {
                        Text(
                            text = profile.name,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "SSID: ${profile.ssid}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            OutlinedTextField(
                value = host,
                onValueChange = { host = it },
                label = { Text("Hostname/IP") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
