package de.piecha.switchwerk.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.piecha.switchwerk.R
import de.piecha.switchwerk.data.action.ActionDetailStore
import de.piecha.switchwerk.data.action.ActionOrigin
import de.piecha.switchwerk.data.action.DeviceActionResult
import de.piecha.switchwerk.data.action.DeviceActionDiagnosticEvent
import de.piecha.switchwerk.data.action.DeviceActionService
import de.piecha.switchwerk.data.action.DiagnosticListItem
import de.piecha.switchwerk.data.action.InMemoryActionDetailStore
import de.piecha.switchwerk.data.action.NetworkFailureReason
import de.piecha.switchwerk.data.action.SwitchGroupActionResult
import de.piecha.switchwerk.data.action.SwitchGroupActionService
import de.piecha.switchwerk.data.action.SwitchGroupDiagnosticEvent
import de.piecha.switchwerk.data.action.toActionDetailMessage
import de.piecha.switchwerk.data.repository.AppSettingsRepository
import de.piecha.switchwerk.data.repository.DeviceRepository
import de.piecha.switchwerk.data.repository.SwitchGroupRepository
import de.piecha.switchwerk.data.repository.WifiProfileRepository
import de.piecha.switchwerk.data.network.WifiProximityIssue
import de.piecha.switchwerk.data.network.WifiProximityService
import de.piecha.switchwerk.data.network.WifiProximitySnapshot
import de.piecha.switchwerk.data.update.AppUpdateRepository
import de.piecha.switchwerk.domain.model.AppSettings
import de.piecha.switchwerk.domain.model.AppUpdateCheckResult
import de.piecha.switchwerk.domain.model.AppUpdateSnapshot
import de.piecha.switchwerk.domain.model.DashboardLayoutMode
import de.piecha.switchwerk.domain.model.Device
import de.piecha.switchwerk.domain.model.SwitchGroup
import de.piecha.switchwerk.domain.model.WifiProfile
import de.piecha.switchwerk.ui.UiText
import de.piecha.switchwerk.ui.uiText
import de.piecha.switchwerk.intent.ExternalDeviceActionIntentResult
import de.piecha.switchwerk.intent.ExternalSwitchGroupActionIntentResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

sealed interface DeviceActionUiState {
    data object Loading : DeviceActionUiState

    data class Success(val message: UiText) : DeviceActionUiState

    data class Error(val message: UiText) : DeviceActionUiState
}

sealed interface MainUiEvent {
    data class ConfirmOpenAndroidWifiSettings(val ssid: String) : MainUiEvent

    data object OpenSetupWizard : MainUiEvent
}

enum class DeviceWifiProximityStatus {
    NEARBY,
    NOT_NEARBY,
    UNKNOWN,
    NO_ASSIGNMENT,
    WIFI_DISABLED,
    LOCATION_SERVICES_DISABLED,
    PERMISSION_DENIED,
    SCAN_FAILED
}

sealed interface DashboardItem {
    val key: String
    val id: String
    val name: String
    val actionLabel: String
    val sortOrder: Int

    data class DeviceItem(val device: Device) : DashboardItem {
        override val key: String = actionKey(device.id)
        override val id: String = device.id
        override val name: String = device.name
        override val actionLabel: String = device.actionLabel
        override val sortOrder: Int = device.sortOrder
    }

    data class SwitchGroupItem(
        val group: SwitchGroup,
        val availableMemberCount: Int
    ) : DashboardItem {
        override val key: String = groupKey(group.id)
        override val id: String = group.id
        override val name: String = group.name
        override val actionLabel: String = group.actionLabel
        override val sortOrder: Int = group.sortOrder
        val isExecutable: Boolean = availableMemberCount > 0
    }

    companion object {
        fun actionKey(deviceId: String): String = deviceId
        fun groupKey(groupId: String): String = "group:$groupId"
    }
}

data class MainUiState(
    val devices: List<Device> = emptyList(),
    val switchGroups: List<SwitchGroup> = emptyList(),
    val dashboardItems: List<DashboardItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: UiText? = null,
    val deviceActionStates: Map<String, DeviceActionUiState> = emptyMap(),
    val wifiProximityStatuses: Map<String, DeviceWifiProximityStatus> = emptyMap(),
    val appSettings: AppSettings = AppSettings(),
    val diagnosticItems: List<DiagnosticListItem> = emptyList(),
    val updateSnapshot: AppUpdateSnapshot? = null
)

class MainViewModel(
    private val repository: DeviceRepository,
    private val switchGroupRepository: SwitchGroupRepository = EmptySwitchGroupRepository,
    private val deviceActionService: DeviceActionService,
    private val switchGroupActionService: SwitchGroupActionService = EmptySwitchGroupActionService,
    private val appSettingsRepository: AppSettingsRepository,
    private val wifiProfileRepository: WifiProfileRepository,
    private val wifiProximityService: WifiProximityService,
    private val appUpdateRepository: AppUpdateRepository,
    private val actionDetailStore: ActionDetailStore = InMemoryActionDetailStore()
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        MainUiState(
            isLoading = true,
            appSettings = appSettingsRepository.settings.value
        )
    )
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()
    private val _events = MutableSharedFlow<MainUiEvent>()
    val events: SharedFlow<MainUiEvent> = _events.asSharedFlow()
    private val actionJobs = mutableMapOf<String, Job>()
    private val userCancelledActionIds = mutableSetOf<String>()
    private val actionStateResetJobs = mutableMapOf<String, Job>()
    private var generalErrorResetJob: Job? = null
    private var wifiProfiles: List<WifiProfile> = emptyList()
    private var wifiProximitySnapshot = WifiProximitySnapshot()
    private var wifiRefreshJob: Job? = null
    private var wifiMonitorJob: Job? = null

    init {
        observeDevices()
        observeSwitchGroups()
        observeAppSettings()
        observeWifiProfiles()
        observeActionDetails()
        checkForUpdatesAutomatically()
    }

    private fun observeActionDetails() {
        viewModelScope.launch {
            actionDetailStore.items.collect { items ->
                _uiState.value = _uiState.value.copy(diagnosticItems = items)
            }
        }
    }

    private fun observeDevices() {
        viewModelScope.launch {
            runCatching {
                repository.observeDevices().collect { devices ->
                    val sortedDevices = devices.sortedBy { it.sortOrder }
                    val current = _uiState.value.copy(
                        devices = sortedDevices,
                        wifiProximityStatuses = resolveDashboardWifiProximityStatuses(
                            devices = sortedDevices,
                            switchGroups = _uiState.value.switchGroups,
                            wifiProfiles = wifiProfiles,
                            snapshot = wifiProximitySnapshot
                        ),
                        isLoading = false,
                        errorMessage = null
                    )
                    _uiState.value = current.copy(
                        dashboardItems = createDashboardItems(
                            devices = current.devices,
                            switchGroups = current.switchGroups
                        )
                    )
                }
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error.toUiText(R.string.error_devices_load)
                )
            }
        }
    }

    private fun observeSwitchGroups() {
        viewModelScope.launch {
            runCatching {
                switchGroupRepository.observeSwitchGroups().collect { groups ->
                    val current = _uiState.value.copy(
                        switchGroups = groups.sortedBy { it.sortOrder },
                        wifiProximityStatuses = resolveDashboardWifiProximityStatuses(
                            devices = _uiState.value.devices,
                            switchGroups = groups.sortedBy { it.sortOrder },
                            wifiProfiles = wifiProfiles,
                            snapshot = wifiProximitySnapshot
                        ),
                        isLoading = false,
                        errorMessage = null
                    )
                    _uiState.value = current.copy(
                        dashboardItems = createDashboardItems(
                            devices = current.devices,
                            switchGroups = current.switchGroups
                        )
                    )
                }
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error.toUiText(R.string.error_devices_load)
                )
            }
        }
    }

    fun executeDeviceAction(device: Device, origin: ActionOrigin = ActionOrigin.DASHBOARD) {
        val actionKey = DashboardItem.actionKey(device.id)
        if (actionJobs[actionKey]?.isActive == true) {
            return
        }

        actionStateResetJobs.remove(actionKey)?.cancel()
        updateDeviceActionState(actionKey, DeviceActionUiState.Loading)
        val actionDetails = actionDetailStore.start(origin)
        actionJobs[actionKey] = viewModelScope.launch {
            try {
                val result = deviceActionService.execute(device) { event ->
                    actionDetails.append(event.toActionDetailMessage(device.name))
                }
                val resultState = result.toUiState()
                updateDeviceActionState(actionKey, resultState)
                if (result is DeviceActionResult.AndroidManagedWifiNotActive) {
                    _events.emit(MainUiEvent.ConfirmOpenAndroidWifiSettings(result.ssid))
                }
                when (resultState) {
                    is DeviceActionUiState.Success -> scheduleActionStateReset(
                        actionKey = actionKey,
                        state = resultState,
                        delayMillis = ACTION_SUCCESS_DISPLAY_MILLIS
                    )
                    is DeviceActionUiState.Error -> scheduleActionStateReset(
                        actionKey = actionKey,
                        state = resultState,
                        delayMillis = ACTION_ERROR_DISPLAY_MILLIS
                    )
                    DeviceActionUiState.Loading -> Unit
                }
            } catch (exception: CancellationException) {
                if (actionKey !in userCancelledActionIds) {
                    throw exception
                }
                actionDetails.append(
                    DeviceActionDiagnosticEvent.ActionCancelled.toActionDetailMessage(device.name)
                )
                val cancelledState = DeviceActionUiState.Error(uiText(R.string.action_cancelled))
                updateDeviceActionState(actionKey, cancelledState)
                scheduleActionStateReset(
                    actionKey = actionKey,
                    state = cancelledState,
                    delayMillis = ACTION_ERROR_DISPLAY_MILLIS
                )
                throw exception
            }
        }.also { job ->
            job.invokeOnCompletion {
                actionJobs.remove(actionKey, job)
                userCancelledActionIds.remove(actionKey)
            }
        }
    }

    fun executeSwitchGroupAction(
        group: SwitchGroup,
        origin: ActionOrigin = ActionOrigin.DASHBOARD
    ) {
        val actionKey = DashboardItem.groupKey(group.id)
        if (actionJobs[actionKey]?.isActive == true) {
            return
        }

        actionStateResetJobs.remove(actionKey)?.cancel()
        updateDeviceActionState(actionKey, DeviceActionUiState.Loading)
        val actionDetails = actionDetailStore.start(origin)
        actionJobs[actionKey] = viewModelScope.launch {
            try {
                val result = switchGroupActionService.execute(
                    group = group,
                    devices = _uiState.value.devices
                ) { event ->
                    actionDetails.append(event.toActionDetailMessage(group.name))
                }
                val resultState = result.toUiState()
                updateDeviceActionState(actionKey, resultState)
                when (resultState) {
                    is DeviceActionUiState.Success -> scheduleActionStateReset(
                        actionKey = actionKey,
                        state = resultState,
                        delayMillis = ACTION_SUCCESS_DISPLAY_MILLIS
                    )
                    is DeviceActionUiState.Error -> scheduleActionStateReset(
                        actionKey = actionKey,
                        state = resultState,
                        delayMillis = ACTION_ERROR_DISPLAY_MILLIS
                    )
                    DeviceActionUiState.Loading -> Unit
                }
            } catch (exception: CancellationException) {
                if (actionKey !in userCancelledActionIds) {
                    throw exception
                }
                val cancelledState = DeviceActionUiState.Error(uiText(R.string.action_cancelled))
                updateDeviceActionState(actionKey, cancelledState)
                scheduleActionStateReset(
                    actionKey = actionKey,
                    state = cancelledState,
                    delayMillis = ACTION_ERROR_DISPLAY_MILLIS
                )
                throw exception
            }
        }.also { job ->
            job.invokeOnCompletion {
                actionJobs.remove(actionKey, job)
                userCancelledActionIds.remove(actionKey)
            }
        }
    }

    fun handleShortcutDeviceAction(deviceId: String) {
        val device = _uiState.value.devices.firstOrNull { it.id == deviceId }
        if (device == null) {
            reportEntryPointError(
                message = uiText(R.string.diagnostic_target_unavailable),
                origin = ActionOrigin.APP_SHORTCUT
            )
        } else {
            executeDeviceAction(device, ActionOrigin.APP_SHORTCUT)
        }
    }

    fun handleShortcutSwitchGroupAction(groupId: String) {
        val group = _uiState.value.switchGroups.firstOrNull { it.id == groupId }
        if (group == null || group.members.isEmpty()) {
            reportEntryPointError(
                message = uiText(R.string.diagnostic_target_unavailable),
                origin = ActionOrigin.APP_SHORTCUT
            )
        } else {
            executeSwitchGroupAction(group, ActionOrigin.APP_SHORTCUT)
        }
    }

    fun handleExternalDeviceAction(result: ExternalDeviceActionIntentResult) {
        val validDevice = (result as? ExternalDeviceActionIntentResult.Valid)?.let { request ->
            _uiState.value.devices.firstOrNull { it.id == request.deviceId }
        }
        if (!_uiState.value.appSettings.externalIntentsEnabled) {
            reportExternalIntentError(
                message = uiText(R.string.external_intent_disabled_error),
                actionKey = validDevice?.id?.let(DashboardItem::actionKey)
            )
            return
        }
        when (result) {
            is ExternalDeviceActionIntentResult.Valid -> {
                if (validDevice == null) {
                    reportExternalIntentError(
                        uiText(R.string.external_intent_unknown_device_error)
                    )
                } else {
                    _uiState.value = _uiState.value.copy(errorMessage = null)
                    executeDeviceAction(validDevice, ActionOrigin.EXTERNAL_INTENT)
                }
            }
            ExternalDeviceActionIntentResult.MissingDeviceId -> {
                reportExternalIntentError(
                    uiText(R.string.external_intent_missing_device_id_error)
                )
            }
            ExternalDeviceActionIntentResult.InvalidDeviceId,
            ExternalDeviceActionIntentResult.UnexpectedExtras -> {
                reportExternalIntentError(
                    uiText(R.string.external_intent_invalid_error)
                )
            }
        }
    }

    fun handleExternalSwitchGroupAction(result: ExternalSwitchGroupActionIntentResult) {
        val validGroup = (result as? ExternalSwitchGroupActionIntentResult.Valid)?.let { request ->
            _uiState.value.switchGroups.firstOrNull { it.id == request.groupId }
        }
        if (!_uiState.value.appSettings.externalIntentsEnabled) {
            reportExternalIntentError(
                message = uiText(R.string.external_intent_disabled_error),
                actionKey = validGroup?.id?.let(DashboardItem::groupKey)
            )
            return
        }
        when (result) {
            is ExternalSwitchGroupActionIntentResult.Valid -> {
                when {
                    validGroup == null -> {
                        reportExternalIntentError(
                            uiText(R.string.external_intent_unknown_group_error)
                        )
                    }
                    validGroup.members.isEmpty() -> {
                        reportExternalIntentError(
                            message = uiText(R.string.external_intent_empty_group_error),
                            actionKey = DashboardItem.groupKey(validGroup.id)
                        )
                    }
                    else -> {
                        _uiState.value = _uiState.value.copy(errorMessage = null)
                        executeSwitchGroupAction(validGroup, ActionOrigin.EXTERNAL_INTENT)
                    }
                }
            }
            ExternalSwitchGroupActionIntentResult.MissingGroupId -> {
                reportExternalIntentError(
                    uiText(R.string.external_intent_missing_group_id_error)
                )
            }
            ExternalSwitchGroupActionIntentResult.InvalidGroupId,
            ExternalSwitchGroupActionIntentResult.UnexpectedExtras -> {
                reportExternalIntentError(
                    uiText(R.string.external_intent_invalid_error)
                )
            }
        }
    }

    private fun reportExternalIntentError(message: UiText, actionKey: String? = null) {
        reportEntryPointError(message, ActionOrigin.EXTERNAL_INTENT)
        if (actionKey != null) {
            actionStateResetJobs.remove(actionKey)?.cancel()
            val errorState = DeviceActionUiState.Error(message)
            updateDeviceActionState(actionKey, errorState)
            scheduleActionStateReset(
                actionKey = actionKey,
                state = errorState,
                delayMillis = ACTION_ERROR_DISPLAY_MILLIS
            )
            return
        }

        generalErrorResetJob?.cancel()
        _uiState.value = _uiState.value.copy(errorMessage = message)
        generalErrorResetJob = viewModelScope.launch {
            delay(ACTION_ERROR_DISPLAY_MILLIS)
            if (_uiState.value.errorMessage == message) {
                _uiState.value = _uiState.value.copy(errorMessage = null)
            }
        }
    }

    private fun reportEntryPointError(message: UiText, origin: ActionOrigin) {
        actionDetailStore.start(origin).append(message)
    }

    fun cancelDeviceAction(deviceId: String) {
        val actionKey = DashboardItem.actionKey(deviceId)
        actionJobs[actionKey]?.takeIf(Job::isActive)?.let { job ->
            userCancelledActionIds += actionKey
            job.cancel()
        }
    }

    fun cancelSwitchGroupAction(groupId: String) {
        val actionKey = DashboardItem.groupKey(groupId)
        actionJobs[actionKey]?.takeIf(Job::isActive)?.let { job ->
            userCancelledActionIds += actionKey
            job.cancel()
        }
    }

    fun refreshWifiProximity() {
        wifiRefreshJob?.cancel()
        wifiRefreshJob = viewModelScope.launch {
            wifiProximitySnapshot = wifiProximityService.refresh()
            updateWifiProximityState()
        }
    }

    fun startWifiProximityMonitoring() {
        if (wifiMonitorJob?.isActive == true) {
            return
        }
        wifiMonitorJob = viewModelScope.launch {
            wifiProximityService.observe().collect { snapshot ->
                wifiProximitySnapshot = snapshot
                updateWifiProximityState()
            }
        }
        refreshWifiProximity()
    }

    fun stopWifiProximityMonitoring() {
        wifiRefreshJob?.cancel()
        wifiRefreshJob = null
        wifiMonitorJob?.cancel()
        wifiMonitorJob = null
    }

    fun moveDeviceUp(deviceId: String) {
        moveDashboardItem(itemKey = DashboardItem.actionKey(deviceId), offset = -1)
    }

    fun moveDeviceDown(deviceId: String) {
        moveDashboardItem(itemKey = DashboardItem.actionKey(deviceId), offset = 1)
    }

    fun moveSwitchGroupUp(groupId: String) {
        moveDashboardItem(itemKey = DashboardItem.groupKey(groupId), offset = -1)
    }

    fun moveSwitchGroupDown(groupId: String) {
        moveDashboardItem(itemKey = DashboardItem.groupKey(groupId), offset = 1)
    }

    fun toggleDiagnosticSortOrder() {
        appSettingsRepository.setDiagnosticsNewestFirst(
            !_uiState.value.appSettings.diagnosticsNewestFirst
        )
    }

    fun setDashboardLayoutMode(dashboardLayoutMode: DashboardLayoutMode) {
        appSettingsRepository.setDashboardLayoutMode(dashboardLayoutMode)
    }

    fun clearDiagnosticMessages() {
        actionDetailStore.clear()
    }

    fun hideSetupWizardOnStart() {
        appSettingsRepository.setShowSetupWizardOnStart(false)
    }

    fun showSetupWizardAgain() {
        appSettingsRepository.setShowSetupWizardOnStart(true)
        viewModelScope.launch {
            _events.emit(MainUiEvent.OpenSetupWizard)
        }
    }

    private fun moveDashboardItem(itemKey: String, offset: Int) {
        val items = _uiState.value.dashboardItems.sortedBy { it.sortOrder }
        val currentIndex = items.indexOfFirst { it.key == itemKey }
        val targetIndex = currentIndex + offset

        if (currentIndex !in items.indices || targetIndex !in items.indices) {
            return
        }

        val reorderedItems = items.toMutableList().apply {
            add(targetIndex, removeAt(currentIndex))
        }

        viewModelScope.launch {
            runCatching {
                repository.updateDeviceSortOrders(
                    reorderedItems.mapIndexedNotNull { index, item ->
                        (item as? DashboardItem.DeviceItem)?.device?.id?.let { it to index }
                    }.toMap()
                )
                switchGroupRepository.updateSwitchGroupSortOrders(
                    reorderedItems.mapIndexedNotNull { index, item ->
                        (item as? DashboardItem.SwitchGroupItem)?.group?.id?.let { it to index }
                    }.toMap()
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    errorMessage = error.toUiText(R.string.error_device_order)
                )
            }
        }
    }

    private fun updateDeviceActionState(actionKey: String, state: DeviceActionUiState) {
        _uiState.value = _uiState.value.copy(
            deviceActionStates = _uiState.value.deviceActionStates + (actionKey to state)
        )
    }

    private fun scheduleActionStateReset(
        actionKey: String,
        state: DeviceActionUiState,
        delayMillis: Long
    ) {
        actionStateResetJobs[actionKey] = viewModelScope.launch {
            delay(delayMillis)
            if (_uiState.value.deviceActionStates[actionKey] == state) {
                _uiState.value = _uiState.value.copy(
                    deviceActionStates = _uiState.value.deviceActionStates - actionKey
                )
            }
        }.also { job ->
            job.invokeOnCompletion {
                actionStateResetJobs.remove(actionKey, job)
            }
        }
    }

    private fun observeAppSettings() {
        viewModelScope.launch {
            appSettingsRepository.settings.collect { settings ->
                _uiState.value = _uiState.value.copy(appSettings = settings)
            }
        }
    }

    private fun observeWifiProfiles() {
        viewModelScope.launch {
            wifiProfileRepository.observeWifiProfiles().collect { profiles ->
                wifiProfiles = profiles
                updateWifiProximityState()
            }
        }
    }

    private fun checkForUpdatesAutomatically() {
        _uiState.value = _uiState.value.copy(updateSnapshot = appUpdateRepository.cachedUpdate())
        viewModelScope.launch {
            when (val result = appUpdateRepository.checkForUpdates(force = false)) {
                is AppUpdateCheckResult.Success -> {
                    _uiState.value = _uiState.value.copy(updateSnapshot = result.snapshot)
                }
                is AppUpdateCheckResult.Error -> {
                    _uiState.value = _uiState.value.copy(updateSnapshot = result.snapshot)
                }
            }
        }
    }

    private fun updateWifiProximityState() {
        _uiState.value = _uiState.value.copy(
            wifiProximityStatuses = resolveDashboardWifiProximityStatuses(
                devices = _uiState.value.devices,
                switchGroups = _uiState.value.switchGroups,
                wifiProfiles = wifiProfiles,
                snapshot = wifiProximitySnapshot
            )
        )
    }

    private fun DeviceActionResult.toUiState(): DeviceActionUiState {
        return when (this) {
            DeviceActionResult.Success -> DeviceActionUiState.Success(uiText(R.string.action_success))
            DeviceActionResult.NoConnections -> {
                DeviceActionUiState.Error(uiText(R.string.action_no_wifi))
            }

            DeviceActionResult.WifiConnectionFailed -> {
                DeviceActionUiState.Error(uiText(R.string.action_wifi_unreachable))
            }
            is DeviceActionResult.AndroidManagedWifiNotActive -> {
                DeviceActionUiState.Error(uiText(R.string.action_android_managed_wifi_not_active))
            }

            DeviceActionResult.WifiPermissionDenied -> {
                DeviceActionUiState.Error(uiText(R.string.action_wifi_permission_denied))
            }

            DeviceActionResult.WifiDisabled -> {
                DeviceActionUiState.Error(uiText(R.string.action_wifi_disabled))
            }

            DeviceActionResult.MissingWifiPassword -> {
                DeviceActionUiState.Error(uiText(R.string.action_wifi_missing_password))
            }

            DeviceActionResult.UnsupportedAndroidVersion -> {
                DeviceActionUiState.Error(uiText(R.string.action_android_unsupported))
            }

            is DeviceActionResult.HttpError -> {
                DeviceActionUiState.Error(uiText(R.string.action_http_error, statusCode))
            }

            DeviceActionResult.Timeout -> {
                DeviceActionUiState.Error(uiText(R.string.action_timeout))
            }

            DeviceActionResult.InvalidRequest -> {
                DeviceActionUiState.Error(uiText(R.string.action_invalid_address))
            }

            DeviceActionResult.TlsCertificateError -> {
                DeviceActionUiState.Error(uiText(R.string.action_tls_certificate_error))
            }

            is DeviceActionResult.NetworkError -> {
                val detail = when (reason) {
                    NetworkFailureReason.DNS -> uiText(R.string.action_dns_error)
                    NetworkFailureReason.CONNECTION -> uiText(R.string.action_connection_error)
                    NetworkFailureReason.NO_ROUTE -> uiText(R.string.action_no_route)
                    NetworkFailureReason.TLS_CERTIFICATE -> {
                        uiText(R.string.action_tls_certificate_error)
                    }
                    NetworkFailureReason.VPN_BLOCKED -> {
                        uiText(R.string.action_vpn_blocked)
                    }
                    NetworkFailureReason.OTHER -> uiText(R.string.action_network_error)
                }
                DeviceActionUiState.Error(detail)
            }

            DeviceActionResult.UnexpectedError -> {
                DeviceActionUiState.Error(uiText(R.string.action_failed))
            }
        }
    }

    private fun SwitchGroupActionResult.toUiState(): DeviceActionUiState {
        return when (this) {
            SwitchGroupActionResult.Success -> {
                DeviceActionUiState.Success(uiText(R.string.action_group_success))
            }
            is SwitchGroupActionResult.SuccessWithFailures -> {
                DeviceActionUiState.Success(
                    uiText(R.string.action_group_success_with_failures, failedSteps)
                )
            }
            SwitchGroupActionResult.EmptyGroup -> {
                DeviceActionUiState.Error(uiText(R.string.action_group_empty))
            }
            is SwitchGroupActionResult.MissingDevice -> {
                DeviceActionUiState.Error(uiText(R.string.action_group_missing_device, step))
            }
            is SwitchGroupActionResult.DeviceFailed -> {
                val detail = result.toUiState()
                val message = when (detail) {
                    is DeviceActionUiState.Error -> detail.message
                    is DeviceActionUiState.Success -> detail.message
                    DeviceActionUiState.Loading -> uiText(R.string.action_failed)
                }
                DeviceActionUiState.Error(
                    uiText(R.string.action_group_step_failed, step, deviceName, message)
                )
            }
        }
    }

    private fun Throwable.toUiText(fallbackResourceId: Int): UiText =
        message?.takeIf(String::isNotBlank)?.let(UiText::Dynamic)
            ?: uiText(fallbackResourceId)

    private companion object {
        const val ACTION_SUCCESS_DISPLAY_MILLIS = 2_000L
        const val ACTION_ERROR_DISPLAY_MILLIS = 4_000L
    }
}

private object EmptySwitchGroupRepository : SwitchGroupRepository {
    override fun observeSwitchGroups() = flowOf(emptyList<SwitchGroup>())

    override suspend fun getSwitchGroups(): List<SwitchGroup> = emptyList()

    override suspend fun saveSwitchGroup(group: SwitchGroup) = Unit

    override suspend fun updateSwitchGroupOrder(groupIds: List<String>) = Unit

    override suspend fun updateSwitchGroupSortOrders(sortOrders: Map<String, Int>) = Unit

    override suspend fun deleteSwitchGroup(groupId: String) = Unit
}

private object EmptySwitchGroupActionService : SwitchGroupActionService {
    override suspend fun execute(
        group: SwitchGroup,
        devices: List<Device>,
        onDiagnosticEvent: (SwitchGroupDiagnosticEvent) -> Unit
    ): SwitchGroupActionResult = SwitchGroupActionResult.EmptyGroup
}

private fun createDashboardItems(
    devices: List<Device>,
    switchGroups: List<SwitchGroup>
): List<DashboardItem> {
    val deviceIds = devices.map { it.id }.toSet()
    return (
        devices.map(DashboardItem::DeviceItem) +
            switchGroups.map { group ->
                DashboardItem.SwitchGroupItem(
                    group = group,
                    availableMemberCount = group.members.count { it.deviceId in deviceIds }
                )
            }
    ).sortedWith(compareBy<DashboardItem> { it.sortOrder }.thenBy { it.name.lowercase() })
}

internal fun resolveWifiProximityStatuses(
    devices: List<Device>,
    wifiProfiles: List<WifiProfile>,
    snapshot: WifiProximitySnapshot
): Map<String, DeviceWifiProximityStatus> {
    val ssidByProfileId = wifiProfiles
        .filter { profile -> profile.ssid.isNotBlank() }
        .associate { profile -> profile.id to profile.ssid }

    return devices.associate { device ->
        val assignedSsids = device.connections.mapNotNull { connection ->
            ssidByProfileId[connection.wifiProfileId]
        }
        val status = when {
            assignedSsids.isEmpty() -> DeviceWifiProximityStatus.NO_ASSIGNMENT
            assignedSsids.any(snapshot.unavailableSsids::contains) -> {
                DeviceWifiProximityStatus.NOT_NEARBY
            }
            assignedSsids.any(snapshot.visibleSsids::contains) -> {
                DeviceWifiProximityStatus.NEARBY
            }
            snapshot.issue == WifiProximityIssue.WIFI_DISABLED -> {
                DeviceWifiProximityStatus.WIFI_DISABLED
            }
            snapshot.issue == WifiProximityIssue.LOCATION_SERVICES_DISABLED -> {
                DeviceWifiProximityStatus.LOCATION_SERVICES_DISABLED
            }
            snapshot.issue == WifiProximityIssue.PERMISSION_DENIED -> {
                DeviceWifiProximityStatus.PERMISSION_DENIED
            }
            snapshot.issue == WifiProximityIssue.SCAN_FAILED -> {
                DeviceWifiProximityStatus.SCAN_FAILED
            }
            else -> DeviceWifiProximityStatus.UNKNOWN
        }
        device.id to status
    }
}

private fun resolveDashboardWifiProximityStatuses(
    devices: List<Device>,
    switchGroups: List<SwitchGroup>,
    wifiProfiles: List<WifiProfile>,
    snapshot: WifiProximitySnapshot
): Map<String, DeviceWifiProximityStatus> {
    val deviceStatuses = resolveWifiProximityStatuses(
        devices = devices,
        wifiProfiles = wifiProfiles,
        snapshot = snapshot
    )
    val deviceIds = devices.map { it.id }.toSet()
    val groupStatuses = switchGroups.associate { group ->
        val memberStatuses = group.members.mapNotNull { member ->
            member.deviceId.takeIf(deviceIds::contains)?.let { deviceStatuses[it] }
        }
        val status = when {
            memberStatuses.isEmpty() -> DeviceWifiProximityStatus.NO_ASSIGNMENT
            memberStatuses.any { it.isNegativeWifiStatus() } -> {
                DeviceWifiProximityStatus.NOT_NEARBY
            }
            memberStatuses.all { it == DeviceWifiProximityStatus.NEARBY } -> {
                DeviceWifiProximityStatus.NEARBY
            }
            else -> DeviceWifiProximityStatus.UNKNOWN
        }
        DashboardItem.groupKey(group.id) to status
    }
    return deviceStatuses + groupStatuses
}

private fun DeviceWifiProximityStatus.isNegativeWifiStatus(): Boolean {
    return when (this) {
        DeviceWifiProximityStatus.NOT_NEARBY,
        DeviceWifiProximityStatus.WIFI_DISABLED,
        DeviceWifiProximityStatus.PERMISSION_DENIED,
        DeviceWifiProximityStatus.SCAN_FAILED -> true
        DeviceWifiProximityStatus.NEARBY,
        DeviceWifiProximityStatus.UNKNOWN,
        DeviceWifiProximityStatus.NO_ASSIGNMENT,
        DeviceWifiProximityStatus.LOCATION_SERVICES_DISABLED -> false
    }
}
