package de.piecha.switchwerk.data.network

import android.net.Network
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.InterruptedIOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.resume
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response

class OkHttpApiCallService(
    private val baseClient: OkHttpClient
) : HttpApiCallService {

    override suspend fun resolveHost(
        host: String,
        network: Network,
        timeoutMillis: Long
    ): DnsResolutionResult {
        if (host.isBlank() || timeoutMillis <= 0) {
            return DnsResolutionResult.Error(
                IllegalArgumentException("Host and timeout must be valid")
            )
        }

        return try {
            withTimeoutOrNull(timeoutMillis) {
                suspendCancellableCoroutine { continuation ->
                    val resolverJob = CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                        val result = try {
                            runInterruptible {
                                network.getAllByName(host)
                            }
                            DnsResolutionResult.Success
                        } catch (exception: CancellationException) {
                            throw exception
                        } catch (exception: Exception) {
                            DnsResolutionResult.Error(exception)
                        }

                        if (continuation.isActive) {
                            continuation.resume(result)
                        }
                    }
                    continuation.invokeOnCancellation {
                        resolverJob.cancel()
                    }
                }
            } ?: DnsResolutionResult.Timeout
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            DnsResolutionResult.Error(exception)
        }
    }

    override suspend fun get(
        url: String,
        network: Network?,
        timeoutMillis: Long
    ): HttpApiCallResult {
        return if (network == null) {
            execute(
                requestBuilder = { Request.Builder().url(url).get().build() },
                timeoutMillis = timeoutMillis
            )
        } else {
            executeOnNetwork(
                url = url,
                method = "GET",
                body = null,
                contentType = null,
                network = network,
                timeoutMillis = timeoutMillis
            )
        }
    }

    override suspend fun post(
        url: String,
        body: String?,
        contentType: String,
        network: Network?,
        timeoutMillis: Long
    ): HttpApiCallResult {
        return if (network == null) {
            execute(
                requestBuilder = {
                    Request.Builder()
                        .url(url)
                        .post((body ?: "").toRequestBody(contentType.toMediaType()))
                        .build()
                },
                timeoutMillis = timeoutMillis
            )
        } else {
            executeOnNetwork(
                url = url,
                method = "POST",
                body = body ?: "",
                contentType = contentType,
                network = network,
                timeoutMillis = timeoutMillis
            )
        }
    }

    private suspend fun execute(
        requestBuilder: () -> Request,
        timeoutMillis: Long
    ): HttpApiCallResult {
        if (timeoutMillis <= 0) {
            return HttpApiCallResult.InvalidRequest(
                IllegalArgumentException("Timeout must be greater than zero")
            )
        }

        val request = try {
            requestBuilder()
        } catch (exception: IllegalArgumentException) {
            return HttpApiCallResult.InvalidRequest(exception)
        }

        val call = try {
            clientFor(timeoutMillis).newCall(request)
        } catch (exception: IllegalArgumentException) {
            return HttpApiCallResult.InvalidRequest(exception)
        }
        return executeCall(call)
    }

    private fun clientFor(timeoutMillis: Long): OkHttpClient {
        return baseClient.newBuilder()
            .callTimeout(timeoutMillis, TimeUnit.MILLISECONDS)
            .retryOnConnectionFailure(false)
            .followRedirects(false)
            .followSslRedirects(false)
            .build()
    }

    private suspend fun executeOnNetwork(
        url: String,
        method: String,
        body: String?,
        contentType: String?,
        network: Network,
        timeoutMillis: Long
    ): HttpApiCallResult {
        if (timeoutMillis <= 0) {
            return HttpApiCallResult.InvalidRequest(
                IllegalArgumentException("Timeout must be greater than zero")
            )
        }

        val parsedUrl = try {
            URL(url)
        } catch (exception: IllegalArgumentException) {
            return HttpApiCallResult.InvalidRequest(exception)
        }

        return suspendCancellableCoroutine { continuation ->
            val connectionReference = AtomicReference<HttpURLConnection?>()

            continuation.invokeOnCancellation {
                connectionReference.get()?.disconnect()
            }

            CoroutineScope(continuation.context).launch(Dispatchers.IO) {
                val result = try {
                    val connection = network.openConnection(parsedUrl) as? HttpURLConnection
                        ?: return@launch continuation.resume(
                            HttpApiCallResult.InvalidRequest(
                                IllegalArgumentException("URL is not an HTTP endpoint")
                            )
                        )
                    connectionReference.set(connection)
                    connection.toCallResult(
                        method = method,
                        body = body,
                        contentType = contentType,
                        timeoutMillis = timeoutMillis
                    )
                } catch (exception: CancellationException) {
                    throw exception
                } catch (exception: InterruptedIOException) {
                    HttpApiCallResult.Timeout
                } catch (exception: IOException) {
                    HttpApiCallResult.NetworkError(exception)
                } catch (exception: IllegalArgumentException) {
                    HttpApiCallResult.InvalidRequest(exception)
                } finally {
                    connectionReference.getAndSet(null)?.disconnect()
                }

                if (continuation.isActive) {
                    continuation.resume(result)
                }
            }
        }
    }

    private fun HttpURLConnection.toCallResult(
        method: String,
        body: String?,
        contentType: String?,
        timeoutMillis: Long
    ): HttpApiCallResult {
        val timeout = timeoutMillis.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
        instanceFollowRedirects = false
        requestMethod = method
        connectTimeout = timeout
        readTimeout = timeout

        if (method == "POST") {
            doOutput = true
            setRequestProperty("Content-Type", contentType)
            outputStream.bufferedWriter(Charsets.UTF_8).use { writer ->
                writer.write(body.orEmpty())
            }
        }

        val statusCode = responseCode
        val response = HttpApiResponse(
            statusCode = statusCode,
            headers = headerFields
                .filterKeys { it != null }
                .mapKeys { it.key.orEmpty() },
            body = responseStream(statusCode).readText()
        )
        return if (statusCode in 200..299) {
            HttpApiCallResult.Success(response)
        } else {
            HttpApiCallResult.HttpError(response)
        }
    }

    private fun HttpURLConnection.responseStream(statusCode: Int): InputStream? {
        return if (statusCode in 200..399) {
            inputStream
        } else {
            errorStream
        }
    }

    private fun InputStream?.readText(): String {
        if (this == null) {
            return ""
        }
        return BufferedReader(InputStreamReader(this, Charsets.UTF_8)).use { it.readText() }
    }

    private suspend fun executeCall(call: Call): HttpApiCallResult =
        suspendCancellableCoroutine { continuation ->
            continuation.invokeOnCancellation {
                call.cancel()
            }

            call.enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    if (!continuation.isActive) {
                        return
                    }

                    continuation.resume(e.toCallResult())
                }

                override fun onResponse(call: Call, response: Response) {
                    if (!continuation.isActive) {
                        response.close()
                        return
                    }

                    val result = try {
                        response.use {
                            val apiResponse = HttpApiResponse(
                                statusCode = it.code,
                                headers = it.headers.toMultimap(),
                                body = it.body.string()
                            )
                            if (it.isSuccessful) {
                                HttpApiCallResult.Success(apiResponse)
                            } else {
                                HttpApiCallResult.HttpError(apiResponse)
                            }
                        }
                    } catch (exception: IOException) {
                        exception.toCallResult()
                    }
                    continuation.resume(result)
                }
            })
        }

    private fun IOException.toCallResult(): HttpApiCallResult =
        if (this is InterruptedIOException) {
            HttpApiCallResult.Timeout
        } else {
            HttpApiCallResult.NetworkError(this)
        }
}
