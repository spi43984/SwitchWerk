package de.piecha.switchwerk.data.network

sealed interface HttpApiCallResult {
    data class Success(val response: HttpApiResponse) : HttpApiCallResult

    data class HttpError(val response: HttpApiResponse) : HttpApiCallResult

    data object Timeout : HttpApiCallResult

    data class NetworkError(val cause: Throwable) : HttpApiCallResult

    data class InvalidRequest(val cause: IllegalArgumentException) : HttpApiCallResult
}
