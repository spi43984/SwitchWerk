package de.piecha.switchwerk.data.repository

import de.piecha.switchwerk.data.transfer.ConfigurationAppSettings
import de.piecha.switchwerk.domain.model.AppLanguage
import de.piecha.switchwerk.domain.model.AppSettings
import de.piecha.switchwerk.domain.model.AppThemeMode
import de.piecha.switchwerk.domain.model.DashboardLayoutMode
import de.piecha.switchwerk.domain.model.DetailPanelHeight
import de.piecha.switchwerk.domain.model.WifiProfileSortCriterion
import de.piecha.switchwerk.domain.model.WifiProfileSortDirection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ConfigurationAppSettingsTransferTest {

    @Test
    fun exportIncludesTransferableAppSettingsButNotWizardState() {
        val exported = AppSettings(
            themeMode = AppThemeMode.DARK,
            language = AppLanguage.ENGLISH,
            showActionDetails = true,
            detailPanelHeight = DetailPanelHeight.FORTY_PERCENT,
            diagnosticsNewestFirst = false,
            dashboardLayoutMode = DashboardLayoutMode.WIDGETS,
            wifiProfileSortCriterion = WifiProfileSortCriterion.SSID,
            wifiProfileSortDirection = WifiProfileSortDirection.DESCENDING,
            showSetupWizardOnStart = false,
            externalIntentsEnabled = true
        ).toConfigurationAppSettings()

        assertEquals("ENGLISH", exported.language)
        assertEquals("SSID", exported.wifiProfileSortCriterion)
        assertEquals("DESCENDING", exported.wifiProfileSortDirection)
        assertEquals(true, exported.externalIntentsEnabled)
        assertFalse(ConfigurationAppSettings::class.java.declaredFields.any {
            it.name == "showSetupWizardOnStart"
        })
    }

    @Test
    fun importAppliesNewFieldsAndKeepsWizardStateLocal() {
        val repository = FakeAppSettingsRepository(
            AppSettings(showSetupWizardOnStart = false)
        )
        transferableSettings(externalIntentsEnabled = true).applyTo(repository)

        assertEquals(AppLanguage.ENGLISH, repository.settings.value.language)
        assertEquals(
            WifiProfileSortCriterion.SSID,
            repository.settings.value.wifiProfileSortCriterion
        )
        assertEquals(
            WifiProfileSortDirection.DESCENDING,
            repository.settings.value.wifiProfileSortDirection
        )
        assertTrue(repository.settings.value.externalIntentsEnabled)
        assertFalse(repository.settings.value.showSetupWizardOnStart)
    }

    @Test
    fun oldConfigurationKeepsNewLocalSettingsUnchanged() {
        val initial = AppSettings(
            language = AppLanguage.GERMAN,
            wifiProfileSortCriterion = WifiProfileSortCriterion.SSID,
            wifiProfileSortDirection = WifiProfileSortDirection.DESCENDING,
            externalIntentsEnabled = true
        )
        val repository = FakeAppSettingsRepository(initial)
        ConfigurationAppSettings(
            themeMode = "LIGHT",
            showActionDetails = true,
            detailPanelHeight = "TWENTY_PERCENT",
            diagnosticsNewestFirst = false
        ).applyTo(repository)

        assertEquals(AppLanguage.GERMAN, repository.settings.value.language)
        assertEquals(WifiProfileSortCriterion.SSID, repository.settings.value.wifiProfileSortCriterion)
        assertEquals(
            WifiProfileSortDirection.DESCENDING,
            repository.settings.value.wifiProfileSortDirection
        )
        assertTrue(repository.settings.value.externalIntentsEnabled)
    }

    @Test
    fun externalIntentChangeIsReportedOnlyWhenValueChanges() {
        val currentlyDisabled = AppSettings(externalIntentsEnabled = false)
        val currentlyEnabled = AppSettings(externalIntentsEnabled = true)

        assertEquals(
            true,
            transferableSettings(true).externalIntentsChangeFrom(currentlyDisabled)
        )
        assertEquals(
            false,
            transferableSettings(false).externalIntentsChangeFrom(currentlyEnabled)
        )
        assertNull(transferableSettings(false).externalIntentsChangeFrom(currentlyDisabled))
        assertNull(
            transferableSettings(null).externalIntentsChangeFrom(currentlyDisabled)
        )
    }

    private fun transferableSettings(
        externalIntentsEnabled: Boolean?
    ) = ConfigurationAppSettings(
        themeMode = "DARK",
        showActionDetails = true,
        detailPanelHeight = "FORTY_PERCENT",
        diagnosticsNewestFirst = false,
        dashboardLayoutMode = "WIDGETS",
        language = "ENGLISH",
        wifiProfileSortCriterion = "SSID",
        wifiProfileSortDirection = "DESCENDING",
        externalIntentsEnabled = externalIntentsEnabled
    )
}
