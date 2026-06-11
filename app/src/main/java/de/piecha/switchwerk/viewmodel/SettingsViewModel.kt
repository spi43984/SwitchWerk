package de.piecha.switchwerk.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.piecha.switchwerk.data.repository.DeviceRepository
import de.piecha.switchwerk.data.repository.WifiProfileRepository
import de.piecha.switchwerk.domain.model.ApiCall
import de.piecha.switchwerk.domain.model.ApiMethod
import de.piecha.switchwerk.domain.model.Device
import de.piecha.switchwerk.domain.model.DeviceConnection
import de.piecha.switchwerk.domain.model.WifiProfile
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class WifiProfileFormState(
    val id: String? = null,
    val ssid: String = "",
    val password: String = "",
    val hasSavedPassword: Boolean = false,
    val isPasswordVisible: Boolean = false,
    val isPasswordChanged: Boolean = false
)

data class DeviceConnectionFormState(
    val wifiProfileId: String,
    val ssid: String,
    val host: String = ""
)

data class DeviceFormState(
    val id: String? = null,
    val name: String = "",
    val actionLabel: String = "",
    val apiMethod: String = ApiMethod.GET.name,
    val apiPath: String = "",
    val sortOrder: String = "",
    val connections: List<DeviceConnectionFormState> = emptyList()
)

data class SettingsUiState(
    val wifiProfiles: List<WifiProfile> = emptyList(),
    val devices: List<Device> = emptyList(),
    val form: WifiProfileFormState = WifiProfileFormState(),
    val deviceForm: DeviceFormState = DeviceFormState(),
    val isEditingWifiProfile: Boolean = false,
    val isEditingDevice: Boolean = false,
    val errorMessage: String? = null
)

class SettingsViewModel(
    private val wifiProfileRepository: WifiProfileRepository,
    private val deviceRepository: DeviceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        observeWifiProfiles()
        observeDevices()
    }

    fun startNewWifiProfile() {
        _uiState.value = _uiState.value.copy(
            form = WifiProfileFormState(isPasswordChanged = true),
            isEditingWifiProfile = true,
            isEditingDevice = false,
            errorMessage = null
        )
    }

    fun startEditWifiProfile(profile: WifiProfile) {
        _uiState.value = _uiState.value.copy(
            form = WifiProfileFormState(id = profile.id, ssid = profile.ssid),
            isEditingWifiProfile = true,
            isEditingDevice = false,
            errorMessage = null
        )

        viewModelScope.launch {
            val savedPassword = runCatching {
                wifiProfileRepository.getPassword(profile.id)
            }.getOrNull()

            _uiState.value = _uiState.value.copy(
                form = _uiState.value.form.copy(
                    password = savedPassword?.takeIf { it.isNotEmpty() }?.let { PASSWORD_MASK }.orEmpty(),
                    hasSavedPassword = !savedPassword.isNullOrEmpty(),
                    isPasswordChanged = false
                )
            )
        }
    }

    fun cancelWifiProfileEdit() {
        _uiState.value = _uiState.value.copy(
            form = WifiProfileFormState(),
            isEditingWifiProfile = false,
            errorMessage = null
        )
    }

    fun updateWifiProfileSsid(ssid: String) {
        _uiState.value = _uiState.value.copy(
            form = _uiState.value.form.copy(ssid = ssid),
            errorMessage = null
        )
    }

    fun updateWifiProfilePassword(password: String) {
        _uiState.value = _uiState.value.copy(
            form = _uiState.value.form.copy(
                password = password,
                hasSavedPassword = false,
                isPasswordChanged = true
            ),
            errorMessage = null
        )
    }

    fun clearWifiProfilePassword() {
        _uiState.value = _uiState.value.copy(
            form = _uiState.value.form.copy(
                password = "",
                hasSavedPassword = false,
                isPasswordVisible = false,
                isPasswordChanged = true
            ),
            errorMessage = null
        )
    }

    fun toggleWifiPasswordVisibility() {
        val form = _uiState.value.form

        if (form.isPasswordVisible) {
            _uiState.value = _uiState.value.copy(
                form = form.copy(
                    password = if (form.hasSavedPassword && !form.isPasswordChanged) PASSWORD_MASK else form.password,
                    isPasswordVisible = false
                )
            )
            return
        }

        if (form.hasSavedPassword && !form.isPasswordChanged && form.id != null) {
            viewModelScope.launch {
                val savedPassword = runCatching {
                    wifiProfileRepository.getPassword(form.id)
                }.getOrNull()

                _uiState.value = _uiState.value.copy(
                    form = _uiState.value.form.copy(
                        password = savedPassword ?: PASSWORD_MASK,
                        isPasswordVisible = true
                    )
                )
            }
        } else {
            _uiState.value = _uiState.value.copy(
                form = form.copy(isPasswordVisible = true)
            )
        }
    }

    fun saveWifiProfile() {
        val form = _uiState.value.form
        val trimmedSsid = form.ssid.trim()

        if (trimmedSsid.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "SSID darf nicht leer sein")
            return
        }

        viewModelScope.launch {
            runCatching {
                val profile = WifiProfile(
                    id = form.id ?: UUID.randomUUID().toString(),
                    ssid = trimmedSsid
                )

                wifiProfileRepository.saveWifiProfile(
                    profile = profile,
                    password = if (form.isPasswordChanged) form.password.takeIf { it.isNotEmpty() } else null,
                    shouldUpdatePassword = form.isPasswordChanged
                )
            }.onSuccess {
                _uiState.value = _uiState.value.copy(
                    form = WifiProfileFormState(),
                    isEditingWifiProfile = false,
                    errorMessage = null
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    errorMessage = error.message ?: "WLAN-Profil konnte nicht gespeichert werden"
                )
            }
        }
    }

    fun deleteWifiProfile(profileId: String) {
        viewModelScope.launch {
            runCatching {
                wifiProfileRepository.deleteWifiProfile(profileId)
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    errorMessage = error.message ?: "WLAN-Profil konnte nicht gelöscht werden"
                )
            }
        }
    }

    fun startNewDevice() {
        val nextSortOrder = (_uiState.value.devices.maxOfOrNull { it.sortOrder } ?: 0) + 1

        _uiState.value = _uiState.value.copy(
            deviceForm = DeviceFormState(
                actionLabel = "Schalten",
                apiMethod = ApiMethod.GET.name,
                apiPath = "/rpc/Switch.Toggle?id=0",
                sortOrder = nextSortOrder.toString(),
                connections = buildConnectionForms(
                    wifiProfiles = _uiState.value.wifiProfiles,
                    device = null
                )
            ),
            isEditingDevice = true,
            isEditingWifiProfile = false,
            errorMessage = null
        )
    }

    fun startEditDevice(device: Device) {
        _uiState.value = _uiState.value.copy(
            deviceForm = DeviceFormState(
                id = device.id,
                name = device.name,
                actionLabel = device.actionLabel,
                apiMethod = device.apiCall.method.name,
                apiPath = device.apiCall.path,
                sortOrder = device.sortOrder.toString(),
                connections = buildConnectionForms(
                    wifiProfiles = _uiState.value.wifiProfiles,
                    device = device
                )
            ),
            isEditingDevice = true,
            isEditingWifiProfile = false,
            errorMessage = null
        )
    }

    fun cancelDeviceEdit() {
        _uiState.value = _uiState.value.copy(
            deviceForm = DeviceFormState(),
            isEditingDevice = false,
            errorMessage = null
        )
    }

    fun updateDeviceName(name: String) {
        updateDeviceForm { it.copy(name = name) }
    }

    fun updateDeviceActionLabel(actionLabel: String) {
        updateDeviceForm { it.copy(actionLabel = actionLabel) }
    }

    fun updateDeviceApiMethod(apiMethod: String) {
        updateDeviceForm { it.copy(apiMethod = apiMethod) }
    }

    fun updateDeviceApiPath(apiPath: String) {
        updateDeviceForm { it.copy(apiPath = apiPath) }
    }

    fun updateDeviceSortOrder(sortOrder: String) {
        updateDeviceForm { it.copy(sortOrder = sortOrder.filter { char -> char.isDigit() }) }
    }

    fun updateDeviceConnectionHost(wifiProfileId: String, host: String) {
        updateDeviceForm { form ->
            form.copy(
                connections = form.connections.map { connection ->
                    if (connection.wifiProfileId == wifiProfileId) {
                        connection.copy(host = host)
                    } else {
                        connection
                    }
                }
            )
        }
    }

    fun saveDevice() {
        val form = _uiState.value.deviceForm
        val trimmedName = form.name.trim()
        val trimmedActionLabel = form.actionLabel.trim()
        val trimmedApiPath = form.apiPath.trim()
        val sortOrder = form.sortOrder.toIntOrNull()

        if (trimmedName.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Gerätename darf nicht leer sein")
            return
        }

        if (trimmedActionLabel.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Button-Beschriftung darf nicht leer sein")
            return
        }

        if (trimmedApiPath.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "API-Aufruf darf nicht leer sein")
            return
        }

        if (sortOrder == null) {
            _uiState.value = _uiState.value.copy(errorMessage = "Sortierreihenfolge muss eine Zahl sein")
            return
        }

        val apiMethod = runCatching {
            ApiMethod.valueOf(form.apiMethod)
        }.getOrElse {
            _uiState.value = _uiState.value.copy(errorMessage = "API-Methode ist ungültig")
            return
        }

        val connections = form.connections
            .mapNotNull { connection ->
                val host = connection.host.trim()
                if (host.isBlank()) {
                    null
                } else {
                    DeviceConnection(
                        wifiProfileId = connection.wifiProfileId,
                        host = host
                    )
                }
            }

        viewModelScope.launch {
            runCatching {
                deviceRepository.saveDevice(
                    Device(
                        id = form.id ?: UUID.randomUUID().toString(),
                        name = trimmedName,
                        actionLabel = trimmedActionLabel,
                        apiCall = ApiCall(
                            method = apiMethod,
                            path = trimmedApiPath
                        ),
                        connections = connections,
                        sortOrder = sortOrder
                    )
                )
            }.onSuccess {
                _uiState.value = _uiState.value.copy(
                    deviceForm = DeviceFormState(),
                    isEditingDevice = false,
                    errorMessage = null
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    errorMessage = error.message ?: "Gerät konnte nicht gespeichert werden"
                )
            }
        }
    }

    fun deleteDevice(deviceId: String) {
        viewModelScope.launch {
            runCatching {
                deviceRepository.deleteDevice(deviceId)
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    errorMessage = error.message ?: "Gerät konnte nicht gelöscht werden"
                )
            }
        }
    }

    private fun observeWifiProfiles() {
        viewModelScope.launch {
            wifiProfileRepository.observeWifiProfiles().collect { profiles ->
                val current = _uiState.value
                _uiState.value = current.copy(
                    wifiProfiles = profiles,
                    deviceForm = if (current.isEditingDevice) {
                        current.deviceForm.copy(
                            connections = mergeConnectionForms(
                                profiles = profiles,
                                existing = current.deviceForm.connections
                            )
                        )
                    } else {
                        current.deviceForm
                    },
                    errorMessage = null
                )
            }
        }
    }

    private fun observeDevices() {
        viewModelScope.launch {
            deviceRepository.observeDevices().collect { devices ->
                _uiState.value = _uiState.value.copy(
                    devices = devices.sortedBy { it.sortOrder },
                    errorMessage = null
                )
            }
        }
    }

    private fun updateDeviceForm(update: (DeviceFormState) -> DeviceFormState) {
        _uiState.value = _uiState.value.copy(
            deviceForm = update(_uiState.value.deviceForm),
            errorMessage = null
        )
    }

    private fun buildConnectionForms(
        wifiProfiles: List<WifiProfile>,
        device: Device?
    ): List<DeviceConnectionFormState> {
        return wifiProfiles.map { profile ->
            val existingConnection = device?.connections?.firstOrNull {
                it.wifiProfileId == profile.id
            }

            DeviceConnectionFormState(
                wifiProfileId = profile.id,
                ssid = profile.ssid,
                host = existingConnection?.host.orEmpty()
            )
        }
    }

    private fun mergeConnectionForms(
        profiles: List<WifiProfile>,
        existing: List<DeviceConnectionFormState>
    ): List<DeviceConnectionFormState> {
        return profiles.map { profile ->
            val existingConnection = existing.firstOrNull {
                it.wifiProfileId == profile.id
            }

            DeviceConnectionFormState(
                wifiProfileId = profile.id,
                ssid = profile.ssid,
                host = existingConnection?.host.orEmpty()
            )
        }
    }

    private companion object {
        const val PASSWORD_MASK = "********"
    }
}
