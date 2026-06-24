package de.piecha.switchwerk.data.action

import de.piecha.switchwerk.domain.model.ApiMethod

sealed interface DeviceActionDiagnosticEvent {
    data object ActionStarted : DeviceActionDiagnosticEvent

    data class WifiProfileAttempt(
        val profileName: String,
        val index: Int,
        val total: Int
    ) : DeviceActionDiagnosticEvent

    data class WifiRequestStarted(val profileName: String) : DeviceActionDiagnosticEvent

    data object WifiSecurityDetectionStarted : DeviceActionDiagnosticEvent

    data object WifiSecurityDetectionSucceeded : DeviceActionDiagnosticEvent

    data object WifiSecurityDetectionUnavailable : DeviceActionDiagnosticEvent

    data object WifiFound : DeviceActionDiagnosticEvent

    data object WifiConnected : DeviceActionDiagnosticEvent

    data object IpAddressReceived : DeviceActionDiagnosticEvent

    data object WifiConnectionFailed : DeviceActionDiagnosticEvent

    data object WifiDisabled : DeviceActionDiagnosticEvent

    data class DeviceAddress(val address: String) : DeviceActionDiagnosticEvent

    data class HttpRequestStarted(
        val method: ApiMethod,
        val address: String
    ) : DeviceActionDiagnosticEvent

    data class HttpResponseReceived(val statusCode: Int) : DeviceActionDiagnosticEvent

    data class HttpRequestSucceeded(val statusCode: Int) : DeviceActionDiagnosticEvent

    data class DnsResolutionStarted(val address: String) : DeviceActionDiagnosticEvent

    data object DnsResolutionSucceeded : DeviceActionDiagnosticEvent

    data object DnsResolutionFailed : DeviceActionDiagnosticEvent

    data object DeviceNotReachable : DeviceActionDiagnosticEvent

    data object RequestSucceeded : DeviceActionDiagnosticEvent

    data object RequestFailed : DeviceActionDiagnosticEvent

    data object ActionCancelled : DeviceActionDiagnosticEvent

    data class Timeout(val stage: DiagnosticStage) : DeviceActionDiagnosticEvent

    data object ActionCompleted : DeviceActionDiagnosticEvent
}

enum class DiagnosticStage {
    WIFI_REQUEST,
    WIFI,
    DNS,
    HTTP
}
