package de.piecha.switchwerk.data.transfer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class ConfigurationImportValidatorTest {

    private val validator = ConfigurationImportValidator()

    @Test
    fun validDocumentAcceptsEmptyPasswordAsDeletion() {
        validator.validate(
            validDocument(
                wifiProfiles = listOf(
                    wifiProfile(
                        password = "",
                        isPasswordPresent = true
                    )
                )
            )
        )
    }

    @Test
    fun unsupportedSchemaVersionIsRejected() {
        val error = assertThrows(IllegalArgumentException::class.java) {
            validator.validate(validDocument().copy(schemaVersion = CONFIGURATION_SCHEMA_VERSION + 1))
        }

        assertTrue(error.message.orEmpty().contains("schemaVersion"))
    }

    @Test
    fun schemaVersionOneWithoutAppSettingsRemainsSupported() {
        validator.validate(
            validDocument().copy(
                schemaVersion = 1,
                appSettings = null
            )
        )
    }

    @Test
    fun actionWithoutRequestBodyAndContentTypeUsesCompatibleDefaults() {
        val action = ConfigurationDeviceAction(
            method = "POST",
            path = "/rpc/action"
        )

        validator.validate(
            validDocument(
                devices = listOf(device().copy(action = action))
            )
        )

        assertEquals(null, action.requestBody)
        assertEquals("APPLICATION_JSON", action.contentType)
    }

    @Test
    fun validAppSettingsAreAccepted() {
        validator.validate(
            validDocument().copy(
                appSettings = ConfigurationAppSettings(
                    themeMode = "DARK",
                    showActionDetails = true,
                    detailPanelHeight = "FORTY_PERCENT",
                    diagnosticsNewestFirst = false,
                    dashboardLayoutMode = "WIDGETS",
                    language = "ENGLISH",
                    wifiProfileSortCriterion = "SSID",
                    wifiProfileSortDirection = "DESCENDING",
                    externalIntentsEnabled = true
                )
            )
        )
    }

    @Test
    fun appSettingsWithoutDashboardLayoutRemainSupported() {
        validator.validate(
            validDocument().copy(
                appSettings = ConfigurationAppSettings(
                    themeMode = "SYSTEM",
                    showActionDetails = false,
                    detailPanelHeight = "THIRTY_PERCENT",
                    diagnosticsNewestFirst = true
                )
            )
        )
    }

    @Test
    fun unsupportedDashboardLayoutIsRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            validator.validate(
                validDocument().copy(
                    appSettings = ConfigurationAppSettings(
                        themeMode = "SYSTEM",
                        showActionDetails = false,
                        detailPanelHeight = "THIRTY_PERCENT",
                        diagnosticsNewestFirst = true,
                        dashboardLayoutMode = "FREEFORM"
                    )
                )
            )
        }
    }

    @Test
    fun unsupportedAppThemeIsRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            validator.validate(
                validDocument().copy(
                    appSettings = ConfigurationAppSettings(
                        themeMode = "BLUE",
                        showActionDetails = true,
                        detailPanelHeight = "THIRTY_PERCENT",
                        diagnosticsNewestFirst = true
                    )
                )
            )
        }
    }

    @Test
    fun unsupportedNewAppSettingEnumsAreRejected() {
        listOf(
            validAppSettings().copy(language = "KLINGON"),
            validAppSettings().copy(wifiProfileSortCriterion = "CREATED_AT"),
            validAppSettings().copy(wifiProfileSortDirection = "RANDOM")
        ).forEach { settings ->
            assertThrows(IllegalArgumentException::class.java) {
                validator.validate(validDocument().copy(appSettings = settings))
            }
        }
    }

    @Test
    fun incompleteWifiProfileSortingIsRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            validator.validate(
                validDocument().copy(
                    appSettings = validAppSettings().copy(wifiProfileSortDirection = null)
                )
            )
        }
    }

    @Test
    fun duplicateWifiProfileIdsAreRejected() {
        val profile = wifiProfile()

        assertThrows(IllegalArgumentException::class.java) {
            validator.validate(
                validDocument(wifiProfiles = listOf(profile, profile))
            )
        }
    }

    @Test
    fun duplicateWifiProfileNamesAreRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            validator.validate(
                validDocument(
                    wifiProfiles = listOf(
                        wifiProfile(id = "wifi-1", name = "Home"),
                        wifiProfile(id = "wifi-2", name = " home ")
                    )
                )
            )
        }
    }

    @Test
    fun mergeRejectsImportedWifiProfileNameThatExistsLocallyForAnotherProfile() {
        val error = assertThrows(IllegalArgumentException::class.java) {
            validator.validateMerge(
                document = validDocument(
                    wifiProfiles = listOf(
                        wifiProfile(id = "wifi-imported", name = " garage ")
                    )
                ),
                existingWifiProfileNamesById = mapOf("wifi-local" to "Garage")
            )
        }

        assertTrue(error.message.orEmpty().contains("existiert bereits lokal"))
    }

    @Test
    fun mergeAllowsImportedWifiProfileNameThatBelongsToSameLocalProfile() {
        validator.validateMerge(
            document = validDocument(
                wifiProfiles = listOf(
                    wifiProfile(id = "wifi-1", name = " garage ")
                )
            ),
            existingWifiProfileNamesById = mapOf("wifi-1" to "GARAGE")
        )
    }

    @Test
    fun duplicateWifiProfileSsidsAreAcceptedWhenNamesAreUnique() {
        validator.validate(
            validDocument(
                wifiProfiles = listOf(
                    wifiProfile(id = "wifi-1", name = "Garage", ssid = "Shelly"),
                    wifiProfile(id = "wifi-2", name = "Keller", ssid = "Shelly")
                )
            )
        )
    }

    @Test
    fun wpa3SecurityTypeIsAccepted() {
        validator.validate(
            validDocument(
                wifiProfiles = listOf(wifiProfile(securityType = "WPA3_SAE"))
            )
        )
    }

    @Test
    fun missingSecurityTypeIsAcceptedAsUnknown() {
        validator.validate(
            validDocument(
                wifiProfiles = listOf(wifiProfile(securityType = null))
            )
        )
    }

    @Test
    fun androidManagedWifiConnectionModeIsAccepted() {
        validator.validate(
            validDocument(
                wifiProfiles = listOf(wifiProfile(connectionMode = "ANDROID_MANAGED"))
            )
        )
    }

    @Test
    fun unsupportedWifiConnectionModeIsRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            validator.validate(
                validDocument(
                    wifiProfiles = listOf(wifiProfile(connectionMode = "EXTERNAL"))
                )
            )
        }
    }

    @Test
    fun connectionToUnknownWifiProfileIsRejected() {
        val device = validDocument().devices.single().copy(
            connections = listOf(
                ConfigurationDeviceConnection(
                    wifiProfileId = "unknown",
                    host = "192.168.1.10"
                )
            )
        )

        assertThrows(IllegalArgumentException::class.java) {
            validator.validate(validDocument(devices = listOf(device)))
        }
    }

    @Test
    fun unsupportedApiMethodIsRejected() {
        val device = validDocument().devices.single().copy(
            action = ConfigurationDeviceAction(
                method = "DELETE",
                path = "/rpc/action"
            )
        )

        assertThrows(IllegalArgumentException::class.java) {
            validator.validate(validDocument(devices = listOf(device)))
        }
    }

    @Test
    fun unsupportedDeviceProtocolIsRejected() {
        val device = validDocument().devices.single().copy(
            action = ConfigurationDeviceAction(
                protocol = "FTP",
                method = "GET",
                path = "/rpc/action"
            )
        )

        assertThrows(IllegalArgumentException::class.java) {
            validator.validate(validDocument(devices = listOf(device)))
        }
    }

    @Test
    fun supportedActionContentTypesAreAccepted() {
        validator.validate(
            validDocument(
                devices = listOf(
                    device().copy(
                        action = ConfigurationDeviceAction(
                            method = "POST",
                            path = "/rpc/action",
                            requestBody = "line 1\nline 2",
                            contentType = "TEXT_PLAIN"
                        )
                    )
                )
            )
        )
    }

    @Test
    fun unsupportedActionContentTypeIsRejected() {
        val device = validDocument().devices.single().copy(
            action = ConfigurationDeviceAction(
                method = "POST",
                path = "/rpc/action",
                contentType = "APPLICATION_XML"
            )
        )

        assertThrows(IllegalArgumentException::class.java) {
            validator.validate(validDocument(devices = listOf(device)))
        }
    }

    @Test
    fun switchGroupMayContainSameDeviceMultipleTimes() {
        validator.validate(
            validDocument(
                switchGroups = listOf(
                    switchGroup(
                        members = listOf(
                            switchGroupMember(id = "member-1", deviceId = "device-1"),
                            switchGroupMember(id = "member-2", deviceId = "device-1")
                        )
                    )
                )
            )
        )
    }

    @Test
    fun duplicateSwitchGroupMemberIdsAreRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            validator.validate(
                validDocument(
                    switchGroups = listOf(
                        switchGroup(
                            members = listOf(
                                switchGroupMember(id = "member-1", deviceId = "device-1"),
                                switchGroupMember(id = "member-1", deviceId = "device-1")
                            )
                        )
                    )
                )
            )
        }
    }

    @Test
    fun oneHourSwitchGroupPauseIsAccepted() {
        validator.validate(
            validDocument(
                switchGroups = listOf(
                    switchGroup(
                        members = listOf(
                            switchGroupMember(pauseAfterMillis = 3_600_000L)
                        )
                    )
                )
            )
        )
    }

    @Test
    fun switchGroupPauseAboveOneHourIsRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            validator.validate(
                validDocument(
                    switchGroups = listOf(
                        switchGroup(
                            members = listOf(
                                switchGroupMember(pauseAfterMillis = 3_600_001L)
                            )
                        )
                    )
                )
            )
        }
    }

    @Test
    fun unknownDeviceColorIsRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            validator.validate(
                validDocument(devices = listOf(device().copy(color = "CUSTOM")))
            )
        }
    }

    @Test
    fun unknownSwitchGroupColorIsRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            validator.validate(
                validDocument(
                    switchGroups = listOf(switchGroup().copy(color = "CUSTOM"))
                )
            )
        }
    }

    private fun validDocument(
        wifiProfiles: List<ConfigurationWifiProfile> = listOf(wifiProfile()),
        devices: List<ConfigurationDevice> = listOf(device()),
        switchGroups: List<ConfigurationSwitchGroup> = emptyList()
    ): ConfigurationDocument {
        return ConfigurationDocument(
            schemaVersion = CONFIGURATION_SCHEMA_VERSION,
            wifiProfiles = wifiProfiles,
            devices = devices,
            switchGroups = switchGroups
        )
    }

    private fun validAppSettings() = ConfigurationAppSettings(
        themeMode = "SYSTEM",
        showActionDetails = false,
        detailPanelHeight = "THIRTY_PERCENT",
        diagnosticsNewestFirst = true,
        dashboardLayoutMode = "LIST",
        language = "SYSTEM",
        wifiProfileSortCriterion = "PROFILE_NAME",
        wifiProfileSortDirection = "ASCENDING",
        externalIntentsEnabled = false
    )

    private fun wifiProfile(
        id: String = "wifi-1",
        name: String = "Home",
        ssid: String = "Home",
        connectionMode: String = "SWITCHWERK_MANAGED",
        securityType: String? = "WPA2_PSK",
        password: String? = null,
        isPasswordPresent: Boolean = false
    ): ConfigurationWifiProfile {
        return ConfigurationWifiProfile(
            id = id,
            name = name,
            ssid = ssid,
            connectionMode = connectionMode,
            securityType = securityType,
            password = password,
            isPasswordPresent = isPasswordPresent
        )
    }

    private fun device(): ConfigurationDevice {
        return ConfigurationDevice(
            id = "device-1",
            name = "Light",
            actionLabel = "Switch",
            action = ConfigurationDeviceAction(
                method = "GET",
                path = "/rpc/action"
            ),
            connections = listOf(
                ConfigurationDeviceConnection(
                    wifiProfileId = "wifi-1",
                    host = "192.168.1.10"
                )
            )
        )
    }

    private fun switchGroup(
        members: List<ConfigurationSwitchGroupMember> = listOf(switchGroupMember())
    ): ConfigurationSwitchGroup {
        return ConfigurationSwitchGroup(
            id = "group-1",
            name = "Group",
            actionLabel = "Run",
            members = members
        )
    }

    private fun switchGroupMember(
        id: String = "member-1",
        deviceId: String = "device-1",
        pauseAfterMillis: Long = 0L
    ): ConfigurationSwitchGroupMember {
        return ConfigurationSwitchGroupMember(
            id = id,
            deviceId = deviceId,
            pauseAfterMillis = pauseAfterMillis
        )
    }
}
