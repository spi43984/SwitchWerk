package de.piecha.switchwerk.data.action

import android.net.Network
import android.util.Log
import de.piecha.switchwerk.data.network.HttpApiCallResult
import de.piecha.switchwerk.data.network.HttpApiCallService
import de.piecha.switchwerk.data.network.SecurityAttemptFailure
import de.piecha.switchwerk.data.network.WifiConnectionResult
import de.piecha.switchwerk.data.network.WifiConnectionService
import de.piecha.switchwerk.data.repository.WifiProfileRepository
import de.piecha.switchwerk.domain.model.ApiCall
import de.piecha.switchwerk.domain.model.ApiMethod
import de.piecha.switchwerk.domain.model.Device
import de.piecha.switchwerk.domain.model.DeviceConnection
import de.piecha.switchwerk.domain.model.WifiProfile
import de.piecha.switchwerk.domain.model.WifiSecurityType
import java.net.ConnectException
import java.net.NoRouteToHostException
import java.net.SocketException
import java.net.UnknownHostException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

class DefaultDeviceActionService(
    private val wifiProfileRepository: WifiProfileRepository,
    private val wifiConnectionService: WifiConnectionService,
    private val httpApiCallService: HttpApiCallService
) : DeviceActionService {

    private val actionMutex = Mutex()

    override suspend fun execute(device: Device): DeviceActionResult {
        return execute(device) { }
    }

    override suspend fun execute(
        device: Device,
        onDiagnosticEvent: (DeviceActionDiagnosticEvent) -> Unit
    ): DeviceActionResult {
        return actionMutex.withLock {
            emitDiagnostic(onDiagnosticEvent, DeviceActionDiagnosticEvent.ActionStarted)
            val result = try {
                executeSerially(device, onDiagnosticEvent)
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Exception) {
                DeviceActionResult.UnexpectedError
            }
            if (result != DeviceActionResult.Success) {
                emitDiagnostic(onDiagnosticEvent, DeviceActionDiagnosticEvent.RequestFailed)
            }
            emitDiagnostic(onDiagnosticEvent, DeviceActionDiagnosticEvent.ActionCompleted)
            result
        }
    }

    private suspend fun executeSerially(
        device: Device,
        onDiagnosticEvent: (DeviceActionDiagnosticEvent) -> Unit
    ): DeviceActionResult {
        if (device.connections.isEmpty()) {
            return DeviceActionResult.NoConnections
        }

        val profilesById = wifiProfileRepository.getWifiProfiles().associateBy { it.id }
        val availableConnections = device.connections.mapNotNull { connection ->
            profilesById[connection.wifiProfileId]?.let { profile ->
                ConnectionWithProfile(connection, profile)
            }
        }
        if (availableConnections.isEmpty()) {
            return DeviceActionResult.NoConnections
        }

        var lastFailure: DeviceActionResult = DeviceActionResult.WifiConnectionFailed
        for ((index, connectionWithProfile) in availableConnections.withIndex()) {
            val password = wifiProfileRepository.getPassword(connectionWithProfile.profile.id)
            emitDiagnostic(
                onDiagnosticEvent,
                DeviceActionDiagnosticEvent.WifiProfileConnecting(
                    connectionWithProfile.profile.name
                )
            )
            logInfo("Starting explicit WiFi profile attempt ${index + 1}/${availableConnections.size}")

            try {
                when (val connectionResult = connectWifiProfile(connectionWithProfile.profile, password)) {
                    is WifiConnectionResult.Success -> {
                        emitDiagnostic(
                            onDiagnosticEvent,
                            DeviceActionDiagnosticEvent.WifiConnectionSucceeded
                        )
                        emitDiagnostic(
                            onDiagnosticEvent,
                            DeviceActionDiagnosticEvent.DeviceAddress(
                                diagnosticAddress(connectionWithProfile.connection.host)
                            )
                        )
                        logInfo("Requested WiFi network available; starting bound HTTP call")
                        when (
                            val outcome = callApi(
                                connection = connectionWithProfile.connection,
                                apiCall = device.apiCall,
                                network = connectionResult.network,
                                onDiagnosticEvent = onDiagnosticEvent
                            )
                        ) {
                            ApiCallOutcome.Success -> {
                                emitDiagnostic(
                                    onDiagnosticEvent,
                                    DeviceActionDiagnosticEvent.RequestSucceeded
                                )
                                return DeviceActionResult.Success
                            }
                            is ApiCallOutcome.Terminal -> return outcome.result
                            is ApiCallOutcome.TryNextNetwork -> {
                                lastFailure = DeviceActionResult.NetworkError(outcome.reason)
                            }
                        }
                    }

                    WifiConnectionResult.PermissionDenied -> {
                        return DeviceActionResult.WifiPermissionDenied
                    }

                    WifiConnectionResult.UnsupportedAndroidVersion -> {
                        return DeviceActionResult.UnsupportedAndroidVersion
                    }

                    WifiConnectionResult.Timeout,
                    WifiConnectionResult.Unavailable,
                    is WifiConnectionResult.SecurityTypesFailed,
                    is WifiConnectionResult.Error -> {
                        emitDiagnostic(
                            onDiagnosticEvent,
                            DeviceActionDiagnosticEvent.WifiConnectionFailed
                        )
                        logWarning("Explicit WiFi attempt failed: ${connectionResult.logName()}")
                        lastFailure = DeviceActionResult.WifiConnectionFailed
                    }
                }
            } finally {
                logInfo("Releasing requested WiFi network")
                wifiConnectionService.disconnect()
            }
        }

        return lastFailure
    }

    private suspend fun connectWifiProfile(
        profile: WifiProfile,
        password: String?
    ): WifiConnectionResult {
        val startedAtNanos = System.nanoTime()
        val securityTypes = securityAttemptOrder(profile.lastSuccessfulSecurityType)
        val failures = mutableListOf<SecurityAttemptFailure>()
        var lastResult: WifiConnectionResult = WifiConnectionResult.Timeout

        securityTypes.forEachIndexed { index, securityType ->
            val remainingTimeout = remainingTimeoutMillis(startedAtNanos)
            if (remainingTimeout <= 0L) {
                return securityFailureResult(failures, lastResult)
            }

            logInfo("Starting explicit WiFi security attempt ${index + 1}/${securityTypes.size}")
            val connectionResult = wifiConnectionService.connect(
                ssid = profile.ssid,
                password = password,
                securityType = securityType,
                timeoutMillis = perAttemptTimeoutMillis(
                    remainingTimeout = remainingTimeout,
                    attemptsLeftAfterThis = securityTypes.lastIndex - index
                )
            )
            lastResult = connectionResult

            when (connectionResult) {
                is WifiConnectionResult.Success -> {
                    wifiProfileRepository.updateLastSuccessfulSecurityType(profile.id, securityType)
                    return connectionResult
                }

                WifiConnectionResult.PermissionDenied,
                WifiConnectionResult.UnsupportedAndroidVersion -> return connectionResult

                WifiConnectionResult.Timeout,
                WifiConnectionResult.Unavailable,
                is WifiConnectionResult.SecurityTypesFailed,
                is WifiConnectionResult.Error -> {
                    failures += SecurityAttemptFailure(securityType, connectionResult)
                    if (index == securityTypes.lastIndex || !connectionResult.allowsSecurityFallback()) {
                        return securityFailureResult(failures, connectionResult)
                    }
                    logWarning("Explicit WiFi security attempt failed; trying fallback type")
                }
            }
        }

        return lastResult
    }

    private fun securityFailureResult(
        failures: List<SecurityAttemptFailure>,
        fallbackResult: WifiConnectionResult
    ): WifiConnectionResult {
        return if (failures.size >= 2) {
            WifiConnectionResult.SecurityTypesFailed(failures)
        } else {
            fallbackResult
        }
    }

    private fun securityAttemptOrder(
        preferredSecurityType: WifiSecurityType?
    ): List<WifiSecurityType> {
        val first = preferredSecurityType ?: WifiSecurityType.WPA2
        return listOf(first, first.fallback())
    }

    private fun remainingTimeoutMillis(startedAtNanos: Long): Long {
        val elapsedMillis = (System.nanoTime() - startedAtNanos) / NANOS_PER_MILLISECOND
        return WifiConnectionService.DEFAULT_TIMEOUT_MILLIS - elapsedMillis
    }

    private fun perAttemptTimeoutMillis(
        remainingTimeout: Long,
        attemptsLeftAfterThis: Int
    ): Long {
        if (attemptsLeftAfterThis <= 0) {
            return remainingTimeout
        }
        return remainingTimeout.coerceAtMost(FIRST_SECURITY_ATTEMPT_TIMEOUT_MILLIS)
    }

    private suspend fun callApi(
        connection: DeviceConnection,
        apiCall: ApiCall,
        network: Network,
        onDiagnosticEvent: (DeviceActionDiagnosticEvent) -> Unit
    ): ApiCallOutcome {
        val url = buildUrl(connection.host, apiCall.path)
            ?: return ApiCallOutcome.Terminal(DeviceActionResult.InvalidRequest)

        emitDiagnostic(
            onDiagnosticEvent,
            DeviceActionDiagnosticEvent.HttpRequestStarted(
                method = apiCall.method,
                address = diagnosticAddress(connection.host)
            )
        )

        val result = when (apiCall.method) {
            ApiMethod.GET -> httpApiCallService.get(
                url = url,
                network = network
            )

            ApiMethod.POST -> httpApiCallService.post(
                url = url,
                body = apiCall.optionalPayload,
                network = network
            )
        }

        return when (result) {
            is HttpApiCallResult.Success -> {
                emitDiagnostic(
                    onDiagnosticEvent,
                    DeviceActionDiagnosticEvent.HttpResponseReceived(
                        result.response.statusCode
                    )
                )
                ApiCallOutcome.Success
            }
            is HttpApiCallResult.HttpError -> {
                emitDiagnostic(
                    onDiagnosticEvent,
                    DeviceActionDiagnosticEvent.HttpResponseReceived(
                        result.response.statusCode
                    )
                )
                logWarning("Bound HTTP call returned status ${result.response.statusCode}")
                ApiCallOutcome.Terminal(DeviceActionResult.HttpError(result.response.statusCode))
            }

            HttpApiCallResult.Timeout -> {
                logWarning("Bound HTTP call timed out")
                ApiCallOutcome.Terminal(DeviceActionResult.Timeout)
            }
            is HttpApiCallResult.InvalidRequest -> {
                logWarning("Bound HTTP request is invalid")
                ApiCallOutcome.Terminal(DeviceActionResult.InvalidRequest)
            }

            is HttpApiCallResult.NetworkError -> {
                val reason = result.cause.toNetworkFailureReason()
                when (reason) {
                    NetworkFailureReason.DNS -> emitDiagnostic(
                        onDiagnosticEvent,
                        DeviceActionDiagnosticEvent.DnsResolutionFailed
                    )
                    NetworkFailureReason.CONNECTION,
                    NetworkFailureReason.NO_ROUTE -> emitDiagnostic(
                        onDiagnosticEvent,
                        DeviceActionDiagnosticEvent.DeviceNotReachable
                    )
                    NetworkFailureReason.VPN_BLOCKED,
                    NetworkFailureReason.OTHER -> Unit
                }
                logWarning(
                    "Bound HTTP call failed: ${result.cause.javaClass.simpleName}; reason=$reason"
                )
                if (
                    reason == NetworkFailureReason.DNS ||
                    reason == NetworkFailureReason.CONNECTION ||
                    reason == NetworkFailureReason.NO_ROUTE
                ) {
                    ApiCallOutcome.TryNextNetwork(reason)
                } else {
                    ApiCallOutcome.Terminal(DeviceActionResult.NetworkError(reason))
                }
            }
        }
    }

    private fun buildUrl(host: String, path: String): String? {
        val normalizedHost = host.trim().let {
            if (it.startsWith("http://") || it.startsWith("https://")) {
                it
            } else {
                "http://$it"
            }
        }
        val baseUrl = "${normalizedHost.trimEnd('/')}/".toHttpUrlOrNull() ?: return null
        return baseUrl.resolve(path.trim().trimStart('/'))?.toString()
    }

    private fun diagnosticAddress(host: String): String {
        val normalizedHost = host.trim().let {
            if (it.startsWith("http://") || it.startsWith("https://")) {
                it
            } else {
                "http://$it"
            }
        }
        val url = normalizedHost.toHttpUrlOrNull() ?: return "ungültige Geräteadresse"
        val defaultPort = if (url.isHttps) 443 else 80
        return if (url.port == defaultPort) url.host else "${url.host}:${url.port}"
    }

    private fun Throwable.toNetworkFailureReason(): NetworkFailureReason {
        var current: Throwable? = this
        while (current != null) {
            when (current) {
                is UnknownHostException -> return NetworkFailureReason.DNS
                is NoRouteToHostException -> return NetworkFailureReason.NO_ROUTE
                is ConnectException -> return NetworkFailureReason.CONNECTION
                is SocketException -> {
                    if (
                        current.message?.contains("EPERM", ignoreCase = true) == true ||
                        current.message?.contains(
                            "Operation not permitted",
                            ignoreCase = true
                        ) == true
                    ) {
                        return NetworkFailureReason.VPN_BLOCKED
                    }
                }
            }
            current = current.cause
        }
        return NetworkFailureReason.OTHER
    }

    private fun WifiConnectionResult.logName(): String {
        return when (this) {
            is WifiConnectionResult.Success -> "Success"
            is WifiConnectionResult.SecurityTypesFailed -> "SecurityTypesFailed"
            WifiConnectionResult.Timeout -> "Timeout"
            WifiConnectionResult.Unavailable -> "Unavailable"
            WifiConnectionResult.PermissionDenied -> "PermissionDenied"
            WifiConnectionResult.UnsupportedAndroidVersion -> "UnsupportedAndroidVersion"
            is WifiConnectionResult.Error -> "Error(${cause.javaClass.simpleName})"
        }
    }

    private fun WifiConnectionResult.allowsSecurityFallback(): Boolean {
        return when (this) {
            WifiConnectionResult.Timeout,
            WifiConnectionResult.Unavailable -> true

            is WifiConnectionResult.Error -> cause !is IllegalArgumentException

            is WifiConnectionResult.Success,
            is WifiConnectionResult.SecurityTypesFailed,
            WifiConnectionResult.PermissionDenied,
            WifiConnectionResult.UnsupportedAndroidVersion -> false
        }
    }

    private fun logInfo(message: String) {
        runCatching { Log.i(LOG_TAG, message) }
    }

    private fun logWarning(message: String) {
        runCatching { Log.w(LOG_TAG, message) }
    }

    private fun emitDiagnostic(
        callback: (DeviceActionDiagnosticEvent) -> Unit,
        event: DeviceActionDiagnosticEvent
    ) {
        runCatching { callback(event) }
    }

    private data class ConnectionWithProfile(
        val connection: DeviceConnection,
        val profile: WifiProfile
    )

    private sealed interface ApiCallOutcome {
        data object Success : ApiCallOutcome

        data class TryNextNetwork(val reason: NetworkFailureReason) : ApiCallOutcome

        data class Terminal(val result: DeviceActionResult) : ApiCallOutcome
    }

    private companion object {
        const val LOG_TAG = "SwitchWerkNetwork"
        const val NANOS_PER_MILLISECOND = 1_000_000L
        const val FIRST_SECURITY_ATTEMPT_TIMEOUT_MILLIS = 10_000L
    }
}
