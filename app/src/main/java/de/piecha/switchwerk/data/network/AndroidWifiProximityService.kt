package de.piecha.switchwerk.data.network

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.location.LocationManager
import android.os.Build
import android.os.SystemClock
import android.content.pm.PackageManager
import androidx.core.location.LocationManagerCompat
import androidx.core.content.ContextCompat
import androidx.annotation.RequiresApi
import kotlin.coroutines.resume
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull

class AndroidWifiProximityService(
    private val context: Context,
    private val connectivityManager: ConnectivityManager,
    private val wifiManager: WifiManager,
    private val locationManager: LocationManager
) : WifiProximityService {

    override fun observe(): Flow<WifiProximitySnapshot> = callbackFlow {
        fun publishSnapshot(scanFailed: Boolean = false) {
            trySend(readObservedSnapshot(scanFailed))
        }

        fun launchForegroundRefreshLoop() = launch {
            while (true) {
                delay(FOREGROUND_REFRESH_INTERVAL_MILLIS)
                trySend(refreshSnapshot())
            }
        }

        val statusReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                publishSnapshot()
            }
        }
        val statusReceiverRegistered = runCatching {
            registerReceiver(statusReceiver, statusIntentFilter())
        }.isSuccess

        val legacyScanReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val scanUpdated = intent?.getBooleanExtra(
                    WifiManager.EXTRA_RESULTS_UPDATED,
                    false
                ) == true
                publishSnapshot(scanFailed = !scanUpdated)
            }
        }
        val legacyScanReceiverRegistered = Build.VERSION.SDK_INT < Build.VERSION_CODES.R &&
            runCatching {
                registerReceiver(legacyScanReceiver, scanIntentFilter())
            }.isSuccess

        var scanResultsCallback: WifiManager.ScanResultsCallback? = null
        var scanResultsCallbackRegistered = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            scanResultsCallback = object : WifiManager.ScanResultsCallback() {
                override fun onScanResultsAvailable() {
                    publishSnapshot()
                }
            }
            scanResultsCallbackRegistered = runCatching {
                registerScanResultsCallback(scanResultsCallback)
            }.isSuccess
        }

        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                publishAfterNetworkCallback()
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                publishAfterNetworkCallback()
            }

            override fun onLost(network: Network) {
                publishAfterNetworkCallback()
            }

            private fun publishAfterNetworkCallback() {
                launch {
                    delay(NETWORK_CALLBACK_SETTLE_MILLIS)
                    publishSnapshot()
                }
            }
        }
        val networkCallbackRegistered = runCatching {
            connectivityManager.registerDefaultNetworkCallback(networkCallback)
        }.isSuccess
        val foregroundRefreshJob = launchForegroundRefreshLoop()

        publishSnapshot()

        awaitClose {
            foregroundRefreshJob.cancel()
            if (statusReceiverRegistered) {
                unregisterReceiver(statusReceiver)
            }
            if (legacyScanReceiverRegistered) {
                unregisterReceiver(legacyScanReceiver)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                scanResultsCallback
                    ?.takeIf { scanResultsCallbackRegistered }
                    ?.let { callback ->
                        runCatching { unregisterScanResultsCallback(callback) }
                    }
            }
            if (networkCallbackRegistered) {
                runCatching { connectivityManager.unregisterNetworkCallback(networkCallback) }
            }
        }
    }.distinctUntilChanged()

    override suspend fun refresh(): WifiProximitySnapshot = try {
        refreshSnapshot()
    } catch (exception: CancellationException) {
        throw exception
    } catch (_: SecurityException) {
        WifiProximitySnapshot(issue = WifiProximityIssue.PERMISSION_DENIED)
    } catch (_: RuntimeException) {
        WifiProximitySnapshot(issue = WifiProximityIssue.SCAN_FAILED)
    }

    private suspend fun refreshSnapshot(): WifiProximitySnapshot {
        if (!wifiManager.isWifiEnabled) {
            return WifiProximitySnapshot(issue = WifiProximityIssue.WIFI_DISABLED)
        }

        val locationServicesEnabled = LocationManagerCompat.isLocationEnabled(locationManager)
        val connectedSsid = try {
            readConnectedSsid()
        } catch (_: SecurityException) {
            if (locationServicesEnabled) {
                return WifiProximitySnapshot(issue = WifiProximityIssue.PERMISSION_DENIED)
            }
            null
        } catch (_: RuntimeException) {
            null
        }

        if (!locationServicesEnabled) {
            return WifiProximitySnapshot(
                visibleSsids = setOfNotNull(connectedSsid),
                issue = WifiProximityIssue.LOCATION_SERVICES_DISABLED
            )
        }

        val scanResult = scanForSsids()
        val visibleSsids = buildSet {
            connectedSsid?.let(::add)
            addAll(scanResult.ssids)
        }
        return WifiProximitySnapshot(
            visibleSsids = visibleSsids,
            issue = scanResult.issue
        )
    }

    private fun readObservedSnapshot(scanFailed: Boolean): WifiProximitySnapshot {
        return try {
            if (!wifiManager.isWifiEnabled) {
                return WifiProximitySnapshot(issue = WifiProximityIssue.WIFI_DISABLED)
            }
            val locationServicesEnabled = LocationManagerCompat.isLocationEnabled(locationManager)
            val connectedSsid = try {
                readConnectedSsid()
            } catch (exception: SecurityException) {
                if (locationServicesEnabled) {
                    throw exception
                }
                null
            }
            if (!locationServicesEnabled) {
                return WifiProximitySnapshot(
                    visibleSsids = setOfNotNull(connectedSsid),
                    issue = WifiProximityIssue.LOCATION_SERVICES_DISABLED
                )
            }
            val scannedSsids = readFreshScanSsids()
            WifiProximitySnapshot(
                visibleSsids = buildSet {
                    connectedSsid?.let(::add)
                    addAll(scannedSsids)
                },
                issue = if (scanFailed && scannedSsids.isEmpty()) {
                    WifiProximityIssue.SCAN_FAILED
                } else {
                    null
                }
            )
        } catch (_: SecurityException) {
            WifiProximitySnapshot(issue = WifiProximityIssue.PERMISSION_DENIED)
        } catch (_: RuntimeException) {
            WifiProximitySnapshot(issue = WifiProximityIssue.SCAN_FAILED)
        }
    }

    private fun readConnectedSsid(): String? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val network = connectivityManager.activeNetwork ?: return null
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return null
            if (!capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                return null
            }
            val wifiInfo = capabilities.transportInfo as? WifiInfo ?: return null
            wifiInfo.ssid.normalizedSsid()
        } else {
            @Suppress("DEPRECATION")
            val wifiInfo = wifiManager.connectionInfo ?: return null
            wifiInfo.ssid.normalizedSsid()
        }
    }

    @Suppress("DEPRECATION")
    private suspend fun scanForSsids(): ScanReadResult {
        return try {
            val scanUpdated = withTimeoutOrNull(SCAN_TIMEOUT_MILLIS) {
                suspendCancellableCoroutine { continuation ->
                    val receiver = object : BroadcastReceiver() {
                        override fun onReceive(context: Context?, intent: Intent?) {
                            if (continuation.isActive) {
                                unregisterReceiver(this)
                                continuation.resume(
                                    intent?.getBooleanExtra(
                                        WifiManager.EXTRA_RESULTS_UPDATED,
                                        false
                                    ) == true
                                )
                            }
                        }
                    }
                    registerReceiver(receiver, scanIntentFilter())
                    continuation.invokeOnCancellation {
                        unregisterReceiver(receiver)
                    }

                    if (!hasWifiScanPermission()) {
                        unregisterReceiver(receiver)
                        continuation.resumeWith(
                            Result.failure(
                                SecurityException("Missing Wi-Fi scan permission")
                            )
                        )
                        return@suspendCancellableCoroutine
                    }

                    val started = try {
                        wifiManager.startScan()
                    } catch (exception: SecurityException) {
                        unregisterReceiver(receiver)
                        continuation.resumeWith(Result.failure(exception))
                        return@suspendCancellableCoroutine
                    } catch (_: RuntimeException) {
                        false
                    }
                    if (!started && continuation.isActive) {
                        unregisterReceiver(receiver)
                        continuation.resume(false)
                    }
                }
            } ?: false

            if (!scanUpdated) {
                val cachedSsids = readFreshScanSsids()
                if (cachedSsids.isEmpty()) {
                    ScanReadResult(issue = WifiProximityIssue.SCAN_FAILED)
                } else {
                    ScanReadResult(ssids = cachedSsids)
                }
            } else {
                ScanReadResult(ssids = readFreshScanSsids())
            }
        } catch (_: SecurityException) {
            ScanReadResult(issue = WifiProximityIssue.PERMISSION_DENIED)
        } catch (_: RuntimeException) {
            ScanReadResult(issue = WifiProximityIssue.SCAN_FAILED)
        }
    }

    @Suppress("DEPRECATION")
    private fun readFreshScanSsids(): Set<String> {
        if (!hasWifiScanPermission()) {
            throw SecurityException("Missing Wi-Fi scan permission")
        }
        val nowMicros = SystemClock.elapsedRealtimeNanos() / NANOS_PER_MICROSECOND
        return try {
            wifiManager.scanResults
                .asSequence()
                .filter { result ->
                    result.timestamp > 0L &&
                        nowMicros - result.timestamp in 0..MAX_SCAN_RESULT_AGE_MICROS
                }
                .mapNotNull { result -> result.SSID.normalizedSsid() }
                .toSet()
        } catch (exception: SecurityException) {
            throw exception
        }
    }

    private fun scanIntentFilter(): IntentFilter {
        return IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
    }

    private fun statusIntentFilter(): IntentFilter {
        return IntentFilter().apply {
            addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
            addAction(LocationManager.MODE_CHANGED_ACTION)
        }
    }

    private fun registerReceiver(receiver: BroadcastReceiver, filter: IntentFilter) {
        ContextCompat.registerReceiver(
            context,
            receiver,
            filter,
            ContextCompat.RECEIVER_EXPORTED
        )
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun registerScanResultsCallback(
        scanResultsCallback: WifiManager.ScanResultsCallback
    ) {
        wifiManager.registerScanResultsCallback(
            ContextCompat.getMainExecutor(context),
            scanResultsCallback
        )
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun unregisterScanResultsCallback(
        scanResultsCallback: WifiManager.ScanResultsCallback
    ) {
        wifiManager.unregisterScanResultsCallback(scanResultsCallback)
    }

    private fun unregisterReceiver(receiver: BroadcastReceiver) {
        runCatching { context.unregisterReceiver(receiver) }
    }

    private fun String?.normalizedSsid(): String? {
        return this
            ?.removeSurrounding("\"")
            ?.trim()
            ?.takeIf { it.isNotEmpty() && it != WifiManager.UNKNOWN_SSID }
    }

    private fun hasWifiScanPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.NEARBY_WIFI_DEVICES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private data class ScanReadResult(
        val ssids: Set<String> = emptySet(),
        val issue: WifiProximityIssue? = null
    )

    private companion object {
        const val SCAN_TIMEOUT_MILLIS = 10_000L
        const val NANOS_PER_MICROSECOND = 1_000L
        const val MAX_SCAN_RESULT_AGE_MICROS = 30_000_000L
        const val NETWORK_CALLBACK_SETTLE_MILLIS = 100L
        const val FOREGROUND_REFRESH_INTERVAL_MILLIS = 30_000L
    }
}
