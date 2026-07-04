package de.piecha.switchwerk.domain.model

enum class AppThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

enum class AppLanguage(val languageTag: String?) {
    SYSTEM(null),
    GERMAN("de"),
    ENGLISH("en")
}

enum class DetailPanelHeight(val fraction: Float) {
    TWENTY_PERCENT(0.20f),
    THIRTY_PERCENT(0.30f),
    FORTY_PERCENT(0.40f)
}

enum class DashboardLayoutMode {
    LIST,
    WIDGETS
}

enum class WifiProfileSortCriterion {
    PROFILE_NAME,
    SSID
}

enum class WifiProfileSortDirection {
    ASCENDING,
    DESCENDING
}

data class AppSettings(
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val language: AppLanguage = AppLanguage.SYSTEM,
    val showActionDetails: Boolean = false,
    val detailPanelHeight: DetailPanelHeight = DetailPanelHeight.THIRTY_PERCENT,
    val diagnosticsNewestFirst: Boolean = true,
    val dashboardLayoutMode: DashboardLayoutMode = DashboardLayoutMode.LIST,
    val wifiProfileSortCriterion: WifiProfileSortCriterion = WifiProfileSortCriterion.PROFILE_NAME,
    val wifiProfileSortDirection: WifiProfileSortDirection = WifiProfileSortDirection.ASCENDING,
    val showSetupWizardOnStart: Boolean = true,
    val externalIntentsEnabled: Boolean = false
)
