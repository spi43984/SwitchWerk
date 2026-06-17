package de.piecha.switchwerk.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.piecha.switchwerk.data.action.DeviceActionResult
import de.piecha.switchwerk.data.action.DeviceActionService
import de.piecha.switchwerk.data.action.NetworkFailureReason
import de.piecha.switchwerk.data.repository.DeviceRepository
import de.piecha.switchwerk.domain.model.Device
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface DeviceActionUiState {
    data object Loading : DeviceActionUiState

    data class Success(val message: String) : DeviceActionUiState

    data class Error(val message: String) : DeviceActionUiState
}

data class MainUiState(
    val devices: List<Device> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val deviceActionStates: Map<String, DeviceActionUiState> = emptyMap()
)

class MainViewModel(
    private val repository: DeviceRepository,
    private val deviceActionService: DeviceActionService
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState(isLoading = true))
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()
    private val actionJobs = mutableMapOf<String, Job>()

    init {
        observeDevices()
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

        updateDeviceActionState(device.id, DeviceActionUiState.Loading)
        actionJobs[device.id] = viewModelScope.launch {
            val result = deviceActionService.execute(device)
            updateDeviceActionState(device.id, result.toUiState())
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

    private fun DeviceActionResult.toUiState(): DeviceActionUiState {
        return when (this) {
            DeviceActionResult.Success -> DeviceActionUiState.Success("Aktion erfolgreich ausgeführt")
            DeviceActionResult.NoConnections -> {
                DeviceActionUiState.Error("Für dieses Gerät ist kein WLAN konfiguriert")
            }

            DeviceActionResult.WifiConnectionFailed -> {
                DeviceActionUiState.Error("Keines der zugeordneten WLANs ist erreichbar")
            }

            DeviceActionResult.WifiPermissionDenied -> {
                DeviceActionUiState.Error("WLAN-Verbindung wurde nicht erlaubt")
            }

            DeviceActionResult.UnsupportedAndroidVersion -> {
                DeviceActionUiState.Error("Diese Android-Version unterstützt die WLAN-Verbindung nicht")
            }

            is DeviceActionResult.HttpError -> {
                DeviceActionUiState.Error("Gerät antwortet mit HTTP-Status $statusCode")
            }

            DeviceActionResult.Timeout -> {
                DeviceActionUiState.Error("Zeitüberschreitung; die Aktion wird nicht wiederholt")
            }

            DeviceActionResult.InvalidRequest -> {
                DeviceActionUiState.Error("Die gespeicherte Geräteadresse ist ungültig")
            }

            is DeviceActionResult.NetworkError -> {
                val detail = when (reason) {
                    NetworkFailureReason.DNS -> "Der Gerätename konnte nicht aufgelöst werden"
                    NetworkFailureReason.CONNECTION -> "Port 80 des Geräts ist nicht erreichbar"
                    NetworkFailureReason.NO_ROUTE -> "Es besteht keine Netzwerkroute zum Gerät"
                    NetworkFailureReason.VPN_BLOCKED -> {
                        "VPN oder Firewall blockiert den lokalen Netzwerkzugriff"
                    }
                    NetworkFailureReason.OTHER -> "Der gebundene Netzwerkzugriff ist fehlgeschlagen"
                }
                DeviceActionUiState.Error(detail)
            }

            DeviceActionResult.UnexpectedError -> {
                DeviceActionUiState.Error("Die Geräteaktion ist fehlgeschlagen")
            }
        }
    }
}
