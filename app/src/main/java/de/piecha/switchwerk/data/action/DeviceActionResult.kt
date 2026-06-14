package de.piecha.switchwerk.data.action

sealed interface DeviceActionResult {
    data object Success : DeviceActionResult

    data object NoConnections : DeviceActionResult

    data object WifiConnectionFailed : DeviceActionResult

    data object WifiPermissionDenied : DeviceActionResult

    data object UnsupportedAndroidVersion : DeviceActionResult

    data class HttpError(val statusCode: Int) : DeviceActionResult

    data object Timeout : DeviceActionResult

    data object InvalidRequest : DeviceActionResult

    data object NetworkError : DeviceActionResult

    data object UnexpectedError : DeviceActionResult
}
