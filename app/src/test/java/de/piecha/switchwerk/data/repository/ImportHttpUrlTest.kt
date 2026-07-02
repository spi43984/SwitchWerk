package de.piecha.switchwerk.data.repository

import org.junit.Assert.assertEquals
import org.junit.Test

class ImportHttpUrlTest {

    @Test
    fun httpUrlWithPortIsAccepted() {
        val url = importHttpUrl(" http://192.0.2.10:8080/TeamWerk.json ")

        assertEquals("http://192.0.2.10:8080/TeamWerk.json", url.toString())
    }

    @Test
    fun httpsUrlIsAccepted() {
        val url = importHttpUrl("https://server.domain.com/TeamWerk.json")

        assertEquals("https://server.domain.com/TeamWerk.json", url.toString())
    }

    @Test
    fun unsupportedSchemeIsRejected() {
        val error = runCatching {
            importHttpUrl("ftp://server.domain.com/TeamWerk.json")
        }.exceptionOrNull()

        assertEquals(
            "Für den URL-Import ist eine gültige HTTP/HTTPS-URL erforderlich",
            error?.message
        )
    }
}
