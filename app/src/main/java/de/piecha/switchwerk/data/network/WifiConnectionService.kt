package de.piecha.switchwerk.data.network

import de.piecha.switchwerk.domain.model.WifiSecurityType

interface WifiConnectionService {
    suspend fun connect(
        ssid: String,
        password: String?,
        securityType: WifiSecurityType = WifiSecurityType.WPA2,
        timeoutMillis: Long = DEFAULT_TIMEOUT_MILLIS
    ): WifiConnectionResult

    fun disconnect()

    companion object {
        const val DEFAULT_TIMEOUT_MILLIS = 20_000L
    }
}
