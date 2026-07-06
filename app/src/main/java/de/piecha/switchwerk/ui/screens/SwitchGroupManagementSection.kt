package de.piecha.switchwerk.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
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
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import de.piecha.switchwerk.R
import de.piecha.switchwerk.domain.model.Device
import de.piecha.switchwerk.domain.model.DeviceColor
import de.piecha.switchwerk.domain.model.SwitchGroup
import de.piecha.switchwerk.domain.model.SwitchGroupErrorStrategy
import de.piecha.switchwerk.ui.components.InfoHint
import de.piecha.switchwerk.ui.components.DeviceColorPicker
import de.piecha.switchwerk.ui.components.contentColor
import de.piecha.switchwerk.ui.components.toComposeColor
import de.piecha.switchwerk.ui.components.LazyListScrollIndicator
import de.piecha.switchwerk.ui.components.StandardConfigurationDialog
import de.piecha.switchwerk.ui.components.SwipeToDeleteListItem
import de.piecha.switchwerk.viewmodel.SwitchGroupFormState
import de.piecha.switchwerk.viewmodel.SwitchGroupMemberFormState
import java.util.Locale

@Composable
fun SwitchGroupManagementSection(
    groups: List<SwitchGroup>,
    devices: List<Device>,
    isEditing: Boolean,
    form: SwitchGroupFormState,
    openSwipeItemId: String?,
    onOpenSwipeItem: (String) -> Unit,
    onCloseSwipeItem: () -> Unit,
    onAddClick: () -> Unit,
    onEditClick: (SwitchGroup) -> Unit,
    onDeleteClick: (String) -> Unit,
    onNameChange: (String) -> Unit,
    onActionLabelChange: (String) -> Unit,
    onErrorStrategyChange: (SwitchGroupErrorStrategy) -> Unit,
    onShortcutEnabledChange: (Boolean) -> Unit,
    onColorChange: (DeviceColor) -> Unit,
    onAddMember: (String) -> Unit,
    onDeleteMember: (String) -> Unit,
    onMoveMember: (String, Int) -> Unit,
    onMemberPauseChange: (String, Long) -> Unit,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isEditing) {
        SwitchGroupEditDialog(
            form = form,
            devices = devices,
            onNameChange = onNameChange,
            onActionLabelChange = onActionLabelChange,
            onErrorStrategyChange = onErrorStrategyChange,
            onShortcutEnabledChange = onShortcutEnabledChange,
            onColorChange = onColorChange,
            onAddMember = onAddMember,
            onDeleteMember = onDeleteMember,
            onMoveMember = onMoveMember,
            onMemberPauseChange = onMemberPauseChange,
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
                text = stringResource(R.string.switch_groups),
                style = MaterialTheme.typography.titleSmall
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                InfoHint(R.string.groups_info_title, R.string.groups_info)
                IconButton(onClick = onAddClick, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = stringResource(R.string.add_group)
                    )
                }
            }
        }

        SwitchGroupList(
            groups = groups,
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
private fun SwitchGroupEditDialog(
    form: SwitchGroupFormState,
    devices: List<Device>,
    onNameChange: (String) -> Unit,
    onActionLabelChange: (String) -> Unit,
    onErrorStrategyChange: (SwitchGroupErrorStrategy) -> Unit,
    onShortcutEnabledChange: (Boolean) -> Unit,
    onColorChange: (DeviceColor) -> Unit,
    onAddMember: (String) -> Unit,
    onDeleteMember: (String) -> Unit,
    onMoveMember: (String, Int) -> Unit,
    onMemberPauseChange: (String, Long) -> Unit,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    StandardConfigurationDialog(
        title = stringResource(if (form.id == null) R.string.add_group else R.string.edit_group),
        onDismissRequest = onCancelClick,
        actionText = stringResource(R.string.save),
        onAction = onSaveClick,
        infoTitleResourceId = R.string.group_dialog_info_title,
        infoMessageResourceId = R.string.group_dialog_info
    ) {
        SwitchGroupForm(
            form = form,
            devices = devices,
            onNameChange = onNameChange,
            onActionLabelChange = onActionLabelChange,
            onErrorStrategyChange = onErrorStrategyChange,
            onShortcutEnabledChange = onShortcutEnabledChange,
            onColorChange = onColorChange,
            onAddMember = onAddMember,
            onDeleteMember = onDeleteMember,
            onMoveMember = onMoveMember,
            onMemberPauseChange = onMemberPauseChange
        )
    }
}

@Composable
private fun SwitchGroupList(
    groups: List<SwitchGroup>,
    openSwipeItemId: String?,
    onOpenSwipeItem: (String) -> Unit,
    onCloseSwipeItem: () -> Unit,
    onEditClick: (SwitchGroup) -> Unit,
    onDeleteClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (groups.isEmpty()) {
        Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
            Text(
                text = stringResource(R.string.no_groups_configured),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        return
    }

    val listState = rememberLazyListState()
    Box(modifier = modifier) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(end = 8.dp)
                .clickable(enabled = openSwipeItemId != null, onClick = onCloseSwipeItem)
        ) {
            items(items = groups, key = { it.id }) { group ->
                val swipeItemId = "group:${group.id}"
                SwitchGroupRow(
                    group = group,
                    isOpen = openSwipeItemId == swipeItemId,
                    isAnyItemOpen = openSwipeItemId != null,
                    onOpen = { onOpenSwipeItem(swipeItemId) },
                    onClose = onCloseSwipeItem,
                    onContentClick = {
                        if (openSwipeItemId == null) onEditClick(group) else onCloseSwipeItem()
                    },
                    onDeleteClick = { onDeleteClick(group.id) }
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
private fun SwitchGroupRow(
    group: SwitchGroup,
    isOpen: Boolean,
    isAnyItemOpen: Boolean,
    onOpen: () -> Unit,
    onClose: () -> Unit,
    onContentClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var pendingDeleteGroup by remember { mutableStateOf<SwitchGroup?>(null) }

    pendingDeleteGroup?.let { groupToDelete ->
        StandardConfigurationDialog(
            title = stringResource(R.string.delete_group),
            onDismissRequest = { pendingDeleteGroup = null },
            actionText = stringResource(R.string.yes),
            onAction = {
                pendingDeleteGroup = null
                onDeleteClick()
            },
            cancelText = stringResource(R.string.no)
        ) {
            Text(stringResource(R.string.delete_group_confirmation, groupToDelete.name))
        }
    }

    SwipeToDeleteListItem(
        isOpen = isOpen,
        isAnyItemOpen = isAnyItemOpen,
        onOpen = onOpen,
        onClose = onClose,
        onContentClick = onContentClick,
        onDeleteClick = { pendingDeleteGroup = group }
    ) {
        val containerColor = group.color.toComposeColor()
        val contentColor = if (containerColor == null) {
            LocalContentColor.current
        } else {
            group.color.contentColor()
        }
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (containerColor != null) Modifier.background(containerColor) else Modifier)
                    .padding(vertical = 8.dp, horizontal = 8.dp)
            ) {
                Text(text = group.name, style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = stringResource(R.string.group_member_count, group.members.size),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun SwitchGroupForm(
    form: SwitchGroupFormState,
    devices: List<Device>,
    onNameChange: (String) -> Unit,
    onActionLabelChange: (String) -> Unit,
    onErrorStrategyChange: (SwitchGroupErrorStrategy) -> Unit,
    onShortcutEnabledChange: (Boolean) -> Unit,
    onColorChange: (DeviceColor) -> Unit,
    onAddMember: (String) -> Unit,
    onDeleteMember: (String) -> Unit,
    onMoveMember: (String, Int) -> Unit,
    onMemberPauseChange: (String, Long) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = form.name,
            onValueChange = onNameChange,
            label = { Text(stringResource(R.string.name)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = stringResource(R.string.group_color),
            style = MaterialTheme.typography.labelLarge
        )
        DeviceColorPicker(
            selectedColor = form.color,
            onColorChange = onColorChange
        )
        OutlinedTextField(
            value = form.actionLabel,
            onValueChange = onActionLabelChange,
            label = { Text(stringResource(R.string.button_label)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth()
        )

        SwitchGroupErrorStrategySwitch(
            errorStrategy = form.errorStrategy,
            onErrorStrategyChange = onErrorStrategyChange
        )

        SwitchGroupShortcutSwitch(
            enabled = form.shortcutEnabled,
            onEnabledChange = onShortcutEnabledChange
        )

        SwitchGroupMemberList(
            devices = devices,
            members = form.members,
            onAddMember = onAddMember,
            onDeleteMember = onDeleteMember,
            onMoveMember = onMoveMember,
            onMemberPauseChange = onMemberPauseChange
        )
    }
}

@Composable
private fun SwitchGroupErrorStrategySwitch(
    errorStrategy: SwitchGroupErrorStrategy,
    onErrorStrategyChange: (SwitchGroupErrorStrategy) -> Unit
) {
    val continueAfterErrors = errorStrategy == SwitchGroupErrorStrategy.CONTINUE_ON_ERROR
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onErrorStrategyChange(
                    if (continueAfterErrors) {
                        SwitchGroupErrorStrategy.ABORT_ON_ERROR
                    } else {
                        SwitchGroupErrorStrategy.CONTINUE_ON_ERROR
                    }
                )
            },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.group_error_strategy),
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                text = if (continueAfterErrors) {
                    stringResource(R.string.group_error_strategy_continue)
                } else {
                    stringResource(R.string.group_error_strategy_abort)
                },
                style = MaterialTheme.typography.bodySmall
            )
        }
        Switch(
            checked = continueAfterErrors,
            onCheckedChange = { checked ->
                onErrorStrategyChange(
                    if (checked) {
                        SwitchGroupErrorStrategy.CONTINUE_ON_ERROR
                    } else {
                        SwitchGroupErrorStrategy.ABORT_ON_ERROR
                    }
                )
            }
        )
    }
}

@Composable
private fun SwitchGroupShortcutSwitch(
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEnabledChange(!enabled) },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.app_shortcut),
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                text = stringResource(R.string.group_app_shortcut_description),
                style = MaterialTheme.typography.bodySmall
            )
        }
        Switch(
            checked = enabled,
            onCheckedChange = onEnabledChange
        )
    }
}

@Composable
private fun SwitchGroupMemberList(
    devices: List<Device>,
    members: List<SwitchGroupMemberFormState>,
    onAddMember: (String) -> Unit,
    onDeleteMember: (String) -> Unit,
    onMoveMember: (String, Int) -> Unit,
    onMemberPauseChange: (String, Long) -> Unit
) {
    var isAddingMember by remember { mutableStateOf(false) }
    var openMemberSwipeId by remember { mutableStateOf<String?>(null) }
    var pendingDeleteMember by remember { mutableStateOf<SwitchGroupMemberFormState?>(null) }
    val selectableDevices = devices

    if (isAddingMember) {
        SwitchGroupMemberDialog(
            devices = selectableDevices,
            onSave = { deviceId ->
                onAddMember(deviceId)
                isAddingMember = false
            },
            onCancel = { isAddingMember = false }
        )
    }

    pendingDeleteMember?.let { member ->
        StandardConfigurationDialog(
            title = stringResource(R.string.delete_group_member),
            onDismissRequest = { pendingDeleteMember = null },
            actionText = stringResource(R.string.yes),
            onAction = {
                pendingDeleteMember = null
                openMemberSwipeId = null
                onDeleteMember(member.id)
            },
            cancelText = stringResource(R.string.no)
        ) {
            Text(stringResource(R.string.delete_group_member_confirmation, member.deviceName))
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.group_members),
            style = MaterialTheme.typography.titleSmall
        )
        IconButton(
            onClick = { isAddingMember = true },
            enabled = selectableDevices.isNotEmpty(),
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = stringResource(R.string.add_group_member)
            )
        }
    }
    if (members.isEmpty()) {
        Text(
            text = stringResource(R.string.no_group_members),
            style = MaterialTheme.typography.bodySmall
        )
        return
    }
    Text(
        text = stringResource(R.string.group_order_hint),
        style = MaterialTheme.typography.bodySmall
    )

    val listState = rememberLazyListState()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(360.dp)
    ) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(vertical = 8.dp),
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    enabled = openMemberSwipeId != null,
                    onClick = { openMemberSwipeId = null }
                )
        ) {
            itemsIndexed(items = members, key = { _, member -> member.id }) { index, member ->
                val swipeItemId = "group-member:${member.id}"
                SwitchGroupMemberRow(
                    member = member,
                    isOpen = openMemberSwipeId == swipeItemId,
                    isAnyItemOpen = openMemberSwipeId != null,
                    onOpen = { openMemberSwipeId = swipeItemId },
                    onClose = { openMemberSwipeId = null },
                    canMoveUp = index > 0,
                    canMoveDown = index < members.lastIndex,
                    onMoveUpClick = { onMoveMember(member.id, index - 1) },
                    onMoveDownClick = { onMoveMember(member.id, index + 1) },
                    onDeleteClick = { pendingDeleteMember = member },
                    onPauseChange = { pause -> onMemberPauseChange(member.id, pause) }
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
private fun SwitchGroupMemberRow(
    member: SwitchGroupMemberFormState,
    isOpen: Boolean,
    isAnyItemOpen: Boolean,
    onOpen: () -> Unit,
    onClose: () -> Unit,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onMoveUpClick: () -> Unit,
    onMoveDownClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onPauseChange: (Long) -> Unit
) {
    var isCustomPauseDialogOpen by remember { mutableStateOf(false) }

    if (isCustomPauseDialogOpen) {
        CustomPauseDialog(
            initialPauseMillis = member.pauseAfterMillis,
            onSave = { pauseMillis ->
                onPauseChange(pauseMillis)
                isCustomPauseDialogOpen = false
            },
            onCancel = { isCustomPauseDialogOpen = false }
        )
    }

    SwipeToDeleteListItem(
        isOpen = isOpen,
        isAnyItemOpen = isAnyItemOpen,
        onOpen = onOpen,
        onClose = onClose,
        onContentClick = onClose,
        onDeleteClick = onDeleteClick
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = member.deviceName, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = stringResource(
                            R.string.group_pause_after,
                            pauseLabel(member.pauseAfterMillis)
                        ),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                IconButton(onClick = onMoveUpClick, enabled = canMoveUp, modifier = Modifier.size(30.dp)) {
                    Icon(Icons.Filled.KeyboardArrowUp, contentDescription = stringResource(R.string.move_group_member_up))
                }
                IconButton(onClick = onMoveDownClick, enabled = canMoveDown, modifier = Modifier.size(30.dp)) {
                    Icon(Icons.Filled.KeyboardArrowDown, contentDescription = stringResource(R.string.move_group_member_down))
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                GroupPauseOption.entries.forEach { option ->
                    OutlinedButton(
                        onClick = { onPauseChange(option.millis) },
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (member.pauseAfterMillis == option.millis) {
                                stringResource(R.string.selected_pause, option.label())
                            } else {
                                option.label()
                            },
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                OutlinedButton(
                    onClick = { isCustomPauseDialogOpen = true },
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = stringResource(R.string.pause_custom),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun SwitchGroupMemberDialog(
    devices: List<Device>,
    onSave: (String) -> Unit,
    onCancel: () -> Unit
) {
    var selectedDeviceId by remember(devices) { mutableStateOf(devices.firstOrNull()?.id.orEmpty()) }
    StandardConfigurationDialog(
        title = stringResource(R.string.add_group_member),
        onDismissRequest = onCancel,
        actionText = stringResource(R.string.save),
        onAction = { onSave(selectedDeviceId) },
        actionEnabled = selectedDeviceId.isNotBlank()
    ) {
        if (devices.isEmpty()) {
            Text(stringResource(R.string.no_devices_for_group))
        } else {
            devices.forEach { device ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedDeviceId = device.id },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedDeviceId == device.id,
                        onClick = { selectedDeviceId = device.id }
                    )
                    Text(device.name)
                }
            }
        }
    }
}

private enum class GroupPauseOption(val millis: Long) {
    NONE(0),
    MEDIUM(500),
    ONE_SECOND(1_000),
    TWO_SECONDS(2_000);

    @Composable
    fun label(): String = pauseLabel(millis)
}

@Composable
private fun pauseLabel(millis: Long): String {
    return when (millis) {
        0L -> stringResource(R.string.pause_0_ms)
        500L -> stringResource(R.string.pause_500_ms)
        1_000L -> stringResource(R.string.pause_1_s)
        2_000L -> stringResource(R.string.pause_2_s)
        else -> formatPauseDuration(millis)
    }
}

private fun formatPauseDuration(millis: Long): String {
    val hours = millis / 3_600_000
    val minutes = (millis % 3_600_000) / 60_000
    val seconds = (millis % 60_000) / 1_000
    val milliseconds = millis % 1_000
    return String.format(
        Locale.ROOT,
        "%02d:%02d:%02d.%03d",
        hours,
        minutes,
        seconds,
        milliseconds
    )
}

@Composable
private fun CustomPauseDialog(
    initialPauseMillis: Long,
    onSave: (Long) -> Unit,
    onCancel: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    var value by remember(initialPauseMillis) { mutableStateOf(formatPauseDuration(initialPauseMillis)) }
    val pauseMillis = parsePauseDuration(value)
    val isValid = pauseMillis != null && pauseMillis in 0L..MAX_CUSTOM_PAUSE_MILLIS

    StandardConfigurationDialog(
        title = stringResource(R.string.pause_custom_title),
        onDismissRequest = onCancel,
        actionText = stringResource(R.string.save),
        onAction = { pauseMillis?.let(onSave) },
        actionEnabled = isValid
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = { input ->
                value = input.filter { it.isDigit() || it == ':' || it == '.' }.take(12)
            },
            label = { Text(stringResource(R.string.pause_custom_label)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = stringResource(R.string.pause_custom_hint),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

private fun parsePauseDuration(value: String): Long? {
    val match = PAUSE_DURATION_PATTERN.matchEntire(value) ?: return null
    val hours = match.groupValues[1].toLong()
    val minutes = match.groupValues[2].toLong()
    val seconds = match.groupValues[3].toLong()
    val milliseconds = match.groupValues[4].toLong()
    if (minutes > 59 || seconds > 59) return null
    return hours * 3_600_000 + minutes * 60_000 + seconds * 1_000 + milliseconds
}

private const val MAX_CUSTOM_PAUSE_MILLIS = 3_600_000L
private val PAUSE_DURATION_PATTERN = Regex("""(\d{2}):(\d{2}):(\d{2})\.(\d{3})""")
