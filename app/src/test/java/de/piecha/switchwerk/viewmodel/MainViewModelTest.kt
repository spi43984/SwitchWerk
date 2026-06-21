package de.piecha.switchwerk.viewmodel

import de.piecha.switchwerk.data.action.DeviceActionResult
import de.piecha.switchwerk.data.action.DeviceActionDiagnosticEvent
import de.piecha.switchwerk.data.action.DeviceActionService
import de.piecha.switchwerk.data.repository.DeviceRepository
import de.piecha.switchwerk.data.repository.FakeAppSettingsRepository
import de.piecha.switchwerk.domain.model.ApiCall
import de.piecha.switchwerk.domain.model.ApiMethod
import de.piecha.switchwerk.domain.model.DashboardLayoutMode
import de.piecha.switchwerk.domain.model.Device
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
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
    fun repeatedClickDoesNotStartSecondActionForSameDevice() = runTest(dispatcher) {
        val device = device(id = "device-1", sortOrder = 0)
        val actionService = WaitingDeviceActionService()
        val viewModel = MainViewModel(
            repository = FakeDeviceRepository(listOf(device)),
            deviceActionService = actionService,
            appSettingsRepository = FakeAppSettingsRepository()
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
        assertTrue(
            viewModel.uiState.value.diagnosticItems
                .filterIsInstance<DiagnosticListItem.Message>()
                .all {
                    it.text.matches(
                        Regex("\\d{2}:\\d{2}:\\d{2}\\.\\d{3} \\(\\+\\d+ ms\\) .+")
                    )
                }
        )
        assertTrue(
            viewModel.uiState.value.diagnosticItems
                .filterIsInstance<DiagnosticListItem.Message>()
                .first()
                .text
                .endsWith("Geräteaktion „Device“ gestartet")
        )

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
            appSettingsRepository = FakeAppSettingsRepository()
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
            appSettingsRepository = FakeAppSettingsRepository()
        )
        runCurrent()

        viewModel.setDashboardLayoutMode(DashboardLayoutMode.WIDGETS)
        runCurrent()

        assertEquals(
            DashboardLayoutMode.WIDGETS,
            viewModel.uiState.value.appSettings.dashboardLayoutMode
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

        override suspend fun deleteDevice(deviceId: String) = Unit
    }
}
