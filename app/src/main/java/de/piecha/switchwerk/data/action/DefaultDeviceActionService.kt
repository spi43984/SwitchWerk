package de.piecha.switchwerk.data.action

import android.net.Network
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
        for (connectionWithProfile in availableConnections) {
            val password = wifiProfileRepository.getPassword(connectionWithProfile.profile.id)

            try {
                when (
                    val connectionResult = wifiConnectionService.connect(
                        ssid = connectionWithProfile.profile.ssid,
                        password = password
                    )
                ) {
                    is WifiConnectionResult.Success -> {
                        when (
                            val outcome = callApi(
                                connection = connectionWithProfile.connection,
                                apiCall = device.apiCall,
                                network = connectionResult.network
                            )
                        ) {
                            ApiCallOutcome.Success -> return DeviceActionResult.Success
                            is ApiCallOutcome.Terminal -> return outcome.result
                            ApiCallOutcome.TryNextNetwork -> {
                                lastFailure = DeviceActionResult.NetworkError
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
                        lastFailure = DeviceActionResult.WifiConnectionFailed
                    }
                }
            } finally {
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
                ApiCallOutcome.Terminal(DeviceActionResult.HttpError(result.response.statusCode))
            }

            HttpApiCallResult.Timeout -> ApiCallOutcome.Terminal(DeviceActionResult.Timeout)
            is HttpApiCallResult.InvalidRequest -> {
                ApiCallOutcome.Terminal(DeviceActionResult.InvalidRequest)
            }

            is HttpApiCallResult.NetworkError -> {
                if (result.cause.isSafeBeforeRequestFailure()) {
                    ApiCallOutcome.TryNextNetwork
                } else {
                    ApiCallOutcome.Terminal(DeviceActionResult.NetworkError)
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

    private fun Throwable.isSafeBeforeRequestFailure(): Boolean {
        var current: Throwable? = this
        while (current != null) {
            if (
                current is UnknownHostException ||
                current is ConnectException ||
                current is NoRouteToHostException
            ) {
                return true
            }
            current = current.cause
        }
        return false
    }

    private data class ConnectionWithProfile(
        val connection: DeviceConnection,
        val profile: WifiProfile
    )

    private sealed interface ApiCallOutcome {
        data object Success : ApiCallOutcome

        data object TryNextNetwork : ApiCallOutcome

        data class Terminal(val result: DeviceActionResult) : ApiCallOutcome
    }
}
