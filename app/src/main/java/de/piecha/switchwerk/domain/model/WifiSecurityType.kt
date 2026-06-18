package de.piecha.switchwerk.domain.model

enum class WifiSecurityType(val storageValue: String) {
    WPA2("WPA2_PSK"),
    WPA3("WPA3_SAE");

    fun fallback(): WifiSecurityType {
        return when (this) {
            WPA2 -> WPA3
            WPA3 -> WPA2
        }
    }

    companion object {
        fun fromStorageValue(value: String?): WifiSecurityType? {
            return entries.firstOrNull { it.storageValue == value }
        }
    }
}
