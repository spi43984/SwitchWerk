package de.piecha.switchwerk.data.action

import de.piecha.switchwerk.domain.model.ApiCall
import de.piecha.switchwerk.domain.model.ApiMethod
import de.piecha.switchwerk.domain.model.Device
import de.piecha.switchwerk.domain.model.SwitchGroup
import de.piecha.switchwerk.domain.model.SwitchGroupErrorStrategy
import de.piecha.switchwerk.domain.model.SwitchGroupMember
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultSwitchGroupActionServiceTest {

    @Test
    fun executesMembersSequentiallyAndWaitsConfiguredPause() = runTest {
        val actionService = RecordingDeviceActionService(now = { currentTime })
        val service = DefaultSwitchGroupActionService(actionService)
        val group = switchGroup(
            members = listOf(
                SwitchGroupMember(
                    id = "member-1",
                    deviceId = "device-1",
                    pauseAfterMillis = 500,
                    sortOrder = 0
                ),
                SwitchGroupMember(
                    id = "member-2",
                    deviceId = "device-2",
                    pauseAfterMillis = 0,
                    sortOrder = 1
                )
            )
        )

        val result = service.execute(
            group = group,
            devices = listOf(device("device-1"), device("device-2")),
            onDiagnosticEvent = {}
        )

        assertEquals(SwitchGroupActionResult.Success, result)
        assertEquals(listOf("device-1", "device-2"), actionService.calledDeviceIds)
        assertEquals(listOf(0L, 500L), actionService.callTimes)
    }

    @Test
    fun abortsOnFirstDeviceFailure() = runTest {
        val actionService = RecordingDeviceActionService(
            now = { currentTime },
            results = mapOf("device-1" to DeviceActionResult.Timeout)
        )
        val service = DefaultSwitchGroupActionService(actionService)

        val result = service.execute(
            group = switchGroup(
                members = listOf(
                    SwitchGroupMember(
                        id = "member-1",
                        deviceId = "device-1",
                        pauseAfterMillis = 500,
                        sortOrder = 0
                    ),
                    SwitchGroupMember(
                        id = "member-2",
                        deviceId = "device-2",
                        pauseAfterMillis = 0,
                        sortOrder = 1
                    )
                )
            ),
            devices = listOf(device("device-1"), device("device-2")),
            onDiagnosticEvent = {}
        )

        assertTrue(result is SwitchGroupActionResult.DeviceFailed)
        assertEquals(listOf("device-1"), actionService.calledDeviceIds)
    }

    @Test
    fun continuesAfterFailureWhenConfigured() = runTest {
        val actionService = RecordingDeviceActionService(
            now = { currentTime },
            results = mapOf("device-1" to DeviceActionResult.Timeout)
        )
        val service = DefaultSwitchGroupActionService(actionService)

        val result = service.execute(
            group = switchGroup(
                errorStrategy = SwitchGroupErrorStrategy.CONTINUE_ON_ERROR,
                members = listOf(
                    SwitchGroupMember(
                        id = "member-1",
                        deviceId = "device-1",
                        pauseAfterMillis = 500,
                        sortOrder = 0
                    ),
                    SwitchGroupMember(
                        id = "member-2",
                        deviceId = "device-2",
                        pauseAfterMillis = 0,
                        sortOrder = 1
                    )
                )
            ),
            devices = listOf(device("device-1"), device("device-2")),
            onDiagnosticEvent = {}
        )

        assertEquals(SwitchGroupActionResult.SuccessWithFailures(failedSteps = 1), result)
        assertEquals(listOf("device-1", "device-2"), actionService.calledDeviceIds)
        assertEquals(listOf(0L, 500L), actionService.callTimes)
    }

    @Test
    fun reportsEmptyGroupWithoutExecutingDeviceAction() = runTest {
        val actionService = RecordingDeviceActionService(now = { currentTime })
        val service = DefaultSwitchGroupActionService(actionService)

        val result = service.execute(
            group = switchGroup(members = emptyList()),
            devices = listOf(device("device-1")),
            onDiagnosticEvent = {}
        )

        assertEquals(SwitchGroupActionResult.EmptyGroup, result)
        assertTrue(actionService.calledDeviceIds.isEmpty())
    }

    private class RecordingDeviceActionService(
        private val now: () -> Long,
        private val results: Map<String, DeviceActionResult> = emptyMap()
    ) : DeviceActionService {
        val calledDeviceIds = mutableListOf<String>()
        val callTimes = mutableListOf<Long>()

        override suspend fun execute(device: Device): DeviceActionResult {
            calledDeviceIds += device.id
            callTimes += now()
            return results[device.id] ?: DeviceActionResult.Success
        }
    }

    private fun switchGroup(
        members: List<SwitchGroupMember>,
        errorStrategy: SwitchGroupErrorStrategy = SwitchGroupErrorStrategy.ABORT_ON_ERROR
    ): SwitchGroup {
        return SwitchGroup(
            id = "group-1",
            name = "Group",
            actionLabel = "Run",
            sortOrder = 0,
            errorStrategy = errorStrategy,
            members = members
        )
    }

    private fun device(id: String): Device {
        return Device(
            id = id,
            name = id,
            actionLabel = "Switch",
            apiCall = ApiCall(ApiMethod.GET, "/rpc/Switch.Toggle?id=0"),
            connections = emptyList(),
            sortOrder = 0
        )
    }
}
