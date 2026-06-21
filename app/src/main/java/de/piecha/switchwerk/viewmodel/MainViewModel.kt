package de.piecha.switchwerk.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    data class Success(val message: String) : DeviceActionUiState

    data class Error(val message: String) : DeviceActionUiState
}

sealed interface DiagnosticListItem {
    data class Message(val text: String) : DiagnosticListItem

    data object Separator : DiagnosticListItem
}

data class MainUiState(
    val devices: List<Device> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
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
                    errorMessage = error.message ?: "Geräte konnten nicht geladen werden"
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
                    errorMessage = error.message ?: "Gerätereihenfolge konnte nicht geändert werden"
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

    private fun appendDiagnosticMessage(message: String, elapsedMillis: Long) {
        val timestamp = SimpleDateFormat(DIAGNOSTIC_TIMESTAMP_PATTERN, Locale.GERMANY)
            .format(Date())
        _uiState.value = _uiState.value.copy(
            diagnosticItems = (
                _uiState.value.diagnosticItems + DiagnosticListItem.Message(
                    "$timestamp (+${elapsedMillis} ms) $message"
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

    private fun DeviceActionDiagnosticEvent.toUserMessage(deviceName: String): String {
        return when (this) {
            DeviceActionDiagnosticEvent.ActionStarted -> "Geräteaktion „$deviceName“ gestartet"
            is DeviceActionDiagnosticEvent.WifiRequestStarted -> {
                "WLAN-Anforderung für Profil „$profileName“ gestartet"
            }
            DeviceActionDiagnosticEvent.WifiSecurityDetectionStarted -> {
                "WLAN-Sicherheitstyp wird ermittelt"
            }
            DeviceActionDiagnosticEvent.WifiSecurityDetectionSucceeded -> {
                "WLAN-Sicherheitstyp erkannt"
            }
            DeviceActionDiagnosticEvent.WifiSecurityDetectionUnavailable -> {
                "WLAN-Sicherheitstyp nicht ermittelbar; Fallback wird verwendet"
            }
            DeviceActionDiagnosticEvent.WifiFound -> "WLAN gefunden"
            DeviceActionDiagnosticEvent.WifiConnected -> "WLAN verbunden"
            DeviceActionDiagnosticEvent.IpAddressReceived -> "IP-Adresse erhalten"
            DeviceActionDiagnosticEvent.WifiConnectionFailed -> {
                "WLAN-Verbindung fehlgeschlagen"
            }
            DeviceActionDiagnosticEvent.WifiDisabled -> "WLAN ist deaktiviert"
            is DeviceActionDiagnosticEvent.DeviceAddress -> {
                "Geräteadresse: $address"
            }
            is DeviceActionDiagnosticEvent.HttpRequestStarted -> when (method) {
                ApiMethod.GET -> "HTTP/RPC GET an $address gestartet"
                ApiMethod.POST -> "HTTP/RPC POST an $address gestartet"
            }
            is DeviceActionDiagnosticEvent.HttpResponseReceived -> {
                "HTTP/RPC-Antwort erhalten: Status $statusCode"
            }
            is DeviceActionDiagnosticEvent.HttpRequestSucceeded -> {
                "HTTP/RPC-Aufruf erfolgreich: Status $statusCode"
            }
            is DeviceActionDiagnosticEvent.DnsResolutionStarted -> {
                "DNS-Auflösung für $address gestartet"
            }
            DeviceActionDiagnosticEvent.DnsResolutionSucceeded -> "DNS-Auflösung erfolgreich"
            DeviceActionDiagnosticEvent.DnsResolutionFailed -> {
                "DNS-Name des Geräts konnte nicht aufgelöst werden"
            }
            DeviceActionDiagnosticEvent.DeviceNotReachable -> {
                "IP-Adresse des Geräts ist nicht erreichbar"
            }
            DeviceActionDiagnosticEvent.RequestSucceeded -> "Gerät erfolgreich geschaltet"
            DeviceActionDiagnosticEvent.RequestFailed -> "Anfrage fehlgeschlagen"
            is DeviceActionDiagnosticEvent.Timeout -> when (stage) {
                DiagnosticStage.WIFI_REQUEST -> {
                    "Timeout bei der WLAN-Anforderung; keine Verbindung hergestellt"
                }
                DiagnosticStage.WIFI -> {
                    "Timeout beim WLAN-Verbindungsaufbau"
                }
                DiagnosticStage.DNS -> {
                    "Timeout bei der DNS-Auflösung"
                }
                DiagnosticStage.HTTP -> {
                    "Timeout beim HTTP/RPC-Aufruf"
                }
            }
            DeviceActionDiagnosticEvent.ActionCompleted -> "Geräteaktion abgeschlossen"
        }
    }

    private fun DeviceActionResult.toUiState(): DeviceActionUiState {
        return when (this) {
            DeviceActionResult.Success -> DeviceActionUiState.Success("Erfolg")
            DeviceActionResult.NoConnections -> {
                DeviceActionUiState.Error("Kein WLAN für Gerät konfiguriert")
            }

            DeviceActionResult.WifiConnectionFailed -> {
                DeviceActionUiState.Error("Zugeordnetes WLAN nicht erreichbar")
            }

            DeviceActionResult.WifiPermissionDenied -> {
                DeviceActionUiState.Error("WLAN-Verbindung nicht erlaubt")
            }

            DeviceActionResult.WifiDisabled -> {
                DeviceActionUiState.Error("WLAN ist deaktiviert")
            }

            DeviceActionResult.UnsupportedAndroidVersion -> {
                DeviceActionUiState.Error("Android unterstützt WLAN-Verbindung nicht")
            }

            is DeviceActionResult.HttpError -> {
                DeviceActionUiState.Error("Gerät meldet HTTP-Fehler $statusCode")
            }

            DeviceActionResult.Timeout -> {
                DeviceActionUiState.Error("Zeitüberschreitung beim Schalten")
            }

            DeviceActionResult.InvalidRequest -> {
                DeviceActionUiState.Error("Gespeicherte Geräteadresse ungültig")
            }

            is DeviceActionResult.NetworkError -> {
                val detail = when (reason) {
                    NetworkFailureReason.DNS -> "Gerätename nicht auflösbar"
                    NetworkFailureReason.CONNECTION -> "Gerät im Netzwerk nicht erreichbar"
                    NetworkFailureReason.NO_ROUTE -> "Keine Netzwerkroute zum Gerät"
                    NetworkFailureReason.VPN_BLOCKED -> {
                        "VPN oder Firewall blockiert Zugriff"
                    }
                    NetworkFailureReason.OTHER -> "Netzwerkzugriff zum Gerät fehlgeschlagen"
                }
                DeviceActionUiState.Error(detail)
            }

            DeviceActionResult.UnexpectedError -> {
                DeviceActionUiState.Error("Geräteaktion fehlgeschlagen")
            }
        }
    }

    private companion object {
        const val DIAGNOSTIC_TIMESTAMP_PATTERN = "HH:mm:ss.SSS"
        const val MAX_DIAGNOSTIC_MESSAGES = 200
        const val NANOS_PER_MILLISECOND = 1_000_000L
        const val ACTION_SUCCESS_DISPLAY_MILLIS = 2_000L
        const val ACTION_ERROR_DISPLAY_MILLIS = 4_000L
    }
}
