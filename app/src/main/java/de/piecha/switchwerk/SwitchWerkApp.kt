package de.piecha.switchwerk

import android.app.Application
import de.piecha.switchwerk.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class SwitchWerkApp : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@SwitchWerkApp)
            modules(appModule)
        }
    }
}
