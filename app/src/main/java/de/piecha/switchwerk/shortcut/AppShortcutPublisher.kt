package de.piecha.switchwerk.shortcut

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import de.piecha.switchwerk.MainActivity
import de.piecha.switchwerk.R
import de.piecha.switchwerk.domain.model.Device
import de.piecha.switchwerk.domain.model.SwitchGroup

interface AppShortcutPublisher {
    fun publish(devices: List<Device>, switchGroups: List<SwitchGroup>)
}

class AndroidAppShortcutPublisher(
    private val context: Context,
    private val shortcutManager: ShortcutManager
) : AppShortcutPublisher {

    override fun publish(devices: List<Device>, switchGroups: List<SwitchGroup>) {
        val shortcuts = selectAppShortcuts(
            devices = devices,
            switchGroups = switchGroups,
            maximumCount = minOf(shortcutManager.maxShortcutCountPerActivity, RECOMMENDED_MAXIMUM)
        ).map { shortcut ->
            ShortcutInfo.Builder(context, shortcut.shortcutId)
                .setShortLabel(shortcut.shortLabel)
                .setLongLabel(shortcut.longLabel)
                .setIcon(Icon.createWithResource(context, R.mipmap.ic_launcher))
                .setIntent(shortcut.toIntent())
                .build()
        }
        shortcutManager.dynamicShortcuts = shortcuts
    }

    private fun AppShortcut.toIntent(): Intent {
        val intent = Intent(context, MainActivity::class.java)
            .setAction(APP_SHORTCUT_ACTION)
        return when (this) {
            is DeviceAppShortcut -> intent.putExtra(APP_SHORTCUT_DEVICE_ID, deviceId)
            is SwitchGroupAppShortcut -> intent.putExtra(APP_SHORTCUT_GROUP_ID, groupId)
        }
    }

    private companion object {
        const val RECOMMENDED_MAXIMUM = 4
    }
}
