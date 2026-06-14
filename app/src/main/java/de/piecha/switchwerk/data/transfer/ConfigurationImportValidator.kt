package de.piecha.switchwerk.data.transfer

import de.piecha.switchwerk.domain.model.ApiMethod

class ConfigurationImportValidator {

    fun validate(document: ConfigurationDocument) {
        require(document.schemaVersion == CONFIGURATION_SCHEMA_VERSION) {
            "Nicht unterstützte schemaVersion: ${document.schemaVersion}"
        }

        requireUniqueIds(
            ids = document.wifiProfiles.map { it.id },
            type = "WLAN-Profil"
        )
        requireUniqueIds(
            ids = document.devices.map { it.id },
            type = "Gerät"
        )

        document.wifiProfiles.forEach { profile ->
            requireNotBlank(profile.id, "WLAN-Profil-ID")
            requireNotBlank(profile.ssid, "SSID")
            requireNotBlank(profile.securityType, "WLAN-Sicherheitstyp")
            require(profile.securityType == SUPPORTED_SECURITY_TYPE) {
                "Nicht unterstützter WLAN-Sicherheitstyp: ${profile.securityType}"
            }
        }

        val wifiProfileIds = document.wifiProfiles.map { it.id }.toSet()
        document.devices.forEach { device ->
            requireNotBlank(device.id, "Geräte-ID")
            requireNotBlank(device.name, "Gerätename")
            requireNotBlank(device.actionLabel, "Button-Beschriftung")
            requireNotBlank(device.action.path, "API-Aufruf")
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

    private companion object {
        const val SUPPORTED_SECURITY_TYPE = "WPA2_PSK"
    }
}
