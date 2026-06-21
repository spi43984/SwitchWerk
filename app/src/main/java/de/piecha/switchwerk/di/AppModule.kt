package de.piecha.switchwerk.di

import android.content.Context
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import androidx.room.Room
import de.piecha.switchwerk.data.action.DefaultDeviceActionService
import de.piecha.switchwerk.data.action.DeviceActionService
import de.piecha.switchwerk.data.local.AppDatabase
import de.piecha.switchwerk.data.network.AndroidWifiConnectionService
import de.piecha.switchwerk.data.network.HttpApiCallService
import de.piecha.switchwerk.data.network.OkHttpApiCallService
import de.piecha.switchwerk.data.network.WifiConnectionService
import de.piecha.switchwerk.data.repository.ConfigurationTransferRepository
import de.piecha.switchwerk.data.repository.AppSettingsRepository
import de.piecha.switchwerk.data.repository.DefaultConfigurationTransferRepository
import de.piecha.switchwerk.data.repository.DeviceRepository
import de.piecha.switchwerk.data.repository.RoomDeviceRepository
import de.piecha.switchwerk.data.repository.RoomWifiProfileRepository
import de.piecha.switchwerk.data.repository.SharedPreferencesAppSettingsRepository
import de.piecha.switchwerk.data.repository.WifiProfileRepository
import de.piecha.switchwerk.data.security.EncryptedWifiCredentialStore
import de.piecha.switchwerk.data.security.WifiCredentialStore
import de.piecha.switchwerk.data.transfer.ConfigurationImportValidator
import de.piecha.switchwerk.data.transfer.ConfigurationJsonCodec
import de.piecha.switchwerk.viewmodel.MainViewModel
import de.piecha.switchwerk.viewmodel.SettingsViewModel
import de.piecha.switchwerk.ui.AndroidStringProvider
import de.piecha.switchwerk.ui.StringProvider
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import okhttp3.OkHttpClient

val appModule = module {
    single {
        Room.databaseBuilder(
            context = androidContext(),
            klass = AppDatabase::class.java,
            name = "switchwerk.db"
        )
            .addMigrations(AppDatabase.MIGRATION_1_2)
            .addMigrations(AppDatabase.MIGRATION_2_3)
            .addMigrations(AppDatabase.MIGRATION_3_4)
            .addMigrations(AppDatabase.MIGRATION_4_5)
            .build()
    }

    single { get<AppDatabase>().deviceDao() }
    single { get<AppDatabase>().deviceConnectionDao() }
    single { get<AppDatabase>().wifiProfileDao() }

    single {
        androidContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    single {
        androidContext().getSystemService(Context.WIFI_SERVICE) as WifiManager
    }

    single<WifiConnectionService> {
        AndroidWifiConnectionService(
            connectivityManager = get(),
            wifiManager = get()
        )
    }

    single { OkHttpClient() }

    single { ConfigurationJsonCodec() }
    single { ConfigurationImportValidator() }

    single<AppSettingsRepository> {
        SharedPreferencesAppSettingsRepository(androidContext())
    }

    single<StringProvider> { AndroidStringProvider(androidContext()) }

    single<HttpApiCallService> {
        OkHttpApiCallService(
            baseClient = get()
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

    single<ConfigurationTransferRepository> {
        DefaultConfigurationTransferRepository(
            contentResolver = androidContext().contentResolver,
            database = get(),
            deviceDao = get(),
            deviceConnectionDao = get(),
            wifiProfileDao = get(),
            credentialStore = get(),
            httpClient = get(),
            jsonCodec = get(),
            validator = get(),
            appSettingsRepository = get()
        )
    }

    single<DeviceActionService> {
        DefaultDeviceActionService(
            wifiProfileRepository = get(),
            wifiConnectionService = get(),
            httpApiCallService = get()
        )
    }

    viewModel {
        MainViewModel(
            repository = get(),
            deviceActionService = get(),
            appSettingsRepository = get()
        )
    }

    viewModel {
        SettingsViewModel(
            wifiProfileRepository = get(),
            deviceRepository = get(),
            configurationTransferRepository = get(),
            appSettingsRepository = get(),
            stringProvider = get()
        )
    }
}
