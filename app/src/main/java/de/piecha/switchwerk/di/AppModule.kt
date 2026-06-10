package de.piecha.switchwerk.di

import de.piecha.switchwerk.data.repository.DeviceRepository
import de.piecha.switchwerk.data.repository.FakeDeviceRepository
import de.piecha.switchwerk.data.repository.FakeWifiProfileRepository
import de.piecha.switchwerk.data.repository.WifiProfileRepository
import de.piecha.switchwerk.data.security.EncryptedWifiCredentialStore
import de.piecha.switchwerk.data.security.WifiCredentialStore
import de.piecha.switchwerk.viewmodel.MainViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single<DeviceRepository> {
        FakeDeviceRepository()
    }

    single<WifiProfileRepository> {
        FakeWifiProfileRepository()
    }

    single<WifiCredentialStore> {
        EncryptedWifiCredentialStore(
            context = androidContext()
        )
    }

    viewModel {
        MainViewModel(get())
    }
}
