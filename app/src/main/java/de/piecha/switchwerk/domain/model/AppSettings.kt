package de.piecha.switchwerk.domain.model

enum class AppThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

enum class DetailPanelHeight(val fraction: Float) {
    TWENTY_PERCENT(0.20f),
    THIRTY_PERCENT(0.30f),
    FORTY_PERCENT(0.40f)
}

data class AppSettings(
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val showActionDetails: Boolean = false,
    val detailPanelHeight: DetailPanelHeight = DetailPanelHeight.THIRTY_PERCENT,
    val diagnosticsNewestFirst: Boolean = true
)
