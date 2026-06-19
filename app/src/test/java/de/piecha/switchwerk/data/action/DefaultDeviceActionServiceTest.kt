package de.piecha.switchwerk.data.action

import android.net.Network
import de.piecha.switchwerk.data.network.HttpApiCallResult
import de.piecha.switchwerk.data.network.HttpApiCallService
import de.piecha.switchwerk.data.network.HttpApiResponse
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
import java.net.SocketException
import java.net.UnknownHostException
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.mock

class DefaultDeviceActionServiceTest {

    @Test
    fun actionUsesOnlyExplicitlyRequestedWifiNetwork() = runBlocking {
        val requestedNetwork = mock(Network::class.java)
        val wifiService = FakeWifiConnectionService(
            results = ArrayDeque(listOf(WifiConnectionResult.Success(requestedNetwork)))
        )
        val httpService = FakeHttpApiCallService(
            results = ArrayDeque(listOf(successResult()))
        )
        val service = createService(
            profiles = listOf(WifiProfile("wifi-1", "Device WiFi")),
            wifiService = wifiService,
            httpService = httpService
        )

        val diagnosticEvents = mutableListOf<DeviceActionDiagnosticEvent>()
        val result = service.execute(
            device(
                method = ApiMethod.GET,
                connections = listOf(DeviceConnection("wifi-1", "192.168.1.10"))
            ),
            diagnosticEvents::add
        )

        assertEquals(DeviceActionResult.Success, result)
        assertEquals(1, httpService.calls.size)
        assertEquals("GET", httpService.calls.single().method)
        assertEquals("http://192.168.1.10/rpc/action", httpService.calls.single().url)
        assertSame(requestedNetwork, httpService.calls.single().network)
        assertEquals(listOf("Device WiFi"), wifiService.requestedSsids)
        assertEquals(1, wifiService.disconnectCount)
        assertTrue(
            diagnosticEvents.contains(
                DeviceActionDiagnosticEvent.DeviceAddress("192.168.1.10")
            )
        )
        assertTrue(
            diagnosticEvents.contains(
                DeviceActionDiagnosticEvent.HttpRequestStarted(
                    method = ApiMethod.GET,
                    address = "192.168.1.10"
                )
            )
        )
        assertTrue(
            diagnosticEvents.contains(DeviceActionDiagnosticEvent.HttpResponseReceived(200))
        )
    }

    @Test
    fun shellyGetRequestPreservesRpcPathAndQueryParameters() = runBlocking {
        val requestedNetwork = mock(Network::class.java)
        val httpService = FakeHttpApiCallService(
            results = ArrayDeque(listOf(successResult()))
        )
        val service = createService(
            profiles = listOf(WifiProfile("wifi-1", "TEAMWERK-Tor")),
            wifiService = FakeWifiConnectionService(
                results = ArrayDeque(listOf(WifiConnectionResult.Success(requestedNetwork)))
            ),
            httpService = httpService
        )

        val result = service.execute(
            device(
                path = "rpc/Switch.Set?id=0&on=true&toggle_after=1",
                connections = listOf(DeviceConnection("wifi-1", "192.168.33.1"))
            )
        )

        assertEquals(DeviceActionResult.Success, result)
        assertEquals(
            "http://192.168.33.1/rpc/Switch.Set?id=0&on=true&toggle_after=1",
            httpService.calls.single().url
        )
        assertSame(requestedNetwork, httpService.calls.single().network)
    }

    @Test
    fun profilesAreRequestedInStoredOrderAndPostIsSupported() = runBlocking {
        val secondNetwork = mock(Network::class.java)
        val wifiService = FakeWifiConnectionService(
            results = ArrayDeque(
                listOf(
                    WifiConnectionResult.Unavailable,
                    WifiConnectionResult.Unavailable,
                    WifiConnectionResult.Success(secondNetwork)
                )
            )
        )
        val httpService = FakeHttpApiCallService(
            results = ArrayDeque(listOf(successResult()))
        )
        val service = createService(
            profiles = listOf(
                WifiProfile("wifi-1", "First WiFi"),
                WifiProfile("wifi-2", "Second WiFi")
            ),
            wifiService = wifiService,
            httpService = httpService
        )

        val result = service.execute(
            device(
                method = ApiMethod.POST,
                payload = """{"on":true}""",
                connections = listOf(
                    DeviceConnection("wifi-1", "first.local"),
                    DeviceConnection("wifi-2", "second.local")
                )
            )
        )

        assertEquals(DeviceActionResult.Success, result)
        assertEquals(listOf("First WiFi", "First WiFi", "Second WiFi"), wifiService.requestedSsids)
        assertEquals(
            listOf(WifiSecurityType.WPA2, WifiSecurityType.WPA3, WifiSecurityType.WPA2),
            wifiService.requestedSecurityTypes
        )
        assertEquals(2, wifiService.disconnectCount)
        assertEquals("POST", httpService.calls.single().method)
        assertEquals("""{"on":true}""", httpService.calls.single().body)
        assertSame(secondNetwork, httpService.calls.single().network)
    }

    @Test
    fun missingStoredSecurityTypeTriesWpa2FirstAndStoresSuccessfulFallback() = runBlocking {
        val network = mock(Network::class.java)
        val wifiRepository = FakeWifiProfileRepository(
            profiles = listOf(WifiProfile("wifi-1", "Device WiFi"))
        )
        val wifiService = FakeWifiConnectionService(
            results = ArrayDeque(
                listOf(
                    WifiConnectionResult.Unavailable,
                    WifiConnectionResult.Success(network)
                )
            )
        )
        val service = createService(
            wifiProfileRepository = wifiRepository,
            wifiService = wifiService,
            httpService = FakeHttpApiCallService(ArrayDeque(listOf(successResult())))
        )

        val result = service.execute(
            device(
                connections = listOf(DeviceConnection("wifi-1", "192.168.33.1"))
            )
        )

        assertEquals(DeviceActionResult.Success, result)
        assertEquals(listOf(WifiSecurityType.WPA2, WifiSecurityType.WPA3), wifiService.requestedSecurityTypes)
        assertEquals(WifiSecurityType.WPA3, wifiRepository.savedSecurityTypes["wifi-1"])
        assertEquals(2, wifiService.requestedTimeouts.size)
        assertEquals(10_000L, wifiService.requestedTimeouts.first())
        assertTrue(wifiService.requestedTimeouts.last() >= 9_000L)
        assertTrue(wifiService.requestedTimeouts.last() <= WifiConnectionService.DEFAULT_TIMEOUT_MILLIS)
    }

    @Test
    fun storedWpa3IsTriedFirstAndFallsBackToWpa2() = runBlocking {
        val network = mock(Network::class.java)
        val wifiRepository = FakeWifiProfileRepository(
            profiles = listOf(
                WifiProfile(
                    id = "wifi-1",
                    ssid = "Device WiFi",
                    lastSuccessfulSecurityType = WifiSecurityType.WPA3
                )
            )
        )
        val wifiService = FakeWifiConnectionService(
            results = ArrayDeque(
                listOf(
                    WifiConnectionResult.Timeout,
                    WifiConnectionResult.Success(network)
                )
            )
        )
        val service = createService(
            wifiProfileRepository = wifiRepository,
            wifiService = wifiService,
            httpService = FakeHttpApiCallService(ArrayDeque(listOf(successResult())))
        )

        val result = service.execute(
            device(
                connections = listOf(DeviceConnection("wifi-1", "192.168.33.1"))
            )
        )

        assertEquals(DeviceActionResult.Success, result)
        assertEquals(listOf(WifiSecurityType.WPA3, WifiSecurityType.WPA2), wifiService.requestedSecurityTypes)
        assertEquals(WifiSecurityType.WPA2, wifiRepository.savedSecurityTypes["wifi-1"])
    }

    @Test
    fun appWifiErrorDoesNotTriggerSecurityFallback() = runBlocking {
        val network = mock(Network::class.java)
        val wifiService = FakeWifiConnectionService(
            results = ArrayDeque(
                listOf(
                    WifiConnectionResult.Error(IllegalArgumentException("invalid request")),
                    WifiConnectionResult.Success(network)
                )
            )
        )
        val service = createService(
            profiles = listOf(
                WifiProfile("wifi-1", "First WiFi"),
                WifiProfile("wifi-2", "Second WiFi")
            ),
            wifiService = wifiService,
            httpService = FakeHttpApiCallService(ArrayDeque(listOf(successResult())))
        )

        val result = service.execute(
            device(
                connections = listOf(
                    DeviceConnection("wifi-1", "first.local"),
                    DeviceConnection("wifi-2", "second.local")
                )
            )
        )

        assertEquals(DeviceActionResult.Success, result)
        assertEquals(listOf("First WiFi", "Second WiFi"), wifiService.requestedSsids)
        assertEquals(listOf(WifiSecurityType.WPA2, WifiSecurityType.WPA2), wifiService.requestedSecurityTypes)
    }

    @Test
    fun bothSecurityTypesFailedContinuesWithNextWifiProfile() = runBlocking {
        val network = mock(Network::class.java)
        val wifiService = FakeWifiConnectionService(
            results = ArrayDeque(
                listOf(
                    WifiConnectionResult.Unavailable,
                    WifiConnectionResult.Timeout,
                    WifiConnectionResult.Success(network)
                )
            )
        )
        val service = createService(
            profiles = listOf(
                WifiProfile("wifi-1", "First WiFi"),
                WifiProfile("wifi-2", "Second WiFi")
            ),
            wifiService = wifiService,
            httpService = FakeHttpApiCallService(ArrayDeque(listOf(successResult())))
        )

        val result = service.execute(
            device(
                connections = listOf(
                    DeviceConnection("wifi-1", "first.local"),
                    DeviceConnection("wifi-2", "second.local")
                )
            )
        )

        assertEquals(DeviceActionResult.Success, result)
        assertEquals(listOf("First WiFi", "First WiFi", "Second WiFi"), wifiService.requestedSsids)
        assertEquals(
            listOf(WifiSecurityType.WPA2, WifiSecurityType.WPA3, WifiSecurityType.WPA2),
            wifiService.requestedSecurityTypes
        )
    }

    @Test
    fun dnsFailureTriesNextProfileEvenWhenSsidIsIdentical() = runBlocking {
        val firstNetwork = mock(Network::class.java)
        val secondNetwork = mock(Network::class.java)
        val wifiService = FakeWifiConnectionService(
            results = ArrayDeque(
                listOf(
                    WifiConnectionResult.Success(firstNetwork),
                    WifiConnectionResult.Success(secondNetwork)
                )
            )
        )
        val httpService = FakeHttpApiCallService(
            results = ArrayDeque(
                listOf(
                    HttpApiCallResult.NetworkError(UnknownHostException()),
                    successResult()
                )
            )
        )
        val service = createService(
            profiles = listOf(
                WifiProfile("wifi-1", "Device WiFi"),
                WifiProfile("wifi-2", "Device WiFi")
            ),
            wifiService = wifiService,
            httpService = httpService
        )

        val diagnosticEvents = mutableListOf<DeviceActionDiagnosticEvent>()
        val result = service.execute(
            device(
                connections = listOf(
                    DeviceConnection("wifi-1", "first.local"),
                    DeviceConnection("wifi-2", "second.local")
                )
            ),
            diagnosticEvents::add
        )

        assertEquals(DeviceActionResult.Success, result)
        assertEquals(listOf("Device WiFi", "Device WiFi"), wifiService.requestedSsids)
        assertEquals(2, httpService.calls.size)
        assertSame(firstNetwork, httpService.calls[0].network)
        assertSame(secondNetwork, httpService.calls[1].network)
        assertEquals(2, wifiService.disconnectCount)
        assertTrue(
            diagnosticEvents.contains(DeviceActionDiagnosticEvent.DnsResolutionFailed)
        )
    }

    @Test
    fun connectionFailureReportsDeviceAddressAsNotReachable() = runBlocking {
        val network = mock(Network::class.java)
        val service = createService(
            profiles = listOf(WifiProfile("wifi-1", "Device WiFi")),
            wifiService = FakeWifiConnectionService(
                results = ArrayDeque(listOf(WifiConnectionResult.Success(network)))
            ),
            httpService = FakeHttpApiCallService(
                ArrayDeque(listOf(HttpApiCallResult.NetworkError(ConnectException())))
            )
        )
        val diagnosticEvents = mutableListOf<DeviceActionDiagnosticEvent>()

        service.execute(
            device(
                connections = listOf(DeviceConnection("wifi-1", "192.168.1.20"))
            ),
            diagnosticEvents::add
        )

        assertTrue(
            diagnosticEvents.contains(DeviceActionDiagnosticEvent.DeviceNotReachable)
        )
    }

    @Test
    fun httpErrorAndTimeoutDoNotTryAnotherWifi() = runBlocking {
        val firstNetwork = mock(Network::class.java)
        val secondNetwork = mock(Network::class.java)

        listOf(
            HttpApiCallResult.HttpError(HttpApiResponse(404, emptyMap(), "")) to
                DeviceActionResult.HttpError(404),
            HttpApiCallResult.Timeout to DeviceActionResult.Timeout
        ).forEach { (apiResult, expectedResult) ->
            val wifiService = FakeWifiConnectionService(
                results = ArrayDeque(
                    listOf(
                        WifiConnectionResult.Success(firstNetwork),
                        WifiConnectionResult.Success(secondNetwork)
                    )
                )
            )
            val service = createService(
                profiles = listOf(
                    WifiProfile("wifi-1", "First WiFi"),
                    WifiProfile("wifi-2", "Second WiFi")
                ),
                wifiService = wifiService,
                httpService = FakeHttpApiCallService(ArrayDeque(listOf(apiResult)))
            )

            val result = service.execute(
                device(
                    connections = listOf(
                        DeviceConnection("wifi-1", "first.local"),
                        DeviceConnection("wifi-2", "second.local")
                    )
                )
            )

            assertEquals(expectedResult, result)
            assertEquals(listOf("First WiFi"), wifiService.requestedSsids)
            assertEquals(1, wifiService.disconnectCount)
        }
    }

    @Test
    fun vpnSocketBindingFailureIsReportedExplicitly() = runBlocking {
        val network = mock(Network::class.java)
        val service = createService(
            profiles = listOf(WifiProfile("wifi-1", "Device WiFi")),
            wifiService = FakeWifiConnectionService(
                results = ArrayDeque(listOf(WifiConnectionResult.Success(network)))
            ),
            httpService = FakeHttpApiCallService(
                ArrayDeque(
                    listOf(
                        HttpApiCallResult.NetworkError(
                            SocketException(
                                "Binding socket to network failed: EPERM (Operation not permitted)"
                            )
                        )
                    )
                )
            )
        )

        val result = service.execute(
            device(
                connections = listOf(DeviceConnection("wifi-1", "192.168.33.1"))
            )
        )

        assertEquals(
            DeviceActionResult.NetworkError(NetworkFailureReason.VPN_BLOCKED),
            result
        )
    }

    @Test
    fun cancellationDuringApiCallDisconnectsRequestedWifi() = runBlocking {
        val network = mock(Network::class.java)
        val wifiService = FakeWifiConnectionService(
            results = ArrayDeque(listOf(WifiConnectionResult.Success(network)))
        )
        val service = createService(
            profiles = listOf(WifiProfile("wifi-1", "Device WiFi")),
            wifiService = wifiService,
            httpService = SuspendingHttpApiCallService()
        )

        val job = launch {
            service.execute(
                device(
                    connections = listOf(DeviceConnection("wifi-1", "device.local"))
                )
            )
        }
        while (wifiService.requestedSsids.isEmpty()) {
            kotlinx.coroutines.yield()
        }

        job.cancelAndJoin()

        assertEquals(1, wifiService.disconnectCount)
    }

    private fun createService(
        profiles: List<WifiProfile>,
        wifiService: FakeWifiConnectionService,
        httpService: HttpApiCallService
    ): DeviceActionService {
        return createService(
            wifiProfileRepository = FakeWifiProfileRepository(profiles),
            wifiService = wifiService,
            httpService = httpService
        )
    }

    private fun createService(
        wifiProfileRepository: FakeWifiProfileRepository,
        wifiService: FakeWifiConnectionService,
        httpService: HttpApiCallService
    ): DeviceActionService {
        return DefaultDeviceActionService(
            wifiProfileRepository = wifiProfileRepository,
            wifiConnectionService = wifiService,
            httpApiCallService = httpService
        )
    }

    private fun device(
        method: ApiMethod = ApiMethod.GET,
        payload: String? = null,
        path: String = "/rpc/action",
        connections: List<DeviceConnection>
    ): Device {
        return Device(
            id = "device-1",
            name = "Device",
            actionLabel = "Switch",
            apiCall = ApiCall(
                method = method,
                path = path,
                optionalPayload = payload
            ),
            connections = connections,
            sortOrder = 0
        )
    }

    private fun successResult(): HttpApiCallResult {
        return HttpApiCallResult.Success(HttpApiResponse(200, emptyMap(), ""))
    }

    private class FakeWifiProfileRepository(
        private val profiles: List<WifiProfile>
    ) : WifiProfileRepository {
        val savedSecurityTypes = mutableMapOf<String, WifiSecurityType>()

        override fun observeWifiProfiles(): Flow<List<WifiProfile>> = flowOf(profiles)

        override suspend fun getWifiProfiles(): List<WifiProfile> = profiles

        override suspend fun saveWifiProfile(
            profile: WifiProfile,
            password: String?,
            shouldUpdatePassword: Boolean
        ) = Unit

        override suspend fun getPassword(id: String): String? = "password-$id"

        override suspend fun hasPassword(id: String): Boolean = true

        override suspend fun updateLastSuccessfulSecurityType(
            id: String,
            securityType: WifiSecurityType
        ) {
            savedSecurityTypes[id] = securityType
        }

        override suspend fun deletePassword(id: String) = Unit

        override suspend fun deleteWifiProfile(id: String) = Unit
    }

    private class FakeWifiConnectionService(
        private val results: ArrayDeque<WifiConnectionResult> = ArrayDeque()
    ) : WifiConnectionService {
        val requestedSsids = mutableListOf<String>()
        val requestedSecurityTypes = mutableListOf<WifiSecurityType>()
        val requestedTimeouts = mutableListOf<Long>()
        var disconnectCount = 0

        override suspend fun connect(
            ssid: String,
            password: String?,
            securityType: WifiSecurityType,
            timeoutMillis: Long
        ): WifiConnectionResult {
            requestedSsids += ssid
            requestedSecurityTypes += securityType
            requestedTimeouts += timeoutMillis
            return results.removeFirst()
        }

        override fun disconnect() {
            disconnectCount += 1
        }
    }

    private class FakeHttpApiCallService(
        private val results: ArrayDeque<HttpApiCallResult>
    ) : HttpApiCallService {
        val calls = mutableListOf<ApiCallRecord>()

        override suspend fun get(
            url: String,
            network: Network?,
            timeoutMillis: Long
        ): HttpApiCallResult {
            calls += ApiCallRecord("GET", url, null, network)
            return results.removeFirst()
        }

        override suspend fun post(
            url: String,
            body: String?,
            contentType: String,
            network: Network?,
            timeoutMillis: Long
        ): HttpApiCallResult {
            calls += ApiCallRecord("POST", url, body, network)
            return results.removeFirst()
        }
    }

    private class SuspendingHttpApiCallService : HttpApiCallService {
        override suspend fun get(
            url: String,
            network: Network?,
            timeoutMillis: Long
        ): HttpApiCallResult = awaitCancellation()

        override suspend fun post(
            url: String,
            body: String?,
            contentType: String,
            network: Network?,
            timeoutMillis: Long
        ): HttpApiCallResult = awaitCancellation()
    }

    private data class ApiCallRecord(
        val method: String,
        val url: String,
        val body: String?,
        val network: Network?
    )
}
