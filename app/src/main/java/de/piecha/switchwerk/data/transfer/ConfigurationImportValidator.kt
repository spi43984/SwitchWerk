package de.piecha.switchwerk.data.transfer

import de.piecha.switchwerk.domain.model.ApiMethod
import de.piecha.switchwerk.domain.model.AppThemeMode
import de.piecha.switchwerk.domain.model.DashboardLayoutMode
import de.piecha.switchwerk.domain.model.DeviceProtocol
import de.piecha.switchwerk.domain.model.DetailPanelHeight
import de.piecha.switchwerk.domain.model.WifiConnectionMode

class ConfigurationImportValidator {

    fun validate(document: ConfigurationDocument) {
        require(document.schemaVersion in 1..CONFIGURATION_SCHEMA_VERSION) {
            "Nicht unterstützte schemaVersion: ${document.schemaVersion}"
        }

        document.appSettings?.let { settings ->
            require(AppThemeMode.entries.any { it.name == settings.themeMode }) {
                "Nicht unterstützte App-Darstellung: ${settings.themeMode}"
            }
            require(DetailPanelHeight.entries.any { it.name == settings.detailPanelHeight }) {
                "Nicht unterstützte Detailbereich-Höhe: ${settings.detailPanelHeight}"
            }
            settings.dashboardLayoutMode?.let { dashboardLayoutMode ->
                require(DashboardLayoutMode.entries.any { it.name == dashboardLayoutMode }) {
                    "Nicht unterstützte Dashboard-Darstellung: $dashboardLayoutMode"
                }
            }
        }

        requireUniqueIds(
            ids = document.wifiProfiles.map { it.id },
            type = "WLAN-Profil"
        )
        requireUniqueIds(
            ids = document.devices.map { it.id },
            type = "Gerät"
        )
        requireUniqueNames(
            names = document.wifiProfiles.map { it.name },
            type = "WLAN-Profilname"
        )

        document.wifiProfiles.forEach { profile ->
            requireNotBlank(profile.id, "WLAN-Profil-ID")
            requireNotBlank(profile.name, "WLAN-Profilname")
            requireNotBlank(profile.ssid, "SSID")
            require(
                WifiConnectionMode.entries.any { mode -> mode.name == profile.connectionMode }
            ) {
                "Nicht unterstützter WLAN-Verbindungsmodus: ${profile.connectionMode}"
            }
            profile.securityType?.let { securityType ->
                requireNotBlank(securityType, "WLAN-Sicherheitstyp")
                require(securityType in SUPPORTED_SECURITY_TYPES) {
                    "Nicht unterstützter WLAN-Sicherheitstyp: $securityType"
                }
            }
        }

        val wifiProfileIds = document.wifiProfiles.map { it.id }.toSet()
        document.devices.forEach { device ->
            requireNotBlank(device.id, "Geräte-ID")
            requireNotBlank(device.name, "Gerätename")
            requireNotBlank(device.actionLabel, "Button-Beschriftung")
            requireNotBlank(device.action.path, "API-Aufruf")
            require(
                DeviceProtocol.entries.any { protocol -> protocol.name == device.action.protocol }
            ) {
                "Unbekanntes Geräteprotokoll: ${device.action.protocol}"
            }
            require(
                ApiMethod.entries.any { method -> method.name == device.action.method }
            ) {
                "Unbekannte API-Methode: ${device.action.method}"
            }

            requireUniqueIds(
                ids = device.connections.map { it.wifiProfileId },
                type = "WLAN-Zuordnung für ${device.name}"
            )
            device.connections.forEach { connection ->
                require(connection.wifiProfileId in wifiProfileIds) {
                    "Gerät ${device.name} verweist auf ein unbekanntes WLAN-Profil"
                }
                requireNotBlank(connection.host, "Hostname/IP")
            }
        }
    }

    private fun requireUniqueIds(ids: List<String>, type: String) {
        require(ids.size == ids.toSet().size) {
            "Doppelte IDs für $type"
        }
    }

    private fun requireNotBlank(value: String, fieldName: String) {
        require(value.isNotBlank()) {
            "$fieldName darf nicht leer sein"
        }
    }

    private fun requireUniqueNames(names: List<String>, type: String) {
        val normalizedNames = names.map { it.trim().lowercase() }
        require(normalizedNames.size == normalizedNames.toSet().size) {
            "Doppelte Namen für $type"
        }
    }

    private companion object {
        val SUPPORTED_SECURITY_TYPES = setOf("WPA2_PSK", "WPA3_SAE")
    }
}
