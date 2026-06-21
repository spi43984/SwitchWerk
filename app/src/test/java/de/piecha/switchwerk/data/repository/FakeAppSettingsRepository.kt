package de.piecha.switchwerk.data.repository

import de.piecha.switchwerk.domain.model.AppSettings
import de.piecha.switchwerk.domain.model.AppThemeMode
import de.piecha.switchwerk.domain.model.DashboardLayoutMode
import de.piecha.switchwerk.domain.model.DetailPanelHeight
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
}
