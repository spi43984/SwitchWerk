package de.piecha.switchwerk.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.piecha.switchwerk.R
import de.piecha.switchwerk.data.action.DeviceActionResult
import de.piecha.switchwerk.data.action.DeviceActionDiagnosticEvent
import de.piecha.switchwerk.data.action.DeviceActionService
import de.piecha.switchwerk.data.action.DiagnosticStage
import de.piecha.switchwerk.data.action.NetworkFailureReason
import de.piecha.switchwerk.data.repository.AppSettingsRepository
import de.piecha.switchwerk.data.repository.DeviceRepository
import de.piecha.switchwerk.domain.model.ApiMethod
import de.piecha.switchwerk.domain.model.AppSettings
import de.piecha.switchwerk.domain.model.DashboardLayoutMode
import de.piecha.switchwerk.domain.model.Device
import de.piecha.switchwerk.ui.UiText
import de.piecha.switchwerk.ui.uiText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface DeviceActionUiState {
    data object Loading : DeviceActionUiState

    data class Success(val message: UiText) : DeviceActionUiState

    data class Error(val message: UiText) : DeviceActionUiState
}

sealed interface DiagnosticListItem {
    data class Message(val text: UiText) : DiagnosticListItem

    data object Separator : DiagnosticListItem
}

data class MainUiState(
    val devices: List<Device> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: UiText? = null,
    val deviceActionStates: Map<String, DeviceActionUiState> = emptyMap(),
    val appSettings: AppSettings = AppSettings(),
    val diagnosticItems: List<DiagnosticListItem> = emptyList()
)

class MainViewModel(
    private val repository: DeviceRepository,
    private val deviceActionService: DeviceActionService,
    private val appSettingsRepository: AppSettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        MainUiState(
            isLoading = true,
            appSettings = appSettingsRepository.settings.value
        )
    )
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()
    private val actionJobs = mutableMapOf<String, Job>()
    private val actionStateResetJobs = mutableMapOf<String, Job>()

    init {
        observeDevices()
        observeAppSettings()
    }

    private fun observeDevices() {
        viewModelScope.launch {
            runCatching {
                repository.observeDevices().collect { devices ->
                    _uiState.value = _uiState.value.copy(
                        devices = devices.sortedBy { it.sortOrder },
                        isLoading = false,
                        errorMessage = null
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

    fun executeDeviceAction(device: Device) {
        if (actionJobs[device.id]?.isActive == true) {
            return
        }

        actionStateResetJobs.remove(device.id)?.cancel()
        updateDeviceActionState(device.id, DeviceActionUiState.Loading)
        actionJobs[device.id] = viewModelScope.launch {
            var previousDiagnosticAtNanos: Long? = null
            val result = deviceActionService.execute(device) { event ->
                val nowNanos = System.nanoTime()
                val elapsedMillis = previousDiagnosticAtNanos?.let { previous ->
                    ((nowNanos - previous) / NANOS_PER_MILLISECOND).coerceAtLeast(0L)
                } ?: 0L
                previousDiagnosticAtNanos = nowNanos
                if (event == DeviceActionDiagnosticEvent.ActionStarted) {
                    appendActionSeparator()
                }
                appendDiagnosticMessage(
                    message = event.toUserMessage(device.name),
                    elapsedMillis = elapsedMillis
                )
            }
            val resultState = result.toUiState()
            updateDeviceActionState(device.id, resultState)
            when (resultState) {
                is DeviceActionUiState.Success -> scheduleActionStateReset(
                    deviceId = device.id,
                    state = resultState,
                    delayMillis = ACTION_SUCCESS_DISPLAY_MILLIS
                )
                is DeviceActionUiState.Error -> scheduleActionStateReset(
                    deviceId = device.id,
                    state = resultState,
                    delayMillis = ACTION_ERROR_DISPLAY_MILLIS
                )
                DeviceActionUiState.Loading -> Unit
            }
        }.also { job ->
            job.invokeOnCompletion {
                actionJobs.remove(device.id, job)
            }
        }
    }

    fun moveDeviceUp(deviceId: String) {
        moveDevice(deviceId = deviceId, offset = -1)
    }

    fun moveDeviceDown(deviceId: String) {
        moveDevice(deviceId = deviceId, offset = 1)
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
        _uiState.value = _uiState.value.copy(diagnosticItems = emptyList())
    }

    private fun moveDevice(deviceId: String, offset: Int) {
        val devices = _uiState.value.devices.sortedBy { it.sortOrder }
        val currentIndex = devices.indexOfFirst { it.id == deviceId }
        val targetIndex = currentIndex + offset

        if (currentIndex !in devices.indices || targetIndex !in devices.indices) {
            return
        }

        val reorderedDevices = devices.toMutableList().apply {
            add(targetIndex, removeAt(currentIndex))
        }

        viewModelScope.launch {
            runCatching {
                repository.updateDeviceOrder(reorderedDevices.map { it.id })
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    errorMessage = error.toUiText(R.string.error_device_order)
                )
            }
        }
    }

    private fun updateDeviceActionState(deviceId: String, state: DeviceActionUiState) {
        _uiState.value = _uiState.value.copy(
            deviceActionStates = _uiState.value.deviceActionStates + (deviceId to state)
        )
    }

    private fun scheduleActionStateReset(
        deviceId: String,
        state: DeviceActionUiState,
        delayMillis: Long
    ) {
        actionStateResetJobs[deviceId] = viewModelScope.launch {
            delay(delayMillis)
            if (_uiState.value.deviceActionStates[deviceId] == state) {
                _uiState.value = _uiState.value.copy(
                    deviceActionStates = _uiState.value.deviceActionStates - deviceId
                )
            }
        }.also { job ->
            job.invokeOnCompletion {
                actionStateResetJobs.remove(deviceId, job)
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

    private fun appendDiagnosticMessage(message: UiText, elapsedMillis: Long) {
        val timestamp = SimpleDateFormat(DIAGNOSTIC_TIMESTAMP_PATTERN, Locale.getDefault())
            .format(Date())
        _uiState.value = _uiState.value.copy(
            diagnosticItems = (
                _uiState.value.diagnosticItems + DiagnosticListItem.Message(
                    uiText(R.string.diagnostic_entry, timestamp, elapsedMillis, message)
                )
            )
                .takeLast(MAX_DIAGNOSTIC_MESSAGES)
        )
    }

    private fun appendActionSeparator() {
        if (_uiState.value.diagnosticItems.isEmpty()) {
            return
        }
        _uiState.value = _uiState.value.copy(
            diagnosticItems = (_uiState.value.diagnosticItems + DiagnosticListItem.Separator)
                .takeLast(MAX_DIAGNOSTIC_MESSAGES)
        )
    }

    private fun DeviceActionDiagnosticEvent.toUserMessage(deviceName: String): UiText {
        return when (this) {
            DeviceActionDiagnosticEvent.ActionStarted ->
                uiText(R.string.diagnostic_action_started, deviceName)
            is DeviceActionDiagnosticEvent.WifiRequestStarted -> {
                uiText(R.string.diagnostic_wifi_request_started, profileName)
            }
            DeviceActionDiagnosticEvent.WifiSecurityDetectionStarted -> {
                uiText(R.string.diagnostic_wifi_security_started)
            }
            DeviceActionDiagnosticEvent.WifiSecurityDetectionSucceeded -> {
                uiText(R.string.diagnostic_wifi_security_succeeded)
            }
            DeviceActionDiagnosticEvent.WifiSecurityDetectionUnavailable -> {
                uiText(R.string.diagnostic_wifi_security_unavailable)
            }
            DeviceActionDiagnosticEvent.WifiFound -> uiText(R.string.diagnostic_wifi_found)
            DeviceActionDiagnosticEvent.WifiConnected -> uiText(R.string.diagnostic_wifi_connected)
            DeviceActionDiagnosticEvent.IpAddressReceived -> uiText(R.string.diagnostic_ip_received)
            DeviceActionDiagnosticEvent.WifiConnectionFailed -> {
                uiText(R.string.diagnostic_wifi_failed)
            }
            DeviceActionDiagnosticEvent.WifiDisabled -> uiText(R.string.diagnostic_wifi_disabled)
            is DeviceActionDiagnosticEvent.DeviceAddress -> {
                uiText(R.string.diagnostic_device_address, address)
            }
            is DeviceActionDiagnosticEvent.HttpRequestStarted -> when (method) {
                ApiMethod.GET -> uiText(R.string.diagnostic_http_get_started, address)
                ApiMethod.POST -> uiText(R.string.diagnostic_http_post_started, address)
            }
            is DeviceActionDiagnosticEvent.HttpResponseReceived -> {
                uiText(R.string.diagnostic_http_response, statusCode)
            }
            is DeviceActionDiagnosticEvent.HttpRequestSucceeded -> {
                uiText(R.string.diagnostic_http_success, statusCode)
            }
            is DeviceActionDiagnosticEvent.DnsResolutionStarted -> {
                uiText(R.string.diagnostic_dns_started, address)
            }
            DeviceActionDiagnosticEvent.DnsResolutionSucceeded ->
                uiText(R.string.diagnostic_dns_succeeded)
            DeviceActionDiagnosticEvent.DnsResolutionFailed -> {
                uiText(R.string.diagnostic_dns_failed)
            }
            DeviceActionDiagnosticEvent.DeviceNotReachable -> {
                uiText(R.string.diagnostic_device_unreachable)
            }
            DeviceActionDiagnosticEvent.RequestSucceeded ->
                uiText(R.string.diagnostic_request_succeeded)
            DeviceActionDiagnosticEvent.RequestFailed -> uiText(R.string.diagnostic_request_failed)
            is DeviceActionDiagnosticEvent.Timeout -> when (stage) {
                DiagnosticStage.WIFI_REQUEST -> {
                    uiText(R.string.diagnostic_timeout_wifi_request)
                }
                DiagnosticStage.WIFI -> {
                    uiText(R.string.diagnostic_timeout_wifi)
                }
                DiagnosticStage.DNS -> {
                    uiText(R.string.diagnostic_timeout_dns)
                }
                DiagnosticStage.HTTP -> {
                    uiText(R.string.diagnostic_timeout_http)
                }
            }
            DeviceActionDiagnosticEvent.ActionCompleted ->
                uiText(R.string.diagnostic_action_completed)
        }
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

            DeviceActionResult.WifiPermissionDenied -> {
                DeviceActionUiState.Error(uiText(R.string.action_wifi_permission_denied))
            }

            DeviceActionResult.WifiDisabled -> {
                DeviceActionUiState.Error(uiText(R.string.action_wifi_disabled))
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

            is DeviceActionResult.NetworkError -> {
                val detail = when (reason) {
                    NetworkFailureReason.DNS -> uiText(R.string.action_dns_error)
                    NetworkFailureReason.CONNECTION -> uiText(R.string.action_connection_error)
                    NetworkFailureReason.NO_ROUTE -> uiText(R.string.action_no_route)
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

    private fun Throwable.toUiText(fallbackResourceId: Int): UiText =
        message?.takeIf(String::isNotBlank)?.let(UiText::Dynamic)
            ?: uiText(fallbackResourceId)

    private companion object {
        const val DIAGNOSTIC_TIMESTAMP_PATTERN = "HH:mm:ss.SSS"
        const val MAX_DIAGNOSTIC_MESSAGES = 200
        const val NANOS_PER_MILLISECOND = 1_000_000L
        const val ACTION_SUCCESS_DISPLAY_MILLIS = 2_000L
        const val ACTION_ERROR_DISPLAY_MILLIS = 4_000L
    }
}
