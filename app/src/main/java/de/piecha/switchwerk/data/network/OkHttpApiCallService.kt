package de.piecha.switchwerk.data.network

import android.net.Network
import java.io.IOException
import java.io.InterruptedIOException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
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

    override suspend fun get(
        url: String,
        network: Network?,
        timeoutMillis: Long
    ): HttpApiCallResult = execute(
        requestBuilder = { Request.Builder().url(url).get().build() },
        network = network,
        timeoutMillis = timeoutMillis
    )

    override suspend fun post(
        url: String,
        body: String?,
        contentType: String,
        network: Network?,
        timeoutMillis: Long
    ): HttpApiCallResult = execute(
        requestBuilder = {
            Request.Builder()
                .url(url)
                .post((body ?: "").toRequestBody(contentType.toMediaType()))
                .build()
        },
        network = network,
        timeoutMillis = timeoutMillis
    )

    private suspend fun execute(
        requestBuilder: () -> Request,
        network: Network?,
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
            clientFor(network, timeoutMillis).newCall(request)
        } catch (exception: IllegalArgumentException) {
            return HttpApiCallResult.InvalidRequest(exception)
        }
        return executeCall(call)
    }

    private fun clientFor(network: Network?, timeoutMillis: Long): OkHttpClient {
        val builder = baseClient.newBuilder()
            .callTimeout(timeoutMillis, TimeUnit.MILLISECONDS)
            .retryOnConnectionFailure(false)
            .followRedirects(false)
            .followSslRedirects(false)

        if (network != null) {
            builder
                .socketFactory(network.socketFactory)
                .dns { hostname -> network.getAllByName(hostname).toList() }
        }

        return builder.build()
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
