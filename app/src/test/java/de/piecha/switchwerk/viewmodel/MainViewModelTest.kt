package de.piecha.switchwerk.viewmodel

import de.piecha.switchwerk.data.action.DeviceActionResult
import de.piecha.switchwerk.data.action.DeviceActionService
import de.piecha.switchwerk.data.repository.DeviceRepository
import de.piecha.switchwerk.domain.model.ApiCall
import de.piecha.switchwerk.domain.model.ApiMethod
import de.piecha.switchwerk.domain.model.Device
import kotlinx.coroutines.CompletableDeferred
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
        val device = device()
        val actionService = WaitingDeviceActionService()
        val viewModel = MainViewModel(
            repository = FakeDeviceRepository(device),
            deviceActionService = actionService
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
    }

    private fun device(): Device {
        return Device(
            id = "device-1",
            name = "Device",
            actionLabel = "Switch",
            apiCall = ApiCall(ApiMethod.GET, "/rpc/action"),
            connections = emptyList(),
            sortOrder = 0
        )
    }

    private class WaitingDeviceActionService : DeviceActionService {
        val result = CompletableDeferred<DeviceActionResult>()
        var callCount = 0

        override suspend fun execute(device: Device): DeviceActionResult {
            callCount += 1
            return result.await()
        }
    }

    private class FakeDeviceRepository(
        private val device: Device
    ) : DeviceRepository {
        override fun observeDevices(): Flow<List<Device>> = flowOf(listOf(device))

        override suspend fun getDevices(): List<Device> = listOf(device)

        override suspend fun saveDevice(device: Device) = Unit

        override suspend fun deleteDevice(deviceId: String) = Unit
    }
}
