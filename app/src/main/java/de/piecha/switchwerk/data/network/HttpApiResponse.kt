package de.piecha.switchwerk.data.network

data class HttpApiResponse(
    val statusCode: Int,
    val headers: Map<String, List<String>>,
    val body: String
)
