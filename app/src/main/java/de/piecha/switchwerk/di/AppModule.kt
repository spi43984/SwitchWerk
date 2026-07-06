package de.piecha.switchwerk.di

import android.content.Context
import android.appwidget.AppWidgetManager
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.location.LocationManager
import android.content.pm.ShortcutManager
import androidx.room.Room
import de.piecha.switchwerk.data.action.DefaultDeviceActionService
import de.piecha.switchwerk.data.action.DefaultSwitchGroupActionService
import de.piecha.switchwerk.data.action.DeviceActionService
import de.piecha.switchwerk.data.action.SwitchGroupActionService
import de.piecha.switchwerk.data.local.AppDatabase
import de.piecha.switchwerk.data.network.AndroidWifiConnectionService
import de.piecha.switchwerk.data.network.AndroidWifiProximityService
import de.piecha.switchwerk.data.network.HttpApiCallService
import de.piecha.switchwerk.data.network.OkHttpApiCallService
import de.piecha.switchwerk.data.network.WifiConnectionService
import de.piecha.switchwerk.data.network.WifiProximityConfirmationStore
import de.piecha.switchwerk.data.network.WifiProximityService
import de.piecha.switchwerk.data.repository.ConfigurationTransferRepository
import de.piecha.switchwerk.data.repository.AppSettingsRepository
import de.piecha.switchwerk.data.repository.DefaultConfigurationTransferRepository
import de.piecha.switchwerk.data.repository.DeviceRepository
import de.piecha.switchwerk.data.repository.RoomDeviceRepository
import de.piecha.switchwerk.data.repository.RoomSwitchGroupRepository
import de.piecha.switchwerk.data.repository.RoomWifiProfileRepository
import de.piecha.switchwerk.data.repository.SharedPreferencesAppSettingsRepository
import de.piecha.switchwerk.data.repository.SwitchGroupRepository
import de.piecha.switchwerk.data.repository.WifiProfileRepository
import de.piecha.switchwerk.data.security.EncryptedWifiCredentialStore
import de.piecha.switchwerk.data.security.WifiCredentialStore
import de.piecha.switchwerk.data.transfer.ConfigurationImportValidator
import de.piecha.switchwerk.data.transfer.ConfigurationJsonCodec
import de.piecha.switchwerk.data.update.AndroidAppUpdateDownloadService
import de.piecha.switchwerk.data.update.AndroidAppUpdateInstallService
import de.piecha.switchwerk.data.update.AppUpdateCacheRepository
import de.piecha.switchwerk.data.update.AppUpdateDownloadService
import de.piecha.switchwerk.data.update.AppUpdateInstallService
import de.piecha.switchwerk.data.update.AppUpdateRepository
import de.piecha.switchwerk.data.update.DefaultAppUpdateRepository
import de.piecha.switchwerk.data.update.GitHubReleaseEvaluator
import de.piecha.switchwerk.data.update.GitHubReleaseService
import de.piecha.switchwerk.data.update.OkHttpGitHubReleaseService
import de.piecha.switchwerk.data.update.SharedPreferencesAppUpdateCacheRepository
import de.piecha.switchwerk.BuildConfig
import de.piecha.switchwerk.viewmodel.MainViewModel
import de.piecha.switchwerk.viewmodel.SettingsViewModel
import de.piecha.switchwerk.ui.AndroidStringProvider
import de.piecha.switchwerk.ui.StringProvider
import de.piecha.switchwerk.shortcut.AndroidAppShortcutPublisher
import de.piecha.switchwerk.shortcut.AppShortcutCoordinator
import de.piecha.switchwerk.shortcut.AppShortcutPublisher
import de.piecha.switchwerk.widget.SharedPreferencesWidgetActionStore
import de.piecha.switchwerk.widget.SwitchWerkWidgetRenderer
import de.piecha.switchwerk.widget.WidgetActionStore
import de.piecha.switchwerk.widget.WidgetConfigurationViewModel
import de.piecha.switchwerk.widget.WidgetUpdateCoordinator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
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
            .addMigrations(AppDatabase.MIGRATION_5_6)
            .addMigrations(AppDatabase.MIGRATION_6_7)
            .addMigrations(AppDatabase.MIGRATION_7_8)
            .addMigrations(AppDatabase.MIGRATION_8_9)
            .addMigrations(AppDatabase.MIGRATION_9_10)
            .addMigrations(AppDatabase.MIGRATION_10_11)
            .addMigrations(AppDatabase.MIGRATION_11_12)
            .addMigrations(AppDatabase.MIGRATION_12_13)
            .addMigrations(AppDatabase.MIGRATION_13_14)
            .build()
    }

    single { get<AppDatabase>().deviceDao() }
    single { get<AppDatabase>().deviceConnectionDao() }
    single { get<AppDatabase>().wifiProfileDao() }
    single { get<AppDatabase>().switchGroupDao() }
    single { get<AppDatabase>().switchGroupMemberDao() }

    single {
        androidContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    single {
        androidContext().getSystemService(Context.WIFI_SERVICE) as WifiManager
    }

    single {
        androidContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    single { WifiProximityConfirmationStore() }

    single { androidContext().getSystemService(ShortcutManager::class.java) }
    single { AppWidgetManager.getInstance(androidContext()) }
    single<AppShortcutPublisher> {
        AndroidAppShortcutPublisher(androidContext(), get())
    }
    single { CoroutineScope(SupervisorJob() + Dispatchers.Default) }
    single { AppShortcutCoordinator(get(), get(), get(), get()) }
    single<WidgetActionStore> { SharedPreferencesWidgetActionStore(androidContext()) }
    single {
        SwitchWerkWidgetRenderer(
            context = androidContext(),
            appWidgetManager = get(),
            store = get(),
            deviceRepository = get(),
            switchGroupRepository = get(),
            wifiProfileRepository = get(),
            wifiProximityService = get()
        )
    }
    single { WidgetUpdateCoordinator(get(), get(), get(), get()) }

    single<WifiConnectionService> {
        AndroidWifiConnectionService(
            connectivityManager = get(),
            wifiManager = get(),
            proximityConfirmationStore = get()
        )
    }

    single<WifiProximityService> {
        AndroidWifiProximityService(
            context = androidContext(),
            connectivityManager = get(),
            wifiManager = get(),
            locationManager = get(),
            proximityConfirmationStore = get()
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

    single<GitHubReleaseService> {
        OkHttpGitHubReleaseService(client = get())
    }

    single { GitHubReleaseEvaluator() }

    single<AppUpdateCacheRepository> {
        SharedPreferencesAppUpdateCacheRepository(androidContext())
    }

    single<AppUpdateDownloadService> {
        AndroidAppUpdateDownloadService(
            context = androidContext(),
            client = get()
        )
    }

    single<AppUpdateInstallService> {
        AndroidAppUpdateInstallService(androidContext())
    }

    single<AppUpdateRepository> {
        DefaultAppUpdateRepository(
            releaseService = get(),
            releaseEvaluator = get(),
            cacheRepository = get(),
            downloadService = get(),
            installedVersion = BuildConfig.VERSION_NAME,
            isDebugBuild = BuildConfig.DEBUG
        )
    }

    single<DeviceRepository> {
        RoomDeviceRepository(
            deviceDao = get(),
            deviceConnectionDao = get(),
            switchGroupMemberDao = get()
        )
    }

    single<SwitchGroupRepository> {
        RoomSwitchGroupRepository(
            switchGroupDao = get(),
            switchGroupMemberDao = get()
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
            deviceConnectionDao = get(),
            credentialStore = get()
        )
    }

    single<ConfigurationTransferRepository> {
        DefaultConfigurationTransferRepository(
            contentResolver = androidContext().contentResolver,
            database = get(),
            deviceDao = get(),
            deviceConnectionDao = get(),
            switchGroupDao = get(),
            switchGroupMemberDao = get(),
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

    single<SwitchGroupActionService> {
        DefaultSwitchGroupActionService(
            deviceActionService = get()
        )
    }

    viewModel {
        MainViewModel(
            repository = get(),
            switchGroupRepository = get(),
            deviceActionService = get(),
            switchGroupActionService = get(),
            appSettingsRepository = get(),
            wifiProfileRepository = get(),
            wifiProximityService = get(),
            appUpdateRepository = get()
        )
    }

    viewModel {
        SettingsViewModel(
            wifiProfileRepository = get(),
            deviceRepository = get(),
            switchGroupRepository = get(),
            configurationTransferRepository = get(),
            appSettingsRepository = get(),
            wifiConnectionService = get(),
            stringProvider = get(),
            appUpdateRepository = get(),
            appUpdateInstallService = get()
        )
    }

    viewModel {
        WidgetConfigurationViewModel(
            deviceRepository = get(),
            switchGroupRepository = get(),
            store = get(),
            renderer = get()
        )
    }
}
