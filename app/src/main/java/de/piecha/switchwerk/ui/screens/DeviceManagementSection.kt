package de.piecha.switchwerk.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.unit.dp
import de.piecha.switchwerk.domain.model.ApiMethod
import de.piecha.switchwerk.domain.model.Device
import de.piecha.switchwerk.domain.model.WifiProfile
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
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit
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
            onSaveClick = onSaveClick,
            onCancelClick = onCancelClick
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(start = 12.dp, top = 6.dp, end = 6.dp, bottom = 6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Geräte",
                    style = MaterialTheme.typography.titleMedium
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
                onDeleteClick = onDeleteClick
            )
        }
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
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    Dialog(
        onDismissRequest = onCancelClick,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (form.id == null) "Gerät hinzufügen" else "Gerät bearbeiten",
                    style = MaterialTheme.typography.titleLarge
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 600.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
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
                        onDeleteConnection = onDeleteConnection
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onSaveClick
                    ) {
                        Text("Speichern")
                    }

                    OutlinedButton(
                        onClick = onCancelClick
                    ) {
                        Text("Abbrechen")
                    }
                }
            }
        }
    }
}

@Composable
private fun DeviceList(
    devices: List<Device>,
    openSwipeItemId: String?,
    onOpenSwipeItem: (String) -> Unit,
    onCloseSwipeItem: () -> Unit,
    onEditClick: (Device) -> Unit,
    onDeleteClick: (String) -> Unit
) {
    if (devices.isEmpty()) {
        EmptyListArea(text = "Keine Geräte konfiguriert.")
        return
    }

    val listState = rememberLazyListState()
    Column(
        modifier = Modifier
            .height(172.dp)
            .background(MaterialTheme.colorScheme.surface),
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
                .height(140.dp)
                .background(MaterialTheme.colorScheme.surface)
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
private fun EmptyListArea(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(172.dp)
            .background(MaterialTheme.colorScheme.surface),
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
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Gerät löschen",
                    style = MaterialTheme.typography.titleLarge
                )

                Text("Gerät ${device.name} wirklich löschen?")

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onConfirm
                    ) {
                        Text("Ja")
                    }

                    OutlinedButton(
                        onClick = onDismiss
                    ) {
                        Text("Nein")
                    }
                }
            }
        }
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
    onDeleteConnection: (String) -> Unit
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
        onDeleteConnection = onDeleteConnection
    )
}

@Composable
private fun DeviceConnectionList(
    wifiProfiles: List<WifiProfile>,
    connections: List<DeviceConnectionFormState>,
    onAddConnection: (String, String) -> Unit,
    onUpdateConnection: (String, String, String) -> Unit,
    onDeleteConnection: (String) -> Unit
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
            text = "WLAN-Zuordnungen",
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

    val listState = rememberLazyListState()

    Column(
        modifier = Modifier.height(172.dp),
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
                    contentDescription = "Weitere Zuordnungen oberhalb",
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        LazyColumn(
            state = listState,
            verticalArrangement = Arrangement.spacedBy(0.dp),
            modifier = Modifier.height(140.dp)
        ) {
            items(
                items = connections,
                key = { connection -> connection.wifiProfileId }
            ) { connection ->
                DeviceConnectionRow(
                    connection = connection,
                    onEditClick = {
                        editingConnection = connection
                    },
                    onDeleteClick = {
                        onDeleteConnection(connection.wifiProfileId)
                    }
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
                    contentDescription = "Weitere Zuordnungen unterhalb",
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun DeviceConnectionRow(
    connection: DeviceConnectionFormState,
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
                text = connection.ssid,
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = connection.host,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
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

    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 480.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
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
                                    onClick = {
                                        selectedWifiProfileId = profile.id
                                    }
                                )

                                Text(
                                    text = profile.ssid,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        OutlinedTextField(
                            value = host,
                            onValueChange = {
                                host = it
                            },
                            label = {
                                Text("Hostname/IP")
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            onSave(selectedWifiProfileId, host)
                        },
                        enabled = selectedWifiProfileId.isNotBlank() && host.isNotBlank()
                    ) {
                        Text("Speichern")
                    }

                    OutlinedButton(
                        onClick = onCancel
                    ) {
                        Text("Abbrechen")
                    }
                }
            }
        }
    }
}
