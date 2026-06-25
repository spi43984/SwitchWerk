package de.piecha.switchwerk.domain.model

data class ApiCall(
    val method: ApiMethod,
    val path: String,
    val requestBody: String = "",
    val contentType: ApiContentType = ApiContentType.APPLICATION_JSON
)
