package de.piecha.switchwerk

import android.app.Application
import de.piecha.switchwerk.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import de.piecha.switchwerk.shortcut.AppShortcutCoordinator

class SwitchWerkApp : Application() {
    override fun onCreate() {
        super.onCreate()

        val koin = startKoin {
            androidContext(this@SwitchWerkApp)
            modules(appModule)
        }.koin
        koin.get<AppShortcutCoordinator>().start()
    }
}
