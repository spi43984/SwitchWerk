package de.piecha.switchwerk.viewmodel

import android.net.Uri
import de.piecha.switchwerk.R
import de.piecha.switchwerk.data.repository.ConfigurationImportMode
import de.piecha.switchwerk.data.repository.ConfigurationImportSummary
import de.piecha.switchwerk.data.repository.ConfigurationTransferRepository
import de.piecha.switchwerk.data.repository.DeviceRepository
import de.piecha.switchwerk.data.repository.FakeAppSettingsRepository
import de.piecha.switchwerk.data.repository.PreparedConfigurationImport
import de.piecha.switchwerk.data.repository.WifiProfileRepository
import de.piecha.switchwerk.data.network.WifiConnectionService
import de.piecha.switchwerk.data.transfer.CONFIGURATION_SCHEMA_VERSION
import de.piecha.switchwerk.data.transfer.ConfigurationDocument
import de.piecha.switchwerk.domain.model.ApiCall
import de.piecha.switchwerk.domain.model.ApiMethod
import de.piecha.switchwerk.domain.model.Device
import de.piecha.switchwerk.domain.model.DeviceConnection
import de.piecha.switchwerk.domain.model.AppThemeMode
import de.piecha.switchwerk.domain.model.AppLanguage
import de.piecha.switchwerk.domain.model.DetailPanelHeight
import de.piecha.switchwerk.domain.model.WifiProfile
import de.piecha.switchwerk.domain.model.WifiSecurityType
import de.piecha.switchwerk.domain.model.WifiConnectionMode
import de.piecha.switchwerk.ui.StringProvider
import de.piecha.switchwerk.ui.UiText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun validQrCodeUrlStartsUrlImport() = runTest(dispatcher) {
        val transferRepository = FakeConfigurationTransferRepository()
        val viewModel = settingsViewModel(transferRepository)
        runCurrent()

        viewModel.prepareImportFromQrCode(
            content = " https://example.com/switchwerk.json ",
            mode = ConfigurationImportMode.REPLACE
        )
        runCurrent()

        assertEquals("https://example.com/switchwerk.json", transferRepository.lastUrl)
        assertEquals(ConfigurationImportMode.REPLACE, transferRepository.lastMode)
        assertTrue(viewModel.uiState.value.importSummary != null)
    }

    @Test
    fun invalidQrCodeContentDoesNotStartImport() = runTest(dispatcher) {
        val transferRepository = FakeConfigurationTransferRepository()
        val viewModel = settingsViewModel(transferRepository)
        runCurrent()

        viewModel.prepareImportFromQrCode(
            content = "not a url",
            mode = ConfigurationImportMode.MERGE
        )
        runCurrent()

        assertEquals(null, transferRepository.lastUrl)
        assertFalse(viewModel.uiState.value.isTransferInProgress)
        assertEquals(
            R.string.error_invalid_qr_url,
            (viewModel.uiState.value.errorMessage as UiText.Resource).resourceId
        )
    }

    @Test
    fun blankWifiProfileNameIsRejected() = runTest(dispatcher) {
        val wifiRepository = FakeWifiProfileRepository()
        val viewModel = settingsViewModel(
            transferRepository = FakeConfigurationTransferRepository(),
            wifiProfileRepository = wifiRepository
        )
        runCurrent()

        viewModel.startNewWifiProfile()
        viewModel.updateWifiProfileSsid("Home")
        viewModel.updateWifiProfileName("")
        viewModel.saveWifiProfile()
        runCurrent()

        assertEquals(
            R.string.error_profile_name_empty,
            (viewModel.uiState.value.errorMessage as UiText.Resource).resourceId
        )
        assertTrue(wifiRepository.savedProfiles.isEmpty())
    }

    @Test
    fun newWifiProfileNameIsSuggestedFromSsid() = runTest(dispatcher) {
        val viewModel = settingsViewModel(
            transferRepository = FakeConfigurationTransferRepository(),
            wifiProfileRepository = FakeWifiProfileRepository(
                profiles = listOf(
                    WifiProfile(
                        id = "wifi-1",
                        name = "Shelly Garage",
                        ssid = "Shelly Garage"
                    )
                )
            )
        )
        runCurrent()

        viewModel.startNewWifiProfile()
        viewModel.updateWifiProfileSsid(" Shelly Garage ")

        assertEquals("Shelly Garage (2)", viewModel.uiState.value.form.name)
    }

    @Test
    fun androidManagedWifiProfileIsSavedWithoutPassword() = runTest(dispatcher) {
        val wifiRepository = FakeWifiProfileRepository()
        val viewModel = settingsViewModel(
            transferRepository = FakeConfigurationTransferRepository(),
            wifiProfileRepository = wifiRepository
        )
        runCurrent()

        viewModel.startNewWifiProfile()
        viewModel.updateWifiProfileSsid("Office")
        viewModel.updateWifiConnectionMode(WifiConnectionMode.ANDROID_MANAGED)
        viewModel.saveWifiProfile()
        runCurrent()

        assertEquals(WifiConnectionMode.ANDROID_MANAGED, wifiRepository.savedProfiles.single().connectionMode)
        assertTrue(wifiRepository.lastPasswordUpdate?.shouldUpdatePassword == true)
        assertEquals(null, wifiRepository.lastPasswordUpdate?.password)
    }

    @Test
    fun duplicateWifiProfileNameIsRejected() = runTest(dispatcher) {
        val wifiRepository = FakeWifiProfileRepository(
            profiles = listOf(
                WifiProfile(
                    id = "wifi-1",
                    name = "Zuhause",
                    ssid = "Home"
                )
            )
        )
        val viewModel = settingsViewModel(
            transferRepository = FakeConfigurationTransferRepository(),
            wifiProfileRepository = wifiRepository
        )
        runCurrent()

        viewModel.startNewWifiProfile()
        viewModel.updateWifiProfileName(" zuhause ")
        viewModel.updateWifiProfileSsid("Home 2")
        viewModel.saveWifiProfile()
        runCurrent()

        assertEquals(
            R.string.error_profile_name_duplicate,
            (viewModel.uiState.value.errorMessage as UiText.Resource).resourceId
        )
        assertTrue(wifiRepository.savedProfiles.isEmpty())
    }

    @Test
    fun deletingUsedWifiProfileListsAffectedDevicesAndRequiresConfirmation() = runTest(dispatcher) {
        val wifiRepository = FakeWifiProfileRepository(
            profiles = listOf(WifiProfile(id = "wifi-1", name = "Home", ssid = "Home"))
        )
        val deviceRepository = FakeDeviceRepository(
            devices = listOf(
                device(id = "device-1", name = "Garage", wifiProfileId = "wifi-1"),
                device(id = "device-2", name = "Garden", wifiProfileId = "wifi-2")
            )
        )
        val viewModel = settingsViewModel(
            transferRepository = FakeConfigurationTransferRepository(),
            wifiProfileRepository = wifiRepository,
            deviceRepository = deviceRepository
        )
        runCurrent()

        viewModel.requestWifiProfileDeletion("wifi-1")

        assertEquals(
            listOf("Garage"),
            viewModel.uiState.value.wifiProfileDeletionConfirmation?.affectedDeviceNames
        )
        assertTrue(wifiRepository.deletedProfileIds.isEmpty())

        viewModel.confirmWifiProfileDeletion()
        runCurrent()

        assertEquals(listOf("wifi-1"), wifiRepository.deletedProfileIds)
        assertEquals(null, viewModel.uiState.value.wifiProfileDeletionConfirmation)
    }

    @Test
    fun deletingUnusedWifiProfileDoesNotAddAffectedDeviceWarning() = runTest(dispatcher) {
        val viewModel = settingsViewModel(
            transferRepository = FakeConfigurationTransferRepository(),
            wifiProfileRepository = FakeWifiProfileRepository(
                profiles = listOf(WifiProfile(id = "wifi-1", name = "Home", ssid = "Home"))
            ),
            deviceRepository = FakeDeviceRepository(
                devices = listOf(device(id = "device-1", name = "Garage", wifiProfileId = "wifi-2"))
            )
        )
        runCurrent()

        viewModel.requestWifiProfileDeletion("wifi-1")

        assertTrue(
            viewModel.uiState.value.wifiProfileDeletionConfirmation?.affectedDeviceNames.orEmpty().isEmpty()
        )
    }

    @Test
    fun reorderedDeviceConnectionsAreSavedInDisplayedOrder() = runTest(dispatcher) {
        val profiles = listOf(
            WifiProfile(id = "wifi-1", name = "First", ssid = "First SSID"),
            WifiProfile(id = "wifi-2", name = "Second", ssid = "Second SSID")
        )
        val device = Device(
            id = "device-1",
            name = "Device",
            actionLabel = "Switch",
            apiCall = ApiCall(ApiMethod.GET, "/rpc/Switch.Toggle?id=0"),
            connections = listOf(
                DeviceConnection(wifiProfileId = "wifi-1", host = "first.local"),
                DeviceConnection(wifiProfileId = "wifi-2", host = "second.local")
            ),
            sortOrder = 0
        )
        val deviceRepository = FakeDeviceRepository(devices = listOf(device))
        val viewModel = settingsViewModel(
            transferRepository = FakeConfigurationTransferRepository(),
            wifiProfileRepository = FakeWifiProfileRepository(profiles),
            deviceRepository = deviceRepository
        )
        runCurrent()

        viewModel.startEditDevice(device)
        viewModel.moveDeviceConnection(wifiProfileId = "wifi-1", targetIndex = 1)

        assertEquals(
            listOf("wifi-2", "wifi-1"),
            viewModel.uiState.value.deviceForm.connections.map { it.wifiProfileId }
        )

        viewModel.saveDevice()
        runCurrent()

        assertEquals(
            listOf("wifi-2", "wifi-1"),
            deviceRepository.savedDevice?.connections?.map { it.wifiProfileId }
        )
    }

    @Test
    fun displaySettingsAreUpdatedThroughRepository() = runTest(dispatcher) {
        val appSettingsRepository = FakeAppSettingsRepository()
        val viewModel = settingsViewModel(
            transferRepository = FakeConfigurationTransferRepository(),
            appSettingsRepository = appSettingsRepository
        )
        runCurrent()

        viewModel.setThemeMode(AppThemeMode.DARK)
        viewModel.setLanguage(AppLanguage.ENGLISH)
        viewModel.setShowActionDetails(true)
        viewModel.setDetailPanelHeight(DetailPanelHeight.FORTY_PERCENT)
        viewModel.setDiagnosticsNewestFirst(false)
        runCurrent()

        assertEquals(AppThemeMode.DARK, viewModel.uiState.value.appSettings.themeMode)
        assertEquals(AppLanguage.ENGLISH, viewModel.uiState.value.appSettings.language)
        assertTrue(viewModel.uiState.value.appSettings.showActionDetails)
        assertEquals(
            DetailPanelHeight.FORTY_PERCENT,
            viewModel.uiState.value.appSettings.detailPanelHeight
        )
        assertFalse(viewModel.uiState.value.appSettings.diagnosticsNewestFirst)
    }

    private fun settingsViewModel(
        transferRepository: ConfigurationTransferRepository,
        wifiProfileRepository: WifiProfileRepository = FakeWifiProfileRepository(),
        appSettingsRepository: FakeAppSettingsRepository = FakeAppSettingsRepository(),
        deviceRepository: DeviceRepository = FakeDeviceRepository()
    ): SettingsViewModel {
        return SettingsViewModel(
            wifiProfileRepository = wifiProfileRepository,
            deviceRepository = deviceRepository,
            configurationTransferRepository = transferRepository,
            appSettingsRepository = appSettingsRepository,
            wifiConnectionService = object : WifiConnectionService {
                override suspend fun connect(
                    ssid: String,
                    password: String?,
                    securityType: WifiSecurityType,
                    timeoutMillis: Long,
                    onProgress: (de.piecha.switchwerk.data.network.WifiConnectionProgress) -> Unit
                ) = de.piecha.switchwerk.data.network.WifiConnectionResult.Unavailable

                override fun disconnect() = Unit
            },
            stringProvider = FakeStringProvider
        )
    }

    private object FakeStringProvider : StringProvider {
        override fun get(resourceId: Int, vararg arguments: Any): String = when (resourceId) {
            R.string.default_action_label -> "Schalten"
            R.string.unknown_wifi -> "Unbekanntes WLAN"
            else -> resourceId.toString()
        }
    }

    private fun device(id: String, name: String, wifiProfileId: String): Device {
        return Device(
            id = id,
            name = name,
            actionLabel = "Switch",
            apiCall = ApiCall(ApiMethod.GET, "/rpc/Switch.Toggle?id=0"),
            connections = listOf(DeviceConnection(wifiProfileId = wifiProfileId, host = "$id.local")),
            sortOrder = 0
        )
    }

    private class FakeWifiProfileRepository(
        private val profiles: List<WifiProfile> = emptyList()
    ) : WifiProfileRepository {
        val savedProfiles = mutableListOf<WifiProfile>()
        val deletedProfileIds = mutableListOf<String>()
        var lastPasswordUpdate: PasswordUpdate? = null

        override fun observeWifiProfiles(): Flow<List<WifiProfile>> = flowOf(profiles)

        override suspend fun getWifiProfiles(): List<WifiProfile> = profiles

        override suspend fun saveWifiProfile(
            profile: WifiProfile,
            password: String?,
            shouldUpdatePassword: Boolean
        ) {
            savedProfiles += profile
            lastPasswordUpdate = PasswordUpdate(password, shouldUpdatePassword)
        }

        override suspend fun getPassword(id: String): String? = null

        override suspend fun hasPassword(id: String): Boolean = false

        override suspend fun updateLastSuccessfulSecurityType(
            id: String,
            securityType: WifiSecurityType
        ) = Unit

        override suspend fun deletePassword(id: String) = Unit

        override suspend fun deleteWifiProfile(id: String) {
            deletedProfileIds += id
        }
    }

    private data class PasswordUpdate(
        val password: String?,
        val shouldUpdatePassword: Boolean
    )

    private class FakeDeviceRepository(
        private val devices: List<Device> = emptyList()
    ) : DeviceRepository {
        var savedDevice: Device? = null

        override fun observeDevices(): Flow<List<Device>> = flowOf(devices)

        override suspend fun getDevices(): List<Device> = devices

        override suspend fun saveDevice(device: Device) {
            savedDevice = device
        }

        override suspend fun updateDeviceOrder(deviceIds: List<String>) = Unit

        override suspend fun deleteDevice(deviceId: String) = Unit
    }

    private class FakeConfigurationTransferRepository : ConfigurationTransferRepository {
        var lastUrl: String? = null
        var lastMode: ConfigurationImportMode? = null

        override suspend fun exportToUri(uri: Uri, includePasswords: Boolean) = Unit

        override suspend fun prepareImportFromUri(
            uri: Uri,
            mode: ConfigurationImportMode
        ): PreparedConfigurationImport = preparedImport()

        override suspend fun prepareImportFromUrl(
            url: String,
            mode: ConfigurationImportMode
        ): PreparedConfigurationImport {
            lastUrl = url
            lastMode = mode
            return preparedImport()
        }

        override suspend fun applyImport(
            preparedImport: PreparedConfigurationImport,
            mode: ConfigurationImportMode
        ) = Unit

        private fun preparedImport(): PreparedConfigurationImport {
            return PreparedConfigurationImport(
                document = ConfigurationDocument(
                    schemaVersion = CONFIGURATION_SCHEMA_VERSION,
                    wifiProfiles = emptyList(),
                    devices = emptyList()
                ),
                summary = ConfigurationImportSummary(
                    wifiProfilesNew = 0,
                    wifiProfilesOverwritten = 0,
                    devicesNew = 0,
                    devicesOverwritten = 0,
                    passwordsIncluded = 0,
                    passwordsDeleted = 0,
                    localWifiProfilesDeleted = 0,
                    localDevicesDeleted = 0
                )
            )
        }
    }
}
