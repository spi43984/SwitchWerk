package de.piecha.switchwerk.widget

interface WidgetActionStore {
    fun getWidgetIds(): List<Int>
    fun getTitle(appWidgetId: Int): String
    fun getColumnMode(appWidgetId: Int): WidgetColumnMode
    fun getTargets(appWidgetId: Int): List<WidgetActionTarget>
    fun saveWidget(
        appWidgetId: Int,
        title: String,
        columnMode: WidgetColumnMode,
        targets: List<WidgetActionTarget>
    )
    fun saveTargets(appWidgetId: Int, targets: List<WidgetActionTarget>)
    fun deleteWidget(appWidgetId: Int)
    fun getStatus(appWidgetId: Int, entryIndex: Int): WidgetActionStatus
    fun setStatus(appWidgetId: Int, entryIndex: Int, status: WidgetActionStatus)
}

enum class WidgetColumnMode {
    AUTO,
    ONE,
    TWO
}

enum class WidgetActionStatus {
    IDLE,
    RUNNING,
    SUCCESS,
    ERROR
}
