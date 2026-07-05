package de.piecha.switchwerk.data.action

sealed interface SwitchGroupActionResult {
    data object Success : SwitchGroupActionResult

    data class SuccessWithFailures(val failedSteps: Int) : SwitchGroupActionResult

    data object EmptyGroup : SwitchGroupActionResult

    data class MissingDevice(
        val step: Int,
        val deviceId: String
    ) : SwitchGroupActionResult

    data class DeviceFailed(
        val step: Int,
        val deviceName: String,
        val result: DeviceActionResult
    ) : SwitchGroupActionResult
}
