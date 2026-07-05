package de.piecha.switchwerk.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import org.koin.core.context.GlobalContext

open class BaseSwitchWerkWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val renderer = GlobalContext.get().get<SwitchWerkWidgetRenderer>()
        val scope = GlobalContext.get().get<kotlinx.coroutines.CoroutineScope>()
        scope.launchWidgetUpdate {
            appWidgetIds.forEach { appWidgetId ->
                renderer.updateWidget(appWidgetId)
            }
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: android.os.Bundle
    ) {
        val renderer = GlobalContext.get().get<SwitchWerkWidgetRenderer>()
        val scope = GlobalContext.get().get<kotlinx.coroutines.CoroutineScope>()
        scope.launchWidgetUpdate {
            renderer.updateWidget(appWidgetId)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        val store = GlobalContext.get().get<WidgetActionStore>()
        appWidgetIds.forEach(store::deleteWidget)
    }

}

class SwitchWerkWidgetProvider1x1 : BaseSwitchWerkWidgetProvider()

class SwitchWerkWidgetProvider1x2 : BaseSwitchWerkWidgetProvider()

class SwitchWerkWidgetProvider2x1 : BaseSwitchWerkWidgetProvider()
