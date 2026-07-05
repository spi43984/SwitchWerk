package de.piecha.switchwerk.data.action

import de.piecha.switchwerk.domain.model.Device
import de.piecha.switchwerk.domain.model.SwitchGroup
import de.piecha.switchwerk.domain.model.SwitchGroupErrorStrategy
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay

class DefaultSwitchGroupActionService(
    private val deviceActionService: DeviceActionService
) : SwitchGroupActionService {

    override suspend fun execute(
        group: SwitchGroup,
        devices: List<Device>,
        onDiagnosticEvent: (SwitchGroupDiagnosticEvent) -> Unit
    ): SwitchGroupActionResult {
        val orderedMembers = group.members.sortedBy { it.sortOrder }
        if (orderedMembers.isEmpty()) {
            return SwitchGroupActionResult.EmptyGroup
        }

        val devicesById = devices.associateBy { it.id }
        onDiagnosticEvent(SwitchGroupDiagnosticEvent.GroupStarted)
        var failedSteps = 0

        try {
            orderedMembers.forEachIndexed { index, member ->
                val step = index + 1
                val device = devicesById[member.deviceId]
                    ?: return SwitchGroupActionResult.MissingDevice(
                        step = step,
                        deviceId = member.deviceId
                    )
                onDiagnosticEvent(
                    SwitchGroupDiagnosticEvent.MemberStarted(
                        step = step,
                        total = orderedMembers.size,
                        deviceName = device.name
                    )
                )
                val result = deviceActionService.execute(device) { event ->
                    onDiagnosticEvent(
                        SwitchGroupDiagnosticEvent.DeviceEvent(
                            deviceName = device.name,
                            event = event
                        )
                    )
                }
                if (result != DeviceActionResult.Success) {
                    failedSteps += 1
                    onDiagnosticEvent(
                        SwitchGroupDiagnosticEvent.MemberFailed(
                            step = step,
                            total = orderedMembers.size,
                            deviceName = device.name
                        )
                    )
                    if (group.errorStrategy == SwitchGroupErrorStrategy.ABORT_ON_ERROR) {
                        return SwitchGroupActionResult.DeviceFailed(
                            step = step,
                            deviceName = device.name,
                            result = result
                        )
                    }
                } else {
                    onDiagnosticEvent(
                        SwitchGroupDiagnosticEvent.MemberSucceeded(
                            step = step,
                            total = orderedMembers.size,
                            deviceName = device.name
                        )
                    )
                }
                if (index < orderedMembers.lastIndex && member.pauseAfterMillis > 0L) {
                    onDiagnosticEvent(
                        SwitchGroupDiagnosticEvent.PauseStarted(member.pauseAfterMillis)
                    )
                    delay(member.pauseAfterMillis)
                }
            }
        } catch (exception: CancellationException) {
            onDiagnosticEvent(SwitchGroupDiagnosticEvent.GroupCancelled)
            throw exception
        }

        onDiagnosticEvent(SwitchGroupDiagnosticEvent.GroupCompleted)
        return if (failedSteps > 0) {
            SwitchGroupActionResult.SuccessWithFailures(failedSteps)
        } else {
            SwitchGroupActionResult.Success
        }
    }
}
