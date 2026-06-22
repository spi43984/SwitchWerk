package de.piecha.switchwerk.data.network

import kotlinx.coroutines.flow.Flow

enum class WifiProximityIssue {
    WIFI_DISABLED,
    LOCATION_SERVICES_DISABLED,
    PERMISSION_DENIED,
    SCAN_FAILED
}

data class WifiProximitySnapshot(
    val visibleSsids: Set<String> = emptySet(),
    val issue: WifiProximityIssue? = null
)

interface WifiProximityService {
    fun observe(): Flow<WifiProximitySnapshot>

    suspend fun refresh(): WifiProximitySnapshot
}
