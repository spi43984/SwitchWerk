package de.piecha.switchwerk.data.network

import android.net.Network

sealed interface WifiConnectionResult {
    data class Success(val network: Network) : WifiConnectionResult

    data object Timeout : WifiConnectionResult

    data object Unavailable : WifiConnectionResult

    data object PermissionDenied : WifiConnectionResult

    data object UnsupportedAndroidVersion : WifiConnectionResult

    data class Error(val cause: Throwable) : WifiConnectionResult
}
