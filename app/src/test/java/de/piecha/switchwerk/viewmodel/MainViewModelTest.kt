package de.piecha.switchwerk.viewmodel

import de.piecha.switchwerk.R
import de.piecha.switchwerk.data.action.DeviceActionResult
import de.piecha.switchwerk.data.action.DeviceActionDiagnosticEvent
import de.piecha.switchwerk.data.action.DeviceActionService
import de.piecha.switchwerk.data.action.SwitchGroupActionResult
import de.piecha.switchwerk.data.action.SwitchGroupActionService
import de.piecha.switchwerk.data.action.SwitchGroupDiagnosticEvent
import de.piecha.switchwerk.data.repository.DeviceRepository
import de.piecha.switchwerk.data.repository.FakeAppSettingsRepository
import de.piecha.switchwerk.data.repository.FakeWifiProfileRepository
import de.piecha.switchwerk.data.repository.SwitchGroupRepository
import de.piecha.switchwerk.data.network.WifiProximityIssue
import de.piecha.switchwerk.data.network.WifiProximityService
import de.piecha.switchwerk.data.network.WifiProximitySnapshot
import de.piecha.switchwerk.data.update.FakeAppUpdateRepository
import de.piecha.switchwerk.domain.model.ApiCall
import de.piecha.switchwerk.domain.model.ApiMethod
import de.piecha.switchwerk.domain.model.AppSettings
import de.piecha.switchwerk.domain.model.DashboardLayoutMode
import de.piecha.switchwerk.domain.model.Device
import de.piecha.switchwerk.domain.model.DeviceConnection
import de.piecha.switchwerk.domain.model.SwitchGroup
import de.piecha.switchwerk.domain.model.SwitchGroupMember
import de.piecha.switchwerk.domain.model.WifiProfile
import de.piecha.switchwerk.ui.UiText
import de.piecha.switchwerk.intent.ExternalDeviceActionIntentResult
import de.piecha.switchwerk.intent.ExternalSwitchGroupActionIntentResult
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

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
    fun externalActionIsRejectedWhenSettingIsDisabled() = runTest(dispatcher) {
        val actionService = WaitingDeviceActionService()
        val viewModel = MainViewModel(
            repository = FakeDeviceRepository(listOf(device(id = "device-1", sortOrder = 0))),
            deviceActionService = actionService,
            appSettingsRepository = FakeAppSettingsRepository(),
            wifiProfileRepository = FakeWifiProfileRepository(),
            wifiProximityService = FixedWifiProximityService(),
            appUpdateRepository = FakeAppUpdateRepository()
        )
        runCurrent()

        viewModel.handleExternalDeviceAction(
            ExternalDeviceActionIntentResult.Valid("device-1")
        )
        runCurrent()

        assertEquals(0, actionService.callCount)
        assertEquals(null, viewModel.uiState.value.errorMessage)
        val actionError = viewModel.uiState.value.deviceActionStates["device-1"]
            as DeviceActionUiState.Error
        assertEquals(
            R.string.external_intent_disabled_error,
            (actionError.message as UiText.Resource).resourceId
        )
        assertEquals(1, viewModel.uiState.value.diagnosticItems.size)

        advanceTimeBy(4_001L)
        runCurrent()

        assertEquals(null, viewModel.uiState.value.deviceActionStates["device-1"])
        assertEquals(1, viewModel.uiState.value.diagnosticItems.size)
    }

    @Test
    fun unassignedExternalIntentErrorIsTemporaryButRemainsInDiagnostics() =
        runTest(dispatcher) {
            val viewModel = MainViewModel(
                repository = FakeDeviceRepository(emptyList()),
                deviceActionService = WaitingDeviceActionService(),
                appSettingsRepository = FakeAppSettingsRepository(
                    AppSettings(externalIntentsEnabled = true)
                ),
                wifiProfileRepository = FakeWifiProfileRepository(),
                wifiProximityService = FixedWifiProximityService(),
                appUpdateRepository = FakeAppUpdateRepository()
            )
            runCurrent()

            viewModel.handleExternalDeviceAction(
                ExternalDeviceActionIntentResult.MissingDeviceId
            )

            assertEquals(
                R.string.external_intent_missing_device_id_error,
                (viewModel.uiState.value.errorMessage as UiText.Resource).resourceId
            )
            assertEquals(1, viewModel.uiState.value.diagnosticItems.size)

            advanceTimeBy(4_001L)
            runCurrent()

            assertEquals(null, viewModel.uiState.value.errorMessage)
            assertEquals(1, viewModel.uiState.value.diagnosticItems.size)
        }

    @Test
    fun enabledExternalActionUsesExistingActionService() = runTest(dispatcher) {
        val actionService = WaitingDeviceActionService()
        val viewModel = MainViewModel(
            repository = FakeDeviceRepository(listOf(device(id = "device-1", sortOrder = 0))),
            deviceActionService = actionService,
            appSettingsRepository = FakeAppSettingsRepository(
                AppSettings(externalIntentsEnabled = true)
            ),
            wifiProfileRepository = FakeWifiProfileRepository(),
            wifiProximityService = FixedWifiProximityService(),
            appUpdateRepository = FakeAppUpdateRepository()
        )
        runCurrent()

        viewModel.handleExternalDeviceAction(
            ExternalDeviceActionIntentResult.Valid("device-1")
        )
        runCurrent()

        assertEquals(1, actionService.callCount)
        assertEquals(DeviceActionUiState.Loading, viewModel.uiState.value.deviceActionStates["device-1"])
    }

    @Test
    fun enabledExternalGroupActionUsesExistingGroupActionService() = runTest(dispatcher) {
        val groupActionService = WaitingSwitchGroupActionService()
        val group = switchGroup(id = "group-1", memberCount = 1)
        val viewModel = MainViewModel(
            repository = FakeDeviceRepository(listOf(device(id = "device-1", sortOrder = 0))),
            switchGroupRepository = FakeSwitchGroupRepository(listOf(group)),
            deviceActionService = WaitingDeviceActionService(),
            switchGroupActionService = groupActionService,
            appSettingsRepository = FakeAppSettingsRepository(
                AppSettings(externalIntentsEnabled = true)
            ),
            wifiProfileRepository = FakeWifiProfileRepository(),
            wifiProximityService = FixedWifiProximityService(),
            appUpdateRepository = FakeAppUpdateRepository()
        )
        runCurrent()

        viewModel.handleExternalSwitchGroupAction(
            ExternalSwitchGroupActionIntentResult.Valid("group-1")
        )
        runCurrent()

        assertEquals(1, groupActionService.callCount)
        assertEquals(
            DeviceActionUiState.Loading,
            viewModel.uiState.value.deviceActionStates[DashboardItem.groupKey("group-1")]
        )
    }

    @Test
    fun externalGroupActionRejectsEmptyGroup() = runTest(dispatcher) {
        val group = switchGroup(id = "group-1", memberCount = 0)
        val viewModel = MainViewModel(
            repository = FakeDeviceRepository(emptyList()),
            switchGroupRepository = FakeSwitchGroupRepository(listOf(group)),
            deviceActionService = WaitingDeviceActionService(),
            switchGroupActionService = WaitingSwitchGroupActionService(),
            appSettingsRepository = FakeAppSettingsRepository(
                AppSettings(externalIntentsEnabled = true)
            ),
            wifiProfileRepository = FakeWifiProfileRepository(),
            wifiProximityService = FixedWifiProximityService(),
            appUpdateRepository = FakeAppUpdateRepository()
        )
        runCurrent()

        viewModel.handleExternalSwitchGroupAction(
            ExternalSwitchGroupActionIntentResult.Valid("group-1")
        )
        runCurrent()

        val actionError = viewModel.uiState.value
            .deviceActionStates[DashboardItem.groupKey("group-1")] as DeviceActionUiState.Error
        assertEquals(
            R.string.external_intent_empty_group_error,
            (actionError.message as UiText.Resource).resourceId
        )
    }

    @Test
    fun externalGroupActionRejectsUnknownGroup() = runTest(dispatcher) {
        val viewModel = MainViewModel(
            repository = FakeDeviceRepository(emptyList()),
            switchGroupRepository = FakeSwitchGroupRepository(emptyList()),
            deviceActionService = WaitingDeviceActionService(),
            switchGroupActionService = WaitingSwitchGroupActionService(),
            appSettingsRepository = FakeAppSettingsRepository(
                AppSettings(externalIntentsEnabled = true)
            ),
            wifiProfileRepository = FakeWifiProfileRepository(),
            wifiProximityService = FixedWifiProximityService(),
            appUpdateRepository = FakeAppUpdateRepository()
        )
        runCurrent()

        viewModel.handleExternalSwitchGroupAction(
            ExternalSwitchGroupActionIntentResult.Valid("group-1")
        )

        assertEquals(
            R.string.external_intent_unknown_group_error,
            (viewModel.uiState.value.errorMessage as UiText.Resource).resourceId
        )
    }

    @Test
    fun repeatedClickDoesNotStartSecondActionForSameDevice() = runTest(dispatcher) {
        val device = device(id = "device-1", sortOrder = 0)
        val actionService = WaitingDeviceActionService()
        val viewModel = MainViewModel(
            repository = FakeDeviceRepository(listOf(device)),
            deviceActionService = actionService,
            appSettingsRepository = FakeAppSettingsRepository(),
            wifiProfileRepository = FakeWifiProfileRepository(),
            wifiProximityService = FixedWifiProximityService(),
            appUpdateRepository = FakeAppUpdateRepository()
        )
        runCurrent()

        viewModel.executeDeviceAction(device)
        viewModel.executeDeviceAction(device)
        runCurrent()

        assertEquals(1, actionService.callCount)
        assertEquals(DeviceActionUiState.Loading, viewModel.uiState.value.deviceActionStates[device.id])

        actionService.result.complete(DeviceActionResult.Success)
        runCurrent()

        assertTrue(viewModel.uiState.value.deviceActionStates[device.id] is DeviceActionUiState.Success)
        val firstDiagnostic = viewModel.uiState.value.diagnosticItems
            .filterIsInstance<DiagnosticListItem.Message>()
            .first()
            .text as UiText.Resource
        assertEquals(R.string.diagnostic_entry, firstDiagnostic.resourceId)
        assertTrue((firstDiagnostic.arguments[0] as String).matches(Regex("\\d{2}:\\d{2}:\\d{2}\\.\\d{3}")))
        val diagnosticMessage = firstDiagnostic.arguments[2] as UiText.Resource
        assertEquals(R.string.diagnostic_action_started, diagnosticMessage.resourceId)
        assertEquals("Device", diagnosticMessage.arguments.single())

        viewModel.executeDeviceAction(device)
        runCurrent()

        assertEquals(2, actionService.callCount)
        assertEquals(
            1,
            viewModel.uiState.value.diagnosticItems.count {
                it == DiagnosticListItem.Separator
            }
        )

        viewModel.clearDiagnosticMessages()

        assertTrue(viewModel.uiState.value.diagnosticItems.isEmpty())
    }

    @Test
    fun cancelActionStopsOnlyTheSelectedDeviceActionAndShowsCancellation() = runTest(dispatcher) {
        val firstDevice = device(id = "device-1", sortOrder = 0)
        val secondDevice = device(id = "device-2", sortOrder = 1)
        val actionService = WaitingDeviceActionService()
        val viewModel = MainViewModel(
            repository = FakeDeviceRepository(listOf(firstDevice, secondDevice)),
            deviceActionService = actionService,
            appSettingsRepository = FakeAppSettingsRepository(),
            wifiProfileRepository = FakeWifiProfileRepository(),
            wifiProximityService = FixedWifiProximityService(),
            appUpdateRepository = FakeAppUpdateRepository()
        )
        runCurrent()

        viewModel.executeDeviceAction(firstDevice)
        runCurrent()
        viewModel.cancelDeviceAction(firstDevice.id)
        runCurrent()

        val actionState = viewModel.uiState.value.deviceActionStates[firstDevice.id]
            as DeviceActionUiState.Error
        assertEquals(R.string.action_cancelled, (actionState.message as UiText.Resource).resourceId)
        assertEquals(null, viewModel.uiState.value.deviceActionStates[secondDevice.id])
        val cancellationDiagnostic = viewModel.uiState.value.diagnosticItems
            .filterIsInstance<DiagnosticListItem.Message>()
            .last()
            .text as UiText.Resource
        val diagnosticMessage = cancellationDiagnostic.arguments[2] as UiText.Resource
        assertEquals(R.string.diagnostic_action_cancelled, diagnosticMessage.resourceId)
    }

    @Test
    fun moveDeviceDownPersistsReorderedDeviceIds() = runTest(dispatcher) {
        val repository = FakeDeviceRepository(
            listOf(
                device(id = "device-1", sortOrder = 0),
                device(id = "device-2", sortOrder = 1),
                device(id = "device-3", sortOrder = 2)
            )
        )
        val viewModel = MainViewModel(
            repository = repository,
            deviceActionService = WaitingDeviceActionService(),
            appSettingsRepository = FakeAppSettingsRepository(),
            wifiProfileRepository = FakeWifiProfileRepository(),
            wifiProximityService = FixedWifiProximityService(),
            appUpdateRepository = FakeAppUpdateRepository()
        )
        runCurrent()

        viewModel.moveDeviceDown("device-1")
        runCurrent()

        assertEquals(listOf("device-2", "device-1", "device-3"), repository.lastDeviceOrder)
    }

    @Test
    fun dashboardLayoutModeIsAppliedImmediately() = runTest(dispatcher) {
        val viewModel = MainViewModel(
            repository = FakeDeviceRepository(emptyList()),
            deviceActionService = WaitingDeviceActionService(),
            appSettingsRepository = FakeAppSettingsRepository(),
            wifiProfileRepository = FakeWifiProfileRepository(),
            wifiProximityService = FixedWifiProximityService(),
            appUpdateRepository = FakeAppUpdateRepository()
        )
        runCurrent()

        viewModel.setDashboardLayoutMode(DashboardLayoutMode.WIDGETS)
        runCurrent()

        assertEquals(
            DashboardLayoutMode.WIDGETS,
            viewModel.uiState.value.appSettings.dashboardLayoutMode
        )
    }

    @Test
    fun assignedVisibleWifiIsNearby() {
        val device = device(id = "device-1", sortOrder = 0).copy(
            connections = listOf(DeviceConnection(wifiProfileId = "wifi-1", host = "device.local"))
        )

        val statuses = resolveWifiProximityStatuses(
            devices = listOf(device),
            wifiProfiles = listOf(WifiProfile(id = "wifi-1", ssid = "Home")),
            snapshot = WifiProximitySnapshot(visibleSsids = setOf("Home"))
        )

        assertEquals(DeviceWifiProximityStatus.NEARBY, statuses[device.id])
    }

    @Test
    fun explicitConnectionFailureOverridesVisibleWifi() {
        val device = device(id = "device-1", sortOrder = 0).copy(
            connections = listOf(DeviceConnection(wifiProfileId = "wifi-1", host = "device.local"))
        )

        val statuses = resolveWifiProximityStatuses(
            devices = listOf(device),
            wifiProfiles = listOf(WifiProfile(id = "wifi-1", ssid = "Home")),
            snapshot = WifiProximitySnapshot(
                visibleSsids = setOf("Home"),
                unavailableSsids = setOf("Home")
            )
        )

        assertEquals(DeviceWifiProximityStatus.NOT_NEARBY, statuses[device.id])
    }

    @Test
    fun deviceWithoutAssignmentIsNeverNearby() {
        val device = device(id = "device-1", sortOrder = 0)

        val statuses = resolveWifiProximityStatuses(
            devices = listOf(device),
            wifiProfiles = listOf(WifiProfile(id = "wifi-1", ssid = "Home")),
            snapshot = WifiProximitySnapshot(visibleSsids = setOf("Home"))
        )

        assertEquals(DeviceWifiProximityStatus.NO_ASSIGNMENT, statuses[device.id])
    }

    @Test
    fun scanFailureKeepsMatchingConnectedWifiNearbyAndMarksOtherDeviceFailed() {
        val matchingDevice = device(id = "matching", sortOrder = 0).copy(
            connections = listOf(DeviceConnection(wifiProfileId = "wifi-1", host = "device.local"))
        )
        val otherDevice = device(id = "other", sortOrder = 1).copy(
            connections = listOf(DeviceConnection(wifiProfileId = "wifi-2", host = "other.local"))
        )

        val statuses = resolveWifiProximityStatuses(
            devices = listOf(matchingDevice, otherDevice),
            wifiProfiles = listOf(
                WifiProfile(id = "wifi-1", ssid = "Connected"),
                WifiProfile(id = "wifi-2", ssid = "Other")
            ),
            snapshot = WifiProximitySnapshot(
                visibleSsids = setOf("Connected"),
                issue = WifiProximityIssue.SCAN_FAILED
            )
        )

        assertEquals(DeviceWifiProximityStatus.NEARBY, statuses[matchingDevice.id])
        assertEquals(DeviceWifiProximityStatus.SCAN_FAILED, statuses[otherDevice.id])
    }

    @Test
    fun disabledLocationServicesAreGrayUnlessAssignedWifiIsConnected() {
        val connectedDevice = device(id = "connected", sortOrder = 0).copy(
            connections = listOf(DeviceConnection(wifiProfileId = "wifi-1", host = "one.local"))
        )
        val scanDependentDevice = device(id = "scan", sortOrder = 1).copy(
            connections = listOf(DeviceConnection(wifiProfileId = "wifi-2", host = "two.local"))
        )

        val statuses = resolveWifiProximityStatuses(
            devices = listOf(connectedDevice, scanDependentDevice),
            wifiProfiles = listOf(
                WifiProfile(id = "wifi-1", ssid = "Connected"),
                WifiProfile(id = "wifi-2", ssid = "Nearby")
            ),
            snapshot = WifiProximitySnapshot(
                visibleSsids = setOf("Connected"),
                issue = WifiProximityIssue.LOCATION_SERVICES_DISABLED
            )
        )

        assertEquals(DeviceWifiProximityStatus.NEARBY, statuses[connectedDevice.id])
        assertEquals(
            DeviceWifiProximityStatus.LOCATION_SERVICES_DISABLED,
            statuses[scanDependentDevice.id]
        )
    }

    @Test
    fun refreshPublishesProximityThroughUiState() = runTest(dispatcher) {
        val device = device(id = "device-1", sortOrder = 0).copy(
            connections = listOf(
                DeviceConnection(wifiProfileId = "garage-ap", host = "device.local")
            )
        )
        val viewModel = MainViewModel(
            repository = FakeDeviceRepository(listOf(device)),
            deviceActionService = WaitingDeviceActionService(),
            appSettingsRepository = FakeAppSettingsRepository(),
            wifiProfileRepository = FakeWifiProfileRepository(),
            wifiProximityService = FixedWifiProximityService(
                WifiProximitySnapshot(visibleSsids = setOf("Shelly-Garage"))
            ),
            appUpdateRepository = FakeAppUpdateRepository()
        )
        runCurrent()

        viewModel.refreshWifiProximity()
        runCurrent()

        assertEquals(
            DeviceWifiProximityStatus.NEARBY,
            viewModel.uiState.value.wifiProximityStatuses[device.id]
        )
    }

    @Test
    fun changedWifiAssignmentReusesLatestSnapshot() = runTest(dispatcher) {
        val repository = FakeDeviceRepository(
            listOf(
                device(id = "device-1", sortOrder = 0).copy(
                    connections = listOf(
                        DeviceConnection(wifiProfileId = "garage-ap", host = "device.local")
                    )
                )
            )
        )
        val viewModel = MainViewModel(
            repository = repository,
            deviceActionService = WaitingDeviceActionService(),
            appSettingsRepository = FakeAppSettingsRepository(),
            wifiProfileRepository = FakeWifiProfileRepository(),
            wifiProximityService = FixedWifiProximityService(
                WifiProximitySnapshot(visibleSsids = setOf("Home-WLAN"))
            ),
            appUpdateRepository = FakeAppUpdateRepository()
        )
        runCurrent()
        viewModel.refreshWifiProximity()
        runCurrent()

        repository.setDevices(
            listOf(
                device(id = "device-1", sortOrder = 0).copy(
                    connections = listOf(
                        DeviceConnection(wifiProfileId = "home-wifi", host = "device.local")
                    )
                )
            )
        )
        runCurrent()

        assertEquals(
            DeviceWifiProximityStatus.NEARBY,
            viewModel.uiState.value.wifiProximityStatuses["device-1"]
        )
    }

    @Test
    fun monitoringPublishesEventsOnlyWhileStarted() = runTest(dispatcher) {
        val device = device(id = "device-1", sortOrder = 0).copy(
            connections = listOf(
                DeviceConnection(wifiProfileId = "home-wifi", host = "device.local")
            )
        )
        val proximityService = MonitoringWifiProximityService()
        val viewModel = MainViewModel(
            repository = FakeDeviceRepository(listOf(device)),
            deviceActionService = WaitingDeviceActionService(),
            appSettingsRepository = FakeAppSettingsRepository(),
            wifiProfileRepository = FakeWifiProfileRepository(),
            wifiProximityService = proximityService,
            appUpdateRepository = FakeAppUpdateRepository()
        )
        runCurrent()

        viewModel.startWifiProximityMonitoring()
        runCurrent()
        proximityService.snapshots.value = WifiProximitySnapshot(
            visibleSsids = setOf("Home-WLAN")
        )
        runCurrent()

        assertEquals(
            DeviceWifiProximityStatus.NEARBY,
            viewModel.uiState.value.wifiProximityStatuses[device.id]
        )

        viewModel.stopWifiProximityMonitoring()
        proximityService.snapshots.value = WifiProximitySnapshot()
        runCurrent()

        assertEquals(
            DeviceWifiProximityStatus.NEARBY,
            viewModel.uiState.value.wifiProximityStatuses[device.id]
        )
    }

    private fun device(id: String, sortOrder: Int): Device {
        return Device(
            id = id,
            name = "Device",
            actionLabel = "Switch",
            apiCall = ApiCall(ApiMethod.GET, "/rpc/action"),
            connections = emptyList(),
            sortOrder = sortOrder
        )
    }

    private fun switchGroup(id: String, memberCount: Int): SwitchGroup {
        return SwitchGroup(
            id = id,
            name = "Group",
            actionLabel = "Run",
            sortOrder = 0,
            members = List(memberCount) { index ->
                SwitchGroupMember(
                    id = "member-$index",
                    deviceId = "device-1",
                    pauseAfterMillis = 0L,
                    sortOrder = index
                )
            }
        )
    }

    private class WaitingDeviceActionService : DeviceActionService {
        val result = CompletableDeferred<DeviceActionResult>()
        var callCount = 0

        override suspend fun execute(device: Device): DeviceActionResult {
            callCount += 1
            return result.await()
        }

        override suspend fun execute(
            device: Device,
            onDiagnosticEvent: (DeviceActionDiagnosticEvent) -> Unit
        ): DeviceActionResult {
            callCount += 1
            onDiagnosticEvent(DeviceActionDiagnosticEvent.ActionStarted)
            val actionResult = result.await()
            if (actionResult == DeviceActionResult.Success) {
                onDiagnosticEvent(DeviceActionDiagnosticEvent.RequestSucceeded)
            }
            onDiagnosticEvent(DeviceActionDiagnosticEvent.ActionCompleted)
            return actionResult
        }
    }

    private class WaitingSwitchGroupActionService : SwitchGroupActionService {
        val result = CompletableDeferred<SwitchGroupActionResult>()
        var callCount = 0

        override suspend fun execute(
            group: SwitchGroup,
            devices: List<Device>,
            onDiagnosticEvent: (SwitchGroupDiagnosticEvent) -> Unit
        ): SwitchGroupActionResult {
            callCount += 1
            onDiagnosticEvent(SwitchGroupDiagnosticEvent.GroupStarted)
            return result.await()
        }
    }

    private class FixedWifiProximityService(
        private val snapshot: WifiProximitySnapshot = WifiProximitySnapshot()
    ) : WifiProximityService {
        override fun observe(): Flow<WifiProximitySnapshot> = flowOf(snapshot)

        override suspend fun refresh(): WifiProximitySnapshot = snapshot
    }

    private class MonitoringWifiProximityService : WifiProximityService {
        val snapshots = MutableStateFlow(WifiProximitySnapshot())

        override fun observe(): Flow<WifiProximitySnapshot> = snapshots

        override suspend fun refresh(): WifiProximitySnapshot = snapshots.value
    }

    private class FakeDeviceRepository(
        devices: List<Device>
    ) : DeviceRepository {
        private val devicesFlow = MutableStateFlow(devices)
        var lastDeviceOrder: List<String> = emptyList()

        override fun observeDevices(): Flow<List<Device>> = devicesFlow

        override suspend fun getDevices(): List<Device> = devicesFlow.value

        override suspend fun saveDevice(device: Device) = Unit

        override suspend fun updateDeviceOrder(deviceIds: List<String>) {
            lastDeviceOrder = deviceIds
            val devicesById = devicesFlow.value.associateBy { it.id }
            devicesFlow.value = deviceIds.mapIndexedNotNull { index, deviceId ->
                devicesById[deviceId]?.copy(sortOrder = index)
            }
        }

        override suspend fun updateDeviceSortOrders(sortOrders: Map<String, Int>) {
            devicesFlow.value = devicesFlow.value.map { device ->
                sortOrders[device.id]?.let { device.copy(sortOrder = it) } ?: device
            }
            lastDeviceOrder = devicesFlow.value
                .sortedBy { it.sortOrder }
                .map { it.id }
        }

        override suspend fun deleteDevice(deviceId: String) = Unit

        fun setDevices(devices: List<Device>) {
            devicesFlow.value = devices
        }
    }

    private class FakeSwitchGroupRepository(
        groups: List<SwitchGroup>
    ) : SwitchGroupRepository {
        private val groupsFlow = MutableStateFlow(groups)

        override fun observeSwitchGroups(): Flow<List<SwitchGroup>> = groupsFlow

        override suspend fun getSwitchGroups(): List<SwitchGroup> = groupsFlow.value

        override suspend fun saveSwitchGroup(group: SwitchGroup) = Unit

        override suspend fun updateSwitchGroupOrder(groupIds: List<String>) = Unit

        override suspend fun updateSwitchGroupSortOrders(sortOrders: Map<String, Int>) = Unit

        override suspend fun deleteSwitchGroup(groupId: String) = Unit
    }
}
