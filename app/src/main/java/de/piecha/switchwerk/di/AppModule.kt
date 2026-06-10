package de.piecha.switchwerk.di

import androidx.room.Room
import de.piecha.switchwerk.data.local.AppDatabase
import de.piecha.switchwerk.data.repository.DeviceRepository
import de.piecha.switchwerk.data.repository.FakeDeviceRepository
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

    single {
        get<AppDatabase>().wifiProfileDao()
    }

    single<DeviceRepository> {
        FakeDeviceRepository()
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
        SettingsViewModel(get())
    }
}
