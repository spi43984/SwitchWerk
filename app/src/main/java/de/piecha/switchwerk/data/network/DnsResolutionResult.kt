package de.piecha.switchwerk.data.network

sealed interface DnsResolutionResult {
    data object Success : DnsResolutionResult

    data object Timeout : DnsResolutionResult

    data class Error(val cause: Throwable) : DnsResolutionResult
}
