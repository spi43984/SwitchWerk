package de.piecha.switchwerk.data.network

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class WifiProximityConfirmation { AVAILABLE, UNAVAILABLE }

class WifiProximityConfirmationStore {
    private val _confirmations = MutableStateFlow<Map<String, WifiProximityConfirmation>>(emptyMap())
    val confirmations: StateFlow<Map<String, WifiProximityConfirmation>> = _confirmations.asStateFlow()

    fun markAvailable(ssid: String) = update(ssid, WifiProximityConfirmation.AVAILABLE)

    fun markUnavailable(ssid: String) = update(ssid, WifiProximityConfirmation.UNAVAILABLE)

    private fun update(ssid: String, confirmation: WifiProximityConfirmation) {
        if (ssid.isNotBlank()) {
            _confirmations.value = _confirmations.value + (ssid to confirmation)
        }
    }
}
