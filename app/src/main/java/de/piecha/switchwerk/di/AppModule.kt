package de.piecha.switchwerk.di

import android.content.Context
import android.net.ConnectivityManager
import androidx.room.Room
import de.piecha.switchwerk.data.local.AppDatabase
import de.piecha.switchwerk.data.network.AndroidWifiConnectionService
import de.piecha.switchwerk.data.network.WifiConnectionService
import de.piecha.switchwerk.data.repository.DeviceRepository
import de.piecha.switchwerk.data.repository.RoomDeviceRepository
import de.piecha.switchwerk.data.repository.RoomWifiProfileRepository
import de.piecha.switchwerk.data.repository.WifiProfileRepository
import de.piecha.switchwerk.data.security.EncryptedWifiCredentialStore
import de.piecha.switchwerk.data.security.WifiCredentialStore
import de.piecha.switchwerk.viewmodel.MainViewModel
import de.piecha.switchwerk.viewmodel.SettingsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single {
        Room.databaseBuilder(
            context = androidContext(),
            klass = AppDatabase::class.java,
            name = "switchwerk.db"
        ).build()
    }

    single { get<AppDatabase>().deviceDao() }
    single { get<AppDatabase>().deviceConnectionDao() }
    single { get<AppDatabase>().wifiProfileDao() }

    single {
        androidContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    single<WifiConnectionService> {
        AndroidWifiConnectionService(
            connectivityManager = get()
        )
    }

    single<DeviceRepository> {
        RoomDeviceRepository(
            deviceDao = get(),
            deviceConnectionDao = get()
        )
    }

    single<WifiCredentialStore> {
        EncryptedWifiCredentialStore(
            context = androidContext()
        )
    }

    single<WifiProfileRepository> {
        RoomWifiProfileRepository(
            wifiProfileDao = get(),
            credentialStore = get()
        )
    }

    viewModel {
        MainViewModel(get())
    }

    viewModel {
        SettingsViewModel(
            wifiProfileRepository = get(),
            deviceRepository = get()
        )
    }
}
