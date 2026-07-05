package de.piecha.switchwerk.data.action

sealed interface SwitchGroupDiagnosticEvent {
    data object GroupStarted : SwitchGroupDiagnosticEvent

    data class MemberStarted(
        val step: Int,
        val total: Int,
        val deviceName: String
    ) : SwitchGroupDiagnosticEvent

    data class MemberSucceeded(
        val step: Int,
        val total: Int,
        val deviceName: String
    ) : SwitchGroupDiagnosticEvent

    data class MemberFailed(
        val step: Int,
        val total: Int,
        val deviceName: String
    ) : SwitchGroupDiagnosticEvent

    data class PauseStarted(
        val pauseMillis: Long
    ) : SwitchGroupDiagnosticEvent

    data object GroupCompleted : SwitchGroupDiagnosticEvent

    data object GroupCancelled : SwitchGroupDiagnosticEvent

    data class DeviceEvent(
        val deviceName: String,
        val event: DeviceActionDiagnosticEvent
    ) : SwitchGroupDiagnosticEvent
}
