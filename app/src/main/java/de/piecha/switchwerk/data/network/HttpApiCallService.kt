package de.piecha.switchwerk.data.network

import android.net.Network

interface HttpApiCallService {
    suspend fun resolveHost(
        host: String,
        network: Network,
        timeoutMillis: Long = DEFAULT_DNS_TIMEOUT_MILLIS
    ): DnsResolutionResult

    suspend fun get(
        url: String,
        network: Network? = null,
        timeoutMillis: Long = DEFAULT_TIMEOUT_MILLIS
    ): HttpApiCallResult

    suspend fun post(
        url: String,
        body: String?,
        contentType: String = DEFAULT_CONTENT_TYPE,
        network: Network? = null,
        timeoutMillis: Long = DEFAULT_TIMEOUT_MILLIS
    ): HttpApiCallResult

    companion object {
        const val DEFAULT_TIMEOUT_MILLIS = 10_000L
        const val DEFAULT_DNS_TIMEOUT_MILLIS = 5_000L
        const val DEFAULT_CONTENT_TYPE = "application/json"
    }
}
