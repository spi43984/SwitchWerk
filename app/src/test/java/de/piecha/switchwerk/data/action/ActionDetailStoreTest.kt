package de.piecha.switchwerk.data.action

import de.piecha.switchwerk.R
import de.piecha.switchwerk.ui.UiText
import de.piecha.switchwerk.ui.uiText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ActionDetailStoreTest {

    @Test
    fun simultaneousSessionsRemainSeparateBlocks() {
        val store = InMemoryActionDetailStore()
        val dashboard = store.start(ActionOrigin.DASHBOARD)
        val widget = store.start(ActionOrigin.WIDGET)

        dashboard.append(uiText(R.string.diagnostic_action_started, "Device A"))
        widget.append(uiText(R.string.diagnostic_action_started, "Device B"))
        dashboard.append(uiText(R.string.diagnostic_action_completed))

        val items = store.items.value
        assertEquals(1, items.count { it == DiagnosticListItem.Separator })
        val messages = items.filterIsInstance<DiagnosticListItem.Message>()
        assertEquals(5, messages.size)
        assertEquals(
            R.string.diagnostic_origin_dashboard,
            messages[0].nestedMessage().resourceId
        )
        assertEquals(
            R.string.diagnostic_action_completed,
            messages[2].nestedMessage().resourceId
        )
        assertEquals(
            R.string.diagnostic_origin_widget,
            messages[3].nestedMessage().resourceId
        )
    }

    @Test
    fun clearRemovesAllBlocks() {
        val store = InMemoryActionDetailStore()
        store.start(ActionOrigin.APP_SHORTCUT)

        store.clear()

        assertTrue(store.items.value.isEmpty())
    }

    @Test
    fun addressEventsDoNotExposeTheirAddress() {
        val address = "192.0.2.10"

        val message = DeviceActionDiagnosticEvent.DeviceAddress(address)
            .toActionDetailMessage("Device") as UiText.Resource

        assertEquals(R.string.diagnostic_device_address, message.resourceId)
        assertFalse(message.arguments.contains(address))
    }

    private fun DiagnosticListItem.Message.nestedMessage(): UiText.Resource {
        val entry = text as UiText.Resource
        return entry.arguments[2] as UiText.Resource
    }
}
