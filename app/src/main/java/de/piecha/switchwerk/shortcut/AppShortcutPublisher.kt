package de.piecha.switchwerk.shortcut

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import de.piecha.switchwerk.MainActivity
import de.piecha.switchwerk.R
import de.piecha.switchwerk.domain.model.Device

interface AppShortcutPublisher {
    fun publish(devices: List<Device>)
}

class AndroidAppShortcutPublisher(
    private val context: Context,
    private val shortcutManager: ShortcutManager
) : AppShortcutPublisher {

    override fun publish(devices: List<Device>) {
        val shortcuts = selectAppShortcuts(
            devices = devices,
            maximumCount = minOf(shortcutManager.maxShortcutCountPerActivity, RECOMMENDED_MAXIMUM)
        ).map { shortcut ->
            ShortcutInfo.Builder(context, "device:${shortcut.deviceId}")
                .setShortLabel(shortcut.shortLabel)
                .setLongLabel(shortcut.longLabel)
                .setIcon(Icon.createWithResource(context, R.mipmap.ic_launcher))
                .setIntent(
                    Intent(context, MainActivity::class.java)
                        .setAction(APP_SHORTCUT_ACTION)
                        .putExtra(APP_SHORTCUT_DEVICE_ID, shortcut.deviceId)
                )
                .build()
        }
        shortcutManager.dynamicShortcuts = shortcuts
    }

    private companion object {
        const val RECOMMENDED_MAXIMUM = 4
    }
}
