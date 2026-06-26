package de.piecha.switchwerk.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import de.piecha.switchwerk.R
import de.piecha.switchwerk.domain.model.ApiContentType
import de.piecha.switchwerk.domain.model.ApiMethod
import de.piecha.switchwerk.domain.model.Device
import de.piecha.switchwerk.domain.model.DeviceProtocol
import de.piecha.switchwerk.domain.model.WifiProfile
import de.piecha.switchwerk.ui.components.InfoHint
import de.piecha.switchwerk.ui.components.LazyListScrollIndicator
import de.piecha.switchwerk.ui.components.StandardConfigurationDialog
import de.piecha.switchwerk.ui.components.SwipeToDeleteListItem
import de.piecha.switchwerk.viewmodel.DeviceConnectionFormState
import de.piecha.switchwerk.viewmodel.DeviceFormState

private val technicalPathKeyboardOptions = KeyboardOptions(
    capitalization = KeyboardCapitalization.None,
    autoCorrectEnabled = false,
    keyboardType = KeyboardType.Uri,
    imeAction = ImeAction.Done
)

private val hostKeyboardOptions = KeyboardOptions(
    capitalization = KeyboardCapitalization.None,
    autoCorrectEnabled = false,
    keyboardType = KeyboardType.Uri,
    imeAction = ImeAction.Done
)

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
    onApiProtocolChange: (String) -> Unit,
    onApiMethodChange: (String) -> Unit,
    onApiPathChange: (String) -> Unit,
    onApiRequestBodyChange: (String) -> Unit,
    onApiContentTypeChange: (String) -> Unit,
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
            openSwipeItemId = openSwipeItemId,
            onOpenSwipeItem = onOpenSwipeItem,
            onCloseSwipeItem = onCloseSwipeItem,
            onNameChange = onNameChange,
            onActionLabelChange = onActionLabelChange,
            onApiProtocolChange = onApiProtocolChange,
            onApiMethodChange = onApiMethodChange,
            onApiPathChange = onApiPathChange,
            onApiRequestBodyChange = onApiRequestBodyChange,
            onApiContentTypeChange = onApiContentTypeChange,
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
                text = stringResource(R.string.name),
                style = MaterialTheme.typography.titleSmall
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                InfoHint(R.string.devices_info_title, R.string.list_interaction_info)
                IconButton(onClick = onAddClick, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = stringResource(R.string.add_device)
                    )
                }
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
    openSwipeItemId: String?,
    onOpenSwipeItem: (String) -> Unit,
    onCloseSwipeItem: () -> Unit,
    onNameChange: (String) -> Unit,
    onActionLabelChange: (String) -> Unit,
    onApiProtocolChange: (String) -> Unit,
    onApiMethodChange: (String) -> Unit,
    onApiPathChange: (String) -> Unit,
    onApiRequestBodyChange: (String) -> Unit,
    onApiContentTypeChange: (String) -> Unit,
    onAddConnection: (String, String) -> Unit,
    onUpdateConnection: (String, String, String) -> Unit,
    onDeleteConnection: (String) -> Unit,
    onMoveConnection: (String, Int) -> Unit,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    StandardConfigurationDialog(
        title = stringResource(if (form.id == null) R.string.add_device else R.string.edit_device),
        onDismissRequest = {
            onCloseSwipeItem()
            onCancelClick()
        },
        actionText = stringResource(R.string.save),
        onAction = {
            onCloseSwipeItem()
            onSaveClick()
        },
        infoTitleResourceId = R.string.device_dialog_info_title,
        infoMessageResourceId = R.string.device_dialog_info
    ) {
        DeviceForm(
            form = form,
            wifiProfiles = wifiProfiles,
            openSwipeItemId = openSwipeItemId,
            onOpenSwipeItem = onOpenSwipeItem,
            onCloseSwipeItem = onCloseSwipeItem,
            onNameChange = onNameChange,
            onActionLabelChange = onActionLabelChange,
            onApiProtocolChange = onApiProtocolChange,
            onApiMethodChange = onApiMethodChange,
            onApiPathChange = onApiPathChange,
            onApiRequestBodyChange = onApiRequestBodyChange,
            onApiContentTypeChange = onApiContentTypeChange,
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
        EmptyListArea(text = stringResource(R.string.no_devices_configured_period), modifier = modifier)
        return
    }

    val listState = rememberLazyListState()
    Box(modifier = modifier) {
        LazyColumn(
            state = listState,
            verticalArrangement = Arrangement.spacedBy(0.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(end = 8.dp)
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
                    onDeleteClick = { onDeleteClick(device.id) }
                )
            }
        }
        LazyListScrollIndicator(
            listState = listState,
            modifier = Modifier.align(Alignment.CenterEnd)
        )
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

    SwipeToDeleteListItem(
        isOpen = isOpen,
        isAnyItemOpen = isAnyItemOpen,
        onOpen = onOpen,
        onClose = onClose,
        onContentClick = onContentClick,
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
        title = stringResource(R.string.delete_device),
        onDismissRequest = onDismiss,
        actionText = stringResource(R.string.yes),
        onAction = onConfirm,
        cancelText = stringResource(R.string.no)
    ) {
        Text(stringResource(R.string.delete_device_confirmation, device.name))
    }
}

@Composable
private fun DeviceForm(
    form: DeviceFormState,
    wifiProfiles: List<WifiProfile>,
    openSwipeItemId: String?,
    onOpenSwipeItem: (String) -> Unit,
    onCloseSwipeItem: () -> Unit,
    onNameChange: (String) -> Unit,
    onActionLabelChange: (String) -> Unit,
    onApiProtocolChange: (String) -> Unit,
    onApiMethodChange: (String) -> Unit,
    onApiPathChange: (String) -> Unit,
    onApiRequestBodyChange: (String) -> Unit,
    onApiContentTypeChange: (String) -> Unit,
    onAddConnection: (String, String) -> Unit,
    onUpdateConnection: (String, String, String) -> Unit,
    onDeleteConnection: (String) -> Unit,
    onMoveConnection: (String, Int) -> Unit
) {
    val focusManager = LocalFocusManager.current
    val actionLabelFocusRequester = remember { FocusRequester() }
    val apiPathFocusRequester = remember { FocusRequester() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .closeSwipeOnTap(openSwipeItemId != null, onCloseSwipeItem),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = form.name,
            onValueChange = onNameChange,
            label = { Text(stringResource(R.string.name)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(
                onNext = { actionLabelFocusRequester.requestFocus() }
            ),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = form.actionLabel,
            onValueChange = onActionLabelChange,
            label = { Text(stringResource(R.string.button_label)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(
                onNext = { apiPathFocusRequester.requestFocus() }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(actionLabelFocusRequester)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DeviceProtocol.entries.forEach { protocol ->
                OutlinedButton(
                    onClick = {
                        onApiProtocolChange(protocol.name)
                    }
                ) {
                    Text(
                        text = if (form.apiProtocol == protocol.name) {
                            stringResource(R.string.selected_device_protocol, protocol.name)
                        } else {
                            protocol.name
                        }
                    )
                }
            }
        }

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
                        text = if (form.apiMethod == method.name) {
                            stringResource(R.string.selected_api_method, method.name)
                        } else {
                            method.name
                        }
                    )
                }
            }
        }

        OutlinedTextField(
            value = form.apiPath,
            onValueChange = onApiPathChange,
            label = { Text(stringResource(R.string.api_call)) },
            singleLine = true,
            keyboardOptions = technicalPathKeyboardOptions,
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(apiPathFocusRequester)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val isPostMethod = form.apiMethod == ApiMethod.POST.name
            ApiContentType.entries.forEach { contentType ->
                OutlinedButton(
                    onClick = {
                        onApiContentTypeChange(contentType.name)
                    },
                    enabled = isPostMethod,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = if (form.apiContentType == contentType.name) {
                            stringResource(R.string.selected_api_content_type, contentType.value)
                        } else {
                            contentType.value
                        }
                    )
                }
            }
        }

        OutlinedTextField(
            value = form.apiRequestBody,
            onValueChange = onApiRequestBodyChange,
            label = { Text(stringResource(R.string.api_request_body)) },
            minLines = 4,
            maxLines = 8,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.None,
                autoCorrectEnabled = false,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Default
            ),
            modifier = Modifier.fillMaxWidth()
        )

        DeviceConnectionList(
            wifiProfiles = wifiProfiles,
            connections = form.connections,
            openSwipeItemId = openSwipeItemId,
            onOpenSwipeItem = onOpenSwipeItem,
            onCloseSwipeItem = onCloseSwipeItem,
            onAddConnection = onAddConnection,
            onUpdateConnection = onUpdateConnection,
            onDeleteConnection = onDeleteConnection,
            onMoveConnection = onMoveConnection
        )
    }
}

@Composable
private fun DeviceConnectionList(
    wifiProfiles: List<WifiProfile>,
    connections: List<DeviceConnectionFormState>,
    openSwipeItemId: String?,
    onOpenSwipeItem: (String) -> Unit,
    onCloseSwipeItem: () -> Unit,
    onAddConnection: (String, String) -> Unit,
    onUpdateConnection: (String, String, String) -> Unit,
    onDeleteConnection: (String) -> Unit,
    onMoveConnection: (String, Int) -> Unit
) {
    var isAddingConnection by remember { mutableStateOf(false) }
    var editingConnection by remember { mutableStateOf<DeviceConnectionFormState?>(null) }
    var pendingDeleteConnection by remember { mutableStateOf<DeviceConnectionFormState?>(null) }

    if (isAddingConnection) {
        ConnectionEditDialog(
            title = stringResource(R.string.add_wifi_assignment),
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
            title = stringResource(R.string.edit_wifi_assignment),
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

    pendingDeleteConnection?.let { connection ->
        StandardConfigurationDialog(
            title = stringResource(R.string.delete_wifi_assignment),
            onDismissRequest = { pendingDeleteConnection = null },
            actionText = stringResource(R.string.yes),
            onAction = {
                pendingDeleteConnection = null
                onDeleteConnection(connection.wifiProfileId)
            },
            cancelText = stringResource(R.string.no)
        ) {
            Text(
                stringResource(
                    R.string.delete_wifi_assignment_confirmation,
                    connection.wifiProfileName
                )
            )
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.wifi_connection_order),
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
                contentDescription = stringResource(R.string.add_wifi_assignment)
            )
        }
    }

    if (connections.isEmpty()) {
        Text(
            text = stringResource(R.string.no_wifi_assignment),
            style = MaterialTheme.typography.bodySmall
        )
        return
    }

    Text(
        text = stringResource(R.string.wifi_order_hint),
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
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    enabled = openSwipeItemId != null,
                    onClick = onCloseSwipeItem
                )
        ) {
            itemsIndexed(
                items = connections,
                key = { _, connection -> connection.wifiProfileId }
            ) { index, connection ->
                val swipeItemId = "connection:${connection.wifiProfileId}"
                DeviceConnectionRow(
                    connection = connection,
                    isOpen = openSwipeItemId == swipeItemId,
                    isAnyItemOpen = openSwipeItemId != null,
                    onOpen = {
                        onOpenSwipeItem(swipeItemId)
                    },
                    onClose = onCloseSwipeItem,
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
                        pendingDeleteConnection = connection
                    }
                )
            }
        }

        if (listState.canScrollBackward) {
            Icon(
                imageVector = Icons.Filled.KeyboardArrowUp,
                contentDescription = stringResource(R.string.more_wifi_assignments_above),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .size(16.dp)
            )
        }

        if (listState.canScrollForward) {
            Icon(
                imageVector = Icons.Filled.KeyboardArrowDown,
                contentDescription = stringResource(R.string.more_wifi_assignments_below),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .size(16.dp)
            )
        }
    }
}

private fun Modifier.closeSwipeOnTap(
    enabled: Boolean,
    onCloseSwipeItem: () -> Unit
): Modifier = pointerInput(enabled, onCloseSwipeItem) {
    if (!enabled) return@pointerInput

    awaitEachGesture {
        val down = awaitFirstDown(
            requireUnconsumed = false,
            pass = PointerEventPass.Final
        )
        var movedBeyondTap = false
        var isPressed = true

        while (isPressed) {
            val event = awaitPointerEvent(PointerEventPass.Final)
            val change = event.changes.firstOrNull { it.id == down.id }
            if (change != null) {
                movedBeyondTap = movedBeyondTap ||
                    (change.position - down.position).getDistance() > viewConfiguration.touchSlop
                isPressed = change.pressed
            } else {
                isPressed = false
            }
        }

        if (!movedBeyondTap) {
            onCloseSwipeItem()
        }
    }
}

@Composable
private fun DeviceConnectionRow(
    connection: DeviceConnectionFormState,
    isOpen: Boolean,
    isAnyItemOpen: Boolean,
    onOpen: () -> Unit,
    onClose: () -> Unit,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onMoveUpClick: () -> Unit,
    onMoveDownClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    SwipeToDeleteListItem(
        isOpen = isOpen,
        isAnyItemOpen = isAnyItemOpen,
        onOpen = onOpen,
        onClose = onClose,
        onContentClick = onEditClick,
        onDeleteClick = onDeleteClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
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
                    text = stringResource(R.string.ssid_host_value, connection.ssid, connection.host),
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
                        contentDescription = stringResource(R.string.move_wifi_assignment_up)
                    )
                }

                IconButton(
                    onClick = onMoveDownClick,
                    enabled = canMoveDown,
                    modifier = Modifier.size(26.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = stringResource(R.string.move_wifi_assignment_down)
                    )
                }
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
    val focusManager = LocalFocusManager.current
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
        actionText = stringResource(R.string.save),
        onAction = { onSave(selectedWifiProfileId, host) },
        actionEnabled = selectedWifiProfileId.isNotBlank() && host.isNotBlank(),
        infoTitleResourceId = R.string.wifi_assignment_dialog_info_title,
        infoMessageResourceId = R.string.wifi_assignment_dialog_info
    ) {
        if (selectableProfiles.isEmpty()) {
            Text(stringResource(R.string.no_more_wifi_profiles))
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
                            text = stringResource(R.string.ssid_value, profile.ssid),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            OutlinedTextField(
                value = host,
                onValueChange = { host = it },
                label = { Text(stringResource(R.string.hostname_ip)) },
                singleLine = true,
                keyboardOptions = hostKeyboardOptions,
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
