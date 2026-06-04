package de.piecha.switchwerk.di

import de.piecha.switchwerk.data.repository.DeviceRepository
import de.piecha.switchwerk.data.repository.FakeDeviceRepository
import de.piecha.switchwerk.viewmodel.MainViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single<DeviceRepository> {
        FakeDeviceRepository()
    }

    viewModel {
        MainViewModel(get())
    }
}
