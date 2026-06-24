package de.piecha.switchwerk.data.network

import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import android.os.SystemClock
import android.util.Log
import androidx.annotation.RequiresApi
import de.piecha.switchwerk.domain.model.WifiSecurityType
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull

class AndroidWifiConnectionService(
    private val connectivityManager: ConnectivityManager,
    private val wifiManager: WifiManager,
    private val proximityConfirmationStore: WifiProximityConfirmationStore
) : WifiConnectionService {

    private val callbackLock = Any()
    private var activeRequest: ActiveWifiRequest? = null
    private val requestIdCounter = AtomicLong(0L)

    @Suppress("DEPRECATION")
    override suspend fun detectedSecurityTypes(ssid: String): Set<WifiSecurityType>? {
        if (ssid.isBlank() || !wifiManager.isWifiEnabled) {
            return null
        }
        readDetectedSecurityTypes(ssid)?.let { return it }

        val scanStarted = try {
            wifiManager.startScan()
        } catch (_: SecurityException) {
            false
        } catch (_: RuntimeException) {
            false
        }
        if (!scanStarted) {
            return null
        }

        return withTimeoutOrNull(SCAN_RESULT_TIMEOUT_MILLIS) {
            while (true) {
                delay(SCAN_RESULT_POLL_INTERVAL_MILLIS)
                readDetectedSecurityTypes(ssid)?.let { return@withTimeoutOrNull it }
            }
            @Suppress("UNREACHABLE_CODE")
            null
        }
    }

    @Suppress("DEPRECATION")
    override suspend fun visibleSsids(): Set<String> {
        if (!wifiManager.isWifiEnabled) return emptySet()
        try {
            wifiManager.startScan()
        } catch (_: SecurityException) {
            return emptySet()
        } catch (_: RuntimeException) {
            return emptySet()
        }
        return try {
            wifiManager.scanResults
                .asSequence()
                .mapNotNull { it.SSID.normalizedSsid() }
                .toSet()
        } catch (_: SecurityException) {
            emptySet()
        } catch (_: RuntimeException) {
            emptySet()
        }
    }

    @Suppress("DEPRECATION")
    override fun activeWifiNetworkForSsid(ssid: String): Network? {
        @Suppress("DEPRECATION")
        val connectedSsid = wifiManager.connectionInfo?.ssid.normalizedSsid()
        if (connectedSsid != ssid.trim()) {
            return null
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return connectivityManager.activeNetwork
        }

        return runCatching {
            connectivityManager.allNetworks.firstOrNull { network ->
                val capabilities = connectivityManager.getNetworkCapabilities(network)
                    ?: return@firstOrNull false
                if (!capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    return@firstOrNull false
                }
                true
            }
        }.getOrNull()
    }

    @Suppress("DEPRECATION")
    private fun readDetectedSecurityTypes(ssid: String): Set<WifiSecurityType>? {
        val matchingResults = try {
            wifiManager.scanResults.filter { result -> result.SSID == ssid }
        } catch (_: SecurityException) {
            return null
        } catch (_: RuntimeException) {
            return null
        }
        if (matchingResults.isEmpty()) {
            return null
        }

        return buildSet {
            matchingResults.forEach { result ->
                val capabilities = result.capabilities.orEmpty()
                if (capabilities.contains("PSK", ignoreCase = true)) {
                    add(WifiSecurityType.WPA2)
                }
                if (capabilities.contains("SAE", ignoreCase = true)) {
                    add(WifiSecurityType.WPA3)
                }
            }
        }.takeIf { it.isNotEmpty() }
    }

    override suspend fun connect(
        ssid: String,
        password: String?,
        securityType: WifiSecurityType,
        timeoutMillis: Long,
        onProgress: (WifiConnectionProgress) -> Unit
    ): WifiConnectionResult {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return WifiConnectionResult.UnsupportedAndroidVersion
        }
        if (!wifiManager.isWifiEnabled) {
            return WifiConnectionResult.WifiDisabled
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

        val requestId = requestIdCounter.incrementAndGet()
        disconnect()
        logInfo("WiFi request id=$requestId event=requested")
        emitProgress(onProgress, WifiConnectionProgress.RequestStarted)

        val result = try {
            requestNetwork(
                requestId = requestId,
                ssid = ssid,
                password = password,
                securityType = securityType,
                timeoutMillis = timeoutMillis,
                onProgress = onProgress
            )
        } catch (_: SecurityException) {
            WifiConnectionResult.PermissionDenied
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: IllegalArgumentException) {
            WifiConnectionResult.Error(exception)
        } catch (exception: RuntimeException) {
            WifiConnectionResult.Error(exception)
        }
        when (result) {
            is WifiConnectionResult.Success -> proximityConfirmationStore.markAvailable(ssid)
            WifiConnectionResult.NetworkRequestTimeout,
            WifiConnectionResult.Unavailable,
            is WifiConnectionResult.SecurityTypesFailed -> {
                proximityConfirmationStore.markUnavailable(ssid)
            }
            else -> Unit
        }
        return result
    }

    override fun disconnect() {
        val request = synchronized(callbackLock) {
            activeRequest.also {
                activeRequest = null
            }
        }
        request?.let {
            logInfo("WiFi request id=${it.id} event=released")
            unregisterCallback(it.callback)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private suspend fun requestNetwork(
        requestId: Long,
        ssid: String,
        password: String?,
        securityType: WifiSecurityType,
        timeoutMillis: Long,
        onProgress: (WifiConnectionProgress) -> Unit
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
            val readiness = NetworkReadiness()
            val requestedAtMillis = SystemClock.elapsedRealtime()
            val networkAvailable = AtomicBoolean(false)
            val callback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    networkAvailable.set(true)
                    logInfo("WiFi request id=$requestId event=available")
                    readiness.markFound(network, onProgress, continuation.isActive)
                    val capabilitiesReady = readiness.updateCapabilities(
                        network = network,
                        capabilities = connectivityManager.getNetworkCapabilities(network),
                        onProgress = onProgress,
                        isActive = continuation.isActive
                    )
                    val linkPropertiesReady = readiness.updateLinkProperties(
                        network = network,
                        linkProperties = connectivityManager.getLinkProperties(network),
                        onProgress = onProgress,
                        isActive = continuation.isActive
                    )
                    (capabilitiesReady ?: linkPropertiesReady)?.let { readyNetwork ->
                        logInfo("WiFi request id=$requestId event=ip_ready")
                        if (continuation.isActive) {
                            continuation.resume(WifiConnectionResult.Success(readyNetwork))
                        }
                    }
                }

                override fun onCapabilitiesChanged(
                    network: Network,
                    networkCapabilities: NetworkCapabilities
                ) {
                    readiness.updateCapabilities(
                        network = network,
                        capabilities = networkCapabilities,
                        onProgress = onProgress,
                        isActive = continuation.isActive
                    )?.let { readyNetwork ->
                        logInfo("WiFi request id=$requestId event=ip_ready")
                        if (continuation.isActive) {
                            continuation.resume(WifiConnectionResult.Success(readyNetwork))
                        }
                    }
                }

                override fun onLinkPropertiesChanged(
                    network: Network,
                    linkProperties: LinkProperties
                ) {
                    readiness.updateLinkProperties(
                        network = network,
                        linkProperties = linkProperties,
                        onProgress = onProgress,
                        isActive = continuation.isActive
                    )?.let { readyNetwork ->
                        logInfo("WiFi request id=$requestId event=ip_ready")
                        if (continuation.isActive) {
                            continuation.resume(WifiConnectionResult.Success(readyNetwork))
                        }
                    }
                }

                override fun onUnavailable() {
                    val elapsedMillis = SystemClock.elapsedRealtime() - requestedAtMillis
                    val timedOut = elapsedMillis >= timeoutMillis - PLATFORM_TIMEOUT_TOLERANCE_MILLIS
                    val event = when {
                        !timedOut -> "unavailable"
                        networkAvailable.get() -> "timeout_after_available"
                        else -> "timeout_before_available"
                    }
                    logWarning("WiFi request id=$requestId event=$event")
                    clearActiveCallback(this)
                    unregisterCallback(this)
                    if (continuation.isActive) {
                        continuation.resume(
                            if (timedOut) {
                                WifiConnectionResult.NetworkRequestTimeout
                            } else {
                                WifiConnectionResult.Unavailable
                            }
                        )
                    }
                }

                override fun onLost(network: Network) {
                    logWarning("WiFi request id=$requestId event=lost")
                    clearActiveCallback(this)
                    unregisterCallback(this)
                    if (continuation.isActive) {
                        continuation.resume(WifiConnectionResult.Unavailable)
                    }
                }
            }

            setActiveCallback(requestId, callback)

            continuation.invokeOnCancellation {
                logInfo("WiFi request id=$requestId event=cancelled")
                clearActiveCallback(callback)
                unregisterCallback(callback)
            }

            try {
                connectivityManager.requestNetwork(
                    request,
                    callback,
                    timeoutMillis.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
                )
            } catch (exception: RuntimeException) {
                clearActiveCallback(callback)
                unregisterCallback(callback)
                if (continuation.isActive) {
                    continuation.resumeWithException(exception)
                }
            }
        }
    }

    private fun setActiveCallback(
        requestId: Long,
        callback: ConnectivityManager.NetworkCallback
    ) {
        synchronized(callbackLock) {
            activeRequest = ActiveWifiRequest(requestId, callback)
        }
    }

    private fun clearActiveCallback(callback: ConnectivityManager.NetworkCallback) {
        synchronized(callbackLock) {
            if (activeRequest?.callback === callback) {
                activeRequest = null
            }
        }
    }

    private data class ActiveWifiRequest(
        val id: Long,
        val callback: ConnectivityManager.NetworkCallback
    )

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

    private fun emitProgress(
        callback: (WifiConnectionProgress) -> Unit,
        progress: WifiConnectionProgress
    ) {
        runCatching { callback(progress) }
    }

    private fun String?.normalizedSsid(): String? {
        return this?.removeSurrounding("\"")?.trim()
            ?.takeIf { it.isNotEmpty() && it != WifiManager.UNKNOWN_SSID }
    }

    internal class NetworkReadiness {
        private var network: Network? = null
        private var foundEmitted = false
        private var connectedEmitted = false
        private var ipAddressEmitted = false
        private var hasWifiTransport = false
        private var hasIpAddress = false
        private var completed = false

        fun markFound(
            network: Network,
            onProgress: (WifiConnectionProgress) -> Unit,
            isActive: Boolean
        ) = synchronized(this) {
            if (!isActive || completed) return@synchronized
            this.network = network
            if (!foundEmitted) {
                foundEmitted = true
                emit(onProgress, WifiConnectionProgress.NetworkFound)
            }
        }

        fun updateCapabilities(
            network: Network,
            capabilities: NetworkCapabilities?,
            onProgress: (WifiConnectionProgress) -> Unit,
            isActive: Boolean
        ): Network? = synchronized(this) {
            if (!isActive || completed) return@synchronized null
            markFound(network, onProgress, isActive)
            hasWifiTransport = capabilities?.hasTransport(
                NetworkCapabilities.TRANSPORT_WIFI
            ) == true
            readyNetwork(onProgress)
        }

        fun updateLinkProperties(
            network: Network,
            linkProperties: LinkProperties?,
            onProgress: (WifiConnectionProgress) -> Unit,
            isActive: Boolean
        ): Network? = synchronized(this) {
            if (!isActive || completed) return@synchronized null
            markFound(network, onProgress, isActive)
            hasIpAddress = linkProperties?.linkAddresses.orEmpty().any { linkAddress ->
                val address = linkAddress.address
                !address.isAnyLocalAddress && !address.isLoopbackAddress
            }
            readyNetwork(onProgress)
        }

        private fun readyNetwork(
            onProgress: (WifiConnectionProgress) -> Unit
        ): Network? {
            if (hasWifiTransport && !connectedEmitted) {
                connectedEmitted = true
                emit(onProgress, WifiConnectionProgress.Connected)
            }
            if (hasWifiTransport && hasIpAddress && !ipAddressEmitted) {
                ipAddressEmitted = true
                emit(onProgress, WifiConnectionProgress.IpAddressAvailable)
            }
            if (!hasWifiTransport || !hasIpAddress || completed) {
                return null
            }
            completed = true
            return network
        }

        private fun emit(
            callback: (WifiConnectionProgress) -> Unit,
            progress: WifiConnectionProgress
        ) {
            runCatching { callback(progress) }
        }
    }

    private companion object {
        const val LOG_TAG = "SwitchWerkNetwork"
        const val SCAN_RESULT_TIMEOUT_MILLIS = 8_000L
        const val SCAN_RESULT_POLL_INTERVAL_MILLIS = 500L
        const val PLATFORM_TIMEOUT_TOLERANCE_MILLIS = 250L
    }
}
