package de.piecha.switchwerk.data.repository

import android.content.Context
import de.piecha.switchwerk.domain.model.AppSettings
import de.piecha.switchwerk.domain.model.AppThemeMode
import de.piecha.switchwerk.domain.model.DetailPanelHeight
import kotlinx.coroutines.flow.StateFlow

interface AppSettingsRepository {
    val settings: StateFlow<AppSettings>

    fun setThemeMode(themeMode: AppThemeMode)

    fun setShowActionDetails(showActionDetails: Boolean)

    fun setDetailPanelHeight(detailPanelHeight: DetailPanelHeight)

    fun setDiagnosticsNewestFirst(diagnosticsNewestFirst: Boolean)
}

class SharedPreferencesAppSettingsRepository(context: Context) : AppSettingsRepository {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    private val mutableSettings = kotlinx.coroutines.flow.MutableStateFlow(loadSettings())

    override val settings: StateFlow<AppSettings> = mutableSettings

    override fun setThemeMode(themeMode: AppThemeMode) {
        preferences.edit().putString(KEY_THEME_MODE, themeMode.name).apply()
        mutableSettings.value = mutableSettings.value.copy(themeMode = themeMode)
    }

    override fun setShowActionDetails(showActionDetails: Boolean) {
        preferences.edit().putBoolean(KEY_SHOW_ACTION_DETAILS, showActionDetails).apply()
        mutableSettings.value = mutableSettings.value.copy(showActionDetails = showActionDetails)
    }

    override fun setDetailPanelHeight(detailPanelHeight: DetailPanelHeight) {
        preferences.edit().putString(KEY_DETAIL_PANEL_HEIGHT, detailPanelHeight.name).apply()
        mutableSettings.value = mutableSettings.value.copy(detailPanelHeight = detailPanelHeight)
    }

    override fun setDiagnosticsNewestFirst(diagnosticsNewestFirst: Boolean) {
        preferences.edit()
            .putBoolean(KEY_DIAGNOSTICS_NEWEST_FIRST, diagnosticsNewestFirst)
            .apply()
        mutableSettings.value = mutableSettings.value.copy(
            diagnosticsNewestFirst = diagnosticsNewestFirst
        )
    }

    private fun loadSettings(): AppSettings {
        return AppSettings(
            themeMode = preferences.getEnum(KEY_THEME_MODE, AppThemeMode.SYSTEM),
            showActionDetails = preferences.getBoolean(KEY_SHOW_ACTION_DETAILS, false),
            detailPanelHeight = preferences.getEnum(
                KEY_DETAIL_PANEL_HEIGHT,
                DetailPanelHeight.THIRTY_PERCENT
            ),
            diagnosticsNewestFirst = preferences.getBoolean(
                KEY_DIAGNOSTICS_NEWEST_FIRST,
                true
            )
        )
    }

    private inline fun <reified T : Enum<T>> android.content.SharedPreferences.getEnum(
        key: String,
        defaultValue: T
    ): T {
        val storedValue = getString(key, null) ?: return defaultValue
        return enumValues<T>().firstOrNull { it.name == storedValue } ?: defaultValue
    }

    private companion object {
        const val PREFERENCES_NAME = "app_settings"
        const val KEY_THEME_MODE = "theme_mode"
        const val KEY_SHOW_ACTION_DETAILS = "show_action_details"
        const val KEY_DETAIL_PANEL_HEIGHT = "detail_panel_height"
        const val KEY_DIAGNOSTICS_NEWEST_FIRST = "diagnostics_newest_first"
    }
}
