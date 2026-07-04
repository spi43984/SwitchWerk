package de.piecha.switchwerk.data.repository

import de.piecha.switchwerk.domain.model.AppSettings
import de.piecha.switchwerk.domain.model.AppLanguage
import de.piecha.switchwerk.domain.model.AppThemeMode
import de.piecha.switchwerk.domain.model.DashboardLayoutMode
import de.piecha.switchwerk.domain.model.DetailPanelHeight
import de.piecha.switchwerk.domain.model.WifiProfileSortCriterion
import de.piecha.switchwerk.domain.model.WifiProfileSortDirection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeAppSettingsRepository(
    initialSettings: AppSettings = AppSettings()
) : AppSettingsRepository {
    private val mutableSettings = MutableStateFlow(initialSettings)

    override val settings: StateFlow<AppSettings> = mutableSettings

    override fun setThemeMode(themeMode: AppThemeMode) {
        mutableSettings.value = mutableSettings.value.copy(themeMode = themeMode)
    }

    override fun setLanguage(language: AppLanguage) {
        mutableSettings.value = mutableSettings.value.copy(language = language)
    }

    override fun setShowActionDetails(showActionDetails: Boolean) {
        mutableSettings.value = mutableSettings.value.copy(showActionDetails = showActionDetails)
    }

    override fun setDetailPanelHeight(detailPanelHeight: DetailPanelHeight) {
        mutableSettings.value = mutableSettings.value.copy(detailPanelHeight = detailPanelHeight)
    }

    override fun setDiagnosticsNewestFirst(diagnosticsNewestFirst: Boolean) {
        mutableSettings.value = mutableSettings.value.copy(
            diagnosticsNewestFirst = diagnosticsNewestFirst
        )
    }

    override fun setDashboardLayoutMode(dashboardLayoutMode: DashboardLayoutMode) {
        mutableSettings.value = mutableSettings.value.copy(
            dashboardLayoutMode = dashboardLayoutMode
        )
    }

    override fun setWifiProfileSorting(
        criterion: WifiProfileSortCriterion,
        direction: WifiProfileSortDirection
    ) {
        mutableSettings.value = mutableSettings.value.copy(
            wifiProfileSortCriterion = criterion,
            wifiProfileSortDirection = direction
        )
    }

    override fun setShowSetupWizardOnStart(showSetupWizardOnStart: Boolean) {
        mutableSettings.value = mutableSettings.value.copy(
            showSetupWizardOnStart = showSetupWizardOnStart
        )
    }

    override fun setExternalIntentsEnabled(enabled: Boolean) {
        mutableSettings.value = mutableSettings.value.copy(externalIntentsEnabled = enabled)
    }
}
