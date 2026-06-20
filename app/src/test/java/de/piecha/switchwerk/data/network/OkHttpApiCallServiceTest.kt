package de.piecha.switchwerk.data.network

import android.net.Network
import java.io.ByteArrayInputStream
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.UnknownHostException
import java.net.URL
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.runBlocking
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class OkHttpApiCallServiceTest {

    private lateinit var server: MockWebServer
    private lateinit var service: HttpApiCallService

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        service = OkHttpApiCallService(OkHttpClient())
    }

    @After
    fun tearDown() {
        server.close()
    }

    @Test
    fun getReturnsSuccessfulResponse() = runBlocking {
        server.enqueue(
            MockResponse.Builder()
                .code(200)
                .addHeader("X-Device", "Shelly")
                .body("""{"output":true}""")
                .build()
        )

        val result = service.get(server.url("/relay/0").toString())

        assertTrue(result is HttpApiCallResult.Success)
        val response = (result as HttpApiCallResult.Success).response
        assertEquals(200, response.statusCode)
        assertEquals("""{"output":true}""", response.body)
        assertEquals(listOf("Shelly"), response.headers["X-Device"])
        assertEquals("GET", server.takeRequest().method)
    }

    @Test
    fun postSendsPayloadAndReturnsSuccessfulResponse() = runBlocking {
        server.enqueue(
            MockResponse.Builder()
                .code(204)
                .build()
        )

        val result = service.post(
            url = server.url("/rpc/Switch.Set").toString(),
            body = """{"id":0,"on":true}"""
        )

        assertTrue(result is HttpApiCallResult.Success)
        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("application/json; charset=utf-8", request.headers["Content-Type"])
        assertEquals("""{"id":0,"on":true}""", request.body?.utf8())
    }

    @Test
    fun boundGetUsesAndroidNetworkOpenConnection() = runBlocking {
        val network = mock(Network::class.java)
        val connection = mock(HttpURLConnection::class.java)
        val url = "http://192.168.33.1/rpc/Switch.Set?id=0&on=true&toggle_after=1"
        `when`(network.openConnection(URL(url))).thenReturn(connection)
        `when`(connection.responseCode).thenReturn(200)
        `when`(connection.headerFields).thenReturn(emptyMap())
        `when`(connection.inputStream).thenReturn(
            ByteArrayInputStream("""{"was_on":false}""".toByteArray())
        )

        val result = service.get(
            url = url,
            network = network,
            timeoutMillis = 5_000
        )

        assertTrue(result is HttpApiCallResult.Success)
        assertEquals("""{"was_on":false}""", (result as HttpApiCallResult.Success).response.body)
        verify(network).openConnection(URL(url))
        verify(connection).requestMethod = "GET"
        verify(connection).connectTimeout = 5_000
        verify(connection).readTimeout = 5_000
        verify(connection).disconnect()
    }

    @Test
    fun dnsResolutionUsesRequestedAndroidNetwork() = runBlocking {
        val network = mock(Network::class.java)
        val address = InetAddress.getByAddress(byteArrayOf(192.toByte(), 168.toByte(), 1, 20))
        `when`(network.getAllByName("device.local")).thenReturn(arrayOf(address))

        val result = service.resolveHost("device.local", network)

        assertEquals(DnsResolutionResult.Success, result)
        verify(network).getAllByName("device.local")
        Unit
    }

    @Test
    fun dnsResolutionReturnsStructuredError() = runBlocking {
        val network = mock(Network::class.java)
        `when`(network.getAllByName("missing.local")).thenThrow(UnknownHostException())

        val result = service.resolveHost("missing.local", network)

        assertTrue(result is DnsResolutionResult.Error)
    }

    @Test
    fun nonSuccessfulStatusReturnsHttpErrorWithResponse() = runBlocking {
        server.enqueue(
            MockResponse.Builder()
                .code(503)
                .body("unavailable")
                .build()
        )

        val result = service.get(server.url("/status").toString())

        assertTrue(result is HttpApiCallResult.HttpError)
        val response = (result as HttpApiCallResult.HttpError).response
        assertEquals(503, response.statusCode)
        assertEquals("unavailable", response.body)
    }

    @Test
    fun redirectIsReturnedWithoutFollowingIt() = runBlocking {
        server.enqueue(
            MockResponse.Builder()
                .code(302)
                .addHeader("Location", server.url("/redirected"))
                .build()
        )

        val result = service.get(server.url("/original").toString())

        assertTrue(result is HttpApiCallResult.HttpError)
        assertEquals(302, (result as HttpApiCallResult.HttpError).response.statusCode)
        assertEquals("/original", server.takeRequest().url.encodedPath)
        assertEquals(null, server.takeRequest(100, TimeUnit.MILLISECONDS))
    }

    @Test
    fun timeoutReturnsStructuredTimeout() = runBlocking {
        server.enqueue(
            MockResponse.Builder()
                .code(200)
                .body("late")
                .bodyDelay(250, TimeUnit.MILLISECONDS)
                .build()
        )

        val result = service.get(
            url = server.url("/slow").toString(),
            timeoutMillis = 50
        )

        assertEquals(HttpApiCallResult.Timeout, result)
    }

    @Test
    fun connectionFailureReturnsNetworkError() = runBlocking {
        val url = server.url("/offline").toString()
        server.close()

        val result = service.get(url)

        assertTrue(result is HttpApiCallResult.NetworkError)
    }

    @Test
    fun invalidUrlReturnsInvalidRequest() = runBlocking {
        val result = service.get("not a url")

        assertTrue(result is HttpApiCallResult.InvalidRequest)
    }

    @Test
    fun invalidTimeoutReturnsInvalidRequest() = runBlocking {
        val result = service.get(
            url = server.url("/status").toString(),
            timeoutMillis = 0
        )

        assertTrue(result is HttpApiCallResult.InvalidRequest)
    }
}
