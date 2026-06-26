package de.piecha.switchwerk.domain.validation

import de.piecha.switchwerk.domain.model.ApiCall
import de.piecha.switchwerk.domain.model.ApiContentType
import de.piecha.switchwerk.domain.model.ApiMethod
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TechnicalFieldValidatorTest {

    @Test
    fun validHostsAreAccepted() {
        listOf(
            "192.0.2.10",
            "device.local",
            "server.domain.com",
            "device.local:8080"
        ).forEach { host ->
            assertEquals(HostValidationResult.Valid, TechnicalFieldValidator.validateHost(host))
        }
    }

    @Test
    fun invalidHostsAreRejected() {
        listOf(
            "",
            "999.999.999.999",
            "bad host name",
            "http://device.local",
            "device.local/path",
            "user@device.local",
            "device.local:70000",
            "-device.local"
        ).forEach { host ->
            val result = TechnicalFieldValidator.validateHost(host)
            assertTrue(result == HostValidationResult.Empty || result == HostValidationResult.Invalid)
        }
    }

    @Test
    fun validApiPathsAreAccepted() {
        listOf(
            "/rpc/Switch.Toggle?id=0",
            "rpc/Switch.Set?id=0&on=true",
            "/relay/0?turn=on"
        ).forEach { path ->
            assertEquals(ApiPathValidationResult.Valid, TechnicalFieldValidator.validateApiPath(path))
        }
    }

    @Test
    fun invalidApiPathsAreRejected() {
        listOf(
            "",
            "https://server.domain.com/rpc/action",
            "//server.domain.com/rpc/action",
            "/../admin",
            "/%2e%2e/admin",
            "/rpc/action#fragment",
            "/bad path"
        ).forEach { path ->
            val result = TechnicalFieldValidator.validateApiPath(path)
            assertTrue(result == ApiPathValidationResult.Empty || result == ApiPathValidationResult.Invalid)
        }
    }

    @Test
    fun apiCallValidationAcceptsConfiguredEnums() {
        assertEquals(
            ApiCallValidationResult.Valid,
            TechnicalFieldValidator.validateApiCall(
                ApiCall(
                    method = ApiMethod.POST,
                    path = "/rpc/Switch.Toggle?id=0",
                    requestBody = """{"on":true}""",
                    contentType = ApiContentType.APPLICATION_JSON
                )
            )
        )
    }
}
