package de.piecha.switchwerk.data.network

interface WifiConnectionService {
    suspend fun connect(
        ssid: String,
        password: String?,
        timeoutMillis: Long = DEFAULT_TIMEOUT_MILLIS
    ): WifiConnectionResult

    fun disconnect()

    companion object {
        const val DEFAULT_TIMEOUT_MILLIS = 15_000L
    }
}
