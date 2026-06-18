package de.piecha.switchwerk.data.network

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import de.piecha.switchwerk.domain.model.WifiSecurityType
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull

class AndroidWifiConnectionService(
    private val connectivityManager: ConnectivityManager
) : WifiConnectionService {

    private val callbackLock = Any()
    private var activeCallback: ConnectivityManager.NetworkCallback? = null

    override suspend fun connect(
        ssid: String,
        password: String?,
        securityType: WifiSecurityType,
        timeoutMillis: Long
    ): WifiConnectionResult {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return WifiConnectionResult.UnsupportedAndroidVersion
        }
        if (ssid.isBlank()) {
            return WifiConnectionResult.Error(
                IllegalArgumentException("SSID must not be blank")
            )
        }
        if (timeoutMillis <= 0) {
            return WifiConnectionResult.Error(
                IllegalArgumentException("Timeout must be greater than zero")
            )
        }

        disconnect()
        logInfo("Requesting explicit WiFi network")

        return try {
            withTimeoutOrNull(timeoutMillis) {
                requestNetwork(
                    ssid = ssid,
                    password = password,
                    securityType = securityType
                )
            } ?: WifiConnectionResult.Timeout
        } catch (_: SecurityException) {
            WifiConnectionResult.PermissionDenied
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: IllegalArgumentException) {
            WifiConnectionResult.Error(exception)
        } catch (exception: RuntimeException) {
            WifiConnectionResult.Error(exception)
        }
    }

    override fun disconnect() {
        val callback = synchronized(callbackLock) {
            activeCallback.also {
                activeCallback = null
            }
        }
        callback?.let(::unregisterCallback)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private suspend fun requestNetwork(
        ssid: String,
        password: String?,
        securityType: WifiSecurityType
    ): WifiConnectionResult {
        val specifierBuilder = WifiNetworkSpecifier.Builder()
            .setSsid(ssid)

        if (!password.isNullOrEmpty()) {
            when (securityType) {
                WifiSecurityType.WPA2 -> specifierBuilder.setWpa2Passphrase(password)
                WifiSecurityType.WPA3 -> specifierBuilder.setWpa3Passphrase(password)
            }
        }

        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .setNetworkSpecifier(specifierBuilder.build())
            .build()

        return suspendCancellableCoroutine { continuation ->
            val callback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    logInfo("Explicit WiFi network callback: available")
                    if (continuation.isActive) {
                        continuation.resume(WifiConnectionResult.Success(network))
                    }
                }

                override fun onUnavailable() {
                    logWarning("Explicit WiFi network callback: unavailable")
                    clearActiveCallback(this)
                    if (continuation.isActive) {
                        continuation.resume(WifiConnectionResult.Unavailable)
                    }
                }

                override fun onLost(network: Network) {
                    logWarning("Explicit WiFi network callback: lost")
                    clearActiveCallback(this)
                    unregisterCallback(this)
                }
            }

            setActiveCallback(callback)

            continuation.invokeOnCancellation {
                clearActiveCallback(callback)
                unregisterCallback(callback)
            }

            try {
                connectivityManager.requestNetwork(request, callback)
            } catch (exception: RuntimeException) {
                clearActiveCallback(callback)
                unregisterCallback(callback)
                if (continuation.isActive) {
                    continuation.resumeWithException(exception)
                }
            }
        }
    }

    private fun setActiveCallback(callback: ConnectivityManager.NetworkCallback) {
        synchronized(callbackLock) {
            activeCallback = callback
        }
    }

    private fun clearActiveCallback(callback: ConnectivityManager.NetworkCallback) {
        synchronized(callbackLock) {
            if (activeCallback === callback) {
                activeCallback = null
            }
        }
    }

    private fun unregisterCallback(callback: ConnectivityManager.NetworkCallback) {
        try {
            connectivityManager.unregisterNetworkCallback(callback)
        } catch (_: IllegalArgumentException) {
            // The callback may already have been released by Android.
        }
    }

    private fun logInfo(message: String) {
        runCatching { Log.i(LOG_TAG, message) }
    }

    private fun logWarning(message: String) {
        runCatching { Log.w(LOG_TAG, message) }
    }

    private companion object {
        const val LOG_TAG = "SwitchWerkNetwork"
    }
}
