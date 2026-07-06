package de.piecha.switchwerk.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.View
import android.widget.RemoteViews
import de.piecha.switchwerk.R
import de.piecha.switchwerk.data.network.WifiProximityService
import de.piecha.switchwerk.data.repository.DeviceRepository
import de.piecha.switchwerk.data.repository.SwitchGroupRepository
import de.piecha.switchwerk.data.repository.WifiProfileRepository
import de.piecha.switchwerk.viewmodel.DashboardItem
import de.piecha.switchwerk.viewmodel.DeviceWifiProximityStatus
import de.piecha.switchwerk.viewmodel.resolveWifiProximityStatuses
import java.util.concurrent.ConcurrentHashMap

class SwitchWerkWidgetRenderer(
    private val context: Context,
    private val appWidgetManager: AppWidgetManager,
    private val store: WidgetActionStore,
    private val deviceRepository: DeviceRepository,
    private val switchGroupRepository: SwitchGroupRepository,
    private val wifiProfileRepository: WifiProfileRepository,
    private val wifiProximityService: WifiProximityService
) {
    private val proximityStatusesByWidget =
        ConcurrentHashMap<Int, Map<String, DeviceWifiProximityStatus>>()

    suspend fun updateAllWidgets() {
        store.getWidgetIds().forEach { appWidgetId ->
            updateWidget(appWidgetId)
        }
    }

    suspend fun updateWidget(appWidgetId: Int) {
        updateWidget(appWidgetId, refreshProximity = true)
    }

    suspend fun updateWidgetFast(appWidgetId: Int) {
        updateWidget(appWidgetId, refreshProximity = false)
    }

    private suspend fun updateWidget(appWidgetId: Int, refreshProximity: Boolean) {
        val physicalDimensions = appWidgetManager.getAppWidgetOptions(appWidgetId).toDimensions()
        val dimensions = physicalDimensions
            .withColumnMode(store.getColumnMode(appWidgetId))
        val devices = deviceRepository.getDevices()
        val groups = switchGroupRepository.getSwitchGroups()
        val proximityStatuses = if (refreshProximity) {
            resolveWidgetWifiProximityStatuses(
                devices = devices,
                groups = groups,
                wifiProfiles = wifiProfileRepository.getWifiProfiles(),
                snapshot = wifiProximityService.refresh()
            ).also { proximityStatusesByWidget[appWidgetId] = it }
        } else {
            proximityStatusesByWidget[appWidgetId].orEmpty()
        }
        val actions = resolveWidgetActions(
            assignedTargets = store.getTargets(appWidgetId),
            devices = devices,
            switchGroups = groups,
            maximumEntries = dimensions.maximumEntries
        )
        appWidgetManager.updateAppWidget(
            appWidgetId,
            createRemoteViews(
                appWidgetId,
                actions,
                dimensions,
                proximityStatuses,
                showTitle = physicalDimensions.shouldShowTitle(store.getTitle(appWidgetId))
            )
        )
    }

    private fun createRemoteViews(
        appWidgetId: Int,
        actions: List<ResolvedWidgetAction>,
        dimensions: WidgetDimensions,
        proximityStatuses: Map<String, DeviceWifiProximityStatus>,
        showTitle: Boolean
    ): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_switchwerk)
        views.setTextViewText(R.id.widget_title, store.getTitle(appWidgetId))
        views.setViewVisibility(R.id.widget_title, if (showTitle) View.VISIBLE else View.GONE)
        views.removeAllViews(R.id.widget_left_column)
        views.removeAllViews(R.id.widget_right_column)

        val hasActions = actions.isNotEmpty()
        views.setViewVisibility(R.id.widget_empty, if (hasActions) View.GONE else View.VISIBLE)
        views.setViewVisibility(R.id.widget_actions_container, if (hasActions) View.VISIBLE else View.GONE)
        views.setViewVisibility(
            R.id.widget_right_column,
            if (dimensions.columns == 2 && hasActions) View.VISIBLE else View.GONE
        )
        if (!hasActions) {
            views.setTextViewText(R.id.widget_empty, context.getString(R.string.widget_empty))
            views.setOnClickPendingIntent(R.id.widget_empty, configurationPendingIntent(appWidgetId))
            return views
        }

        actions.forEachIndexed { index, action ->
            val item = createActionRemoteViews(appWidgetId, index, action, proximityStatuses)
            val columnId = if (dimensions.columns == 2 && index % 2 == 1) {
                R.id.widget_right_column
            } else {
                R.id.widget_left_column
            }
            views.addView(columnId, item)
        }
        return views
    }

    private fun createActionRemoteViews(
        appWidgetId: Int,
        entryIndex: Int,
        action: ResolvedWidgetAction,
        proximityStatuses: Map<String, DeviceWifiProximityStatus>
    ): RemoteViews {
        val item = RemoteViews(context.packageName, R.layout.widget_action_item)
        when (action) {
            is ResolvedWidgetAction.Executable -> {
                item.setTextViewText(R.id.widget_action_title, action.title)
                item.setTextViewText(R.id.widget_action_subtitle, statusText(appWidgetId, entryIndex, action.subtitle))
                item.setOnClickPendingIntent(
                    R.id.widget_action_root,
                    actionPendingIntent(appWidgetId, entryIndex)
                )
                item.setInt(
                    R.id.widget_action_root,
                    "setBackgroundResource",
                    actionBackgroundResource(appWidgetId, entryIndex, action.target, proximityStatuses)
                )
            }

            is ResolvedWidgetAction.Unavailable -> {
                item.setTextViewText(R.id.widget_action_title, context.getString(R.string.widget_action_unavailable))
                item.setTextViewText(R.id.widget_action_subtitle, context.getString(R.string.widget_action_removed))
                item.setInt(R.id.widget_action_root, "setBackgroundResource", R.drawable.widget_action_unavailable_background)
            }
        }
        return item
    }

    private fun actionBackgroundResource(
        appWidgetId: Int,
        entryIndex: Int,
        target: WidgetActionTarget,
        proximityStatuses: Map<String, DeviceWifiProximityStatus>
    ): Int {
        return when (store.getStatus(appWidgetId, entryIndex)) {
            WidgetActionStatus.SUCCESS -> R.drawable.widget_action_success_feedback_background
            WidgetActionStatus.ERROR -> R.drawable.widget_action_error_feedback_background
            WidgetActionStatus.RUNNING -> R.drawable.widget_action_unavailable_background
            WidgetActionStatus.IDLE -> {
                when (proximityStatuses[target.statusKey()]) {
                    DeviceWifiProximityStatus.NEARBY -> R.drawable.widget_action_success_background
                    DeviceWifiProximityStatus.NOT_NEARBY,
                    DeviceWifiProximityStatus.WIFI_DISABLED,
                    DeviceWifiProximityStatus.PERMISSION_DENIED,
                    DeviceWifiProximityStatus.SCAN_FAILED -> R.drawable.widget_action_error_background
                    DeviceWifiProximityStatus.UNKNOWN,
                    DeviceWifiProximityStatus.NO_ASSIGNMENT,
                    DeviceWifiProximityStatus.LOCATION_SERVICES_DISABLED,
                    null -> R.drawable.widget_action_background
                }
            }
        }
    }

    private fun statusText(
        appWidgetId: Int,
        entryIndex: Int,
        subtitle: String
    ): String {
        return when (store.getStatus(appWidgetId, entryIndex)) {
            WidgetActionStatus.IDLE -> subtitle
            WidgetActionStatus.RUNNING -> context.getString(R.string.widget_status_running)
            WidgetActionStatus.SUCCESS -> context.getString(R.string.widget_status_success)
            WidgetActionStatus.ERROR -> context.getString(R.string.widget_status_error)
        }
    }

    private fun actionPendingIntent(appWidgetId: Int, entryIndex: Int): PendingIntent {
        val intent = Intent(context, WidgetActionExecutionService::class.java)
            .setAction(WidgetActionExecutionService.ACTION_RUN_WIDGET_ACTION)
            .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            .putExtra(WidgetActionExecutionService.EXTRA_ENTRY_INDEX, entryIndex)
            .setData(Uri.parse("switchwerk://widget/$appWidgetId/$entryIndex"))
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_IMMUTABLE
            } else {
                0
            }
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PendingIntent.getForegroundService(context, appWidgetId * 100 + entryIndex, intent, flags)
        } else {
            PendingIntent.getService(context, appWidgetId * 100 + entryIndex, intent, flags)
        }
    }

    private fun configurationPendingIntent(appWidgetId: Int): PendingIntent {
        val intent = Intent(context, WidgetConfigurationActivity::class.java)
            .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            .setData(Uri.parse("switchwerk://widget/$appWidgetId/edit"))
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_IMMUTABLE
            } else {
                0
            }
        return PendingIntent.getActivity(context, appWidgetId, intent, flags)
    }
}

data class WidgetDimensions(
    val columns: Int,
    val rows: Int
) {
    val maximumEntries: Int = columns * rows
}

internal fun WidgetDimensions.withColumnMode(mode: WidgetColumnMode): WidgetDimensions {
    val selectedColumns = when (mode) {
        WidgetColumnMode.AUTO -> columns
        WidgetColumnMode.ONE -> 1
        WidgetColumnMode.TWO -> 2
    }
    return copy(columns = selectedColumns)
}

internal fun WidgetDimensions.shouldShowTitle(title: String): Boolean {
    return maximumEntries > 1 && title.isNotBlank()
}

internal fun android.os.Bundle.toWidgetDimensions(): WidgetDimensions {
    return widgetDimensionsForOptions(
        minWidth = getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, DEFAULT_CELL_DP),
        minHeight = getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, DEFAULT_CELL_DP),
        maxWidth = getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, DEFAULT_CELL_DP),
        maxHeight = getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, DEFAULT_CELL_DP)
    )
}

internal fun widgetDimensionsForOptions(
    minWidth: Int,
    minHeight: Int,
    maxWidth: Int,
    maxHeight: Int
): WidgetDimensions {
    val width = minWidth
    val height = maxOf(minHeight, maxHeight)
    val columns = if (width >= TWO_COLUMN_MIN_WIDTH_DP) 2 else 1
    val rows = widgetCells(height).coerceAtLeast(1)
    return WidgetDimensions(columns = columns, rows = rows.coerceAtMost(MAX_ROWS))
}

private fun android.os.Bundle.toDimensions(): WidgetDimensions = toWidgetDimensions()

private fun widgetCells(sizeDp: Int): Int {
    return ((sizeDp + CELL_SIZE_OFFSET_DP) / CELL_SIZE_DP).coerceAtLeast(1)
}

private const val DEFAULT_CELL_DP = 40
private const val TWO_COLUMN_MIN_WIDTH_DP = 110
private const val CELL_SIZE_DP = 70
private const val CELL_SIZE_OFFSET_DP = 30
private const val MAX_ROWS = 6

private fun WidgetActionTarget.statusKey(): String {
    return when (type) {
        WidgetActionTargetType.DEVICE -> id
        WidgetActionTargetType.SWITCH_GROUP -> DashboardItem.groupKey(id)
    }
}

private fun resolveWidgetWifiProximityStatuses(
    devices: List<de.piecha.switchwerk.domain.model.Device>,
    groups: List<de.piecha.switchwerk.domain.model.SwitchGroup>,
    wifiProfiles: List<de.piecha.switchwerk.domain.model.WifiProfile>,
    snapshot: de.piecha.switchwerk.data.network.WifiProximitySnapshot
): Map<String, DeviceWifiProximityStatus> {
    val deviceStatuses = resolveWifiProximityStatuses(devices, wifiProfiles, snapshot)
    val deviceIds = devices.map { it.id }.toSet()
    val groupStatuses = groups.associate { group ->
        val memberStatuses = group.members.mapNotNull { member ->
            member.deviceId.takeIf(deviceIds::contains)?.let { deviceStatuses[it] }
        }
        val status = when {
            memberStatuses.isEmpty() -> DeviceWifiProximityStatus.NO_ASSIGNMENT
            memberStatuses.any { it.isNegativeWidgetWifiStatus() } -> DeviceWifiProximityStatus.NOT_NEARBY
            memberStatuses.all { it == DeviceWifiProximityStatus.NEARBY } -> DeviceWifiProximityStatus.NEARBY
            else -> DeviceWifiProximityStatus.UNKNOWN
        }
        DashboardItem.groupKey(group.id) to status
    }
    return deviceStatuses + groupStatuses
}

private fun DeviceWifiProximityStatus.isNegativeWidgetWifiStatus(): Boolean {
    return when (this) {
        DeviceWifiProximityStatus.NOT_NEARBY,
        DeviceWifiProximityStatus.WIFI_DISABLED,
        DeviceWifiProximityStatus.PERMISSION_DENIED,
        DeviceWifiProximityStatus.SCAN_FAILED -> true
        DeviceWifiProximityStatus.NEARBY,
        DeviceWifiProximityStatus.UNKNOWN,
        DeviceWifiProximityStatus.NO_ASSIGNMENT,
        DeviceWifiProximityStatus.LOCATION_SERVICES_DISABLED -> false
    }
}
