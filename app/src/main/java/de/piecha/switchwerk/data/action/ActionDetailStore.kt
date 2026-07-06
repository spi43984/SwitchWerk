package de.piecha.switchwerk.data.action

import de.piecha.switchwerk.R
import de.piecha.switchwerk.domain.model.ApiMethod
import de.piecha.switchwerk.ui.UiText
import de.piecha.switchwerk.ui.uiText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface DiagnosticListItem {
    data class Message(val text: UiText) : DiagnosticListItem

    data object Separator : DiagnosticListItem
}

enum class ActionOrigin(val label: UiText) {
    DASHBOARD(uiText(R.string.diagnostic_origin_dashboard)),
    WIDGET(uiText(R.string.diagnostic_origin_widget)),
    APP_SHORTCUT(uiText(R.string.diagnostic_origin_app_shortcut)),
    EXTERNAL_INTENT(uiText(R.string.diagnostic_origin_external_intent))
}

interface ActionDetailSession {
    fun append(message: UiText)
}

interface ActionDetailStore {
    val items: StateFlow<List<DiagnosticListItem>>

    fun start(origin: ActionOrigin): ActionDetailSession

    fun clear()
}

class InMemoryActionDetailStore : ActionDetailStore {
    private val lock = Any()
    private val blocks = mutableListOf<ActionBlock>()
    private val _items = MutableStateFlow<List<DiagnosticListItem>>(emptyList())
    override val items: StateFlow<List<DiagnosticListItem>> = _items.asStateFlow()

    override fun start(origin: ActionOrigin): ActionDetailSession {
        val id = UUID.randomUUID().toString()
        val session = DefaultActionDetailSession(id, origin)
        synchronized(lock) {
            blocks += ActionBlock(id, mutableListOf())
            appendLocked(id, origin.label, elapsedMillis = 0L)
        }
        return session
    }

    override fun clear() {
        synchronized(lock) {
            blocks.clear()
            _items.value = emptyList()
        }
    }

    private fun appendLocked(id: String, message: UiText, elapsedMillis: Long) {
        val block = blocks.firstOrNull { it.id == id } ?: return
        val timestamp = SimpleDateFormat(TIMESTAMP_PATTERN, Locale.getDefault()).format(Date())
        block.messages += DiagnosticListItem.Message(
            uiText(R.string.diagnostic_entry, timestamp, elapsedMillis, message)
        )
        trimLocked()
        _items.value = blocks.flatMapIndexed { index, actionBlock ->
            if (index == 0) actionBlock.messages else listOf(DiagnosticListItem.Separator) + actionBlock.messages
        }
    }

    private fun trimLocked() {
        while (blocks.sumOf { it.messages.size } > MAX_MESSAGES) {
            blocks.first().messages.removeAt(0)
            if (blocks.first().messages.isEmpty()) blocks.removeAt(0)
        }
    }

    private inner class DefaultActionDetailSession(
        private val id: String,
        private val origin: ActionOrigin
    ) : ActionDetailSession {
        private var previousEventNanos: Long? = null

        override fun append(message: UiText) {
            synchronized(lock) {
                if (blocks.none { it.id == id }) {
                    blocks += ActionBlock(id, mutableListOf())
                    appendLocked(id, origin.label, elapsedMillis = 0L)
                }
                val now = System.nanoTime()
                val elapsedMillis = previousEventNanos?.let {
                    ((now - it) / NANOS_PER_MILLISECOND).coerceAtLeast(0L)
                } ?: 0L
                previousEventNanos = now
                appendLocked(id, message, elapsedMillis)
            }
        }
    }

    private data class ActionBlock(
        val id: String,
        val messages: MutableList<DiagnosticListItem.Message>
    )

    private companion object {
        const val TIMESTAMP_PATTERN = "HH:mm:ss.SSS"
        const val MAX_MESSAGES = 200
        const val NANOS_PER_MILLISECOND = 1_000_000L
    }
}

fun DeviceActionDiagnosticEvent.toActionDetailMessage(deviceName: String): UiText = when (this) {
    DeviceActionDiagnosticEvent.ActionStarted -> uiText(R.string.diagnostic_action_started, deviceName)
    is DeviceActionDiagnosticEvent.WifiProfileAttempt ->
        uiText(R.string.diagnostic_wifi_profile_attempt, index, total, profileName)
    is DeviceActionDiagnosticEvent.WifiRequestStarted ->
        uiText(R.string.diagnostic_wifi_request_started, profileName)
    DeviceActionDiagnosticEvent.WifiSecurityDetectionStarted -> uiText(R.string.diagnostic_wifi_security_started)
    DeviceActionDiagnosticEvent.WifiSecurityDetectionSucceeded -> uiText(R.string.diagnostic_wifi_security_succeeded)
    DeviceActionDiagnosticEvent.WifiSecurityDetectionUnavailable -> uiText(R.string.diagnostic_wifi_security_unavailable)
    DeviceActionDiagnosticEvent.WifiFound -> uiText(R.string.diagnostic_wifi_found)
    DeviceActionDiagnosticEvent.WifiConnected -> uiText(R.string.diagnostic_wifi_connected)
    DeviceActionDiagnosticEvent.IpAddressReceived -> uiText(R.string.diagnostic_ip_received)
    DeviceActionDiagnosticEvent.WifiConnectionFailed -> uiText(R.string.diagnostic_wifi_failed)
    DeviceActionDiagnosticEvent.WifiDisabled -> uiText(R.string.diagnostic_wifi_disabled)
    DeviceActionDiagnosticEvent.MissingWifiPassword -> uiText(R.string.diagnostic_wifi_missing_password)
    is DeviceActionDiagnosticEvent.DeviceAddress -> uiText(R.string.diagnostic_device_address)
    is DeviceActionDiagnosticEvent.HttpRequestStarted -> when (method) {
        ApiMethod.GET -> uiText(R.string.diagnostic_http_get_started)
        ApiMethod.POST -> uiText(R.string.diagnostic_http_post_started)
    }
    is DeviceActionDiagnosticEvent.HttpResponseReceived -> uiText(R.string.diagnostic_http_response, statusCode)
    is DeviceActionDiagnosticEvent.HttpRequestSucceeded -> uiText(R.string.diagnostic_http_success, statusCode)
    is DeviceActionDiagnosticEvent.DnsResolutionStarted -> uiText(R.string.diagnostic_dns_started)
    DeviceActionDiagnosticEvent.DnsResolutionSucceeded -> uiText(R.string.diagnostic_dns_succeeded)
    DeviceActionDiagnosticEvent.DnsResolutionFailed -> uiText(R.string.diagnostic_dns_failed)
    DeviceActionDiagnosticEvent.DeviceNotReachable -> uiText(R.string.diagnostic_device_unreachable)
    DeviceActionDiagnosticEvent.RequestSucceeded -> uiText(R.string.diagnostic_request_succeeded)
    DeviceActionDiagnosticEvent.RequestFailed -> uiText(R.string.diagnostic_request_failed)
    DeviceActionDiagnosticEvent.ActionCancelled -> uiText(R.string.diagnostic_action_cancelled)
    is DeviceActionDiagnosticEvent.Timeout -> when (stage) {
        DiagnosticStage.WIFI_REQUEST -> uiText(R.string.diagnostic_timeout_wifi_request)
        DiagnosticStage.WIFI -> uiText(R.string.diagnostic_timeout_wifi)
        DiagnosticStage.DNS -> uiText(R.string.diagnostic_timeout_dns)
        DiagnosticStage.HTTP -> uiText(R.string.diagnostic_timeout_http)
    }
    DeviceActionDiagnosticEvent.ActionCompleted -> uiText(R.string.diagnostic_action_completed)
}

fun SwitchGroupDiagnosticEvent.toActionDetailMessage(groupName: String): UiText = when (this) {
    SwitchGroupDiagnosticEvent.GroupStarted -> uiText(R.string.diagnostic_group_started, groupName)
    is SwitchGroupDiagnosticEvent.MemberStarted ->
        uiText(R.string.diagnostic_group_member_started, step, total, deviceName)
    is SwitchGroupDiagnosticEvent.MemberSucceeded ->
        uiText(R.string.diagnostic_group_member_succeeded, step, total, deviceName)
    is SwitchGroupDiagnosticEvent.MemberFailed ->
        uiText(R.string.diagnostic_group_member_failed, step, total, deviceName)
    is SwitchGroupDiagnosticEvent.PauseStarted ->
        uiText(R.string.diagnostic_group_pause, pauseMillis.toPauseDurationLabel())
    SwitchGroupDiagnosticEvent.GroupCompleted -> uiText(R.string.diagnostic_group_completed)
    SwitchGroupDiagnosticEvent.GroupCancelled -> uiText(R.string.diagnostic_action_cancelled)
    is SwitchGroupDiagnosticEvent.DeviceEvent -> event.toActionDetailMessage(deviceName)
}

private fun Long.toPauseDurationLabel(): String {
    val hours = this / 3_600_000
    val minutes = (this % 3_600_000) / 60_000
    val seconds = (this % 60_000) / 1_000
    val milliseconds = this % 1_000
    return String.format(Locale.ROOT, "%02d:%02d:%02d.%03d", hours, minutes, seconds, milliseconds)
}
