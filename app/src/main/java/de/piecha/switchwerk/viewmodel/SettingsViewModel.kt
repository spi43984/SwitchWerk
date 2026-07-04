package de.piecha.switchwerk.viewmodel

import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.annotation.StringRes
import de.piecha.switchwerk.R
import de.piecha.switchwerk.data.repository.ConfigurationImportMode
import de.piecha.switchwerk.data.repository.AppSettingsRepository
import de.piecha.switchwerk.data.repository.ConfigurationImportSummary
import de.piecha.switchwerk.data.repository.ConfigurationTransferRepository
import de.piecha.switchwerk.data.repository.DeviceRepository
import de.piecha.switchwerk.data.repository.PreparedConfigurationImport
import de.piecha.switchwerk.data.repository.WifiProfileRepository
import de.piecha.switchwerk.data.network.WifiConnectionService
import de.piecha.switchwerk.data.update.AppUpdateInstallService
import de.piecha.switchwerk.data.update.AppUpdateRepository
import de.piecha.switchwerk.domain.model.ApiCall
import de.piecha.switchwerk.domain.model.ApiContentType
import de.piecha.switchwerk.domain.model.ApiMethod
import de.piecha.switchwerk.domain.model.AppSettings
import de.piecha.switchwerk.domain.model.AppUpdateCheckResult
import de.piecha.switchwerk.domain.model.AppUpdateDownloadState
import de.piecha.switchwerk.domain.model.AppUpdateError
import de.piecha.switchwerk.domain.model.AppUpdateSnapshot
import de.piecha.switchwerk.domain.model.AppLanguage
import de.piecha.switchwerk.domain.model.AppThemeMode
import de.piecha.switchwerk.domain.model.Device
import de.piecha.switchwerk.domain.model.DeviceConnection
import de.piecha.switchwerk.domain.model.DeviceProtocol
import de.piecha.switchwerk.domain.model.DetailPanelHeight
import de.piecha.switchwerk.domain.model.WifiProfile
import de.piecha.switchwerk.domain.model.WifiConnectionMode
import de.piecha.switchwerk.domain.model.WifiProfileSortCriterion
import de.piecha.switchwerk.domain.model.WifiProfileSortDirection
import de.piecha.switchwerk.ui.StringProvider
import de.piecha.switchwerk.ui.UiText
import de.piecha.switchwerk.ui.uiText
import java.net.URI
import java.util.UUID
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class WifiProfileFormState(
    val id: String? = null,
    val name: String = "",
    val ssid: String = "",
    val password: String = "",
    val hasSavedPassword: Boolean = false,
    val isPasswordVisible: Boolean = false,
    val isPasswordChanged: Boolean = false,
    val connectionMode: WifiConnectionMode = WifiConnectionMode.SWITCHWERK_MANAGED,
    val visibleSsids: List<String> = emptyList()
)

data class DeviceConnectionFormState(
    val wifiProfileId: String,
    val wifiProfileName: String,
    val ssid: String,
    val host: String = ""
)

data class DeviceFormState(
    val id: String? = null,
    val name: String = "",
    val actionLabel: String = "",
    val apiProtocol: String = DeviceProtocol.HTTP.name,
    val apiMethod: String = ApiMethod.GET.name,
    val apiPath: String = "",
    val apiRequestBody: String = "",
    val apiContentType: String = ApiContentType.APPLICATION_JSON.name,
    val connections: List<DeviceConnectionFormState> = emptyList(),
    val shortcutEnabled: Boolean = false
)

data class WifiProfileDeletionConfirmation(
    val profile: WifiProfile,
    val affectedDeviceNames: List<String>
)

data class SettingsUiState(
    val wifiProfiles: List<WifiProfile> = emptyList(),
    val devices: List<Device> = emptyList(),
    val form: WifiProfileFormState = WifiProfileFormState(),
    val deviceForm: DeviceFormState = DeviceFormState(),
    val isEditingWifiProfile: Boolean = false,
    val isEditingDevice: Boolean = false,
    val wifiProfileDeletionConfirmation: WifiProfileDeletionConfirmation? = null,
    val errorMessage: UiText? = null,
    val statusMessage: UiText? = null,
    val isTransferInProgress: Boolean = false,
    val importSummary: ConfigurationImportSummary? = null,
    val importMode: ConfigurationImportMode? = null,
    val appSettings: AppSettings = AppSettings(),
    val updateSnapshot: AppUpdateSnapshot? = null,
    val updateDownloadState: AppUpdateDownloadState = AppUpdateDownloadState.Idle,
    val isUpdateCheckInProgress: Boolean = false
)

class SettingsViewModel(
    private val wifiProfileRepository: WifiProfileRepository,
    private val deviceRepository: DeviceRepository,
    private val configurationTransferRepository: ConfigurationTransferRepository,
    private val appSettingsRepository: AppSettingsRepository,
    private val wifiConnectionService: WifiConnectionService,
    private val stringProvider: StringProvider,
    private val appUpdateRepository: AppUpdateRepository,
    private val appUpdateInstallService: AppUpdateInstallService
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SettingsUiState(appSettings = appSettingsRepository.settings.value)
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    private val _installEvents = MutableSharedFlow<Intent>()
    val installEvents: SharedFlow<Intent> = _installEvents.asSharedFlow()
    private val _releasePageEvents = MutableSharedFlow<Intent>()
    val releasePageEvents: SharedFlow<Intent> = _releasePageEvents.asSharedFlow()
    private var pendingImport: PreparedConfigurationImport? = null

    init {
        observeWifiProfiles()
        observeDevices()
        observeAppSettings()
        loadCachedUpdateState()
    }

    fun setThemeMode(themeMode: AppThemeMode) {
        appSettingsRepository.setThemeMode(themeMode)
    }

    fun setLanguage(language: AppLanguage) {
        appSettingsRepository.setLanguage(language)
    }

    fun setShowActionDetails(showActionDetails: Boolean) {
        appSettingsRepository.setShowActionDetails(showActionDetails)
    }

    fun setDetailPanelHeight(detailPanelHeight: DetailPanelHeight) {
        appSettingsRepository.setDetailPanelHeight(detailPanelHeight)
    }

    fun setDiagnosticsNewestFirst(diagnosticsNewestFirst: Boolean) {
        appSettingsRepository.setDiagnosticsNewestFirst(diagnosticsNewestFirst)
    }

    fun setWifiProfileSorting(
        criterion: WifiProfileSortCriterion,
        direction: WifiProfileSortDirection
    ) {
        appSettingsRepository.setWifiProfileSorting(criterion, direction)
    }

    fun showSetupWizardAgain() {
        appSettingsRepository.setShowSetupWizardOnStart(true)
    }

    fun checkForUpdatesManually() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isUpdateCheckInProgress = true,
                errorMessage = null,
                statusMessage = null
            )
            when (val result = appUpdateRepository.checkForUpdates(force = true)) {
                is AppUpdateCheckResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        updateSnapshot = result.snapshot,
                        isUpdateCheckInProgress = false,
                        statusMessage = uiText(R.string.update_check_completed)
                    )
                }
                is AppUpdateCheckResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        updateSnapshot = result.snapshot,
                        isUpdateCheckInProgress = false,
                        errorMessage = null
                    )
                }
            }
        }
    }

    fun downloadUpdate() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                updateDownloadState = AppUpdateDownloadState.Started,
                errorMessage = null,
                statusMessage = uiText(R.string.update_download_started)
            )
            val result = appUpdateRepository.downloadUpdate { progress ->
                _uiState.value = _uiState.value.copy(updateDownloadState = progress)
            }
            _uiState.value = when (result) {
                is AppUpdateDownloadState.Completed -> _uiState.value.copy(
                    updateSnapshot = appUpdateRepository.cachedUpdate(),
                    updateDownloadState = result,
                    statusMessage = uiText(R.string.update_download_completed),
                    errorMessage = null
                )
                is AppUpdateDownloadState.Failed -> _uiState.value.copy(
                    updateSnapshot = appUpdateRepository.cachedUpdate(),
                    updateDownloadState = result,
                    statusMessage = null,
                    errorMessage = null
                )
                else -> _uiState.value.copy(updateDownloadState = result)
            }
        }
    }

    fun installDownloadedUpdate() {
        val uri = when (val state = _uiState.value.updateDownloadState) {
            is AppUpdateDownloadState.Completed -> state.apkUri
            else -> _uiState.value.updateSnapshot?.downloadedApkUri
        } ?: run {
            _uiState.value = _uiState.value.copy(errorMessage = AppUpdateError.Install.toUpdateUiText())
            return
        }

        viewModelScope.launch {
            appUpdateInstallService.installIntent(uri)
                .onSuccess { intent -> _installEvents.emit(intent) }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = AppUpdateError.Install.toUpdateUiText()
                    )
                }
        }
    }

    fun openAvailableReleasePage() {
        val htmlUrl = _uiState.value.updateSnapshot
            ?.availableRelease
            ?.htmlUrl
            ?.takeIf { it.isNotBlank() } ?: return

        viewModelScope.launch {
            _releasePageEvents.emit(
                Intent(Intent.ACTION_VIEW, Uri.parse(htmlUrl))
                    .addCategory(Intent.CATEGORY_BROWSABLE)
            )
        }
    }

    fun reportUpdateInstallFailed() {
        _uiState.value = _uiState.value.copy(
            errorMessage = AppUpdateError.Install.toUpdateUiText()
        )
    }

    fun reportReleasePageOpenFailed() {
        _uiState.value = _uiState.value.copy(
            errorMessage = uiText(R.string.update_error_open_release)
        )
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
            form = WifiProfileFormState(
                id = profile.id,
                name = profile.name,
                ssid = profile.ssid,
                connectionMode = profile.connectionMode
            ),
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

    fun updateWifiProfileName(name: String) {
        _uiState.value = _uiState.value.copy(
            form = _uiState.value.form.copy(name = name),
            errorMessage = null
        )
    }

    fun updateWifiProfileSsid(ssid: String) {
        val form = _uiState.value.form
        val shouldSuggestName = form.id == null &&
            (form.name.isBlank() || form.name.trim() == form.ssid.trim())
        _uiState.value = _uiState.value.copy(
            form = form.copy(
                ssid = ssid,
                name = if (shouldSuggestName) uniqueWifiProfileName(ssid.trim()) else form.name
            ),
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

    fun updateWifiConnectionMode(connectionMode: WifiConnectionMode) {
        val form = _uiState.value.form
        _uiState.value = _uiState.value.copy(
            form = form.copy(
                connectionMode = connectionMode,
                password = if (connectionMode == WifiConnectionMode.ANDROID_MANAGED) "" else form.password,
                hasSavedPassword = if (connectionMode == WifiConnectionMode.ANDROID_MANAGED) false else form.hasSavedPassword,
                isPasswordVisible = false,
                isPasswordChanged = form.isPasswordChanged ||
                    connectionMode == WifiConnectionMode.ANDROID_MANAGED
            ),
            errorMessage = null
        )
    }

    fun loadVisibleSsids() {
        viewModelScope.launch {
            val visibleSsids = runCatching { wifiConnectionService.visibleSsids() }
                .getOrDefault(emptySet())
                .sorted()
            _uiState.value = _uiState.value.copy(
                form = _uiState.value.form.copy(visibleSsids = visibleSsids)
            )
        }
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
        val trimmedName = form.name.trim()
        val trimmedSsid = form.ssid.trim()

        if (trimmedName.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = uiText(R.string.error_profile_name_empty))
            return
        }

        if (trimmedSsid.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = uiText(R.string.error_ssid_empty))
            return
        }

        if (isWifiProfileNameUsed(trimmedName, form.id)) {
            _uiState.value = _uiState.value.copy(errorMessage = uiText(R.string.error_profile_name_duplicate))
            return
        }

        viewModelScope.launch {
            runCatching {
                val existingProfile = form.id?.let { id ->
                    _uiState.value.wifiProfiles.firstOrNull { it.id == id }
                }
                val profile = WifiProfile(
                    id = form.id ?: UUID.randomUUID().toString(),
                    name = trimmedName,
                    ssid = trimmedSsid,
                    connectionMode = form.connectionMode,
                    lastSuccessfulSecurityType = existingProfile?.lastSuccessfulSecurityType
                )

                wifiProfileRepository.saveWifiProfile(
                    profile = profile,
                    password = if (form.isPasswordChanged) form.password.takeIf { it.isNotEmpty() } else null,
                    shouldUpdatePassword = form.isPasswordChanged ||
                        form.connectionMode == WifiConnectionMode.ANDROID_MANAGED
                )
            }.onSuccess {
                _uiState.value = _uiState.value.copy(
                    form = WifiProfileFormState(),
                    isEditingWifiProfile = false,
                    errorMessage = null
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    errorMessage = error.toUiText(R.string.error_wifi_profile_save)
                )
            }
        }
    }

    fun requestWifiProfileDeletion(profileId: String) {
        val profile = _uiState.value.wifiProfiles.firstOrNull { it.id == profileId } ?: return
        val affectedDeviceNames = _uiState.value.devices
            .filter { device -> device.connections.any { it.wifiProfileId == profileId } }
            .map { it.name }

        _uiState.value = _uiState.value.copy(
            wifiProfileDeletionConfirmation = WifiProfileDeletionConfirmation(
                profile = profile,
                affectedDeviceNames = affectedDeviceNames
            ),
            errorMessage = null
        )
    }

    fun cancelWifiProfileDeletion() {
        _uiState.value = _uiState.value.copy(wifiProfileDeletionConfirmation = null)
    }

    fun confirmWifiProfileDeletion() {
        val confirmation = _uiState.value.wifiProfileDeletionConfirmation ?: return
        _uiState.value = _uiState.value.copy(wifiProfileDeletionConfirmation = null)

        viewModelScope.launch {
            runCatching {
                wifiProfileRepository.deleteWifiProfile(confirmation.profile.id)
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    errorMessage = error.toUiText(R.string.error_wifi_profile_delete)
                )
            }
        }
    }

    fun exportConfiguration(uri: Uri, includePasswords: Boolean) {
        runTransfer(
            successMessage = uiText(R.string.configuration_exported)
        ) {
            configurationTransferRepository.exportToUri(
                uri = uri,
                includePasswords = includePasswords
            )
        }
    }

    fun prepareImportFromFile(uri: Uri, mode: ConfigurationImportMode) {
        prepareImport(mode) {
            configurationTransferRepository.prepareImportFromUri(
                uri = uri,
                mode = mode
            )
        }
    }

    fun prepareImportFromUrl(url: String, mode: ConfigurationImportMode) {
        prepareImport(mode) {
            configurationTransferRepository.prepareImportFromUrl(
                url = url,
                mode = mode
            )
        }
    }

    fun prepareImportFromQrCode(content: String, mode: ConfigurationImportMode) {
        val url = content.trim()
        if (!url.isValidImportUrl()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = uiText(R.string.error_invalid_qr_url),
                statusMessage = null
            )
            return
        }

        prepareImportFromUrl(url, mode)
    }

    fun reportQrCameraPermissionDenied() {
        _uiState.value = _uiState.value.copy(
            errorMessage = uiText(R.string.error_camera_permission),
            statusMessage = null
        )
    }

    fun reportQrScanCancelled() {
        _uiState.value = _uiState.value.copy(
            statusMessage = null
        )
    }

    fun updateImportMode(mode: ConfigurationImportMode) {
        val prepared = pendingImport ?: return
        _uiState.value = _uiState.value.copy(
            importMode = mode,
            importSummary = prepared.summaryFor(mode)
        )
    }

    fun confirmImport(includePasswords: Boolean) {
        applyPendingImport(includePasswords)
    }

    fun cancelPendingImport() {
        pendingImport = null
        _uiState.value = _uiState.value.copy(
            importSummary = null,
            importMode = null
        )
    }

    fun clearStatusMessage() {
        _uiState.value = _uiState.value.copy(statusMessage = null)
    }

    fun startNewDevice() {
        _uiState.value = _uiState.value.copy(
            deviceForm = DeviceFormState(
                actionLabel = stringProvider.get(R.string.default_action_label),
                apiMethod = ApiMethod.GET.name,
                apiPath = "/rpc/Switch.Toggle?id=0"
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
                apiProtocol = device.protocol.name,
                apiMethod = device.apiCall.method.name,
                apiPath = device.apiCall.path,
                apiRequestBody = device.apiCall.requestBody,
                apiContentType = device.apiCall.contentType.name,
                shortcutEnabled = device.shortcutEnabled,
                connections = device.connections.map { connection ->
                    val profile = _uiState.value.wifiProfiles.firstOrNull {
                        it.id == connection.wifiProfileId
                    }
                    DeviceConnectionFormState(
                        wifiProfileId = connection.wifiProfileId,
                        wifiProfileName = profile?.name ?: stringProvider.get(R.string.unknown_wifi),
                        ssid = profile?.ssid ?: stringProvider.get(R.string.unknown_wifi),
                        host = connection.host
                    )
                }
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

    fun updateDeviceShortcutEnabled(enabled: Boolean) {
        updateDeviceForm { it.copy(shortcutEnabled = enabled) }
    }

    fun updateDeviceApiMethod(apiMethod: String) {
        updateDeviceForm { it.copy(apiMethod = apiMethod) }
    }

    fun updateDeviceApiProtocol(apiProtocol: String) {
        updateDeviceForm { it.copy(apiProtocol = apiProtocol) }
    }

    fun updateDeviceApiPath(apiPath: String) {
        updateDeviceForm { it.copy(apiPath = apiPath) }
    }

    fun updateDeviceApiRequestBody(apiRequestBody: String) {
        updateDeviceForm { it.copy(apiRequestBody = apiRequestBody) }
    }

    fun updateDeviceApiContentType(apiContentType: String) {
        updateDeviceForm { it.copy(apiContentType = apiContentType) }
    }

    fun addDeviceConnection(wifiProfileId: String, host: String) {
        val profile = _uiState.value.wifiProfiles.firstOrNull { it.id == wifiProfileId } ?: return
        val trimmedHost = host.trim()

        if (trimmedHost.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = uiText(R.string.error_host_empty))
            return
        }

        val form = _uiState.value.deviceForm
        if (form.connections.any { it.wifiProfileId == wifiProfileId }) {
            _uiState.value = _uiState.value.copy(errorMessage = uiText(R.string.error_wifi_already_assigned))
            return
        }

        updateDeviceForm {
            it.copy(
                connections = it.connections + DeviceConnectionFormState(
                    wifiProfileId = profile.id,
                    wifiProfileName = profile.name,
                    ssid = profile.ssid,
                    host = trimmedHost
                )
            )
        }
    }

    fun updateDeviceConnection(
        oldWifiProfileId: String,
        newWifiProfileId: String,
        host: String
    ) {
        val profile = _uiState.value.wifiProfiles.firstOrNull { it.id == newWifiProfileId } ?: return
        val trimmedHost = host.trim()

        if (trimmedHost.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = uiText(R.string.error_host_empty))
            return
        }

        val form = _uiState.value.deviceForm
        if (oldWifiProfileId != newWifiProfileId && form.connections.any { it.wifiProfileId == newWifiProfileId }) {
            _uiState.value = _uiState.value.copy(errorMessage = uiText(R.string.error_wifi_already_assigned))
            return
        }

        updateDeviceForm {
            it.copy(
                connections = it.connections.map { connection ->
                    if (connection.wifiProfileId == oldWifiProfileId) {
                        DeviceConnectionFormState(
                            wifiProfileId = profile.id,
                            wifiProfileName = profile.name,
                            ssid = profile.ssid,
                            host = trimmedHost
                        )
                    } else {
                        connection
                    }
                }
            )
        }
    }

    fun deleteDeviceConnection(wifiProfileId: String) {
        updateDeviceForm {
            it.copy(
                connections = it.connections.filterNot { connection ->
                    connection.wifiProfileId == wifiProfileId
                }
            )
        }
    }

    fun moveDeviceConnection(wifiProfileId: String, targetIndex: Int) {
        val connections = _uiState.value.deviceForm.connections
        val currentIndex = connections.indexOfFirst { it.wifiProfileId == wifiProfileId }

        if (currentIndex !in connections.indices || targetIndex !in connections.indices) {
            return
        }

        updateDeviceForm { form ->
            form.copy(
                connections = connections.toMutableList().apply {
                    add(targetIndex, removeAt(currentIndex))
                }
            )
        }
    }

    fun saveDevice() {
        val form = _uiState.value.deviceForm
        val trimmedName = form.name.trim()
        val trimmedActionLabel = form.actionLabel.trim()
        val trimmedApiPath = form.apiPath.trim()

        if (trimmedName.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = uiText(R.string.error_device_name_empty))
            return
        }

        if (trimmedActionLabel.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = uiText(R.string.error_button_label_empty))
            return
        }

        if (trimmedApiPath.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = uiText(R.string.error_api_call_empty))
            return
        }

        val apiMethod = runCatching {
            ApiMethod.valueOf(form.apiMethod)
        }.getOrElse {
            _uiState.value = _uiState.value.copy(errorMessage = uiText(R.string.error_api_method_invalid))
            return
        }

        val apiProtocol = runCatching {
            DeviceProtocol.valueOf(form.apiProtocol)
        }.getOrElse {
            _uiState.value = _uiState.value.copy(errorMessage = uiText(R.string.error_api_protocol_invalid))
            return
        }

        val apiContentType = runCatching {
            ApiContentType.valueOf(form.apiContentType)
        }.getOrElse {
            _uiState.value = _uiState.value.copy(errorMessage = uiText(R.string.error_api_content_type_invalid))
            return
        }

        val sortOrder = form.id?.let { existingId ->
            _uiState.value.devices.firstOrNull { it.id == existingId }?.sortOrder
        } ?: ((_uiState.value.devices.maxOfOrNull { it.sortOrder } ?: 0) + 1)

        viewModelScope.launch {
            runCatching {
                deviceRepository.saveDevice(
                    Device(
                        id = form.id ?: UUID.randomUUID().toString(),
                        name = trimmedName,
                        actionLabel = trimmedActionLabel,
                        protocol = apiProtocol,
                        apiCall = ApiCall(
                            method = apiMethod,
                            path = trimmedApiPath,
                            requestBody = form.apiRequestBody,
                            contentType = apiContentType
                        ),
                        connections = form.connections.map {
                            DeviceConnection(
                                wifiProfileId = it.wifiProfileId,
                                host = it.host.trim()
                            )
                        },
                        sortOrder = sortOrder,
                        shortcutEnabled = form.shortcutEnabled
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
                    errorMessage = error.toUiText(R.string.error_device_save)
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
                    errorMessage = error.toUiText(R.string.error_device_delete)
                )
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

        viewModelScope.launch {
            runCatching {
                val reorderedDevices = devices.toMutableList().apply {
                    add(targetIndex, removeAt(currentIndex))
                }
                deviceRepository.updateDeviceOrder(reorderedDevices.map { it.id })
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    errorMessage = error.toUiText(R.string.error_device_order)
                )
            }
        }
    }

    private fun observeWifiProfiles() {
        viewModelScope.launch {
            wifiProfileRepository.observeWifiProfiles().collect { profiles ->
                val current = _uiState.value
                _uiState.value = current.copy(
                    wifiProfiles = sortWifiProfiles(
                        profiles,
                        current.appSettings.wifiProfileSortCriterion,
                        current.appSettings.wifiProfileSortDirection
                    ),
                    deviceForm = current.deviceForm.copy(
                        connections = current.deviceForm.connections.mapNotNull { connection ->
                            val profile = profiles.firstOrNull { it.id == connection.wifiProfileId }
                            profile?.let {
                                connection.copy(
                                    wifiProfileName = it.name,
                                    ssid = it.ssid
                                )
                            }
                        }
                    ),
                    errorMessage = null
                )
            }
        }
    }

    private fun prepareImport(
        mode: ConfigurationImportMode,
        load: suspend () -> PreparedConfigurationImport
    ) {
        if (_uiState.value.isTransferInProgress) {
            return
        }
        _uiState.value = _uiState.value.copy(
            isTransferInProgress = true,
            importSummary = null,
            errorMessage = null,
            statusMessage = null
        )
        viewModelScope.launch {
            runCatching { load() }
                .onSuccess { prepared ->
                    pendingImport = prepared
                    _uiState.value = _uiState.value.copy(
                        isTransferInProgress = false,
                        importSummary = prepared.summary,
                        importMode = mode
                    )
                }
                .onFailure { error ->
                    pendingImport = null
                    _uiState.value = _uiState.value.copy(
                        isTransferInProgress = false,
                        errorMessage = error.toUiText(R.string.error_import_prepare)
                    )
                }
        }
    }

    private fun applyPendingImport(includePasswords: Boolean) {
        val prepared = pendingImport ?: return
        val mode = _uiState.value.importMode ?: return
        pendingImport = null
        _uiState.value = _uiState.value.copy(
            isTransferInProgress = true,
            importSummary = null,
            errorMessage = null,
            statusMessage = null
        )
        viewModelScope.launch {
            runCatching {
                configurationTransferRepository.applyImport(
                    preparedImport = prepared,
                    mode = mode,
                    includePasswords = includePasswords
                )
            }.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isTransferInProgress = false,
                    importMode = null,
                    statusMessage = uiText(R.string.configuration_imported)
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isTransferInProgress = false,
                    importMode = null,
                    errorMessage = error.toUiText(R.string.error_configuration_import)
                )
            }
        }
    }

    private fun runTransfer(
        successMessage: UiText,
        transfer: suspend () -> Unit
    ) {
        if (_uiState.value.isTransferInProgress) {
            return
        }
        _uiState.value = _uiState.value.copy(
            isTransferInProgress = true,
            errorMessage = null,
            statusMessage = null
        )
        viewModelScope.launch {
            runCatching { transfer() }
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isTransferInProgress = false,
                        statusMessage = successMessage
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isTransferInProgress = false,
                        errorMessage = error.toUiText(R.string.error_operation_failed)
                    )
                }
        }
    }

    private fun observeDevices() {
        viewModelScope.launch {
            deviceRepository.observeDevices().collect { devices ->
                _uiState.value = _uiState.value.copy(
                    devices = devices.sortedWith(
                        compareBy<Device, String>(String.CASE_INSENSITIVE_ORDER) { it.name }
                            .thenBy { it.id }
                    ),
                    errorMessage = null
                )
            }
        }
    }

    private fun observeAppSettings() {
        viewModelScope.launch {
            appSettingsRepository.settings.collect { settings ->
                _uiState.value = _uiState.value.copy(
                    appSettings = settings,
                    wifiProfiles = sortWifiProfiles(
                        _uiState.value.wifiProfiles,
                        settings.wifiProfileSortCriterion,
                        settings.wifiProfileSortDirection
                    )
                )
            }
        }
    }

    private fun loadCachedUpdateState() {
        _uiState.value = _uiState.value.copy(
            updateSnapshot = appUpdateRepository.cachedUpdate()
        )
    }

    private fun AppUpdateError?.toUpdateUiText(): UiText = when (this) {
        AppUpdateError.DebugBuild -> uiText(R.string.update_error_debug_build)
        AppUpdateError.NoRegularRelease -> uiText(R.string.update_error_no_regular_release)
        AppUpdateError.MissingApkAsset -> uiText(R.string.update_error_missing_apk)
        AppUpdateError.AmbiguousApkAsset -> uiText(R.string.update_error_ambiguous_apk)
        AppUpdateError.InvalidReleaseData -> uiText(R.string.update_error_invalid_release)
        AppUpdateError.Network -> uiText(R.string.update_error_network)
        AppUpdateError.GitHub -> uiText(R.string.update_error_github)
        AppUpdateError.Download -> uiText(R.string.update_error_download)
        AppUpdateError.Install -> uiText(R.string.update_error_install)
        null -> uiText(R.string.update_error_unknown)
    }

    private fun sortWifiProfiles(
        profiles: List<WifiProfile>,
        criterion: WifiProfileSortCriterion,
        direction: WifiProfileSortDirection
    ): List<WifiProfile> {
        val valueComparator = String.CASE_INSENSITIVE_ORDER
        val primaryComparator = compareBy<WifiProfile, String>(valueComparator) {
                when (criterion) {
                    WifiProfileSortCriterion.PROFILE_NAME -> it.name
                    WifiProfileSortCriterion.SSID -> it.ssid
                }
            }
        val comparator = if (direction == WifiProfileSortDirection.DESCENDING) {
            primaryComparator.reversed()
        } else {
            primaryComparator
        }
        return profiles.sortedWith(comparator.thenBy { it.id })
    }

    private fun updateDeviceForm(update: (DeviceFormState) -> DeviceFormState) {
        _uiState.value = _uiState.value.copy(
            deviceForm = update(_uiState.value.deviceForm),
            errorMessage = null
        )
    }

    private fun String.isValidImportUrl(): Boolean {
        val uri = runCatching { URI(this) }.getOrNull() ?: return false
        return (uri.scheme.equals("http", ignoreCase = true) ||
            uri.scheme.equals("https", ignoreCase = true)) &&
            !uri.host.isNullOrBlank()
    }

    private fun Throwable.toUiText(@StringRes fallbackResourceId: Int): UiText =
        message?.takeIf(String::isNotBlank)?.let(UiText::Dynamic)
            ?: uiText(fallbackResourceId)

    private fun isWifiProfileNameUsed(name: String, currentProfileId: String?): Boolean {
        return _uiState.value.wifiProfiles.any { profile ->
            profile.id != currentProfileId && profile.name.equals(name, ignoreCase = true)
        }
    }

    private fun uniqueWifiProfileName(baseName: String): String {
        if (baseName.isBlank()) {
            return ""
        }

        var candidate = baseName
        var suffix = 2
        while (isWifiProfileNameUsed(candidate, currentProfileId = null)) {
            candidate = "$baseName ($suffix)"
            suffix += 1
        }
        return candidate
    }

    private companion object {
        const val PASSWORD_MASK = "********"
    }
}
