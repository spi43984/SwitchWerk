package de.piecha.switchwerk.data.action

import android.net.Network
import android.util.Log
import de.piecha.switchwerk.data.network.DnsResolutionResult
import de.piecha.switchwerk.data.network.HttpApiCallResult
import de.piecha.switchwerk.data.network.HttpApiCallService
import de.piecha.switchwerk.data.network.SecurityAttemptFailure
import de.piecha.switchwerk.data.network.WifiConnectionProgress
import de.piecha.switchwerk.data.network.WifiConnectionResult
import de.piecha.switchwerk.data.network.WifiConnectionService
import de.piecha.switchwerk.data.repository.WifiProfileRepository
import de.piecha.switchwerk.domain.model.ApiCall
import de.piecha.switchwerk.domain.model.ApiMethod
import de.piecha.switchwerk.domain.model.Device
import de.piecha.switchwerk.domain.model.DeviceConnection
import de.piecha.switchwerk.domain.model.DeviceProtocol
import de.piecha.switchwerk.domain.model.WifiProfile
import de.piecha.switchwerk.domain.model.WifiConnectionMode
import de.piecha.switchwerk.domain.model.WifiSecurityType
import java.net.ConnectException
import java.net.NoRouteToHostException
import java.net.SocketException
import java.net.UnknownHostException
import java.security.cert.CertificateException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import javax.net.ssl.SSLException

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
            emitDiagnostic(
                onDiagnosticEvent,
                DeviceActionDiagnosticEvent.WifiProfileAttempt(
                    profileName = connectionWithProfile.profile.name,
                    index = index + 1,
                    total = availableConnections.size
                )
            )
            if (connectionWithProfile.profile.connectionMode == WifiConnectionMode.ANDROID_MANAGED) {
                val activeNetwork = wifiConnectionService.activeWifiNetworkForSsid(
                    connectionWithProfile.profile.ssid
                ) ?: return DeviceActionResult.AndroidManagedWifiNotActive(
                    connectionWithProfile.profile.ssid
                )
                when (
                    val outcome = callApi(
                        connection = connectionWithProfile.connection,
                        protocol = device.protocol,
                        apiCall = device.apiCall,
                        network = activeNetwork,
                        onDiagnosticEvent = onDiagnosticEvent
                    )
                ) {
                    ApiCallOutcome.Success -> {
                        emitDiagnostic(onDiagnosticEvent, DeviceActionDiagnosticEvent.RequestSucceeded)
                        return DeviceActionResult.Success
                    }
                    is ApiCallOutcome.Terminal -> return outcome.result
                    is ApiCallOutcome.TryNextNetwork -> {
                        lastFailure = DeviceActionResult.NetworkError(outcome.reason)
                        continue
                    }
                }
            }
            val password = wifiProfileRepository.getPassword(connectionWithProfile.profile.id)
            logInfo("Starting explicit WiFi profile attempt ${index + 1}/${availableConnections.size}")

            try {
                when (
                    val connectionResult = connectWifiProfile(
                        profile = connectionWithProfile.profile,
                        password = password,
                        onDiagnosticEvent = onDiagnosticEvent
                    )
                ) {
                    is WifiConnectionResult.Success -> {
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
                                protocol = device.protocol,
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

                    WifiConnectionResult.MissingPassword -> {
                        emitDiagnostic(
                            onDiagnosticEvent,
                            DeviceActionDiagnosticEvent.MissingWifiPassword
                        )
                        lastFailure = DeviceActionResult.MissingWifiPassword
                    }

                    WifiConnectionResult.WifiDisabled -> {
                        emitDiagnostic(
                            onDiagnosticEvent,
                            DeviceActionDiagnosticEvent.WifiDisabled
                        )
                        return DeviceActionResult.WifiDisabled
                    }

                    WifiConnectionResult.UnsupportedAndroidVersion -> {
                        return DeviceActionResult.UnsupportedAndroidVersion
                    }

                    WifiConnectionResult.Timeout,
                    WifiConnectionResult.NetworkRequestTimeout,
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
        password: String?,
        onDiagnosticEvent: (DeviceActionDiagnosticEvent) -> Unit
    ): WifiConnectionResult {
        val detectedSecurityTypes = if (profile.isSecurityTypeVerifiedLocally) {
            null
        } else {
            emitDiagnostic(
                onDiagnosticEvent,
                DeviceActionDiagnosticEvent.WifiSecurityDetectionStarted
            )
            wifiConnectionService.detectedSecurityTypes(profile.ssid).also { detected ->
                emitDiagnostic(
                    onDiagnosticEvent,
                    if (detected.isNullOrEmpty()) {
                        DeviceActionDiagnosticEvent.WifiSecurityDetectionUnavailable
                    } else {
                        DeviceActionDiagnosticEvent.WifiSecurityDetectionSucceeded
                    }
                )
            }
        }
        val startedAtNanos = System.nanoTime()
        if (password.isNullOrEmpty() && requiresPassword(profile, detectedSecurityTypes)) {
            return WifiConnectionResult.MissingPassword
        }
        val securityTypes = securityAttemptOrder(
            preferredSecurityType = profile.lastSuccessfulSecurityType,
            detectedSecurityTypes = detectedSecurityTypes
        )
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
                ),
                onProgress = { progress ->
                    val event = when (progress) {
                        WifiConnectionProgress.RequestStarted -> {
                            DeviceActionDiagnosticEvent.WifiRequestStarted(profile.name)
                        }
                        WifiConnectionProgress.NetworkFound -> {
                            DeviceActionDiagnosticEvent.WifiFound
                        }
                        WifiConnectionProgress.Connected -> {
                            DeviceActionDiagnosticEvent.WifiConnected
                        }
                        WifiConnectionProgress.IpAddressAvailable -> {
                            DeviceActionDiagnosticEvent.IpAddressReceived
                        }
                    }
                    emitDiagnostic(onDiagnosticEvent, event)
                }
            )
            lastResult = connectionResult

            when (connectionResult) {
                is WifiConnectionResult.Success -> {
                    wifiProfileRepository.updateLastSuccessfulSecurityType(profile.id, securityType)
                    return connectionResult
                }

                WifiConnectionResult.PermissionDenied,
                WifiConnectionResult.WifiDisabled,
                WifiConnectionResult.MissingPassword,
                WifiConnectionResult.UnsupportedAndroidVersion -> return connectionResult

                WifiConnectionResult.Timeout,
                WifiConnectionResult.NetworkRequestTimeout,
                WifiConnectionResult.Unavailable,
                is WifiConnectionResult.SecurityTypesFailed,
                is WifiConnectionResult.Error -> {
                    if (
                        connectionResult == WifiConnectionResult.Timeout ||
                        connectionResult == WifiConnectionResult.NetworkRequestTimeout
                    ) {
                        emitDiagnostic(
                            onDiagnosticEvent,
                            DeviceActionDiagnosticEvent.Timeout(
                                if (
                                    connectionResult ==
                                    WifiConnectionResult.NetworkRequestTimeout
                                ) {
                                    DiagnosticStage.WIFI_REQUEST
                                } else {
                                    DiagnosticStage.WIFI
                                }
                            )
                        )
                    }
                    failures += SecurityAttemptFailure(securityType, connectionResult)
                    if (
                        index == securityTypes.lastIndex ||
                        !connectionResult.allowsSecurityFallback(
                            nextSecurityType = securityTypes[index + 1],
                            detectedSecurityTypes = detectedSecurityTypes
                        )
                    ) {
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
        preferredSecurityType: WifiSecurityType?,
        detectedSecurityTypes: Set<WifiSecurityType>?
    ): List<WifiSecurityType> {
        val first = preferredSecurityType ?: WifiSecurityType.WPA2
        val configuredOrder = listOf(first, first.fallback())
        if (detectedSecurityTypes.isNullOrEmpty()) {
            return listOf(first)
        }
        return configuredOrder.filter(detectedSecurityTypes::contains)
            .ifEmpty { listOf(first) }
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
        protocol: DeviceProtocol,
        apiCall: ApiCall,
        network: Network,
        onDiagnosticEvent: (DeviceActionDiagnosticEvent) -> Unit
    ): ApiCallOutcome {
        val url = buildUrl(connection.host, protocol, apiCall.path)
            ?: return ApiCallOutcome.Terminal(DeviceActionResult.InvalidRequest)
        val parsedUrl = url.toHttpUrlOrNull()
            ?: return ApiCallOutcome.Terminal(DeviceActionResult.InvalidRequest)

        if (!parsedUrl.host.isIpAddress()) {
            emitDiagnostic(
                onDiagnosticEvent,
                DeviceActionDiagnosticEvent.DnsResolutionStarted(
                    diagnosticAddress(connection.host)
                )
            )
            when (val dnsResult = httpApiCallService.resolveHost(parsedUrl.host, network)) {
                DnsResolutionResult.Success -> emitDiagnostic(
                    onDiagnosticEvent,
                    DeviceActionDiagnosticEvent.DnsResolutionSucceeded
                )
                DnsResolutionResult.Timeout -> {
                    emitDiagnostic(
                        onDiagnosticEvent,
                        DeviceActionDiagnosticEvent.Timeout(DiagnosticStage.DNS)
                    )
                    return ApiCallOutcome.TryNextNetwork(NetworkFailureReason.DNS)
                }
                is DnsResolutionResult.Error -> {
                    emitDiagnostic(
                        onDiagnosticEvent,
                        DeviceActionDiagnosticEvent.DnsResolutionFailed
                    )
                    logWarning(
                        "Bound DNS lookup failed: ${dnsResult.cause.javaClass.simpleName}"
                    )
                    return ApiCallOutcome.TryNextNetwork(NetworkFailureReason.DNS)
                }
            }
        }

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
                    DeviceActionDiagnosticEvent.HttpRequestSucceeded(
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
                emitDiagnostic(
                    onDiagnosticEvent,
                    DeviceActionDiagnosticEvent.Timeout(DiagnosticStage.HTTP)
                )
                logWarning("Bound HTTP call timed out")
                ApiCallOutcome.Terminal(DeviceActionResult.Timeout)
            }
            is HttpApiCallResult.InvalidRequest -> {
                logWarning("Bound HTTP request is invalid")
                ApiCallOutcome.Terminal(DeviceActionResult.InvalidRequest)
            }

            is HttpApiCallResult.NetworkError -> {
                val reason = result.cause.toNetworkFailureReason()
                if (reason == NetworkFailureReason.TLS_CERTIFICATE) {
                    logWarning("Bound HTTPS call failed: ${result.cause.javaClass.simpleName}")
                    return ApiCallOutcome.Terminal(DeviceActionResult.TlsCertificateError)
                }
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
                    NetworkFailureReason.TLS_CERTIFICATE,
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

    private fun buildUrl(host: String, protocol: DeviceProtocol, path: String): String? {
        val normalizedHost = "${protocol.scheme}://${host.trim().removeHttpScheme()}"
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

    private fun String.removeHttpScheme(): String {
        return removePrefix("http://").removePrefix("https://")
    }

    private fun String.isIpAddress(): Boolean {
        if (contains(':')) {
            return true
        }
        val parts = split('.')
        return parts.size == 4 && parts.all { part ->
            part.isNotEmpty() && part.length <= 3 &&
                part.all(Char::isDigit) && part.toIntOrNull() in 0..255
        }
    }

    private fun Throwable.toNetworkFailureReason(): NetworkFailureReason {
        var current: Throwable? = this
        while (current != null) {
            when (current) {
                is SSLException,
                is CertificateException -> return NetworkFailureReason.TLS_CERTIFICATE
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
            WifiConnectionResult.NetworkRequestTimeout -> "NetworkRequestTimeout"
            WifiConnectionResult.Unavailable -> "Unavailable"
            WifiConnectionResult.MissingPassword -> "MissingPassword"
            WifiConnectionResult.PermissionDenied -> "PermissionDenied"
            WifiConnectionResult.WifiDisabled -> "WifiDisabled"
            WifiConnectionResult.UnsupportedAndroidVersion -> "UnsupportedAndroidVersion"
            is WifiConnectionResult.Error -> "Error(${cause.javaClass.simpleName})"
        }
    }

    private fun WifiConnectionResult.allowsSecurityFallback(
        nextSecurityType: WifiSecurityType,
        detectedSecurityTypes: Set<WifiSecurityType>?
    ): Boolean {
        if (nextSecurityType in detectedSecurityTypes.orEmpty()) {
            return true
        }
        return false
    }

    private fun requiresPassword(
        profile: WifiProfile,
        detectedSecurityTypes: Set<WifiSecurityType>?
    ): Boolean {
        if (!detectedSecurityTypes.isNullOrEmpty()) {
            return detectedSecurityTypes.any { it.requiresPassword() }
        }
        return profile.isSecurityTypeVerifiedLocally &&
            profile.lastSuccessfulSecurityType?.requiresPassword() == true
    }

    private fun WifiSecurityType.requiresPassword(): Boolean {
        return when (this) {
            WifiSecurityType.WPA2,
            WifiSecurityType.WPA3 -> true
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
        const val FIRST_SECURITY_ATTEMPT_TIMEOUT_MILLIS = 25_000L
    }
}
