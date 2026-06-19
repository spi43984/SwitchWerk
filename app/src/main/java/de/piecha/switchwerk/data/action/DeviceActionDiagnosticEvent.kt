package de.piecha.switchwerk.data.action

import de.piecha.switchwerk.domain.model.ApiMethod

sealed interface DeviceActionDiagnosticEvent {
    data object ActionStarted : DeviceActionDiagnosticEvent

    data class WifiProfileConnecting(val profileName: String) : DeviceActionDiagnosticEvent

    data object WifiConnectionSucceeded : DeviceActionDiagnosticEvent

    data object WifiConnectionFailed : DeviceActionDiagnosticEvent

    data class DeviceAddress(val address: String) : DeviceActionDiagnosticEvent

    data class HttpRequestStarted(
        val method: ApiMethod,
        val address: String
    ) : DeviceActionDiagnosticEvent

    data class HttpResponseReceived(val statusCode: Int) : DeviceActionDiagnosticEvent

    data object DnsResolutionFailed : DeviceActionDiagnosticEvent

    data object DeviceNotReachable : DeviceActionDiagnosticEvent

    data object RequestSucceeded : DeviceActionDiagnosticEvent

    data object RequestFailed : DeviceActionDiagnosticEvent

    data object ActionCompleted : DeviceActionDiagnosticEvent
}
