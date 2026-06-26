package de.piecha.switchwerk.domain.validation

import de.piecha.switchwerk.domain.model.ApiCall
import de.piecha.switchwerk.domain.model.ApiContentType
import de.piecha.switchwerk.domain.model.ApiMethod
import de.piecha.switchwerk.domain.model.DeviceConnection

object TechnicalFieldValidator {

    fun validateHost(value: String): HostValidationResult {
        val input = value.trim()
        if (input.isBlank()) {
            return HostValidationResult.Empty
        }
        if (
            input.any { it.isWhitespace() || it.isControlCharacter() } ||
            input.contains("://") ||
            input.contains('/') ||
            input.contains('\\') ||
            input.contains('@') ||
            input.contains('?') ||
            input.contains('#')
        ) {
            return HostValidationResult.Invalid
        }

        val (host, port) = splitHostAndPort(input) ?: return HostValidationResult.Invalid
        if (port != null && port !in 1..65535) {
            return HostValidationResult.Invalid
        }

        return if (isValidIpv4Address(host) || isValidDnsName(host)) {
            HostValidationResult.Valid
        } else {
            HostValidationResult.Invalid
        }
    }

    fun validateApiPath(value: String): ApiPathValidationResult {
        val input = value.trim()
        if (input.isBlank()) {
            return ApiPathValidationResult.Empty
        }
        if (
            input.any { it.isWhitespace() || it.isControlCharacter() } ||
            input.contains("://") ||
            input.startsWith("//") ||
            input.contains('\\') ||
            input.contains('#')
        ) {
            return ApiPathValidationResult.Invalid
        }

        val path = input.substringBefore('?')
        if (path.isBlank()) {
            return ApiPathValidationResult.Invalid
        }

        val normalizedPath = path.trimStart('/')
        val segments = normalizedPath.split('/').filter(String::isNotEmpty)
        if (segments.any { segment -> segment == "." || segment == ".." || isEncodedDotSegment(segment) }) {
            return ApiPathValidationResult.Invalid
        }

        return ApiPathValidationResult.Valid
    }

    fun validateApiMethod(value: String): ApiEnumValidationResult {
        return if (ApiMethod.entries.any { method -> method.name == value }) {
            ApiEnumValidationResult.Valid
        } else {
            ApiEnumValidationResult.Invalid
        }
    }

    fun validateContentType(value: String): ApiEnumValidationResult {
        return if (ApiContentType.entries.any { contentType -> contentType.name == value }) {
            ApiEnumValidationResult.Valid
        } else {
            ApiEnumValidationResult.Invalid
        }
    }

    fun validateApiCall(apiCall: ApiCall): ApiCallValidationResult {
        val pathResult = validateApiPath(apiCall.path)
        if (pathResult != ApiPathValidationResult.Valid) {
            return ApiCallValidationResult.InvalidPath
        }
        if (validateApiMethod(apiCall.method.name) != ApiEnumValidationResult.Valid) {
            return ApiCallValidationResult.InvalidMethod
        }
        if (validateContentType(apiCall.contentType.name) != ApiEnumValidationResult.Valid) {
            return ApiCallValidationResult.InvalidContentType
        }
        return ApiCallValidationResult.Valid
    }

    fun validateDeviceConnection(connection: DeviceConnection): HostValidationResult {
        return validateHost(connection.host)
    }

    private fun splitHostAndPort(input: String): Pair<String, Int?>? {
        if (input.count { it == ':' } > 1) {
            return null
        }
        val parts = input.split(':')
        if (parts.size == 1) {
            return parts[0] to null
        }
        val port = parts[1].toIntOrNull() ?: return null
        return parts[0] to port
    }

    private fun isValidIpv4Address(value: String): Boolean {
        val parts = value.split('.')
        return parts.size == 4 && parts.all { part ->
            part.isNotEmpty() &&
                part.length <= 3 &&
                part.all(Char::isDigit) &&
                part.toIntOrNull() in 0..255
        }
    }

    private fun isValidDnsName(value: String): Boolean {
        if (value.length !in 1..253 || value.endsWith(".")) {
            return false
        }
        val labels = value.split('.')
        if (labels.all { label -> label.all(Char::isDigit) }) {
            return false
        }
        return labels.all { label ->
            label.length in 1..63 &&
                !label.startsWith("-") &&
                !label.endsWith("-") &&
                label.all { it.isLetterOrDigit() || it == '-' }
        }
    }

    private fun isEncodedDotSegment(segment: String): Boolean {
        val lowerSegment = segment.lowercase()
        return lowerSegment == "%2e" || lowerSegment == "%2e%2e" || lowerSegment == ".%2e" ||
            lowerSegment == "%2e."
    }

    private fun Char.isControlCharacter(): Boolean {
        return code in 0..31 || code == 127
    }
}

enum class HostValidationResult {
    Valid,
    Empty,
    Invalid
}

enum class ApiPathValidationResult {
    Valid,
    Empty,
    Invalid
}

enum class ApiEnumValidationResult {
    Valid,
    Invalid
}

enum class ApiCallValidationResult {
    Valid,
    InvalidPath,
    InvalidMethod,
    InvalidContentType
}
