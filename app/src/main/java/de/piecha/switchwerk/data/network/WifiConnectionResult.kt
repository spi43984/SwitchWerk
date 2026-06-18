package de.piecha.switchwerk.data.network

import android.net.Network
import de.piecha.switchwerk.domain.model.WifiSecurityType

sealed interface WifiConnectionResult {
    data class Success(val network: Network) : WifiConnectionResult

    data class SecurityTypesFailed(
        val failures: List<SecurityAttemptFailure>
    ) : WifiConnectionResult

    data object Timeout : WifiConnectionResult

    data object Unavailable : WifiConnectionResult

    data object PermissionDenied : WifiConnectionResult

    data object UnsupportedAndroidVersion : WifiConnectionResult

    data class Error(val cause: Throwable) : WifiConnectionResult
}

data class SecurityAttemptFailure(
    val securityType: WifiSecurityType,
    val result: WifiConnectionResult
)
