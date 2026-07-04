package de.piecha.switchwerk.intent

import org.junit.Assert.assertEquals
import org.junit.Test

class ExternalDeviceActionIntentTest {
    @Test
    fun validDeviceIdIsAccepted() {
        assertEquals(
            ExternalDeviceActionIntentResult.Valid("device-123"),
            parseExternalDeviceActionIntent("device-123", setOf(EXTRA_DEVICE_ID))
        )
    }

    @Test
    fun missingDeviceIdIsRejected() {
        assertEquals(
            ExternalDeviceActionIntentResult.MissingDeviceId,
            parseExternalDeviceActionIntent(null, emptySet())
        )
    }

    @Test
    fun invalidDeviceIdIsRejected() {
        assertEquals(
            ExternalDeviceActionIntentResult.InvalidDeviceId,
            parseExternalDeviceActionIntent("https://server.domain.com/action", setOf(EXTRA_DEVICE_ID))
        )
    }

    @Test
    fun unexpectedExtraIsRejected() {
        assertEquals(
            ExternalDeviceActionIntentResult.UnexpectedExtras,
            parseExternalDeviceActionIntent("device-123", setOf(EXTRA_DEVICE_ID, "command"))
        )
    }
}
