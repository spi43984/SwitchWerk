package de.piecha.switchwerk.data.action

sealed interface DeviceActionResult {
    data object Success : DeviceActionResult

    data object NoConnections : DeviceActionResult

    data object WifiConnectionFailed : DeviceActionResult

    data class AndroidManagedWifiNotActive(val ssid: String) : DeviceActionResult

    data object WifiPermissionDenied : DeviceActionResult

    data object WifiDisabled : DeviceActionResult

    data object MissingWifiPassword : DeviceActionResult

    data object UnsupportedAndroidVersion : DeviceActionResult

    data class HttpError(val statusCode: Int) : DeviceActionResult

    data object Timeout : DeviceActionResult

    data object InvalidRequest : DeviceActionResult

    data object TlsCertificateError : DeviceActionResult

    data class NetworkError(val reason: NetworkFailureReason) : DeviceActionResult

    data object UnexpectedError : DeviceActionResult
}

enum class NetworkFailureReason {
    DNS,
    CONNECTION,
    NO_ROUTE,
    TLS_CERTIFICATE,
    VPN_BLOCKED,
    OTHER
}
