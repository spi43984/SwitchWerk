package de.piecha.switchwerk.viewmodel

import android.net.Uri
import de.piecha.switchwerk.data.repository.ConfigurationImportMode
import de.piecha.switchwerk.data.repository.ConfigurationImportSummary
import de.piecha.switchwerk.data.repository.ConfigurationTransferRepository
import de.piecha.switchwerk.data.repository.DeviceRepository
import de.piecha.switchwerk.data.repository.PreparedConfigurationImport
import de.piecha.switchwerk.data.repository.WifiProfileRepository
import de.piecha.switchwerk.data.transfer.CONFIGURATION_SCHEMA_VERSION
import de.piecha.switchwerk.data.transfer.ConfigurationDocument
import de.piecha.switchwerk.domain.model.Device
import de.piecha.switchwerk.domain.model.WifiProfile
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
        assertEquals("QR-Code enthält keine gültige HTTPS-URL", viewModel.uiState.value.errorMessage)
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

        assertEquals("Profilname darf nicht leer sein", viewModel.uiState.value.errorMessage)
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

        assertEquals("Profilname ist bereits vergeben", viewModel.uiState.value.errorMessage)
        assertTrue(wifiRepository.savedProfiles.isEmpty())
    }

    private fun settingsViewModel(
        transferRepository: ConfigurationTransferRepository,
        wifiProfileRepository: WifiProfileRepository = FakeWifiProfileRepository()
    ): SettingsViewModel {
        return SettingsViewModel(
            wifiProfileRepository = wifiProfileRepository,
            deviceRepository = FakeDeviceRepository(),
            configurationTransferRepository = transferRepository
        )
    }

    private class FakeWifiProfileRepository(
        private val profiles: List<WifiProfile> = emptyList()
    ) : WifiProfileRepository {
        val savedProfiles = mutableListOf<WifiProfile>()

        override fun observeWifiProfiles(): Flow<List<WifiProfile>> = flowOf(profiles)

        override suspend fun getWifiProfiles(): List<WifiProfile> = profiles

        override suspend fun saveWifiProfile(
            profile: WifiProfile,
            password: String?,
            shouldUpdatePassword: Boolean
        ) {
            savedProfiles += profile
        }

        override suspend fun getPassword(id: String): String? = null

        override suspend fun hasPassword(id: String): Boolean = false

        override suspend fun deletePassword(id: String) = Unit

        override suspend fun deleteWifiProfile(id: String) = Unit
    }

    private class FakeDeviceRepository : DeviceRepository {
        override fun observeDevices(): Flow<List<Device>> = flowOf(emptyList())

        override suspend fun getDevices(): List<Device> = emptyList()

        override suspend fun saveDevice(device: Device) = Unit

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
