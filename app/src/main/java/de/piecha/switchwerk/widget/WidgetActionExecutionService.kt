package de.piecha.switchwerk.widget

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import de.piecha.switchwerk.R
import de.piecha.switchwerk.data.action.DeviceActionResult
import de.piecha.switchwerk.data.action.DeviceActionService
import de.piecha.switchwerk.data.action.SwitchGroupActionResult
import de.piecha.switchwerk.data.action.SwitchGroupActionService
import de.piecha.switchwerk.data.repository.DeviceRepository
import de.piecha.switchwerk.data.repository.SwitchGroupRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class WidgetActionExecutionService : Service() {

    private val store: WidgetActionStore by inject()
    private val deviceRepository: DeviceRepository by inject()
    private val switchGroupRepository: SwitchGroupRepository by inject()
    private val deviceActionService: DeviceActionService by inject()
    private val switchGroupActionService: SwitchGroupActionService by inject()
    private val renderer: SwitchWerkWidgetRenderer by inject()
    private val scope: CoroutineScope by inject()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val appWidgetId = intent?.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID
        val entryIndex = intent?.getIntExtra(EXTRA_ENTRY_INDEX, -1) ?: -1
        if (
            intent?.action != ACTION_RUN_WIDGET_ACTION ||
            appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID ||
            entryIndex < 0
        ) {
            stopSelf(startId)
            return START_NOT_STICKY
        }

        startForeground(FOREGROUND_NOTIFICATION_ID, runningNotification())
        scope.launch(Dispatchers.IO) {
            runCatching {
                executeWidgetAction(appWidgetId, entryIndex)
            }.onFailure {
                store.setStatus(appWidgetId, entryIndex, WidgetActionStatus.ERROR)
                renderer.updateWidget(appWidgetId)
                resetStatusAfterFeedback(appWidgetId, entryIndex)
            }
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf(startId)
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private suspend fun executeWidgetAction(appWidgetId: Int, entryIndex: Int) {
        val target = store.getTargets(appWidgetId).getOrNull(entryIndex)
        if (target == null) {
            store.setStatus(appWidgetId, entryIndex, WidgetActionStatus.ERROR)
            renderer.updateWidget(appWidgetId)
            return
        }

        store.setStatus(appWidgetId, entryIndex, WidgetActionStatus.RUNNING)
        renderer.updateWidget(appWidgetId)

        val devices = deviceRepository.getDevices()
        val groups = switchGroupRepository.getSwitchGroups()
        val result = when (target.type) {
            WidgetActionTargetType.DEVICE -> {
                val device = devices.firstOrNull { it.id == target.id }
                    ?: return markError(appWidgetId, entryIndex)
                deviceActionService.execute(device) == DeviceActionResult.Success
            }

            WidgetActionTargetType.SWITCH_GROUP -> {
                val group = groups.firstOrNull { it.id == target.id }
                    ?: return markError(appWidgetId, entryIndex)
                if (group.members.isEmpty()) {
                    return markError(appWidgetId, entryIndex)
                }
                when (
                    switchGroupActionService.execute(
                        group = group,
                        devices = devices,
                        onDiagnosticEvent = { }
                    )
                ) {
                    SwitchGroupActionResult.Success -> true
                    is SwitchGroupActionResult.SuccessWithFailures,
                    SwitchGroupActionResult.EmptyGroup,
                    is SwitchGroupActionResult.DeviceFailed,
                    is SwitchGroupActionResult.MissingDevice -> false
                }
            }
        }
        store.setStatus(
            appWidgetId = appWidgetId,
            entryIndex = entryIndex,
            status = if (result) WidgetActionStatus.SUCCESS else WidgetActionStatus.ERROR
        )
        renderer.updateWidget(appWidgetId)
        resetStatusAfterFeedback(appWidgetId, entryIndex)
    }

    private suspend fun markError(appWidgetId: Int, entryIndex: Int) {
        store.setStatus(appWidgetId, entryIndex, WidgetActionStatus.ERROR)
        renderer.updateWidget(appWidgetId)
        resetStatusAfterFeedback(appWidgetId, entryIndex)
    }

    private suspend fun resetStatusAfterFeedback(appWidgetId: Int, entryIndex: Int) {
        kotlinx.coroutines.delay(ACTION_FEEDBACK_MILLIS)
        store.setStatus(appWidgetId, entryIndex, WidgetActionStatus.IDLE)
        renderer.updateWidget(appWidgetId)
    }

    private fun runningNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(
                NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    getString(R.string.widget_notification_channel),
                    NotificationManager.IMPORTANCE_LOW
                )
            )
        }
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
        }
        return builder
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(getString(R.string.widget_title))
            .setContentText(getString(R.string.widget_status_running))
            .setOngoing(true)
            .build()
    }

    companion object {
        const val ACTION_RUN_WIDGET_ACTION = "de.piecha.switchwerk.widget.RUN_WIDGET_ACTION"
        const val EXTRA_ENTRY_INDEX = "de.piecha.switchwerk.widget.extra.ENTRY_INDEX"
        private const val NOTIFICATION_CHANNEL_ID = "widget_actions"
        private const val FOREGROUND_NOTIFICATION_ID = 2001
        private const val ACTION_FEEDBACK_MILLIS = 4_000L
    }
}
