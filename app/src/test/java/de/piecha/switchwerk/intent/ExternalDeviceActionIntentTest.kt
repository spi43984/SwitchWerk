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

    @Test
    fun validGroupIdIsAccepted() {
        assertEquals(
            ExternalSwitchGroupActionIntentResult.Valid("group-123"),
            parseExternalSwitchGroupActionIntent("group-123", setOf(EXTRA_GROUP_ID))
        )
    }

    @Test
    fun missingGroupIdIsRejected() {
        assertEquals(
            ExternalSwitchGroupActionIntentResult.MissingGroupId,
            parseExternalSwitchGroupActionIntent(null, emptySet())
        )
    }

    @Test
    fun invalidGroupIdIsRejected() {
        assertEquals(
            ExternalSwitchGroupActionIntentResult.InvalidGroupId,
            parseExternalSwitchGroupActionIntent("https://server.domain.com/group", setOf(EXTRA_GROUP_ID))
        )
    }

    @Test
    fun unexpectedGroupExtraIsRejected() {
        assertEquals(
            ExternalSwitchGroupActionIntentResult.UnexpectedExtras,
            parseExternalSwitchGroupActionIntent("group-123", setOf(EXTRA_GROUP_ID, "command"))
        )
    }
}
