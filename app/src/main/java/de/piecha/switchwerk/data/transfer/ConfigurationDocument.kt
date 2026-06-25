package de.piecha.switchwerk.data.transfer

data class ConfigurationDocument(
    val schemaVersion: Int,
    val wifiProfiles: List<ConfigurationWifiProfile>,
    val devices: List<ConfigurationDevice>,
    val appSettings: ConfigurationAppSettings? = null
)

data class ConfigurationAppSettings(
    val themeMode: String,
    val showActionDetails: Boolean,
    val detailPanelHeight: String,
    val diagnosticsNewestFirst: Boolean,
    val dashboardLayoutMode: String? = null
)

data class ConfigurationWifiProfile(
    val id: String,
    val name: String,
    val ssid: String,
    val connectionMode: String = "SWITCHWERK_MANAGED",
    val securityType: String?,
    val password: String? = null,
    val isPasswordPresent: Boolean = false
)

data class ConfigurationDevice(
    val id: String,
    val name: String,
    val actionLabel: String,
    val action: ConfigurationDeviceAction,
    val connections: List<ConfigurationDeviceConnection>
)

data class ConfigurationDeviceAction(
    val protocol: String = "HTTP",
    val method: String,
    val path: String
)

data class ConfigurationDeviceConnection(
    val wifiProfileId: String,
    val host: String
)

const val CONFIGURATION_SCHEMA_VERSION = 3
