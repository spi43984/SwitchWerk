package de.piecha.switchwerk.ui.screens

import android.content.res.Configuration
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import de.piecha.switchwerk.domain.model.DashboardLayoutMode
import de.piecha.switchwerk.domain.model.Device
import de.piecha.switchwerk.ui.components.AppMenuLayout
import de.piecha.switchwerk.ui.components.AppOverflowMenu
import de.piecha.switchwerk.ui.components.InfoHint
import de.piecha.switchwerk.viewmodel.DeviceActionUiState
import de.piecha.switchwerk.viewmodel.DeviceWifiProximityStatus
import de.piecha.switchwerk.viewmodel.DiagnosticListItem
import de.piecha.switchwerk.viewmodel.MainViewModel
import de.piecha.switchwerk.R
import de.piecha.switchwerk.ui.asString
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun StartScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToHelp: () -> Unit,
    onNavigateToAbout: () -> Unit,
    viewModel: MainViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val devices = uiState.devices.sortedBy { it.sortOrder }
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val screenPadding = if (isLandscape) 12.dp else 24.dp
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> viewModel.startWifiProximityMonitoring()
                Lifecycle.Event.ON_PAUSE -> viewModel.stopWifiProximityMonitoring()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            viewModel.startWifiProximityMonitoring()
        }
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.stopWifiProximityMonitoring()
        }
    }

    AppMenuLayout(
        onOpenSettings = onNavigateToSettings,
        onOpenHelp = onNavigateToHelp,
        onOpenAbout = onNavigateToAbout,
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(screenPadding),
        rightEdgeExtension = screenPadding
    ) { openMenu ->
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            DashboardHeader(
                deviceCount = devices.size,
                selectedMode = uiState.appSettings.dashboardLayoutMode,
                isLandscape = isLandscape,
                onModeSelected = viewModel::setDashboardLayoutMode,
                onOpenMenu = openMenu
            )

            uiState.errorMessage?.let { message ->
                Text(
                    text = message.asString(),
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(if (isLandscape) 6.dp else 12.dp))

            Column(modifier = Modifier.weight(1f)) {
                val detailHeight = uiState.appSettings.detailPanelHeight.fraction
                val showActionDetails = uiState.appSettings.showActionDetails && !isLandscape
                val deviceAreaWeight = if (showActionDetails) {
                    1f - detailHeight
                } else {
                    1f
                }

                if (devices.isEmpty()) {
                    EmptyDeviceList(modifier = Modifier.weight(deviceAreaWeight))
                } else {
                    when (uiState.appSettings.dashboardLayoutMode) {
                        DashboardLayoutMode.LIST -> DeviceList(
                            devices = devices,
                            actionStates = uiState.deviceActionStates,
                            wifiProximityStatuses = uiState.wifiProximityStatuses,
                            onDeviceActionClick = viewModel::executeDeviceAction,
                            onMoveUpClick = viewModel::moveDeviceUp,
                            onMoveDownClick = viewModel::moveDeviceDown,
                            modifier = Modifier.weight(deviceAreaWeight)
                        )

                        DashboardLayoutMode.WIDGETS -> DeviceWidgetGrid(
                            devices = devices,
                            actionStates = uiState.deviceActionStates,
                            wifiProximityStatuses = uiState.wifiProximityStatuses,
                            onDeviceActionClick = viewModel::executeDeviceAction,
                            onMoveUpClick = viewModel::moveDeviceUp,
                            onMoveDownClick = viewModel::moveDeviceDown,
                            modifier = Modifier.weight(deviceAreaWeight)
                        )
                    }
                }

                if (showActionDetails) {
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
private fun DashboardHeader(
    deviceCount: Int,
    selectedMode: DashboardLayoutMode,
    isLandscape: Boolean,
    onModeSelected: (DashboardLayoutMode) -> Unit,
    onOpenMenu: () -> Unit
) {
    if (isLandscape) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = pluralStringResource(R.plurals.devices_found, deviceCount, deviceCount),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1
            )
            DashboardLayoutSelector(
                selectedMode = selectedMode,
                onModeSelected = onModeSelected
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                InfoHint(R.string.dashboard_info_title, R.string.dashboard_info)
                AppOverflowMenu(onClick = onOpenMenu)
            }
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineLarge
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                InfoHint(R.string.dashboard_info_title, R.string.dashboard_info)
                AppOverflowMenu(onClick = onOpenMenu)
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = pluralStringResource(R.plurals.devices_found, deviceCount, deviceCount),
                style = MaterialTheme.typography.bodyLarge
            )
            DashboardLayoutSelector(
                selectedMode = selectedMode,
                onModeSelected = onModeSelected
            )
        }
    }
}

@Composable
private fun DashboardLayoutSelector(
    selectedMode: DashboardLayoutMode,
    onModeSelected: (DashboardLayoutMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedMode == DashboardLayoutMode.LIST,
            onClick = { onModeSelected(DashboardLayoutMode.LIST) },
            label = { Text(stringResource(R.string.layout_list)) }
        )
        FilterChip(
            selected = selectedMode == DashboardLayoutMode.WIDGETS,
            onClick = { onModeSelected(DashboardLayoutMode.WIDGETS) },
            label = { Text(stringResource(R.string.layout_widgets)) }
        )
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
            text = stringResource(R.string.no_devices_configured),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun DeviceList(
    devices: List<Device>,
    actionStates: Map<String, DeviceActionUiState>,
    wifiProximityStatuses: Map<String, DeviceWifiProximityStatus>,
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
                wifiProximityStatus = wifiProximityStatuses[device.id]
                    ?: DeviceWifiProximityStatus.UNKNOWN,
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
private fun DeviceWidgetGrid(
    devices: List<Device>,
    actionStates: Map<String, DeviceActionUiState>,
    wifiProximityStatuses: Map<String, DeviceWifiProximityStatus>,
    onDeviceActionClick: (Device) -> Unit,
    onMoveUpClick: (String) -> Unit,
    onMoveDownClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 140.dp),
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = devices,
            key = { device -> device.id }
        ) { device ->
            DeviceWidget(
                device = device,
                actionState = actionStates[device.id],
                wifiProximityStatus = wifiProximityStatuses[device.id]
                    ?: DeviceWifiProximityStatus.UNKNOWN,
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
private fun DeviceWidget(
    device: Device,
    actionState: DeviceActionUiState?,
    wifiProximityStatus: DeviceWifiProximityStatus,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onActionClick: () -> Unit,
    onMoveUpClick: () -> Unit,
    onMoveDownClick: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(
                start = 12.dp,
                top = 10.dp,
                end = 12.dp,
                bottom = 0.dp
            )
        ) {
            DeviceTitle(
                name = device.name,
                wifiProximityStatus = wifiProximityStatus,
                isActionRunning = actionState == DeviceActionUiState.Loading,
                maxLines = 2,
                modifier = Modifier.height(48.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            DeviceActionArea(
                device = device,
                actionState = actionState,
                canMoveUp = canMoveUp,
                canMoveDown = canMoveDown,
                onActionClick = onActionClick,
                onMoveUpClick = onMoveUpClick,
                onMoveDownClick = onMoveDownClick
            )
        }
    }
}

@Composable
private fun DeviceActionFooter(
    actionState: DeviceActionUiState?,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onMoveUpClick: () -> Unit,
    onMoveDownClick: () -> Unit
) {
    val showSortButtons = actionState == null || actionState is DeviceActionUiState.Success

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (actionState == null) {
            Spacer(modifier = Modifier.weight(1f))
        } else if (actionState is DeviceActionUiState.Success) {
            DeviceActionStatus(
                actionState = actionState,
                modifier = Modifier.weight(1f)
            )
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }
        if (showSortButtons) {
            Row {
                IconButton(
                    onClick = onMoveUpClick,
                    enabled = canMoveUp,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowUp,
                        contentDescription = stringResource(R.string.move_device_forward)
                    )
                }
                IconButton(
                    onClick = onMoveDownClick,
                    enabled = canMoveDown,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = stringResource(R.string.move_device_backward)
                    )
                }
            }
        }
    }
}

@Composable
private fun DeviceActionArea(
    device: Device,
    actionState: DeviceActionUiState?,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onActionClick: () -> Unit,
    onMoveUpClick: () -> Unit,
    onMoveDownClick: () -> Unit
) {
    if (actionState is DeviceActionUiState.Error) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(84.dp)
                .padding(bottom = 4.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = actionState.message.asString(),
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    } else {
        Column(modifier = Modifier.height(84.dp)) {
            DeviceActionButton(
                device = device,
                actionState = actionState,
                onActionClick = onActionClick
            )
            Spacer(modifier = Modifier.height(4.dp))
            DeviceActionFooter(
                actionState = actionState,
                canMoveUp = canMoveUp,
                canMoveDown = canMoveDown,
                onMoveUpClick = onMoveUpClick,
                onMoveDownClick = onMoveDownClick
            )
        }
    }
}

@Composable
private fun DeviceActionButton(
    device: Device,
    actionState: DeviceActionUiState?,
    onActionClick: () -> Unit
) {
    val label = when (actionState) {
        DeviceActionUiState.Loading -> stringResource(R.string.switching)
        is DeviceActionUiState.Error -> actionState.message.asString()
        is DeviceActionUiState.Success,
        null -> device.actionLabel
    }
    Button(
        onClick = onActionClick,
        enabled = actionState !is DeviceActionUiState.Loading,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        Text(
            text = label,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun DeviceActionStatus(
    actionState: DeviceActionUiState,
    modifier: Modifier = Modifier
) {
    val text = when (actionState) {
        DeviceActionUiState.Loading -> stringResource(R.string.executing)
        is DeviceActionUiState.Success -> actionState.message.asString()
        is DeviceActionUiState.Error -> actionState.message.asString()
    }
    val color = when (actionState) {
        is DeviceActionUiState.Success -> MaterialTheme.colorScheme.primary
        is DeviceActionUiState.Error -> MaterialTheme.colorScheme.error
        DeviceActionUiState.Loading -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Text(
        text = text,
        color = color,
        style = MaterialTheme.typography.bodySmall,
        modifier = modifier,
        minLines = 1,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
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
                    text = stringResource(R.string.action_details),
                    style = MaterialTheme.typography.titleSmall
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(onClick = onToggleSortOrder) {
                        Text(
                            stringResource(
                                if (newestFirst) R.string.newest_first else R.string.newest_last
                            )
                        )
                    }
                    IconButton(
                        onClick = onClear,
                        enabled = items.isNotEmpty()
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = stringResource(R.string.clear_action_log)
                        )
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
                            text = stringResource(R.string.no_device_action_yet),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                } else {
                    items(displayedItems) { item ->
                        when (item) {
                            is DiagnosticListItem.Message -> Text(
                                text = item.text.asString(),
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
    wifiProximityStatus: DeviceWifiProximityStatus,
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
            modifier = Modifier.padding(
                start = 16.dp,
                top = 12.dp,
                end = 16.dp,
                bottom = 0.dp
            )
        ) {
            DeviceTitle(
                name = device.name,
                wifiProximityStatus = wifiProximityStatus,
                isActionRunning = actionState == DeviceActionUiState.Loading,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(12.dp))

            DeviceActionArea(
                device = device,
                actionState = actionState,
                canMoveUp = canMoveUp,
                canMoveDown = canMoveDown,
                onActionClick = onActionClick,
                onMoveUpClick = onMoveUpClick,
                onMoveDownClick = onMoveDownClick
            )
        }
    }
}

@Composable
internal fun DeviceTitle(
    name: String,
    wifiProximityStatus: DeviceWifiProximityStatus,
    isActionRunning: Boolean,
    maxLines: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.titleMedium,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        WifiProximityIndicator(
            status = wifiProximityStatus,
            isActionRunning = isActionRunning,
            modifier = Modifier.padding(top = 5.dp)
        )
    }
}

@Composable
private fun WifiProximityIndicator(
    status: DeviceWifiProximityStatus,
    isActionRunning: Boolean,
    modifier: Modifier = Modifier
) {
    val statusDescription = deviceWifiProximityText(status)
    val description = if (isActionRunning) {
        stringResource(R.string.wifi_proximity_action_running, statusDescription)
    } else {
        statusDescription
    }
    val alpha = if (isActionRunning) {
        val transition = rememberInfiniteTransition(label = "wifi proximity pulse")
        transition.animateFloat(
            initialValue = 0.35f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 650),
                repeatMode = RepeatMode.Reverse
            ),
            label = "wifi proximity alpha"
        ).value
    } else {
        1f
    }
    val color = when (status) {
        DeviceWifiProximityStatus.NEARBY -> WifiNearbyColor
        DeviceWifiProximityStatus.UNKNOWN,
        DeviceWifiProximityStatus.NO_ASSIGNMENT,
        DeviceWifiProximityStatus.LOCATION_SERVICES_DISABLED -> WifiUnavailableColor
        else -> WifiNotNearbyColor
    }

    Box(
        modifier = modifier
            .size(14.dp)
            .alpha(alpha)
            .background(color = color, shape = CircleShape)
            .clearAndSetSemantics { contentDescription = description }
    )
}

@Composable
private fun deviceWifiProximityText(status: DeviceWifiProximityStatus): String {
    val resourceId = when (status) {
        DeviceWifiProximityStatus.NEARBY -> R.string.wifi_proximity_nearby
        DeviceWifiProximityStatus.NOT_NEARBY -> R.string.wifi_proximity_not_nearby
        DeviceWifiProximityStatus.UNKNOWN -> R.string.wifi_proximity_unknown
        DeviceWifiProximityStatus.NO_ASSIGNMENT -> R.string.wifi_proximity_no_assignment
        DeviceWifiProximityStatus.WIFI_DISABLED -> R.string.wifi_proximity_wifi_disabled
        DeviceWifiProximityStatus.LOCATION_SERVICES_DISABLED -> {
            R.string.wifi_proximity_location_services_disabled
        }
        DeviceWifiProximityStatus.PERMISSION_DENIED -> R.string.wifi_proximity_permission_denied
        DeviceWifiProximityStatus.SCAN_FAILED -> R.string.wifi_proximity_scan_failed
    }
    return stringResource(resourceId)
}

private val WifiNearbyColor = Color(0xFF2E7D32)
private val WifiNotNearbyColor = Color(0xFFC62828)
private val WifiUnavailableColor = Color(0xFF757575)
