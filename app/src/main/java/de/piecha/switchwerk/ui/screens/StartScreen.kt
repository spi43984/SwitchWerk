package de.piecha.switchwerk.ui.screens

import android.content.res.Configuration
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.LocalContentColor
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import de.piecha.switchwerk.domain.model.DashboardLayoutMode
import de.piecha.switchwerk.domain.model.Device
import de.piecha.switchwerk.domain.model.DeviceColor
import de.piecha.switchwerk.domain.model.SwitchGroup
import de.piecha.switchwerk.ui.components.AppMenuLayout
import de.piecha.switchwerk.ui.components.AppOverflowMenu
import de.piecha.switchwerk.ui.components.InfoHint
import de.piecha.switchwerk.ui.components.contentColor
import de.piecha.switchwerk.ui.components.toComposeColor
import de.piecha.switchwerk.ui.components.LazyGridScrollIndicator
import de.piecha.switchwerk.ui.components.LazyListScrollIndicator
import de.piecha.switchwerk.viewmodel.DeviceActionUiState
import de.piecha.switchwerk.viewmodel.DeviceWifiProximityStatus
import de.piecha.switchwerk.data.action.DiagnosticListItem
import de.piecha.switchwerk.viewmodel.DashboardItem
import de.piecha.switchwerk.viewmodel.MainViewModel
import de.piecha.switchwerk.R
import de.piecha.switchwerk.ui.asString
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.ceil
import kotlin.math.roundToInt

@Composable
fun StartScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToUpdates: () -> Unit,
    onNavigateToHelp: () -> Unit,
    onNavigateToAbout: () -> Unit,
    viewModel: MainViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val dashboardItems = uiState.dashboardItems.sortedBy { it.sortOrder }
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val screenPadding = if (isLandscape) 12.dp else 24.dp
    val lifecycleOwner = LocalLifecycleOwner.current
    val showActionDetails = uiState.appSettings.showActionDetails && !isLandscape
    val isDeviceActionRunning = uiState.deviceActionStates.values.any {
        it == DeviceActionUiState.Loading
    }
    val runningDeviceId = uiState.deviceActionStates.entries
        .lastOrNull { it.value == DeviceActionUiState.Loading }
        ?.key
    val diagnosticMessageCount = uiState.diagnosticItems.count {
        it is DiagnosticListItem.Message
    }
    var isActionDetailsExpanded by remember { mutableStateOf(false) }
    var previousDiagnosticMessageCount by remember {
        mutableIntStateOf(diagnosticMessageCount)
    }

    LaunchedEffect(showActionDetails, isDeviceActionRunning) {
        if (showActionDetails && isDeviceActionRunning) {
            isActionDetailsExpanded = true
        }
    }

    LaunchedEffect(diagnosticMessageCount) {
        if (showActionDetails && diagnosticMessageCount > previousDiagnosticMessageCount) {
            isActionDetailsExpanded = true
        }
        previousDiagnosticMessageCount = diagnosticMessageCount
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> viewModel.startWifiProximityMonitoring()
                Lifecycle.Event.ON_PAUSE -> {
                    isActionDetailsExpanded = false
                    viewModel.stopWifiProximityMonitoring()
                }
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
        onOpenUpdates = onNavigateToUpdates,
        onOpenHelp = onNavigateToHelp,
        onOpenAbout = onNavigateToAbout,
        isUpdateAvailable = uiState.updateSnapshot?.isUpdateAvailable == true,
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(screenPadding),
        rightEdgeExtension = screenPadding
    ) { openMenu ->
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val density = LocalDensity.current
            val configuration = LocalConfiguration.current
            val textMeasurer = rememberTextMeasurer()
            val switchingText = stringResource(R.string.switching)
            val widgetMinWidth = requiredWidgetMinWidth(
                items = dashboardItems,
                actionStates = uiState.deviceActionStates,
                switchingText = switchingText,
                actionStyle = MaterialTheme.typography.labelSmall,
                textMeasurer = textMeasurer,
                density = density
            )
            val canShowLayoutSelector = canShowDashboardLayoutSelector(
                itemCount = dashboardItems.size,
                availableWidth = maxWidth,
                widgetMinWidth = widgetMinWidth
            )
            val layoutDiagnostics = dashboardLayoutDiagnosticsText(
                screenWidth = configuration.screenWidthDp.dp,
                dashboardWidth = maxWidth,
                fontScale = density.fontScale,
                widgetMinWidth = widgetMinWidth,
                selectorVisible = canShowLayoutSelector
            )

            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                DashboardHeader(
                    itemCount = dashboardItems.size,
                    selectedMode = uiState.appSettings.dashboardLayoutMode,
                    isLandscape = isLandscape,
                    showLayoutSelector = canShowLayoutSelector,
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

                uiState.updateSnapshot?.takeIf { it.isUpdateAvailable }?.availableRelease?.let { release ->
                    UpdateAvailableBanner(
                        version = release.version,
                        onOpenSettings = onNavigateToUpdates,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(if (isLandscape) 6.dp else 12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    val detailHeight = uiState.appSettings.detailPanelHeight.fraction
                    val deviceAreaWeight = if (showActionDetails && isActionDetailsExpanded) {
                        1f - detailHeight
                    } else {
                        1f
                    }

                    if (dashboardItems.isEmpty()) {
                        EmptyDeviceList(modifier = Modifier.weight(deviceAreaWeight))
                    } else {
                        when (uiState.appSettings.dashboardLayoutMode) {
                            DashboardLayoutMode.LIST -> DashboardList(
                                items = dashboardItems,
                                actionStates = uiState.deviceActionStates,
                                wifiProximityStatuses = uiState.wifiProximityStatuses,
                                runningDeviceId = runningDeviceId,
                                isActionDetailsExpanded = isActionDetailsExpanded,
                                onDeviceActionClick = viewModel::executeDeviceAction,
                                onSwitchGroupActionClick = viewModel::executeSwitchGroupAction,
                                onCancelDeviceActionClick = viewModel::cancelDeviceAction,
                                onCancelSwitchGroupActionClick = viewModel::cancelSwitchGroupAction,
                                onMoveUpClick = viewModel::moveDeviceUp,
                                onMoveDownClick = viewModel::moveDeviceDown,
                                onMoveGroupUpClick = viewModel::moveSwitchGroupUp,
                                onMoveGroupDownClick = viewModel::moveSwitchGroupDown,
                                modifier = Modifier.weight(deviceAreaWeight)
                            )

                            DashboardLayoutMode.WIDGETS -> DashboardWidgetGrid(
                                items = dashboardItems,
                                actionStates = uiState.deviceActionStates,
                                wifiProximityStatuses = uiState.wifiProximityStatuses,
                                runningDeviceId = runningDeviceId,
                                isActionDetailsExpanded = isActionDetailsExpanded,
                                onDeviceActionClick = viewModel::executeDeviceAction,
                                onSwitchGroupActionClick = viewModel::executeSwitchGroupAction,
                                onCancelDeviceActionClick = viewModel::cancelDeviceAction,
                                onCancelSwitchGroupActionClick = viewModel::cancelSwitchGroupAction,
                                onMoveUpClick = viewModel::moveDeviceUp,
                                onMoveDownClick = viewModel::moveDeviceDown,
                                onMoveGroupUpClick = viewModel::moveSwitchGroupUp,
                                onMoveGroupDownClick = viewModel::moveSwitchGroupDown,
                                modifier = Modifier.weight(deviceAreaWeight)
                            )
                        }
                    }

                    if (showActionDetails) {
                        Spacer(modifier = Modifier.height(12.dp))
                        if (isActionDetailsExpanded) {
                            DiagnosticPanel(
                                items = uiState.diagnosticItems,
                                layoutDiagnostics = layoutDiagnostics,
                                newestFirst = uiState.appSettings.diagnosticsNewestFirst,
                                onClear = viewModel::clearDiagnosticMessages,
                                onToggleSortOrder = viewModel::toggleDiagnosticSortOrder,
                                onMinimize = { isActionDetailsExpanded = false },
                                modifier = Modifier.weight(detailHeight)
                            )
                        } else {
                            CollapsedActionDetailsPanel(
                                onExpand = { isActionDetailsExpanded = true }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UpdateAvailableBanner(
    version: String,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.dashboard_update_available, version),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            Button(onClick = onOpenSettings) {
                Text(stringResource(R.string.open_settings))
            }
        }
    }
}

@Composable
private fun DashboardHeader(
    itemCount: Int,
    selectedMode: DashboardLayoutMode,
    isLandscape: Boolean,
    showLayoutSelector: Boolean,
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
                text = pluralStringResource(R.plurals.dashboard_items_found, itemCount, itemCount),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1
            )
            if (showLayoutSelector) {
                DashboardLayoutSelector(
                    selectedMode = selectedMode,
                    onModeSelected = onModeSelected
                )
            }
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
                text = pluralStringResource(R.plurals.dashboard_items_found, itemCount, itemCount),
                style = MaterialTheme.typography.bodyLarge
            )
            if (showLayoutSelector) {
                DashboardLayoutSelector(
                    selectedMode = selectedMode,
                    onModeSelected = onModeSelected
                )
            }
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
            label = {
                Text(
                    text = stringResource(R.string.layout_list),
                    maxLines = 1,
                    softWrap = false
                )
            }
        )
        FilterChip(
            selected = selectedMode == DashboardLayoutMode.WIDGETS,
            onClick = { onModeSelected(DashboardLayoutMode.WIDGETS) },
            label = {
                Text(
                    text = stringResource(R.string.layout_widgets),
                    maxLines = 1,
                    softWrap = false
                )
            }
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
private fun DashboardList(
    items: List<DashboardItem>,
    actionStates: Map<String, DeviceActionUiState>,
    wifiProximityStatuses: Map<String, DeviceWifiProximityStatus>,
    runningDeviceId: String?,
    isActionDetailsExpanded: Boolean,
    onDeviceActionClick: (Device) -> Unit,
    onSwitchGroupActionClick: (SwitchGroup) -> Unit,
    onCancelDeviceActionClick: (String) -> Unit,
    onCancelSwitchGroupActionClick: (String) -> Unit,
    onMoveUpClick: (String) -> Unit,
    onMoveDownClick: (String) -> Unit,
    onMoveGroupUpClick: (String) -> Unit,
    onMoveGroupDownClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()

    LaunchedEffect(runningDeviceId, isActionDetailsExpanded) {
        val runningDeviceIndex = items.indexOfFirst { it.key == runningDeviceId }
        if (runningDeviceIndex >= 0) {
            listState.animateScrollToItem(runningDeviceIndex)
        }
    }

    Box(modifier = modifier) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(end = 8.dp),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = items,
                key = { item -> item.key }
            ) { item ->
                DashboardCard(
                    item = item,
                    actionState = actionStates[item.key],
                    wifiProximityStatus = wifiProximityStatuses[item.key]
                        ?: DeviceWifiProximityStatus.UNKNOWN,
                    canMoveUp = items.indexOf(item) > 0,
                    canMoveDown = items.indexOf(item) < items.lastIndex,
                    onActionClick = {
                        item.runAction(onDeviceActionClick, onSwitchGroupActionClick)
                    },
                    onCancelActionClick = {
                        item.cancelAction(onCancelDeviceActionClick, onCancelSwitchGroupActionClick)
                    },
                    onMoveUpClick = {
                        item.moveUp(onMoveUpClick, onMoveGroupUpClick)
                    },
                    onMoveDownClick = {
                        item.moveDown(onMoveDownClick, onMoveGroupDownClick)
                    }
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
private fun DashboardWidgetGrid(
    items: List<DashboardItem>,
    actionStates: Map<String, DeviceActionUiState>,
    wifiProximityStatuses: Map<String, DeviceWifiProximityStatus>,
    runningDeviceId: String?,
    isActionDetailsExpanded: Boolean,
    onDeviceActionClick: (Device) -> Unit,
    onSwitchGroupActionClick: (SwitchGroup) -> Unit,
    onCancelDeviceActionClick: (String) -> Unit,
    onCancelSwitchGroupActionClick: (String) -> Unit,
    onMoveUpClick: (String) -> Unit,
    onMoveDownClick: (String) -> Unit,
    onMoveGroupUpClick: (String) -> Unit,
    onMoveGroupDownClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val gridState = androidx.compose.foundation.lazy.grid.rememberLazyGridState()

    LaunchedEffect(runningDeviceId, isActionDetailsExpanded) {
        val runningDeviceIndex = items.indexOfFirst { it.key == runningDeviceId }
        if (runningDeviceIndex >= 0) {
            gridState.animateScrollToItem(runningDeviceIndex)
        }
    }

    BoxWithConstraints(modifier = modifier) {
        val textMeasurer = rememberTextMeasurer()
        val density = LocalDensity.current
        val switchingText = stringResource(R.string.switching)
        val adaptiveMinWidth = requiredWidgetMinWidth(
            items = items,
            actionStates = actionStates,
            switchingText = switchingText,
            actionStyle = MaterialTheme.typography.labelSmall,
            textMeasurer = textMeasurer,
            density = density
        ).coerceAtMost(maxWidth)

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = adaptiveMinWidth),
            modifier = Modifier
                .fillMaxSize()
                .padding(end = 8.dp),
            state = gridState,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = items,
                key = { item -> item.key }
            ) { item ->
                DashboardWidget(
                    item = item,
                    actionState = actionStates[item.key],
                    wifiProximityStatus = wifiProximityStatuses[item.key]
                        ?: DeviceWifiProximityStatus.UNKNOWN,
                    canMoveUp = items.indexOf(item) > 0,
                    canMoveDown = items.indexOf(item) < items.lastIndex,
                    onActionClick = {
                        item.runAction(onDeviceActionClick, onSwitchGroupActionClick)
                    },
                    onCancelActionClick = {
                        item.cancelAction(onCancelDeviceActionClick, onCancelSwitchGroupActionClick)
                    },
                    onMoveUpClick = {
                        item.moveUp(onMoveUpClick, onMoveGroupUpClick)
                    },
                    onMoveDownClick = {
                        item.moveDown(onMoveDownClick, onMoveGroupDownClick)
                    }
                )
            }
        }
        LazyGridScrollIndicator(
            gridState = gridState,
            modifier = Modifier.align(Alignment.CenterEnd)
        )
    }
}

@Composable
private fun DashboardWidget(
    item: DashboardItem,
    actionState: DeviceActionUiState?,
    wifiProximityStatus: DeviceWifiProximityStatus,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onActionClick: () -> Unit,
    onCancelActionClick: () -> Unit,
    onMoveUpClick: () -> Unit,
    onMoveDownClick: () -> Unit
) {
    val itemColor = item.deviceColor()
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = itemColor.cardColors()
    ) {
        Column(
            modifier = Modifier.padding(
                start = 8.dp,
                top = 10.dp,
                end = 8.dp,
                bottom = 0.dp
            )
        ) {
            DeviceTitle(
                name = item.name,
                wifiProximityStatus = wifiProximityStatus,
                isActionRunning = actionState == DeviceActionUiState.Loading,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.height(52.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            DeviceActionArea(
                item = item,
                actionState = actionState,
                canMoveUp = canMoveUp,
                canMoveDown = canMoveDown,
                onActionClick = onActionClick,
                onCancelActionClick = onCancelActionClick,
                onMoveUpClick = onMoveUpClick,
                onMoveDownClick = onMoveDownClick,
                singleLineActionText = true
            )
        }
    }
}

@Composable
private fun DeviceActionFooter(
    actionState: DeviceActionUiState?,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onCancelActionClick: () -> Unit,
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
        if (actionState == DeviceActionUiState.Loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = LocalContentColor.current
            )
            Spacer(modifier = Modifier.weight(1f))
            CompositionLocalProvider(
                LocalMinimumInteractiveComponentSize provides Dp.Unspecified
            ) {
                IconButton(
                    onClick = onCancelActionClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.cancel_action)
                    )
                }
            }
        } else if (actionState == null) {
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
    item: DashboardItem,
    actionState: DeviceActionUiState?,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onActionClick: () -> Unit,
    onCancelActionClick: () -> Unit,
    onMoveUpClick: () -> Unit,
    onMoveDownClick: () -> Unit,
    singleLineActionText: Boolean = false
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
        Column(modifier = Modifier.height(78.dp)) {
            DeviceActionButton(
                item = item,
                actionState = actionState,
                onActionClick = onActionClick,
                singleLineText = singleLineActionText
            )
            Spacer(modifier = Modifier.height(4.dp))
            DeviceActionFooter(
                actionState = actionState,
                canMoveUp = canMoveUp,
                canMoveDown = canMoveDown,
                onCancelActionClick = onCancelActionClick,
                onMoveUpClick = onMoveUpClick,
                onMoveDownClick = onMoveDownClick
            )
        }
    }
}

@Composable
private fun DeviceActionButton(
    item: DashboardItem,
    actionState: DeviceActionUiState?,
    onActionClick: () -> Unit,
    singleLineText: Boolean = false
) {
    val label = when (actionState) {
        DeviceActionUiState.Loading -> stringResource(R.string.switching)
        is DeviceActionUiState.Error -> actionState.message.asString()
        is DeviceActionUiState.Success,
        null -> if (item is DashboardItem.SwitchGroupItem && !item.isExecutable) {
            stringResource(R.string.action_group_empty)
        } else {
            item.actionLabel
        }
    }
    Button(
        onClick = onActionClick,
        enabled = actionState !is DeviceActionUiState.Loading &&
            (item !is DashboardItem.SwitchGroupItem || item.isExecutable),
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        contentPadding = if (singleLineText) {
            PaddingValues(horizontal = 6.dp)
        } else {
            ButtonDefaults.ContentPadding
        }
    ) {
        Text(
            text = label,
            style = if (singleLineText) {
                MaterialTheme.typography.labelSmall
            } else {
                MaterialTheme.typography.labelLarge
            },
            maxLines = if (singleLineText) 1 else 2,
            softWrap = !singleLineText,
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
        is DeviceActionUiState.Success -> LocalContentColor.current
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
    layoutDiagnostics: String,
    newestFirst: Boolean,
    onClear: () -> Unit,
    onToggleSortOrder: () -> Unit,
    onMinimize: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    val displayedItems = if (newestFirst) items.asReversed() else items

    LaunchedEffect(items.size, newestFirst) {
        if (displayedItems.isNotEmpty()) {
            listState.scrollToItem(if (newestFirst) 0 else displayedItems.size)
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
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.clickable(onClick = onMinimize)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(onClick = onToggleSortOrder) {
                        Text(
                            stringResource(
                                if (newestFirst) R.string.newest_first else R.string.newest_last
                            )
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    InfoHint(
                        titleResourceId = R.string.action_details,
                        messageResourceId = R.string.action_details_info
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    CompositionLocalProvider(
                        LocalMinimumInteractiveComponentSize provides Dp.Unspecified
                    ) {
                        IconButton(
                            onClick = onClear,
                            enabled = items.isNotEmpty(),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = stringResource(R.string.clear_action_log)
                            )
                        }
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
                        LayoutDiagnosticsEntry(layoutDiagnostics)
                    }
                    item {
                        Text(
                            text = stringResource(R.string.no_device_action_yet),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                } else {
                    if (!newestFirst) {
                        item {
                            LayoutDiagnosticsEntry(layoutDiagnostics)
                        }
                    }
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
                    if (newestFirst) {
                        item {
                            LayoutDiagnosticsEntry(layoutDiagnostics)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LayoutDiagnosticsEntry(layoutDiagnostics: String) {
    Text(
        text = layoutDiagnostics,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun CollapsedActionDetailsPanel(
    onExpand: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onExpand)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.action_details),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 12.dp)
            )
            InfoHint(
                titleResourceId = R.string.action_details,
                messageResourceId = R.string.action_details_info
            )
        }
    }
}

@Composable
private fun DashboardCard(
    item: DashboardItem,
    actionState: DeviceActionUiState?,
    wifiProximityStatus: DeviceWifiProximityStatus,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onActionClick: () -> Unit,
    onCancelActionClick: () -> Unit,
    onMoveUpClick: () -> Unit,
    onMoveDownClick: () -> Unit
) {
    val itemColor = item.deviceColor()
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = itemColor.cardColors()
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
                name = item.name,
                wifiProximityStatus = wifiProximityStatus,
                isActionRunning = actionState == DeviceActionUiState.Loading,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(12.dp))

            DeviceActionArea(
                item = item,
                actionState = actionState,
                canMoveUp = canMoveUp,
                canMoveDown = canMoveDown,
                onActionClick = onActionClick,
                onCancelActionClick = onCancelActionClick,
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
    modifier: Modifier = Modifier,
    overflow: TextOverflow = TextOverflow.Ellipsis,
    softWrap: Boolean = true
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
            softWrap = softWrap,
            overflow = overflow,
            modifier = Modifier.weight(1f)
        )
        WifiProximityIndicator(
            status = wifiProximityStatus,
            isActionRunning = isActionRunning,
            modifier = Modifier.padding(top = 5.dp)
        )
    }
}

private fun DashboardItem.deviceColor(): DeviceColor = when (this) {
    is DashboardItem.DeviceItem -> device.color
    is DashboardItem.SwitchGroupItem -> group.color
}

@Composable
private fun DeviceColor.cardColors() = if (this == DeviceColor.NONE) {
    CardDefaults.cardColors()
} else {
    CardDefaults.cardColors(
        containerColor = requireNotNull(toComposeColor()),
        contentColor = contentColor()
    )
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
    val statusColor = when (status) {
        DeviceWifiProximityStatus.NEARBY -> WifiNearbyColor
        DeviceWifiProximityStatus.UNKNOWN,
        DeviceWifiProximityStatus.NO_ASSIGNMENT,
        DeviceWifiProximityStatus.LOCATION_SERVICES_DISABLED -> WifiUnavailableColor
        else -> WifiNotNearbyColor
    }
    val frameColor = LocalContentColor.current
    val innerColor = if (isActionRunning) {
        val transition = rememberInfiniteTransition(label = "wifi proximity pulse")
        val pulseProgress = transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 650),
                repeatMode = RepeatMode.Reverse
            ),
            label = "wifi proximity color progress"
        ).value
        lerp(statusColor, frameColor, pulseProgress)
    } else {
        statusColor
    }

    Box(
        modifier = modifier
            .size(16.dp)
            .background(color = frameColor, shape = CircleShape)
            .clearAndSetSemantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color = innerColor, shape = CircleShape)
        )
    }
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

private val WidgetBaseMinWidth = 164.dp
private const val WidgetCompactScale = 0.75f
private val WidgetGridEndPadding = 8.dp
private val WidgetGridHorizontalSpacing = 12.dp
private val WidgetCardHorizontalPadding = 16.dp
private val WidgetButtonHorizontalPadding = 12.dp

private fun canShowDashboardLayoutSelector(
    itemCount: Int,
    availableWidth: Dp,
    widgetMinWidth: Dp
): Boolean {
    if (itemCount < 2) {
        return true
    }
    val widgetAreaWidth = availableWidth - WidgetGridEndPadding
    val twoColumnWidth = widgetMinWidth + widgetMinWidth + WidgetGridHorizontalSpacing
    return twoColumnWidth <= widgetAreaWidth
}

@Composable
private fun dashboardLayoutDiagnosticsText(
    screenWidth: Dp,
    dashboardWidth: Dp,
    fontScale: Float,
    widgetMinWidth: Dp,
    selectorVisible: Boolean
): String {
    val selectorState = stringResource(
        if (selectorVisible) {
            R.string.dashboard_layout_selector_visible
        } else {
            R.string.dashboard_layout_selector_hidden
        }
    )
    return stringResource(
        R.string.dashboard_layout_diagnostics,
        screenWidth.value.roundToInt(),
        dashboardWidth.value.roundToInt(),
        fontScale,
        widgetMinWidth.value.roundToInt(),
        selectorState
    )
}

private fun requiredWidgetMinWidth(
    items: List<DashboardItem>,
    actionStates: Map<String, DeviceActionUiState>,
    switchingText: String,
    actionStyle: androidx.compose.ui.text.TextStyle,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    density: androidx.compose.ui.unit.Density
): Dp {
    val actionTextWidth = items.minOfOrNull { item ->
        val actionText = if (actionStates[item.key] == DeviceActionUiState.Loading) {
            switchingText
        } else {
            item.actionLabel
        }
        measureTextWidth(actionText, actionStyle, textMeasurer, density)
    } ?: 0.dp
    val measuredMinWidth = actionTextWidth + WidgetButtonHorizontalPadding + WidgetCardHorizontalPadding
    val baseScale = if (density.fontScale < 1f) WidgetCompactScale else density.fontScale
    val scaledBaseMinWidth = WidgetBaseMinWidth * baseScale
    return maxOf(scaledBaseMinWidth, measuredMinWidth)
}

private fun DashboardItem.runAction(
    onDeviceActionClick: (Device) -> Unit,
    onSwitchGroupActionClick: (SwitchGroup) -> Unit
) {
    when (this) {
        is DashboardItem.DeviceItem -> onDeviceActionClick(device)
        is DashboardItem.SwitchGroupItem -> if (isExecutable) onSwitchGroupActionClick(group)
    }
}

private fun DashboardItem.cancelAction(
    onCancelDeviceActionClick: (String) -> Unit,
    onCancelSwitchGroupActionClick: (String) -> Unit
) {
    when (this) {
        is DashboardItem.DeviceItem -> onCancelDeviceActionClick(device.id)
        is DashboardItem.SwitchGroupItem -> onCancelSwitchGroupActionClick(group.id)
    }
}

private fun DashboardItem.moveUp(
    onMoveUpClick: (String) -> Unit,
    onMoveGroupUpClick: (String) -> Unit
) {
    when (this) {
        is DashboardItem.DeviceItem -> onMoveUpClick(device.id)
        is DashboardItem.SwitchGroupItem -> onMoveGroupUpClick(group.id)
    }
}

private fun DashboardItem.moveDown(
    onMoveDownClick: (String) -> Unit,
    onMoveGroupDownClick: (String) -> Unit
) {
    when (this) {
        is DashboardItem.DeviceItem -> onMoveDownClick(device.id)
        is DashboardItem.SwitchGroupItem -> onMoveGroupDownClick(group.id)
    }
}

private fun measureTextWidth(
    text: String,
    style: androidx.compose.ui.text.TextStyle,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    density: androidx.compose.ui.unit.Density
): Dp {
    val width = textMeasurer.measure(
        text = text,
        style = style,
        maxLines = 1,
        softWrap = false
    ).size.width
    return with(density) { ceil(width.toFloat()).toDp() }
}
