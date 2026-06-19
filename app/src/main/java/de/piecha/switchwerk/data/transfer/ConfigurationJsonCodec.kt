package de.piecha.switchwerk.data.transfer

import android.util.JsonReader
import android.util.JsonToken
import android.util.JsonWriter
import java.io.StringReader
import java.io.StringWriter

class ConfigurationJsonCodec {

    fun encode(document: ConfigurationDocument): String {
        return StringWriter().use { output ->
            JsonWriter(output).use { writer ->
                writer.setIndent("  ")
                writer.beginObject()
                writer.name("schemaVersion").value(document.schemaVersion.toLong())
                document.appSettings?.let { settings ->
                    writer.name("appSettings")
                    writer.beginObject()
                    writer.name("themeMode").value(settings.themeMode)
                    writer.name("showActionDetails").value(settings.showActionDetails)
                    writer.name("detailPanelHeight").value(settings.detailPanelHeight)
                    writer.name("diagnosticsNewestFirst").value(settings.diagnosticsNewestFirst)
                    writer.endObject()
                }
                writer.name("wifiProfiles")
                writer.beginArray()
                document.wifiProfiles.forEach { profile ->
                    writer.beginObject()
                    writer.name("id").value(profile.id)
                    writer.name("name").value(profile.name)
                    writer.name("ssid").value(profile.ssid)
                    profile.securityType?.let { securityType ->
                        writer.name("securityType").value(securityType)
                    }
                    if (profile.isPasswordPresent) {
                        writer.name("password").value(profile.password.orEmpty())
                    }
                    writer.endObject()
                }
                writer.endArray()
                writer.name("devices")
                writer.beginArray()
                document.devices.forEach { device ->
                    writer.beginObject()
                    writer.name("id").value(device.id)
                    writer.name("name").value(device.name)
                    writer.name("actionLabel").value(device.actionLabel)
                    writer.name("action")
                    writer.beginObject()
                    writer.name("method").value(device.action.method)
                    writer.name("path").value(device.action.path)
                    writer.endObject()
                    writer.name("connections")
                    writer.beginArray()
                    device.connections.forEach { connection ->
                        writer.beginObject()
                        writer.name("wifiProfileId").value(connection.wifiProfileId)
                        writer.name("host").value(connection.host)
                        writer.endObject()
                    }
                    writer.endArray()
                    writer.endObject()
                }
                writer.endArray()
                writer.endObject()
            }
            output.toString()
        }
    }

    fun decode(json: String): ConfigurationDocument {
        return JsonReader(StringReader(json)).use { reader ->
            var schemaVersion: Int? = null
            var wifiProfiles: List<ConfigurationWifiProfile>? = null
            var devices: List<ConfigurationDevice>? = null
            var appSettings: ConfigurationAppSettings? = null

            reader.beginObject()
            while (reader.hasNext()) {
                when (reader.nextName()) {
                    "schemaVersion" -> schemaVersion = reader.nextInt()
                    "appSettings" -> appSettings = reader.readAppSettings()
                    "wifiProfiles" -> wifiProfiles = reader.readWifiProfiles()
                    "devices" -> devices = reader.readDevices()
                    else -> reader.skipValue()
                }
            }
            reader.endObject()

            ConfigurationDocument(
                schemaVersion = requireField(schemaVersion, "schemaVersion"),
                wifiProfiles = requireField(wifiProfiles, "wifiProfiles"),
                devices = requireField(devices, "devices"),
                appSettings = appSettings
            )
        }
    }

    private fun JsonReader.readAppSettings(): ConfigurationAppSettings {
        var themeMode: String? = null
        var showActionDetails: Boolean? = null
        var detailPanelHeight: String? = null
        var diagnosticsNewestFirst: Boolean? = null

        beginObject()
        while (hasNext()) {
            when (nextName()) {
                "themeMode" -> themeMode = nextString()
                "showActionDetails" -> showActionDetails = nextBoolean()
                "detailPanelHeight" -> detailPanelHeight = nextString()
                "diagnosticsNewestFirst" -> diagnosticsNewestFirst = nextBoolean()
                else -> skipValue()
            }
        }
        endObject()

        return ConfigurationAppSettings(
            themeMode = requireField(themeMode, "appSettings.themeMode"),
            showActionDetails = requireField(
                showActionDetails,
                "appSettings.showActionDetails"
            ),
            detailPanelHeight = requireField(
                detailPanelHeight,
                "appSettings.detailPanelHeight"
            ),
            diagnosticsNewestFirst = requireField(
                diagnosticsNewestFirst,
                "appSettings.diagnosticsNewestFirst"
            )
        )
    }

    private fun JsonReader.readWifiProfiles(): List<ConfigurationWifiProfile> {
        val profiles = mutableListOf<ConfigurationWifiProfile>()
        beginArray()
        while (hasNext()) {
            var id: String? = null
            var name: String? = null
            var ssid: String? = null
            var securityType: String? = null
            var password: String? = null
            var isPasswordPresent = false

            beginObject()
            while (hasNext()) {
                when (nextName()) {
                    "id" -> id = nextString()
                    "name" -> name = nextString()
                    "ssid" -> ssid = nextString()
                    "securityType" -> {
                        securityType = if (peek() == JsonToken.NULL) {
                            nextNull()
                            null
                        } else {
                            nextString()
                        }
                    }
                    "password" -> {
                        isPasswordPresent = true
                        if (peek() == JsonToken.NULL) {
                            nextNull()
                            throw IllegalArgumentException("Passwort darf nicht null sein")
                        }
                        password = nextString()
                    }
                    else -> skipValue()
                }
            }
            endObject()

            val requiredSsid = requireField(ssid, "wifiProfiles.ssid")
            profiles += ConfigurationWifiProfile(
                id = requireField(id, "wifiProfiles.id"),
                name = name ?: requiredSsid,
                ssid = requiredSsid,
                securityType = securityType,
                password = password,
                isPasswordPresent = isPasswordPresent
            )
        }
        endArray()
        return profiles
    }

    private fun JsonReader.readDevices(): List<ConfigurationDevice> {
        val devices = mutableListOf<ConfigurationDevice>()
        beginArray()
        while (hasNext()) {
            var id: String? = null
            var name: String? = null
            var actionLabel: String? = null
            var action: ConfigurationDeviceAction? = null
            var connections: List<ConfigurationDeviceConnection>? = null

            beginObject()
            while (hasNext()) {
                when (nextName()) {
                    "id" -> id = nextString()
                    "name" -> name = nextString()
                    "actionLabel" -> actionLabel = nextString()
                    "action" -> action = readAction()
                    "connections" -> connections = readConnections()
                    else -> skipValue()
                }
            }
            endObject()

            devices += ConfigurationDevice(
                id = requireField(id, "devices.id"),
                name = requireField(name, "devices.name"),
                actionLabel = requireField(actionLabel, "devices.actionLabel"),
                action = requireField(action, "devices.action"),
                connections = requireField(connections, "devices.connections")
            )
        }
        endArray()
        return devices
    }

    private fun JsonReader.readAction(): ConfigurationDeviceAction {
        var method: String? = null
        var path: String? = null

        beginObject()
        while (hasNext()) {
            when (nextName()) {
                "method" -> method = nextString()
                "path" -> path = nextString()
                else -> skipValue()
            }
        }
        endObject()

        return ConfigurationDeviceAction(
            method = requireField(method, "devices.action.method"),
            path = requireField(path, "devices.action.path")
        )
    }

    private fun JsonReader.readConnections(): List<ConfigurationDeviceConnection> {
        val connections = mutableListOf<ConfigurationDeviceConnection>()
        beginArray()
        while (hasNext()) {
            var wifiProfileId: String? = null
            var host: String? = null

            beginObject()
            while (hasNext()) {
                when (nextName()) {
                    "wifiProfileId" -> wifiProfileId = nextString()
                    "host" -> host = nextString()
                    else -> skipValue()
                }
            }
            endObject()

            connections += ConfigurationDeviceConnection(
                wifiProfileId = requireField(wifiProfileId, "connections.wifiProfileId"),
                host = requireField(host, "connections.host")
            )
        }
        endArray()
        return connections
    }

    private fun <T> requireField(value: T?, fieldName: String): T {
        return value ?: throw IllegalArgumentException("Pflichtfeld fehlt: $fieldName")
    }
}
