package de.piecha.switchwerk.widget

import android.content.Context

class SharedPreferencesWidgetActionStore(
    context: Context
) : WidgetActionStore {

    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    override fun getWidgetIds(): List<Int> {
        return preferences.getStringSet(KEY_WIDGET_IDS, emptySet())
            .orEmpty()
            .mapNotNull(String::toIntOrNull)
            .sorted()
    }

    override fun getTargets(appWidgetId: Int): List<WidgetActionTarget> {
        return preferences.getString(keyTargets(appWidgetId), null)
            ?.lineSequence()
            .orEmpty()
            .mapNotNull(::decodeTarget)
            .toList()
    }

    override fun getTitle(appWidgetId: Int): String {
        return preferences.getString(keyTitle(appWidgetId), null).orEmpty()
    }

    override fun getColumnMode(appWidgetId: Int): WidgetColumnMode {
        val mode = preferences.getString(keyColumnMode(appWidgetId), null)
        return WidgetColumnMode.entries.firstOrNull { it.name == mode } ?: WidgetColumnMode.AUTO
    }

    override fun saveWidget(
        appWidgetId: Int,
        title: String,
        columnMode: WidgetColumnMode,
        targets: List<WidgetActionTarget>
    ) {
        val widgetIds = getWidgetIds().toMutableSet().apply { add(appWidgetId) }
        preferences.edit()
            .putStringSet(KEY_WIDGET_IDS, widgetIds.map(Int::toString).toSet())
            .putString(keyTitle(appWidgetId), title.trim())
            .putString(keyColumnMode(appWidgetId), columnMode.name)
            .putString(keyTargets(appWidgetId), targets.joinToString(separator = "\n", transform = ::encodeTarget))
            .apply()
    }

    override fun saveTargets(appWidgetId: Int, targets: List<WidgetActionTarget>) {
        saveWidget(appWidgetId, getTitle(appWidgetId), getColumnMode(appWidgetId), targets)
    }

    override fun deleteWidget(appWidgetId: Int) {
        val widgetIds = getWidgetIds().filterNot { it == appWidgetId }
        preferences.edit()
            .putStringSet(KEY_WIDGET_IDS, widgetIds.map(Int::toString).toSet())
            .remove(keyTitle(appWidgetId))
            .remove(keyColumnMode(appWidgetId))
            .remove(keyTargets(appWidgetId))
            .remove(keyStatus(appWidgetId))
            .apply()
    }

    @Synchronized
    override fun getStatus(appWidgetId: Int, entryIndex: Int): WidgetActionStatus {
        val statusName = preferences.getStringSet(keyStatus(appWidgetId), emptySet())
            .orEmpty()
            .firstOrNull { it.startsWith("$entryIndex:") }
            ?.substringAfter(":")
        return statusName
            ?.let { name -> WidgetActionStatus.entries.firstOrNull { it.name == name } }
            ?: WidgetActionStatus.IDLE
    }

    @Synchronized
    override fun trySetRunning(appWidgetId: Int, entryIndex: Int): Boolean {
        if (getStatus(appWidgetId, entryIndex) == WidgetActionStatus.RUNNING) return false
        setStatus(appWidgetId, entryIndex, WidgetActionStatus.RUNNING)
        return true
    }

    @Synchronized
    override fun setStatus(appWidgetId: Int, entryIndex: Int, status: WidgetActionStatus) {
        val statusValues = preferences.getStringSet(keyStatus(appWidgetId), emptySet())
            .orEmpty()
            .filterNot { it.startsWith("$entryIndex:") }
            .toMutableSet()
        if (status != WidgetActionStatus.IDLE) {
            statusValues += "$entryIndex:${status.name}"
        }
        preferences.edit()
            .putStringSet(keyStatus(appWidgetId), statusValues)
            .apply()
    }

    private fun encodeTarget(target: WidgetActionTarget): String {
        return "${target.type.name}:${target.id}"
    }

    private fun decodeTarget(value: String): WidgetActionTarget? {
        val typeName = value.substringBefore(":", missingDelimiterValue = "")
        val id = value.substringAfter(":", missingDelimiterValue = "")
        if (id.isBlank()) return null
        val type = WidgetActionTargetType.entries.firstOrNull { it.name == typeName } ?: return null
        return WidgetActionTarget(type, id)
    }

    private fun keyTargets(appWidgetId: Int): String = "widget.$appWidgetId.targets"

    private fun keyTitle(appWidgetId: Int): String = "widget.$appWidgetId.title"

    private fun keyColumnMode(appWidgetId: Int): String = "widget.$appWidgetId.column_mode"

    private fun keyStatus(appWidgetId: Int): String = "widget.$appWidgetId.status"

    private companion object {
        const val PREFERENCES_NAME = "switchwerk_widgets"
        const val KEY_WIDGET_IDS = "widget_ids"
    }
}
