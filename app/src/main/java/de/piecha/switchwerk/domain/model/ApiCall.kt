package de.piecha.switchwerk.domain.model

data class ApiCall(
    val method: ApiMethod,
    val path: String,
    val optionalPayload: String? = null
)
