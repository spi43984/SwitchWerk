package de.piecha.switchwerk.data.action

import android.net.Network
import android.util.Log
import de.piecha.switchwerk.data.network.HttpApiCallResult
import de.piecha.switchwerk.data.network.HttpApiCallService
import de.piecha.switchwerk.data.network.WifiConnectionResult
import de.piecha.switchwerk.data.network.WifiConnectionService
import de.piecha.switchwerk.data.repository.WifiProfileRepository
import de.piecha.switchwerk.domain.model.ApiCall
import de.piecha.switchwerk.domain.model.ApiMethod
import de.piecha.switchwerk.domain.model.Device
import de.piecha.switchwerk.domain.model.DeviceConnection
import de.piecha.switchwerk.domain.model.WifiProfile
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
        return actionMutex.withLock {
            try {
                executeSerially(device)
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Exception) {
                DeviceActionResult.UnexpectedError
            }
        }
    }

    private suspend fun executeSerially(device: Device): DeviceActionResult {
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
            logInfo("Starting explicit WiFi attempt ${index + 1}/${availableConnections.size}")

            try {
                when (
                    val connectionResult = wifiConnectionService.connect(
                        ssid = connectionWithProfile.profile.ssid,
                        password = password
                    )
                ) {
                    is WifiConnectionResult.Success -> {
                        logInfo("Requested WiFi network available; starting bound HTTP call")
                        when (
                            val outcome = callApi(
                                connection = connectionWithProfile.connection,
                                apiCall = device.apiCall,
                                network = connectionResult.network
                            )
                        ) {
                            ApiCallOutcome.Success -> return DeviceActionResult.Success
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
                    is WifiConnectionResult.Error -> {
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

    private suspend fun callApi(
        connection: DeviceConnection,
        apiCall: ApiCall,
        network: Network
    ): ApiCallOutcome {
        val url = buildUrl(connection.host, apiCall.path)
            ?: return ApiCallOutcome.Terminal(DeviceActionResult.InvalidRequest)

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
            is HttpApiCallResult.Success -> ApiCallOutcome.Success
            is HttpApiCallResult.HttpError -> {
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
            WifiConnectionResult.Timeout -> "Timeout"
            WifiConnectionResult.Unavailable -> "Unavailable"
            WifiConnectionResult.PermissionDenied -> "PermissionDenied"
            WifiConnectionResult.UnsupportedAndroidVersion -> "UnsupportedAndroidVersion"
            is WifiConnectionResult.Error -> "Error(${cause.javaClass.simpleName})"
        }
    }

    private fun logInfo(message: String) {
        runCatching { Log.i(LOG_TAG, message) }
    }

    private fun logWarning(message: String) {
        runCatching { Log.w(LOG_TAG, message) }
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
    }
}
